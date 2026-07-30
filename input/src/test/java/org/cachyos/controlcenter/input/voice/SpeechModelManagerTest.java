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
  void rejectsMissingSelectedDirectory() {
    SpeechModelManager manager = new SpeechModelManager(temporary);
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class, () -> manager.select(temporary.resolve("missing")));
  }
}
