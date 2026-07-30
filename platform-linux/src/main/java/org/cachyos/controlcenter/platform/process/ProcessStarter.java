package org.cachyos.controlcenter.platform.process;

import java.io.IOException;

/** Unprivileged process start boundary used only by allowlisted platform modules. */
@FunctionalInterface
public interface ProcessStarter {
  ProcessLaunchResult start(CommandSpec command) throws IOException;
}
