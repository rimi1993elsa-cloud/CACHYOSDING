package org.cachyos.controlcenter.modules.packages;

import java.time.Instant;
import java.util.List;

public record PackageSnapshot(
    boolean available,
    boolean locked,
    List<PackageEntry> installed,
    List<PackageEntry> updates,
    List<String> orphanNames,
    long cacheBytes,
    Instant capturedAt,
    String message) {
  public PackageSnapshot {
    installed = List.copyOf(installed);
    updates = List.copyOf(updates);
    orphanNames = List.copyOf(orphanNames);
    message = message == null ? "" : message;
  }

  public static PackageSnapshot unavailable(String message) {
    return new PackageSnapshot(
        false, false, List.of(), List.of(), List.of(), 0, Instant.now(), message);
  }
}
