package org.cachyos.controlcenter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class BootstrapTest {
  @Test
  void createsUnprivilegedPhaseZeroContext() {
    AppContext context = Bootstrap.createContext();

    assertNotNull(context.platformInfo());
    assertNotNull(context.systemSnapshot());
    assertNotNull(context.dashboardMonitor());
    assertNotNull(context.networkManager());
    assertNotNull(context.networkEvents());
    assertNotNull(context.audioManager());
    assertNotNull(context.audioEvents());
    assertNotNull(context.applicationManager());
    assertNotNull(context.lifecycleManager());
    assertNotNull(context.actionDispatcher());
    assertNotNull(context.auditLog());
    assertNotNull(context.moduleRegistry());
    context.lifecycleManager().close();
  }
}
