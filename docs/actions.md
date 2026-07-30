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

Unbekannte IDs werden vor jedem Handler abgelehnt und auditiert. Die Desktop-Aktionen weisen
sämtliche Parameter zurück. Prozesse erhalten eine absolute Executable und eine getrennte
Argumentliste; ein Shellinterpreter wird nicht verwendet.

## Audit

Phase 2 speichert Auditereignisse threadsicher im Speicher. Ein Eintrag enthält Zeitpunkt,
Action-ID, Eingabequelle, Ergebnis, Dauer und Privilegstatus. Parameter und Exception-Nachrichten
werden nicht gespeichert. Persistenz folgt in einer späteren Phase.
