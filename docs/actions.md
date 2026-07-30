# Aktionsmodell

## Datenfluss

```text
Direkter Button
→ feste ActionId
→ unveränderlicher ActionRequest
→ ActionRegistry-Allowlist
→ DefaultActionDispatcher auf Hintergrund-Executor
→ registriertes ManagerModule
→ strukturierter Plattformadapter
→ ActionResult und parameterfreies Audit
```

`InputSource` enthält ausschließlich `BUTTON`, `TEXT`, `VOICE` und
`INTERNAL_SCHEDULED_REFRESH`. Eine KI ist bewusst keine Aktionsquelle.

## Aktuelle Aktionen

| Action-ID | Wirkung | Privilegiert | Parameter |
|---|---|---:|---|
| `desktop.open-firefox` | Firefox starten | nein | keine |
| `desktop.open-file-manager` | Dateimanager im Home öffnen | nein | keine |
| `desktop.open-terminal` | fest erkannten Terminalemulator starten | nein | keine |
| `desktop.lock-screen` | Sitzungssperre anfordern | nein | keine |
| `network.scan-wifi` | WLAN-Suche aktualisieren | nein | keine |
| `network.wifi-on` | WLAN einschalten | nein | keine |
| `network.wifi-off` | WLAN ausschalten | nein | keine |
| `network.activate-profile` | gespeichertes Profil aktivieren | nein | validierte UUID |
| `network.disconnect-device` | Netzwerkgerät trennen | nein | validierter Gerätename |
| `audio.set-output-volume` | Ausgabelautstärke setzen | nein | Gerät, 0–150 |
| `audio.set-input-volume` | Mikrofonlautstärke setzen | nein | Gerät, 0–150 |
| `audio.set-output-mute` | Ausgabe stumm/aktiv setzen | nein | Gerät, Boolean |
| `audio.set-input-mute` | Mikrofon stumm/aktiv setzen | nein | Gerät, Boolean |
| `audio.set-default-output` | Standardausgabe setzen | nein | Gerät |
| `audio.set-default-input` | Standardmikrofon setzen | nein | Gerät |
| `audio.test-tone` | festen Systemtestton abspielen | nein | keine |
| `applications.launch` | katalogisierte Desktop-Anwendung starten | nein | validierte App-ID |

Unbekannte IDs werden vor jedem Handler abgelehnt und auditiert. Die Desktop-Aktionen weisen
sämtliche Parameter zurück. Prozesse erhalten eine absolute Executable und eine getrennte
Argumentliste; ein Shellinterpreter wird nicht verwendet. Netzwerkaktionen akzeptieren exakt ihre
definierten Parameter. UUIDs und Gerätenamen werden vor dem Plattformadapter validiert.

## Audit

Phase 2 speichert Auditereignisse threadsicher im Speicher. Ein Eintrag enthält Zeitpunkt,
Action-ID, Eingabequelle, Ergebnis, Dauer und Privilegstatus. Parameter und Exception-Nachrichten
werden nicht gespeichert. Persistenz folgt in einer späteren Phase.
