package org.cachyos.controlcenter.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SettingsServiceTest {
  @TempDir Path temporary;

  @Test
  void exportsOnlyExplicitSecretFreeSchemaAndImportsIt() throws Exception {
    SettingsService service = new SettingsService(temporary.resolve("config"));
    Path exported = temporary.resolve("export.json");
    service.exportSettings(exported);
    String json = Files.readString(exported);
    assertFalse(json.toLowerCase(java.util.Locale.ROOT).contains("api_key"));
    assertFalse(json.toLowerCase(java.util.Locale.ROOT).contains("token"));
    service.importSettings(exported);
    assertEquals(ApplicationSettings.defaults(), service.current());

    Path legacy = temporary.resolve("legacy-v1.json");
    Files.writeString(legacy, json.replaceAll("(?m)^\\s*\"aiModel\"\\s*:\\s*\"[^\"]+\",?\\R", ""));
    service.importSettings(legacy);
    assertEquals("gpt-5.6-sol", service.current().aiModel());
  }

  @Test
  void rejectsUnknownImportFieldsAndDeletesOptInHistory() throws Exception {
    SettingsService service = new SettingsService(temporary.resolve("config"));
    ApplicationSettings defaults = ApplicationSettings.defaults();
    service.update(
        new ApplicationSettings(
            defaults.enabledModules(),
            defaults.quickButtons(),
            defaults.microphoneEnabled(),
            defaults.microphoneId(),
            defaults.onlineAiEnabled(),
            defaults.aiProvider(),
            defaults.monthlyBudgetCents(),
            defaults.shareDocumentation(),
            defaults.shareDiagnostics(),
            defaults.shareHardware(),
            defaults.shareSystemContext(),
            true));
    service.recordChat("user", "Lokaler Test");
    assertEquals(1, service.history().size());
    service.clearHistory();
    assertTrue(service.history().isEmpty());

    Path malformed = temporary.resolve("unknown.json");
    Files.writeString(malformed, "{\"unknown\":\"field\"}");
    assertThrows(IllegalArgumentException.class, () -> service.importSettings(malformed));
  }

  @Test
  void rejectsSymlinkAsConfigurationRoot() throws Exception {
    Path real = Files.createDirectory(temporary.resolve("real"));
    Path link = temporary.resolve("linked-config");
    try {
      Files.createSymbolicLink(link, real);
    } catch (UnsupportedOperationException | java.io.IOException exception) {
      Assumptions.assumeTrue(false, "Symlinks sind auf diesem Testsystem nicht verfügbar");
    }
    assertThrows(IllegalArgumentException.class, () -> new SettingsService(link));
  }

  @Test
  void recordsFirstRunWithoutMixingSetupStateIntoExportedSettings() throws Exception {
    SettingsService service = new SettingsService(temporary.resolve("config"));
    assertTrue(service.firstRunRequired());

    service.completeFirstRun();
    assertFalse(service.firstRunRequired());
    Path exported = temporary.resolve("export.json");
    service.exportSettings(exported);
    assertFalse(Files.readString(exported).contains("completedAt"));

    service.deletePersonalData();
    assertTrue(service.firstRunRequired());
  }

  @Test
  void persistsAValidatedUserSelectedAiModel() {
    SettingsService service = new SettingsService(temporary.resolve("config"));
    ApplicationSettings defaults = service.current();
    service.update(
        new ApplicationSettings(
            defaults.enabledModules(),
            defaults.quickButtons(),
            defaults.microphoneEnabled(),
            defaults.microphoneId(),
            true,
            "openai",
            "gpt-5.6-terra",
            defaults.monthlyBudgetCents(),
            defaults.shareDocumentation(),
            defaults.shareDiagnostics(),
            defaults.shareHardware(),
            defaults.shareSystemContext(),
            defaults.storeChatHistory()));

    assertEquals("gpt-5.6-terra", service.current().aiModel());
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ApplicationSettings(
                defaults.enabledModules(),
                defaults.quickButtons(),
                false,
                "",
                true,
                "openai",
                "untrusted-model",
                500,
                false,
                false,
                false,
                false,
                false));
  }

  @Test
  void persistsSettingsHistoryAndUsageInSqlite() {
    Path config = temporary.resolve("config");
    Path data = temporary.resolve("data");
    SettingsService service = new SettingsService(config, data);
    ApplicationSettings defaults = service.current();
    service.update(
        new ApplicationSettings(
            defaults.enabledModules(),
            defaults.quickButtons(),
            defaults.microphoneEnabled(),
            defaults.microphoneId(),
            true,
            "openai",
            "gpt-5.6-luna",
            500,
            defaults.shareDocumentation(),
            defaults.shareDiagnostics(),
            defaults.shareHardware(),
            defaults.shareSystemContext(),
            true));
    service.recordChat("user", "Persistenter Test");
    service.recordAiUsage("gpt-5.6-luna", 1_000, 500);

    SettingsService reopened = new SettingsService(config, data);

    assertEquals("gpt-5.6-luna", reopened.current().aiModel());
    assertEquals(1, reopened.history().size());
    assertEquals(1, reopened.currentMonthUsage().requests());
    assertTrue(Files.isRegularFile(reopened.databaseFile()));
    assertFalse(Files.exists(config.resolve("settings.json")));
  }

  @Test
  void migratesLegacyJsonSettingsIntoSqlite() throws Exception {
    Path sourceConfig = temporary.resolve("source");
    SettingsService source = new SettingsService(sourceConfig);
    ApplicationSettings defaults = source.current();
    source.update(
        new ApplicationSettings(
            defaults.enabledModules(),
            defaults.quickButtons(),
            true,
            "default",
            defaults.onlineAiEnabled(),
            defaults.aiProvider(),
            defaults.aiModel(),
            defaults.monthlyBudgetCents(),
            defaults.shareDocumentation(),
            defaults.shareDiagnostics(),
            defaults.shareHardware(),
            defaults.shareSystemContext(),
            defaults.storeChatHistory()));
    Path targetConfig = temporary.resolve("legacy-config");
    Files.createDirectories(targetConfig);
    source.exportSettings(targetConfig.resolve("settings.json"));

    SettingsService migrated =
        new SettingsService(targetConfig, temporary.resolve("migrated-data"));

    assertTrue(migrated.current().microphoneEnabled());
    assertFalse(Files.exists(targetConfig.resolve("settings.json")));
    assertTrue(Files.isRegularFile(migrated.databaseFile()));
  }

  @Test
  void createsTheRequiredPersistentSchema() throws Exception {
    SettingsService service =
        new SettingsService(temporary.resolve("config"), temporary.resolve("data"));
    Set<String> tables = new HashSet<>();
    try (var connection = DriverManager.getConnection("jdbc:sqlite:" + service.databaseFile());
        var statement =
            connection.prepareStatement(
                "SELECT name FROM sqlite_master WHERE type = 'table' ORDER BY name");
        var rows = statement.executeQuery()) {
      while (rows.next()) {
        tables.add(rows.getString(1));
      }
    }

    assertTrue(
        tables.containsAll(
            Set.of(
                "settings",
                "module_preferences",
                "quick_actions",
                "action_history",
                "diagnostic_runs",
                "chat_sessions",
                "chat_messages",
                "knowledge_sources",
                "knowledge_documents",
                "knowledge_chunks",
                "ai_usage",
                "schema_migrations")));
  }
}
