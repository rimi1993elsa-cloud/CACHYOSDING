package org.cachyos.controlcenter.modules.snapshots;

import java.util.List;

public record SnapshotState(boolean available, List<SnapshotEntry> entries, String message) {
  public SnapshotState {
    entries = List.copyOf(entries);
  }
}
