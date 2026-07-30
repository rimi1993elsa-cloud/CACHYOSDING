package org.cachyos.controlcenter.modules.power;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public final class PowerManager implements AutoCloseable {
  private static final Pattern PROFILE = Pattern.compile("[a-z][a-z0-9-]{0,31}");
  private static final Set<String> KNOWN_PROFILES =
      Set.of("power-saver", "balanced", "performance");
  private final PowerBackend backend;
  private final ExecutorService worker =
      Executors.newSingleThreadExecutor(Thread.ofPlatform().name("power-manager").factory());

  public PowerManager(PowerBackend backend) {
    this.backend = backend;
  }

  public CompletableFuture<PowerState> inspect() {
    return CompletableFuture.supplyAsync(backend::inspect, worker);
  }

  public CompletableFuture<PowerResult> setProfile(String profile) {
    if (profile == null
        || !PROFILE.matcher(profile).matches()
        || !KNOWN_PROFILES.contains(profile)) {
      return CompletableFuture.completedFuture(
          new PowerResult(false, "Unbekanntes Energieprofil."));
    }
    return CompletableFuture.supplyAsync(() -> backend.setProfile(profile), worker);
  }

  public CompletableFuture<PowerResult> suspend(boolean confirmed) {
    if (!confirmed) {
      return CompletableFuture.completedFuture(
          new PowerResult(false, "Suspend wurde nicht bestätigt."));
    }
    return CompletableFuture.supplyAsync(backend::suspend, worker);
  }

  public CompletableFuture<PowerResult> hibernate(String confirmation) {
    if (!"RUHEZUSTAND".equals(confirmation)) {
      return CompletableFuture.completedFuture(
          new PowerResult(false, "Für Hibernate ist RUHEZUSTAND einzugeben."));
    }
    return CompletableFuture.supplyAsync(backend::hibernate, worker);
  }

  @Override
  public void close() {
    worker.shutdownNow();
  }
}
