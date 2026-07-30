package org.cachyos.controlcenter.modules.snapshots;

public interface SnapshotGateway {
  boolean available();

  SnapshotResult create(String description);

  SnapshotResult delete(int id);
}
