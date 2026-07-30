package org.cachyos.controlcenter.platform.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.cachyos.controlcenter.modules.services.ServiceScope;
import org.junit.jupiter.api.Test;

class LinuxServiceBackendTest {
  @Test
  void keepsSystemAndUserScopeExplicit() {
    var units =
        LinuxServiceBackend.parse(
            List.of("pipewire.service loaded active running PipeWire"), ServiceScope.USER);
    assertEquals(ServiceScope.USER, units.getFirst().scope());
  }
}
