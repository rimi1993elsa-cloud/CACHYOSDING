package org.cachyos.controlcenter.ui;

import java.util.EnumMap;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
import org.cachyos.controlcenter.core.action.ActionDispatcher;
import org.cachyos.controlcenter.systeminfo.PlatformInfo;
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
  private boolean compact;
  private boolean compactSidebarOpen;

  ApplicationShell(
      PlatformInfo platformInfo,
      NavigationCatalog catalog,
      ThemeManager themeManager,
      NotificationCenter notifications,
      ActionDispatcher actionDispatcher) {
    this.notifications = notifications;
    router =
        new ContentRouter(createPages(platformInfo, themeManager, notifications, actionDispatcher));
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
    Label phase = new Label("Entwicklungsstand · Phase 2");
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

    commandField.setPromptText("Text- und Sprachsteuerung wird in Phase 8/9 aktiviert");
    commandField.setDisable(true);
    commandField.setAccessibleHelp(
        "Text- und Sprachsteuerung ist in diesem Entwicklungsstand noch nicht verfügbar.");
    Button send = new Button("→");
    send.setDisable(true);
    send.setAccessibleText("Eingabe senden");
    HBox inputBar = new HBox(8, commandField, send);
    HBox.setHgrow(commandField, Priority.ALWAYS);
    inputBar.setPadding(new Insets(8, 16, 12, 16));
    inputBar.getStyleClass().add("inputbar");

    return new VBox(statusLine, inputBar);
  }

  private Map<NavigationId, Node> createPages(
      PlatformInfo platformInfo,
      ThemeManager themeManager,
      NotificationCenter notificationCenter,
      ActionDispatcher actionDispatcher) {
    Map<NavigationId, Node> pages = new EnumMap<>(NavigationId.class);
    pages.put(
        NavigationId.OVERVIEW,
        ShellPages.overview(platformInfo, actionDispatcher, notificationCenter));
    pages.put(NavigationId.SYSTEM, ShellPages.system(platformInfo));
    pages.put(
        NavigationId.SETTINGS,
        ShellPages.settings(
            themeManager,
            mode ->
                notificationCenter.show(
                    "Darstellung", "Theme auf „" + mode.displayName() + "“ gesetzt.")));
    return pages;
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
