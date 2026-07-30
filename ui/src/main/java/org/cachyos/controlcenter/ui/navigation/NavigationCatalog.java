package org.cachyos.controlcenter.ui.navigation;

import java.util.List;

/** Ordered catalog for the sidebar. */
public final class NavigationCatalog {
  private final List<NavigationEntry> entries =
      List.of(
          active(NavigationId.OVERVIEW, "Übersicht", "Aktueller lokaler Status"),
          active(NavigationId.SYSTEM, "System", "Sichere Plattforminformationen"),
          planned(NavigationId.SECURITY, "Sicherheit", "Sicherheitsstatus", 15),
          active(NavigationId.NETWORK, "Netzwerk", "Verbindungen und Diagnose"),
          active(NavigationId.APPLICATIONS, "Programme", "Installierte Anwendungen"),
          active(NavigationId.VOICE, "Sprache", "Offline Push-to-Talk"),
          planned(NavigationId.PACKAGES, "Pakete", "Pacman und optionale AUR-Pakete", 14),
          planned(NavigationId.HARDWARE, "Hardware", "Geräte und Sensoren", 16),
          planned(NavigationId.STORAGE, "Speicher", "Laufwerke und Dateisysteme", 17),
          planned(NavigationId.SNAPSHOTS, "Snapshots", "Btrfs- und Snapper-Verwaltung", 17),
          active(NavigationId.AUDIO, "Audio", "PipeWire-Geräte und Streams"),
          planned(NavigationId.DISPLAY, "Anzeige", "Monitore und Grafik", 19),
          planned(NavigationId.POWER, "Energie", "Akku und Energieprofile", 19),
          planned(NavigationId.SERVICES, "Dienste", "Systemd-Units", 18),
          planned(NavigationId.PROCESSES, "Prozesse", "Laufende Prozesse", 18),
          planned(NavigationId.DIAGNOSTICS, "Diagnose", "Lokale Systemdiagnose", 12),
          active(NavigationId.AI_ASSISTANT, "KI-Assistent", "Optionale Online-Hilfe"),
          active(NavigationId.SETTINGS, "Einstellungen", "Lokale Darstellung"));

  public List<NavigationEntry> entries() {
    return entries;
  }

  private static NavigationEntry active(NavigationId id, String label, String description) {
    return new NavigationEntry(id, label, description, true, "Verfügbar");
  }

  private static NavigationEntry planned(
      NavigationId id, String label, String description, int phase) {
    return new NavigationEntry(id, label, description, false, "Phase " + Integer.toString(phase));
  }
}
