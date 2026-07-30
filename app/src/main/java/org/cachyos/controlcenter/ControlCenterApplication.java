package org.cachyos.controlcenter;

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
        new MainView(context.platformInfo(), context.systemSnapshot(), context.actionDispatcher());
    Scene scene = new Scene(mainView.root(), 1100, 720);
    mainView.install(scene);
    stage.setTitle("CachyOS Control Center AI");
    stage.setMinWidth(720);
    stage.setMinHeight(480);
    stage.setScene(scene);
    stage.show();
    context.lifecycleManager().applicationStarted();
  }

  @Override
  public void stop() {
    if (context != null) {
      context.lifecycleManager().close();
    }
  }
}
