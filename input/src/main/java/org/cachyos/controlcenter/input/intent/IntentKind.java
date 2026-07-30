package org.cachyos.controlcenter.input.intent;

/** Mutually exclusive local interpretation outcomes. */
public enum IntentKind {
  ACTION,
  NAVIGATION,
  QUESTION,
  AMBIGUOUS,
  UNKNOWN
}
