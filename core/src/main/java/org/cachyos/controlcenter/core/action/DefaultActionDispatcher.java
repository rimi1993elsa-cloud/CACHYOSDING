package org.cachyos.controlcenter.core.action;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.cachyos.controlcenter.core.audit.ActionAuditEvent;
import org.cachyos.controlcenter.core.audit.AuditSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Allowlist-backed dispatcher that maps failures and writes parameter-free audit events. */
public final class DefaultActionDispatcher implements ActionDispatcher, AutoCloseable {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultActionDispatcher.class);

  private final ActionRegistry registry;
  private final ExecutorService executor;
  private final AuditSink auditSink;

  public DefaultActionDispatcher(
      ActionRegistry registry, ExecutorService executor, AuditSink auditSink) {
    this.registry = Objects.requireNonNull(registry, "registry");
    this.executor = Objects.requireNonNull(executor, "executor");
    this.auditSink = Objects.requireNonNull(auditSink, "auditSink");
  }

  @Override
  public CompletableFuture<ActionResult> dispatch(ActionRequest request) {
    Objects.requireNonNull(request, "request");
    Instant startedAt = Instant.now();
    ActionHandler handler = registry.find(request.actionId()).orElse(null);
    if (handler == null) {
      ActionResult result =
          ActionResult.rejected(
              "Diese Aktion ist nicht registriert.", "Unknown action id: " + request.actionId());
      audit(request, result, startedAt, false);
      return CompletableFuture.completedFuture(result);
    }

    boolean privileged = registry.isPrivileged(request.actionId());
    return CompletableFuture.supplyAsync(
            () -> {
              try {
                return Objects.requireNonNull(
                    handler.execute(request), "Action handler returned null");
              } catch (ActionRejectedException exception) {
                return ActionResult.rejected(
                    exception.getMessage(), exception.getClass().getSimpleName());
              } catch (RuntimeException exception) {
                LOGGER.error(
                    "Local action failed actionId={} exceptionType={}",
                    request.actionId(),
                    exception.getClass().getName());
                return ActionResult.failed(
                    "Die Aktion konnte nicht ausgeführt werden.",
                    exception.getClass().getSimpleName());
              }
            },
            executor)
        .whenComplete(
            (result, throwable) -> {
              ActionResult auditedResult =
                  throwable == null
                      ? result
                      : ActionResult.failed(
                          "Die Aktion konnte nicht ausgeführt werden.",
                          throwable.getClass().getSimpleName());
              audit(request, auditedResult, startedAt, privileged);
            });
  }

  @Override
  public void close() {
    executor.shutdownNow();
  }

  private void audit(
      ActionRequest request, ActionResult result, Instant startedAt, boolean privileged) {
    long durationMillis = Math.max(0, Duration.between(startedAt, Instant.now()).toMillis());
    auditSink.record(
        new ActionAuditEvent(
            Instant.now(),
            request.actionId(),
            request.source(),
            result.status(),
            durationMillis,
            privileged));
  }
}
