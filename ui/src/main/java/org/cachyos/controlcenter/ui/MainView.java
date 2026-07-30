package org.cachyos.controlcenter.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import org.cachyos.controlcenter.core.action.ActionDispatcher;
import org.cachyos.controlcenter.core.audit.InMemoryAuditLog;
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
      ActionDispatcher actionDispatcher) {
    NotificationCenter notifications = new NotificationCenter();
    themeManager = new ThemeManager();
    shell =
        new ApplicationShell(
            platformInfo,
            systemSnapshot,
            dashboardMonitor,
            auditLog,
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
