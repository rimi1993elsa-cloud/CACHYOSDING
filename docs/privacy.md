# Datenschutz

## Grundsätze

- Systemdaten und Diagnosen werden lokal ermittelt.
- Mikrofonzugriff erfolgt nur während gedrücktem Push-to-Talk; Audiodaten werden nicht gespeichert.
- Online-KI ist optional und besitzt keine Fähigkeit, lokale Aktionen auszuführen.
- Dokumentation, Diagnose, Hardware und Systemkontext haben getrennte Freigaben.
- Chatverlauf ist standardmäßig aus und bei Opt-in auf 200 Einträge begrenzt.
- API-Keys bleiben in Secret Service/KWallet und fehlen im JSON-Export.

Vor jeder Online-Anfrage zeigt die Chatseite den freigegebenen Kontext. Abgerufene Dokumente gelten
als nicht vertrauenswürdig, werden auf Prompt-Injection-Marker geprüft und nur als zitierter
Lesekontext verwendet. Offizielle Wissensquellen werden ausschließlich nach einem bewussten Klick
aktualisiert.

## Lokale Speicherorte

```text
$XDG_DATA_HOME/cachyos-control-center/cachyos-control-center.sqlite3
$XDG_CACHE_HOME/cachyos-control-center/
$XDG_DATA_HOME/cachyos-control-center/
```

Die SQLite-Datenbank enthält Einstellungen, bewusst aktivierten Chatverlauf, parameterfreie
Audit-Metadaten und lokale KI-Tokenstatistiken. Bestehende JSON-Einstellungen und JSON-Verläufe
werden beim ersten Start atomar importiert und anschließend entfernt. API-Schlüssel befinden sich
nie in dieser Datenbank.

„Lokale persönliche Daten löschen“ entfernt Einstellungen, Verlauf, Verbrauch und Setup-Status aus
der Datenbank. Cache und optionale Sprachmodelle können separat im jeweiligen XDG-Verzeichnis
gelöscht werden. Eine Paketdeinstallation löscht Benutzerdaten absichtlich nicht.
