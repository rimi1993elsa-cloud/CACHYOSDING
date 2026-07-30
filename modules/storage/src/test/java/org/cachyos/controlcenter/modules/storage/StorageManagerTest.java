package org.cachyos.controlcenter.modules.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StorageManagerTest {
  @TempDir Path temporary;

  @Test
  void analysisUsesOnlyConfiguredHome() {
    StorageBackend backend =
        new StorageBackend() {
          @Override
          public StorageSnapshot inspect() {
            return new StorageSnapshot(
                true, List.of(), List.of(), List.of(), false, "", Instant.now(), "");
          }

          @Override
          public List<LargeFile> findLargeFiles(Path root) {
            return List.of(new LargeFile(root.resolve("large"), 10));
          }
        };
    try (StorageManager manager = new StorageManager(backend, temporary)) {
      assertEquals(temporary.resolve("large"), manager.findLargeFiles().join().getFirst().path());
    }
  }
}
