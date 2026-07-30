package org.cachyos.controlcenter.systeminfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class SystemSnapshotDetectorTest {
  @Test
  void recognizesCachyOsFromReleaseId() {
    SystemSnapshot.DistributionInfo distribution =
        SystemSnapshotDetector.distribution(
            OperatingSystemFamily.LINUX,
            Map.of(
                "ID", "cachyos",
                "NAME", "CachyOS",
                "PRETTY_NAME", "CachyOS Linux",
                "BUILD_ID", "rolling"));

    assertTrue(distribution.cachyOs());
    assertEquals("rolling", distribution.version());
  }

  @Test
  void doesNotGuessCachyOsForOtherDistribution() {
    SystemSnapshot.DistributionInfo distribution =
        SystemSnapshotDetector.distribution(
            OperatingSystemFamily.LINUX, Map.of("ID", "arch", "PRETTY_NAME", "Arch Linux"));

    assertFalse(distribution.cachyOs());
  }

  @Test
  void currentPlatformSnapshotIsCompleteAndInternallyConsistent() {
    SystemSnapshot snapshot = SystemSnapshotDetector.detect(PlatformDetector.detect());

    assertNotNull(snapshot.distribution());
    assertTrue(snapshot.hardware().logicalProcessors() >= 1);
    assertTrue(snapshot.hardware().totalMemoryBytes() >= 0);
    assertEquals(Capability.values().length, snapshot.capabilities().statuses().size());
    assertNotNull(snapshot.capturedAt());
  }
}
