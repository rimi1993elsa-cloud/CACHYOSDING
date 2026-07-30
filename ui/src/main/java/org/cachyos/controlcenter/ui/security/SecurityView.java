package org.cachyos.controlcenter.ui.security;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.cachyos.controlcenter.modules.security.ListeningPort;
import org.cachyos.controlcenter.modules.security.SecurityCheck;
import org.cachyos.controlcenter.modules.security.SecurityManager;
import org.cachyos.controlcenter.modules.security.SecuritySnapshot;
import org.cachyos.controlcenter.ui.notifications.NotificationCenter;

public final class SecurityView extends VBox {
  private final SecurityManager manager;
  private final NotificationCenter notifications;
  private final Label summary = new Label("Sicherheitsstatus wird lokal gelesen …");
  private final ListView<SecurityCheck> checks = new ListView<>();
  private final ListView<ListeningPort> ports = new ListView<>();
  private final Button firewall = new Button("Firewall aktivieren");
  private boolean firewallEnabled;

  public SecurityView(SecurityManager manager, NotificationCenter notifications) {
    this.manager = manager;
    this.notifications = notifications;
    setId("security-view");
    setSpacing(10);
    setPadding(new Insets(4));

    checks.setId("security-checks");
    checks.setCellFactory(ignored -> new CheckCell());
    ports.setId("security-ports");
    ports.setCellFactory(ignored -> new PortCell());
    ports.setPrefHeight(140);
    VBox.setVgrow(checks, Priority.ALWAYS);

    Button refresh = new Button("Neu prüfen");
    refresh.setOnAction(ignored -> inspect());
    firewall.setDisable(true);
    firewall.setOnAction(ignored -> changeFirewall());
    HBox actions = new HBox(8, refresh, firewall);
    getChildren()
        .addAll(
            summary,
            actions,
            checks,
            new Label("Lauschende lokale Ports"),
            ports,
            new Label(
                "Kein Gesamtscore: Jeder Befund wird einzeln mit Evidenz und Unsicherheit bewertet."));
    inspect();
  }

  private void inspect() {
    manager
        .inspect()
        .whenComplete(
            (snapshot, error) ->
                Platform.runLater(
                    () -> {
                      if (error != null) {
                        notifications.show("Sicherheit", "Status konnte nicht gelesen werden.");
                      } else {
                        show(snapshot);
                      }
                    }));
  }

  private void show(SecuritySnapshot snapshot) {
    checks.getItems().setAll(snapshot.checks());
    ports.getItems().setAll(snapshot.listeningPorts());
    firewallEnabled = snapshot.firewallEnabled();
    firewall.setText(firewallEnabled ? "Firewall deaktivieren" : "Firewall aktivieren");
    firewall.setDisable(!snapshot.available());
    long warnings =
        snapshot.checks().stream()
            .filter(
                check ->
                    check.status()
                        != org.cachyos.controlcenter.modules.security.SecurityStatus.GOOD)
            .count();
    summary.setText(
        snapshot.checks().size()
            + " Einzelprüfungen · "
            + warnings
            + " Warnungen/Unbekannt · "
            + snapshot.message());
  }

  private void changeFirewall() {
    boolean enable = !firewallEnabled;
    Alert dialog =
        new Alert(
            Alert.AlertType.CONFIRMATION,
            enable
                ? "firewalld wird dauerhaft aktiviert und gestartet."
                : "firewalld wird gestoppt und dauerhaft deaktiviert.",
            ButtonType.CANCEL,
            ButtonType.OK);
    dialog.setHeaderText("Firewall-Änderung über Polkit bestätigen");
    if (dialog.showAndWait().filter(ButtonType.OK::equals).isEmpty()) {
      return;
    }
    firewall.setDisable(true);
    manager
        .setFirewallEnabled(enable)
        .whenComplete(
            (result, error) ->
                Platform.runLater(
                    () -> {
                      if (error != null) {
                        notifications.show("Firewall", "Aktion fehlgeschlagen.");
                      } else {
                        notifications.show("Firewall", result.message());
                        if (result.successful()) {
                          inspect();
                        } else {
                          firewall.setDisable(false);
                        }
                      }
                    }));
  }

  private static final class CheckCell extends ListCell<SecurityCheck> {
    @Override
    protected void updateItem(SecurityCheck item, boolean empty) {
      super.updateItem(item, empty);
      setText(
          empty || item == null
              ? null
              : item.status()
                  + " · "
                  + item.title()
                  + "\n"
                  + item.evidence()
                  + (item.recommendation().isBlank() ? "" : "\n" + item.recommendation()));
      setWrapText(true);
    }
  }

  private static final class PortCell extends ListCell<ListeningPort> {
    @Override
    protected void updateItem(ListeningPort item, boolean empty) {
      super.updateItem(item, empty);
      setText(
          empty || item == null
              ? null
              : item.protocol()
                  + " · "
                  + item.localAddress()
                  + ":"
                  + item.port()
                  + (item.process().isBlank() ? "" : " · " + item.process()));
    }
  }
}
