package org.cachyos.controlcenter.input.voice;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/** Session model selection with automatic discovery of the packaged German desktop model. */
public final class SpeechModelManager {
  public static final String PACKAGED_MODEL_NAME = "vosk-model-small-de-0.15";
  private Path modelDirectory;

  public SpeechModelManager(Path dataDirectory) {
    this(dataDirectory, List.of(Path.of("/usr/share/vosk/models").resolve(PACKAGED_MODEL_NAME)));
  }

  SpeechModelManager(Path dataDirectory, List<Path> systemCandidates) {
    Path userModel =
        Objects.requireNonNull(dataDirectory, "dataDirectory")
            .resolve("models/vosk-de")
            .toAbsolutePath()
            .normalize();
    modelDirectory =
        systemCandidates.stream()
            .map(path -> path.toAbsolutePath().normalize())
            .filter(SpeechModelManager::isModel)
            .findFirst()
            .orElse(userModel);
  }

  public synchronized Path modelDirectory() {
    return modelDirectory;
  }

  public synchronized void select(Path directory) {
    Path normalized = Objects.requireNonNull(directory, "directory").toAbsolutePath().normalize();
    if (!isModel(normalized)) {
      throw new IllegalArgumentException("Directory is not a complete Vosk model");
    }
    modelDirectory = normalized;
  }

  public synchronized ModelStatus status() {
    Path model = modelDirectory;
    boolean valid = isModel(model);
    return new ModelStatus(
        model,
        valid,
        valid
            ? (model.getFileName().toString().equals(PACKAGED_MODEL_NAME)
                ? "Installiertes deutsches Vosk-Modell ist bereit."
                : "Deutsches Vosk-Modell ist bereit.")
            : "Deutsches Vosk-Paket fehlt. Installiere cachyos-control-center-stt-de.");
  }

  public record ModelStatus(Path directory, boolean available, String message) {}

  private static boolean isModel(Path model) {
    return Files.isRegularFile(model.resolve("am/final.mdl"))
        && Files.isDirectory(model.resolve("conf"))
        && Files.isDirectory(model.resolve("graph"));
  }
}
