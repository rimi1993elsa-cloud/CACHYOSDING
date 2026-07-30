package org.cachyos.controlcenter.ui.applications;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.cachyos.controlcenter.core.action.ActionDispatcher;
import org.cachyos.controlcenter.core.action.ActionId;
import org.cachyos.controlcenter.core.action.ActionRequest;
import org.cachyos.controlcenter.core.action.InputSource;
import org.cachyos.controlcenter.modules.applications.ApplicationEntry;
import org.cachyos.controlcenter.modules.applications.ApplicationManagerModule;
import org.cachyos.controlcenter.ui.notifications.NotificationCenter;

/** Searchable XDG application catalog with ID-only launch requests. */
public final class ApplicationsView extends VBox {
  private final ApplicationManagerModule manager;
  private final ActionDispatcher dispatcher;
  private final NotificationCenter notifications;
  private final TextField search = new TextField();
  private final ListView<ApplicationEntry> applications = new ListView<>();
  private final Label details = new Label("Anwendung auswählen");
  private final Label packageName = new Label();
  private final Button launch = new Button("Starten");
  private final Button favorite = new Button("Favorit umschalten");
  private List<ApplicationEntry> allApplications = List.of();

  public ApplicationsView(
      ApplicationManagerModule manager,
      ActionDispatcher dispatcher,
      NotificationCenter notifications) {
    this.manager = manager;
    this.dispatcher = dispatcher;
    this.notifications = notifications;
    search.setPromptText("Installierte Anwendungen durchsuchen");
    search.setId("application-search");
    applications.setId("application-list");
    applications.setPrefHeight(420);
    applications.setCellFactory(ignored -> new ApplicationCell());
    search.textProperty().addListener((ignored, previous, value) -> filter(value));
    applications
        .getSelectionModel()
        .selectedItemProperty()
        .addListener((ignored, previous, selected) -> select(selected));
    launch.setDisable(true);
    favorite.setDisable(true);
    launch.setOnAction(ignored -> launchSelected());
    favorite.setOnAction(
        ignored -> {
          ApplicationEntry selected = applications.getSelectionModel().getSelectedItem();
          if (selected != null) {
            manager.setFavorite(selected.id(), !selected.favorite());
            reload();
          }
        });
    details.setWrapText(true);
    packageName.getStyleClass().add("muted-label");
    HBox controls = new HBox(8, launch, favorite);
    VBox info = new VBox(7, details, packageName, controls);
    info.setPadding(new Insets(14));
    info.getStyleClass().add("details-panel");
    setSpacing(14);
    getChildren().addAll(search, applications, info);
    VBox.setVgrow(applications, Priority.ALWAYS);
    reload();
  }

  private void reload() {
    search.setDisable(true);
    CompletableFuture.supplyAsync(manager::applications)
        .whenComplete(
            (entries, failure) ->
                Platform.runLater(
                    () -> {
                      search.setDisable(false);
                      allApplications = failure == null ? entries : List.of();
                      filter(search.getText());
                    }));
  }

  private void filter(String query) {
    String normalized = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
    applications
        .getItems()
        .setAll(
            allApplications.stream()
                .filter(
                    entry ->
                        normalized.isBlank()
                            || entry.name().toLowerCase(Locale.ROOT).contains(normalized)
                            || entry.comment().toLowerCase(Locale.ROOT).contains(normalized))
                .toList());
  }

  private void select(ApplicationEntry entry) {
    launch.setDisable(entry == null);
    favorite.setDisable(entry == null);
    if (entry == null) {
      details.setText("Anwendung auswählen");
      packageName.setText("");
      return;
    }
    details.setText(
        (entry.favorite() ? "★ " : "")
            + entry.name()
            + (entry.comment().isBlank() ? "" : "\n" + entry.comment()));
    packageName.setText("Paket wird ermittelt …");
    CompletableFuture.supplyAsync(() -> manager.findPackage(entry.id()))
        .whenComplete(
            (result, failure) ->
                Platform.runLater(
                    () -> {
                      ApplicationEntry selected =
                          applications.getSelectionModel().getSelectedItem();
                      if (selected != null && selected.id().equals(entry.id())) {
                        packageName.setText(
                            "Paket: "
                                + (failure == null
                                    ? result.orElse("Nicht ermittelbar")
                                    : "Nicht ermittelbar"));
                      }
                    }));
  }

  private void launchSelected() {
    ApplicationEntry selected = applications.getSelectionModel().getSelectedItem();
    if (selected == null) {
      return;
    }
    launch.setDisable(true);
    dispatcher
        .dispatch(
            new ActionRequest(
                ActionId.APPLICATION_LAUNCH,
                InputSource.BUTTON,
                Map.of("applicationId", selected.id()),
                Instant.now()))
        .whenComplete(
            (result, failure) ->
                Platform.runLater(
                    () -> {
                      launch.setDisable(false);
                      notifications.show(
                          "Anwendungen",
                          failure == null
                              ? result.userMessage()
                              : "Die Anwendung konnte nicht gestartet werden.");
                    }));
  }

  private static final class ApplicationCell extends ListCell<ApplicationEntry> {
    @Override
    protected void updateItem(ApplicationEntry item, boolean empty) {
      super.updateItem(item, empty);
      if (empty || item == null) {
        setText(null);
        setGraphic(null);
        return;
      }
      Label name = new Label((item.favorite() ? "★ " : "") + item.name());
      name.getStyleClass().add("card-title");
      Label comment = new Label(item.comment());
      comment.getStyleClass().add("muted-label");
      VBox text = new VBox(2, name, comment);
      HBox row = new HBox(10);
      row.setAlignment(Pos.CENTER_LEFT);
      Optional<java.nio.file.Path> icon = item.icon();
      if (icon.isPresent()) {
        Image image = new Image(icon.get().toUri().toString(), 32, 32, true, true, true);
        ImageView view = new ImageView(image);
        view.setFitWidth(32);
        view.setFitHeight(32);
        row.getChildren().add(view);
      }
      row.getChildren().add(text);
      setText(null);
      setGraphic(row);
    }
  }
}
