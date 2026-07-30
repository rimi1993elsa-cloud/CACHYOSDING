package org.cachyos.controlcenter.core.action;

/** Stable result status used by UI, audit, and later persistence. */
public enum ActionStatus {
  SUCCESS,
  FAILED,
  REJECTED,
  UNAVAILABLE
}
