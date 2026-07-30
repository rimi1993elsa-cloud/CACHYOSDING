# Abschlussbericht Phase 3

Datum: 30. Juli 2026

## Implementiert

- Unveränderliches `SystemSnapshot`-Datenmodell mit klar getrennten Teilmodellen
- Parser für `/etc/os-release` einschließlich CachyOS-Erkennung
- Dynamische Kernel-, Desktop-, Sitzungs-, CPU-, GPU- und RAM-Erkennung
- Speichererkennung über Java FileStore ohne externe Prozesse
- Akkuerkennung über `/sys/class/power_supply` mit sicherem Fallback
- Netzwerkstatus über Java NetworkInterface ohne private Adressen im Snapshot
- Erkennung von systemd-boot, Limine, GRUB und rEFInd anhand lokaler Systempfade
- Zentrale `CapabilityRegistry` für die im Masterauftrag geforderten Werkzeuge
- Verständliche Gründe und Installationshinweise für fehlende Capabilities
- Reale Systemansicht und Übersichtskarten mit scrollbarem Inhalt

## Geänderte Dateien

- Systemmodelle, Parser und Detektoren in `system-info`
- Bootstrap und Anwendungskontext in `app`
- Übersicht und Systemseite in `ui`
- README, Changelog, Modulstatus und Benutzerhandbuch

## Tests

- Capability-Erkennung mit kontrolliertem Suchpfad
- Parsing einer CachyOS-`os-release`-Datei
- Snapshot-Erzeugung und unveränderliche Capability-Daten
- Fallbacks für fehlende Betriebssystemdateien
- Vollständiger Build, Spotless und Checkstyle

Gesamtstand: 27 Tests, 0 Fehler, 0 übersprungen.

## Manuelle Prüfung

Die JavaFX-Anwendung wurde auf dem Windows-Entwicklungsrechner gestartet und lief nach zehn
Sekunden stabil. Der Composition Root erzeugt den Snapshot vor dem Anzeigen der Oberfläche; die
Systemseite lässt sich ohne Linux-Dateien und ohne optionale Werkzeuge aufbauen. Die konkrete
CachyOS-, KDE-/Wayland-, Akku-, GPU- und Bootmanager-Erkennung ist auf dem Zielgerät anhand des
Testplans praktisch abzunehmen.

## Sicherheitsprüfung

- Systemerkennung ist ausschließlich lesend und fordert keine erhöhten Rechte an.
- Es werden keine Shellstrings oder frei zusammengesetzten Befehle ausgeführt.
- Netzwerkadressen und Geräteseriennummern werden nicht in den Snapshot aufgenommen.
- Fehlende oder unlesbare Dateien führen zu definierten Fallbacks.
- Capabilities werden nur über feste Namen in kontrollierten Suchpfaden erkannt.

## Bekannte Einschränkungen

- Auf dem Windows-Entwicklungsrechner können Linux-spezifische Werte nur über Fallbacks geprüft
  werden.
- GPU-Modellnamen hängen ohne `lspci` von den verfügbaren sysfs-Daten ab.
- Die Werte sind in Phase 3 ein Start-Snapshot; lastarme Aktualisierung folgt mit dem Dashboard.

## Nächster Schritt

Phase 4 baut ein dauerhaft nutzbares Dashboard mit lastarmen Aktualisierungen, Warnungen,
Update- und Dienstestatus sowie Verlauf der letzten Aktionen.
