package org.cachyos.controlcenter.platform.packages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.cachyos.controlcenter.modules.packages.PackageAction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PacmanPackageBackendTest {
  @TempDir Path temporary;

  @Test
  void parsesStableEnglishPacmanOutput() {
    var entries =
        PacmanPackageBackend.parseSearch(
            List.of(
                "core/bash 5.2.037-1 [installed]",
                "    The GNU Bourne Again shell",
                "extra/nano 8.5-1",
                "    Pico editor clone"));
    assertEquals(2, entries.size());
    assertTrue(entries.getFirst().installed());
    assertEquals("nano", entries.getLast().name());
  }

  @Test
  void passesValidatedPackageAsSeparateArgument() {
    RecordingRunner runner = new RecordingRunner();
    PacmanPackageBackend backend =
        new PacmanPackageBackend(true, runner, temporary.resolve("lock"), temporary);
    backend.preview(PackageAction.INSTALL, "linux-cachyos");
    assertEquals(
        List.of("-Sp", "--print-format", "%n\t%v\t%s", "--", "linux-cachyos"), runner.arguments);
    assertFalse(runner.arguments.contains("sh"));
  }

  @Test
  void rejectsManipulationWithoutStartingProcess() {
    RecordingRunner runner = new RecordingRunner();
    PacmanPackageBackend backend =
        new PacmanPackageBackend(true, runner, temporary.resolve("lock"), temporary);
    assertThrows(
        IllegalArgumentException.class, () -> backend.preview(PackageAction.INSTALL, "nano;touch"));
    assertTrue(runner.arguments.isEmpty());
  }

  private static final class RecordingRunner extends PacmanCommandRunner {
    private List<String> arguments = new ArrayList<>();

    @Override
    CommandOutput run(Path executable, List<String> arguments, Duration timeout) {
      this.arguments = List.copyOf(arguments);
      return new CommandOutput(0, List.of("linux-cachyos\t6.1\t1"));
    }
  }
}
