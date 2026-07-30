package org.cachyos.controlcenter;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.cachyos.controlcenter.persistence.ApplicationSettings;
import org.cachyos.controlcenter.persistence.SettingsService;

/** First-run privacy and AI setup. No credential is ever collected by the application UI. */
final class SetupWizard {
  private SetupWizard() {}

  static void showIfRequired(Stage owner, SettingsService settingsService) {
    if (!settingsService.firstRunRequired()) {
      return;
    }
    ApplicationSettings current = settingsService.current();
    Stage wizard = new Stage();
    wizard.initOwner(owner);
    wizard.initModality(Modality.WINDOW_MODAL);
    wizard.setTitle("CachyOS Control Center einrichten");

    Label heading = new Label("Willkommen – lokale Kontrolle zuerst");
    heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
    CheckBox onlineAi = new CheckBox("Online-KI aktivieren");
    onlineAi.setSelected(current.onlineAiEnabled());
    CheckBox diagnostics = new CheckBox("Diagnosedaten nach Bestätigung mit der KI teilen");
    diagnostics.setSelected(current.shareDiagnostics());
    CheckBox history = new CheckBox("Lokalen Chatverlauf speichern");
    history.setSelected(current.storeChatHistory());
    Spinner<Integer> budget =
        new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 5));
    budget.getValueFactory().setValue(current.monthlyBudgetCents() / 100);
    budget.setEditable(false);

    Label explanation =
        new Label(
            """
            Systemaktionen bleiben lokal; die Online-KI darf keine Aktionen ausführen. API-Keys
            werden nur über Secret Service/KWallet eingerichtet (siehe API-Key-Hilfe). Das Budget
            ist eine lokale Warnschwelle, keine Abrechnungssperre.""");
    explanation.setWrapText(true);
    explanation.setMinWidth(0);
    explanation.setPrefWidth(500);
    explanation.setMaxWidth(500);
    Label budgetLabel = new Label("Monatliche Warnschwelle in Euro:");
    Region spacer = new Region();
    VBox.setVgrow(spacer, Priority.ALWAYS);
    Button cancel = new Button("Später");
    Button finish = new Button("Einrichtung abschließen");
    finish.setDefaultButton(true);
    cancel.setCancelButton(true);
    HBox actions = new HBox(10, cancel, finish);
    actions.setAlignment(Pos.CENTER_RIGHT);
    VBox content =
        new VBox(
            12,
            heading,
            explanation,
            onlineAi,
            diagnostics,
            history,
            budgetLabel,
            budget,
            spacer,
            actions);
    content.setPadding(new Insets(20));
    content.setStyle("-fx-background-color: -fx-control-inner-background;");

    cancel.setOnAction(ignored -> wizard.close());
    finish.setOnAction(
        ignored -> {
          settingsService.update(
              new ApplicationSettings(
                  current.enabledModules(),
                  current.quickButtons(),
                  current.microphoneEnabled(),
                  current.microphoneId(),
                  onlineAi.isSelected(),
                  onlineAi.isSelected() ? "openai" : "offline",
                  budget.getValue() * 100,
                  current.shareDocumentation(),
                  diagnostics.isSelected(),
                  current.shareHardware(),
                  current.shareSystemContext(),
                  history.isSelected()));
          settingsService.completeFirstRun();
          wizard.close();
        });
    wizard.setScene(new Scene(content, 540, 460));
    wizard.setMinWidth(500);
    wizard.setMinHeight(430);
    wizard.showAndWait();
  }
}
