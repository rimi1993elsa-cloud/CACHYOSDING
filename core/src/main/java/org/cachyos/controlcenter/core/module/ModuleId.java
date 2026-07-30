package org.cachyos.controlcenter.core.module;

import java.util.Objects;
import java.util.regex.Pattern;

/** Stable module identifier. */
public record ModuleId(String value) {
  private static final Pattern FORMAT = Pattern.compile("[a-z][a-z0-9-]{2,47}");

  public ModuleId {
    Objects.requireNonNull(value, "value");
    if (!FORMAT.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid module id");
    }
  }
}
