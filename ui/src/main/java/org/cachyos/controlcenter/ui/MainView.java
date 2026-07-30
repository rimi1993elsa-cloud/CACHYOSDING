package org.cachyos.controlcenter.ui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.cachyos.controlcenter.systeminfo.PlatformInfo;

/** Minimal, honest Phase 0 window. Navigation belongs to Phase 1. */
public final class MainView {
  private final BorderPane root;

  public MainView(PlatformInfo platformInfo) {
    Label title = new Label("CachyOS Control Center AI");
    title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

    Label phase = new Label("Phase 0 – Architektur und Projektbasis");
    Label platform =
        new Label(
            "Erkannte Plattform: "
                + platformInfo.operatingSystemFamily().displayName()
                + " · "
                + platformInfo.architecture());
    Label notice =
        new Label(
            "Systemaktionen und Verwaltungsfunktionen werden erst in den folgenden Phasen freigeschaltet.");
    notice.setWrapText(true);

    VBox content = new VBox(12, title, phase, platform, notice);
    content.setPadding(new Insets(32));
    root = new BorderPane(content);
  }

  public Parent root() {
    return root;
  }
}
