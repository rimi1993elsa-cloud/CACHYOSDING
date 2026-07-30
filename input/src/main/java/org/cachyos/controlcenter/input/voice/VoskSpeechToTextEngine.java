package org.cachyos.controlcenter.input.voice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javax.sound.sampled.TargetDataLine;
import org.vosk.Model;
import org.vosk.Recognizer;

/** Offline Vosk decoder that opens a microphone only for an active push-to-talk session. */
public final class VoskSpeechToTextEngine implements SpeechToTextEngine {
  private final MicrophoneCatalog microphones;
  private final ObjectMapper mapper = new ObjectMapper();
  private final ExecutorService worker =
      Executors.newSingleThreadExecutor(
          runnable -> {
            Thread thread = new Thread(runnable, "vosk-stt");
            thread.setDaemon(true);
            return thread;
          });
  private final AtomicBoolean recording = new AtomicBoolean();
  private volatile TargetDataLine line;
  private Model model;
  private Path loadedModel;

  public VoskSpeechToTextEngine(MicrophoneCatalog microphones) {
    this.microphones = microphones;
  }

  @Override
  public void start(
      Path modelDirectory, MicrophoneDescriptor microphone, Consumer<TranscriptEvent> listener) {
    if (!recording.compareAndSet(false, true)) {
      return;
    }
    listener.accept(new TranscriptEvent(TranscriptEvent.State.LOADING, ""));
    worker.execute(() -> recognize(modelDirectory, microphone, listener));
  }

  private void recognize(
      Path modelDirectory, MicrophoneDescriptor microphone, Consumer<TranscriptEvent> listener) {
    try {
      ensureModel(modelDirectory);
      if (!recording.get()) {
        listener.accept(new TranscriptEvent(TranscriptEvent.State.STOPPED, ""));
        return;
      }
      TargetDataLine activeLine = microphones.open(microphone);
      line = activeLine;
      activeLine.start();
      listener.accept(new TranscriptEvent(TranscriptEvent.State.RECORDING, ""));
      try (Recognizer recognizer = new Recognizer(model, 16_000)) {
        byte[] buffer = new byte[4_096];
        while (recording.get()) {
          int count = activeLine.read(buffer, 0, buffer.length);
          if (count <= 0) {
            continue;
          }
          if (recognizer.acceptWaveForm(buffer, count)) {
            emitJson(recognizer.getResult(), "text", TranscriptEvent.State.FINAL, listener);
          } else {
            emitJson(
                recognizer.getPartialResult(), "partial", TranscriptEvent.State.PARTIAL, listener);
          }
        }
        emitJson(recognizer.getFinalResult(), "text", TranscriptEvent.State.FINAL, listener);
      } finally {
        activeLine.stop();
        activeLine.close();
        line = null;
      }
      listener.accept(new TranscriptEvent(TranscriptEvent.State.STOPPED, ""));
    } catch (IOException
        | RuntimeException
        | javax.sound.sampled.LineUnavailableException exception) {
      listener.accept(
          new TranscriptEvent(
              TranscriptEvent.State.ERROR,
              "Spracherkennung konnte nicht gestartet werden: "
                  + exception.getClass().getSimpleName()));
    } finally {
      recording.set(false);
    }
  }

  private synchronized void ensureModel(Path modelDirectory) throws IOException {
    Path normalized = modelDirectory.toAbsolutePath().normalize();
    if (model != null && normalized.equals(loadedModel)) {
      return;
    }
    if (model != null) {
      model.close();
    }
    model = new Model(normalized.toString());
    loadedModel = normalized;
  }

  private void emitJson(
      String json, String field, TranscriptEvent.State state, Consumer<TranscriptEvent> listener) {
    try {
      JsonNode parsed = mapper.readTree(json);
      String text = parsed.path(field).asText().strip();
      if (!text.isEmpty()) {
        listener.accept(new TranscriptEvent(state, text));
      }
    } catch (IOException ignored) {
      // Invalid native output is ignored and never interpreted.
    }
  }

  @Override
  public void stop() {
    recording.set(false);
    TargetDataLine activeLine = line;
    if (activeLine != null) {
      activeLine.stop();
      activeLine.close();
    }
  }

  @Override
  public boolean recording() {
    return recording.get();
  }

  @Override
  public synchronized void close() {
    stop();
    worker.shutdownNow();
    if (model != null) {
      model.close();
      model = null;
    }
  }
}
