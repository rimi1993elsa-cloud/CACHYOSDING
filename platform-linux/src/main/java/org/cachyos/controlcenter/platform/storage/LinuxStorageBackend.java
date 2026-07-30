package org.cachyos.controlcenter.platform.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import org.cachyos.controlcenter.modules.storage.LargeFile;
import org.cachyos.controlcenter.modules.storage.MountEntry;
import org.cachyos.controlcenter.modules.storage.SmartHealth;
import org.cachyos.controlcenter.modules.storage.StorageBackend;
import org.cachyos.controlcenter.modules.storage.StorageDevice;
import org.cachyos.controlcenter.modules.storage.StorageSnapshot;
import org.cachyos.controlcenter.platform.status.FixedCommandReader;

public final class LinuxStorageBackend implements StorageBackend {
  private static final Duration TIMEOUT = Duration.ofSeconds(15);
  private static final long LARGE_FILE = 100L * 1024 * 1024;
  private static final Pattern DEVICE =
      Pattern.compile("/dev/(sd[a-z]|nvme[0-9]+n[0-9]+|mmcblk[0-9]+)");
  private final boolean linux;
  private final ObjectMapper mapper = new ObjectMapper();

  public LinuxStorageBackend(boolean linux) {
    this.linux = linux;
  }

  @Override
  public StorageSnapshot inspect() {
    if (!linux) {
      return new StorageSnapshot(
          false,
          List.of(),
          List.of(),
          List.of(),
          false,
          "",
          Instant.now(),
          "Linux-Speichererkennung nicht verfügbar");
    }
    List<StorageDevice> devices =
        FixedCommandReader.read(
                Path.of("/usr/bin/lsblk"),
                List.of("-J", "-b", "-o", "PATH,TYPE,SIZE,FSTYPE,MOUNTPOINT,MODEL"),
                TIMEOUT)
            .map(lines -> parseDevices(String.join("\n", lines)))
            .orElse(List.of());
    List<MountEntry> mounts =
        FixedCommandReader.read(
                Path.of("/usr/bin/findmnt"),
                List.of("-J", "-o", "SOURCE,TARGET,FSTYPE,OPTIONS"),
                TIMEOUT)
            .map(lines -> parseMounts(String.join("\n", lines)))
            .orElse(List.of());
    List<SmartHealth> smart = smart(devices);
    boolean btrfsRoot =
        mounts.stream().anyMatch(m -> m.target().equals("/") && m.fileSystem().equals("btrfs"));
    String usage =
        btrfsRoot
            ? FixedCommandReader.read(
                    Path.of("/usr/bin/btrfs"), List.of("filesystem", "usage", "-b", "/"), TIMEOUT)
                .map(lines -> String.join("\n", lines))
                .orElse("Btrfs-Nutzung nicht lesbar")
            : "Root-Dateisystem ist nicht Btrfs";
    return new StorageSnapshot(
        true, devices, mounts, smart, btrfsRoot, usage, Instant.now(), "Nur lesende Erkennung");
  }

  @Override
  public List<LargeFile> findLargeFiles(Path root) {
    Path normalized = root.toAbsolutePath().normalize();
    if (!Files.isDirectory(normalized, LinkOption.NOFOLLOW_LINKS)
        || Files.isSymbolicLink(normalized)) {
      return List.of();
    }
    try (var paths =
        Files.find(
            normalized,
            8,
            (path, attributes) ->
                attributes.isRegularFile()
                    && !attributes.isSymbolicLink()
                    && attributes.size() >= LARGE_FILE)) {
      return paths
          .limit(20_000)
          .map(path -> new LargeFile(path, size(path)))
          .sorted(Comparator.comparingLong(LargeFile::sizeBytes).reversed())
          .limit(50)
          .toList();
    } catch (IOException | SecurityException exception) {
      return List.of();
    }
  }

  List<StorageDevice> parseDevices(String json) {
    try {
      List<StorageDevice> result = new ArrayList<>();
      for (JsonNode node : mapper.readTree(json).path("blockdevices")) {
        appendDevice(node, result);
      }
      return List.copyOf(result);
    } catch (IOException exception) {
      return List.of();
    }
  }

  List<MountEntry> parseMounts(String json) {
    try {
      List<MountEntry> result = new ArrayList<>();
      for (JsonNode node : mapper.readTree(json).path("filesystems")) {
        appendMount(node, result);
      }
      return List.copyOf(result);
    } catch (IOException exception) {
      return List.of();
    }
  }

  private List<SmartHealth> smart(List<StorageDevice> devices) {
    if (!Files.isExecutable(Path.of("/usr/bin/smartctl"))) {
      return List.of();
    }
    return devices.stream()
        .filter(device -> device.type().equals("disk") && DEVICE.matcher(device.path()).matches())
        .limit(32)
        .map(
            device -> {
              List<String> output =
                  FixedCommandReader.read(
                          Path.of("/usr/bin/smartctl"), List.of("-H", "--", device.path()), TIMEOUT)
                      .orElse(List.of());
              String status =
                  output.stream()
                      .filter(
                          line -> line.contains("overall-health") || line.contains("SMART Health"))
                      .findFirst()
                      .orElse("SMART-Status nicht lesbar");
              return new SmartHealth(device.path(), status, !output.isEmpty());
            })
        .toList();
  }

  private static void appendDevice(JsonNode node, List<StorageDevice> result) {
    result.add(
        new StorageDevice(
            text(node, "path"),
            text(node, "type"),
            node.path("size").asLong(),
            text(node, "fstype"),
            text(node, "mountpoint"),
            text(node, "model")));
    node.path("children").forEach(child -> appendDevice(child, result));
  }

  private static void appendMount(JsonNode node, List<MountEntry> result) {
    result.add(
        new MountEntry(
            text(node, "source"),
            text(node, "target"),
            text(node, "fstype"),
            text(node, "options")));
    node.path("children").forEach(child -> appendMount(child, result));
  }

  private static String text(JsonNode node, String field) {
    return node.path(field).isNull() ? "" : node.path(field).asText("");
  }

  private static long size(Path path) {
    try {
      return Files.size(path);
    } catch (IOException exception) {
      return 0;
    }
  }
}
