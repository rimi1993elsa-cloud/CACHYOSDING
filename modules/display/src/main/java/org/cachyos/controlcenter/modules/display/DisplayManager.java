package org.cachyos.controlcenter.modules.display;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class DisplayManager implements AutoCloseable {
  private final DisplayBackend backend;
  private final ExecutorService worker =
      Executors.newSingleThreadExecutor(Thread.ofPlatform().name("display-manager").factory());

  public DisplayManager(DisplayBackend backend) {
    this.backend = backend;
  }

  public CompletableFuture<DisplayState> inspect() {
    return CompletableFuture.supplyAsync(backend::inspect, worker);
  }

  public CompletableFuture<DisplayResult> setBrightness(int percent) {
    if (percent < 1 || percent > 100) {
      return CompletableFuture.completedFuture(
          new DisplayResult(false, "Helligkeit muss zwischen 1 und 100 Prozent liegen."));
    }
    return CompletableFuture.supplyAsync(() -> backend.setBrightness(percent), worker);
  }

  public CompletableFuture<DisplayResult> setNightMode(boolean enabled) {
    return CompletableFuture.supplyAsync(() -> backend.setNightMode(enabled), worker);
  }

  @Override
  public void close() {
    worker.shutdownNow();
  }
}
