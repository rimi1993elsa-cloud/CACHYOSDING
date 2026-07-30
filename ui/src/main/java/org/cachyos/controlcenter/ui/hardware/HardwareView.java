package org.cachyos.controlcenter.ui.hardware;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.cachyos.controlcenter.modules.hardware.HardwareDevice;
import org.cachyos.controlcenter.modules.hardware.HardwareManager;
import org.cachyos.controlcenter.modules.hardware.HardwareSnapshot;
import org.cachyos.controlcenter.ui.notifications.NotificationCenter;

public final class HardwareView extends VBox {
  private final HardwareManager manager;
  private final NotificationCenter notifications;
  private final Label summary = new Label("Hardware wird erkannt …");
  private final ListView<String> devices = new ListView<>();
  private final TextArea report = new TextArea();
  private HardwareSnapshot snapshot;

  public HardwareView(HardwareManager manager, NotificationCenter notifications) {
    this.manager = manager;
    this.notifications = notifications;
    setId("hardware-view");
    setSpacing(10);
    setPadding(new Insets(4));
    devices.setId("hardware-devices");
    VBox.setVgrow(devices, Priority.ALWAYS);
    report.setEditable(false);
    report.setWrapText(true);
    report.setPrefRowCount(8);
    Button refresh = new Button("Neu erkennen");
    refresh.setOnAction(ignored -> load());
    Button anonymized = new Button("Anonymisierten Bericht kopieren");
    anonymized.setOnAction(ignored -> copy(true));
    Button local = new Button("Lokalen Bericht anzeigen");
    local.setOnAction(ignored -> showReport(false));
    getChildren().addAll(summary, new HBox(8, refresh, anonymized, local), devices, report);
    load();
  }

  private void load() {
    manager
        .inspect()
        .whenComplete(
            (result, error) ->
                Platform.runLater(
                    () -> {
                      if (error != null) {
                        notifications.show("Hardware", "Erkennung fehlgeschlagen.");
                        return;
                      }
                      snapshot = result;
                      showSnapshot(result);
                    }));
  }

  private void showSnapshot(HardwareSnapshot value) {
    summary.setText(
        value.available()
            ? value.manufacturer()
                + " "
                + value.product()
                + " · "
                + value.cpu()
                + " · "
                + formatMemory(value.memoryBytes())
                + " · Akku "
                + value.battery()
            : value.message());
    devices.getItems().clear();
    value.graphics().forEach(device -> add("GPU", device));
    value.pciDevices().forEach(device -> add("PCI", device));
    value.usbDevices().forEach(device -> add("USB", device));
    value
        .sensors()
        .forEach(
            sensor ->
                devices
                    .getItems()
                    .add("Sensor · " + sensor.label() + " " + sensor.value() + sensor.unit()));
    showReport(true);
  }

  private void add(String category, HardwareDevice device) {
    devices
        .getItems()
        .add(
            category
                + " · "
                + device.identifier()
                + " · "
                + device.description()
                + " · "
                + device.driver());
  }

  private void showReport(boolean anonymize) {
    if (snapshot != null) {
      report.setText(manager.report(snapshot, anonymize).text());
    }
  }

  private void copy(boolean anonymize) {
    if (snapshot == null) {
      return;
    }
    String text = manager.report(snapshot, anonymize).text();
    ClipboardContent content = new ClipboardContent();
    content.putString(text);
    Clipboard.getSystemClipboard().setContent(content);
    report.setText(text);
    notifications.show("Hardware", "Anonymisierter Bericht wurde kopiert.");
  }

  private static String formatMemory(long bytes) {
    return String.format(java.util.Locale.GERMAN, "%.1f GiB RAM", bytes / 1_073_741_824.0);
  }
}
