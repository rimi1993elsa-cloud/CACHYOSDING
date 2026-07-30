package org.cachyos.controlcenter.modules.storage;

import java.time.Instant;
import java.util.List;

public record StorageSnapshot(
    boolean available,
    List<StorageDevice> devices,
    List<MountEntry> mounts,
    List<SmartHealth> smart,
    boolean btrfsRoot,
    String btrfsUsage,
    Instant capturedAt,
    String message) {
  public StorageSnapshot {
    devices = List.copyOf(devices);
    mounts = List.copyOf(mounts);
    smart = List.copyOf(smart);
  }
}
