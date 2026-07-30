package org.cachyos.controlcenter.input.intent;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.cachyos.controlcenter.core.action.ActionId;

/** Data-only routing result. It cannot execute itself. */
public record IntentResult(
    IntentKind kind,
    Optional<ActionId> actionId,
    Map<String, String> parameters,
    Optional<String> navigationTarget,
    double confidence,
    boolean confirmationRequired,
    String message) {
  public IntentResult {
    Objects.requireNonNull(kind, "kind");
    actionId = Objects.requireNonNull(actionId, "actionId");
    parameters = Map.copyOf(Objects.requireNonNull(parameters, "parameters"));
    navigationTarget = Objects.requireNonNull(navigationTarget, "navigationTarget");
    if (confidence < 0 || confidence > 1) {
      throw new IllegalArgumentException("Confidence must be between zero and one");
    }
    message = Objects.requireNonNullElse(message, "");
    if (kind != IntentKind.ACTION && (actionId.isPresent() || !parameters.isEmpty())) {
      throw new IllegalArgumentException("Only action results may carry an action");
    }
    if (kind != IntentKind.NAVIGATION && navigationTarget.isPresent()) {
      throw new IllegalArgumentException("Only navigation results may carry a target");
    }
  }

  public static IntentResult action(
      ActionId id,
      Map<String, String> parameters,
      double confidence,
      boolean confirmationRequired,
      String message) {
    return new IntentResult(
        IntentKind.ACTION,
        Optional.of(id),
        parameters,
        Optional.empty(),
        confidence,
        confirmationRequired,
        message);
  }

  public static IntentResult navigation(String target, double confidence) {
    return new IntentResult(
        IntentKind.NAVIGATION,
        Optional.empty(),
        Map.of(),
        Optional.of(target),
        confidence,
        false,
        "Bereich öffnen");
  }

  public static IntentResult passive(IntentKind kind, double confidence, String message) {
    if (kind == IntentKind.ACTION || kind == IntentKind.NAVIGATION) {
      throw new IllegalArgumentException("Use a specific result factory");
    }
    return new IntentResult(
        kind, Optional.empty(), Map.of(), Optional.empty(), confidence, false, message);
  }
}
