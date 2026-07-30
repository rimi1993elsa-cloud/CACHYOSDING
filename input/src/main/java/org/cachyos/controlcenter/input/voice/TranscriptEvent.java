package org.cachyos.controlcenter.input.voice;

import java.util.Objects;

/** Visible STT state; text is never interpreted or dispatched by this component. */
public record TranscriptEvent(State state, String text) {
  public TranscriptEvent {
    Objects.requireNonNull(state, "state");
    text = Objects.requireNonNullElse(text, "");
  }

  public enum State {
    LOADING,
    RECORDING,
    PARTIAL,
    FINAL,
    STOPPED,
    ERROR
  }
}
