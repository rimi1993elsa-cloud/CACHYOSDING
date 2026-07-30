package org.cachyos.controlcenter.core.action;

/** Synchronous module handler; the dispatcher owns asynchronous execution. */
@FunctionalInterface
public interface ActionHandler {
  ActionResult execute(ActionRequest request);
}
