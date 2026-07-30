package org.cachyos.controlcenter.ui.processes;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.cachyos.controlcenter.modules.processes.ProcessEntry;
import org.cachyos.controlcenter.modules.processes.ProcessManager;
import org.cachyos.controlcenter.ui.notifications.NotificationCenter;

public final class ProcessesView extends VBox {
  private final ProcessManager manager;
  private final NotificationCenter notifications;
  private final Label summary = new Label("Prozesse werden gelesen …");
  private final ListView<ProcessEntry> processes = new ListView<>();
  private final Spinner<Integer> priority = new Spinner<>(-20, 19, 0);

  public ProcessesView(ProcessManager manager, NotificationCenter notifications) {
    this.manager = manager;
    this.notifications = notifications;
    setId("processes-view");
    setSpacing(8);
    setPadding(new Insets(4));
    processes.setId("process-list");
    processes.setCellFactory(ignored -> new ProcessCell());
    VBox.setVgrow(processes, Priority.ALWAYS);
    Button refresh = new Button("Aktualisieren");
    refresh.setOnAction(ignored -> load());
    Button terminate = new Button("Beenden (TERM)");
    terminate.setOnAction(ignored -> terminate());
    Button kill = new Button("Erzwingen (KILL)");
    kill.setOnAction(ignored -> kill());
    Button applyPriority = new Button("Priorität setzen");
    applyPriority.setOnAction(ignored -> priority());
    getChildren()
        .addAll(
            summary,
            new HBox(8, refresh, terminate, kill, new Label("Nice:"), priority, applyPriority),
            processes);
    load();
  }

  private void load() {
    manager
        .inspect()
        .whenComplete(
            (entries, error) ->
                Platform.runLater(
                    () -> {
                      if (error != null) {
                        notifications.show("Prozesse", "Prozessliste nicht verfügbar.");
                      } else {
                        processes.getItems().setAll(entries);
                        summary.setText(
                            entries.size()
                                + " Prozesse · "
                                + entries.stream().filter(ProcessEntry::critical).count()
                                + " kritisch geschützt");
                      }
                    }));
  }

  private void terminate() {
    ProcessEntry entry = selected();
    if (entry != null) {
      complete(manager.terminate(entry.pid()));
    }
  }

  private void kill() {
    ProcessEntry entry = selected();
    if (entry == null) {
      return;
    }
    TextInputDialog dialog = new TextInputDialog();
    dialog.setHeaderText("Zum Erzwingen PID " + entry.pid() + " eingeben");
    dialog
        .showAndWait()
        .ifPresent(confirmation -> complete(manager.kill(entry.pid(), confirmation)));
  }

  private void priority() {
    ProcessEntry entry = selected();
    if (entry != null) {
      complete(manager.setPriority(entry.pid(), priority.getValue()));
    }
  }

  private void complete(
      java.util.concurrent.CompletableFuture<
              org.cachyos.controlcenter.modules.processes.ProcessResult>
          result) {
    result.whenComplete(
        (value, error) ->
            Platform.runLater(
                () ->
                    notifications.show(
                        "Prozesse", error == null ? value.message() : "Aktion fehlgeschlagen.")));
  }

  private ProcessEntry selected() {
    return processes.getSelectionModel().getSelectedItem();
  }

  private static final class ProcessCell extends ListCell<ProcessEntry> {
    @Override
    protected void updateItem(ProcessEntry item, boolean empty) {
      super.updateItem(item, empty);
      setText(
          empty || item == null
              ? null
              : (item.critical() ? "GESCHÜTZT · " : "")
                  + item.pid()
                  + " · "
                  + item.command()
                  + " · "
                  + item.user()
                  + " · RAM "
                  + (item.residentBytes() / 1_048_576)
                  + " MiB · CPU "
                  + item.cpuMillis()
                  + " ms · Nice "
                  + item.priority());
    }
  }
}
