package org.cachyos.controlcenter.modules.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.Map;
import org.cachyos.controlcenter.core.action.ActionId;
import org.cachyos.controlcenter.core.action.ActionRequest;
import org.cachyos.controlcenter.core.action.InputSource;
import org.junit.jupiter.api.Test;

class NetworkManagerModuleTest {
  private final RecordingBackend backend = new RecordingBackend();
  private final NetworkManagerModule module = new NetworkManagerModule(backend);

  @Test
  void acceptsOnlyValidatedProfileUuid() {
    String uuid = "12345678-1234-1234-1234-123456789abc";
    module.execute(request(ActionId.NETWORK_ACTIVATE_PROFILE, Map.of("profileUuid", uuid)));
    assertEquals(uuid, backend.profile);
  }

  @Test
  void rejectsMetacharactersBeforeBackend() {
    ActionRequest request =
        request(ActionId.NETWORK_DISCONNECT_DEVICE, Map.of("deviceName", "wlan0; shutdown now"));
    assertThrows(IllegalArgumentException.class, () -> module.execute(request));
  }

  private static ActionRequest request(ActionId id, Map<String, String> parameters) {
    return new ActionRequest(id, InputSource.BUTTON, parameters, Instant.now());
  }

  private static final class RecordingBackend implements NetworkBackend {
    private String profile;

    @Override
    public NetworkSnapshot readSnapshot() {
      return NetworkSnapshot.unavailable("test");
    }

    @Override
    public NetworkOperationResult scanWifi() {
      return NetworkOperationResult.success("ok");
    }

    @Override
    public NetworkOperationResult setWifiEnabled(boolean enabled) {
      return NetworkOperationResult.success("ok");
    }

    @Override
    public NetworkOperationResult activateProfile(String profileUuid) {
      profile = profileUuid;
      return NetworkOperationResult.success("ok");
    }

    @Override
    public NetworkOperationResult disconnectDevice(String deviceName) {
      return NetworkOperationResult.success("ok");
    }
  }
}
