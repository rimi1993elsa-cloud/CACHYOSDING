package org.cachyos.controlcenter.ui.theme;

import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import javafx.scene.Scene;

/** Applies bundled themes without network or system command access. */
public final class ThemeManager {
  private Scene scene;
  private ThemeMode mode = ThemeMode.SYSTEM;

  public ThemeMode mode() {
    return mode;
  }

  public void install(Scene targetScene) {
    scene = Objects.requireNonNull(targetScene, "targetScene");
    apply();
  }

  public void setMode(ThemeMode newMode) {
    mode = Objects.requireNonNull(newMode, "newMode");
    apply();
  }

  public static ThemeMode resolvedMode(
      ThemeMode requested, Map<String, String> environment, Properties properties) {
    Objects.requireNonNull(requested, "requested");
    if (requested != ThemeMode.SYSTEM) {
      return requested;
    }
    String hint =
        String.join(
                " ",
                safe(environment.get("GTK_THEME")),
                safe(environment.get("KDE_COLOR_SCHEME")),
                safe(properties.getProperty("ui.theme")))
            .toLowerCase(Locale.ROOT);
    return hint.contains("dark") || hint.contains("dunkel") ? ThemeMode.DARK : ThemeMode.LIGHT;
  }

  private void apply() {
    if (scene == null) {
      return;
    }
    ThemeMode resolved = resolvedMode(mode, System.getenv(), System.getProperties());
    scene.getStylesheets().setAll(resource("base.css"), resource(fileName(resolved)));
  }

  private static String fileName(ThemeMode resolved) {
    return resolved == ThemeMode.DARK ? "dark.css" : "light.css";
  }

  private static String resource(String name) {
    URL url = ThemeManager.class.getResource("/org/cachyos/controlcenter/ui/theme/" + name);
    if (url == null) {
      throw new IllegalStateException("Missing theme resource: " + name);
    }
    return url.toExternalForm();
  }

  private static String safe(String value) {
    return value == null ? "" : value;
  }
}
