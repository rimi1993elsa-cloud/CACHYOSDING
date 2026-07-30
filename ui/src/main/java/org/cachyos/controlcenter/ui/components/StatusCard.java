package org.cachyos.controlcenter.ui.components;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** Reusable summary card with an explicit title, value, and context. */
public final class StatusCard extends VBox {
  public StatusCard(String title, String value, String detail) {
    Label titleLabel = new Label(display(title));
    titleLabel.getStyleClass().add("card-title");
    Label valueLabel = new Label(display(value));
    valueLabel.getStyleClass().add("card-value");
    Label detailLabel = new Label(display(detail));
    detailLabel.setWrapText(true);
    detailLabel.getStyleClass().add("card-detail");

    setSpacing(7);
    setPadding(new Insets(16));
    setPrefWidth(230);
    setMinHeight(130);
    getStyleClass().add("status-card");
    getChildren().addAll(titleLabel, valueLabel, detailLabel);
  }

  private static String display(String value) {
    return value == null || value.isBlank() || "unbekannt".equalsIgnoreCase(value)
        ? "Nicht verfügbar"
        : value;
  }
}
