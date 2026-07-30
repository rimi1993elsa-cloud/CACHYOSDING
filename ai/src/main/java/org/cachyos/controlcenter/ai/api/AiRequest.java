package org.cachyos.controlcenter.ai.api;

import java.util.List;
import java.util.Objects;

/** Sanitized text request accepted by an AI provider. */
public record AiRequest(String question, List<AiMessage> history, String approvedContext) {
  public AiRequest {
    question = requireText(question, "question", 8_000);
    history = List.copyOf(Objects.requireNonNull(history, "history"));
    approvedContext = Objects.requireNonNullElse(approvedContext, "");
    if (approvedContext.length() > 32_000) {
      throw new IllegalArgumentException("Approved context is too long");
    }
  }

  public static AiRequest question(String question) {
    return new AiRequest(question, List.of(), "");
  }

  private static String requireText(String value, String name, int maximumLength) {
    if (value == null || value.isBlank() || value.length() > maximumLength) {
      throw new IllegalArgumentException(name + " must be present and bounded");
    }
    return value.strip();
  }
}
