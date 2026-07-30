package org.cachyos.controlcenter.ui;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.cachyos.controlcenter.ai.api.AiProvider;
import org.cachyos.controlcenter.ai.knowledge.KnowledgeService;
import org.cachyos.controlcenter.ai.provider.AiConfiguration;
import org.cachyos.controlcenter.core.action.ActionDispatcher;
import org.cachyos.controlcenter.core.action.ActionRequest;
import org.cachyos.controlcenter.core.action.InputSource;
import org.cachyos.controlcenter.core.audit.InMemoryAuditLog;
import org.cachyos.controlcenter.input.intent.GermanIntentRouter;
import org.cachyos.controlcenter.input.intent.IntentResult;
import org.cachyos.controlcenter.input.voice.MicrophoneCatalog;
import org.cachyos.controlcenter.input.voice.SpeechModelManager;
import org.cachyos.controlcenter.input.voice.SpeechToTextEngine;
import org.cachyos.controlcenter.modules.applications.ApplicationManagerModule;
import org.cachyos.controlcenter.modules.audio.AudioEvents;
import org.cachyos.controlcenter.modules.audio.AudioManagerModule;
import org.cachyos.controlcenter.modules.diagnostics.DiagnosticManager;
import org.cachyos.controlcenter.modules.display.DisplayManager;
import org.cachyos.controlcenter.modules.hardware.HardwareManager;
import org.cachyos.controlcenter.modules.network.NetworkEvents;
import org.cachyos.controlcenter.modules.network.NetworkManagerModule;
import org.cachyos.controlcenter.modules.packages.PackageManager;
import org.cachyos.controlcenter.modules.power.PowerManager;
import org.cachyos.controlcenter.modules.processes.ProcessManager;
import org.cachyos.controlcenter.modules.security.SecurityManager;
import org.cachyos.controlcenter.modules.services.ServiceManager;
import org.cachyos.controlcenter.modules.snapshots.SnapshotManager;
import org.cachyos.controlcenter.modules.storage.StorageManager;
import org.cachyos.controlcenter.systeminfo.DashboardMonitor;
import org.cachyos.controlcenter.systeminfo.PlatformInfo;
import org.cachyos.controlcenter.systeminfo.SystemSnapshot;
import org.cachyos.controlcenter.ui.ai.ChatView;
import org.cachyos.controlcenter.ui.navigation.ContentRouter;
import org.cachyos.controlcenter.ui.navigation.NavigationCatalog;
import org.cachyos.controlcenter.ui.navigation.NavigationEntry;
import org.cachyos.controlcenter.ui.navigation.NavigationId;
import org.cachyos.controlcenter.ui.notifications.NotificationCenter;
import org.cachyos.controlcenter.ui.theme.ThemeManager;

/** Responsive, keyboard-accessible window chrome shared by all later manager pages. */
final class ApplicationShell {
  private static final double COMPACT_WIDTH = 800;

  private final BorderPane frame = new BorderPane();
  private final StackPane root = new StackPane();
  private final VBox sidebar;
  private final ListView<NavigationEntry> navigation = new ListView<>();
  private final Button menuButton = new Button("☰");
  private final TextField commandField = new TextField();
  private final ContentRouter router;
  private final NotificationCenter notifications;
  private final GermanIntentRouter intentRouter;
  private final ActionDispatcher actionDispatcher;
  private ChatView chatView;
  private boolean compact;
  private boolean compactSidebarOpen;

  ApplicationShell(
      PlatformInfo platformInfo,
      SystemSnapshot systemSnapshot,
      DashboardMonitor dashboardMonitor,
      InMemoryAuditLog auditLog,
      NetworkManagerModule networkManager,
      NetworkEvents networkEvents,
      AudioManagerModule audioManager,
      AudioEvents audioEvents,
      ApplicationManagerModule applicationManager,
      DiagnosticManager diagnosticManager,
      PackageManager packageManager,
      SecurityManager securityManager,
      HardwareManager hardwareManager,
      StorageManager storageManager,
      SnapshotManager snapshotManager,
      ServiceManager serviceManager,
      ProcessManager processManager,
      DisplayManager displayManager,
      PowerManager powerManager,
      GermanIntentRouter intentRouter,
      MicrophoneCatalog microphoneCatalog,
      SpeechModelManager speechModelManager,
      SpeechToTextEngine speechToTextEngine,
      AiProvider aiProvider,
      AiConfiguration aiConfiguration,
      KnowledgeService knowledgeService,
      NavigationCatalog catalog,
      ThemeManager themeManager,
      NotificationCenter notifications,
      ActionDispatcher actionDispatcher) {
    this.notifications = notifications;
    this.intentRouter = intentRouter;
    this.actionDispatcher = actionDispatcher;
    router =
        new ContentRouter(
            createPages(
                platformInfo,
                systemSnapshot,
                dashboardMonitor,
                auditLog,
                networkManager,
                networkEvents,
                audioManager,
                audioEvents,
                applicationManager,
                diagnosticManager,
                packageManager,
                securityManager,
                hardwareManager,
                storageManager,
                snapshotManager,
                serviceManager,
                processManager,
                displayManager,
                powerManager,
                microphoneCatalog,
                speechModelManager,
                speechToTextEngine,
                aiProvider,
                aiConfiguration,
                knowledgeService,
                themeManager,
                notifications,
                actionDispatcher));
    sidebar = createSidebar(catalog);

    frame.getStyleClass().add("application-shell");
    frame.setTop(createTopBar());
    frame.setLeft(sidebar);
    frame.setCenter(router.view());
    frame.setBottom(createBottomArea());

    notifications.attach(root);
    root.getChildren().addFirst(frame);

    navigation
        .getSelectionModel()
        .selectedItemProperty()
        .addListener((ignored, previous, selected) -> navigate(selected, previous));
    navigation.getSelectionModel().selectFirst();

    ChangeListener<Number> responsiveListener =
        (ignored, oldWidth, newWidth) -> updateResponsiveState(newWidth.doubleValue());
    root.widthProperty().addListener(responsiveListener);
  }

  Parent root() {
    return root;
  }

  void installKeyboardNavigation(Scene scene) {
    scene
        .getAccelerators()
        .put(
            new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.ALT_DOWN),
            () -> select(NavigationId.OVERVIEW));
    scene
        .getAccelerators()
        .put(
            new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.ALT_DOWN),
            () -> select(NavigationId.SYSTEM));
    scene
        .getAccelerators()
        .put(
            new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.ALT_DOWN),
            () -> select(NavigationId.SETTINGS));
    scene
        .getAccelerators()
        .put(
            new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN),
            commandField::requestFocus);
  }

  private VBox createSidebar(NavigationCatalog catalog) {
    navigation.getItems().setAll(catalog.entries());
    navigation.setId("primary-navigation");
    navigation.setCellFactory(ignored -> new NavigationCell());
    navigation.setPrefWidth(220);
    navigation.setMinWidth(200);
    navigation.setMaxWidth(240);
    navigation.getStyleClass().add("navigation-list");
    VBox.setVgrow(navigation, Priority.ALWAYS);

    Label heading = new Label("Bereiche");
    heading.getStyleClass().add("sidebar-heading");
    VBox result = new VBox(8, heading, navigation);
    result.setPadding(new Insets(16, 8, 12, 12));
    result.getStyleClass().add("sidebar");
    return result;
  }

  private Node createTopBar() {
    menuButton.getStyleClass().add("menu-button");
    menuButton.setAccessibleText("Navigation öffnen oder schließen");
    menuButton.setTooltip(new Tooltip("Navigation öffnen (Alt+1 bis Alt+3 für Schnellzugriff)"));
    menuButton.setOnAction(
        ignored -> {
          compactSidebarOpen = !compactSidebarOpen;
          applySidebarVisibility();
        });
    menuButton.setVisible(false);
    menuButton.setManaged(false);

    Label title = new Label("CachyOS Control Center");
    title.getStyleClass().add("app-title");
    Label phase = new Label("Entwicklungsstand · Phase 19");
    phase.getStyleClass().add("phase-badge");

    Label statusDot = new Label("●");
    statusDot.getStyleClass().add("status-ok");
    Label status = new Label("Lokal bereit");
    status.setTooltip(new Tooltip("Die Oberfläche läuft ohne erhöhte Rechte."));

    HBox spacer = new HBox();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox topBar = new HBox(10, menuButton, title, phase, spacer, statusDot, status);
    topBar.setAlignment(Pos.CENTER_LEFT);
    topBar.setPadding(new Insets(12, 18, 12, 18));
    topBar.getStyleClass().add("topbar");
    return topBar;
  }

  private Node createBottomArea() {
    Label status = new Label("Keine Hintergrundaufgabe aktiv");
    status.getStyleClass().add("muted-label");
    Label privilege = new Label("Standardbenutzer");
    privilege.getStyleClass().add("privacy-badge");
    HBox statusLine = new HBox(10, status, new HBox(), privilege);
    HBox.setHgrow(statusLine.getChildren().get(1), Priority.ALWAYS);
    statusLine.setAlignment(Pos.CENTER_LEFT);
    statusLine.getStyleClass().add("statusbar");
    statusLine.setPadding(new Insets(5, 16, 5, 16));

    commandField.setId("command-field");
    commandField.setPromptText("Lokaler Befehl, Navigation oder Frage …");
    commandField.setAccessibleHelp(
        "Eingaben werden zuerst vollständig lokal klassifiziert. Unklares wird nicht ausgeführt.");
    Button send = new Button("→");
    send.setId("command-submit");
    send.setAccessibleText("Eingabe senden");
    send.disableProperty().bind(commandField.textProperty().isEmpty());
    send.setOnAction(ignored -> submit(commandField.getText(), InputSource.TEXT));
    commandField.setOnAction(ignored -> submit(commandField.getText(), InputSource.TEXT));
    HBox inputBar = new HBox(8, commandField, send);
    HBox.setHgrow(commandField, Priority.ALWAYS);
    inputBar.setPadding(new Insets(8, 16, 12, 16));
    inputBar.getStyleClass().add("inputbar");

    return new VBox(statusLine, inputBar);
  }

  private Map<NavigationId, Node> createPages(
      PlatformInfo platformInfo,
      SystemSnapshot systemSnapshot,
      DashboardMonitor dashboardMonitor,
      InMemoryAuditLog auditLog,
      NetworkManagerModule networkManager,
      NetworkEvents networkEvents,
      AudioManagerModule audioManager,
      AudioEvents audioEvents,
      ApplicationManagerModule applicationManager,
      DiagnosticManager diagnosticManager,
      PackageManager packageManager,
      SecurityManager securityManager,
      HardwareManager hardwareManager,
      StorageManager storageManager,
      SnapshotManager snapshotManager,
      ServiceManager serviceManager,
      ProcessManager processManager,
      DisplayManager displayManager,
      PowerManager powerManager,
      MicrophoneCatalog microphoneCatalog,
      SpeechModelManager speechModelManager,
      SpeechToTextEngine speechToTextEngine,
      AiProvider aiProvider,
      AiConfiguration aiConfiguration,
      KnowledgeService knowledgeService,
      ThemeManager themeManager,
      NotificationCenter notificationCenter,
      ActionDispatcher actionDispatcher) {
    Map<NavigationId, Node> pages = new EnumMap<>(NavigationId.class);
    pages.put(
        NavigationId.OVERVIEW,
        ShellPages.overview(dashboardMonitor, auditLog, actionDispatcher, notificationCenter));
    pages.put(NavigationId.SYSTEM, ShellPages.system(platformInfo, systemSnapshot));
    pages.put(
        NavigationId.NETWORK,
        ShellPages.network(networkManager, networkEvents, actionDispatcher, notificationCenter));
    pages.put(
        NavigationId.AUDIO,
        ShellPages.audio(audioManager, audioEvents, actionDispatcher, notificationCenter));
    pages.put(
        NavigationId.APPLICATIONS,
        ShellPages.applications(applicationManager, actionDispatcher, notificationCenter));
    pages.put(NavigationId.PACKAGES, ShellPages.packages(packageManager, notificationCenter));
    pages.put(NavigationId.SECURITY, ShellPages.security(securityManager, notificationCenter));
    pages.put(NavigationId.HARDWARE, ShellPages.hardware(hardwareManager, notificationCenter));
    pages.put(NavigationId.STORAGE, ShellPages.storage(storageManager, notificationCenter));
    pages.put(NavigationId.SNAPSHOTS, ShellPages.snapshots(snapshotManager, notificationCenter));
    pages.put(NavigationId.SERVICES, ShellPages.services(serviceManager, notificationCenter));
    pages.put(NavigationId.PROCESSES, ShellPages.processes(processManager, notificationCenter));
    pages.put(NavigationId.DISPLAY, ShellPages.display(displayManager, notificationCenter));
    pages.put(NavigationId.POWER, ShellPages.power(powerManager, notificationCenter));
    pages.put(
        NavigationId.VOICE,
        ShellPages.voice(microphoneCatalog, speechModelManager, speechToTextEngine, this::submit));
    chatView = ShellPages.createChat(aiProvider, aiConfiguration, knowledgeService);
    pages.put(NavigationId.AI_ASSISTANT, ShellPages.chat(chatView));
    pages.put(
        NavigationId.DIAGNOSTICS,
        ShellPages.diagnostics(
            diagnosticManager,
            actionDispatcher,
            notificationCenter,
            report -> {
              chatView.setDraft(report);
              select(NavigationId.AI_ASSISTANT);
            }));
    pages.put(
        NavigationId.SETTINGS,
        ShellPages.settings(
            themeManager,
            mode ->
                notificationCenter.show(
                    "Darstellung", "Theme auf „" + mode.displayName() + "“ gesetzt.")));
    return pages;
  }

  private void submit(String text, InputSource source) {
    IntentResult result = intentRouter.route(text);
    switch (result.kind()) {
      case ACTION -> handleAction(result, source);
      case NAVIGATION -> handleNavigation(result);
      case QUESTION -> {
        chatView.setDraft(text);
        select(NavigationId.AI_ASSISTANT);
        notifications.show(
            "Frage erkannt",
            "Die Frage wurde nur als Entwurf übernommen. Senden bleibt eine bewusste Online-Aktion.");
      }
      case AMBIGUOUS -> notifications.show("Mehrdeutige Eingabe", result.message());
      case UNKNOWN -> notifications.show("Nicht erkannt", result.message());
      default -> notifications.show("Eingabe", "Die Eingabe wurde sicher verworfen.");
    }
  }

  private void handleAction(IntentResult result, InputSource source) {
    if (result.confirmationRequired() && !confirm(result.message())) {
      notifications.show("Abgebrochen", "Die lokale Aktion wurde nicht ausgeführt.");
      return;
    }
    ActionRequest request =
        new ActionRequest(
            result.actionId().orElseThrow(), source, result.parameters(), Instant.now());
    actionDispatcher
        .dispatch(request)
        .whenComplete(
            (actionResult, error) ->
                Platform.runLater(
                    () -> {
                      if (error != null) {
                        notifications.show(
                            "Aktion fehlgeschlagen", "Die Aktion konnte nicht ausgeführt werden.");
                      } else {
                        notifications.show("Lokale Aktion", actionResult.userMessage());
                      }
                    }));
  }

  private void handleNavigation(IntentResult result) {
    try {
      select(
          NavigationId.valueOf(
              result.navigationTarget().orElseThrow().toUpperCase(java.util.Locale.ROOT)));
    } catch (IllegalArgumentException ignored) {
      notifications.show("Navigation", "Der erkannte Bereich ist nicht registriert.");
    }
  }

  private boolean confirm(String description) {
    Alert dialog =
        new Alert(
            Alert.AlertType.CONFIRMATION,
            description + "\n\nDiese Aktion verändert den aktuellen Sitzungszustand.",
            ButtonType.CANCEL,
            ButtonType.OK);
    dialog.setTitle("Lokale Aktion bestätigen");
    dialog.setHeaderText("Sicherheitsbestätigung");
    return dialog.showAndWait().filter(ButtonType.OK::equals).isPresent();
  }

  private void navigate(NavigationEntry selected, NavigationEntry previous) {
    if (selected == null) {
      return;
    }
    if (!selected.enabled()) {
      notifications.show(
          selected.label(),
          "Dieser Bereich wird in " + selected.availability() + " freigeschaltet.");
      if (previous != null) {
        navigation.getSelectionModel().select(previous);
      }
      return;
    }
    router.navigate(selected.id());
    if (compact) {
      compactSidebarOpen = false;
      applySidebarVisibility();
    }
  }

  private void select(NavigationId id) {
    navigation.getItems().stream()
        .filter(entry -> entry.id() == id)
        .findFirst()
        .ifPresent(navigation.getSelectionModel()::select);
  }

  private void updateResponsiveState(double width) {
    compact = width > 0 && width < COMPACT_WIDTH;
    if (!compact) {
      compactSidebarOpen = false;
    }
    menuButton.setManaged(compact);
    menuButton.setVisible(compact);
    applySidebarVisibility();
  }

  private void applySidebarVisibility() {
    boolean show = !compact || compactSidebarOpen;
    sidebar.setManaged(show);
    sidebar.setVisible(show);
  }

  private static final class NavigationCell extends ListCell<NavigationEntry> {
    @Override
    protected void updateItem(NavigationEntry item, boolean empty) {
      super.updateItem(item, empty);
      if (empty || item == null) {
        setText(null);
        setTooltip(null);
        setDisable(false);
        return;
      }
      setText(item.label());
      setAccessibleText(item.label());
      setDisable(!item.enabled());
      setTooltip(new Tooltip(item.description() + " · " + item.availability()));
    }
  }
}
