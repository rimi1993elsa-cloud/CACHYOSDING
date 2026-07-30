package org.cachyos.controlcenter.core.action;

import java.util.concurrent.CompletionStage;

/** Asynchronous local action boundary exposed to trusted UI and input components. */
@FunctionalInterface
public interface ActionDispatcher {
  CompletionStage<ActionResult> dispatch(ActionRequest request);
}
