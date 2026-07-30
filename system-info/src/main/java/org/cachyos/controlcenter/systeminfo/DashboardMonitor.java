package org.cachyos.controlcenter.systeminfo;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/** Low-frequency background refresh that never runs work on the JavaFX thread. */
public final class DashboardMonitor implements AutoCloseable {
  private final DashboardDataSource source;
  private final ScheduledExecutorService scheduler;
  private final List<Consumer<DashboardMetrics>> listeners = new CopyOnWriteArrayList<>();
  private volatile DashboardMetrics latest;

  public DashboardMonitor(
      DashboardDataSource source, DashboardMetrics initialMetrics, Duration interval) {
    if (interval.compareTo(Duration.ofSeconds(10)) < 0) {
      throw new IllegalArgumentException("Dashboard interval must be at least ten seconds");
    }
    this.source = source;
    latest = initialMetrics;
    scheduler =
        Executors.newSingleThreadScheduledExecutor(
            runnable -> {
              Thread thread = new Thread(runnable, "dashboard-refresh");
              thread.setDaemon(true);
              return thread;
            });
    scheduler.scheduleWithFixedDelay(this::refresh, 0, interval.toSeconds(), TimeUnit.SECONDS);
  }

  public DashboardMetrics latest() {
    return latest;
  }

  public void subscribe(Consumer<DashboardMetrics> listener) {
    listeners.add(listener);
  }

  private void refresh() {
    try {
      DashboardMetrics metrics = source.read();
      latest = metrics;
      for (Consumer<DashboardMetrics> listener : listeners) {
        try {
          listener.accept(metrics);
        } catch (RuntimeException ignored) {
          // One presentation listener must not stop future metric refreshes.
        }
      }
    } catch (RuntimeException ignored) {
      // Keep the last valid snapshot and let the next scheduled refresh retry.
    }
  }

  @Override
  public void close() {
    scheduler.shutdownNow();
  }
}
