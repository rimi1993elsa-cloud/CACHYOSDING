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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/** Atomic, symlink-resistant local settings and bounded opt-in chat history. */
public final class SettingsService {
  private static final int MAXIMUM_IMPORT_BYTES = 64 * 1024;
  private static final int MAXIMUM_HISTORY = 200;
  private static final Set<PosixFilePermission> PRIVATE_FILE =
      Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
  private final Path configDirectory;
  private final Path settingsFile;
  private final Path historyFile;
  private final Path firstRunMarker;
  private final ObjectMapper mapper =
      JsonMapper.builder()
          .addModule(new JavaTimeModule())
          .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
          .build();
  private final AtomicReference<ApplicationSettings> settings;
  private final CopyOnWriteArrayList<ChatHistoryEntry> history;

  public SettingsService(Path configDirectory) {
    this.configDirectory = configDirectory.toAbsolutePath().normalize();
    if (Files.exists(this.configDirectory, LinkOption.NOFOLLOW_LINKS)
        && (!Files.isDirectory(this.configDirectory, LinkOption.NOFOLLOW_LINKS)
            || Files.isSymbolicLink(this.configDirectory))) {
      throw new IllegalArgumentException("Unsicheres XDG-Konfigurationsverzeichnis");
    }
    settingsFile = this.configDirectory.resolve("settings.json");
    historyFile = this.configDirectory.resolve("chat-history.json");
    firstRunMarker = this.configDirectory.resolve("setup-v1.complete");
    settings = new AtomicReference<>(loadSettings());
    history = new CopyOnWriteArrayList<>(loadHistory());
  }

  public ApplicationSettings current() {
    return settings.get();
  }

  public synchronized void update(ApplicationSettings updated) {
    write(settingsFile, updated);
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
    write(historyFile, List.copyOf(history));
  }

  public List<ChatHistoryEntry> history() {
    return List.copyOf(history);
  }

  public synchronized void clearHistory() {
    history.clear();
    deleteRegularFile(historyFile);
  }

  public synchronized void deletePersonalData() {
    clearHistory();
    deleteRegularFile(settingsFile);
    deleteRegularFile(firstRunMarker);
    settings.set(ApplicationSettings.defaults());
  }

  public boolean firstRunRequired() {
    return !safeExistingFile(firstRunMarker);
  }

  public synchronized void completeFirstRun() {
    write(firstRunMarker, java.util.Map.of("schema", 1, "completedAt", Instant.now().toString()));
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
    if (!safeExistingFile(historyFile) || !currentForLoad().storeChatHistory()) {
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
