package org.cachyos.controlcenter.modules.hardware;

import java.time.Instant;
import java.util.List;

public record HardwareSnapshot(
    boolean available,
    String manufacturer,
    String product,
    String cpu,
    long memoryBytes,
    String battery,
    List<HardwareDevice> graphics,
    List<HardwareDevice> pciDevices,
    List<HardwareDevice> usbDevices,
    List<SensorReading> sensors,
    Instant capturedAt,
    String message) {
  public HardwareSnapshot {
    graphics = List.copyOf(graphics);
    pciDevices = List.copyOf(pciDevices);
    usbDevices = List.copyOf(usbDevices);
    sensors = List.copyOf(sensors);
  }
}
