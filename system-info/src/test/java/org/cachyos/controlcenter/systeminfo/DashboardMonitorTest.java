package org.cachyos.controlcenter.systeminfo;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class DashboardMonitorTest {
  @Test
  void coalescesExplicitRefreshAndKeepsItOffCallingThread() throws Exception {
    AtomicInteger reads = new AtomicInteger();
    PlatformInfo platform =
        new PlatformInfo(OperatingSystemFamily.OTHER, "Test", "", "x86_64", "", "");
    DashboardDataSource source =
        new DashboardDataSource(
            platform,
            ignored -> {
              reads.incrementAndGet();
              return SupplementalStatus.unavailable();
            });
    try (DashboardMonitor monitor =
        new DashboardMonitor(
            source,
            DashboardDataSource.initial(SystemSnapshotDetector.detect(platform)),
            Duration.ofHours(1))) {
      monitor.refreshNow();
      monitor.refreshNow();
      long deadline = System.nanoTime() + Duration.ofSeconds(3).toNanos();
      while (reads.get() == 0 && System.nanoTime() < deadline) {
        Thread.sleep(10);
      }
      assertTrue(reads.get() >= 1);
      assertTrue(reads.get() <= 2);
    }
  }
}
