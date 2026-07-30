package org.cachyos.controlcenter.platform.storage;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.cachyos.controlcenter.modules.snapshots.SnapshotBackend;
import org.cachyos.controlcenter.modules.snapshots.SnapshotEntry;
import org.cachyos.controlcenter.modules.snapshots.SnapshotState;
import org.cachyos.controlcenter.platform.status.FixedCommandReader;

public final class LinuxSnapshotBackend implements SnapshotBackend {
  private final boolean linux;

  public LinuxSnapshotBackend(boolean linux) {
    this.linux = linux;
  }

  @Override
  public SnapshotState inspect() {
    if (!linux || !java.nio.file.Files.isExecutable(Path.of("/usr/bin/snapper"))) {
      return new SnapshotState(false, List.of(), "Snapper ist nicht verfügbar");
    }
    List<String> lines =
        FixedCommandReader.read(
                Path.of("/usr/bin/snapper"),
                List.of("--csvout", "--no-headers", "list"),
                Duration.ofSeconds(15))
            .orElse(List.of());
    List<SnapshotEntry> entries = new ArrayList<>();
    for (String line : lines) {
      List<String> fields = csv(line);
      if (fields.size() < 10) {
        continue;
      }
      try {
        int id = Integer.parseInt(fields.get(0));
        if (id > 0) {
          entries.add(new SnapshotEntry(id, fields.get(2), fields.get(3), fields.get(9)));
        }
      } catch (NumberFormatException ignored) {
        // Header or malformed line.
      }
    }
    return new SnapshotState(true, entries, "Snapper-Snapshots");
  }

  static List<String> csv(String line) {
    List<String> fields = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean quoted = false;
    for (int index = 0; index < line.length(); index++) {
      char value = line.charAt(index);
      if (value == '"') {
        if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
          current.append('"');
          index++;
        } else {
          quoted = !quoted;
        }
      } else if (value == ',' && !quoted) {
        fields.add(current.toString());
        current.setLength(0);
      } else {
        current.append(value);
      }
    }
    fields.add(current.toString());
    return List.copyOf(fields);
  }
}
