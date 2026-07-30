package org.cachyos.controlcenter.helper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

final class PolkitAuthorizer implements HelperAuthorizer {
  private static final long TIMEOUT_SECONDS = 90;

  @Override
  public boolean authorize(String sender, HelperAction action) {
    if (!HelperValidation.sender(sender)) {
      return false;
    }
    List<String> command =
        List.of(
            "/usr/bin/pkcheck",
            "--action-id",
            action.polkitId(),
            "--system-bus-name",
            sender,
            "--allow-user-interaction");
    try {
      Process process =
          new ProcessBuilder(command)
              .redirectInput(ProcessBuilder.Redirect.from(new java.io.File("/dev/null")))
              .redirectOutput(ProcessBuilder.Redirect.DISCARD)
              .redirectError(ProcessBuilder.Redirect.DISCARD)
              .start();
      if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
        process.destroyForcibly();
        return false;
      }
      return process.exitValue() == 0;
    } catch (IOException exception) {
      return false;
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      return false;
    }
  }
}
