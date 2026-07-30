package org.cachyos.controlcenter.systeminfo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class PlatformDetectorTest {
  @Test
  void detectsLinuxKdeWaylandWithoutStartingCommands() {
    Properties properties = new Properties();
    properties.setProperty("os.name", "Linux");
    properties.setProperty("os.version", "6.15.7-2-cachyos");
    properties.setProperty("os.arch", "amd64");

    PlatformInfo result =
        PlatformDetector.detect(
            properties, Map.of("XDG_CURRENT_DESKTOP", "KDE", "XDG_SESSION_TYPE", "wayland"));

    assertEquals(OperatingSystemFamily.LINUX, result.operatingSystemFamily());
    assertEquals("KDE", result.desktopSession());
    assertEquals("wayland", result.sessionType());
  }

  @Test
  void mapsMissingValuesToUnknown() {
    PlatformInfo result = PlatformDetector.detect(new Properties(), Map.of());

    assertEquals(OperatingSystemFamily.OTHER, result.operatingSystemFamily());
    assertEquals("unbekannt", result.architecture());
    assertEquals("unbekannt", result.desktopSession());
  }
}
