package org.cachyos.controlcenter.ui.display;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.cachyos.controlcenter.modules.display.DisplayManager;
import org.cachyos.controlcenter.modules.display.DisplayState;
import org.cachyos.controlcenter.ui.notifications.NotificationCenter;

public final class DisplayView extends VBox {
  private final DisplayManager manager;
  private final NotificationCenter notifications;
  private final Label summary = new Label("Anzeige wird erkannt …");
  private final ListView<String> monitors = new ListView<>();
  private final Slider brightness = new Slider(1, 100, 50);
  private final CheckBox nightMode = new CheckBox("Nachtmodus");
  private boolean updating;

  public DisplayView(DisplayManager manager, NotificationCenter notifications) {
    this.manager = manager;
    this.notifications = notifications;
    setId("display-view");
    setSpacing(10);
    setPadding(new Insets(4));
    VBox.setVgrow(monitors, Priority.ALWAYS);
    brightness.setShowTickLabels(true);
    brightness.setMajorTickUnit(25);
    Button applyBrightness = new Button("Helligkeit anwenden");
    applyBrightness.setOnAction(ignored -> setBrightness());
    nightMode.setOnAction(ignored -> setNightMode());
    Button refresh = new Button("Neu erkennen");
    refresh.setOnAction(ignored -> load());
    getChildren()
        .addAll(
            summary,
            new HBox(8, refresh, nightMode),
            new Label("Helligkeit"),
            brightness,
            applyBrightness,
            monitors);
    load();
  }

  private void load() {
    manager
        .inspect()
        .whenComplete(
            (state, error) ->
                Platform.runLater(
                    () -> {
                      if (error != null) {
                        notifications.show("Anzeige", "Erkennung fehlgeschlagen.");
                      } else {
                        show(state);
                      }
                    }));
  }

  private void show(DisplayState state) {
    updating = true;
    summary.setText(
        state.message()
            + " GPU: "
            + state.graphics().gpu()
            + " · "
            + state.graphics().driver()
            + " · "
            + state.graphics().vulkan()
            + " · "
            + state.graphics().openGl());
    monitors.getItems().clear();
    state
        .monitors()
        .forEach(
            monitor ->
                monitors
                    .getItems()
                    .add(
                        monitor.name()
                            + " · "
                            + monitor.mode()
                            + " · Skalierung "
                            + monitor.scale()
                            + (monitor.primary() ? " · Primär" : "")));
    brightness.setValue(Math.max(1, state.brightnessPercent()));
    brightness.setDisable(!state.brightnessAdjustable());
    nightMode.setSelected(state.nightMode());
    nightMode.setDisable(!state.nightModeAdjustable());
    updating = false;
  }

  private void setBrightness() {
    manager
        .setBrightness((int) Math.round(brightness.getValue()))
        .thenAccept(
            result -> Platform.runLater(() -> notifications.show("Anzeige", result.message())));
  }

  private void setNightMode() {
    if (updating) {
      return;
    }
    manager
        .setNightMode(nightMode.isSelected())
        .thenAccept(
            result ->
                Platform.runLater(
                    () -> {
                      notifications.show("Anzeige", result.message());
                      if (!result.success()) {
                        load();
                      }
                    }));
  }
}
