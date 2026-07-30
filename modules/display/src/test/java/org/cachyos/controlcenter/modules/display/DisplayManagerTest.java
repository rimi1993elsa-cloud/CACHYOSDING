package org.cachyos.controlcenter.modules.display;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import org.junit.jupiter.api.Test;

class DisplayManagerTest {
  @Test
  void rejectsOutOfRangeBrightnessBeforeBackend() {
    DisplayBackend backend =
        new DisplayBackend() {
          @Override
          public DisplayState inspect() {
            return new DisplayState(
                false,
                false,
                List.of(),
                0,
                false,
                false,
                false,
                new GraphicsInfo("", "", "", ""),
                "");
          }

          @Override
          public DisplayResult setBrightness(int percent) {
            throw new AssertionError("must not be called");
          }

          @Override
          public DisplayResult setNightMode(boolean enabled) {
            return new DisplayResult(false, "");
          }
        };
    try (DisplayManager manager = new DisplayManager(backend)) {
      assertFalse(manager.setBrightness(0).join().success());
      assertFalse(manager.setBrightness(101).join().success());
    }
  }
}
