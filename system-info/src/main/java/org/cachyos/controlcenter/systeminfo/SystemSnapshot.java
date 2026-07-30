package org.cachyos.controlcenter.systeminfo;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** Complete immutable Phase 3 system snapshot. Unknown values remain explicit. */
public record SystemSnapshot(
    DistributionInfo distribution,
    String kernel,
    SessionInfo session,
    HardwareInfo hardware,
    StorageInfo storage,
    BatteryInfo battery,
    NetworkInfo network,
    BootManager bootManager,
    CapabilityRegistry capabilities,
    Instant capturedAt) {
  public SystemSnapshot {
    Objects.requireNonNull(distribution, "distribution");
    kernel = known(kernel);
    Objects.requireNonNull(session, "session");
    Objects.requireNonNull(hardware, "hardware");
    Objects.requireNonNull(storage, "storage");
    Objects.requireNonNull(battery, "battery");
    Objects.requireNonNull(network, "network");
    Objects.requireNonNull(bootManager, "bootManager");
    Objects.requireNonNull(capabilities, "capabilities");
    Objects.requireNonNull(capturedAt, "capturedAt");
  }

  public record DistributionInfo(
      String id, String name, String prettyName, String version, boolean cachyOs) {
    public DistributionInfo {
      id = known(id);
      name = known(name);
      prettyName = known(prettyName);
      version = known(version);
    }
  }

  public record SessionInfo(String desktop, String type, boolean kde, boolean wayland) {
    public SessionInfo {
      desktop = known(desktop);
      type = known(type);
    }
  }

  public record HardwareInfo(
      String cpuModel, int logicalProcessors, long totalMemoryBytes, List<String> graphics) {
    public HardwareInfo {
      cpuModel = known(cpuModel);
      if (logicalProcessors < 1 || totalMemoryBytes < 0) {
        throw new IllegalArgumentException("Invalid hardware values");
      }
      graphics = List.copyOf(graphics);
    }
  }

  public record StorageInfo(String root, String fileSystem, long totalBytes, long usableBytes) {
    public StorageInfo {
      root = known(root);
      fileSystem = known(fileSystem);
      if (totalBytes < 0 || usableBytes < 0) {
        throw new IllegalArgumentException("Invalid storage values");
      }
    }
  }

  public record BatteryInfo(boolean present, int percentage, String status) {
    public BatteryInfo {
      if (percentage < -1 || percentage > 100) {
        throw new IllegalArgumentException("Invalid battery percentage");
      }
      status = known(status);
    }
  }

  public record NetworkInfo(boolean online, List<NetworkInterfaceInfo> interfaces) {
    public NetworkInfo {
      interfaces = List.copyOf(interfaces);
    }
  }

  public record NetworkInterfaceInfo(
      String name, String displayName, boolean up, boolean loopback) {
    public NetworkInterfaceInfo {
      name = known(name);
      displayName = known(displayName);
    }
  }

  public enum BootManager {
    SYSTEMD_BOOT("systemd-boot"),
    LIMINE("Limine"),
    GRUB("GRUB"),
    REFIND("rEFInd"),
    OTHER("Nicht verfügbar");

    private final String displayName;

    BootManager(String displayName) {
      this.displayName = displayName;
    }

    public String displayName() {
      return displayName;
    }
  }

  private static String known(String value) {
    return value == null || value.isBlank() ? "Nicht verfügbar" : value;
  }
}
