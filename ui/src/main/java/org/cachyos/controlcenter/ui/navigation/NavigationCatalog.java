package org.cachyos.controlcenter.ui.navigation;

import java.util.List;
import java.util.Set;

/** Ordered catalog for the sidebar. */
public final class NavigationCatalog {
  private final Set<String> enabledModules;
  private final List<NavigationEntry> entries =
      List.of(
          active(NavigationId.OVERVIEW, "Übersicht", "Aktueller lokaler Status"),
          active(NavigationId.SYSTEM, "System", "Sichere Plattforminformationen"),
          active(NavigationId.BOOT, "Boot & Kernel", "Bootanalyse und CachyOS-Kernel"),
          active(NavigationId.SECURITY, "Sicherheit", "Sicherheitsstatus"),
          active(NavigationId.NETWORK, "Netzwerk", "Verbindungen und Diagnose"),
          active(NavigationId.APPLICATIONS, "Programme", "Installierte Anwendungen"),
          active(NavigationId.VOICE, "Sprache", "Offline Push-to-Talk"),
          active(NavigationId.PACKAGES, "Pakete", "Pacman und optionale AUR-Pakete"),
          active(NavigationId.HARDWARE, "Hardware", "Geräte und Sensoren"),
          active(NavigationId.STORAGE, "Speicher", "Laufwerke und Dateisysteme"),
          active(NavigationId.SNAPSHOTS, "Snapshots", "Btrfs- und Snapper-Verwaltung"),
          active(NavigationId.AUDIO, "Audio", "PipeWire-Geräte und Streams"),
          active(NavigationId.DISPLAY, "Anzeige", "Monitore und Grafik"),
          active(NavigationId.POWER, "Energie", "Akku und Energieprofile"),
          active(NavigationId.SERVICES, "Dienste", "Systemd-Units"),
          active(NavigationId.PROCESSES, "Prozesse", "Laufende Prozesse"),
          active(NavigationId.DIAGNOSTICS, "Diagnose", "Lokale Systemdiagnose"),
          active(NavigationId.AI_ASSISTANT, "KI-Assistent", "Optionale Online-Hilfe"),
          active(NavigationId.SETTINGS, "Einstellungen", "Lokale Darstellung"));

  public NavigationCatalog() {
    this(Set.of());
  }

  public NavigationCatalog(Set<String> enabledModules) {
    this.enabledModules = Set.copyOf(enabledModules);
  }

  public List<NavigationEntry> entries() {
    if (enabledModules.isEmpty()) {
      return entries;
    }
    return entries.stream().map(this::configured).toList();
  }

  private NavigationEntry configured(NavigationEntry entry) {
    String key =
        switch (entry.id()) {
          case OVERVIEW, SETTINGS -> "";
          case AI_ASSISTANT -> "ai";
          case APPLICATIONS -> "applications";
          default -> entry.id().name().toLowerCase(java.util.Locale.ROOT);
        };
    return key.isEmpty() || enabledModules.contains(key)
        ? entry
        : new NavigationEntry(
            entry.id(), entry.label(), entry.description(), false, "In Einstellungen deaktiviert");
  }

  private static NavigationEntry active(NavigationId id, String label, String description) {
    return new NavigationEntry(id, label, description, true, "Verfügbar");
  }

  private static NavigationEntry planned(
      NavigationId id, String label, String description, int phase) {
    return new NavigationEntry(id, label, description, false, "Phase " + Integer.toString(phase));
  }
}
