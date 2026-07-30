package org.cachyos.controlcenter.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import org.cachyos.controlcenter.core.action.ActionDispatcher;
import org.cachyos.controlcenter.core.audit.InMemoryAuditLog;
import org.cachyos.controlcenter.input.intent.GermanIntentRouter;
import org.cachyos.controlcenter.input.voice.MicrophoneCatalog;
import org.cachyos.controlcenter.input.voice.SpeechModelManager;
import org.cachyos.controlcenter.input.voice.SpeechToTextEngine;
import org.cachyos.controlcenter.modules.applications.ApplicationManagerModule;
import org.cachyos.controlcenter.modules.audio.AudioEvents;
import org.cachyos.controlcenter.modules.audio.AudioManagerModule;
import org.cachyos.controlcenter.modules.network.NetworkEvents;
import org.cachyos.controlcenter.modules.network.NetworkManagerModule;
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
      InMemoryAuditLog auditLog,
      NetworkManagerModule networkManager,
      NetworkEvents networkEvents,
      AudioManagerModule audioManager,
      AudioEvents audioEvents,
      ApplicationManagerModule applicationManager,
      GermanIntentRouter intentRouter,
      MicrophoneCatalog microphoneCatalog,
      SpeechModelManager speechModelManager,
      SpeechToTextEngine speechToTextEngine,
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
            intentRouter,
            microphoneCatalog,
            speechModelManager,
            speechToTextEngine,
            new NavigationCatalog(),
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
