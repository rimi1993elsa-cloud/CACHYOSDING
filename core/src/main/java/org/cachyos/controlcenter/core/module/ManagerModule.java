package org.cachyos.controlcenter.core.module;

import java.util.Set;
import org.cachyos.controlcenter.core.action.ActionId;
import org.cachyos.controlcenter.core.action.ActionRequest;
import org.cachyos.controlcenter.core.action.ActionResult;

/** Common contract for all manager modules. */
public interface ManagerModule {
  ModuleId id();

  String displayName();

  ModuleCapabilities capabilities();

  ModuleSnapshot loadSnapshot();

  Set<ActionId> actions();

  default boolean supports(ActionId actionId) {
    return actions().contains(actionId);
  }

  ActionResult execute(ActionRequest request);
}
