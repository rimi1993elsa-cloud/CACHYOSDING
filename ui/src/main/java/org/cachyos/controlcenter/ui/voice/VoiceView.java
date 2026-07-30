package org.cachyos.controlcenter.ui.voice;

import java.io.File;
import java.util.function.BiConsumer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.cachyos.controlcenter.core.action.InputSource;
import org.cachyos.controlcenter.input.voice.MicrophoneCatalog;
import org.cachyos.controlcenter.input.voice.MicrophoneDescriptor;
import org.cachyos.controlcenter.input.voice.SpeechModelManager;
import org.cachyos.controlcenter.input.voice.SpeechToTextEngine;
import org.cachyos.controlcenter.input.voice.TranscriptEvent;

/** Push-to-talk UI that exposes transcripts but has no reference to the action dispatcher. */
public final class VoiceView extends VBox {
  private final MicrophoneCatalog microphones;
  private final SpeechModelManager models;
  private final SpeechToTextEngine engine;
  private final ComboBox<MicrophoneDescriptor> microphone = new ComboBox<>();
  private final Label modelStatus = new Label();
  private final Label recordingStatus = new Label("Mikrofon ist aus.");
  private final Label partial = new Label();
  private final TextArea transcript = new TextArea();
  private final Button pushToTalk = new Button("Gedrückt halten zum Sprechen");
  private final BiConsumer<String, InputSource> onSubmit;

  public VoiceView(
      MicrophoneCatalog microphones,
      SpeechModelManager models,
      SpeechToTextEngine engine,
      BiConsumer<String, InputSource> onSubmit) {
    this.microphones = microphones;
    this.models = models;
    this.engine = engine;
    this.onSubmit = onSubmit;
    microphone.setId("voice-microphone");
    microphone.getItems().setAll(microphones.availableMicrophones());
    microphone.getSelectionModel().selectFirst();
    transcript.setId("voice-transcript");
    transcript.setEditable(false);
    transcript.setWrapText(true);
    transcript.setPrefRowCount(8);
    partial.setWrapText(true);
    partial.getStyleClass().add("muted-label");
    recordingStatus.getStyleClass().add("muted-label");
    Button selectModel = new Button("Vosk-Modellordner auswählen");
    selectModel.setOnAction(ignored -> chooseModel());
    pushToTalk.setId("push-to-talk");
    pushToTalk.setOnMousePressed(ignored -> start());
    pushToTalk.setOnMouseReleased(ignored -> engine.stop());
    pushToTalk.setOnKeyPressed(
        event -> {
          if (event.getCode() == KeyCode.SPACE) {
            start();
            event.consume();
          }
        });
    pushToTalk.setOnKeyReleased(
        event -> {
          if (event.getCode() == KeyCode.SPACE) {
            engine.stop();
            event.consume();
          }
        });
    Button submit = new Button("Transkript lokal auswerten");
    submit.setId("voice-submit");
    submit.disableProperty().bind(transcript.textProperty().isEmpty());
    submit.setOnAction(ignored -> onSubmit.accept(transcript.getText(), InputSource.VOICE));
    setSpacing(14);
    setPadding(new Insets(2));
    getChildren()
        .addAll(
            new Label("Mikrofon"),
            microphone,
            modelStatus,
            selectModel,
            pushToTalk,
            recordingStatus,
            new Label("Live-Erkennung"),
            partial,
            new Label("Bestätigtes Transkript"),
            transcript,
            submit,
            new Label(
                "Eine Auswertung erfolgt erst nach bewusstem Klick. Online wird nichts versendet."));
    updateAvailability();
  }

  private void chooseModel() {
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle("Entpacktes deutsches Vosk-Modell auswählen");
    File selected = chooser.showDialog(getScene().getWindow());
    if (selected != null) {
      try {
        models.select(selected.toPath());
      } catch (IllegalArgumentException ignored) {
        // Status below remains authoritative.
      }
      updateAvailability();
    }
  }

  private void updateAvailability() {
    SpeechModelManager.ModelStatus status = models.status();
    modelStatus.setText(status.message() + "\n" + status.directory());
    pushToTalk.setDisable(!status.available() || microphone.getItems().isEmpty());
  }

  private void start() {
    MicrophoneDescriptor selected = microphone.getValue();
    SpeechModelManager.ModelStatus model = models.status();
    if (selected != null && model.available()) {
      engine.start(model.directory(), selected, this::handle);
    }
  }

  private void handle(TranscriptEvent event) {
    Platform.runLater(
        () -> {
          switch (event.state()) {
            case LOADING -> recordingStatus.setText("Sprachmodell wird geladen …");
            case RECORDING -> recordingStatus.setText("Aufnahme aktiv – Taste gedrückt halten.");
            case PARTIAL -> partial.setText(event.text());
            case FINAL -> {
              partial.setText("");
              if (!event.text().isBlank()) {
                transcript.appendText(
                    (transcript.getText().isBlank() ? "" : System.lineSeparator()) + event.text());
              }
            }
            case STOPPED -> recordingStatus.setText("Mikrofon ist aus.");
            case ERROR -> recordingStatus.setText(event.text());
            default -> recordingStatus.setText("Unbekannter Aufnahmezustand.");
          }
        });
  }
}
