package org.cachyos.controlcenter.core.action;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ActionRegistryTest {
  @Test
  void rejectsDuplicateActionRegistration() {
    ActionRegistry registry = new ActionRegistry();
    ActionRegistration registration =
        new ActionRegistration(
            ActionId.OPEN_TERMINAL,
            "Terminal öffnen",
            false,
            request -> ActionResult.success("ok"));

    registry.register(registration);

    assertThrows(IllegalStateException.class, () -> registry.register(registration));
  }
}
