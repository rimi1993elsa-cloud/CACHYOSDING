package org.cachyos.controlcenter.core.action;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/** Immutable, typed request crossing the local action boundary. */
public record ActionRequest(
    ActionId actionId, InputSource source, Map<String, String> parameters, Instant requestedAt) {
  public ActionRequest {
    Objects.requireNonNull(actionId, "actionId");
    Objects.requireNonNull(source, "source");
    parameters = Map.copyOf(Objects.requireNonNull(parameters, "parameters"));
    Objects.requireNonNull(requestedAt, "requestedAt");
  }

  public static ActionRequest fromButton(ActionId actionId) {
    return new ActionRequest(actionId, InputSource.BUTTON, Map.of(), Instant.now());
  }
}
