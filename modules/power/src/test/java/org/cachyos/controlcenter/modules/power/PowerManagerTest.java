package org.cachyos.controlcenter.modules.power;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import org.junit.jupiter.api.Test;

class PowerManagerTest {
  @Test
  void validatesProfilesAndSleepConfirmation() {
    PowerBackend backend =
        new PowerBackend() {
          @Override
          public PowerState inspect() {
            return new PowerState(false, false, 0, "", List.of(), false, false, "");
          }

          @Override
          public PowerResult setProfile(String profile) {
            throw new AssertionError("must not be called");
          }

          @Override
          public PowerResult suspend() {
            throw new AssertionError("must not be called");
          }

          @Override
          public PowerResult hibernate() {
            throw new AssertionError("must not be called");
          }
        };
    try (PowerManager manager = new PowerManager(backend)) {
      assertFalse(manager.setProfile("balanced; reboot").join().success());
      assertFalse(manager.suspend(false).join().success());
      assertFalse(manager.hibernate("ja").join().success());
    }
  }
}
