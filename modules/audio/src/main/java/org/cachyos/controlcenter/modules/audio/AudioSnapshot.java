package org.cachyos.controlcenter.modules.audio;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** Secret-free PipeWire/PulseAudio compatibility state. */
public record AudioSnapshot(
    boolean available,
    String server,
    List<Device> outputs,
    List<Device> inputs,
    List<Stream> streams,
    String message,
    Instant capturedAt) {
  public AudioSnapshot {
    server = safe(server);
    outputs = List.copyOf(outputs);
    inputs = List.copyOf(inputs);
    streams = List.copyOf(streams);
    message = Objects.requireNonNullElse(message, "");
    capturedAt = Objects.requireNonNull(capturedAt, "capturedAt");
  }

  public static AudioSnapshot unavailable(String message) {
    return new AudioSnapshot(false, "", List.of(), List.of(), List.of(), message, Instant.now());
  }

  public record Device(
      String name, String description, int volumePercent, boolean muted, boolean defaultDevice) {
    public Device {
      name = safe(name);
      description = safe(description);
      volumePercent = Math.max(0, Math.min(150, volumePercent));
    }
  }

  public record Stream(String id, String application, int volumePercent, boolean muted) {
    public Stream {
      id = safe(id);
      application = safe(application);
      volumePercent = Math.max(0, Math.min(150, volumePercent));
    }
  }

  private static String safe(String value) {
    return value == null || value.isBlank() ? "Nicht verfügbar" : value;
  }
}
