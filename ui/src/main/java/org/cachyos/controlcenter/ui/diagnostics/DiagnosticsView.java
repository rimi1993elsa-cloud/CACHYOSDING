package org.cachyos.controlcenter.ui.diagnostics;

import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.cachyos.controlcenter.core.action.ActionDispatcher;
import org.cachyos.controlcenter.core.action.ActionRequest;
import org.cachyos.controlcenter.modules.diagnostics.DiagnosticFinding;
import org.cachyos.controlcenter.modules.diagnostics.DiagnosticManager;
import org.cachyos.controlcenter.modules.diagnostics.DiagnosticReport;
import org.cachyos.controlcenter.ui.notifications.NotificationCenter;

/** Local diagnostics with explicit fixed remediation buttons and opt-in AI draft handoff. */
public final class DiagnosticsView extends VBox {
  private final DiagnosticManager manager;
  private final ActionDispatcher dispatcher;
  private final NotificationCenter notifications;
  private final Consumer<String> explain;
  private final VBox findings = new VBox(10);
  private final Button run = new Button("Lokale Diagnose starten");
  private final Button explainButton = new Button("Bereinigten Bericht im KI-Chat erklären");
  private final Label status = new Label("Noch keine Diagnose ausgeführt.");
  private DiagnosticReport report;

  public DiagnosticsView(
      DiagnosticManager manager,
      ActionDispatcher dispatcher,
      NotificationCenter notifications,
      Consumer<String> explain) {
    this.manager = manager;
    this.dispatcher = dispatcher;
    this.notifications = notifications;
    this.explain = explain;
    run.setId("diagnostics-run");
    run.setOnAction(ignored -> run());
    explainButton.setId("diagnostics-explain");
    explainButton.setDisable(true);
    explainButton.setOnAction(
        ignored -> {
          if (report != null) {
            explain.accept(
                "Erkläre diesen bereinigten lokalen Diagnosebericht. Führe nichts aus:\n\n"
                    + report.asSanitizedText());
          }
        });
    status.getStyleClass().add("muted-label");
    setSpacing(12);
    setPadding(new Insets(2));
    getChildren().addAll(run, explainButton, status, findings);
  }

  private void run() {
    run.setDisable(true);
    explainButton.setDisable(true);
    status.setText("Lokale Diagnose läuft …");
    manager
        .runAll()
        .whenComplete(
            (result, error) ->
                Platform.runLater(
                    () -> {
                      run.setDisable(false);
                      if (error != null) {
                        status.setText("Diagnose fehlgeschlagen.");
                        return;
                      }
                      report = result;
                      render(result);
                      explainButton.setDisable(false);
                      status.setText("Diagnose abgeschlossen · Bericht ist bereinigt.");
                    }));
  }

  private void render(DiagnosticReport result) {
    findings.getChildren().clear();
    for (DiagnosticFinding finding : result.findings()) {
      Label title = new Label(finding.category().displayName() + " · " + finding.status().name());
      title.getStyleClass().add("card-title");
      Label summary = new Label(finding.summary());
      summary.setWrapText(true);
      Label details = new Label(finding.details());
      details.setWrapText(true);
      details.getStyleClass().add("muted-label");
      VBox card = new VBox(6, title, summary, details);
      finding
          .suggestedAction()
          .ifPresent(
              actionId -> {
                Button action = new Button("Sichere lokale Maßnahme ausführen");
                action.setOnAction(
                    ignored ->
                        dispatcher
                            .dispatch(ActionRequest.fromButton(actionId))
                            .whenComplete(
                                (actionResult, error) ->
                                    Platform.runLater(
                                        () ->
                                            notifications.show(
                                                "Diagnosemaßnahme",
                                                error == null
                                                    ? actionResult.userMessage()
                                                    : "Die Maßnahme ist fehlgeschlagen."))));
                card.getChildren().add(action);
              });
      card.getStyleClass().add("status-card");
      findings.getChildren().add(card);
    }
  }
}
