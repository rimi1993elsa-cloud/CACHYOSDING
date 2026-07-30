# Abschlussbericht Phase 12

Datum: 30. Juli 2026

## Implementiert

- Asynchrones lokales Diagnoseframework mit immutable Report
- Netzwerkprobe über feste `nmcli`-Argumente
- Audioprobe über festen `pactl info`-Aufruf
- Dienstprobe über `systemctl --failed`
- Prozessfreies Lesen der Kernel-Bootparameter
- Grafikprobe über feste `lspci`-Argumente
- Pacman-Datenbankprüfung über `pacman -Dk`
- Capability-basierte, verständliche Nichtverfügbarkeitszustände
- Isolierung defekter Einzelprobes
- Zentrale Redaction für E-Mail, Home-Pfade, private IPs, Secrets, Hostname, MAC, UUID und Serien
- Feste lokale Abhilfen für Netzwerk- und Audiowarnungen
- Optionale KI-Erklärung als bereinigter Entwurf mit separatem Sendeklick

## Geänderte Dateien

- Diagnosemodelle, Manager und Sanitizer in `modules/diagnostics`
- Feste Linux-Probes in `platform-linux`
- Diagnoseansicht, Navigation und UI-Tests in `ui`
- Composition Root und Lebenszyklus in `app`
- Datenschutz-, Architektur- und Benutzerdokumentation

## Tests

- Sämtliche Kategorien werden ausgeführt und zentral bereinigt
- Nur feste passende Action-IDs werden vorgeschlagen
- Linux- und Windows-Homepfade werden maskiert
- Eine defekte Probe bricht den Bericht nicht ab und leakt keine Exception
- Bootparameter werden ohne Prozess gelesen
- Fehlende optionale Werkzeuge liefern `UNAVAILABLE`
- Diagnose-Navigation enthält real nutzbaren Inhalt
- Vollständiger Build, Spotless und Checkstyle

Gesamtstand: 77 Tests, 0 Fehler, 0 übersprungen.

## Manuelle Prüfung

Die Anwendung wurde auf dem Windows-Entwicklungsrechner gestartet. Alle sechs Kategorien liefern
dort stabile Nichtverfügbarkeits- beziehungsweise prozessfreie Fallbacks. Die realen Linux-Probes
sind auf CachyOS gemäß Testplan mit und ohne die jeweiligen optionalen Werkzeuge abzunehmen.

## Sicherheitsprüfung

- Alle Prozesse verwenden feste Executables und getrennte, konstante Argumentlisten.
- Kein Diagnose- oder Nutzertext erreicht `ProcessBuilder`.
- Ausgabe, Laufzeit und Reportlänge sind begrenzt.
- Bootparameter werden nur aus einer regulären, nicht symbolischen `/proc`-Datei gelesen.
- Rohbefunde werden vor UI und KI zentral redigiert.
- KI-Erklärung ist optional, zweistufig und ohne Dispatcher-Bezug.
- Abhilfen stammen nur aus einem festen Category-to-Action-ID-Mapping.

## Bekannte Einschränkungen

- Die Windows-Entwicklungsumgebung kann die realen CachyOS-Probeausgaben nicht validieren.
- Diagnoseergebnisse werden bis zur Persistenzphase nicht dauerhaft gespeichert.
- Paketdiagnose prüft zunächst die lokale Pacman-Datenbankkonsistenz, keine Online-Repositories.
- Tiefere Grafik- und Bootanalyse wird mit den jeweiligen Fachmanagern erweitert.

## Nächster Schritt

Phase 13 implementiert den separaten privilegierten D-Bus/Polkit-Helper mit minimaler Allowlist,
doppelter Parameterprüfung, Timeout, Audit und Manipulationstests.
