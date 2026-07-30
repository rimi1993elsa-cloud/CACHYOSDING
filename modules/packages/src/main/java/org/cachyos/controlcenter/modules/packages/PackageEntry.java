package org.cachyos.controlcenter.modules.packages;

import java.util.Objects;

public record PackageEntry(
    String name, String version, String repository, String description, boolean installed) {
  public PackageEntry {
    name = Objects.requireNonNullElse(name, "");
    version = Objects.requireNonNullElse(version, "");
    repository = Objects.requireNonNullElse(repository, "");
    description = Objects.requireNonNullElse(description, "");
  }
}
