# Sicherheitsrichtlinie

## Unterstützte Versionen

Das Projekt befindet sich vor Version 1.0. Sicherheitskorrekturen werden nur für den aktuellen
Entwicklungsstand bereitgestellt.

## Schwachstellen melden

Bitte Sicherheitsprobleme nicht als öffentliches Issue veröffentlichen. Nutze die private
Security-Advisory-Funktion des Repositorys oder kontaktiere die Maintainer über den dort
angegebenen privaten Kanal. Eine Meldung sollte Reproduktionsschritte, Auswirkungen und – soweit
möglich – eine sichere Testumgebung enthalten. Keine echten API-Schlüssel, Passwörter oder
personenbezogenen Logs mitsenden.

## Verbindliche Grenzen

- Die JavaFX-Anwendung läuft als normaler Benutzer.
- Freier Shelltext wird niemals ausgeführt.
- Administrative Aktionen dürfen später ausschließlich über typisierte, erneut validierte
  D-Bus-/Polkit-Methoden erfolgen.
- Die Online-KI erhält keine Referenz auf lokale Executor- oder Dispatcher-Komponenten.
- Secrets werden weder in Git noch SQLite oder Logs gespeichert.
- Telemetrie und Mikrofonaufzeichnung erfolgen niemals heimlich.

Details: [`docs/threat-model.md`](docs/threat-model.md).

