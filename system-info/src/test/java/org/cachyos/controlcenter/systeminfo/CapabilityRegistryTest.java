package org.cachyos.controlcenter.systeminfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class CapabilityRegistryTest {
  @Test
  void marksEveryKnownToolUnavailableWhenPathIsEmpty() {
    CapabilityRegistry registry =
        CapabilityRegistry.detect(Map.of("PATH", ""), OperatingSystemFamily.LINUX);

    assertEquals(Capability.values().length, registry.statuses().size());
    assertFalse(registry.available(Capability.NMCLI));
    assertTrue(registry.status(Capability.NMCLI).reason().contains("networkmanager"));
  }
}
