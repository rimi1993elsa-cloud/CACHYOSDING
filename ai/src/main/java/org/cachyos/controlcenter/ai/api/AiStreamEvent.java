package org.cachyos.controlcenter.ai.api;

import java.util.Objects;

/** Sanitized streaming event for presentation. */
public record AiStreamEvent(State state, String text, long inputTokens, long outputTokens) {
  public AiStreamEvent {
    Objects.requireNonNull(state, "state");
    text = Objects.requireNonNullElse(text, "");
    if (inputTokens < 0 || outputTokens < 0) {
      throw new IllegalArgumentException("Token usage must not be negative");
    }
  }

  public static AiStreamEvent delta(String text) {
    return new AiStreamEvent(State.DELTA, text, 0, 0);
  }

  public static AiStreamEvent completed() {
    return new AiStreamEvent(State.COMPLETED, "", 0, 0);
  }

  public static AiStreamEvent usage(long inputTokens, long outputTokens) {
    return new AiStreamEvent(State.USAGE, "", inputTokens, outputTokens);
  }

  public static AiStreamEvent error(String message) {
    return new AiStreamEvent(State.ERROR, message, 0, 0);
  }

  public enum State {
    DELTA,
    USAGE,
    COMPLETED,
    ERROR
  }
}
