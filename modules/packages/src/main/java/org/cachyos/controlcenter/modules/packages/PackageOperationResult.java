package org.cachyos.controlcenter.modules.packages;

import java.util.Objects;

public record PackageOperationResult(boolean successful, String message) {
  public PackageOperationResult {
    message = Objects.requireNonNullElse(message, "");
  }
}
