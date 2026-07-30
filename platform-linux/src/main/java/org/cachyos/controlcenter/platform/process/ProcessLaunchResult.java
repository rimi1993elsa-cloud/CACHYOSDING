package org.cachyos.controlcenter.platform.process;

/** Minimal launch metadata; command arguments are deliberately not retained. */
public record ProcessLaunchResult(long processId) {
  public ProcessLaunchResult {
    if (processId < 0) {
      throw new IllegalArgumentException("processId must not be negative");
    }
  }
}
