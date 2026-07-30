package org.cachyos.controlcenter.ui.network;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.cachyos.controlcenter.core.action.ActionDispatcher;
import org.cachyos.controlcenter.core.action.ActionId;
import org.cachyos.controlcenter.core.action.ActionRequest;
import org.cachyos.controlcenter.core.action.InputSource;
import org.cachyos.controlcenter.modules.network.NetworkEvents;
import org.cachyos.controlcenter.modules.network.NetworkManagerModule;
import org.cachyos.controlcenter.modules.network.NetworkSnapshot;
import org.cachyos.controlcenter.ui.notifications.NotificationCenter;

/** NetworkManager page with secret-free saved-profile actions and event-driven refresh. */
public final class NetworkView extends VBox {
  private final NetworkManagerModule manager;
  private final ActionDispatcher dispatcher;
  private final NotificationCenter notifications;
  private final AtomicBoolean loading = new AtomicBoolean();
  private final Label status = new Label("Netzwerkstatus wird geladen …");
  private final Label details = new Label();
  private final ListView<NetworkSnapshot.Device> devices = new ListView<>();
  private final ListView<NetworkSnapshot.AccessPoint> accessPoints = new ListView<>();
  private final ListView<NetworkSnapshot.Profile> profiles = new ListView<>();
  private final Button wifiOn = new Button("WLAN ein");
  private final Button wifiOff = new Button("WLAN aus");
  private final Button scan = new Button("WLAN neu suchen");
  private final Button connect = new Button("Gespeichertes Profil verbinden");
  private final Button disconnect = new Button("Gerät trennen");

  public NetworkView(
      NetworkManagerModule manager,
      NetworkEvents events,
      ActionDispatcher dispatcher,
      NotificationCenter notifications) {
    this.manager = manager;
    this.dispatcher = dispatcher;
    this.notifications = notifications;
    devices.setId("network-devices");
    accessPoints.setId("network-access-points");
    profiles.setId("network-profiles");
    devices.setPrefHeight(145);
    accessPoints.setPrefHeight(180);
    profiles.setPrefHeight(160);
    devices.setCellFactory(
        ignored ->
            new TextCell<>(
                device ->
                    device.name()
                        + " · "
                        + device.type()
                        + " · "
                        + device.state()
                        + " · "
                        + device.connection()));
    accessPoints.setCellFactory(
        ignored ->
            new TextCell<>(
                accessPoint ->
                    (accessPoint.active() ? "● " : "")
                        + accessPoint.ssid()
                        + " · "
                        + accessPoint.signal()
                        + " % · "
                        + accessPoint.security()));
    profiles.setCellFactory(
        ignored ->
            new TextCell<>(
                profile ->
                    (profile.active() ? "● " : "") + profile.name() + " · " + profile.type()));

    wifiOn.setOnAction(ignored -> dispatch(ActionId.NETWORK_WIFI_ON, Map.of()));
    wifiOff.setOnAction(ignored -> dispatch(ActionId.NETWORK_WIFI_OFF, Map.of()));
    scan.setOnAction(ignored -> dispatch(ActionId.NETWORK_SCAN_WIFI, Map.of()));
    connect.setDisable(true);
    connect.setOnAction(
        ignored -> {
          NetworkSnapshot.Profile profile = profiles.getSelectionModel().getSelectedItem();
          if (profile != null) {
            dispatch(ActionId.NETWORK_ACTIVATE_PROFILE, Map.of("profileUuid", profile.uuid()));
          }
        });
    disconnect.setDisable(true);
    disconnect.setOnAction(
        ignored -> {
          NetworkSnapshot.Device device = devices.getSelectionModel().getSelectedItem();
          if (device != null) {
            dispatch(ActionId.NETWORK_DISCONNECT_DEVICE, Map.of("deviceName", device.name()));
          }
        });
    profiles
        .getSelectionModel()
        .selectedItemProperty()
        .addListener((ignored, previous, selected) -> connect.setDisable(selected == null));
    devices
        .getSelectionModel()
        .selectedItemProperty()
        .addListener((ignored, previous, selected) -> disconnect.setDisable(selected == null));

    FlowPane controls = new FlowPane(8, 8, wifiOn, wifiOff, scan, connect, disconnect);
    details.setWrapText(true);
    details.getStyleClass().add("muted-label");
    setSpacing(14);
    getChildren()
        .addAll(
            status,
            controls,
            details,
            section("Geräte und Verbindungen", devices),
            section("WLANs in Reichweite", accessPoints),
            section("Gespeicherte Profile und VPNs", profiles),
            new Label(
                "Neue geschützte WLANs werden nicht über Prozessargumente verbunden. "
                    + "Lege sie sicher über den NetworkManager-Secret-Agent an."));
    events.subscribe(this::refresh);
    refresh();
  }

  private void refresh() {
    if (!loading.compareAndSet(false, true)) {
      return;
    }
    CompletableFuture.supplyAsync(manager::networkSnapshot)
        .whenComplete(
            (snapshot, failure) ->
                Platform.runLater(
                    () -> {
                      loading.set(false);
                      if (failure != null) {
                        show(
                            NetworkSnapshot.unavailable(
                                "Netzwerkstatus konnte nicht gelesen werden."));
                      } else {
                        show(snapshot);
                      }
                    }));
  }

  private void show(NetworkSnapshot snapshot) {
    status.setText(
        snapshot.available()
            ? (snapshot.online() ? "Verbunden" : "Offline")
                + " · WLAN "
                + (snapshot.wifiEnabled() ? "ein" : "aus")
            : snapshot.message());
    details.setText(
        "Gateway: "
            + display(snapshot.gateways())
            + " · DNS: "
            + display(snapshot.dnsServers())
            + " · Aktualisiert: "
            + snapshot.capturedAt());
    devices.getItems().setAll(snapshot.devices());
    accessPoints.getItems().setAll(snapshot.accessPoints());
    profiles.getItems().setAll(snapshot.profiles());
    wifiOn.setDisable(!snapshot.available() || snapshot.wifiEnabled());
    wifiOff.setDisable(!snapshot.available() || !snapshot.wifiEnabled());
    scan.setDisable(!snapshot.available() || !snapshot.wifiEnabled());
  }

  private void dispatch(ActionId actionId, Map<String, String> parameters) {
    setControlsDisabled(true);
    dispatcher
        .dispatch(new ActionRequest(actionId, InputSource.BUTTON, parameters, Instant.now()))
        .whenComplete(
            (result, failure) ->
                Platform.runLater(
                    () -> {
                      if (failure != null) {
                        notifications.show("Netzwerk", "Die Aktion ist unerwartet fehlgeschlagen.");
                      } else {
                        notifications.show("Netzwerk", result.userMessage());
                      }
                      setControlsDisabled(false);
                      refresh();
                    }));
  }

  private void setControlsDisabled(boolean disabled) {
    wifiOn.setDisable(disabled);
    wifiOff.setDisable(disabled);
    scan.setDisable(disabled);
    connect.setDisable(disabled || profiles.getSelectionModel().getSelectedItem() == null);
    disconnect.setDisable(disabled || devices.getSelectionModel().getSelectedItem() == null);
  }

  private static VBox section(String title, ListView<?> list) {
    Label heading = new Label(title);
    heading.getStyleClass().add("card-title");
    VBox box = new VBox(7, heading, list);
    box.setPadding(new Insets(14));
    box.getStyleClass().add("details-panel");
    return box;
  }

  private static String display(java.util.List<String> values) {
    return values.isEmpty() ? "Nicht verfügbar" : String.join(", ", values);
  }

  private static final class TextCell<T> extends javafx.scene.control.ListCell<T> {
    private final java.util.function.Function<T, String> formatter;

    private TextCell(java.util.function.Function<T, String> formatter) {
      this.formatter = formatter;
    }

    @Override
    protected void updateItem(T item, boolean empty) {
      super.updateItem(item, empty);
      setText(empty || item == null ? null : formatter.apply(item));
    }
  }
}
