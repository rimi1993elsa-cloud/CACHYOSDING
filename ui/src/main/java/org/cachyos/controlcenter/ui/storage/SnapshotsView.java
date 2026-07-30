package org.cachyos.controlcenter.ui.storage;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.cachyos.controlcenter.modules.snapshots.SnapshotEntry;
import org.cachyos.controlcenter.modules.snapshots.SnapshotManager;
import org.cachyos.controlcenter.modules.snapshots.SnapshotState;
import org.cachyos.controlcenter.ui.notifications.NotificationCenter;

public final class SnapshotsView extends VBox {
  private final SnapshotManager manager;
  private final NotificationCenter notifications;
  private final Label summary = new Label("Snapper wird geprüft …");
  private final ListView<SnapshotEntry> entries = new ListView<>();
  private final TextField description = new TextField("CachyOS Control Center");

  public SnapshotsView(SnapshotManager manager, NotificationCenter notifications) {
    this.manager = manager;
    this.notifications = notifications;
    setId("snapshots-view");
    setSpacing(10);
    setPadding(new Insets(4));
    entries.setId("snapshot-list");
    entries.setCellFactory(ignored -> new SnapshotCell());
    VBox.setVgrow(entries, Priority.ALWAYS);
    description.setPromptText("Snapshot-Beschreibung");
    Button create = new Button("Snapshot erstellen");
    create.setOnAction(ignored -> create());
    Button delete = new Button("Ausgewählten Snapshot löschen");
    delete.setOnAction(ignored -> delete());
    getChildren().addAll(summary, new HBox(8, description, create), entries, delete);
    HBox.setHgrow(description, Priority.ALWAYS);
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
                        notifications.show(
                            "Snapshots", "Snapper-Status konnte nicht gelesen werden.");
                      } else {
                        show(state);
                      }
                    }));
  }

  private void show(SnapshotState state) {
    summary.setText(state.available() ? state.entries().size() + " Snapshots" : state.message());
    entries.getItems().setAll(state.entries());
  }

  private void create() {
    manager
        .create(description.getText())
        .whenComplete(
            (result, error) ->
                Platform.runLater(
                    () -> {
                      notifications.show(
                          "Snapshots",
                          error == null ? result.message() : "Erstellung fehlgeschlagen.");
                      if (error == null && result.successful()) {
                        load();
                      }
                    }));
  }

  private void delete() {
    SnapshotEntry selected = entries.getSelectionModel().getSelectedItem();
    if (selected == null) {
      return;
    }
    TextInputDialog dialog = new TextInputDialog();
    dialog.setHeaderText("Zum Löschen Snapshot-ID " + selected.id() + " eingeben");
    dialog.setContentText("Snapshot-ID:");
    dialog
        .showAndWait()
        .ifPresent(
            confirmation ->
                manager
                    .delete(selected.id(), confirmation)
                    .whenComplete(
                        (result, error) ->
                            Platform.runLater(
                                () -> {
                                  notifications.show(
                                      "Snapshots",
                                      error == null
                                          ? result.message()
                                          : "Löschung fehlgeschlagen.");
                                  if (error == null && result.successful()) {
                                    load();
                                  }
                                })));
  }

  private static final class SnapshotCell extends ListCell<SnapshotEntry> {
    @Override
    protected void updateItem(SnapshotEntry item, boolean empty) {
      super.updateItem(item, empty);
      setText(
          empty || item == null
              ? null
              : item.id() + " · " + item.type() + " · " + item.date() + " · " + item.description());
    }
  }
}
