package org.cachyos.controlcenter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    assertNotNull(context.serviceManager());
    assertNotNull(context.processManager());
    assertNotNull(context.intentRouter());
    assertNotNull(context.microphoneCatalog());
    assertNotNull(context.speechModelManager());
    assertNotNull(context.speechToTextEngine());
    assertNotNull(context.aiProvider());
    assertNotNull(context.aiConfiguration());
    assertNotNull(context.secretStore());
    assertNotNull(context.knowledgeService());
    assertNotNull(context.settingsService());
    assertNotNull(context.lifecycleManager());
    assertNotNull(context.actionDispatcher());
    assertNotNull(context.auditLog());
    assertNotNull(context.moduleRegistry());
    assertTrue(context.lifecycleManager().bootstrapMillis() >= 0);
    context.lifecycleManager().close();
  }
}
