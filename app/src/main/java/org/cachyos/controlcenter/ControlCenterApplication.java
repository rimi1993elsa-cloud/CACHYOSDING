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
    Scene scene = new Scene(new MainView(context.platformInfo()).root(), 960, 600);
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
