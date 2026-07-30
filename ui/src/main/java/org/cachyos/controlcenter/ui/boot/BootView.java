package org.cachyos.controlcenter.ui.boot;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.cachyos.controlcenter.modules.boot.BootManager;
import org.cachyos.controlcenter.modules.boot.BootSnapshot;
import org.cachyos.controlcenter.ui.notifications.NotificationCenter;

public final class BootView extends VBox {
  private final BootManager manager;
  private final NotificationCenter notifications;
  private final Label summary = new Label("Bootdaten werden gelesen …");
  private final ListView<String> kernels = new ListView<>();
  private final ListView<String> slowUnits = new ListView<>();
  private final TextArea parameters = new TextArea();
  private final Button launch = new Button("CachyOS Kernel Manager öffnen");

  public BootView(BootManager manager, NotificationCenter notifications) {
    this.manager = manager;
    this.notifications = notifications;
    setId("boot-view");
    setSpacing(10);
    setPadding(new Insets(4));
    parameters.setEditable(false);
    parameters.setWrapText(true);
    parameters.setPrefRowCount(3);
    VBox.setVgrow(slowUnits, Priority.ALWAYS);
    launch.setOnAction(ignored -> launch());
    Button refresh = new Button("Neu lesen");
    refresh.setOnAction(ignored -> load());
    getChildren()
        .addAll(
            summary,
            new HBox(8, refresh, launch),
            new Label("Installierte Kernel"),
            kernels,
            new Label("Kernelparameter (nur lesend)"),
            parameters,
            new Label("Langsame Units"),
            slowUnits);
    load();
  }

  private void load() {
    manager
        .inspect()
        .whenComplete(
            (snapshot, error) ->
                Platform.runLater(
                    () -> {
                      if (error != null) {
                        notifications.show("Boot & Kernel", "Lesen fehlgeschlagen.");
                      } else {
                        show(snapshot);
                      }
                    }));
  }

  private void show(BootSnapshot snapshot) {
    summary.setText(
        "Aktiv: "
            + snapshot.activeKernel()
            + " · "
            + snapshot.bootManager()
            + " · "
            + snapshot.bootDuration());
    kernels
        .getItems()
        .setAll(
            snapshot.installedKernels().stream()
                .map(
                    kernel ->
                        kernel.packageName()
                            + " "
                            + kernel.version()
                            + (kernel.active() ? " · AKTIV" : ""))
                .toList());
    parameters.setText(snapshot.kernelParameters());
    slowUnits
        .getItems()
        .setAll(
            snapshot.slowUnits().stream()
                .map(unit -> unit.duration() + " · " + unit.unit())
                .toList());
    launch.setDisable(!snapshot.kernelManagerAvailable());
  }

  private void launch() {
    manager
        .launchKernelManager()
        .thenAccept(
            result ->
                Platform.runLater(() -> notifications.show("Boot & Kernel", result.message())));
  }
}
