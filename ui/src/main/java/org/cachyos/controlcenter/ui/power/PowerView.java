package org.cachyos.controlcenter.ui.power;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.cachyos.controlcenter.modules.power.PowerManager;
import org.cachyos.controlcenter.modules.power.PowerState;
import org.cachyos.controlcenter.ui.notifications.NotificationCenter;

public final class PowerView extends VBox {
  private final PowerManager manager;
  private final NotificationCenter notifications;
  private final Label summary = new Label("Energiezustand wird erkannt …");
  private final ComboBox<String> profiles = new ComboBox<>();
  private final Button suspend = new Button("Suspend");
  private final Button hibernate = new Button("Hibernate");

  public PowerView(PowerManager manager, NotificationCenter notifications) {
    this.manager = manager;
    this.notifications = notifications;
    setId("power-view");
    setSpacing(10);
    setPadding(new Insets(4));
    Button apply = new Button("Profil anwenden");
    apply.setOnAction(ignored -> setProfile());
    suspend.setOnAction(ignored -> suspend());
    hibernate.setOnAction(ignored -> hibernate());
    Button refresh = new Button("Neu erkennen");
    refresh.setOnAction(ignored -> load());
    getChildren()
        .addAll(
            summary,
            new Label("Energieprofil"),
            new HBox(8, profiles, apply),
            new HBox(8, suspend, hibernate, refresh),
            new Label(
                "Suspend und Hibernate werden erst nach Bestätigung an systemd/logind übergeben."));
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
                        notifications.show("Energie", "Erkennung fehlgeschlagen.");
                      } else {
                        show(state);
                      }
                    }));
  }

  private void show(PowerState state) {
    summary.setText(
        state.batteryPresent()
            ? "Akku " + state.batteryPercent() + " % · " + state.batteryStatus()
            : "Kein Akku erkannt · " + state.message());
    profiles.getItems().setAll(state.profiles().stream().map(profile -> profile.id()).toList());
    state.profiles().stream()
        .filter(profile -> profile.active())
        .findFirst()
        .ifPresent(profile -> profiles.setValue(profile.id()));
    suspend.setDisable(!state.canSuspend());
    hibernate.setDisable(!state.canHibernate());
  }

  private void setProfile() {
    String selected = profiles.getValue();
    manager
        .setProfile(selected)
        .thenAccept(
            result -> Platform.runLater(() -> notifications.show("Energie", result.message())));
  }

  private void suspend() {
    Alert dialog =
        new Alert(
            Alert.AlertType.CONFIRMATION,
            "Die Sitzung wird in den Standby versetzt.",
            ButtonType.CANCEL,
            ButtonType.OK);
    boolean confirmed = dialog.showAndWait().filter(ButtonType.OK::equals).isPresent();
    manager
        .suspend(confirmed)
        .thenAccept(
            result -> Platform.runLater(() -> notifications.show("Energie", result.message())));
  }

  private void hibernate() {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setHeaderText("Zum Bestätigen RUHEZUSTAND eingeben");
    String confirmation = dialog.showAndWait().orElse("");
    manager
        .hibernate(confirmation)
        .thenAccept(
            result -> Platform.runLater(() -> notifications.show("Energie", result.message())));
  }
}
