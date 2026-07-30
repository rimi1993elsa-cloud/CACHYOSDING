package org.cachyos.controlcenter.systeminfo;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

/** One immutable dashboard refresh with explicit unknown values. */
public record DashboardMetrics(
    double cpuLoad,
    long totalMemoryBytes,
    long freeMemoryBytes,
    long totalStorageBytes,
    long freeStorageBytes,
    SystemSnapshot.BatteryInfo battery,
    boolean online,
    OptionalInt availableUpdates,
    OptionalInt failedServices,
    List<String> warnings,
    Instant capturedAt) {
  public DashboardMetrics {
    if (cpuLoad < -1 || cpuLoad > 1) {
      throw new IllegalArgumentException("cpuLoad must be unknown or between zero and one");
    }
    totalMemoryBytes = Math.max(0, totalMemoryBytes);
    freeMemoryBytes = Math.max(0, freeMemoryBytes);
    totalStorageBytes = Math.max(0, totalStorageBytes);
    freeStorageBytes = Math.max(0, freeStorageBytes);
    battery = Objects.requireNonNull(battery, "battery");
    availableUpdates = availableUpdates == null ? OptionalInt.empty() : availableUpdates;
    failedServices = failedServices == null ? OptionalInt.empty() : failedServices;
    warnings = List.copyOf(warnings);
    capturedAt = Objects.requireNonNull(capturedAt, "capturedAt");
  }
}
