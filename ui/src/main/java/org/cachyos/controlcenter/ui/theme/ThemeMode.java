package org.cachyos.controlcenter.ui.theme;

/** User-facing theme choice. */
public enum ThemeMode {
  SYSTEM("System"),
  LIGHT("Hell"),
  DARK("Dunkel");

  private final String displayName;

  ThemeMode(String displayName) {
    this.displayName = displayName;
  }

  public String displayName() {
    return displayName;
  }

  @Override
  public String toString() {
    return displayName;
  }
}
