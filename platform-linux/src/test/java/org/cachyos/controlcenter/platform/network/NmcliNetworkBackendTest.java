package org.cachyos.controlcenter.platform.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class NmcliNetworkBackendTest {
  @Test
  void splitsEscapedNmcliFieldsWithoutShellParsing() {
    assertEquals(
        List.of("wlan0", "wifi", "connected", "Office: West"),
        NmcliNetworkBackend.splitEscaped("wlan0:wifi:connected:Office\\: West"));
  }

  @Test
  void rejectsMalformedAccessPointRows() {
    assertTrue(NmcliNetworkBackend.parseAccessPoint("ssid:not-a-number:WPA2:no").isEmpty());
  }
}
