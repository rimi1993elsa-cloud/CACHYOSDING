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
$XDG_CONFIG_HOME/cachyos-control-center/settings.json
$XDG_CONFIG_HOME/cachyos-control-center/chat-history.json
$XDG_CONFIG_HOME/cachyos-control-center/setup-v1.complete
$XDG_CACHE_HOME/cachyos-control-center/
$XDG_DATA_HOME/cachyos-control-center/
```

„Lokale persönliche Daten löschen“ entfernt Einstellungen, Verlauf und Setup-Marker. Cache und
optionale Sprachmodelle können separat im jeweiligen XDG-Verzeichnis gelöscht werden. Eine
Paketdeinstallation löscht Benutzerdaten absichtlich nicht.
