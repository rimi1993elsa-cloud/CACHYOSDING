package org.cachyos.controlcenter.systeminfo;

import com.sun.management.OperatingSystemMXBean;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.NetworkInterface;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.cachyos.controlcenter.systeminfo.SystemSnapshot.BatteryInfo;
import org.cachyos.controlcenter.systeminfo.SystemSnapshot.BootManager;
import org.cachyos.controlcenter.systeminfo.SystemSnapshot.DistributionInfo;
import org.cachyos.controlcenter.systeminfo.SystemSnapshot.HardwareInfo;
import org.cachyos.controlcenter.systeminfo.SystemSnapshot.NetworkInfo;
import org.cachyos.controlcenter.systeminfo.SystemSnapshot.NetworkInterfaceInfo;
import org.cachyos.controlcenter.systeminfo.SystemSnapshot.SessionInfo;
import org.cachyos.controlcenter.systeminfo.SystemSnapshot.StorageInfo;

/** Collects bounded read-only facts using Java APIs and small kernel text files. */
public final class SystemSnapshotDetector {
  private SystemSnapshotDetector() {}

  public static SystemSnapshot detect(PlatformInfo platformInfo) {
    Map<String, String> environment = System.getenv();
    return new SystemSnapshot(
        distribution(platformInfo.operatingSystemFamily()),
        platformInfo.operatingSystemVersion(),
        session(platformInfo),
        hardware(platformInfo.operatingSystemFamily()),
        storage(platformInfo.operatingSystemFamily()),
        battery(platformInfo.operatingSystemFamily()),
        network(),
        bootManager(platformInfo.operatingSystemFamily()),
        CapabilityRegistry.detect(environment, platformInfo.operatingSystemFamily()),
        Instant.now());
  }

  static DistributionInfo distribution(
      OperatingSystemFamily family, Map<String, String> releaseValues) {
    String id = releaseValues.get("ID");
    String name = releaseValues.get("NAME");
    String prettyName = releaseValues.get("PRETTY_NAME");
    String version =
        releaseValues.getOrDefault("VERSION_ID", releaseValues.getOrDefault("BUILD_ID", ""));
    boolean cachyOs =
        "cachyos".equalsIgnoreCase(id) || containsCachy(name) || containsCachy(prettyName);
    return new DistributionInfo(id, name, prettyName, version, cachyOs);
  }

  private static DistributionInfo distribution(OperatingSystemFamily family) {
    if (family != OperatingSystemFamily.LINUX) {
      String osName = System.getProperty("os.name");
      return new DistributionInfo("", osName, osName, System.getProperty("os.version"), false);
    }
    Path release = Path.of("/etc/os-release");
    try {
      return distribution(family, OsReleaseParser.parse(Files.readString(release)));
    } catch (IOException exception) {
      return new DistributionInfo("linux", "Linux", "Linux", "", false);
    }
  }

  private static SessionInfo session(PlatformInfo info) {
    String desktop = info.desktopSession();
    String type = info.sessionType();
    return new SessionInfo(
        desktop,
        type,
        desktop.toLowerCase(Locale.ROOT).contains("kde"),
        "wayland".equalsIgnoreCase(type));
  }

  private static HardwareInfo hardware(OperatingSystemFamily family) {
    String cpu = cpuModel(family);
    long memory = totalMemory();
    List<String> graphics = graphicsFromSysfs(family);
    return new HardwareInfo(cpu, Runtime.getRuntime().availableProcessors(), memory, graphics);
  }

  private static String cpuModel(OperatingSystemFamily family) {
    if (family == OperatingSystemFamily.LINUX) {
      try {
        Optional<String> model =
            Files.readAllLines(Path.of("/proc/cpuinfo")).stream()
                .filter(line -> line.startsWith("model name"))
                .map(line -> line.substring(line.indexOf(':') + 1).trim())
                .filter(value -> !value.isBlank())
                .findFirst();
        if (model.isPresent()) {
          return model.get();
        }
      } catch (IOException ignored) {
        // Explicit unknown fallback below.
      }
    }
    return System.getenv().getOrDefault("PROCESSOR_IDENTIFIER", "");
  }

  private static long totalMemory() {
    if (ManagementFactory.getOperatingSystemMXBean() instanceof OperatingSystemMXBean bean) {
      return Math.max(0, bean.getTotalMemorySize());
    }
    return 0;
  }

  private static List<String> graphicsFromSysfs(OperatingSystemFamily family) {
    if (family != OperatingSystemFamily.LINUX) {
      return List.of();
    }
    Path drm = Path.of("/sys/class/drm");
    if (!Files.isDirectory(drm)) {
      return List.of();
    }
    List<String> graphics = new ArrayList<>();
    try (var entries = Files.list(drm)) {
      for (Path entry :
          entries.filter(path -> path.getFileName().toString().matches("card\\d+")).toList()) {
        Path uevent = entry.resolve("device/uevent");
        if (!Files.isRegularFile(uevent)) {
          continue;
        }
        Map<String, String> values = simpleKeyValues(Files.readString(uevent));
        String driver = values.get("DRIVER");
        String pciId = values.get("PCI_ID");
        String value = driver == null ? pciId : driver + (pciId == null ? "" : " · " + pciId);
        if (value != null && !value.isBlank()) {
          graphics.add(value);
        }
      }
    } catch (IOException ignored) {
      return List.of();
    }
    return List.copyOf(graphics);
  }

  private static StorageInfo storage(OperatingSystemFamily family) {
    Path root =
        family == OperatingSystemFamily.WINDOWS
            ? Path.of(System.getProperty("user.home")).toAbsolutePath().getRoot()
            : Path.of("/");
    try {
      FileStore store = Files.getFileStore(root);
      return new StorageInfo(
          root.toString(), store.type(), store.getTotalSpace(), store.getUsableSpace());
    } catch (IOException exception) {
      return new StorageInfo(root.toString(), "", 0, 0);
    }
  }

  private static BatteryInfo battery(OperatingSystemFamily family) {
    if (family != OperatingSystemFamily.LINUX) {
      return new BatteryInfo(false, -1, "");
    }
    Path power = Path.of("/sys/class/power_supply");
    if (!Files.isDirectory(power)) {
      return new BatteryInfo(false, -1, "");
    }
    try (var entries = Files.list(power)) {
      Optional<Path> battery =
          entries
              .filter(path -> path.getFileName().toString().startsWith("BAT"))
              .filter(Files::isDirectory)
              .findFirst();
      if (battery.isEmpty()) {
        return new BatteryInfo(false, -1, "");
      }
      int capacity = readInteger(battery.get().resolve("capacity")).orElse(-1);
      String status = readFirstLine(battery.get().resolve("status")).orElse("");
      return new BatteryInfo(true, capacity, status);
    } catch (IOException exception) {
      return new BatteryInfo(false, -1, "");
    }
  }

  private static NetworkInfo network() {
    List<NetworkInterfaceInfo> interfaces = new ArrayList<>();
    boolean online = false;
    try {
      for (NetworkInterface networkInterface :
          Collections.list(NetworkInterface.getNetworkInterfaces())) {
        boolean up = networkInterface.isUp();
        boolean loopback = networkInterface.isLoopback();
        interfaces.add(
            new NetworkInterfaceInfo(
                networkInterface.getName(), networkInterface.getDisplayName(), up, loopback));
        online |= up && !loopback && networkInterface.getInetAddresses().hasMoreElements();
      }
    } catch (IOException exception) {
      return new NetworkInfo(false, List.of());
    }
    return new NetworkInfo(online, interfaces);
  }

  private static BootManager bootManager(OperatingSystemFamily family) {
    if (family != OperatingSystemFamily.LINUX) {
      return BootManager.OTHER;
    }
    if (Files.isDirectory(Path.of("/boot/loader/entries"))) {
      return BootManager.SYSTEMD_BOOT;
    }
    if (Files.exists(Path.of("/boot/limine.conf"))
        || Files.exists(Path.of("/boot/limine/limine.conf"))) {
      return BootManager.LIMINE;
    }
    if (Files.isDirectory(Path.of("/boot/grub"))) {
      return BootManager.GRUB;
    }
    if (Files.isDirectory(Path.of("/boot/EFI/refind"))
        || Files.exists(Path.of("/boot/refind_linux.conf"))) {
      return BootManager.REFIND;
    }
    return BootManager.OTHER;
  }

  private static Map<String, String> simpleKeyValues(String content) {
    return OsReleaseParser.parse(content);
  }

  private static Optional<Integer> readInteger(Path path) {
    try {
      return Optional.of(Integer.parseInt(Files.readString(path).trim()));
    } catch (IOException | NumberFormatException exception) {
      return Optional.empty();
    }
  }

  private static Optional<String> readFirstLine(Path path) {
    try {
      return Files.readAllLines(path).stream().findFirst();
    } catch (IOException exception) {
      return Optional.empty();
    }
  }

  private static boolean containsCachy(String value) {
    return value != null && value.toLowerCase(Locale.ROOT).contains("cachyos");
  }
}
