package org.cachyos.controlcenter.modules.packages;

import java.util.List;
import java.util.Optional;

public interface PackageBackend {
  boolean available();

  boolean locked();

  PackageSnapshot snapshot();

  List<PackageEntry> search(String query);

  Optional<PackageDetails> details(String packageName);

  List<String> preview(PackageAction action, String packageName);
}
