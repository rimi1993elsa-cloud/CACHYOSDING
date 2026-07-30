package org.cachyos.controlcenter.modules.diagnostics;

/** Supported local read-only diagnostic domains. */
public enum DiagnosticCategory {
  NETWORK("Netzwerk"),
  AUDIO("Audio"),
  SERVICES("Dienste"),
  BOOT("Boot"),
  GRAPHICS("Grafik"),
  PACKAGES("Pakete");

  private final String displayName;

  DiagnosticCategory(String displayName) {
    this.displayName = displayName;
  }

  public String displayName() {
    return displayName;
  }
}
