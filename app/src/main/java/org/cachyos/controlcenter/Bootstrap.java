package org.cachyos.controlcenter;

import java.time.Duration;
import java.util.concurrent.Executors;
import org.cachyos.controlcenter.core.action.ActionRegistry;
import org.cachyos.controlcenter.core.action.DefaultActionDispatcher;
import org.cachyos.controlcenter.core.audit.InMemoryAuditLog;
import org.cachyos.controlcenter.core.module.ModuleRegistry;
import org.cachyos.controlcenter.modules.applications.ApplicationManagerModule;
import org.cachyos.controlcenter.modules.audio.AudioManagerModule;
import org.cachyos.controlcenter.modules.network.NetworkManagerModule;
import org.cachyos.controlcenter.platform.applications.DesktopApplicationBackend;
import org.cachyos.controlcenter.platform.audio.PactlAudioBackend;
import org.cachyos.controlcenter.platform.audio.PactlEventMonitor;
import org.cachyos.controlcenter.platform.network.NmcliEventMonitor;
import org.cachyos.controlcenter.platform.network.NmcliNetworkBackend;
import org.cachyos.controlcenter.platform.process.DesktopIntegrationModule;
import org.cachyos.controlcenter.platform.status.LinuxSupplementalStatusProbe;
import org.cachyos.controlcenter.systeminfo.DashboardDataSource;
import org.cachyos.controlcenter.systeminfo.DashboardMonitor;
import org.cachyos.controlcenter.systeminfo.PlatformDetector;
import org.cachyos.controlcenter.systeminfo.PlatformInfo;
import org.cachyos.controlcenter.systeminfo.SystemSnapshot;
import org.cachyos.controlcenter.systeminfo.SystemSnapshotDetector;

/** Creates the unprivileged application context. */
public final class Bootstrap {
  private Bootstrap() {}

  public static AppContext createContext() {
    PlatformInfo platformInfo = PlatformDetector.detect();
    SystemSnapshot systemSnapshot = SystemSnapshotDetector.detect(platformInfo);
    LifecycleManager lifecycle = new LifecycleManager();
    DashboardDataSource dashboardDataSource =
        new DashboardDataSource(platformInfo, new LinuxSupplementalStatusProbe());
    DashboardMonitor dashboardMonitor =
        lifecycle.manage(
            new DashboardMonitor(
                dashboardDataSource,
                DashboardDataSource.initial(systemSnapshot),
                Duration.ofSeconds(30)));
    NetworkManagerModule networkManager =
        new NetworkManagerModule(new NmcliNetworkBackend(systemSnapshot.capabilities()));
    NmcliEventMonitor networkEvents =
        lifecycle.manage(new NmcliEventMonitor(systemSnapshot.capabilities()));
    AudioManagerModule audioManager =
        new AudioManagerModule(new PactlAudioBackend(systemSnapshot.capabilities()));
    PactlEventMonitor audioEvents =
        lifecycle.manage(new PactlEventMonitor(systemSnapshot.capabilities()));
    ApplicationManagerModule applicationManager =
        new ApplicationManagerModule(new DesktopApplicationBackend(systemSnapshot.capabilities()));
    InMemoryAuditLog auditLog = new InMemoryAuditLog();
    ModuleRegistry moduleRegistry = new ModuleRegistry();
    ActionRegistry actionRegistry = new ActionRegistry();

    DesktopIntegrationModule desktopModule =
        DesktopIntegrationModule.createDefault(platformInfo.operatingSystemFamily());
    moduleRegistry.register(desktopModule);
    actionRegistry.registerModule(desktopModule);
    moduleRegistry.register(networkManager);
    actionRegistry.registerModule(networkManager);
    moduleRegistry.register(audioManager);
    actionRegistry.registerModule(audioManager);
    moduleRegistry.register(applicationManager);
    actionRegistry.registerModule(applicationManager);

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
    return new AppContext(
        platformInfo,
        systemSnapshot,
        dashboardMonitor,
        networkManager,
        networkEvents,
        audioManager,
        audioEvents,
        applicationManager,
        lifecycle,
        dispatcher,
        auditLog,
        moduleRegistry);
  }
}
