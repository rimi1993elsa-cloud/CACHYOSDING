package org.cachyos.controlcenter.platform.applications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DesktopExecParserTest {
  @Test
  void parsesQuotedArgumentsAndRemovesFieldCodes() {
    var command =
        DesktopExecParser.parse(
                "browser --profile \"Work Profile\" %U",
                executable ->
                    Optional.of(
                        Path.of(System.getProperty("user.home"), "browser.exe").toAbsolutePath()))
            .orElseThrow();

    assertEquals(List.of("--profile", "Work Profile"), command.arguments());
  }

  @Test
  void rejectsShellInterpretersEvenForDesktopFiles() {
    assertTrue(
        DesktopExecParser.parse(
                "sh -c \"touch /tmp/unsafe\"", executable -> Optional.of(Path.of("/usr/bin/sh")))
            .isEmpty());
  }
}
