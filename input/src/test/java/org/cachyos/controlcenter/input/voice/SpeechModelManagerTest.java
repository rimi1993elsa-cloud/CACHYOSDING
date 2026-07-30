package org.cachyos.controlcenter.input.voice;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SpeechModelManagerTest {
  @TempDir Path temporary;

  @Test
  void requiresRealVoskModelStructure() throws IOException {
    SpeechModelManager manager = new SpeechModelManager(temporary);
    assertFalse(manager.status().available());

    Path model = temporary.resolve("german");
    Files.createDirectories(model.resolve("am"));
    Files.createDirectories(model.resolve("conf"));
    Files.createDirectories(model.resolve("graph"));
    Files.createFile(model.resolve("am/final.mdl"));
    manager.select(model);

    assertTrue(manager.status().available());
  }

  @Test
  void discoversPackagedSystemModelBeforeUserFallback() throws IOException {
    Path packaged = temporary.resolve(SpeechModelManager.PACKAGED_MODEL_NAME);
    Files.createDirectories(packaged.resolve("am"));
    Files.createDirectories(packaged.resolve("conf"));
    Files.createDirectories(packaged.resolve("graph"));
    Files.createFile(packaged.resolve("am/final.mdl"));

    SpeechModelManager manager =
        new SpeechModelManager(temporary.resolve("data"), java.util.List.of(packaged));

    assertTrue(manager.status().available());
    org.junit.jupiter.api.Assertions.assertEquals(packaged, manager.modelDirectory());
  }

  @Test
  void rejectsMissingSelectedDirectory() {
    SpeechModelManager manager = new SpeechModelManager(temporary);
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class, () -> manager.select(temporary.resolve("missing")));
  }

  @Test
  void rejectsIncompleteSelectedDirectory() throws IOException {
    Path incomplete = Files.createDirectory(temporary.resolve("incomplete"));
    SpeechModelManager manager = new SpeechModelManager(temporary);
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class, () -> manager.select(incomplete));
  }
}
