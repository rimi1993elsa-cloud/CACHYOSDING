package org.cachyos.controlcenter.core.action;

import java.util.Objects;
import java.util.regex.Pattern;

/** Validated stable action identifier; labels and user input are never action identifiers. */
public record ActionId(String value) implements Comparable<ActionId> {
  private static final Pattern FORMAT = Pattern.compile("[a-z][a-z0-9.-]{2,63}");

  public static final ActionId OPEN_FIREFOX = new ActionId("desktop.open-firefox");
  public static final ActionId OPEN_FILE_MANAGER = new ActionId("desktop.open-file-manager");
  public static final ActionId OPEN_TERMINAL = new ActionId("desktop.open-terminal");
  public static final ActionId LOCK_SCREEN = new ActionId("desktop.lock-screen");
  public static final ActionId NETWORK_SCAN_WIFI = new ActionId("network.scan-wifi");
  public static final ActionId NETWORK_WIFI_ON = new ActionId("network.wifi-on");
  public static final ActionId NETWORK_WIFI_OFF = new ActionId("network.wifi-off");
  public static final ActionId NETWORK_ACTIVATE_PROFILE = new ActionId("network.activate-profile");
  public static final ActionId NETWORK_DISCONNECT_DEVICE =
      new ActionId("network.disconnect-device");

  public ActionId {
    Objects.requireNonNull(value, "value");
    if (!FORMAT.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid action id");
    }
  }

  public static ActionId of(String value) {
    return new ActionId(value);
  }

  @Override
  public int compareTo(ActionId other) {
    return value.compareTo(other.value);
  }

  @Override
  public String toString() {
    return value;
  }
}
