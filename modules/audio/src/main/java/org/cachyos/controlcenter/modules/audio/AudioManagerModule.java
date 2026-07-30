package org.cachyos.controlcenter.modules.audio;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.cachyos.controlcenter.core.action.ActionId;
import org.cachyos.controlcenter.core.action.ActionRequest;
import org.cachyos.controlcenter.core.action.ActionResult;
import org.cachyos.controlcenter.core.module.ManagerModule;
import org.cachyos.controlcenter.core.module.ModuleCapabilities;
import org.cachyos.controlcenter.core.module.ModuleId;
import org.cachyos.controlcenter.core.module.ModuleSnapshot;
import org.cachyos.controlcenter.core.validation.ActionValidators;

/** Validates mixer controls before passing them to the audio server adapter. */
public final class AudioManagerModule implements ManagerModule {
  private static final ModuleId MODULE_ID = new ModuleId("audio");
  private static final Pattern DEVICE = Pattern.compile("[A-Za-z0-9_.:-]{1,255}");
  private static final Set<ActionId> ACTIONS =
      Set.of(
          ActionId.AUDIO_SET_OUTPUT_VOLUME,
          ActionId.AUDIO_SET_INPUT_VOLUME,
          ActionId.AUDIO_SET_OUTPUT_MUTE,
          ActionId.AUDIO_SET_INPUT_MUTE,
          ActionId.AUDIO_SET_DEFAULT_OUTPUT,
          ActionId.AUDIO_SET_DEFAULT_INPUT,
          ActionId.AUDIO_TEST_TONE);
  private final AudioBackend backend;

  public AudioManagerModule(AudioBackend backend) {
    this.backend = backend;
  }

  public AudioSnapshot audioSnapshot() {
    return backend.readSnapshot();
  }

  @Override
  public ModuleId id() {
    return MODULE_ID;
  }

  @Override
  public String displayName() {
    return "Audio";
  }

  @Override
  public ModuleCapabilities capabilities() {
    return new ModuleCapabilities(true, true, false);
  }

  @Override
  public ModuleSnapshot loadSnapshot() {
    return ModuleSnapshot.empty();
  }

  @Override
  public Set<ActionId> actions() {
    return ACTIONS;
  }

  @Override
  public ActionResult execute(ActionRequest request) {
    if (!supports(request.actionId())) {
      return ActionResult.rejected("Unbekannte Audioaktion.", "Unsupported action id");
    }
    AudioOperationResult result;
    if (request.actionId().equals(ActionId.AUDIO_TEST_TONE)) {
      ActionValidators.requireNoParameters(request.parameters());
      result = backend.playTestTone();
    } else {
      String device = requireDevice(request.parameters());
      if (request.actionId().equals(ActionId.AUDIO_SET_OUTPUT_VOLUME)) {
        result = backend.setOutputVolume(device, requireVolume(request.parameters()));
      } else if (request.actionId().equals(ActionId.AUDIO_SET_INPUT_VOLUME)) {
        result = backend.setInputVolume(device, requireVolume(request.parameters()));
      } else if (request.actionId().equals(ActionId.AUDIO_SET_OUTPUT_MUTE)) {
        result = backend.setOutputMute(device, requireMuted(request.parameters()));
      } else if (request.actionId().equals(ActionId.AUDIO_SET_INPUT_MUTE)) {
        result = backend.setInputMute(device, requireMuted(request.parameters()));
      } else if (request.actionId().equals(ActionId.AUDIO_SET_DEFAULT_OUTPUT)) {
        requireKeys(request.parameters(), Set.of("deviceName"));
        result = backend.setDefaultOutput(device);
      } else {
        requireKeys(request.parameters(), Set.of("deviceName"));
        result = backend.setDefaultInput(device);
      }
    }
    if (result.success()) {
      return ActionResult.success(result.message());
    }
    return result.available()
        ? ActionResult.failed(result.message(), "Audio operation failed")
        : ActionResult.unavailable(result.message());
  }

  private static String requireDevice(Map<String, String> parameters) {
    String value = parameters.get("deviceName");
    if (value == null || !DEVICE.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid audio device");
    }
    return value;
  }

  private static int requireVolume(Map<String, String> parameters) {
    requireKeys(parameters, Set.of("deviceName", "volume"));
    try {
      int value = Integer.parseInt(parameters.get("volume"));
      if (value < 0 || value > 150) {
        throw new IllegalArgumentException("Invalid audio volume");
      }
      return value;
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException("Invalid audio volume", exception);
    }
  }

  private static boolean requireMuted(Map<String, String> parameters) {
    requireKeys(parameters, Set.of("deviceName", "muted"));
    String value = parameters.get("muted");
    if (!"true".equals(value) && !"false".equals(value)) {
      throw new IllegalArgumentException("Invalid mute state");
    }
    return Boolean.parseBoolean(value);
  }

  private static void requireKeys(Map<String, String> parameters, Set<String> expected) {
    if (!parameters.keySet().equals(expected)) {
      throw new IllegalArgumentException("Invalid audio parameters");
    }
  }
}
