package org.cachyos.controlcenter.ui.settings;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.cachyos.controlcenter.ai.provider.SecretOperationResult;
import org.cachyos.controlcenter.ai.provider.SecretStore;
import org.cachyos.controlcenter.core.audit.AuditLog;
import org.cachyos.controlcenter.persistence.ApplicationSettings;
import org.cachyos.controlcenter.persistence.SettingsService;
import org.cachyos.controlcenter.systeminfo.SystemSnapshot;
import org.cachyos.controlcenter.ui.notifications.NotificationCenter;

public final class SettingsView extends VBox {
  private static final String OPENAI_LABEL = "OpenAI (online)";
  private static final String OFFLINE_LABEL = "Nur lokal (keine Online-KI)";
  private final SettingsService service;
  private final AuditLog auditLog;
  private final SecretStore secretStore;
  private final SystemSnapshot systemSnapshot;
  private final NotificationCenter notifications;
  private final Runnable clearLiveChat;
  private final Runnable applySettings;
  private final Map<String, CheckBox> modules = new LinkedHashMap<>();
  private final Map<String, CheckBox> quickButtons = new LinkedHashMap<>();
  private final CheckBox microphone = new CheckBox("Mikrofon/Push-to-Talk erlauben");
  private final CheckBox onlineAi = new CheckBox("Online-KI erlauben");
  private final ComboBox<String> provider = new ComboBox<>();
  private final ComboBox<AiModelChoice> model = new ComboBox<>();
  private final Label modelDescription = new Label();
  private final TextField budget = new TextField();
  private final CheckBox documentation = new CheckBox("Dokumentationsauszüge freigeben");
  private final CheckBox diagnostics = new CheckBox("Diagnoseübergabe an Chat erlauben");
  private final CheckBox hardware = new CheckBox("Anonymisierte Hardwaredaten freigeben");
  private final CheckBox system = new CheckBox("Systembasisdaten freigeben");
  private final CheckBox history = new CheckBox("Chatverlauf lokal speichern");
  private final Label status = new Label();
  private final Label secretStatus = new Label();
  private final PasswordField apiKey = new PasswordField();
  private final Label systemCheck = new Label();

  public SettingsView(
      SettingsService service,
      AuditLog auditLog,
      SecretStore secretStore,
      SystemSnapshot systemSnapshot,
      NotificationCenter notifications,
      Runnable applySettings,
      Runnable clearLiveChat) {
    this.service = service;
    this.auditLog = auditLog;
    this.secretStore = secretStore;
    this.systemSnapshot = systemSnapshot;
    this.notifications = notifications;
    this.applySettings = applySettings;
    this.clearLiveChat = clearLiveChat;
    setId("privacy-settings-view");
    provider.setId("ai-provider");
    model.setId("ai-model");
    setSpacing(10);
    setPadding(new Insets(4));
    provider.getItems().setAll(OPENAI_LABEL, OFFLINE_LABEL);
    model
        .getItems()
        .setAll(
            new AiModelChoice(
                "gpt-5.6-sol",
                "Beste Qualität",
                "Für schwierige Analysen und komplexe technische Fragen."),
            new AiModelChoice(
                "gpt-5.6-terra",
                "Ausgewogen",
                "Gute Qualität bei geringerem Preis – empfohlen für den Alltag."),
            new AiModelChoice(
                "gpt-5.6-luna",
                "Sparsam & schnell",
                "Für kurze, häufige Fragen mit möglichst niedrigen Kosten."));
    provider.setOnAction(ignored -> refreshAiControls());
    onlineAi.setOnAction(ignored -> refreshAiControls());
    model.setOnAction(ignored -> refreshAiControls());
    modelDescription.setWrapText(true);
    modelDescription.getStyleClass().add("muted-label");
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
    Button clearUsage = new Button("KI-Verbrauch zurücksetzen");
    clearUsage.setOnAction(
        ignored -> {
          service.clearAiUsage();
          notifications.show("KI-Verbrauch", "Die lokale Verbrauchsstatistik wurde gelöscht.");
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
    apiKey.setPromptText("OpenAI API-Schlüssel");
    apiKey.setAccessibleText("OpenAI API-Schlüssel sicher in KWallet speichern");
    Button storeKey = new Button("API-Schlüssel sicher speichern");
    storeKey.setId("api-key-store");
    storeKey.setOnAction(ignored -> storeApiKey());
    Button deleteKey = new Button("API-Schlüssel löschen");
    deleteKey.setId("api-key-delete");
    deleteKey.setOnAction(ignored -> deleteApiKey());
    Button runSystemCheck = new Button("Linux-Systemcheck aktualisieren");
    runSystemCheck.setId("system-check-refresh");
    runSystemCheck.setOnAction(ignored -> refreshSystemCheck());
    secretStatus.getStyleClass().add("muted-label");
    secretStatus.setId("api-key-status");
    systemCheck.setWrapText(true);
    systemCheck.setId("system-check-result");
    systemCheck.getStyleClass().add("muted-label");
    getChildren()
        .addAll(
            new Label("Aktive Module (wirksam nach Neustart)"),
            moduleChoices,
            new Label("Schnellbuttons (wirksam nach Neustart)"),
            quickChoices,
            microphone,
            onlineAi,
            new Label("OpenAI-Zugang"),
            apiKey,
            new HBox(8, storeKey, deleteKey),
            secretStatus,
            new HBox(8, new Label("KI-Anbieter"), provider),
            new HBox(8, new Label("Modellprofil"), model),
            modelDescription,
            new HBox(8, new Label("Monatslimit in USD-Cent (0 deaktiviert Online-KI)"), budget),
            documentation,
            diagnostics,
            hardware,
            system,
            history,
            save,
            new HBox(8, clearHistory, clearAudit, clearUsage),
            new HBox(8, export, importButton),
            delete,
            status,
            new Label("Installations- und Systemcheck"),
            runSystemCheck,
            systemCheck,
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
    provider.setValue("openai".equals(value.aiProvider()) ? OPENAI_LABEL : OFFLINE_LABEL);
    model.getItems().stream()
        .filter(choice -> choice.modelId().equals(value.aiModel()))
        .findFirst()
        .ifPresent(model::setValue);
    budget.setText(Integer.toString(value.monthlyBudgetCents()));
    documentation.setSelected(value.shareDocumentation());
    diagnostics.setSelected(value.shareDiagnostics());
    hardware.setSelected(value.shareHardware());
    system.setSelected(value.shareSystemContext());
    history.setSelected(value.storeChatHistory());
    refreshAiControls();
    refreshSecretStatus();
    refreshSystemCheck();
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
              OPENAI_LABEL.equals(provider.getValue()) ? "openai" : "offline",
              model.getValue().modelId(),
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
            + " Einträge · geschätzte KI-Kosten diesen Monat: "
            + String.format(
                java.util.Locale.GERMAN, "%.4f USD", service.currentMonthUsage().estimatedUsd()));
  }

  private void storeApiKey() {
    char[] value = apiKey.getText().toCharArray();
    apiKey.clear();
    secretStatus.setText("KWallet/Secret Service wird geöffnet …");
    CompletableFuture.supplyAsync(() -> secretStore.storeSecret("openai-api-key", value))
        .thenAccept(result -> Platform.runLater(() -> applySecretResult(result)));
  }

  private void deleteApiKey() {
    secretStatus.setText("API-Schlüssel wird gelöscht …");
    CompletableFuture.supplyAsync(() -> secretStore.deleteSecret("openai-api-key"))
        .thenAccept(result -> Platform.runLater(() -> applySecretResult(result)));
  }

  private void applySecretResult(SecretOperationResult result) {
    secretStatus.setText(result.message());
    notifications.show(
        result.success() ? "OpenAI-Zugang" : "OpenAI-Zugang nicht geändert", result.message());
    applySettings.run();
    refreshSystemCheck();
  }

  private void refreshSecretStatus() {
    CompletableFuture.supplyAsync(() -> secretStore.containsSecret("openai-api-key"))
        .thenAccept(
            present ->
                Platform.runLater(
                    () ->
                        secretStatus.setText(
                            present
                                ? "API-Schlüssel ist sicher in KWallet/Secret Service hinterlegt."
                                : "Noch kein API-Schlüssel in KWallet/Secret Service gefunden.")));
  }

  private void refreshSystemCheck() {
    long available =
        systemSnapshot.capabilities().statuses().values().stream()
            .filter(capability -> capability.available())
            .count();
    long total = systemSnapshot.capabilities().statuses().size();
    String missing =
        systemSnapshot.capabilities().statuses().values().stream()
            .filter(capability -> !capability.available())
            .limit(8)
            .map(capability -> capability.capability().displayName() + " → " + capability.reason())
            .collect(java.util.stream.Collectors.joining("\n"));
    String distribution =
        systemSnapshot.distribution().cachyOs()
            ? "CachyOS erkannt"
            : systemSnapshot.distribution().prettyName();
    systemCheck.setText(
        distribution
            + " · "
            + available
            + "/"
            + total
            + " optionale Werkzeuge verfügbar\n"
            + "Datenbank: "
            + service.databaseFile()
            + "\n"
            + (missing.isBlank()
                ? "Alle optionalen Werkzeuge erkannt."
                : "Optional fehlend:\n" + missing)
            + "\nFehlende Werkzeuge deaktivieren nur die jeweils betroffene Funktion. "
            + "Vollständiger Abnahmetest: scripts/verify-linux.sh");
  }

  private void refreshAiControls() {
    boolean openAi = onlineAi.isSelected() && OPENAI_LABEL.equals(provider.getValue());
    model.setDisable(!openAi);
    AiModelChoice choice = model.getValue();
    modelDescription.setText(
        choice == null
            ? "Wähle ein verständliches Modellprofil."
            : choice.description() + " Technische ID: " + choice.modelId());
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

  private record AiModelChoice(String modelId, String label, String description) {
    @Override
    public String toString() {
      return label;
    }
  }
}
