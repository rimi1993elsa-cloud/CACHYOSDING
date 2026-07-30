package org.cachyos.controlcenter.systeminfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class DashboardDataSourceTest {
  @Test
  void createsWarningsOnlyFromRealThresholds() {
    SystemSnapshot snapshot =
        new SystemSnapshot(
            new SystemSnapshot.DistributionInfo("cachyos", "CachyOS", "CachyOS", "", true),
            "6.12",
            new SystemSnapshot.SessionInfo("KDE", "wayland", true, true),
            new SystemSnapshot.HardwareInfo("CPU", 8, 16_000, List.of()),
            new SystemSnapshot.StorageInfo("/", "btrfs", 1000, 50),
            new SystemSnapshot.BatteryInfo(true, 10, "Discharging"),
            new SystemSnapshot.NetworkInfo(false, List.of()),
            SystemSnapshot.BootManager.SYSTEMD_BOOT,
            CapabilityRegistry.detect(Map.of("PATH", ""), OperatingSystemFamily.LINUX),
            Instant.EPOCH);
    SupplementalStatus supplemental = new SupplementalStatus(OptionalInt.of(4), OptionalInt.of(2));

    DashboardMetrics metrics =
        DashboardDataSource.createMetrics(snapshot, supplemental, 0.25, 4_000, Instant.EPOCH);

    assertEquals(5, metrics.warnings().size());
    assertTrue(metrics.warnings().stream().anyMatch(value -> value.contains("Paket")));
    assertEquals(0.25, metrics.cpuLoad());
  }

  @Test
  void marksUnknownCpuLoadExplicitly() {
    SystemSnapshot snapshot = SystemSnapshotDetector.detect(PlatformDetector.detect());

    DashboardMetrics metrics =
        DashboardDataSource.createMetrics(
            snapshot, SupplementalStatus.unavailable(), Double.NaN, 0, Instant.EPOCH);

    assertEquals(-1, metrics.cpuLoad());
    assertTrue(metrics.availableUpdates().isEmpty());
  }
}
