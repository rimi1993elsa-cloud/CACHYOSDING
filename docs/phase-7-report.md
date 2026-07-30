# Abschlussbericht Phase 7

Datum: 30. Juli 2026

## Implementiert

- Begrenzter XDG-Katalog aus Benutzer- und System-Anwendungsverzeichnissen
- Parser ausschließlich für den Abschnitt `[Desktop Entry]`
- Deutsche Namen und Kommentare mit Standard-Fallback
- Shell-freier Desktop-Exec-Tokenizer mit Double-Quote- und Escape-Unterstützung
- Entfernen nicht nutzbarer Desktop-Feldcodes
- Auflösung der Executable auf einen absoluten, ausführbaren Pfad
- Explizite Ablehnung von Shellinterpretern und `env`
- Ausschluss von Hidden-, NoDisplay-, Terminal- und Symlink-Einträgen
- Stabile SHA-256-basierte Anwendungs-ID
- Suche, verfügbare Icons und Sitzungsfavoriten
- ID-only Startaktion mit verständlicher Fehleranzeige
- Bedarfsgesteuerte Paketzuordnung über `pacman -Qoq`
- Asynchroner Katalog, Paketlookup und Start

## Geänderte Dateien

- Anwendungs-Action-ID in `core`
- Katalogmodelle, Modul und ID-Validierung in `modules/applications`
- Desktop-Parser, XDG-Katalog und Prozessstart in `platform-linux`
- Composition Root in `app`
- Programmseite und Navigation in `ui`
- Aktions-, Modul- und Benutzerdokumentation

## Tests

- Quoted Exec-Argumente und Desktop-Feldcodes
- Shellinterpreter werden abgelehnt
- Nur der richtige Desktop-Entry-Abschnitt wird gelesen
- App-ID akzeptiert nur exakt 16 Hexzeichen
- Traversal und Shellsyntax als ID werden abgelehnt
- Navigation zur real implementierten Programmseite
- Vollständiger Build, Spotless und Checkstyle

Gesamtstand: 46 Tests, 0 Fehler, 0 übersprungen.

## Manuelle Prüfung

Die Anwendung wurde auf dem Windows-Entwicklungsrechner gestartet. Ohne Linux-XDG-Systemkatalog
bleibt die Programmseite stabil und leer. Reale KDE-Icons, installierte Anwendungen, Pacman-
Zuordnung und Starts sind auf CachyOS anhand des Testplans praktisch abzunehmen.

## Sicherheitsprüfung

- Keine Shell und keine Interpretation von Suchtext oder Anwendungsname
- Start nur über eine zuvor katalogisierte, validierte ID
- Executable muss absolut und ausführbar sein
- Shellinterpreter, `env`, Symlinks und Terminal-Einträge werden abgelehnt
- Dateigröße und Kataloganzahl sind begrenzt
- Paketlookup erhält nur einen intern katalogisierten Desktop-Dateipfad
- Prozessausgabe wird verworfen; keine privaten Anwendungsdaten gelangen ins Audit

## Bekannte Einschränkungen

- Favoriten werden bis zur Persistenzphase nur für die laufende Sitzung gehalten.
- Feldcodes für übergebene Dateien und URLs werden entfernt; Dateiöffnen folgt einer sicheren,
  expliziten Übergabe-Erweiterung.
- Icons werden aus absoluten Pfaden sowie verbreiteten Hicolor-/Pixmaps-Pfaden geladen.
- Die reale CachyOS-/KDE-Integration kann auf dem Windows-Rechner nicht abgenommen werden.

## Nächster Schritt

Phase 8 implementiert die austauschbare Vosk-Speech-to-Text-Grenze, Modellverwaltung,
Mikrofonauswahl, Push-to-Talk und sichtbare Transkripte ohne dauerhafte Audioaufzeichnung.
