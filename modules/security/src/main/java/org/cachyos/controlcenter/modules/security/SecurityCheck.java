package org.cachyos.controlcenter.modules.security;

import java.util.Objects;

public record SecurityCheck(
    String id, String title, SecurityStatus status, String evidence, String recommendation) {
  public SecurityCheck {
    id = Objects.requireNonNull(id, "id");
    title = Objects.requireNonNullElse(title, "");
    Objects.requireNonNull(status, "status");
    evidence = Objects.requireNonNullElse(evidence, "");
    recommendation = Objects.requireNonNullElse(recommendation, "");
  }
}
