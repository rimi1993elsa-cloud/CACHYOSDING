# Changelog

Alle nennenswerten Änderungen werden in dieser Datei dokumentiert. Das Format orientiert sich an
[Keep a Changelog](https://keepachangelog.com/de/1.1.0/).

## Unreleased

### Added

- Austauschbare Vosk-Speech-to-Text-Grenze mit deutschem 16-kHz-Mono-Profil
- Mikrofon- und lokale Modellwahl mit expliziter Verfügbarkeitsprüfung
- Push-to-Talk per Button oder Leertaste mit sichtbaren Teil- und Endtranskripten
- Aufnahme nur während aktiver Bedienung und ohne dauerhafte Audiospeicherung
- XDG-Anwendungsmanager mit Suche, Icons und Sitzungsfavoriten
- Sicherer Desktop-Exec-Parser mit Feldcodebehandlung und expliziter Shell-Ablehnung
- ID-only Anwendungsstart und verständliche Startfehler
- Bedarfsgesteuerte Paketzuordnung über `pacman -Qoq`
- PipeWire-Audiomanager für Ausgaben, Mikrofone, Standardgeräte und Streams
- Validierte Lautstärke- und Mute-Aktionen bis maximal 150 Prozent
- Ereignisbasierte Audioaktualisierung über `pactl subscribe`
- Optionaler Testton über fest erkanntes `pw-play` und eine feste Systemklangdatei
- Jackson 2.22.0 für strukturierte, lokalisierungsunabhängige Adapterdaten
- NetworkManager-Seite für Geräte, WLANs, Profile, VPNs, Gateway und DNS
- Ereignisbasierte Netzwerkaktualisierung über den festen `nmcli monitor`-Aufruf
- Validierte Aktionen für WLAN, Scan, gespeicherte Profile und Gerätetrennung
- Sicherer Offline- und Fehlende-`nmcli`-Fallback ohne Passwortverarbeitung
- Lastarm aktualisiertes Dashboard für CPU, RAM, Speicher, Netzwerk und Akku
- Gecachter Update- und fehlgeschlagener-Dienste-Status über feste read-only Argumentlisten
- Schwellenwertbasierte Warnungen und Anzeige der letzten lokalen Aktionen
- Schreibgeschützter System-Snapshot für Distribution, Kernel, Sitzung und Hardware
- Dynamische Erkennung von Speicher, Akku, Netzwerk und Bootmanager
- Zentrale Capability Registry mit Gründen und Installationshinweisen für fehlende Werkzeuge
- Reale, scrollbare Systemansicht mit sicheren Fallbacks
- Typisierte Action-Requests und -Results mit allowlist-basierter Registry
- Asynchroner Dispatcher mit Fehlerabbildung und parameterfreiem Audit
- Modulregistrierung und gemeinsame Manager-Schnittstelle
- Sichere Desktop-Schnellaktionen für Firefox, Dateimanager, Terminal und Bildschirmsperre
- Responsive Anwendungsshell mit Sidebar, Topbar, Status- und Eingabebereich
- Tastaturbedienbare Navigation mit ehrlicher Modulverfügbarkeit
- Light-, Dark- und System-Theme
- Wiederverwendbare Statuskarten und nicht blockierende Toast-Benachrichtigungen
- Gradle-Multi-Modul-Grundgerüst mit Java-21-Toolchain
- Minimales JavaFX-Hauptfenster
- Sichere Plattform-Erkennung ohne Prozess- oder Dateizugriff
- XDG-konforme Anwendungspfade
- JUnit 5, Spotless und Checkstyle
- Rotierendes, lokales Anwendungs-Logging
- Architektur-, Sicherheits- und Entwicklungsdokumentation
- GitHub-Actions-Build
