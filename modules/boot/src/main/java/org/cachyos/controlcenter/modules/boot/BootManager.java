package org.cachyos.controlcenter.modules.boot;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class BootManager implements AutoCloseable {
  private final BootBackend backend;
  private final ExecutorService worker =
      Executors.newSingleThreadExecutor(Thread.ofPlatform().name("boot-manager").factory());

  public BootManager(BootBackend backend) {
    this.backend = backend;
  }

  public CompletableFuture<BootSnapshot> inspect() {
    return CompletableFuture.supplyAsync(backend::inspect, worker);
  }

  public CompletableFuture<BootResult> launchKernelManager() {
    return CompletableFuture.supplyAsync(backend::launchKernelManager, worker);
  }

  @Override
  public void close() {
    worker.shutdownNow();
  }
}
