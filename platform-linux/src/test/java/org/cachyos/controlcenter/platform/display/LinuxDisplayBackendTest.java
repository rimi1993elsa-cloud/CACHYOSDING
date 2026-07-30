package org.cachyos.controlcenter.platform.display;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LinuxDisplayBackendTest {
  @Test
  void parsesKscreenJsonWithoutX11Tools() {
    String json =
        """
        {"outputs":[{"name":"eDP-1","enabled":true,"priority":1,"scale":1.25,
        "currentMode":{"size":{"width":1920,"height":1080},"refreshRate":60.0}}]}
        """;
    var monitors = new LinuxDisplayBackend(true).parseKscreen(json);
    assertEquals(1, monitors.size());
    assertEquals("eDP-1", monitors.getFirst().name());
    assertEquals(1.25, monitors.getFirst().scale());
  }
}
