package org.cachyos.controlcenter.ui.notifications;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** Non-blocking toast notifications owned by the UI layer. */
public final class NotificationCenter {
  private final VBox toast = new VBox(3);
  private final Label title = new Label();
  private final Label message = new Label();
  private final PauseTransition hideDelay = new PauseTransition(Duration.seconds(4));

  public NotificationCenter() {
    title.getStyleClass().add("toast-title");
    message.getStyleClass().add("toast-message");
    message.setWrapText(true);
    toast.getChildren().addAll(title, message);
    toast.getStyleClass().add("toast");
    toast.setPadding(new Insets(12));
    toast.setMaxWidth(360);
    toast.setVisible(false);
    toast.setManaged(false);
    hideDelay.setOnFinished(ignored -> hide());
  }

  public void attach(StackPane host) {
    StackPane.setAlignment(toast, Pos.TOP_RIGHT);
    StackPane.setMargin(toast, new Insets(70, 18, 0, 0));
    host.getChildren().add(toast);
  }

  public void show(String heading, String body) {
    title.setText(heading);
    message.setText(body);
    toast.setManaged(true);
    toast.setVisible(true);
    toast.toFront();
    hideDelay.playFromStart();
  }

  private void hide() {
    toast.setVisible(false);
    toast.setManaged(false);
  }
}
