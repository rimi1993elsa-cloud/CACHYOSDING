package org.cachyos.controlcenter.modules.diagnostics;

/** Read-only platform diagnostic port. */
@FunctionalInterface
public interface DiagnosticBackend {
  DiagnosticObservation inspect(DiagnosticCategory category);
}
