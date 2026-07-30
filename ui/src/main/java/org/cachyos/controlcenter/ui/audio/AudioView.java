package org.cachyos.controlcenter.ui.audio;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.cachyos.controlcenter.core.action.ActionDispatcher;
import org.cachyos.controlcenter.core.action.ActionId;
import org.cachyos.controlcenter.core.action.ActionRequest;
import org.cachyos.controlcenter.core.action.InputSource;
import org.cachyos.controlcenter.modules.audio.AudioEvents;
import org.cachyos.controlcenter.modules.audio.AudioManagerModule;
import org.cachyos.controlcenter.modules.audio.AudioSnapshot;
import org.cachyos.controlcenter.ui.notifications.NotificationCenter;

/** Event-driven PipeWire mixer page; it never captures microphone audio. */
public final class AudioView extends VBox {
  private final AudioManagerModule manager;
  private final ActionDispatcher dispatcher;
  private final NotificationCenter notifications;
  private final AtomicBoolean loading = new AtomicBoolean();
  private final Label status = new Label("Audiodienst wird geladen …");
  private final ListView<AudioSnapshot.Device> outputs = new ListView<>();
  private final ListView<AudioSnapshot.Device> inputs = new ListView<>();
  private final ListView<AudioSnapshot.Stream> streams = new ListView<>();
  private final Slider volume = new Slider(0, 150, 100);
  private final Button applyVolume = new Button("Lautstärke anwenden");
  private final Button mute = new Button("Stumm");
  private final Button unmute = new Button("Ton an");
  private final Button setDefault = new Button("Als Standard");
  private final Button testTone = new Button("Testton");
  private boolean inputSelected;

  public AudioView(
      AudioManagerModule manager,
      AudioEvents events,
      ActionDispatcher dispatcher,
      NotificationCenter notifications) {
    this.manager = manager;
    this.dispatcher = dispatcher;
    this.notifications = notifications;
    outputs.setId("audio-outputs");
    inputs.setId("audio-inputs");
    streams.setId("audio-streams");
    outputs.setPrefHeight(150);
    inputs.setPrefHeight(150);
    streams.setPrefHeight(130);
    outputs.setCellFactory(ignored -> new DeviceCell());
    inputs.setCellFactory(ignored -> new DeviceCell());
    streams.setCellFactory(ignored -> new StreamCell());
    outputs
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (ignored, previous, selected) -> {
              if (selected != null) {
                inputSelected = false;
                inputs.getSelectionModel().clearSelection();
                volume.setValue(selected.volumePercent());
                enableControls(true);
              }
            });
    inputs
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (ignored, previous, selected) -> {
              if (selected != null) {
                inputSelected = true;
                outputs.getSelectionModel().clearSelection();
                volume.setValue(selected.volumePercent());
                enableControls(true);
              }
            });
    volume.setShowTickLabels(true);
    volume.setShowTickMarks(true);
    volume.setMajorTickUnit(25);
    applyVolume.setOnAction(
        ignored ->
            selectedAction(
                inputSelected ? ActionId.AUDIO_SET_INPUT_VOLUME : ActionId.AUDIO_SET_OUTPUT_VOLUME,
                Map.of("volume", Integer.toString((int) Math.round(volume.getValue())))));
    mute.setOnAction(
        ignored ->
            selectedAction(
                inputSelected ? ActionId.AUDIO_SET_INPUT_MUTE : ActionId.AUDIO_SET_OUTPUT_MUTE,
                Map.of("muted", "true")));
    unmute.setOnAction(
        ignored ->
            selectedAction(
                inputSelected ? ActionId.AUDIO_SET_INPUT_MUTE : ActionId.AUDIO_SET_OUTPUT_MUTE,
                Map.of("muted", "false")));
    setDefault.setOnAction(
        ignored ->
            selectedAction(
                inputSelected
                    ? ActionId.AUDIO_SET_DEFAULT_INPUT
                    : ActionId.AUDIO_SET_DEFAULT_OUTPUT,
                Map.of()));
    testTone.setOnAction(ignored -> dispatch(ActionId.AUDIO_TEST_TONE, Map.of()));
    enableControls(false);

    FlowPane controls = new FlowPane(8, 8, applyVolume, mute, unmute, setDefault, testTone);
    setSpacing(14);
    getChildren()
        .addAll(
            status,
            section("Ausgabegeräte", outputs),
            section("Mikrofone", inputs),
            new Label("Lautstärke (0–150 %)"),
            volume,
            controls,
            section("Wiedergabestreams", streams),
            new Label(
                "Die Mikrofonsteuerung verändert nur Mixerwerte. Es wird kein Audio aufgenommen."));
    events.subscribe(this::refresh);
    refresh();
  }

  private void refresh() {
    if (!loading.compareAndSet(false, true)) {
      return;
    }
    CompletableFuture.supplyAsync(manager::audioSnapshot)
        .whenComplete(
            (snapshot, failure) ->
                Platform.runLater(
                    () -> {
                      loading.set(false);
                      show(
                          failure == null
                              ? snapshot
                              : AudioSnapshot.unavailable(
                                  "Audiodaten konnten nicht gelesen werden."));
                    }));
  }

  private void show(AudioSnapshot snapshot) {
    status.setText(snapshot.available() ? snapshot.server() + " · bereit" : snapshot.message());
    outputs.getItems().setAll(snapshot.outputs());
    inputs.getItems().setAll(snapshot.inputs());
    streams.getItems().setAll(snapshot.streams());
    testTone.setDisable(!snapshot.available());
    if (!snapshot.available()) {
      enableControls(false);
    }
  }

  private void selectedAction(ActionId id, Map<String, String> extra) {
    AudioSnapshot.Device selected =
        inputSelected
            ? inputs.getSelectionModel().getSelectedItem()
            : outputs.getSelectionModel().getSelectedItem();
    if (selected == null) {
      return;
    }
    java.util.HashMap<String, String> parameters = new java.util.HashMap<>(extra);
    parameters.put("deviceName", selected.name());
    dispatch(id, Map.copyOf(parameters));
  }

  private void dispatch(ActionId actionId, Map<String, String> parameters) {
    enableControls(false);
    dispatcher
        .dispatch(new ActionRequest(actionId, InputSource.BUTTON, parameters, Instant.now()))
        .whenComplete(
            (result, failure) ->
                Platform.runLater(
                    () -> {
                      notifications.show(
                          "Audio",
                          failure == null
                              ? result.userMessage()
                              : "Die Audioaktion ist unerwartet fehlgeschlagen.");
                      refresh();
                    }));
  }

  private void enableControls(boolean enabled) {
    applyVolume.setDisable(!enabled);
    mute.setDisable(!enabled);
    unmute.setDisable(!enabled);
    setDefault.setDisable(!enabled);
  }

  private static VBox section(String title, ListView<?> list) {
    Label heading = new Label(title);
    heading.getStyleClass().add("card-title");
    VBox box = new VBox(7, heading, list);
    box.setPadding(new Insets(14));
    box.getStyleClass().add("details-panel");
    return box;
  }

  private static final class DeviceCell
      extends javafx.scene.control.ListCell<AudioSnapshot.Device> {
    @Override
    protected void updateItem(AudioSnapshot.Device item, boolean empty) {
      super.updateItem(item, empty);
      setText(
          empty || item == null
              ? null
              : (item.defaultDevice() ? "● " : "")
                  + item.description()
                  + " · "
                  + item.volumePercent()
                  + " %"
                  + (item.muted() ? " · stumm" : ""));
    }
  }

  private static final class StreamCell
      extends javafx.scene.control.ListCell<AudioSnapshot.Stream> {
    @Override
    protected void updateItem(AudioSnapshot.Stream item, boolean empty) {
      super.updateItem(item, empty);
      setText(
          empty || item == null
              ? null
              : item.application()
                  + " · "
                  + item.volumePercent()
                  + " %"
                  + (item.muted() ? " · stumm" : ""));
    }
  }
}
