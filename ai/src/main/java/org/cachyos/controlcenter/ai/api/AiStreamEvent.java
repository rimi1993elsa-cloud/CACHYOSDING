package org.cachyos.controlcenter.ai.api;

import java.util.Objects;

/** Sanitized streaming event for presentation. */
public record AiStreamEvent(State state, String text) {
  public AiStreamEvent {
    Objects.requireNonNull(state, "state");
    text = Objects.requireNonNullElse(text, "");
  }

  public static AiStreamEvent delta(String text) {
    return new AiStreamEvent(State.DELTA, text);
  }

  public static AiStreamEvent completed() {
    return new AiStreamEvent(State.COMPLETED, "");
  }

  public static AiStreamEvent error(String message) {
    return new AiStreamEvent(State.ERROR, message);
  }

  public enum State {
    DELTA,
    COMPLETED,
    ERROR
  }
}
