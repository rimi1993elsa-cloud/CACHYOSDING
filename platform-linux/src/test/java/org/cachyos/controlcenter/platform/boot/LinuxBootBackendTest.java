package org.cachyos.controlcenter.platform.boot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class LinuxBootBackendTest {
  @Test
  void parsesKernelPackagesAndBoundedBlame() {
    var backend = new LinuxBootBackend(true);
    var kernels =
        backend.parseKernels(
            List.of("linux-cachyos 6.15.7-2", "linux-cachyos-headers 6.15.7-2", "not-a-kernel 1.0"),
            "6.15.7-2-cachyos");
    assertEquals(1, kernels.size());
    assertTrue(kernels.getFirst().active());
    assertEquals(1, backend.parseBlame(List.of("1.234s NetworkManager.service")).size());
  }
}
