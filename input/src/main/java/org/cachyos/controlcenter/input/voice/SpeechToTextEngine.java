package org.cachyos.controlcenter.input.voice;

import java.nio.file.Path;
import java.util.function.Consumer;

/** Replaceable offline speech-to-text boundary with explicit push-to-talk lifecycle. */
public interface SpeechToTextEngine extends AutoCloseable {
  void start(
      Path modelDirectory, MicrophoneDescriptor microphone, Consumer<TranscriptEvent> listener);

  void stop();

  boolean recording();

  @Override
  void close();
}
