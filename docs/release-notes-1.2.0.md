# CachyOS Control Center AI 1.2.0

Version 1.2 ist der erste direkt auf CachyOS installier- und prüfbare Release Candidate.

## Produktreife

- Einstellungen, opt-in Chatverlauf, Audit-Metadaten und KI-Verbrauch liegen in SQLite.
- Bestehende JSON-Daten aus Version 1.1 werden beim ersten Start übernommen.
- Der OpenAI-Schlüssel kann in der App sicher über KWallet/Secret Service gespeichert und gelöscht
  werden; er wird nie exportiert oder in SQLite geschrieben.
- Provider und Modellwahl werden ohne Neustart für die nächste Anfrage aktualisiert.
- Die von der Responses API gemeldeten Ein- und Ausgabetokens werden lokal protokolliert. Das
  USD-Monatslimit stoppt weitere Anfragen, sobald die lokale Schätzung erreicht ist.
- Ein Systemcheck zeigt Distribution, verfügbare Werkzeuge und den lokalen Datenbankpfad.

Die Kostenschätzung verwendet die am 31. Juli 2026 dokumentierten Texttokenpreise für
[GPT-5.6 Sol](https://developers.openai.com/api/docs/models/gpt-5.6-sol),
[GPT-5.6 Terra](https://developers.openai.com/api/docs/models/gpt-5.6-terra) und
[GPT-5.6 Luna](https://developers.openai.com/api/docs/models/gpt-5.6-luna). Sie ist keine
serverseitige Ausgabensperre und enthält keine möglichen Tool-Gebühren.

## Installation auf CachyOS

```bash
./scripts/install-cachyos.sh
cachyos-control-center-verify
cachyos-control-center
```

`scripts/verify-linux.sh` führt Build, Tests, Qualitätsprüfung, Prüfsummenvalidierung,
Metadatenvalidierung und Sicherheitsgrep aus. Die tatsächliche Mikrofon-, KDE-, Polkit- und
Hardwareabnahme bleibt bewusst auf dem Zielsystem.
