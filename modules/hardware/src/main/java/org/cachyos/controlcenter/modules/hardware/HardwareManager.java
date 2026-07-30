package org.cachyos.controlcenter.modules.hardware;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public final class HardwareManager implements AutoCloseable {
  private static final Pattern SERIAL =
      Pattern.compile("(?i)(serial|machine[-_ ]?id|uuid)\\s*[:=]\\s*\\S+");
  private final HardwareBackend backend;
  private final ExecutorService worker =
      Executors.newSingleThreadExecutor(Thread.ofPlatform().name("hardware-manager").factory());

  public HardwareManager(HardwareBackend backend) {
    this.backend = backend;
  }

  public CompletableFuture<HardwareSnapshot> inspect() {
    return CompletableFuture.supplyAsync(backend::inspect, worker);
  }

  public HardwareReport report(HardwareSnapshot snapshot, boolean anonymize) {
    StringBuilder text = new StringBuilder();
    text.append("Hersteller: ").append(snapshot.manufacturer()).append('\n');
    text.append("Modell: ").append(snapshot.product()).append('\n');
    text.append("CPU: ").append(snapshot.cpu()).append('\n');
    text.append("RAM: ").append(snapshot.memoryBytes()).append(" Bytes\n");
    text.append("Akku: ").append(snapshot.battery()).append('\n');
    appendDevices(text, "Grafik", snapshot.graphics());
    appendDevices(text, "PCI", snapshot.pciDevices());
    appendDevices(text, "USB", snapshot.usbDevices());
    snapshot.sensors().stream()
        .limit(100)
        .forEach(
            sensor ->
                text.append("Sensor: ")
                    .append(sensor.label())
                    .append('=')
                    .append(sensor.value())
                    .append(sensor.unit())
                    .append('\n'));
    String result = text.toString();
    return new HardwareReport(
        anonymize ? SERIAL.matcher(result).replaceAll("$1: [MASKIERT]") : result, anonymize);
  }

  private static void appendDevices(
      StringBuilder text, String category, java.util.List<HardwareDevice> devices) {
    devices.stream()
        .limit(500)
        .forEach(
            device ->
                text.append(category)
                    .append(": ")
                    .append(device.identifier())
                    .append(' ')
                    .append(device.description())
                    .append(" · Treiber ")
                    .append(device.driver().isBlank() ? "unbekannt" : device.driver())
                    .append('\n'));
  }

  @Override
  public void close() {
    worker.shutdownNow();
  }
}
