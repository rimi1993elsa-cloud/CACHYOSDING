package org.cachyos.controlcenter.input.intent;

import java.util.Objects;

/** Minimum safe application metadata exposed to the intent layer. */
public record RegisteredApplication(String id, String name) {
  public RegisteredApplication {
    id = Objects.requireNonNull(id, "id");
    name = Objects.requireNonNull(name, "name");
  }
}
