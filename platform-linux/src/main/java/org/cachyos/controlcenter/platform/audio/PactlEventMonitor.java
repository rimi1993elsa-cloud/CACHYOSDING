package org.cachyos.controlcenter.platform.audio;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import org.cachyos.controlcenter.modules.audio.AudioEvents;
import org.cachyos.controlcenter.systeminfo.Capability;
import org.cachyos.controlcenter.systeminfo.CapabilityRegistry;

/** Streams `pactl subscribe` notifications without microphone capture or polling. */
public final class PactlEventMonitor implements AudioEvents {
  private final List<Runnable> listeners = new CopyOnWriteArrayList<>();
  private final AtomicBoolean closed = new AtomicBoolean();
  private volatile Process process;

  public PactlEventMonitor(CapabilityRegistry capabilities) {
    Optional<Path> executable = capabilities.status(Capability.PACTL).executable();
    if (executable.isPresent()) {
      Thread reader = new Thread(() -> monitor(executable.get()), "audio-events");
      reader.setDaemon(true);
      reader.start();
    }
  }

  @Override
  public void subscribe(Runnable listener) {
    listeners.add(listener);
  }

  private void monitor(Path executable) {
    if (closed.get()) {
      return;
    }
    try {
      process =
          new ProcessBuilder(executable.toAbsolutePath().normalize().toString(), "subscribe")
              .redirectErrorStream(true)
              .start();
      if (closed.get()) {
        process.destroy();
        return;
      }
      try (var lines = process.inputReader().lines()) {
        lines
            .takeWhile(ignored -> !closed.get())
            .forEach(ignored -> listeners.forEach(Runnable::run));
      }
    } catch (IOException ignored) {
      // Snapshot provides the user-visible unavailable state.
    }
  }

  @Override
  public void close() {
    closed.set(true);
    Process active = process;
    if (active != null) {
      active.destroy();
    }
  }
}
