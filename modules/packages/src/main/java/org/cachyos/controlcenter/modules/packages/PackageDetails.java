package org.cachyos.controlcenter.modules.packages;

import java.util.List;
import java.util.Objects;

public record PackageDetails(
    PackageEntry entry, String architecture, long installedSizeBytes, List<String> dependencies) {
  public PackageDetails {
    Objects.requireNonNull(entry, "entry");
    architecture = Objects.requireNonNullElse(architecture, "");
    dependencies = List.copyOf(dependencies);
  }
}
