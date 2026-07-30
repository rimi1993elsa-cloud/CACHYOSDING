package org.cachyos.controlcenter.systeminfo;

import java.util.List;

/** Known optional tools. Names are fixed and never derived from user input. */
public enum Capability {
  NMCLI("NetworkManager", List.of("nmcli"), "networkmanager"),
  WPCTL("PipeWire wpctl", List.of("wpctl"), "wireplumber"),
  PACTL("PulseAudio-Kompatibilität", List.of("pactl"), "libpulse"),
  AUDIO_TEST_PLAYER("PipeWire Testton", List.of("pw-play"), "pipewire-audio"),
  PACMAN("Pacman", List.of("pacman"), "pacman"),
  PARU("Paru", List.of("paru"), "paru"),
  SYSTEMCTL("Systemd", List.of("systemctl"), "systemd"),
  JOURNALCTL("Systemjournal", List.of("journalctl"), "systemd"),
  UPOWER("UPower", List.of("upower"), "upower"),
  SENSORS("Hardware-Sensoren", List.of("sensors"), "lm_sensors"),
  SMARTCTL("SMART", List.of("smartctl"), "smartmontools"),
  BTRFS("Btrfs", List.of("btrfs"), "btrfs-progs"),
  SNAPPER("Snapper", List.of("snapper"), "snapper"),
  LSBLK("Blockgeräte", List.of("lsblk"), "util-linux"),
  LSPCI("PCI-Geräte", List.of("lspci"), "pciutils"),
  LSUSB("USB-Geräte", List.of("lsusb"), "usbutils"),
  LOGINCTL("Sitzungsverwaltung", List.of("loginctl"), "systemd"),
  FIREWALLD("Firewalld", List.of("firewall-cmd"), "firewalld"),
  UFW("UFW", List.of("ufw"), "ufw"),
  KDE_DBUS("KDE D-Bus", List.of("qdbus6", "qdbus"), "qt6-tools"),
  KDE_SYSTEM_SETTINGS("KDE Systemeinstellungen", List.of("systemsettings"), "systemsettings"),
  CACHYOS_KERNEL_MANAGER(
      "CachyOS Kernel Manager", List.of("cachyos-kernel-manager"), "cachyos-kernel-manager"),
  CACHYOS_HELLO("CachyOS Hello", List.of("cachyos-hello"), "cachyos-hello");

  private final String displayName;
  private final List<String> executableNames;
  private final String installHint;

  Capability(String displayName, List<String> executableNames, String installHint) {
    this.displayName = displayName;
    this.executableNames = executableNames;
    this.installHint = installHint;
  }

  public String displayName() {
    return displayName;
  }

  public List<String> executableNames() {
    return executableNames;
  }

  public String installHint() {
    return installHint;
  }
}
