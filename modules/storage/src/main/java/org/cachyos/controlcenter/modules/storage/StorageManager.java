package org.cachyos.controlcenter.modules.storage;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class StorageManager implements AutoCloseable {
  private final StorageBackend backend;
  private final Path userHome;
  private final ExecutorService worker =
      Executors.newSingleThreadExecutor(Thread.ofPlatform().name("storage-manager").factory());

  public StorageManager(StorageBackend backend, Path userHome) {
    this.backend = backend;
    this.userHome = userHome.toAbsolutePath().normalize();
  }

  public CompletableFuture<StorageSnapshot> inspect() {
    return CompletableFuture.supplyAsync(backend::inspect, worker);
  }

  public CompletableFuture<List<LargeFile>> findLargeFiles() {
    if (!Files.isDirectory(userHome, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(userHome)) {
      return CompletableFuture.completedFuture(List.of());
    }
    return CompletableFuture.supplyAsync(() -> backend.findLargeFiles(userHome), worker);
  }

  @Override
  public void close() {
    worker.shutdownNow();
  }
}
