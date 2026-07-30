package org.cachyos.controlcenter.modules.snapshots;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SnapshotManagerTest {
  @Test
  void deletionRequiresExactTypedId() {
    TrackingGateway gateway = new TrackingGateway();
    try (SnapshotManager manager =
        new SnapshotManager(() -> new SnapshotState(true, java.util.List.of(), ""), gateway)) {
      assertFalse(manager.delete(42, "41").join().successful());
      assertFalse(gateway.called);
      assertTrue(manager.delete(42, "42").join().successful());
      assertTrue(gateway.called);
    }
  }

  private static final class TrackingGateway implements SnapshotGateway {
    private boolean called;

    @Override
    public boolean available() {
      return true;
    }

    @Override
    public SnapshotResult create(String description) {
      return new SnapshotResult(true, "ok");
    }

    @Override
    public SnapshotResult delete(int id) {
      called = true;
      return new SnapshotResult(true, "ok");
    }
  }
}
