package org.cachyos.controlcenter.input.voice;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/** Session model selection with an XDG-compatible default and local validation. */
public final class SpeechModelManager {
  private Path modelDirectory;

  public SpeechModelManager(Path dataDirectory) {
    modelDirectory =
        Objects.requireNonNull(dataDirectory, "dataDirectory")
            .resolve("models/vosk-de")
            .normalize();
  }

  public synchronized Path modelDirectory() {
    return modelDirectory;
  }

  public synchronized void select(Path directory) {
    Path normalized = Objects.requireNonNull(directory, "directory").toAbsolutePath().normalize();
    if (!Files.isDirectory(normalized)) {
      throw new IllegalArgumentException("Model directory does not exist");
    }
    modelDirectory = normalized;
  }

  public synchronized ModelStatus status() {
    Path model = modelDirectory;
    boolean valid =
        Files.isRegularFile(model.resolve("am/final.mdl"))
            && Files.isDirectory(model.resolve("conf"))
            && Files.isDirectory(model.resolve("graph"));
    return new ModelStatus(
        model,
        valid,
        valid ? "Deutsches Vosk-Modell ist bereit." : "Kein vollständiges Vosk-Modell gefunden.");
  }

  public record ModelStatus(Path directory, boolean available, String message) {}
}
