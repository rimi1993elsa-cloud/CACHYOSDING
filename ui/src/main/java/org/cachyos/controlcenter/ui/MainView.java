package org.cachyos.controlcenter.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import org.cachyos.controlcenter.ai.api.AiProvider;
import org.cachyos.controlcenter.ai.knowledge.KnowledgeService;
import org.cachyos.controlcenter.ai.provider.AiConfiguration;
import org.cachyos.controlcenter.ai.provider.SecretStore;
import org.cachyos.controlcenter.core.action.ActionDispatcher;
import org.cachyos.controlcenter.core.audit.AuditLog;
import org.cachyos.controlcenter.input.intent.GermanIntentRouter;
import org.cachyos.controlcenter.input.voice.MicrophoneCatalog;
import org.cachyos.controlcenter.input.voice.SpeechModelManager;
import org.cachyos.controlcenter.input.voice.SpeechToTextEngine;
import org.cachyos.controlcenter.modules.applications.ApplicationManagerModule;
import org.cachyos.controlcenter.modules.audio.AudioEvents;
import org.cachyos.controlcenter.modules.audio.AudioManagerModule;
import org.cachyos.controlcenter.modules.boot.BootManager;
import org.cachyos.controlcenter.modules.diagnostics.DiagnosticManager;
import org.cachyos.controlcenter.modules.display.DisplayManager;
import org.cachyos.controlcenter.modules.hardware.HardwareManager;
import org.cachyos.controlcenter.modules.network.NetworkEvents;
import org.cachyos.controlcenter.modules.network.NetworkManagerModule;
import org.cachyos.controlcenter.modules.packages.PackageManager;
import org.cachyos.controlcenter.modules.power.PowerManager;
import org.cachyos.controlcenter.modules.processes.ProcessManager;
import org.cachyos.controlcenter.modules.security.SecurityManager;
import org.cachyos.controlcenter.modules.services.ServiceManager;
import org.cachyos.controlcenter.modules.snapshots.SnapshotManager;
import org.cachyos.controlcenter.modules.storage.StorageManager;
import org.cachyos.controlcenter.persistence.SettingsService;
import org.cachyos.controlcenter.systeminfo.DashboardMonitor;
import org.cachyos.controlcenter.systeminfo.PlatformInfo;
import org.cachyos.controlcenter.systeminfo.SystemSnapshot;
import org.cachyos.controlcenter.ui.navigation.NavigationCatalog;
import org.cachyos.controlcenter.ui.notifications.NotificationCenter;
import org.cachyos.controlcenter.ui.theme.ThemeManager;

/** Public facade for the Phase 1 application shell. */
public final class MainView {
  private final ApplicationShell shell;
  private final ThemeManager themeManager;

  public MainView(
      PlatformInfo platformInfo,
      SystemSnapshot systemSnapshot,
      DashboardMonitor dashboardMonitor,
      AuditLog auditLog,
      NetworkManagerModule networkManager,
      NetworkEvents networkEvents,
      AudioManagerModule audioManager,
      AudioEvents audioEvents,
      ApplicationManagerModule applicationManager,
      DiagnosticManager diagnosticManager,
      PackageManager packageManager,
      SecurityManager securityManager,
      HardwareManager hardwareManager,
      StorageManager storageManager,
      SnapshotManager snapshotManager,
      ServiceManager serviceManager,
      ProcessManager processManager,
      DisplayManager displayManager,
      PowerManager powerManager,
      BootManager bootManager,
      GermanIntentRouter intentRouter,
      MicrophoneCatalog microphoneCatalog,
      SpeechModelManager speechModelManager,
      SpeechToTextEngine speechToTextEngine,
      AiProvider aiProvider,
      AiConfiguration aiConfiguration,
      SecretStore secretStore,
      KnowledgeService knowledgeService,
      SettingsService settingsService,
      ActionDispatcher actionDispatcher) {
    NotificationCenter notifications = new NotificationCenter();
    themeManager = new ThemeManager();
    shell =
        new ApplicationShell(
            platformInfo,
            systemSnapshot,
            dashboardMonitor,
            auditLog,
            networkManager,
            networkEvents,
            audioManager,
            audioEvents,
            applicationManager,
            diagnosticManager,
            packageManager,
            securityManager,
            hardwareManager,
            storageManager,
            snapshotManager,
            serviceManager,
            processManager,
            displayManager,
            powerManager,
            bootManager,
            intentRouter,
            microphoneCatalog,
            speechModelManager,
            speechToTextEngine,
            aiProvider,
            aiConfiguration,
            secretStore,
            knowledgeService,
            settingsService,
            new NavigationCatalog(settingsService.current().enabledModules()),
            themeManager,
            notifications,
            actionDispatcher);
  }

  public Parent root() {
    return shell.root();
  }

  public void install(Scene scene) {
    themeManager.install(scene);
    shell.installKeyboardNavigation(scene);
  }
}
