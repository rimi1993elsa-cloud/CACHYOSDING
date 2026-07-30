package org.cachyos.controlcenter.core.module;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/** Immutable, read-only module snapshot. */
public record ModuleSnapshot(Map<String, Object> values, Instant capturedAt) {
  public ModuleSnapshot {
    values = Map.copyOf(Objects.requireNonNull(values, "values"));
    Objects.requireNonNull(capturedAt, "capturedAt");
  }

  public static ModuleSnapshot empty() {
    return new ModuleSnapshot(Map.of(), Instant.now());
  }
}
