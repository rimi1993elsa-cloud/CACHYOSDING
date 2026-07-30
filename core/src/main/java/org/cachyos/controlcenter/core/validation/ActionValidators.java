package org.cachyos.controlcenter.core.validation;

import java.util.Map;
import org.cachyos.controlcenter.core.action.ActionRejectedException;

/** Shared validation helpers for action boundaries. */
public final class ActionValidators {
  private ActionValidators() {}

  public static void requireNoParameters(Map<String, String> parameters) {
    if (!parameters.isEmpty()) {
      throw new ActionRejectedException("Diese Aktion akzeptiert keine Parameter.");
    }
  }
}
