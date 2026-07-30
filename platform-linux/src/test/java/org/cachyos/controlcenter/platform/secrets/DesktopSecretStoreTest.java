package org.cachyos.controlcenter.platform.secrets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DesktopSecretStoreTest {
  @Test
  void usesFallbackWhenSecretToolIsUnavailable() {
    DesktopSecretStore store =
        new DesktopSecretStore(
            Path.of("definitely-missing-secret-tool"),
            Duration.ofMillis(10),
            key -> Optional.of("development-secret".toCharArray()));

    assertEquals(
        "development-secret", new String(store.readSecret("openai-api-key").orElseThrow()));
  }

  @Test
  void rejectsUnknownSecretNames() {
    DesktopSecretStore store =
        new DesktopSecretStore(
            Path.of("missing"), Duration.ofMillis(10), key -> Optional.of(new char[] {'x'}));

    assertTrue(store.readSecret("other-key").isEmpty());
  }
}
