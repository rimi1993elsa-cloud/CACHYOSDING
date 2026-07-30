package org.cachyos.controlcenter.ui.services;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.cachyos.controlcenter.modules.services.ServiceManager;
import org.cachyos.controlcenter.modules.services.ServiceOperation;
import org.cachyos.controlcenter.modules.services.ServiceUnit;
import org.cachyos.controlcenter.ui.notifications.NotificationCenter;

public final class ServicesView extends VBox {
  private final ServiceManager manager;
  private final NotificationCenter notifications;
  private final Label summary = new Label("System- und Benutzerdienste werden gelesen …");
  private final ListView<ServiceUnit> units = new ListView<>();
  private final TextArea logs = new TextArea();

  public ServicesView(ServiceManager manager, NotificationCenter notifications) {
    this.manager = manager;
    this.notifications = notifications;
    setId("services-view");
    setSpacing(8);
    setPadding(new Insets(4));
    units.setId("service-list");
    units.setCellFactory(ignored -> new ServiceCell());
    VBox.setVgrow(units, Priority.ALWAYS);
    logs.setEditable(false);
    logs.setWrapText(false);
    logs.setPrefRowCount(8);
    Button refresh = new Button("Aktualisieren");
    refresh.setOnAction(ignored -> load());
    Button start = action("Start", ServiceOperation.START);
    Button stop = action("Stop", ServiceOperation.STOP);
    Button restart = action("Restart", ServiceOperation.RESTART);
    Button showLogs = new Button("Logs");
    showLogs.setOnAction(ignored -> logs());
    getChildren()
        .addAll(summary, new HBox(8, refresh, start, stop, restart, showLogs), units, logs);
    load();
  }

  private Button action(String label, ServiceOperation operation) {
    Button button = new Button(label);
    button.setOnAction(
        ignored -> {
          ServiceUnit selected = units.getSelectionModel().getSelectedItem();
          if (selected == null) {
            return;
          }
          manager
              .execute(selected.scope(), selected.name(), operation)
              .whenComplete(
                  (result, error) ->
                      Platform.runLater(
                          () ->
                              notifications.show(
                                  "Dienste",
                                  error == null
                                      ? result.message()
                                      : "Dienstaktion fehlgeschlagen.")));
        });
    return button;
  }

  private void load() {
    manager
        .inspect()
        .whenComplete(
            (state, error) ->
                Platform.runLater(
                    () -> {
                      if (error != null) {
                        notifications.show("Dienste", "Status konnte nicht gelesen werden.");
                      } else {
                        units.getItems().setAll(state.units());
                        long system =
                            state.units().stream()
                                .filter(unit -> unit.scope().name().equals("SYSTEM"))
                                .count();
                        summary.setText(
                            system
                                + " Systemdienste · "
                                + (state.units().size() - system)
                                + " Benutzerdienste");
                      }
                    }));
  }

  private void logs() {
    ServiceUnit selected = units.getSelectionModel().getSelectedItem();
    if (selected == null) {
      return;
    }
    manager
        .logs(selected.scope(), selected.name())
        .whenComplete(
            (lines, error) ->
                Platform.runLater(
                    () ->
                        logs.setText(
                            error == null ? String.join("\n", lines) : "Logs nicht verfügbar")));
  }

  private static final class ServiceCell extends ListCell<ServiceUnit> {
    @Override
    protected void updateItem(ServiceUnit item, boolean empty) {
      super.updateItem(item, empty);
      setText(
          empty || item == null
              ? null
              : item.scope()
                  + " · "
                  + item.name()
                  + " · "
                  + item.activeState()
                  + "/"
                  + item.subState()
                  + " · "
                  + item.description());
    }
  }
}
