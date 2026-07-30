package org.cachyos.controlcenter.modules.packages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.Test;

class PackageManagerTest {
  @Test
  void requiresPreviewBeforeMutation() {
    FakeBackend backend = new FakeBackend();
    FakeGateway gateway = new FakeGateway();
    try (PackageManager manager = new PackageManager(backend, gateway)) {
      PackageTransactionPreview preview = manager.preview(PackageAction.INSTALL, "nano").join();
      assertFalse(gateway.called);
      assertTrue(manager.confirm(preview.id()).join().successful());
      assertTrue(gateway.called);
    }
  }

  @Test
  void refusesDatabaseLockAtPreviewAndExecution() {
    FakeBackend backend = new FakeBackend();
    FakeGateway gateway = new FakeGateway();
    try (PackageManager manager = new PackageManager(backend, gateway)) {
      backend.locked = true;
      CompletionException previewError =
          assertThrows(
              CompletionException.class,
              () -> manager.preview(PackageAction.INSTALL, "nano").join());
      assertTrue(previewError.getCause().getMessage().contains("gesperrt"));
      backend.locked = false;
      PackageTransactionPreview preview = manager.preview(PackageAction.INSTALL, "nano").join();
      backend.locked = true;
      assertThrows(CompletionException.class, () -> manager.confirm(preview.id()).join());
      assertFalse(gateway.called);
    }
  }

  @Test
  void rejectsInjectionBeforeBackend() {
    FakeBackend backend = new FakeBackend();
    try (PackageManager manager = new PackageManager(backend, new FakeGateway())) {
      assertThrows(
          CompletionException.class,
          () -> manager.preview(PackageAction.INSTALL, "nano;id").join());
      assertThrows(CompletionException.class, () -> manager.search("linux $(id)").join());
      assertEquals(0, backend.calls);
    }
  }

  private static final class FakeBackend implements PackageBackend {
    private boolean locked;
    private int calls;

    @Override
    public boolean available() {
      return true;
    }

    @Override
    public boolean locked() {
      return locked;
    }

    @Override
    public PackageSnapshot snapshot() {
      calls++;
      return new PackageSnapshot(
          true, locked, List.of(), List.of(), List.of(), 0, Instant.now(), "");
    }

    @Override
    public List<PackageEntry> search(String query) {
      calls++;
      return List.of();
    }

    @Override
    public Optional<PackageDetails> details(String packageName) {
      calls++;
      return Optional.empty();
    }

    @Override
    public List<String> preview(PackageAction action, String packageName) {
      calls++;
      return List.of(action + " " + packageName);
    }
  }

  private static final class FakeGateway implements PackageMutationGateway {
    private boolean called;

    @Override
    public boolean available() {
      return true;
    }

    @Override
    public PackageOperationResult execute(PackageAction action, String packageName) {
      called = true;
      return new PackageOperationResult(true, "ok");
    }
  }
}
