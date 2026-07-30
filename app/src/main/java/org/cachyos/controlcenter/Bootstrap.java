package org.cachyos.controlcenter;

import java.time.Duration;
import java.util.concurrent.Executors;
import org.cachyos.controlcenter.ai.api.AiProvider;
import org.cachyos.controlcenter.ai.knowledge.HttpKnowledgeFetcher;
import org.cachyos.controlcenter.ai.knowledge.KnowledgeCache;
import org.cachyos.controlcenter.ai.knowledge.KnowledgeService;
import org.cachyos.controlcenter.ai.knowledge.OfficialSourceRegistry;
import org.cachyos.controlcenter.ai.openai.OpenAiResponsesProvider;
import org.cachyos.controlcenter.ai.provider.AiConfiguration;
import org.cachyos.controlcenter.core.action.ActionRegistry;
import org.cachyos.controlcenter.core.action.DefaultActionDispatcher;
import org.cachyos.controlcenter.core.audit.InMemoryAuditLog;
import org.cachyos.controlcenter.core.module.ModuleRegistry;
import org.cachyos.controlcenter.input.intent.GermanIntentRouter;
import org.cachyos.controlcenter.input.intent.RegisteredApplication;
import org.cachyos.controlcenter.input.voice.MicrophoneCatalog;
import org.cachyos.controlcenter.input.voice.SpeechModelManager;
import org.cachyos.controlcenter.input.voice.VoskSpeechToTextEngine;
import org.cachyos.controlcenter.modules.applications.ApplicationManagerModule;
import org.cachyos.controlcenter.modules.audio.AudioManagerModule;
import org.cachyos.controlcenter.modules.diagnostics.DiagnosticManager;
import org.cachyos.controlcenter.modules.network.NetworkManagerModule;
import org.cachyos.controlcenter.platform.applications.DesktopApplicationBackend;
import org.cachyos.controlcenter.platform.audio.PactlAudioBackend;
import org.cachyos.controlcenter.platform.audio.PactlEventMonitor;
import org.cachyos.controlcenter.platform.diagnostics.LinuxDiagnosticBackend;
import org.cachyos.controlcenter.platform.network.NmcliEventMonitor;
import org.cachyos.controlcenter.platform.network.NmcliNetworkBackend;
import org.cachyos.controlcenter.platform.process.DesktopIntegrationModule;
import org.cachyos.controlcenter.platform.secrets.DesktopSecretStore;
import org.cachyos.controlcenter.platform.status.LinuxSupplementalStatusProbe;
import org.cachyos.controlcenter.systeminfo.DashboardDataSource;
import org.cachyos.controlcenter.systeminfo.DashboardMonitor;
import org.cachyos.controlcenter.systeminfo.PlatformDetector;
import org.cachyos.controlcenter.systeminfo.PlatformInfo;
import org.cachyos.controlcenter.systeminfo.SystemSnapshot;
import org.cachyos.controlcenter.systeminfo.SystemSnapshotDetector;
import org.cachyos.controlcenter.xdg.XdgPaths;

/** Creates the unprivileged application context. */
public final class Bootstrap {
  private Bootstrap() {}

  public static AppContext createContext() {
    XdgPaths xdgPaths = XdgPaths.detect();
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
    DiagnosticManager diagnosticManager =
        lifecycle.manage(
            new DiagnosticManager(new LinuxDiagnosticBackend(systemSnapshot.capabilities())));
    GermanIntentRouter intentRouter =
        new GermanIntentRouter(
            () ->
                applicationManager.applications().stream()
                    .map(entry -> new RegisteredApplication(entry.id(), entry.name()))
                    .toList());
    MicrophoneCatalog microphoneCatalog = new MicrophoneCatalog();
    SpeechModelManager speechModelManager = new SpeechModelManager(xdgPaths.dataDirectory());
    VoskSpeechToTextEngine speechToTextEngine =
        lifecycle.manage(new VoskSpeechToTextEngine(microphoneCatalog));
    AiConfiguration aiConfiguration = AiConfiguration.fromEnvironment();
    AiProvider aiProvider =
        lifecycle.manage(new OpenAiResponsesProvider(aiConfiguration, new DesktopSecretStore()));
    KnowledgeService knowledgeService =
        lifecycle.manage(
            new KnowledgeService(
                OfficialSourceRegistry.sources(),
                new KnowledgeCache(xdgPaths.cacheDirectory()),
                new HttpKnowledgeFetcher()));
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
        diagnosticManager,
        intentRouter,
        microphoneCatalog,
        speechModelManager,
        speechToTextEngine,
        aiProvider,
        aiConfiguration,
        knowledgeService,
        lifecycle,
        dispatcher,
        auditLog,
        moduleRegistry);
  }
}
