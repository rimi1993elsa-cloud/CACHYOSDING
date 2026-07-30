package org.cachyos.controlcenter.modules.applications;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/** Safe display metadata for one catalogued desktop application. */
public record ApplicationEntry(
    String id,
    String name,
    String comment,
    Optional<Path> icon,
    Path desktopFile,
    Optional<String> packageName,
    boolean favorite) {
  public ApplicationEntry {
    id = Objects.requireNonNull(id, "id");
    name = display(name);
    comment = Objects.requireNonNullElse(comment, "");
    icon = Objects.requireNonNull(icon, "icon");
    desktopFile = Objects.requireNonNull(desktopFile, "desktopFile").toAbsolutePath().normalize();
    packageName = Objects.requireNonNull(packageName, "packageName");
  }

  public ApplicationEntry withFavorite(boolean newFavorite) {
    return new ApplicationEntry(id, name, comment, icon, desktopFile, packageName, newFavorite);
  }

  public ApplicationEntry withPackageName(Optional<String> newPackageName) {
    return new ApplicationEntry(id, name, comment, icon, desktopFile, newPackageName, favorite);
  }

  private static String display(String value) {
    return value == null || value.isBlank() ? "Unbenannte Anwendung" : value;
  }
}
