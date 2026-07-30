package org.cachyos.controlcenter.modules.diagnostics;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** Immutable local report safe for display and optional explicit sharing. */
public record DiagnosticReport(Instant createdAt, List<DiagnosticFinding> findings) {
  public DiagnosticReport {
    Objects.requireNonNull(createdAt, "createdAt");
    findings = List.copyOf(Objects.requireNonNull(findings, "findings"));
  }

  public String asSanitizedText() {
    StringBuilder text = new StringBuilder("Lokaler Diagnosebericht vom ").append(createdAt);
    for (DiagnosticFinding finding : findings) {
      text.append("\n\n")
          .append(finding.category().displayName())
          .append(" [")
          .append(finding.status())
          .append("]\n")
          .append(finding.summary());
      if (!finding.details().isBlank()) {
        text.append('\n').append(finding.details());
      }
    }
    return text.toString();
  }
}
