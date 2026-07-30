package org.cachyos.controlcenter.platform.applications;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class DesktopApplicationBackendTest {
  @Test
  void readsOnlyDesktopEntrySectionAndPrefersFirstValue() {
    var values =
        DesktopApplicationBackend.desktopValues(
            List.of(
                "[Other]",
                "Name=Ignored",
                "[Desktop Entry]",
                "Type=Application",
                "Name=First",
                "Name=Second"));

    assertEquals("Application", values.get("Type"));
    assertEquals("First", values.get("Name"));
  }
}
