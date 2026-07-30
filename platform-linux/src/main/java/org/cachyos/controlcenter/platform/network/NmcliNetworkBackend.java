package org.cachyos.controlcenter.platform.network;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.cachyos.controlcenter.modules.network.NetworkBackend;
import org.cachyos.controlcenter.modules.network.NetworkOperationResult;
import org.cachyos.controlcenter.modules.network.NetworkSnapshot;
import org.cachyos.controlcenter.platform.status.FixedCommandReader;
import org.cachyos.controlcenter.systeminfo.Capability;
import org.cachyos.controlcenter.systeminfo.CapabilityRegistry;

/** NetworkManager adapter using only a detected absolute nmcli path and fixed argument shapes. */
public final class NmcliNetworkBackend implements NetworkBackend {
  private static final Duration TIMEOUT = Duration.ofSeconds(8);
  private final Optional<Path> nmcli;

  public NmcliNetworkBackend(CapabilityRegistry capabilities) {
    nmcli = capabilities.status(Capability.NMCLI).executable();
  }

  @Override
  public NetworkSnapshot readSnapshot() {
    if (nmcli.isEmpty()) {
      return NetworkSnapshot.unavailable(
          "NetworkManager-Werkzeug nmcli fehlt. Optionales Paket: networkmanager.");
    }
    Optional<List<String>> general =
        run(List.of("-t", "-e", "yes", "-f", "CONNECTIVITY,WIFI", "general"));
    Optional<List<String>> devices =
        run(List.of("-t", "-e", "yes", "-f", "DEVICE,TYPE,STATE,CONNECTION", "device", "status"));
    if (general.isEmpty() || devices.isEmpty()) {
      return NetworkSnapshot.unavailable(
          "NetworkManager antwortet nicht oder die Abfrage lief ab.");
    }
    List<NetworkSnapshot.Device> parsedDevices =
        devices.get().stream()
            .map(NmcliNetworkBackend::parseDevice)
            .flatMap(Optional::stream)
            .toList();
    List<NetworkSnapshot.AccessPoint> accessPoints =
        run(
                List.of(
                    "-t",
                    "-e",
                    "yes",
                    "-f",
                    "SSID,SIGNAL,SECURITY,ACTIVE",
                    "device",
                    "wifi",
                    "list",
                    "--rescan",
                    "no"))
            .orElse(List.of())
            .stream()
            .map(NmcliNetworkBackend::parseAccessPoint)
            .flatMap(Optional::stream)
            .toList();
    List<NetworkSnapshot.Profile> profiles =
        run(List.of("-t", "-e", "yes", "-f", "UUID,NAME,TYPE,DEVICE", "connection", "show"))
            .orElse(List.of())
            .stream()
            .map(NmcliNetworkBackend::parseProfile)
            .flatMap(Optional::stream)
            .toList();
    List<String> ipData =
        run(List.of("-t", "-e", "yes", "-f", "IP4.GATEWAY,IP4.DNS", "device", "show"))
            .orElse(List.of());
    List<String> gateways = valuesWithPrefix(ipData, "IP4.GATEWAY:");
    List<String> dns = valuesWithPrefix(ipData, "IP4.DNS");
    String joined = String.join(":", general.get()).toLowerCase(java.util.Locale.ROOT);
    boolean online = joined.contains("full") || joined.contains("limited");
    boolean wifiEnabled = joined.contains("enabled") || joined.contains("aktiviert");
    return new NetworkSnapshot(
        true,
        online,
        wifiEnabled,
        parsedDevices,
        accessPoints,
        profiles,
        gateways,
        dns,
        "",
        Instant.now());
  }

  @Override
  public NetworkOperationResult scanWifi() {
    return operation(List.of("device", "wifi", "rescan"), "WLAN-Suche wurde aktualisiert.");
  }

  @Override
  public NetworkOperationResult setWifiEnabled(boolean enabled) {
    return operation(
        List.of("radio", "wifi", enabled ? "on" : "off"),
        enabled ? "WLAN wurde eingeschaltet." : "WLAN wurde ausgeschaltet.");
  }

  @Override
  public NetworkOperationResult activateProfile(String profileUuid) {
    return operation(
        List.of("connection", "up", "uuid", profileUuid),
        "Gespeichertes Netzwerkprofil wurde aktiviert.");
  }

  @Override
  public NetworkOperationResult disconnectDevice(String deviceName) {
    return operation(List.of("device", "disconnect", deviceName), "Netzwerkgerät wurde getrennt.");
  }

  private NetworkOperationResult operation(List<String> arguments, String successMessage) {
    if (nmcli.isEmpty()) {
      return NetworkOperationResult.unavailable("NetworkManager ist nicht verfügbar.");
    }
    return run(arguments).isPresent()
        ? NetworkOperationResult.success(successMessage)
        : NetworkOperationResult.failed(
            "NetworkManager hat die Aktion abgelehnt oder nicht beantwortet.");
  }

  private Optional<List<String>> run(List<String> arguments) {
    return nmcli.flatMap(path -> FixedCommandReader.read(path, arguments, TIMEOUT));
  }

  static Optional<NetworkSnapshot.Device> parseDevice(String line) {
    List<String> fields = splitEscaped(line);
    return fields.size() == 4
        ? Optional.of(
            new NetworkSnapshot.Device(fields.get(0), fields.get(1), fields.get(2), fields.get(3)))
        : Optional.empty();
  }

  static Optional<NetworkSnapshot.AccessPoint> parseAccessPoint(String line) {
    List<String> fields = splitEscaped(line);
    if (fields.size() != 4) {
      return Optional.empty();
    }
    try {
      return Optional.of(
          new NetworkSnapshot.AccessPoint(
              fields.get(0),
              Integer.parseInt(fields.get(1)),
              fields.get(2),
              "yes".equals(fields.get(3))));
    } catch (NumberFormatException exception) {
      return Optional.empty();
    }
  }

  static Optional<NetworkSnapshot.Profile> parseProfile(String line) {
    List<String> fields = splitEscaped(line);
    return fields.size() == 4
        ? Optional.of(
            new NetworkSnapshot.Profile(
                fields.get(0),
                fields.get(1),
                fields.get(2),
                !fields.get(3).isBlank() && !"--".equals(fields.get(3))))
        : Optional.empty();
  }

  static List<String> splitEscaped(String line) {
    List<String> fields = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean escaped = false;
    for (int index = 0; index < line.length(); index++) {
      char value = line.charAt(index);
      if (escaped) {
        current.append(value);
        escaped = false;
      } else if (value == '\\') {
        escaped = true;
      } else if (value == ':') {
        fields.add(current.toString());
        current.setLength(0);
      } else {
        current.append(value);
      }
    }
    if (escaped) {
      current.append('\\');
    }
    fields.add(current.toString());
    return List.copyOf(fields);
  }

  private static List<String> valuesWithPrefix(List<String> lines, String prefix) {
    return lines.stream()
        .filter(line -> line.startsWith(prefix))
        .map(line -> line.substring(line.indexOf(':') + 1))
        .filter(value -> !value.isBlank() && !"--".equals(value))
        .distinct()
        .toList();
  }
}
