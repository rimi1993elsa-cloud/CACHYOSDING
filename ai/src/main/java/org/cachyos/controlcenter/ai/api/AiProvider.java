package org.cachyos.controlcenter.ai.api;

import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

/** Text-only online boundary. Implementations must not expose or receive execution capabilities. */
public interface AiProvider extends AutoCloseable {
  boolean available();

  String availabilityMessage();

  default void refreshAvailability() {}

  CompletionStage<Void> stream(AiRequest request, Consumer<AiStreamEvent> listener);

  void cancel();

  @Override
  void close();
}
