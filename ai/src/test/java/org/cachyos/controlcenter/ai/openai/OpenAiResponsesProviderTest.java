package org.cachyos.controlcenter.ai.openai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;
import org.cachyos.controlcenter.ai.api.AiRequest;
import org.cachyos.controlcenter.ai.api.AiStreamEvent;
import org.cachyos.controlcenter.ai.provider.AiConfiguration;
import org.junit.jupiter.api.Test;

class OpenAiResponsesProviderTest {
  @Test
  void staysOfflineWithoutASecret() {
    try (OpenAiResponsesProvider provider =
        new OpenAiResponsesProvider(
            AiConfiguration.defaults(), ignored -> java.util.Optional.empty())) {
      AtomicReference<AiStreamEvent> event = new AtomicReference<>();

      assertFalse(provider.available());
      assertThrows(
          CompletionException.class,
          () ->
              provider.stream(AiRequest.question("Testfrage"), event::set)
                  .toCompletableFuture()
                  .join());
      assertTrue(event.get().text().contains("API-Schlüssel"));
    }
  }

  @Test
  void refreshesAvailabilityWithoutRestartingTheProvider() {
    AtomicReference<char[]> secret = new AtomicReference<>();
    try (OpenAiResponsesProvider provider =
        new OpenAiResponsesProvider(
            AiConfiguration.defaults(),
            ignored -> {
              char[] value = secret.get();
              return value == null
                  ? java.util.Optional.empty()
                  : java.util.Optional.of(value.clone());
            })) {
      assertFalse(provider.available());

      secret.set("development-only-secret-value".toCharArray());
      provider.refreshAvailability();

      assertTrue(provider.available());
    }
  }
}
