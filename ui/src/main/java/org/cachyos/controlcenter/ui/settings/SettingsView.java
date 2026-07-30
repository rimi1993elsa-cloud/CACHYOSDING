package org.cachyos.controlcenter.ui.settings;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.cachyos.controlcenter.core.audit.InMemoryAuditLog;
import org.cachyos.controlcenter.persistence.ApplicationSettings;
import org.cachyos.controlcenter.persistence.SettingsService;
import org.cachyos.controlcenter.ui.notifications.NotificationCenter;

public final class SettingsView extends VBox {
  private final SettingsService service;
  private final InMemoryAuditLog auditLog;
  private final NotificationCenter notifications;
  private final Runnable clearLiveChat;
  private final Runnable applySettings;
  private final Map<String, CheckBox> modules = new LinkedHashMap<>();
  private final Map<String, CheckBox> quickButtons = new LinkedHashMap<>();
  private final CheckBox microphone = new CheckBox("Mikrofon/Push-to-Talk erlauben");
  private final CheckBox onlineAi = new CheckBox("Online-KI erlauben");
  private final ComboBox<String> provider = new ComboBox<>();
  private final TextField budget = new TextField();
  private final CheckBox documentation = new CheckBox("Dokumentationsauszüge freigeben");
  private final CheckBox diagnostics = new CheckBox("Diagnoseübergabe an Chat erlauben");
  private final CheckBox hardware = new CheckBox("Anonymisierte Hardwaredaten freigeben");
  private final CheckBox system = new CheckBox("Systembasisdaten freigeben");
  private final CheckBox history = new CheckBox("Chatverlauf lokal speichern");
  private final Label status = new Label();

  public SettingsView(
      SettingsService service,
      InMemoryAuditLog auditLog,
      NotificationCenter notifications,
      Runnable applySettings,
      Runnable clearLiveChat) {
    this.service = service;
    this.auditLog = auditLog;
    this.notifications = notifications;
    this.applySettings = applySettings;
    this.clearLiveChat = clearLiveChat;
    setId("privacy-settings-view");
    setSpacing(10);
    setPadding(new Insets(4));
    provider.getItems().setAll("openai", "offline");
    FlowPane moduleChoices = choices(modules, moduleKeys());
    FlowPane quickChoices =
        choices(quickButtons, List.of("firefox", "file-manager", "terminal", "lock-screen"));
    Button save = new Button("Datenschutzoptionen speichern");
    save.setOnAction(ignored -> save());
    Button clearHistory = new Button("Chatverlauf löschen");
    clearHistory.setOnAction(
        ignored -> {
          service.clearHistory();
          clearLiveChat.run();
          notifications.show("Datenschutz", "Chatverlauf wurde vollständig gelöscht.");
          refreshStatus();
        });
    Button clearAudit = new Button("Audit leeren");
    clearAudit.setOnAction(
        ignored -> {
          auditLog.clear();
          refreshStatus();
        });
    Button export = new Button("Sichere Einstellungen exportieren");
    export.setOnAction(ignored -> exportSettings());
    Button importButton = new Button("Einstellungen importieren");
    importButton.setOnAction(ignored -> importSettings());
    Button delete = new Button("Lokale persönliche Daten löschen");
    delete.setOnAction(
        ignored -> {
          service.deletePersonalData();
          auditLog.clear();
          clearLiveChat.run();
          load();
          notifications.show("Datenschutz", "Einstellungen, Verlauf und Audit wurden gelöscht.");
        });
    getChildren()
        .addAll(
            new Label("Aktive Module (wirksam nach Neustart)"),
            moduleChoices,
            new Label("Schnellbuttons (wirksam nach Neustart)"),
            quickChoices,
            microphone,
            onlineAi,
            new HBox(8, new Label("KI-Anbieter"), provider),
            new HBox(8, new Label("Monatsbudget in Cent (0 deaktiviert Online-KI)"), budget),
            documentation,
            diagnostics,
            hardware,
            system,
            history,
            save,
            new HBox(8, clearHistory, clearAudit),
            new HBox(8, export, importButton),
            delete,
            status,
            new Label(
                "API-Keys liegen ausschließlich im Secret Service/KDE Wallet und sind nie Teil eines Exports."));
    load();
  }

  private void load() {
    ApplicationSettings value = service.current();
    modules.forEach((key, box) -> box.setSelected(value.enabledModules().contains(key)));
    quickButtons.forEach((key, box) -> box.setSelected(value.quickButtons().contains(key)));
    microphone.setSelected(value.microphoneEnabled());
    onlineAi.setSelected(value.onlineAiEnabled());
    provider.setValue(value.aiProvider());
    budget.setText(Integer.toString(value.monthlyBudgetCents()));
    documentation.setSelected(value.shareDocumentation());
    diagnostics.setSelected(value.shareDiagnostics());
    hardware.setSelected(value.shareHardware());
    system.setSelected(value.shareSystemContext());
    history.setSelected(value.storeChatHistory());
    refreshStatus();
  }

  private void save() {
    ApplicationSettings old = service.current();
    try {
      int budgetCents = Integer.parseInt(budget.getText().strip());
      service.update(
          new ApplicationSettings(
              selected(modules),
              List.copyOf(selected(quickButtons)),
              microphone.isSelected(),
              old.microphoneId(),
              onlineAi.isSelected(),
              provider.getValue(),
              budgetCents,
              documentation.isSelected(),
              diagnostics.isSelected(),
              hardware.isSelected(),
              system.isSelected(),
              history.isSelected()));
      notifications.show("Einstellungen", "Datenschutzoptionen sind ab sofort wirksam.");
      applySettings.run();
      refreshStatus();
    } catch (IllegalArgumentException exception) {
      notifications.show("Einstellungen", "Budget oder Auswahl ist ungültig.");
    }
  }

  private void exportSettings() {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Secret-freie Einstellungen exportieren");
    chooser.setInitialFileName("cachyos-control-center-settings.json");
    File selected = chooser.showSaveDialog(getScene().getWindow());
    if (selected != null) {
      try {
        service.exportSettings(selected.toPath());
        notifications.show("Export", "Einstellungen ohne Secrets exportiert.");
      } catch (IllegalArgumentException | IllegalStateException exception) {
        notifications.show("Export", "Exportziel wurde aus Sicherheitsgründen abgelehnt.");
      }
    }
  }

  private void importSettings() {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Einstellungen importieren");
    File selected = chooser.showOpenDialog(getScene().getWindow());
    if (selected != null) {
      try {
        service.importSettings(selected.toPath());
        load();
        notifications.show("Import", "Validierte Einstellungen wurden übernommen.");
      } catch (IllegalArgumentException exception) {
        notifications.show("Import", "Datei entspricht nicht dem sicheren Einstellungsschema.");
      }
    }
  }

  private void refreshStatus() {
    status.setText(
        "Lokaler Verlauf: "
            + service.history().size()
            + " Einträge · Audit: "
            + auditLog.events().size()
            + " Einträge");
  }

  private static FlowPane choices(Map<String, CheckBox> target, List<String> keys) {
    FlowPane pane = new FlowPane(8, 8);
    for (String key : keys) {
      CheckBox box = new CheckBox(key);
      target.put(key, box);
      pane.getChildren().add(box);
    }
    return pane;
  }

  private static Set<String> selected(Map<String, CheckBox> choices) {
    return choices.entrySet().stream()
        .filter(entry -> entry.getValue().isSelected())
        .map(Map.Entry::getKey)
        .collect(java.util.stream.Collectors.toUnmodifiableSet());
  }

  private static List<String> moduleKeys() {
    return List.of(
        "system",
        "network",
        "audio",
        "applications",
        "packages",
        "security",
        "hardware",
        "storage",
        "snapshots",
        "services",
        "processes",
        "display",
        "power",
        "boot",
        "diagnostics",
        "voice",
        "ai");
  }
}
