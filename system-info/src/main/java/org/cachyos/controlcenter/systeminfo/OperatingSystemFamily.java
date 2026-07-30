package org.cachyos.controlcenter.systeminfo;

import java.util.Locale;

/** Coarse operating-system family used to guard Linux-only adapters. */
public enum OperatingSystemFamily {
  LINUX("Linux"),
  WINDOWS("Windows"),
  MACOS("macOS"),
  OTHER("Unbekannt");

  private final String displayName;

  OperatingSystemFamily(String displayName) {
    this.displayName = displayName;
  }

  public String displayName() {
    return displayName;
  }

  static OperatingSystemFamily fromOsName(String osName) {
    String normalized = osName.toLowerCase(Locale.ROOT);
    if (normalized.contains("linux")) {
      return LINUX;
    }
    if (normalized.contains("windows")) {
      return WINDOWS;
    }
    if (normalized.contains("mac") || normalized.contains("darwin")) {
      return MACOS;
    }
    return OTHER;
  }
}
