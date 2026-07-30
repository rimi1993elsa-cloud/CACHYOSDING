package org.cachyos.controlcenter.modules.boot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class BootManagerTest {
  @Test
  void exposesReadOnlySnapshotAsynchronously() {
    try (BootManager manager =
        new BootManager(
            new BootBackend() {
              public BootSnapshot inspect() {
                return new BootSnapshot(
                    true, "linux-cachyos", List.of(), "systemd-boot", "", "", List.of(), false, "");
              }

              public BootResult launchKernelManager() {
                return new BootResult(false, "");
              }
            })) {
      assertEquals("linux-cachyos", manager.inspect().join().activeKernel());
    }
  }
}
