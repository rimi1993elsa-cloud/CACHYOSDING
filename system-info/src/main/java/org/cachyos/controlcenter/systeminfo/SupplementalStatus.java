package org.cachyos.controlcenter.systeminfo;

import java.util.OptionalInt;

/** Slowly changing read-only status reported by optional platform tools. */
public record SupplementalStatus(OptionalInt availableUpdates, OptionalInt failedServices) {
  public SupplementalStatus {
    availableUpdates = availableUpdates == null ? OptionalInt.empty() : availableUpdates;
    failedServices = failedServices == null ? OptionalInt.empty() : failedServices;
  }

  public static SupplementalStatus unavailable() {
    return new SupplementalStatus(OptionalInt.empty(), OptionalInt.empty());
  }
}
