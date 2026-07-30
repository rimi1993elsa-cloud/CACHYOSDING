package org.cachyos.controlcenter.modules.hardware;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class HardwareManagerTest {
  @Test
  void anonymizedReportMasksUnexpectedIdentifiers() {
    HardwareSnapshot snapshot =
        new HardwareSnapshot(
            true,
            "Dell",
            "Latitude 5440 serial: SECRET",
            "CPU",
            1,
            "ok",
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            Instant.now(),
            "");
    try (HardwareManager manager = new HardwareManager(() -> snapshot)) {
      String report = manager.report(snapshot, true).text();
      assertTrue(report.contains("[MASKIERT]"));
      assertFalse(report.contains("SECRET"));
    }
  }
}
