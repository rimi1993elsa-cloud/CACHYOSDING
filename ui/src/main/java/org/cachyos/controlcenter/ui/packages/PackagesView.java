package org.cachyos.controlcenter.ui.packages;

import java.util.Optional;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.cachyos.controlcenter.modules.packages.PackageAction;
import org.cachyos.controlcenter.modules.packages.PackageEntry;
import org.cachyos.controlcenter.modules.packages.PackageManager;
import org.cachyos.controlcenter.modules.packages.PackageSnapshot;
import org.cachyos.controlcenter.modules.packages.PackageTransactionPreview;
import org.cachyos.controlcenter.ui.notifications.NotificationCenter;

public final class PackagesView extends VBox {
  private final PackageManager manager;
  private final NotificationCenter notifications;
  private final TextField search = new TextField();
  private final ListView<PackageEntry> packages = new ListView<>();
  private final Label summary = new Label("Paketdaten werden geladen …");
  private final Label details = new Label("Paket auswählen, um Details anzuzeigen.");
  private final Label progress = new Label("Bereit");
  private final Button install = new Button("Installieren");
  private final Button remove = new Button("Entfernen");

  public PackagesView(PackageManager manager, NotificationCenter notifications) {
    this.manager = manager;
    this.notifications = notifications;
    setId("packages-view");
    setSpacing(12);
    setPadding(new Insets(4));

    search.setPromptText("Repository-Pakete suchen");
    search.setId("package-search");
    Button searchButton = new Button("Suchen");
    searchButton.setOnAction(ignored -> search());
    search.setOnAction(ignored -> search());
    Button refresh = new Button("Aktualisieren");
    refresh.setOnAction(ignored -> load(true));
    HBox searchBar = new HBox(8, search, searchButton, refresh);
    HBox.setHgrow(search, Priority.ALWAYS);

    packages.setId("package-list");
    packages.setCellFactory(ignored -> new PackageCell());
    packages
        .getSelectionModel()
        .selectedItemProperty()
        .addListener((ignored, previous, selected) -> selected(selected));
    VBox.setVgrow(packages, Priority.ALWAYS);

    install.setOnAction(ignored -> preview(PackageAction.INSTALL));
    remove.setOnAction(ignored -> preview(PackageAction.REMOVE));
    install.setDisable(true);
    remove.setDisable(true);
    HBox actions = new HBox(8, install, remove);

    details.setWrapText(true);
    details.getStyleClass().add("muted-label");
    progress.getStyleClass().add("muted-label");
    getChildren().addAll(searchBar, summary, packages, details, actions, progress);

    manager.onProgress(
        update ->
            Platform.runLater(() -> progress.setText(update.state() + " · " + update.message())));
    load(false);
  }

  private void load(boolean refresh) {
    manager
        .snapshot(refresh)
        .whenComplete(
            (snapshot, error) ->
                Platform.runLater(
                    () -> {
                      if (error != null) {
                        failed("Paketdaten konnten nicht gelesen werden");
                        return;
                      }
                      showSnapshot(snapshot);
                    }));
  }

  private void showSnapshot(PackageSnapshot snapshot) {
    packages.getItems().setAll(snapshot.installed());
    summary.setText(
        snapshot.available()
            ? snapshot.installed().size()
                + " installiert · "
                + snapshot.updates().size()
                + " Updates · "
                + snapshot.orphanNames().size()
                + " verwaist · Cache "
                + formatBytes(snapshot.cacheBytes())
                + (snapshot.locked() ? " · Pacman gesperrt" : "")
            : snapshot.message());
    install.setDisable(!snapshot.available() || snapshot.locked());
  }

  private void search() {
    manager
        .search(search.getText().trim())
        .whenComplete(
            (result, error) ->
                Platform.runLater(
                    () -> {
                      if (error != null) {
                        failed("Suchbegriff ist ungültig oder Pacman nicht verfügbar");
                      } else {
                        packages.getItems().setAll(result);
                        summary.setText(result.size() + " Suchergebnisse");
                      }
                    }));
  }

  private void selected(PackageEntry entry) {
    if (entry == null) {
      install.setDisable(true);
      remove.setDisable(true);
      return;
    }
    install.setDisable(entry.installed());
    remove.setDisable(!entry.installed());
    details.setText(entry.repository() + " · " + entry.name() + " " + entry.version());
    manager
        .details(entry.name())
        .whenComplete(
            (result, error) ->
                Platform.runLater(
                    () -> {
                      if (error == null) {
                        result.ifPresent(
                            value ->
                                details.setText(
                                    value.entry().description()
                                        + "\nArchitektur: "
                                        + value.architecture()
                                        + " · Abhängigkeiten: "
                                        + value.dependencies().size()));
                      }
                    }));
  }

  private void preview(PackageAction action) {
    PackageEntry selected = packages.getSelectionModel().getSelectedItem();
    if (selected == null) {
      return;
    }
    install.setDisable(true);
    remove.setDisable(true);
    manager
        .preview(action, selected.name())
        .whenComplete(
            (preview, error) ->
                Platform.runLater(
                    () -> {
                      selected(selected);
                      if (error != null) {
                        failed("Transaktionsvorschau fehlgeschlagen: " + rootMessage(error));
                      } else {
                        confirm(preview);
                      }
                    }));
  }

  private void confirm(PackageTransactionPreview preview) {
    ButtonType execute =
        new ButtonType("Authentifizieren und ausführen", ButtonBar.ButtonData.OK_DONE);
    Alert dialog =
        new Alert(
            Alert.AlertType.CONFIRMATION,
            String.join("\n", preview.changes()),
            ButtonType.CANCEL,
            execute);
    dialog.setTitle("Pacman-Transaktion bestätigen");
    dialog.setHeaderText(
        (preview.action() == PackageAction.INSTALL ? "Installation: " : "Entfernung: ")
            + preview.packageName()
            + "\nDownload: "
            + formatBytes(preview.downloadBytes())
            + " · Speicheränderung: "
            + formatSignedBytes(preview.installedDeltaBytes()));
    Optional<ButtonType> selected = dialog.showAndWait();
    if (selected.filter(execute::equals).isEmpty()) {
      notifications.show("Pakete", "Transaktion wurde nicht ausgeführt.");
      return;
    }
    manager
        .confirm(preview.id())
        .whenComplete(
            (result, error) ->
                Platform.runLater(
                    () -> {
                      if (error != null) {
                        failed(rootMessage(error));
                      } else {
                        notifications.show("Pacman", result.message());
                        if (result.successful()) {
                          load(true);
                        }
                      }
                    }));
  }

  private void failed(String message) {
    notifications.show("Pakete", message);
    progress.setText(message);
  }

  private static String rootMessage(Throwable error) {
    Throwable current = error;
    while (current.getCause() != null) {
      current = current.getCause();
    }
    return current.getMessage() == null ? "Unbekannter Fehler" : current.getMessage();
  }

  private static String formatBytes(long bytes) {
    return String.format(java.util.Locale.GERMAN, "%.1f GiB", bytes / 1_073_741_824.0);
  }

  private static String formatSignedBytes(long bytes) {
    return String.format(java.util.Locale.GERMAN, "%+.1f MiB", bytes / (1024.0 * 1024.0));
  }

  private static final class PackageCell extends ListCell<PackageEntry> {
    @Override
    protected void updateItem(PackageEntry item, boolean empty) {
      super.updateItem(item, empty);
      setText(
          empty || item == null
              ? null
              : item.name()
                  + "  "
                  + item.version()
                  + "  · "
                  + item.repository()
                  + (item.installed() ? "  · installiert" : ""));
    }
  }
}
