package org.cachyos.controlcenter.platform.hardware;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.cachyos.controlcenter.modules.hardware.HardwareBackend;
import org.cachyos.controlcenter.modules.hardware.HardwareDevice;
import org.cachyos.controlcenter.modules.hardware.HardwareSnapshot;
import org.cachyos.controlcenter.modules.hardware.SensorReading;
import org.cachyos.controlcenter.platform.status.FixedCommandReader;

public final class LinuxHardwareBackend implements HardwareBackend {
  private static final Duration TIMEOUT = Duration.ofSeconds(12);
  private final boolean linux;

  public LinuxHardwareBackend(boolean linux) {
    this.linux = linux;
  }

  @Override
  public HardwareSnapshot inspect() {
    if (!linux) {
      return unavailable();
    }
    String manufacturer = read(Path.of("/sys/class/dmi/id/sys_vendor"));
    String product = read(Path.of("/sys/class/dmi/id/product_name"));
    String cpu =
        lines(Path.of("/proc/cpuinfo")).stream()
            .filter(line -> line.startsWith("model name"))
            .map(LinuxHardwareBackend::afterColon)
            .findFirst()
            .orElse("Unbekannt");
    long memory = parseMemory(lines(Path.of("/proc/meminfo")));
    String battery = battery();
    List<HardwareDevice> pci =
        FixedCommandReader.read(Path.of("/usr/bin/lspci"), List.of("-D", "-nnk"), TIMEOUT)
            .map(LinuxHardwareBackend::parsePci)
            .orElse(List.of());
    List<HardwareDevice> graphics =
        pci.stream()
            .filter(
                device ->
                    device.description().contains("VGA")
                        || device.description().contains("3D controller")
                        || device.description().contains("Display"))
            .toList();
    List<HardwareDevice> usb =
        FixedCommandReader.read(Path.of("/usr/bin/lsusb"), List.of(), TIMEOUT)
            .map(LinuxHardwareBackend::parseUsb)
            .orElse(List.of());
    List<SensorReading> sensors =
        FixedCommandReader.read(Path.of("/usr/bin/sensors"), List.of("-u"), TIMEOUT)
            .map(LinuxHardwareBackend::parseSensors)
            .orElse(List.of());
    return new HardwareSnapshot(
        true,
        manufacturer,
        product,
        cpu,
        memory,
        battery,
        graphics,
        pci,
        usb,
        sensors,
        Instant.now(),
        "Seriennummern werden nicht erhoben");
  }

  static List<HardwareDevice> parsePci(List<String> lines) {
    List<HardwareDevice> devices = new ArrayList<>();
    HardwareDevice current = null;
    for (String line : lines) {
      if (!line.isBlank() && !Character.isWhitespace(line.charAt(0))) {
        String[] parts = line.split("\\s+", 2);
        current = new HardwareDevice("PCI", parts[0], parts.length > 1 ? parts[1] : "", "");
        devices.add(current);
      } else if (current != null && line.trim().startsWith("Kernel driver in use:")) {
        String driver = afterColon(line.trim());
        devices.set(
            devices.size() - 1,
            new HardwareDevice(current.bus(), current.identifier(), current.description(), driver));
        current = devices.getLast();
      }
    }
    return List.copyOf(devices);
  }

  static List<HardwareDevice> parseUsb(List<String> lines) {
    return lines.stream()
        .filter(line -> line.contains(" ID "))
        .map(
            line -> {
              int id = line.indexOf(" ID ");
              String tail = line.substring(id + 4);
              String[] parts = tail.split("\\s+", 2);
              return new HardwareDevice(
                  "USB", parts[0], parts.length > 1 ? parts[1] : "", "Kernel");
            })
        .limit(500)
        .toList();
  }

  static List<SensorReading> parseSensors(List<String> lines) {
    List<SensorReading> result = new ArrayList<>();
    for (String line : lines) {
      String trimmed = line.trim();
      if (!trimmed.contains("_input:")) {
        continue;
      }
      String[] parts = trimmed.split(":", 2);
      try {
        result.add(new SensorReading(parts[0], Double.parseDouble(parts[1].trim()), ""));
      } catch (NumberFormatException ignored) {
        // Ignore malformed optional sensor output.
      }
    }
    return List.copyOf(result);
  }

  private String battery() {
    Path root = Path.of("/sys/class/power_supply");
    if (!Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS)) {
      return "Nicht vorhanden";
    }
    try (var entries = Files.list(root)) {
      return entries
          .filter(path -> path.getFileName().toString().startsWith("BAT"))
          .findFirst()
          .map(path -> read(path.resolve("capacity")) + " % · " + read(path.resolve("status")))
          .orElse("Nicht vorhanden");
    } catch (IOException exception) {
      return "Nicht lesbar";
    }
  }

  private static long parseMemory(List<String> lines) {
    return lines.stream()
        .filter(line -> line.startsWith("MemTotal:"))
        .map(line -> line.replaceAll("[^0-9]", ""))
        .filter(value -> !value.isBlank())
        .mapToLong(Long::parseLong)
        .map(value -> value * 1024)
        .findFirst()
        .orElse(0);
  }

  private static String read(Path path) {
    if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(path)) {
      return "Unbekannt";
    }
    try {
      return Files.readString(path).trim();
    } catch (IOException exception) {
      return "Unbekannt";
    }
  }

  private static List<String> lines(Path path) {
    if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(path)) {
      return List.of();
    }
    try {
      return Files.readAllLines(path).stream().limit(20_000).toList();
    } catch (IOException exception) {
      return List.of();
    }
  }

  private static String afterColon(String line) {
    int separator = line.indexOf(':');
    return separator < 0 ? line : line.substring(separator + 1).trim();
  }

  private static HardwareSnapshot unavailable() {
    return new HardwareSnapshot(
        false,
        "Unbekannt",
        "Unbekannt",
        "Unbekannt",
        0,
        "Nicht verfügbar",
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        Instant.now(),
        "Linux-Hardwareerkennung ist auf dieser Plattform nicht verfügbar");
  }
}
