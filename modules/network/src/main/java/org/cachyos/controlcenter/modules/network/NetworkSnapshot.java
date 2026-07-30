package org.cachyos.controlcenter.modules.network;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** Sanitized NetworkManager state; it never contains passwords or other secrets. */
public record NetworkSnapshot(
    boolean available,
    boolean online,
    boolean wifiEnabled,
    List<Device> devices,
    List<AccessPoint> accessPoints,
    List<Profile> profiles,
    List<String> gateways,
    List<String> dnsServers,
    String message,
    Instant capturedAt) {
  public NetworkSnapshot {
    devices = List.copyOf(devices);
    accessPoints = List.copyOf(accessPoints);
    profiles = List.copyOf(profiles);
    gateways = List.copyOf(gateways);
    dnsServers = List.copyOf(dnsServers);
    message = Objects.requireNonNullElse(message, "");
    capturedAt = Objects.requireNonNull(capturedAt, "capturedAt");
  }

  public static NetworkSnapshot unavailable(String message) {
    return new NetworkSnapshot(
        false,
        false,
        false,
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        message,
        Instant.now());
  }

  public record Device(String name, String type, String state, String connection) {
    public Device {
      name = safe(name);
      type = safe(type);
      state = safe(state);
      connection = safe(connection);
    }
  }

  public record AccessPoint(String ssid, int signal, String security, boolean active) {
    public AccessPoint {
      ssid = safe(ssid);
      signal = Math.max(0, Math.min(100, signal));
      security = safe(security);
    }
  }

  public record Profile(String uuid, String name, String type, boolean active) {
    public Profile {
      uuid = safe(uuid);
      name = safe(name);
      type = safe(type);
    }
  }

  private static String safe(String value) {
    return value == null || value.isBlank() ? "Nicht verfügbar" : value;
  }
}
