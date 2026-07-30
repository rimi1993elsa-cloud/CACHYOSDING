package org.cachyos.controlcenter.platform.status;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class LinuxSupplementalStatusProbeTest {
  @Test
  void countsOnlyNonBlankStatusLines() {
    assertEquals(
        2,
        LinuxSupplementalStatusProbe.countNonBlank(
            List.of("package 1.0 -> 1.1", "", "  ", "service.service failed")));
  }
}
