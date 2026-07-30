package org.cachyos.controlcenter.input.voice;

import java.util.Objects;

/** Stable Java Sound mixer selection without device secrets. */
public record MicrophoneDescriptor(String id, String displayName) {
  public MicrophoneDescriptor {
    id = Objects.requireNonNull(id, "id");
    displayName = Objects.requireNonNull(displayName, "displayName");
  }

  @Override
  public String toString() {
    return displayName;
  }
}
