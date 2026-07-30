package org.cachyos.controlcenter.ai.provider;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/** Non-secret provider settings. */
public record AiConfiguration(String model, int maximumOutputTokens) {
  private static final Pattern MODEL = Pattern.compile("[a-zA-Z0-9][a-zA-Z0-9._-]{1,79}");

  public AiConfiguration {
    model = Objects.requireNonNull(model, "model").strip();
    if (!MODEL.matcher(model).matches()) {
      throw new IllegalArgumentException("Invalid model identifier");
    }
    if (maximumOutputTokens < 128 || maximumOutputTokens > 16_384) {
      throw new IllegalArgumentException("Invalid output token limit");
    }
  }

  public static AiConfiguration defaults() {
    return new AiConfiguration("gpt-5.6-sol", 2_048);
  }

  public static AiConfiguration fromEnvironment() {
    return from(System.getenv());
  }

  static AiConfiguration from(Map<String, String> environment) {
    String model = environment.getOrDefault("CACHYOS_CC_OPENAI_MODEL", "gpt-5.6-sol");
    String tokenText = environment.getOrDefault("CACHYOS_CC_OPENAI_MAX_OUTPUT_TOKENS", "2048");
    try {
      return new AiConfiguration(model, Integer.parseInt(tokenText));
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException("Invalid AI token configuration", exception);
    }
  }
}
