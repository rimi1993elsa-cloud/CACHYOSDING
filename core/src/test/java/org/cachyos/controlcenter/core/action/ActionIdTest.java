package org.cachyos.controlcenter.core.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ActionIdTest {
  @Test
  void acceptsStableNamespacedIdentifier() {
    assertEquals("desktop.open-firefox", ActionId.of("desktop.open-firefox").value());
  }

  @Test
  void rejectsShellSyntaxAndWhitespace() {
    assertThrows(IllegalArgumentException.class, () -> ActionId.of("desktop.open; rm"));
    assertThrows(IllegalArgumentException.class, () -> ActionId.of("../desktop"));
    assertThrows(IllegalArgumentException.class, () -> ActionId.of("UPPERCASE"));
  }
}
