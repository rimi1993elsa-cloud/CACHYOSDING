package org.cachyos.controlcenter.modules.processes;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ProcessManagerTest {
  @Test
  void blocksUnknownCriticalAndUnconfirmedKill() {
    TrackingGateway gateway = new TrackingGateway();
    ProcessBackend backend =
        () ->
            List.of(
                new ProcessEntry(1, "systemd", "root", 0, 0, 0, true),
                new ProcessEntry(42, "app", "user", 0, 0, 0, false));
    try (ProcessManager manager = new ProcessManager(backend, gateway)) {
      manager.inspect().join();
      assertFalse(manager.terminate(1).join().successful());
      assertFalse(manager.kill(42, "41").join().successful());
      assertFalse(manager.terminate(99).join().successful());
      assertFalse(gateway.called);
      assertTrue(manager.kill(42, "42").join().successful());
      assertTrue(gateway.called);
    }
  }

  private static final class TrackingGateway implements ProcessGateway {
    private boolean called;

    public ProcessResult signal(long pid, int signal) {
      called = true;
      return new ProcessResult(true, "ok");
    }

    public ProcessResult priority(long pid, int priority) {
      called = true;
      return new ProcessResult(true, "ok");
    }
  }
}
