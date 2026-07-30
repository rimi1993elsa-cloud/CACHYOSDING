package org.cachyos.controlcenter.ai.provider;

import java.util.Optional;

/** Desktop secret boundary. Secret values must never be logged. */
@FunctionalInterface
public interface SecretStore {
  Optional<char[]> readSecret(String key);

  default SecretOperationResult storeSecret(String key, char[] value) {
    return SecretOperationResult.failure("Der Secret Store ist schreibgeschützt.");
  }

  default SecretOperationResult deleteSecret(String key) {
    return SecretOperationResult.failure("Der Secret Store ist schreibgeschützt.");
  }

  default boolean containsSecret(String key) {
    Optional<char[]> value = readSecret(key);
    value.ifPresent(secret -> java.util.Arrays.fill(secret, '\0'));
    return value.isPresent();
  }
}
