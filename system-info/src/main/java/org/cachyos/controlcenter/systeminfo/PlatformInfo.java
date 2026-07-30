package org.cachyos.controlcenter.systeminfo;

import java.util.Objects;

/** Immutable platform facts available without commands or elevated privileges. */
public record PlatformInfo(
    OperatingSystemFamily operatingSystemFamily,
    String operatingSystemName,
    String operatingSystemVersion,
    String architecture,
    String desktopSession,
    String sessionType) {
  public PlatformInfo {
    Objects.requireNonNull(operatingSystemFamily, "operatingSystemFamily");
    operatingSystemName = safe(operatingSystemName);
    operatingSystemVersion = safe(operatingSystemVersion);
    architecture = safe(architecture);
    desktopSession = safe(desktopSession);
    sessionType = safe(sessionType);
  }

  private static String safe(String value) {
    return value == null || value.isBlank() ? "unbekannt" : value;
  }
}
