package org.cachyos.controlcenter.platform.status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class CommandPerformanceRegistryTest {
  @Test
  void recordsOnlyExecutableNameAndBoundedTiming() {
    CommandPerformanceRegistry.clear();
    assertFalse(
        FixedCommandReader.read(
                Path.of("/definitely/not/a/tool"),
                List.of("--secret", "value"),
                Duration.ofMillis(1))
            .isPresent());
    var sample = CommandPerformanceRegistry.samples().getFirst();
    assertEquals("tool", sample.executable());
    assertFalse(sample.success());
  }
}
