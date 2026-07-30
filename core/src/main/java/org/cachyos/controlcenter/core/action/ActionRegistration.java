package org.cachyos.controlcenter.core.action;

import java.util.Objects;

/** Immutable allowlist entry. */
public record ActionRegistration(
    ActionId id, String displayName, boolean privileged, ActionHandler handler) {
  public ActionRegistration {
    Objects.requireNonNull(id, "id");
    if (displayName == null || displayName.isBlank()) {
      throw new IllegalArgumentException("displayName must not be blank");
    }
    Objects.requireNonNull(handler, "handler");
  }
}
