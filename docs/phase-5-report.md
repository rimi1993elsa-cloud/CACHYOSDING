# Abschlussbericht Phase 5

Datum: 30. Juli 2026

## Implementiert

- Sanitisiertes Netzwerk-Snapshot-Modell ohne Secrets
- Fachmodul und schmale `NetworkBackend`-Schnittstelle
- NetworkManager-Adapter mit absolut erkanntem `nmcli`
- Geräte-, Verbindungs-, WLAN-, Profil- und VPN-Übersicht
- Gateway- und DNS-Anzeige
- WLAN ein/aus und manueller WLAN-Scan
- Aktivierung gespeicherter Profile anhand validierter UUID
- Trennung eines Geräts anhand validierten Schnittstellennamens
- Ereignisbasierte Aktualisierung über `nmcli monitor`
- Acht-Sekunden-Timeout und begrenzte Ausgabe für Einzelabfragen
- Verständlicher Offline- und fehlende-Capability-Fallback
- JavaFX-Netzwerkseite mit vollständig asynchronen Systemzugriffen

## Geänderte Dateien

- Action-IDs in `core`
- Netzwerkmodelle, Modul und Validatoren in `modules/network`
- `nmcli`-Adapter, Parser und Eventmonitor in `platform-linux`
- Composition Root und Lifecycle in `app`
- Netzwerkseite und Navigation in `ui`
- Architektur-, Aktions-, Modul- und Benutzerdokumentation

## Tests

- Escaped-`nmcli`-Felder mit Doppelpunkten
- Ablehnung fehlerhafter WLAN-Zeilen
- Zulässige Profil-UUID
- Shell-Metazeichen im Gerätenamen werden vor dem Backend abgelehnt
- Navigation zur real implementierten Netzwerkseite
- Vollständiger Build, Spotless und Checkstyle

Gesamtstand: 35 Tests, 0 Fehler, 0 übersprungen.

## Manuelle Prüfung

Die Anwendung wurde auf dem Windows-Entwicklungsrechner gestartet. Ohne `nmcli` zeigt die
Netzwerkseite einen stabilen, verständlichen Fallback und deaktiviert nicht mögliche Aktionen.
Geräte-, WLAN-, VPN- und Ereignisdaten sowie echte Aktionen sind auf CachyOS mit NetworkManager
anhand des Testplans praktisch abzunehmen.

## Sicherheitsprüfung

- Keine Shell und keine frei zusammengesetzten Befehle
- `nmcli` muss als absolute Executable von der Capability Registry stammen
- Profil-UUID und Gerätename werden lokal gegen enge Muster validiert
- Passwörter werden weder abgefragt noch gespeichert, geloggt oder als Argument übergeben
- Netzwerk-Snapshots enthalten keine WLAN-Passwörter oder privaten Dateiinhalte
- Nur gespeicherte Profile werden aktiviert; Secrets bleiben beim NetworkManager/KDE Secret Agent
- Eventprozess wird beim Anwendungsende kontrolliert beendet

## Bekannte Einschränkungen

- Neue passwortgeschützte WLAN-Profile werden sicher über KDE/NetworkManager angelegt, nicht in der
  Anwendung.
- Statische IP-Konfiguration und Profilbearbeitung folgen einer späteren Erweiterung.
- Die reale NetworkManager-Integration kann auf dem Windows-Rechner nicht abgenommen werden.

## Nächster Schritt

Phase 6 implementiert PipeWire-/WirePlumber-Geräte, Lautstärke, Mute, Mikrofon,
Standardgeräte, Streams, Testton und Dienststatus.
