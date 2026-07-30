package org.cachyos.controlcenter.core.action;

/** Expected validation rejection mapped to a safe user result. */
public final class ActionRejectedException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public ActionRejectedException(String message) {
    super(message);
  }
}
