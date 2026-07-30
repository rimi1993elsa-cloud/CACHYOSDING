package org.cachyos.controlcenter.ai.provider;

import java.util.Optional;

/** Read-only secret boundary. Secret values must never be logged. */
@FunctionalInterface
public interface SecretStore {
  Optional<char[]> readSecret(String key);
}
