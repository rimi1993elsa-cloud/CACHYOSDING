package org.cachyos.controlcenter.ui;

import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.cachyos.controlcenter.systeminfo.PlatformInfo;
import org.cachyos.controlcenter.ui.components.StatusCard;
import org.cachyos.controlcenter.ui.theme.ThemeManager;
import org.cachyos.controlcenter.ui.theme.ThemeMode;

/** Real Phase 1 content pages. Later manager pages are not created here. */
final class ShellPages {
  private ShellPages() {}

  static Node overview(PlatformInfo platformInfo) {
    FlowPane cards = new FlowPane(14, 14);
    cards
        .getChildren()
        .addAll(
            new StatusCard(
                "Plattform",
                platformInfo.operatingSystemFamily().displayName(),
                platformInfo.operatingSystemName()),
            new StatusCard("Sitzung", platformInfo.sessionType(), platformInfo.desktopSession()),
            new StatusCard(
                "Sicherheitsmodus", "Unprivilegiert", "Keine Systemaktionen in Phase 1"));
    return page(
        "Übersicht",
        "Die Grundoberfläche ist bereit. Angezeigte Werte stammen aus der lokalen Laufzeit.",
        cards);
  }

  static Node system(PlatformInfo info) {
    VBox details =
        new VBox(
            10,
            detail("Betriebssystem", info.operatingSystemName()),
            detail("Version", info.operatingSystemVersion()),
            detail("Architektur", info.architecture()),
            detail("Desktop", info.desktopSession()),
            detail("Sitzungstyp", info.sessionType()));
    details.getStyleClass().add("details-panel");
    return page(
        "System",
        "Sichere Plattformbasis ohne externe Systembefehle oder erhöhte Rechte.",
        details);
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
    VBox page = new VBox(14, heading, description, new Separator(), content);
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
}
