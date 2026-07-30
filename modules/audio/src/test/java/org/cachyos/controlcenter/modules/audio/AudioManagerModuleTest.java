package org.cachyos.controlcenter.modules.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.Map;
import org.cachyos.controlcenter.core.action.ActionId;
import org.cachyos.controlcenter.core.action.ActionRequest;
import org.cachyos.controlcenter.core.action.InputSource;
import org.junit.jupiter.api.Test;

class AudioManagerModuleTest {
  private final RecordingBackend backend = new RecordingBackend();
  private final AudioManagerModule module = new AudioManagerModule(backend);

  @Test
  void validatesVolumeAndDeviceBeforeBackend() {
    module.execute(
        request(
            ActionId.AUDIO_SET_OUTPUT_VOLUME,
            Map.of("deviceName", "alsa_output.pci-0000_00_1f.3", "volume", "75")));
    assertEquals(75, backend.volume);
  }

  @Test
  void rejectsInjectedDeviceName() {
    ActionRequest request =
        request(
            ActionId.AUDIO_SET_INPUT_MUTE,
            Map.of("deviceName", "@DEFAULT_SOURCE@;rm", "muted", "true"));
    assertThrows(IllegalArgumentException.class, () -> module.execute(request));
  }

  private static ActionRequest request(ActionId id, Map<String, String> parameters) {
    return new ActionRequest(id, InputSource.BUTTON, parameters, Instant.now());
  }

  private static final class RecordingBackend implements AudioBackend {
    private int volume;

    @Override
    public AudioSnapshot readSnapshot() {
      return AudioSnapshot.unavailable("test");
    }

    @Override
    public AudioOperationResult setOutputVolume(String deviceName, int percent) {
      volume = percent;
      return AudioOperationResult.success("ok");
    }

    @Override
    public AudioOperationResult setInputVolume(String deviceName, int percent) {
      return AudioOperationResult.success("ok");
    }

    @Override
    public AudioOperationResult setOutputMute(String deviceName, boolean muted) {
      return AudioOperationResult.success("ok");
    }

    @Override
    public AudioOperationResult setInputMute(String deviceName, boolean muted) {
      return AudioOperationResult.success("ok");
    }

    @Override
    public AudioOperationResult setDefaultOutput(String deviceName) {
      return AudioOperationResult.success("ok");
    }

    @Override
    public AudioOperationResult setDefaultInput(String deviceName) {
      return AudioOperationResult.success("ok");
    }

    @Override
    public AudioOperationResult playTestTone() {
      return AudioOperationResult.success("ok");
    }
  }
}
