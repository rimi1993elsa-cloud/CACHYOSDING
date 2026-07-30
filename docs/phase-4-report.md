# Abschlussbericht Phase 4

Datum: 30. Juli 2026

## Implementiert

- Unveränderliches `DashboardMetrics`-Modell mit expliziten Unbekannt-Zuständen
- Hintergrundmonitor mit 30-Sekunden-Intervall und kontrolliertem Lifecycle
- CPU-, RAM-, Systemspeicher-, Netzwerk- und Akkuanzeige
- Fünf-Minuten-Cache für langsam veränderliche Statusabfragen
- Fester read-only Aufruf von `pacman -Qu`
- Fester read-only Aufruf von `systemctl --failed`
- Acht-Sekunden-Timeout und begrenzte Prozessausgabe
- Warnungen für Offlinezustand, niedrigen Akku, knappen Speicher, Updates und Dienste
- Anzeige der letzten fünf lokalen Audit-Ereignisse
- Direkt sichtbare Schnellaktionen und scrollbares Dashboard

## Geänderte Dateien

- Dashboard-Modelle, Datenquelle und Monitor in `system-info`
- Linux-Statusprobe und sicherer Prozessleser in `platform-linux`
- Composition Root und Lifecycle in `app`
- Dashboard-Komponente und Shell-Integration in `ui`
- README, Changelog, Modulstatus und Benutzerhandbuch

## Tests

- Warnungsbildung aus realen Schwellenwerten
- Expliziter Unbekannt-Zustand für nicht messbare CPU-Last
- Zählen nichtleerer Paket- und Dienstzeilen
- Weiterhin funktionierende Navigation und feste Schnellaktionen
- Vollständiger Build, Spotless und Checkstyle

Gesamtstand: 30 Tests, 0 Fehler, 0 übersprungen.

## Manuelle Prüfung

Die Anwendung wurde auf dem Windows-Entwicklungsrechner gestartet. Dashboard und Hintergrundmonitor
laufen ohne Linux-Werkzeuge weiter und zeigen den Update- und Dienstestatus ehrlich als nicht
ermittelbar. Die praktische Anzeige von `pacman`-Updates und fehlgeschlagenen systemd-Diensten ist
auf CachyOS anhand des Testplans abzunehmen.

## Sicherheitsprüfung

- Nur absolute, durch die Capability Registry ermittelte Executables werden gestartet.
- Argumente sind fest im Adapter hinterlegt und enthalten keine Benutzereingaben.
- Es gibt keinen Shellstring und keine erhöhten Rechte.
- Prozesslaufzeit und Ausgabe sind begrenzt.
- Fehlgeschlagene Abfragen werden nicht als Null-Ergebnis ausgegeben.
- Der Monitor arbeitet außerhalb des JavaFX-Threads und wird beim Beenden geschlossen.

## Bekannte Einschränkungen

- Die Updatezahl umfasst in dieser Phase nur offizielle Pacman-Repositories, nicht AUR.
- Audit-Ereignisse erscheinen spätestens mit dem nächsten 30-Sekunden-Refresh.
- Detailansichten und Aktionen für Netzwerk, Pakete und Dienste folgen in ihren Fachphasen.

## Nächster Schritt

Phase 5 implementiert den NetworkManager-Adapter, Verbindungsübersicht, WLAN-Steuerung,
VPN-Anzeige und sichere Netzwerkdiagnose mit Offline-Fallback.
