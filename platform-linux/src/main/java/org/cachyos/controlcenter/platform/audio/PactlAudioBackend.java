package org.cachyos.controlcenter.platform.audio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.cachyos.controlcenter.modules.audio.AudioBackend;
import org.cachyos.controlcenter.modules.audio.AudioOperationResult;
import org.cachyos.controlcenter.modules.audio.AudioSnapshot;
import org.cachyos.controlcenter.platform.status.FixedCommandReader;
import org.cachyos.controlcenter.systeminfo.Capability;
import org.cachyos.controlcenter.systeminfo.CapabilityRegistry;

/** PipeWire-Pulse adapter using pactl JSON and validated argument values. */
public final class PactlAudioBackend implements AudioBackend {
  private static final Duration TIMEOUT = Duration.ofSeconds(8);
  private static final Path TEST_TONE =
      Path.of("/usr/share/sounds/freedesktop/stereo/audio-test-signal.oga");
  private final ObjectMapper mapper = new ObjectMapper();
  private final Optional<Path> pactl;
  private final Optional<Path> testPlayer;

  public PactlAudioBackend(CapabilityRegistry capabilities) {
    pactl = capabilities.status(Capability.PACTL).executable();
    testPlayer = capabilities.status(Capability.AUDIO_TEST_PLAYER).executable();
  }

  @Override
  public AudioSnapshot readSnapshot() {
    if (pactl.isEmpty()) {
      return AudioSnapshot.unavailable(
          "PipeWire-Pulse-Werkzeug pactl fehlt. Optionales Paket: libpulse.");
    }
    Optional<JsonNode> info = json(List.of("-f", "json", "info"));
    Optional<JsonNode> sinks = json(List.of("-f", "json", "list", "sinks"));
    Optional<JsonNode> sources = json(List.of("-f", "json", "list", "sources"));
    if (info.isEmpty() || sinks.isEmpty() || sources.isEmpty()) {
      return AudioSnapshot.unavailable("Der Audiodienst antwortet nicht oder die Abfrage lief ab.");
    }
    String defaultSink = info.get().path("default_sink_name").asText();
    String defaultSource = info.get().path("default_source_name").asText();
    List<AudioSnapshot.Device> outputs = parseDevices(sinks.get(), defaultSink);
    List<AudioSnapshot.Device> inputs = parseDevices(sources.get(), defaultSource);
    List<AudioSnapshot.Stream> streams =
        json(List.of("-f", "json", "list", "sink-inputs"))
            .map(PactlAudioBackend::parseStreams)
            .orElse(List.of());
    return new AudioSnapshot(
        true, info.get().path("server_name").asText(), outputs, inputs, streams, "", Instant.now());
  }

  @Override
  public AudioOperationResult setOutputVolume(String deviceName, int percent) {
    return operation(
        List.of("set-sink-volume", deviceName, percent + "%"), "Ausgabelautstärke wurde gesetzt.");
  }

  @Override
  public AudioOperationResult setInputVolume(String deviceName, int percent) {
    return operation(
        List.of("set-source-volume", deviceName, percent + "%"),
        "Mikrofonlautstärke wurde gesetzt.");
  }

  @Override
  public AudioOperationResult setOutputMute(String deviceName, boolean muted) {
    return operation(
        List.of("set-sink-mute", deviceName, Boolean.toString(muted)),
        muted ? "Ausgabe wurde stummgeschaltet." : "Ausgabe wurde aktiviert.");
  }

  @Override
  public AudioOperationResult setInputMute(String deviceName, boolean muted) {
    return operation(
        List.of("set-source-mute", deviceName, Boolean.toString(muted)),
        muted ? "Mikrofon wurde stummgeschaltet." : "Mikrofon wurde aktiviert.");
  }

  @Override
  public AudioOperationResult setDefaultOutput(String deviceName) {
    return operation(List.of("set-default-sink", deviceName), "Standardausgabe wurde geändert.");
  }

  @Override
  public AudioOperationResult setDefaultInput(String deviceName) {
    return operation(List.of("set-default-source", deviceName), "Standardmikrofon wurde geändert.");
  }

  @Override
  public AudioOperationResult playTestTone() {
    if (testPlayer.isEmpty() || !Files.isRegularFile(TEST_TONE)) {
      return AudioOperationResult.unavailable(
          "Testton ist nicht verfügbar. Benötigt werden pw-play und freedesktop-sounds.");
    }
    return FixedCommandReader.read(testPlayer.get(), List.of(TEST_TONE.toString()), TIMEOUT)
            .isPresent()
        ? AudioOperationResult.success("Testton wurde abgespielt.")
        : AudioOperationResult.failed("Testton konnte nicht abgespielt werden.");
  }

  private AudioOperationResult operation(List<String> arguments, String successMessage) {
    if (pactl.isEmpty()) {
      return AudioOperationResult.unavailable("Der Audiodienst ist nicht verfügbar.");
    }
    return FixedCommandReader.read(pactl.get(), arguments, TIMEOUT).isPresent()
        ? AudioOperationResult.success(successMessage)
        : AudioOperationResult.failed("Der Audiodienst hat die Aktion abgelehnt.");
  }

  private Optional<JsonNode> json(List<String> arguments) {
    Optional<List<String>> output =
        pactl.flatMap(path -> FixedCommandReader.read(path, arguments, TIMEOUT));
    if (output.isEmpty()) {
      return Optional.empty();
    }
    try {
      return Optional.of(mapper.readTree(String.join("\n", output.get())));
    } catch (IOException exception) {
      return Optional.empty();
    }
  }

  static List<AudioSnapshot.Device> parseDevices(JsonNode array, String defaultName) {
    if (!array.isArray()) {
      return List.of();
    }
    List<AudioSnapshot.Device> devices = new ArrayList<>();
    for (JsonNode item : array) {
      String name = item.path("name").asText();
      String description = item.path("description").asText();
      devices.add(
          new AudioSnapshot.Device(
              name,
              description,
              volumePercent(item.path("volume")),
              item.path("mute").asBoolean(),
              name.equals(defaultName)));
    }
    return List.copyOf(devices);
  }

  static List<AudioSnapshot.Stream> parseStreams(JsonNode array) {
    if (!array.isArray()) {
      return List.of();
    }
    List<AudioSnapshot.Stream> streams = new ArrayList<>();
    for (JsonNode item : array) {
      JsonNode properties = item.path("properties");
      String application = properties.path("application.name").asText();
      if (application.isBlank()) {
        application = properties.path("media.name").asText();
      }
      streams.add(
          new AudioSnapshot.Stream(
              item.path("index").asText(),
              application,
              volumePercent(item.path("volume")),
              item.path("mute").asBoolean()));
    }
    return List.copyOf(streams);
  }

  static int volumePercent(JsonNode volume) {
    if (!volume.isObject()) {
      return 0;
    }
    int total = 0;
    int count = 0;
    for (JsonNode channel : volume) {
      String percent = channel.path("value_percent").asText().replace("%", "").trim();
      try {
        total += Integer.parseInt(percent);
        count++;
      } catch (NumberFormatException ignored) {
        // Ignore malformed channels and use the remaining real values.
      }
    }
    return count == 0 ? 0 : Math.min(150, Math.max(0, Math.round((float) total / count)));
  }
}
