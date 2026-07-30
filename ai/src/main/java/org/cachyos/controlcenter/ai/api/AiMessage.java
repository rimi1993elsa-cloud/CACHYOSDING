package org.cachyos.controlcenter.ai.api;

import java.util.Objects;

/** Minimal display history; roles are deliberately closed. */
public record AiMessage(Role role, String text) {
  public AiMessage {
    Objects.requireNonNull(role, "role");
    text = Objects.requireNonNullElse(text, "").strip();
    if (text.isBlank() || text.length() > 16_000) {
      throw new IllegalArgumentException("Message text must be present and bounded");
    }
  }

  public enum Role {
    USER,
    ASSISTANT
  }
}
