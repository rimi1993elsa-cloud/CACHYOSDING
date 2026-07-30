package org.cachyos.controlcenter.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.cachyos.controlcenter.systeminfo.OperatingSystemFamily;
import org.cachyos.controlcenter.systeminfo.PlatformInfo;
import org.cachyos.controlcenter.ui.navigation.NavigationEntry;
import org.cachyos.controlcenter.ui.navigation.NavigationId;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

class ApplicationShellTest extends ApplicationTest {
  @Override
  public void start(Stage stage) {
    PlatformInfo platformInfo =
        new PlatformInfo(
            OperatingSystemFamily.LINUX, "CachyOS", "rolling", "x86_64", "KDE", "wayland");
    MainView view = new MainView(platformInfo);
    Scene scene = new Scene(view.root(), 1100, 720);
    view.install(scene);
    stage.setScene(scene);
    stage.show();
  }

  @Test
  void navigationChangesRegisteredContent() {
    clickOn("System");

    Label systemHeading = lookup(".page-title").queryAs(Label.class);
    assertEquals("System", systemHeading.getText());

    ListView<NavigationEntry> navigation = lookup("#primary-navigation").queryListView();
    interact(
        () ->
            navigation
                .getSelectionModel()
                .select(
                    navigation.getItems().stream()
                        .filter(entry -> entry.id() == NavigationId.SETTINGS)
                        .findFirst()
                        .orElseThrow()));

    Label settingsHeading = lookup(".page-title").queryAs(Label.class);
    assertEquals("Einstellungen", settingsHeading.getText());
  }
}
