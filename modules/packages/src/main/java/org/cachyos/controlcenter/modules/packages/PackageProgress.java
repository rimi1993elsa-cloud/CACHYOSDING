package org.cachyos.controlcenter.modules.packages;

public record PackageProgress(State state, String message) {
  public enum State {
    IDLE,
    READING,
    PREVIEWING,
    AUTHORIZING,
    RUNNING,
    COMPLETED,
    FAILED
  }
}
