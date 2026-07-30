package org.cachyos.controlcenter.persistence;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/** SQLite-backed settings, bounded opt-in chat history and secret-free JSON transfer. */
public final class SettingsService {
  private static final int MAXIMUM_IMPORT_BYTES = 64 * 1024;
  private static final int MAXIMUM_HISTORY = 200;
  private static final Set<PosixFilePermission> PRIVATE_FILE =
      Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
  private final Path configDirectory;
  private final Path settingsFile;
  private final Path historyFile;
  private final Path firstRunMarker;
  private final SqliteDatabase database;
  private final ObjectMapper mapper =
      JsonMapper.builder()
          .addModule(new JavaTimeModule())
          .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
          .build();
  private final AtomicReference<ApplicationSettings> settings;
  private final CopyOnWriteArrayList<ChatHistoryEntry> history;

  public SettingsService(Path configDirectory) {
    this(configDirectory, configDirectory);
  }

  public SettingsService(Path configDirectory, Path dataDirectory) {
    this.configDirectory = configDirectory.toAbsolutePath().normalize();
    if (Files.exists(this.configDirectory, LinkOption.NOFOLLOW_LINKS)
        && (!Files.isDirectory(this.configDirectory, LinkOption.NOFOLLOW_LINKS)
            || Files.isSymbolicLink(this.configDirectory))) {
      throw new IllegalArgumentException("Unsicheres XDG-Konfigurationsverzeichnis");
    }
    settingsFile = this.configDirectory.resolve("settings.json");
    historyFile = this.configDirectory.resolve("chat-history.json");
    firstRunMarker = this.configDirectory.resolve("setup-v1.complete");
    database =
        new SqliteDatabase(
            dataDirectory.toAbsolutePath().normalize().resolve("cachyos-control-center.sqlite3"));
    settings = new AtomicReference<>(loadSettings());
    history = new CopyOnWriteArrayList<>(loadHistory());
    migrateLegacyFiles();
  }

  public ApplicationSettings current() {
    return settings.get();
  }

  public synchronized void update(ApplicationSettings updated) {
    saveSetting("application-settings", serialize(updated));
    settings.set(updated);
    if (!updated.storeChatHistory()) {
      clearHistory();
    }
  }

  public void recordChat(String role, String text) {
    if (!current().storeChatHistory()) {
      return;
    }
    history.add(new ChatHistoryEntry(Instant.now(), role, text));
    while (history.size() > MAXIMUM_HISTORY) {
      history.removeFirst();
    }
    insertChat(history.getLast());
  }

  public List<ChatHistoryEntry> history() {
    return List.copyOf(history);
  }

  public synchronized void clearHistory() {
    history.clear();
    executeUpdate("DELETE FROM chat_messages");
    executeUpdate("DELETE FROM chat_sessions");
    deleteRegularFile(historyFile);
  }

  public synchronized void deletePersonalData() {
    clearHistory();
    clearAiUsage();
    executeUpdate("DELETE FROM action_history");
    executeUpdate("DELETE FROM settings");
    deleteRegularFile(firstRunMarker);
    deleteRegularFile(settingsFile);
    settings.set(ApplicationSettings.defaults());
  }

  public boolean firstRunRequired() {
    return loadSetting("setup-complete").isEmpty() && !safeExistingFile(firstRunMarker);
  }

  public synchronized void completeFirstRun() {
    saveSetting("setup-complete", Instant.now().toString());
    deleteRegularFile(firstRunMarker);
  }

  public void exportSettings(Path destination) {
    requireSafeDestination(destination);
    write(destination.toAbsolutePath().normalize(), current());
  }

  public synchronized void importSettings(Path source) {
    Path normalized = source.toAbsolutePath().normalize();
    if (!Files.isRegularFile(normalized, LinkOption.NOFOLLOW_LINKS)
        || Files.isSymbolicLink(normalized)) {
      throw new IllegalArgumentException("Importquelle ist keine reguläre Datei");
    }
    try {
      long size = Files.size(normalized);
      if (size <= 0 || size > MAXIMUM_IMPORT_BYTES) {
        throw new IllegalArgumentException("Importdatei ist leer oder zu groß");
      }
      ApplicationSettings imported =
          mapper.readValue(normalized.toFile(), ApplicationSettings.class);
      update(imported);
    } catch (IOException exception) {
      throw new IllegalArgumentException(
          "Einstellungen konnten nicht importiert werden", exception);
    }
  }

  private ApplicationSettings loadSettings() {
    java.util.Optional<String> stored = loadSetting("application-settings");
    if (stored.isPresent()) {
      try {
        return mapper.readValue(stored.orElseThrow(), ApplicationSettings.class);
      } catch (IOException | IllegalArgumentException ignored) {
        return ApplicationSettings.defaults();
      }
    }
    if (!safeExistingFile(settingsFile)) {
      return ApplicationSettings.defaults();
    }
    try {
      if (Files.size(settingsFile) > MAXIMUM_IMPORT_BYTES) {
        return ApplicationSettings.defaults();
      }
      return mapper.readValue(settingsFile.toFile(), ApplicationSettings.class);
    } catch (IOException | IllegalArgumentException exception) {
      return ApplicationSettings.defaults();
    }
  }

  private List<ChatHistoryEntry> loadHistory() {
    if (!currentForLoad().storeChatHistory()) {
      return List.of();
    }
    List<ChatHistoryEntry> stored = loadSqliteHistory();
    if (!stored.isEmpty()) {
      return stored;
    }
    if (!safeExistingFile(historyFile)) {
      return List.of();
    }
    try {
      if (Files.size(historyFile) > 2L * 1024 * 1024) {
        return List.of();
      }
      ChatHistoryEntry[] entries = mapper.readValue(historyFile.toFile(), ChatHistoryEntry[].class);
      List<ChatHistoryEntry> bounded = new ArrayList<>(List.of(entries));
      return List.copyOf(
          bounded.subList(Math.max(0, bounded.size() - MAXIMUM_HISTORY), bounded.size()));
    } catch (IOException | IllegalArgumentException exception) {
      return List.of();
    }
  }

  private ApplicationSettings currentForLoad() {
    return settings == null ? ApplicationSettings.defaults() : settings.get();
  }

  public synchronized void recordAiUsage(String model, long inputTokens, long outputTokens) {
    if (inputTokens < 0 || outputTokens < 0) {
      throw new IllegalArgumentException("Tokenwerte dürfen nicht negativ sein");
    }
    long estimatedMillicents = estimateMillicents(model, inputTokens, outputTokens);
    String sql =
        """
        INSERT INTO ai_usage(
          occurred_at, model, input_tokens, output_tokens, estimated_millicents
        ) VALUES (?, ?, ?, ?, ?)
        """;
    try (var connection = database.open();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, Instant.now().toString());
      statement.setString(2, model);
      statement.setLong(3, inputTokens);
      statement.setLong(4, outputTokens);
      statement.setLong(5, estimatedMillicents);
      statement.executeUpdate();
    } catch (SQLException exception) {
      throw new IllegalStateException("KI-Nutzung konnte nicht gespeichert werden", exception);
    }
  }

  public AiUsageSummary currentMonthUsage() {
    Instant start =
        LocalDate.now(ZoneOffset.UTC).withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.UTC);
    String sql =
        """
        SELECT COALESCE(SUM(input_tokens), 0), COALESCE(SUM(output_tokens), 0),
               COALESCE(SUM(estimated_millicents), 0), COUNT(*)
        FROM ai_usage WHERE occurred_at >= ?
        """;
    try (var connection = database.open();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, start.toString());
      try (ResultSet row = statement.executeQuery()) {
        return new AiUsageSummary(row.getLong(1), row.getLong(2), row.getLong(3), row.getLong(4));
      }
    } catch (SQLException exception) {
      throw new IllegalStateException("KI-Nutzung konnte nicht gelesen werden", exception);
    }
  }

  public synchronized void clearAiUsage() {
    executeUpdate("DELETE FROM ai_usage");
  }

  public Path databaseFile() {
    return database.file();
  }

  private void migrateLegacyFiles() {
    if (safeExistingFile(settingsFile)) {
      if (loadSetting("application-settings").isEmpty()) {
        saveSetting("application-settings", serialize(settings.get()));
      }
      deleteRegularFile(settingsFile);
    }
    if (safeExistingFile(firstRunMarker)) {
      if (loadSetting("setup-complete").isEmpty()) {
        saveSetting("setup-complete", Instant.now().toString());
      }
      deleteRegularFile(firstRunMarker);
    }
    if (history.isEmpty() || !safeExistingFile(historyFile)) {
      return;
    }
    history.forEach(this::insertChat);
    deleteRegularFile(historyFile);
  }

  private List<ChatHistoryEntry> loadSqliteHistory() {
    List<ChatHistoryEntry> result = new ArrayList<>();
    String sql =
        """
        SELECT occurred_at, role, content FROM chat_messages
        ORDER BY id DESC LIMIT ?
        """;
    try (var connection = database.open();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setInt(1, MAXIMUM_HISTORY);
      try (ResultSet rows = statement.executeQuery()) {
        while (rows.next()) {
          result.add(
              new ChatHistoryEntry(
                  Instant.parse(rows.getString(1)), rows.getString(2), rows.getString(3)));
        }
      }
      java.util.Collections.reverse(result);
      return List.copyOf(result);
    } catch (SQLException | IllegalArgumentException exception) {
      return List.of();
    }
  }

  private void insertChat(ChatHistoryEntry entry) {
    String sessionSql =
        "INSERT INTO chat_sessions(started_at) SELECT ?"
            + " WHERE NOT EXISTS (SELECT 1 FROM chat_sessions)";
    String messageSql =
        """
        INSERT INTO chat_messages(session_id, occurred_at, role, content)
        VALUES ((SELECT id FROM chat_sessions ORDER BY id DESC LIMIT 1), ?, ?, ?)
        """;
    try (var connection = database.open()) {
      try (PreparedStatement session = connection.prepareStatement(sessionSql)) {
        session.setString(1, entry.timestamp().toString());
        session.executeUpdate();
      }
      try (PreparedStatement message = connection.prepareStatement(messageSql)) {
        message.setString(1, entry.timestamp().toString());
        message.setString(2, entry.role());
        message.setString(3, entry.text());
        message.executeUpdate();
      }
      try (PreparedStatement trim =
          connection.prepareStatement(
              "DELETE FROM chat_messages WHERE id NOT IN"
                  + " (SELECT id FROM chat_messages ORDER BY id DESC LIMIT ?)")) {
        trim.setInt(1, MAXIMUM_HISTORY);
        trim.executeUpdate();
      }
    } catch (SQLException exception) {
      throw new IllegalStateException("Chatverlauf konnte nicht gespeichert werden", exception);
    }
  }

  private java.util.Optional<String> loadSetting(String key) {
    try (var connection = database.open();
        PreparedStatement statement =
            connection.prepareStatement("SELECT value FROM settings WHERE key = ?")) {
      statement.setString(1, key);
      try (ResultSet row = statement.executeQuery()) {
        return row.next() ? java.util.Optional.of(row.getString(1)) : java.util.Optional.empty();
      }
    } catch (SQLException exception) {
      throw new IllegalStateException("Einstellung konnte nicht gelesen werden", exception);
    }
  }

  private void saveSetting(String key, String value) {
    String sql =
        """
        INSERT INTO settings(key, value, updated_at) VALUES (?, ?, ?)
        ON CONFLICT(key) DO UPDATE SET value = excluded.value, updated_at = excluded.updated_at
        """;
    try (var connection = database.open();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, key);
      statement.setString(2, value);
      statement.setString(3, Instant.now().toString());
      statement.executeUpdate();
    } catch (SQLException exception) {
      throw new IllegalStateException("Einstellung konnte nicht gespeichert werden", exception);
    }
  }

  private void executeUpdate(String sql) {
    try (var connection = database.open();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.executeUpdate();
    } catch (SQLException exception) {
      throw new IllegalStateException("Lokale Daten konnten nicht geändert werden", exception);
    }
  }

  private String serialize(Object value) {
    try {
      return mapper.writeValueAsString(value);
    } catch (IOException exception) {
      throw new IllegalStateException("Lokale Daten konnten nicht serialisiert werden", exception);
    }
  }

  private static long estimateMillicents(String model, long inputTokens, long outputTokens) {
    long inputTenthsOfMicrodollar;
    long outputTenthsOfMicrodollar;
    switch (model) {
      case "gpt-5.6-sol" -> {
        inputTenthsOfMicrodollar = 50;
        outputTenthsOfMicrodollar = 300;
      }
      case "gpt-5.6-terra" -> {
        inputTenthsOfMicrodollar = 20;
        outputTenthsOfMicrodollar = 120;
      }
      case "gpt-5.6-luna" -> {
        inputTenthsOfMicrodollar = 2;
        outputTenthsOfMicrodollar = 12;
      }
      default -> throw new IllegalArgumentException("Unbekanntes KI-Modell");
    }
    // Official USD token prices captured for release 1.2; tool-call fees are not included.
    return Math.max(
        1,
        Math.addExact(
                Math.multiplyExact(inputTokens, inputTenthsOfMicrodollar),
                Math.multiplyExact(outputTokens, outputTenthsOfMicrodollar))
            / 100);
  }

  private synchronized void write(Path destination, Object value) {
    Path normalized = destination.toAbsolutePath().normalize();
    Path parent = normalized.getParent();
    if (parent == null || Files.isSymbolicLink(parent)) {
      throw new IllegalArgumentException("Unsicheres Zielverzeichnis");
    }
    try {
      Files.createDirectories(parent);
      if (Files.isSymbolicLink(parent)
          || (Files.exists(normalized, LinkOption.NOFOLLOW_LINKS)
              && !safeExistingFile(normalized))) {
        throw new IllegalArgumentException("Unsicheres Dateiziel");
      }
      Path temporary = Files.createTempFile(parent, ".settings-", ".tmp");
      try {
        setPrivatePermissions(temporary);
        mapper.writerWithDefaultPrettyPrinter().writeValue(temporary.toFile(), value);
        moveAtomically(temporary, normalized);
        setPrivatePermissions(normalized);
      } finally {
        Files.deleteIfExists(temporary);
      }
    } catch (IOException exception) {
      throw new IllegalStateException(
          "Lokale Einstellungen konnten nicht gespeichert werden", exception);
    }
  }

  private static void moveAtomically(Path source, Path target) throws IOException {
    try {
      Files.move(
          source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    } catch (AtomicMoveNotSupportedException ignored) {
      Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  private static void setPrivatePermissions(Path file) {
    try {
      Files.setPosixFilePermissions(file, PRIVATE_FILE);
    } catch (UnsupportedOperationException | IOException ignored) {
      // Non-POSIX development hosts do not expose these permissions.
    }
  }

  private static boolean safeExistingFile(Path file) {
    return Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(file);
  }

  private static void requireSafeDestination(Path destination) {
    if (destination == null) {
      throw new IllegalArgumentException("Exportziel fehlt");
    }
    Path normalized = destination.toAbsolutePath().normalize();
    if (Files.isSymbolicLink(normalized)
        || (Files.exists(normalized, LinkOption.NOFOLLOW_LINKS)
            && !Files.isRegularFile(normalized, LinkOption.NOFOLLOW_LINKS))) {
      throw new IllegalArgumentException("Unsicheres Exportziel");
    }
  }

  private static void deleteRegularFile(Path file) {
    try {
      if (safeExistingFile(file)) {
        Files.delete(file);
      }
    } catch (IOException exception) {
      throw new IllegalStateException("Lokale Datei konnte nicht gelöscht werden", exception);
    }
  }
}
