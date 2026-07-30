package org.cachyos.controlcenter.core.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.cachyos.controlcenter.core.audit.InMemoryAuditLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class DefaultActionDispatcherTest {
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  @AfterEach
  void stopExecutor() {
    executor.shutdownNow();
  }

  @Test
  void executesRegisteredActionAsynchronouslyAndAuditsIt() throws Exception {
    ActionRegistry registry = new ActionRegistry();
    AtomicReference<String> threadName = new AtomicReference<>();
    registry.register(
        new ActionRegistration(
            ActionId.OPEN_FIREFOX,
            "Firefox öffnen",
            false,
            request -> {
              threadName.set(Thread.currentThread().getName());
              return ActionResult.success("Gestartet");
            }));
    InMemoryAuditLog audit = new InMemoryAuditLog();
    DefaultActionDispatcher dispatcher = new DefaultActionDispatcher(registry, executor, audit);

    ActionResult result =
        dispatcher
            .dispatch(ActionRequest.fromButton(ActionId.OPEN_FIREFOX))
            .toCompletableFuture()
            .get(5, TimeUnit.SECONDS);

    assertEquals(ActionStatus.SUCCESS, result.status());
    assertFalse(threadName.get().equals(Thread.currentThread().getName()));
    assertEquals(1, audit.events().size());
    assertEquals(ActionId.OPEN_FIREFOX, audit.events().getFirst().actionId());
    assertFalse(audit.events().getFirst().privileged());
  }

  @Test
  void rejectsUnknownActionAndAuditsRejection() {
    InMemoryAuditLog audit = new InMemoryAuditLog();
    DefaultActionDispatcher dispatcher =
        new DefaultActionDispatcher(new ActionRegistry(), executor, audit);

    ActionResult result =
        dispatcher
            .dispatch(ActionRequest.fromButton(ActionId.of("test.unknown-action")))
            .toCompletableFuture()
            .join();

    assertEquals(ActionStatus.REJECTED, result.status());
    assertEquals(ActionStatus.REJECTED, audit.events().getFirst().result());
  }

  @Test
  void mapsHandlerFailureWithoutLeakingExceptionMessage() {
    ActionRegistry registry = new ActionRegistry();
    registry.register(
        new ActionRegistration(
            ActionId.OPEN_FIREFOX,
            "Firefox öffnen",
            false,
            request -> {
              throw new IllegalStateException("secret-value");
            }));
    DefaultActionDispatcher dispatcher =
        new DefaultActionDispatcher(registry, executor, event -> {});

    ActionResult result =
        dispatcher
            .dispatch(ActionRequest.fromButton(ActionId.OPEN_FIREFOX))
            .toCompletableFuture()
            .join();

    assertEquals(ActionStatus.FAILED, result.status());
    assertTrue(result.technicalMessage().contains("IllegalStateException"));
    assertFalse(result.technicalMessage().contains("secret-value"));
  }
}
