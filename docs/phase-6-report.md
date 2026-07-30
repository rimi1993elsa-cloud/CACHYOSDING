# Abschlussbericht Phase 6

Datum: 30. Juli 2026

## Implementiert

- Sanitisiertes Audio-Snapshot-Modell für Geräte und Wiedergabestreams
- Fachmodul und schmale `AudioBackend`-Schnittstelle
- Strukturierte `pactl -f json`-Abfragen für Server, Ausgaben, Eingaben und Streams
- Durchschnittliche Kanallautstärke ohne lokalisierungsabhängiges Textparsing
- Ausgabe- und Mikrofonlautstärke von 0 bis 150 Prozent
- Mute/Unmute für Ausgabe und Mikrofon
- Auswahl von Standardausgabe und Standardmikrofon
- Optionaler Testton über absolutes `pw-play` und feste Systemklangdatei
- Ereignisbasierte Aktualisierung über `pactl subscribe`
- JavaFX-Audioseite mit asynchronen Systemzugriffen
- Capability- und Dienst-Fallback ohne Absturz

## Geänderte Dateien

- Audio-Action-IDs in `core`
- Audiomodelle, Modul und Validatoren in `modules/audio`
- `pactl`-Adapter, JSON-Parser und Eventmonitor in `platform-linux`
- `pw-play`-Capability in `system-info`
- Composition Root und Lifecycle in `app`
- Audioseite und Navigation in `ui`
- Build- und Benutzerdokumentation

## Tests

- Stereo-Kanallautstärke aus strukturiertem JSON
- Sicherer Fallback bei fehlerhaften Lautstärkefeldern
- Zulässige Lautstärkewerte und Geräte
- Injection im Gerätenamen wird vor dem Backend abgelehnt
- Navigation zur real implementierten Audioseite
- Vollständiger Build, Spotless und Checkstyle

Gesamtstand: 40 Tests, 0 Fehler, 0 übersprungen.

## Manuelle Prüfung

Die Anwendung wurde auf dem Windows-Entwicklungsrechner gestartet. Ohne PipeWire/`pactl` zeigt die
Audioseite den erklärten Fallback und führt keine Mixeraktion aus. Mehrere reale Audio- und
Mikrofongeräte, Streams, Testton und Ereignisse sind auf CachyOS anhand des Testplans abzunehmen.

## Sicherheitsprüfung

- Kein Audio-Capture und keine dauerhafte Mikrofonverarbeitung
- Keine Shell und keine frei zusammengesetzten Befehle
- Gerätebezeichner, Lautstärke und Boolean-Werte werden streng validiert
- Nur absolute, erkannte Executables werden gestartet
- Testton verwendet eine feste Datei unter `/usr/share/sounds`
- Acht-Sekunden-Timeout und begrenzte Ausgabe
- Der Eventprozess wird beim Anwendungsende beendet

## Bekannte Einschränkungen

- Die vollständige Geräteansicht benötigt die PipeWire-Pulse-Kompatibilität und `pactl`; bei einem
  reinen `wpctl`-System bleibt die Seite erklärend deaktiviert.
- Individuelle Stream-Lautstärke und Profil-/Portumschaltung folgen einer Erweiterung.
- Die reale PipeWire-Integration kann auf dem Windows-Rechner nicht abgenommen werden.

## Nächster Schritt

Phase 7 implementiert die sichere Erkennung, Suche, Favoriten, Paketzuordnung und den direkten Start
installierter `.desktop`-Anwendungen.
