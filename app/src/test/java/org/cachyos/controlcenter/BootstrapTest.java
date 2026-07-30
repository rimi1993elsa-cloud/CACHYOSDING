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
    assertNotNull(context.diagnosticManager());
    assertNotNull(context.packageManager());
    assertNotNull(context.securityManager());
    assertNotNull(context.hardwareManager());
    assertNotNull(context.storageManager());
    assertNotNull(context.snapshotManager());
    assertNotNull(context.intentRouter());
    assertNotNull(context.microphoneCatalog());
    assertNotNull(context.speechModelManager());
    assertNotNull(context.speechToTextEngine());
    assertNotNull(context.aiProvider());
    assertNotNull(context.aiConfiguration());
    assertNotNull(context.knowledgeService());
    assertNotNull(context.lifecycleManager());
    assertNotNull(context.actionDispatcher());
    assertNotNull(context.auditLog());
    assertNotNull(context.moduleRegistry());
    context.lifecycleManager().close();
  }
}
