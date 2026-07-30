package org.cachyos.controlcenter.platform.diagnostics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import org.cachyos.controlcenter.modules.diagnostics.DiagnosticCategory;
import org.cachyos.controlcenter.modules.diagnostics.DiagnosticStatus;
import org.cachyos.controlcenter.systeminfo.CapabilityRegistry;
import org.cachyos.controlcenter.systeminfo.OperatingSystemFamily;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LinuxDiagnosticBackendTest {
  @TempDir Path temporary;

  @Test
  void readsBootParametersWithoutAProcess() throws Exception {
    Path commandLine = temporary.resolve("cmdline");
    Files.writeString(commandLine, "quiet splash");
    CapabilityRegistry capabilities =
        CapabilityRegistry.detect(Map.of("PATH", ""), OperatingSystemFamily.LINUX);
    LinuxDiagnosticBackend backend =
        new LinuxDiagnosticBackend(
            capabilities,
            commandLine,
            (executable, arguments) -> {
              throw new AssertionError("Boot probe must not start a process");
            });

    var observation = backend.inspect(DiagnosticCategory.BOOT);

    assertEquals(DiagnosticStatus.OK, observation.status());
    assertTrue(observation.technicalText().contains("quiet"));
  }

  @Test
  void missingOptionalToolIsUnavailable() {
    CapabilityRegistry capabilities =
        CapabilityRegistry.detect(Map.of("PATH", ""), OperatingSystemFamily.LINUX);
    LinuxDiagnosticBackend backend =
        new LinuxDiagnosticBackend(
            capabilities,
            temporary.resolve("missing"),
            (executable, arguments) -> Optional.empty());

    assertEquals(
        DiagnosticStatus.UNAVAILABLE, backend.inspect(DiagnosticCategory.NETWORK).status());
  }
}
