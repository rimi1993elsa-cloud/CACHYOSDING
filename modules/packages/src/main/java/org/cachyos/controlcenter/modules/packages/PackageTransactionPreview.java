package org.cachyos.controlcenter.modules.packages;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record PackageTransactionPreview(
    UUID id,
    PackageAction action,
    String packageName,
    List<String> changes,
    long downloadBytes,
    long installedDeltaBytes,
    Instant createdAt) {
  public PackageTransactionPreview {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(action, "action");
    packageName = Objects.requireNonNull(packageName, "packageName");
    changes = List.copyOf(changes);
    Objects.requireNonNull(createdAt, "createdAt");
  }
}
