package org.cachyos.controlcenter.modules.security;

import java.time.Instant;
import java.util.List;

public record SecuritySnapshot(
    boolean available,
    boolean firewallEnabled,
    List<SecurityCheck> checks,
    List<ListeningPort> listeningPorts,
    Instant capturedAt,
    String message) {
  public SecuritySnapshot {
    checks = List.copyOf(checks);
    listeningPorts = List.copyOf(listeningPorts);
    message = message == null ? "" : message;
  }
}
