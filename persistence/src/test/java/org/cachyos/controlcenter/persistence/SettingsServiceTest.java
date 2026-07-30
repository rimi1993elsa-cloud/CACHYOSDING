package org.cachyos.controlcenter.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
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
}
