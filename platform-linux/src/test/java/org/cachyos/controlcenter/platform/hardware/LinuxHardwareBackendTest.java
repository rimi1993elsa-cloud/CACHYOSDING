package org.cachyos.controlcenter.platform.hardware;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class LinuxHardwareBackendTest {
  @Test
  void parsesPciDriverAndUsbWithoutSerials() {
    var pci =
        LinuxHardwareBackend.parsePci(
            List.of(
                "0000:00:02.0 VGA compatible controller: Intel Device [8086:1234]",
                "\tKernel driver in use: i915"));
    var usb =
        LinuxHardwareBackend.parseUsb(List.of("Bus 001 Device 002: ID 0bda:0129 Realtek Reader"));
    assertEquals("i915", pci.getFirst().driver());
    assertEquals("0bda:0129", usb.getFirst().identifier());
  }
}
