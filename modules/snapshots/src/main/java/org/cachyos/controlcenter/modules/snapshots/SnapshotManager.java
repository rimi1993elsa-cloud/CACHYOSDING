package org.cachyos.controlcenter.modules.snapshots;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public final class SnapshotManager implements AutoCloseable {
  private static final Pattern DESCRIPTION = Pattern.compile("[\\p{L}\\p{N} .,()_+\\-]{1,120}");
  private final SnapshotBackend backend;
  private final SnapshotGateway gateway;
  private final ExecutorService worker =
      Executors.newSingleThreadExecutor(Thread.ofPlatform().name("snapshot-manager").factory());

  public SnapshotManager(SnapshotBackend backend, SnapshotGateway gateway) {
    this.backend = backend;
    this.gateway = gateway;
  }

  public CompletableFuture<SnapshotState> inspect() {
    return CompletableFuture.supplyAsync(backend::inspect, worker);
  }

  public CompletableFuture<SnapshotResult> create(String description) {
    if (description == null || !DESCRIPTION.matcher(description).matches()) {
      return CompletableFuture.completedFuture(new SnapshotResult(false, "Ungültige Beschreibung"));
    }
    return CompletableFuture.supplyAsync(
        () ->
            gateway.available()
                ? gateway.create(description)
                : new SnapshotResult(false, "Snapper-Helper nicht verfügbar"),
        worker);
  }

  public CompletableFuture<SnapshotResult> delete(int id, String typedConfirmation) {
    if (id <= 0 || !Integer.toString(id).equals(typedConfirmation)) {
      return CompletableFuture.completedFuture(
          new SnapshotResult(false, "Snapshot-ID wurde nicht korrekt bestätigt"));
    }
    return CompletableFuture.supplyAsync(
        () ->
            gateway.available()
                ? gateway.delete(id)
                : new SnapshotResult(false, "Snapper-Helper nicht verfügbar"),
        worker);
  }

  @Override
  public void close() {
    worker.shutdownNow();
  }
}
