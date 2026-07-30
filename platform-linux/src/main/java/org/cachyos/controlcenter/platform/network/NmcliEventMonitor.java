package org.cachyos.controlcenter.platform.network;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import org.cachyos.controlcenter.modules.network.NetworkEvents;
import org.cachyos.controlcenter.systeminfo.Capability;
import org.cachyos.controlcenter.systeminfo.CapabilityRegistry;

/** Streams fixed `nmcli monitor` change notifications on a daemon reader thread. */
public final class NmcliEventMonitor implements NetworkEvents {
  private final List<Runnable> listeners = new CopyOnWriteArrayList<>();
  private final AtomicBoolean closed = new AtomicBoolean();
  private volatile Process process;

  public NmcliEventMonitor(CapabilityRegistry capabilities) {
    Optional<Path> executable = capabilities.status(Capability.NMCLI).executable();
    if (executable.isPresent()) {
      Thread reader = new Thread(() -> monitor(executable.get()), "network-events");
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
          new ProcessBuilder(executable.toAbsolutePath().normalize().toString(), "monitor")
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
      // Missing stream remains an explicit unavailable fallback in the page snapshot.
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
