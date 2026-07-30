package org.cachyos.controlcenter.ai.provider;

import java.util.Optional;

/** Non-persistent development fallback. Production uses the desktop Secret Service first. */
public final class EnvironmentSecretStore implements SecretStore {
  @Override
  public Optional<char[]> readSecret(String key) {
    if (!"openai-api-key".equals(key)) {
      return Optional.empty();
    }
    String value = System.getenv("OPENAI_API_KEY");
    return value == null || value.isBlank() ? Optional.empty() : Optional.of(value.toCharArray());
  }
}
