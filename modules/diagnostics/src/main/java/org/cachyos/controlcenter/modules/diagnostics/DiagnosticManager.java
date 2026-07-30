package org.cachyos.controlcenter.modules.diagnostics;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.cachyos.controlcenter.core.action.ActionId;

/** Runs all read-only probes off the UI thread and sanitizes every result centrally. */
public final class DiagnosticManager implements AutoCloseable {
  private final DiagnosticBackend backend;
  private final ExecutorService executor;

  public DiagnosticManager(DiagnosticBackend backend) {
    this.backend = backend;
    executor =
        Executors.newSingleThreadExecutor(
            runnable -> {
              Thread thread = new Thread(runnable, "local-diagnostics");
              thread.setDaemon(true);
              return thread;
            });
  }

  public CompletableFuture<DiagnosticReport> runAll() {
    return CompletableFuture.supplyAsync(
        () -> {
          List<DiagnosticFinding> findings = new ArrayList<>();
          for (DiagnosticCategory category : DiagnosticCategory.values()) {
            DiagnosticObservation observation;
            try {
              observation = backend.inspect(category);
            } catch (RuntimeException exception) {
              observation =
                  new DiagnosticObservation(
                      category,
                      DiagnosticStatus.ERROR,
                      "Die Diagnosekomponente ist unerwartet fehlgeschlagen.",
                      "");
            }
            findings.add(
                new DiagnosticFinding(
                    category,
                    observation.status(),
                    DiagnosticSanitizer.sanitize(observation.summary()),
                    DiagnosticSanitizer.sanitize(observation.technicalText()),
                    suggestedAction(category, observation.status())));
          }
          return new DiagnosticReport(Instant.now(), findings);
        },
        executor);
  }

  @Override
  public void close() {
    executor.shutdownNow();
  }

  private static Optional<ActionId> suggestedAction(
      DiagnosticCategory category, DiagnosticStatus status) {
    if (status == DiagnosticStatus.OK || status == DiagnosticStatus.UNAVAILABLE) {
      return Optional.empty();
    }
    return switch (category) {
      case NETWORK -> Optional.of(ActionId.NETWORK_SCAN_WIFI);
      case AUDIO -> Optional.of(ActionId.AUDIO_TEST_TONE);
      default -> Optional.empty();
    };
  }
}
