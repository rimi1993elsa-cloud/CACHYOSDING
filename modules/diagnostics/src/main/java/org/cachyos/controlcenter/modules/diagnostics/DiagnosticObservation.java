package org.cachyos.controlcenter.modules.diagnostics;

import java.util.Objects;

/** Raw adapter observation before central redaction. */
public record DiagnosticObservation(
    DiagnosticCategory category, DiagnosticStatus status, String summary, String technicalText) {
  public DiagnosticObservation {
    Objects.requireNonNull(category, "category");
    Objects.requireNonNull(status, "status");
    summary = Objects.requireNonNullElse(summary, "").strip();
    technicalText = Objects.requireNonNullElse(technicalText, "");
    if (summary.isBlank()) {
      throw new IllegalArgumentException("Diagnostic summary must not be blank");
    }
  }
}
