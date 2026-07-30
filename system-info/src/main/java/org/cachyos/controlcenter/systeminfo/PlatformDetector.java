package org.cachyos.controlcenter.systeminfo;

import java.util.Map;
import java.util.Properties;

/**
 * Detects only non-sensitive platform facts from JVM properties and the process environment.
 *
 * <p>No command is started and no file is read in Phase 0.
 */
public final class PlatformDetector {
  private PlatformDetector() {}

  public static PlatformInfo detect() {
    return detect(System.getProperties(), System.getenv());
  }

  static PlatformInfo detect(Properties properties, Map<String, String> environment) {
    String osName = properties.getProperty("os.name", "");
    return new PlatformInfo(
        OperatingSystemFamily.fromOsName(osName),
        osName,
        properties.getProperty("os.version"),
        properties.getProperty("os.arch"),
        firstNonBlank(
            environment.get("XDG_CURRENT_DESKTOP"),
            environment.get("XDG_SESSION_DESKTOP"),
            environment.get("DESKTOP_SESSION")),
        environment.get("XDG_SESSION_TYPE"));
  }

  private static String firstNonBlank(String... values) {
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return null;
  }
}
