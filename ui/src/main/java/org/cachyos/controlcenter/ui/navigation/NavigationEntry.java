package org.cachyos.controlcenter.ui.navigation;

import java.util.Objects;

/** User-visible navigation metadata and honest availability state. */
public record NavigationEntry(
    NavigationId id, String label, String description, boolean enabled, String availability) {
  public NavigationEntry {
    Objects.requireNonNull(id, "id");
    label = requireText(label, "label");
    description = requireText(description, "description");
    availability = requireText(availability, "availability");
  }

  private static String requireText(String value, String name) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(name + " must not be blank");
    }
    return value;
  }
}
