# Abschlussbericht Phase 14

Datum: 30. Juli 2026

## Implementiert

- Installierte Pacman-Pakete und Repository-Suche
- Paketdetails mit Version, Beschreibung, Architektur, Größe und Abhängigkeiten
- Verfügbare Updates und verwaiste Pakete
- Begrenzte Cachegrößenanalyse
- Erkennung von `/var/lib/pacman/db.lck`
- Serielle Hintergrundausführung, Fortschrittszustände und 60-Sekunden-Cache
- Installations- und Entfernungsvorschau mit Änderungsliste und Größen
- Höchstens zwei Minuten gültige Vorschau-ID und bewusster Bestätigungsdialog
- Typisierter D-Bus-Aufruf des privilegierten Helpers
- Erneute Paketnamen- und Lock-Prüfung innerhalb des Helpers

## Sicherheit und Datenbankkonsistenz

- Suchtext und Paketname werden vor dem Prozessstart per Allowlist geprüft.
- Pacman erhält Argumente einzeln; es gibt keinen Shell-Interpreter.
- Lesende Prozesse besitzen Timeout, begrenzte Ausgabe und `LC_ALL=C`.
- Kein Mutationsaufruf ist ohne vorherige Pacman-Vorschau möglich.
- Vorschau-IDs sind einmalig, nur einmal verwendbar und laufen nach zwei Minuten ab.
- Ein Pacman-Lock stoppt Vorschau, Bestätigung und als letzte Grenze den Helper.
- Nur die Package-Action und der validierte Paketname passieren die D-Bus-Grenze.

## Tests

- Mutationen erreichen den Gateway erst nach Vorschau und Bestätigung.
- Lock-Erkennung stoppt sowohl Vorschau als auch spätere Bestätigung.
- Shell-Metazeichen erreichen weder Backend noch Gateway.
- Pacman-Suchausgabe wird unabhängig von der Desktop-Sprache geparst.
- Vorschauargumente bleiben getrennt und enthalten keinen Shell-Aufruf.
- Fehlendes Pacman liefert einen ehrlichen, stabilen UI-Zustand.

## Einschränkungen

- AUR ist optional und in dieser Version deaktiviert.
- Repositorydaten werden nicht ungefragt synchronisiert; die Anzeige nutzt den vorhandenen
  Pacman-Datenbankstand.
- Eine echte Paketmutation wird erst nach Installation von Helper, D-Bus- und Polkit-Dateien auf
  CachyOS im Phase-24/25-Zieltest ausgeführt.

## Nächster Schritt

Phase 15 bündelt Firewall, Ports, SSH, fehlgeschlagene Logins, Sicherheitsupdates, Secure Boot,
AppArmor und Dateirechte in einer nachvollziehbaren Sicherheitsansicht.
