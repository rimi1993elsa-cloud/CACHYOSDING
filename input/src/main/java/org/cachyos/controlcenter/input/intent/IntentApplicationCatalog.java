package org.cachyos.controlcenter.input.intent;

import java.util.List;

/** Read-only projection of applications that already passed the desktop-entry safety checks. */
@FunctionalInterface
public interface IntentApplicationCatalog {
  List<RegisteredApplication> applications();
}
