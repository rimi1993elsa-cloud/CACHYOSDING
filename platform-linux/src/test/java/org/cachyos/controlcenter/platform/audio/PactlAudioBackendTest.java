package org.cachyos.controlcenter.platform.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class PactlAudioBackendTest {
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void parsesDeviceVolumeWithoutUsingLocalizedText() throws Exception {
    JsonNode devices =
        mapper.readTree(
            """
            [{"name":"sink.one","description":"Speakers","mute":false,
              "volume":{"left":{"value_percent":"80%"},"right":{"value_percent":"100%"}}}]
            """);

    var parsed = PactlAudioBackend.parseDevices(devices, "sink.one");

    assertEquals(1, parsed.size());
    assertEquals(90, parsed.getFirst().volumePercent());
    assertTrue(parsed.getFirst().defaultDevice());
  }

  @Test
  void malformedVolumeRemainsBounded() throws Exception {
    JsonNode volume = mapper.readTree("{\"left\":{\"value_percent\":\"invalid\"}}");
    assertEquals(0, PactlAudioBackend.volumePercent(volume));
  }
}
