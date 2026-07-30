package org.cachyos.controlcenter.modules.snapshots;

public interface SnapshotBackend {
  SnapshotState inspect();
}
