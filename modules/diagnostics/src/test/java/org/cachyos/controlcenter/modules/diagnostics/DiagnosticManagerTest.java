package org.cachyos.controlcenter.modules.diagnostics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.cachyos.controlcenter.core.action.ActionId;
import org.junit.jupiter.api.Test;

class DiagnosticManagerTest {
  @Test
  void sanitizesEveryFindingAndSuggestsOnlyFixedActions() {
    try (DiagnosticManager manager =
        new DiagnosticManager(
            category ->
                new DiagnosticObservation(
                    category,
                    DiagnosticStatus.WARNING,
                    "Problem bei max@example.org",
                    "host 192.168.1.42 /home/max/datei sk-secretvalue123"))) {
      DiagnosticReport report = manager.runAll().join();

      assertEquals(DiagnosticCategory.values().length, report.findings().size());
      assertFalse(report.asSanitizedText().contains("max@example.org"));
      assertFalse(report.asSanitizedText().contains("192.168.1.42"));
      assertFalse(report.asSanitizedText().contains("sk-secretvalue123"));
      assertEquals(
          ActionId.NETWORK_SCAN_WIFI, report.findings().getFirst().suggestedAction().orElseThrow());
      assertTrue(
          report.findings().stream()
              .filter(finding -> finding.category() == DiagnosticCategory.SERVICES)
              .findFirst()
              .orElseThrow()
              .suggestedAction()
              .isEmpty());
    }
  }

  @Test
  void sanitizerMasksWindowsAndLinuxHomePaths() {
    String sanitized = DiagnosticSanitizer.sanitize("C:\\Users\\Max\\secret /home/alice/private");
    assertFalse(sanitized.contains("Max"));
    assertFalse(sanitized.contains("alice"));
  }

  @Test
  void oneBrokenProbeDoesNotAbortTheReport() {
    try (DiagnosticManager manager =
        new DiagnosticManager(
            category -> {
              if (category == DiagnosticCategory.AUDIO) {
                throw new IllegalStateException("raw private failure");
              }
              return new DiagnosticObservation(category, DiagnosticStatus.OK, "OK", "");
            })) {
      DiagnosticReport report = manager.runAll().join();

      assertEquals(DiagnosticCategory.values().length, report.findings().size());
      assertEquals(
          DiagnosticStatus.ERROR,
          report.findings().stream()
              .filter(finding -> finding.category() == DiagnosticCategory.AUDIO)
              .findFirst()
              .orElseThrow()
              .status());
      assertFalse(report.asSanitizedText().contains("raw private failure"));
    }
  }
}
