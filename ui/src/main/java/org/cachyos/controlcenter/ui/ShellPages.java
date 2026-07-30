package org.cachyos.controlcenter.ui;

import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.cachyos.controlcenter.core.action.ActionDispatcher;
import org.cachyos.controlcenter.core.audit.InMemoryAuditLog;
import org.cachyos.controlcenter.modules.applications.ApplicationManagerModule;
import org.cachyos.controlcenter.modules.audio.AudioEvents;
import org.cachyos.controlcenter.modules.audio.AudioManagerModule;
import org.cachyos.controlcenter.modules.network.NetworkEvents;
import org.cachyos.controlcenter.modules.network.NetworkManagerModule;
import org.cachyos.controlcenter.systeminfo.DashboardMonitor;
import org.cachyos.controlcenter.systeminfo.PlatformInfo;
import org.cachyos.controlcenter.systeminfo.SystemSnapshot;
import org.cachyos.controlcenter.ui.applications.ApplicationsView;
import org.cachyos.controlcenter.ui.audio.AudioView;
import org.cachyos.controlcenter.ui.dashboard.DashboardView;
import org.cachyos.controlcenter.ui.network.NetworkView;
import org.cachyos.controlcenter.ui.notifications.NotificationCenter;
import org.cachyos.controlcenter.ui.theme.ThemeManager;
import org.cachyos.controlcenter.ui.theme.ThemeMode;

/** Real Phase 1 content pages. Later manager pages are not created here. */
final class ShellPages {
  private ShellPages() {}

  static Node overview(
      DashboardMonitor dashboardMonitor,
      InMemoryAuditLog auditLog,
      ActionDispatcher actionDispatcher,
      NotificationCenter notifications) {
    return page(
        "Übersicht",
        "Lokaler Systemstatus mit lastarmer Aktualisierung und ohne erhöhte Rechte.",
        new DashboardView(dashboardMonitor, auditLog, actionDispatcher, notifications));
  }

  static Node system(PlatformInfo info, SystemSnapshot snapshot) {
    long availableCapabilities =
        snapshot.capabilities().statuses().values().stream()
            .filter(status -> status.available())
            .count();
    VBox details =
        new VBox(
            10,
            detail("Betriebssystem", snapshot.distribution().prettyName()),
            detail("CachyOS", snapshot.distribution().cachyOs() ? "Erkannt" : "Nicht erkannt"),
            detail("Kernel", snapshot.kernel()),
            detail("Architektur", info.architecture()),
            detail("Desktop", info.desktopSession()),
            detail("Sitzungstyp", info.sessionType()),
            detail("CPU", snapshot.hardware().cpuModel()),
            detail(
                "Logische Prozessoren", Integer.toString(snapshot.hardware().logicalProcessors())),
            detail("Arbeitsspeicher", formatBytes(snapshot.hardware().totalMemoryBytes())),
            detail(
                "Systemspeicher",
                formatBytes(snapshot.storage().usableBytes())
                    + " frei von "
                    + formatBytes(snapshot.storage().totalBytes())),
            detail(
                "Akku",
                snapshot.battery().present()
                    ? snapshot.battery().percentage() + " % · " + snapshot.battery().status()
                    : "Nicht verfügbar"),
            detail(
                "Netzwerk",
                snapshot.network().online()
                    ? "Verbunden · " + snapshot.network().interfaces().size() + " Schnittstellen"
                    : "Offline oder nicht verfügbar"),
            detail("Bootmanager", snapshot.bootManager().displayName()),
            detail(
                "Optionale Werkzeuge",
                availableCapabilities
                    + " von "
                    + snapshot.capabilities().statuses().size()
                    + " erkannt"));
    details.getStyleClass().add("details-panel");
    return page(
        "System",
        "Sichere Plattformbasis ohne externe Systembefehle oder erhöhte Rechte.",
        details);
  }

  static Node network(
      NetworkManagerModule manager,
      NetworkEvents events,
      ActionDispatcher dispatcher,
      NotificationCenter notifications) {
    return page(
        "Netzwerk",
        "NetworkManager-Status, gespeicherte Profile, WLANs und sichere lokale Aktionen.",
        new NetworkView(manager, events, dispatcher, notifications));
  }

  static Node audio(
      AudioManagerModule manager,
      AudioEvents events,
      ActionDispatcher dispatcher,
      NotificationCenter notifications) {
    return page(
        "Audio",
        "PipeWire-Geräte, Mikrofone, Streams und lokale Mixersteuerung ohne Audioaufnahme.",
        new AudioView(manager, events, dispatcher, notifications));
  }

  static Node applications(
      ApplicationManagerModule manager,
      ActionDispatcher dispatcher,
      NotificationCenter notifications) {
    return page(
        "Programme",
        "Sicher katalogisierte XDG-Anwendungen mit Suche, Favoriten und Paketzuordnung.",
        new ApplicationsView(manager, dispatcher, notifications));
  }

  static Node settings(ThemeManager themeManager, Consumer<ThemeMode> onChanged) {
    ComboBox<ThemeMode> selector = new ComboBox<>();
    selector.getItems().setAll(ThemeMode.values());
    selector.setValue(themeManager.mode());
    selector.setAccessibleText("Farbschema auswählen");
    selector
        .valueProperty()
        .addListener(
            (ignored, previous, selected) -> {
              if (selected != null && selected != previous) {
                themeManager.setMode(selected);
                onChanged.accept(selected);
              }
            });

    VBox themeSetting =
        new VBox(
            8,
            new Label("Farbschema"),
            selector,
            new Label(
                "„System“ berücksichtigt verfügbare Desktop-Hinweise und fällt sicher auf Hell zurück."));
    themeSetting.getStyleClass().add("settings-group");
    return page(
        "Einstellungen",
        "In Phase 1 ist ausschließlich die lokale Darstellung konfigurierbar.",
        themeSetting);
  }

  private static Node page(String title, String subtitle, Node content) {
    Label heading = new Label(title);
    heading.getStyleClass().add("page-title");
    Label description = new Label(subtitle);
    description.setWrapText(true);
    description.getStyleClass().add("page-description");
    ScrollPane scrollPane = new ScrollPane(content);
    scrollPane.setFitToWidth(true);
    scrollPane.getStyleClass().add("page-scroll");
    VBox page = new VBox(14, heading, description, new Separator(), scrollPane);
    VBox.setVgrow(scrollPane, Priority.ALWAYS);
    page.setId("page-" + title.toLowerCase(java.util.Locale.ROOT));
    page.setPadding(new Insets(26));
    page.getStyleClass().add("page");
    return page;
  }

  private static Node detail(String label, String value) {
    Label name = new Label(label);
    name.getStyleClass().add("detail-name");
    Label content = new Label(value);
    content.getStyleClass().add("detail-value");
    VBox row = new VBox(3, name, content);
    return row;
  }

  private static String formatBytes(long bytes) {
    if (bytes <= 0) {
      return "Nicht verfügbar";
    }
    double gibibytes = bytes / (1024.0 * 1024.0 * 1024.0);
    return String.format(java.util.Locale.GERMAN, "%.1f GiB", gibibytes);
  }
}
