package org.cachyos.controlcenter.platform.process;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

/** Starts a structured command directly, never through a shell. */
public final class JvmProcessStarter implements ProcessStarter {
  @Override
  public ProcessLaunchResult start(CommandSpec command) throws IOException {
    Process process =
        new ProcessBuilder(command.commandLine())
            .redirectInput(Redirect.PIPE)
            .redirectOutput(Redirect.DISCARD)
            .redirectError(Redirect.DISCARD)
            .start();
    process.getOutputStream().close();
    return new ProcessLaunchResult(process.pid());
  }
}
