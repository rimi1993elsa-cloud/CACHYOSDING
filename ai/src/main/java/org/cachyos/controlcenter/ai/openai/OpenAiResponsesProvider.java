package org.cachyos.controlcenter.ai.openai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.http.StreamResponse;
import com.openai.errors.RateLimitException;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseStreamEvent;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.cachyos.controlcenter.ai.api.AiProvider;
import org.cachyos.controlcenter.ai.api.AiRequest;
import org.cachyos.controlcenter.ai.api.AiStreamEvent;
import org.cachyos.controlcenter.ai.prompt.SystemPromptBuilder;
import org.cachyos.controlcenter.ai.provider.AiConfiguration;
import org.cachyos.controlcenter.ai.provider.SecretStore;

/** Official-SDK Responses API adapter with no dependency on local execution components. */
public final class OpenAiResponsesProvider implements AiProvider {
  private final AiConfiguration configuration;
  private final Optional<OpenAIClient> client;
  private final ExecutorService executor;
  private final AtomicReference<Future<?>> active = new AtomicReference<>();

  public OpenAiResponsesProvider(AiConfiguration configuration, SecretStore secrets) {
    this.configuration = Objects.requireNonNull(configuration, "configuration");
    Objects.requireNonNull(secrets, "secrets");
    char[] secret = secrets.readSecret("openai-api-key").orElse(null);
    try {
      client =
          secret == null
              ? Optional.empty()
              : Optional.of(
                  OpenAIOkHttpClient.builder().apiKey(new String(secret)).maxRetries(2).build());
    } finally {
      if (secret != null) {
        Arrays.fill(secret, '\0');
      }
    }
    executor =
        Executors.newSingleThreadExecutor(
            runnable -> {
              Thread thread = new Thread(runnable, "openai-stream");
              thread.setDaemon(true);
              return thread;
            });
  }

  @Override
  public boolean available() {
    return client.isPresent();
  }

  @Override
  public String availabilityMessage() {
    return available()
        ? "OpenAI ist konfiguriert."
        : "Kein API-Schlüssel im Secret Service oder in OPENAI_API_KEY gefunden.";
  }

  @Override
  public CompletableFuture<Void> stream(AiRequest request, Consumer<AiStreamEvent> listener) {
    Objects.requireNonNull(request, "request");
    Objects.requireNonNull(listener, "listener");
    if (client.isEmpty()) {
      listener.accept(AiStreamEvent.error(availabilityMessage()));
      return CompletableFuture.failedFuture(new IllegalStateException("AI unavailable"));
    }
    cancel();
    CompletableFuture<Void> completion = new CompletableFuture<>();
    Future<?> future =
        executor.submit(
            () -> {
              try {
                ResponseCreateParams params =
                    ResponseCreateParams.builder()
                        .model(configuration.model())
                        .instructions(SystemPromptBuilder.build(request))
                        .input(buildInput(request))
                        .maxOutputTokens(configuration.maximumOutputTokens())
                        .store(false)
                        .build();
                try (StreamResponse<ResponseStreamEvent> response =
                    client.orElseThrow().responses().createStreaming(params)) {
                  response.stream()
                      .flatMap(event -> event.outputTextDelta().stream())
                      .forEach(delta -> listener.accept(AiStreamEvent.delta(delta.delta())));
                }
                listener.accept(AiStreamEvent.completed());
                completion.complete(null);
              } catch (RateLimitException exception) {
                listener.accept(
                    AiStreamEvent.error(
                        "Das API-Limit wurde erreicht. Bitte später erneut versuchen."));
                completion.completeExceptionally(exception);
              } catch (RuntimeException exception) {
                listener.accept(
                    AiStreamEvent.error(
                        "Die Online-Antwort ist fehlgeschlagen. Lokale Funktionen bleiben verfügbar."));
                completion.completeExceptionally(exception);
              } finally {
                active.set(null);
              }
            });
    active.set(future);
    return completion;
  }

  @Override
  public void cancel() {
    Future<?> future = active.getAndSet(null);
    if (future != null) {
      future.cancel(true);
    }
  }

  @Override
  public void close() {
    cancel();
    executor.shutdownNow();
  }

  private static String buildInput(AiRequest request) {
    StringBuilder input = new StringBuilder();
    request
        .history()
        .forEach(
            message ->
                input
                    .append(message.role().name())
                    .append(": ")
                    .append(message.text())
                    .append('\n'));
    if (!request.approvedContext().isBlank()) {
      input
          .append("FREIGEGEBENER KONTEXT (UNTRUSTED DATA):\n")
          .append(request.approvedContext())
          .append('\n');
    }
    return input.append("USER: ").append(request.question()).toString();
  }
}
