package org.cachyos.controlcenter.systeminfo;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Collects lightweight metrics and caches slower external status checks. */
public final class DashboardDataSource {
  private static final Duration SLOW_REFRESH_INTERVAL = Duration.ofMinutes(5);

  private final PlatformInfo platformInfo;
  private final SupplementalStatusProbe supplementalProbe;
  private final Clock clock;
  private SupplementalStatus supplemental = SupplementalStatus.unavailable();
  private Instant nextSlowRefresh = Instant.EPOCH;

  public DashboardDataSource(PlatformInfo platformInfo, SupplementalStatusProbe supplementalProbe) {
    this(platformInfo, supplementalProbe, Clock.systemUTC());
  }

  DashboardDataSource(
      PlatformInfo platformInfo, SupplementalStatusProbe supplementalProbe, Clock clock) {
    this.platformInfo = platformInfo;
    this.supplementalProbe = supplementalProbe;
    this.clock = clock;
  }

  public synchronized DashboardMetrics read() {
    Instant now = clock.instant();
    SystemSnapshot snapshot = SystemSnapshotDetector.detect(platformInfo);
    if (!now.isBefore(nextSlowRefresh)) {
      supplemental = supplementalProbe.read(snapshot.capabilities());
      nextSlowRefresh = now.plus(SLOW_REFRESH_INTERVAL);
    }
    double cpuLoad = -1;
    long freeMemory = 0;
    if (ManagementFactory.getOperatingSystemMXBean() instanceof OperatingSystemMXBean bean) {
      cpuLoad = normalizeLoad(bean.getCpuLoad());
      freeMemory = Math.max(0, bean.getFreeMemorySize());
    }
    return createMetrics(snapshot, supplemental, cpuLoad, freeMemory, now);
  }

  public static DashboardMetrics initial(SystemSnapshot snapshot) {
    return createMetrics(snapshot, SupplementalStatus.unavailable(), -1, 0, snapshot.capturedAt());
  }

  static DashboardMetrics createMetrics(
      SystemSnapshot snapshot,
      SupplementalStatus supplemental,
      double cpuLoad,
      long freeMemory,
      Instant capturedAt) {
    List<String> warnings = new ArrayList<>();
    if (!snapshot.network().online()) {
      warnings.add("Keine aktive Netzwerkverbindung erkannt.");
    }
    if (snapshot.battery().present()
        && snapshot.battery().percentage() >= 0
        && snapshot.battery().percentage() <= 15) {
      warnings.add("Akkustand ist niedrig.");
    }
    if (snapshot.storage().totalBytes() > 0
        && snapshot.storage().usableBytes() * 10 < snapshot.storage().totalBytes()) {
      warnings.add("Weniger als 10 % Systemspeicher sind frei.");
    }
    supplemental.availableUpdates().stream()
        .filter(count -> count > 0)
        .forEach(count -> warnings.add(count + " Paketaktualisierungen verfügbar."));
    supplemental.failedServices().stream()
        .filter(count -> count > 0)
        .forEach(count -> warnings.add(count + " systemd-Dienste sind fehlgeschlagen."));
    return new DashboardMetrics(
        normalizeLoad(cpuLoad),
        snapshot.hardware().totalMemoryBytes(),
        freeMemory,
        snapshot.storage().totalBytes(),
        snapshot.storage().usableBytes(),
        snapshot.battery(),
        snapshot.network().online(),
        supplemental.availableUpdates(),
        supplemental.failedServices(),
        warnings,
        capturedAt);
  }

  private static double normalizeLoad(double load) {
    return Double.isFinite(load) && load >= 0 ? Math.min(1, load) : -1;
  }
}
