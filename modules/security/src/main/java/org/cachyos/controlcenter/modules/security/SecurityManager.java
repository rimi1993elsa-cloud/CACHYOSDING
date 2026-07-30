package org.cachyos.controlcenter.modules.security;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SecurityManager implements AutoCloseable {
  private final SecurityBackend backend;
  private final SecurityMutationGateway gateway;
  private final ExecutorService worker =
      Executors.newSingleThreadExecutor(Thread.ofPlatform().name("security-manager").factory());

  public SecurityManager(SecurityBackend backend, SecurityMutationGateway gateway) {
    this.backend = backend;
    this.gateway = gateway;
  }

  public CompletableFuture<SecuritySnapshot> inspect() {
    return CompletableFuture.supplyAsync(backend::inspect, worker);
  }

  public CompletableFuture<SecurityOperationResult> setFirewallEnabled(boolean enabled) {
    return CompletableFuture.supplyAsync(
        () ->
            gateway.available()
                ? gateway.setFirewallEnabled(enabled)
                : new SecurityOperationResult(false, "Privilegierter Helper nicht verfügbar"),
        worker);
  }

  @Override
  public void close() {
    worker.shutdownNow();
  }
}
