package org.cachyos.controlcenter.platform.power;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class LinuxPowerBackendTest {
  @Test
  void parsesOnlyKnownShapeProfileLines() {
    var profiles =
        new LinuxPowerBackend(true)
            .parseProfiles(List.of("  performance:", "* balanced:", "  power-saver:", "x; reboot"));
    assertEquals(3, profiles.size());
    assertTrue(profiles.get(1).active());
  }
}
