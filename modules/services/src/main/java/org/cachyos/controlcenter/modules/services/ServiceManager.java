package org.cachyos.controlcenter.modules.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public final class ServiceManager implements AutoCloseable {
  private static final Pattern UNIT =
      Pattern.compile("[A-Za-z0-9:_.@\\\\\\-]{1,160}\\.(service|socket|timer|mount|target)");
  private final ServiceBackend backend;
  private final ServiceGateway gateway;
  private final ExecutorService worker =
      Executors.newSingleThreadExecutor(Thread.ofPlatform().name("service-manager").factory());

  public ServiceManager(ServiceBackend backend, ServiceGateway gateway) {
    this.backend = backend;
    this.gateway = gateway;
  }

  public CompletableFuture<ServiceState> inspect() {
    return CompletableFuture.supplyAsync(backend::inspect, worker);
  }

  public CompletableFuture<List<String>> logs(ServiceScope scope, String unitName) {
    if (!valid(unitName)) {
      return CompletableFuture.completedFuture(List.of("Ungültiger Unit-Name"));
    }
    return CompletableFuture.supplyAsync(() -> backend.logs(scope, unitName), worker);
  }

  public CompletableFuture<ServiceResult> execute(
      ServiceScope scope, String unitName, ServiceOperation operation) {
    if (!valid(unitName)) {
      return CompletableFuture.completedFuture(new ServiceResult(false, "Ungültiger Unit-Name"));
    }
    return CompletableFuture.supplyAsync(() -> gateway.execute(scope, unitName, operation), worker);
  }

  public static boolean valid(String unitName) {
    return unitName != null && UNIT.matcher(unitName).matches();
  }

  @Override
  public void close() {
    worker.shutdownNow();
  }
}
