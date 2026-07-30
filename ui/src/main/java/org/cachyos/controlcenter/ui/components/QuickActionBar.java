package org.cachyos.controlcenter.ui.components;

import java.util.Objects;
import java.util.Set;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import org.cachyos.controlcenter.core.action.ActionDispatcher;
import org.cachyos.controlcenter.core.action.ActionId;
import org.cachyos.controlcenter.core.action.ActionRequest;
import org.cachyos.controlcenter.ui.notifications.NotificationCenter;

/** Direct buttons whose fixed ActionIds never derive from translated labels. */
public final class QuickActionBar extends FlowPane {
  private final ActionDispatcher dispatcher;
  private final NotificationCenter notifications;

  public QuickActionBar(
      ActionDispatcher dispatcher, NotificationCenter notifications, Set<String> enabledButtons) {
    this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher");
    this.notifications = Objects.requireNonNull(notifications, "notifications");
    setHgap(10);
    setVgap(10);
    addIfEnabled(
        enabledButtons, "firefox", "Firefox öffnen", "action-open-firefox", ActionId.OPEN_FIREFOX);
    addIfEnabled(
        enabledButtons,
        "file-manager",
        "Dateimanager öffnen",
        "action-open-file-manager",
        ActionId.OPEN_FILE_MANAGER);
    addIfEnabled(
        enabledButtons,
        "terminal",
        "Terminal öffnen",
        "action-open-terminal",
        ActionId.OPEN_TERMINAL);
    addIfEnabled(
        enabledButtons,
        "lock-screen",
        "Bildschirm sperren",
        "action-lock-screen",
        ActionId.LOCK_SCREEN);
  }

  private void addIfEnabled(
      Set<String> enabled, String setting, String label, String id, ActionId actionId) {
    if (enabled.contains(setting)) {
      getChildren().add(button(label, id, actionId));
    }
  }

  private Button button(String label, String id, ActionId actionId) {
    Button button = new Button(label);
    button.setId(id);
    button.setAccessibleText(label);
    button.setTooltip(
        new Tooltip("Führt ausschließlich die registrierte Aktion „" + actionId + "“ aus."));
    if (actionId.equals(ActionId.LOCK_SCREEN)) {
      button.getStyleClass().add("lock-button");
    }
    button.setOnAction(ignored -> dispatch(button, actionId));
    return button;
  }

  private void dispatch(Button button, ActionId actionId) {
    button.setDisable(true);
    dispatcher
        .dispatch(ActionRequest.fromButton(actionId))
        .whenComplete(
            (result, throwable) ->
                Platform.runLater(
                    () -> {
                      button.setDisable(false);
                      if (throwable != null) {
                        notifications.show(
                            "Aktion fehlgeschlagen",
                            "Die lokale Aktion konnte nicht abgeschlossen werden.");
                      } else {
                        notifications.show("Schnellaktion", result.userMessage());
                      }
                    }));
  }
}
