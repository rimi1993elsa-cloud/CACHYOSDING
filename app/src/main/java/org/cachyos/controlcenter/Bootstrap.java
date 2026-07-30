package org.cachyos.controlcenter;

import java.util.concurrent.Executors;
import org.cachyos.controlcenter.core.action.ActionRegistry;
import org.cachyos.controlcenter.core.action.DefaultActionDispatcher;
import org.cachyos.controlcenter.core.audit.InMemoryAuditLog;
import org.cachyos.controlcenter.core.module.ModuleRegistry;
import org.cachyos.controlcenter.platform.process.DesktopIntegrationModule;
import org.cachyos.controlcenter.systeminfo.PlatformDetector;
import org.cachyos.controlcenter.systeminfo.PlatformInfo;

/** Creates the unprivileged application context. */
public final class Bootstrap {
  private Bootstrap() {}

  public static AppContext createContext() {
    PlatformInfo platformInfo = PlatformDetector.detect();
    LifecycleManager lifecycle = new LifecycleManager();
    InMemoryAuditLog auditLog = new InMemoryAuditLog();
    ModuleRegistry moduleRegistry = new ModuleRegistry();
    ActionRegistry actionRegistry = new ActionRegistry();

    DesktopIntegrationModule desktopModule =
        DesktopIntegrationModule.createDefault(platformInfo.operatingSystemFamily());
    moduleRegistry.register(desktopModule);
    actionRegistry.registerModule(desktopModule);

    DefaultActionDispatcher dispatcher =
        lifecycle.manage(
            new DefaultActionDispatcher(
                actionRegistry,
                Executors.newFixedThreadPool(
                    2,
                    runnable -> {
                      Thread thread = new Thread(runnable, "local-action");
                      thread.setDaemon(true);
                      return thread;
                    }),
                auditLog));
    return new AppContext(platformInfo, lifecycle, dispatcher, auditLog, moduleRegistry);
  }
}
