package org.cachyos.controlcenter;

import java.time.Duration;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.cachyos.controlcenter.ui.MainView;

/** JavaFX lifecycle adapter. */
public final class ControlCenterApplication extends Application {
  private AppContext context;

  @Override
  public void init() {
    context = Bootstrap.createContext();
  }

  @Override
  public void start(Stage stage) {
    MainView mainView =
        new MainView(
            context.platformInfo(),
            context.systemSnapshot(),
            context.dashboardMonitor(),
            context.auditLog(),
            context.networkManager(),
            context.networkEvents(),
            context.audioManager(),
            context.audioEvents(),
            context.applicationManager(),
            context.diagnosticManager(),
            context.packageManager(),
            context.securityManager(),
            context.hardwareManager(),
            context.storageManager(),
            context.snapshotManager(),
            context.serviceManager(),
            context.processManager(),
            context.displayManager(),
            context.powerManager(),
            context.bootManager(),
            context.intentRouter(),
            context.microphoneCatalog(),
            context.speechModelManager(),
            context.speechToTextEngine(),
            context.aiProvider(),
            context.aiConfiguration(),
            context.knowledgeService(),
            context.settingsService(),
            context.actionDispatcher());
    Scene scene = new Scene(mainView.root(), 1100, 720);
    mainView.install(scene);
    stage.setTitle("CachyOS Control Center AI");
    stage.setMinWidth(720);
    stage.setMinHeight(480);
    stage.setScene(scene);
    stage.show();
    stage
        .focusedProperty()
        .addListener(
            (ignored, previous, focused) -> {
              if (focused) {
                context.dashboardMonitor().refreshIfStale(Duration.ofMinutes(2));
              }
            });
    context.lifecycleManager().applicationStarted();
    SetupWizard.showIfRequired(stage, context.settingsService());
  }

  @Override
  public void stop() {
    if (context != null) {
      context.lifecycleManager().close();
    }
  }
}
