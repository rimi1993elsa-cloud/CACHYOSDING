package org.cachyos.controlcenter.ai.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;

class AiConfigurationTest {
  @Test
  void readsValidatedEnvironmentOverrides() {
    AiConfiguration configuration =
        AiConfiguration.from(
            Map.of(
                "CACHYOS_CC_OPENAI_MODEL",
                "gpt-5.6-terra",
                "CACHYOS_CC_OPENAI_MAX_OUTPUT_TOKENS",
                "1024"));

    assertEquals("gpt-5.6-terra", configuration.model());
    assertEquals(1024, configuration.maximumOutputTokens());
  }

  @Test
  void rejectsShellSyntaxInModelName() {
    assertThrows(
        IllegalArgumentException.class,
        () -> AiConfiguration.from(Map.of("CACHYOS_CC_OPENAI_MODEL", "model;rm")));
  }
}
