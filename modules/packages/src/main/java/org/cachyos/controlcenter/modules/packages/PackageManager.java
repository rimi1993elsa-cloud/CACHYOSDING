package org.cachyos.controlcenter.modules.packages;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public final class PackageManager implements AutoCloseable {
  private static final Duration CACHE_LIFETIME = Duration.ofSeconds(60);
  private static final Duration PREVIEW_LIFETIME = Duration.ofMinutes(2);

  private final PackageBackend backend;
  private final PackageMutationGateway gateway;
  private final Clock clock;
  private final ExecutorService worker;
  private final Map<UUID, PackageTransactionPreview> previews = new ConcurrentHashMap<>();
  private volatile PackageSnapshot cached;
  private volatile Consumer<PackageProgress> progress = ignored -> {};

  public PackageManager(PackageBackend backend, PackageMutationGateway gateway) {
    this(backend, gateway, Clock.systemUTC());
  }

  PackageManager(PackageBackend backend, PackageMutationGateway gateway, Clock clock) {
    this.backend = backend;
    this.gateway = gateway;
    this.clock = clock;
    worker =
        Executors.newSingleThreadExecutor(Thread.ofPlatform().name("package-manager").factory());
  }

  public void onProgress(Consumer<PackageProgress> listener) {
    progress = listener == null ? ignored -> {} : listener;
  }

  public CompletableFuture<PackageSnapshot> snapshot(boolean refresh) {
    PackageSnapshot current = cached;
    if (!refresh
        && current != null
        && current.capturedAt().plus(CACHE_LIFETIME).isAfter(clock.instant())) {
      return CompletableFuture.completedFuture(current);
    }
    return submit(
        PackageProgress.State.READING,
        () -> {
          PackageSnapshot read = backend.snapshot();
          cached = read;
          return read;
        });
  }

  public CompletableFuture<List<PackageEntry>> search(String query) {
    if (!PackageNames.validQuery(query)) {
      return CompletableFuture.failedFuture(new IllegalArgumentException("Ungültige Paketsuche"));
    }
    if (!backend.available()) {
      return CompletableFuture.failedFuture(new IllegalStateException("Pacman nicht verfügbar"));
    }
    return submit(PackageProgress.State.READING, () -> backend.search(query));
  }

  public CompletableFuture<Optional<PackageDetails>> details(String packageName) {
    if (!PackageNames.valid(packageName)) {
      return CompletableFuture.failedFuture(new IllegalArgumentException("Ungültiger Paketname"));
    }
    if (!backend.available()) {
      return CompletableFuture.failedFuture(new IllegalStateException("Pacman nicht verfügbar"));
    }
    return submit(PackageProgress.State.READING, () -> backend.details(packageName));
  }

  public CompletableFuture<PackageTransactionPreview> preview(
      PackageAction action, String packageName) {
    if (!PackageNames.valid(packageName)) {
      return CompletableFuture.failedFuture(new IllegalArgumentException("Ungültiger Paketname"));
    }
    return submit(
        PackageProgress.State.PREVIEWING,
        () -> {
          ensureUnlocked();
          List<String> changes = backend.preview(action, packageName);
          if (changes.isEmpty()) {
            throw new IllegalStateException("Pacman konnte keine Transaktionsvorschau erstellen");
          }
          long totalBytes = changes.stream().mapToLong(PackageManager::previewBytes).sum();
          PackageTransactionPreview preview =
              new PackageTransactionPreview(
                  UUID.randomUUID(),
                  action,
                  packageName,
                  changes,
                  action == PackageAction.INSTALL ? totalBytes : 0,
                  action == PackageAction.REMOVE ? -totalBytes : totalBytes,
                  clock.instant());
          previews.put(preview.id(), preview);
          return preview;
        });
  }

  public CompletableFuture<PackageOperationResult> confirm(UUID previewId) {
    PackageTransactionPreview preview = previews.remove(previewId);
    if (preview == null || preview.createdAt().plus(PREVIEW_LIFETIME).isBefore(clock.instant())) {
      return CompletableFuture.completedFuture(
          new PackageOperationResult(false, "Vorschau fehlt oder ist abgelaufen"));
    }
    return submit(
        PackageProgress.State.AUTHORIZING,
        () -> {
          ensureUnlocked();
          if (!gateway.available()) {
            return new PackageOperationResult(false, "Privilegierter Helper nicht verfügbar");
          }
          progress.accept(new PackageProgress(PackageProgress.State.RUNNING, "Pacman arbeitet"));
          PackageOperationResult result = gateway.execute(preview.action(), preview.packageName());
          if (result.successful()) {
            cached = null;
          }
          return result;
        });
  }

  private void ensureUnlocked() {
    if (backend.locked()) {
      throw new IllegalStateException(
          "Pacman-Datenbank ist durch eine andere Transaktion gesperrt");
    }
  }

  private <T> CompletableFuture<T> submit(
      PackageProgress.State state, java.util.concurrent.Callable<T> operation) {
    progress.accept(new PackageProgress(state, "Paketdaten werden verarbeitet"));
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            T result = operation.call();
            progress.accept(new PackageProgress(PackageProgress.State.COMPLETED, "Abgeschlossen"));
            return result;
          } catch (RuntimeException exception) {
            progress.accept(
                new PackageProgress(PackageProgress.State.FAILED, exception.getMessage()));
            throw exception;
          } catch (Exception exception) {
            progress.accept(
                new PackageProgress(PackageProgress.State.FAILED, "Paketoperation fehlgeschlagen"));
            throw new IllegalStateException("Paketoperation fehlgeschlagen", exception);
          }
        },
        worker);
  }

  private static long previewBytes(String line) {
    String[] fields = line.split("\\t");
    if (fields.length < 3) {
      return 0;
    }
    try {
      return Long.parseLong(fields[2]);
    } catch (NumberFormatException exception) {
      return 0;
    }
  }

  @Override
  public void close() {
    previews.clear();
    worker.shutdownNow();
  }
}
