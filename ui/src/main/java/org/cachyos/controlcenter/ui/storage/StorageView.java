package org.cachyos.controlcenter.ui.storage;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.cachyos.controlcenter.modules.storage.StorageManager;
import org.cachyos.controlcenter.modules.storage.StorageSnapshot;
import org.cachyos.controlcenter.ui.notifications.NotificationCenter;

public final class StorageView extends VBox {
  private final StorageManager manager;
  private final NotificationCenter notifications;
  private final Label summary = new Label("Speicher wird erkannt …");
  private final ListView<String> entries = new ListView<>();

  public StorageView(StorageManager manager, NotificationCenter notifications) {
    this.manager = manager;
    this.notifications = notifications;
    setId("storage-view");
    setSpacing(10);
    setPadding(new Insets(4));
    entries.setId("storage-entries");
    VBox.setVgrow(entries, Priority.ALWAYS);
    Button refresh = new Button("Neu erkennen");
    refresh.setOnAction(ignored -> load());
    Button large = new Button("Große Dateien im Home suchen");
    large.setOnAction(ignored -> findLarge());
    getChildren().addAll(summary, new javafx.scene.layout.HBox(8, refresh, large), entries);
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
                        notifications.show("Speicher", "Erkennung fehlgeschlagen.");
                      } else {
                        show(snapshot);
                      }
                    }));
  }

  private void show(StorageSnapshot snapshot) {
    summary.setText(
        snapshot.available()
            ? snapshot.devices().size()
                + " Geräte/Partitionen · "
                + snapshot.mounts().size()
                + " Mounts · Btrfs Root: "
                + snapshot.btrfsRoot()
            : snapshot.message());
    entries.getItems().clear();
    snapshot
        .devices()
        .forEach(
            device ->
                entries
                    .getItems()
                    .add(
                        device.path()
                            + " · "
                            + device.type()
                            + " · "
                            + format(device.sizeBytes())
                            + " · "
                            + device.fileSystem()
                            + " · "
                            + device.mountPoint()));
    snapshot
        .smart()
        .forEach(
            health -> entries.getItems().add("SMART " + health.device() + " · " + health.status()));
    entries.getItems().add(snapshot.btrfsUsage());
  }

  private void findLarge() {
    summary.setText("Große Dateien werden begrenzt und ohne Symlink-Folgen gesucht …");
    manager
        .findLargeFiles()
        .whenComplete(
            (files, error) ->
                Platform.runLater(
                    () -> {
                      if (error != null) {
                        notifications.show("Speicheranalyse", "Analyse fehlgeschlagen.");
                        return;
                      }
                      entries.getItems().clear();
                      files.forEach(
                          file ->
                              entries
                                  .getItems()
                                  .add(format(file.sizeBytes()) + " · " + file.path()));
                      summary.setText(files.size() + " große Dateien im Benutzer-Home");
                    }));
  }

  private static String format(long bytes) {
    return String.format(java.util.Locale.GERMAN, "%.1f GiB", bytes / 1_073_741_824.0);
  }
}
