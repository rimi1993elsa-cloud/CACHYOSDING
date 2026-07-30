package org.cachyos.controlcenter.modules.diagnostics;

import java.util.Objects;
import java.util.Optional;
import org.cachyos.controlcenter.core.action.ActionId;

/** Sanitized user-visible finding with at most one fixed local suggestion. */
public record DiagnosticFinding(
    DiagnosticCategory category,
    DiagnosticStatus status,
    String summary,
    String details,
    Optional<ActionId> suggestedAction) {
  public DiagnosticFinding {
    Objects.requireNonNull(category, "category");
    Objects.requireNonNull(status, "status");
    summary = Objects.requireNonNullElse(summary, "").strip();
    details = Objects.requireNonNullElse(details, "").strip();
    suggestedAction = Objects.requireNonNull(suggestedAction, "suggestedAction");
  }
}
