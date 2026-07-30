package org.cachyos.controlcenter.ui.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class ThemeManagerTest {
  @Test
  void respectsExplicitTheme() {
    assertEquals(
        ThemeMode.DARK, ThemeManager.resolvedMode(ThemeMode.DARK, Map.of(), new Properties()));
    assertEquals(
        ThemeMode.LIGHT,
        ThemeManager.resolvedMode(
            ThemeMode.LIGHT, Map.of("GTK_THEME", "Adwaita-dark"), new Properties()));
  }

  @Test
  void resolvesSystemThemeFromDesktopHint() {
    assertEquals(
        ThemeMode.DARK,
        ThemeManager.resolvedMode(
            ThemeMode.SYSTEM, Map.of("GTK_THEME", "Adwaita-dark"), new Properties()));
    assertEquals(
        ThemeMode.LIGHT, ThemeManager.resolvedMode(ThemeMode.SYSTEM, Map.of(), new Properties()));
  }
}
