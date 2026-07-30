package org.cachyos.controlcenter.core.action;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/** Sanitized action outcome. Technical details must not contain secrets. */
public record ActionResult(
    ActionStatus status,
    String userMessage,
    String technicalMessage,
    Map<String, Object> data,
    Instant completedAt) {
  public ActionResult {
    Objects.requireNonNull(status, "status");
    userMessage = requireText(userMessage, "userMessage");
    technicalMessage = technicalMessage == null ? "" : technicalMessage;
    data = Map.copyOf(Objects.requireNonNull(data, "data"));
    Objects.requireNonNull(completedAt, "completedAt");
  }

  public static ActionResult success(String message) {
    return new ActionResult(ActionStatus.SUCCESS, message, "", Map.of(), Instant.now());
  }

  public static ActionResult rejected(String message, String technicalMessage) {
    return new ActionResult(
        ActionStatus.REJECTED, message, technicalMessage, Map.of(), Instant.now());
  }

  public static ActionResult failed(String message, String technicalMessage) {
    return new ActionResult(
        ActionStatus.FAILED, message, technicalMessage, Map.of(), Instant.now());
  }

  public static ActionResult unavailable(String message) {
    return new ActionResult(
        ActionStatus.UNAVAILABLE, message, "Capability unavailable", Map.of(), Instant.now());
  }

  private static String requireText(String value, String name) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(name + " must not be blank");
    }
    return value;
  }
}
