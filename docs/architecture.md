# Architektur

## Ziel

Die Anwendung trennt Anzeige, lokale Aktionen, privilegierte Aktionen und Online-KI technisch. In
Phase 6 existieren die unprivilegierte UI, sichere Systemerkennung, das Live-Dashboard, eine
allowlist-basierte lokale Action Engine sowie Fachgrenzen für NetworkManager und PipeWire-Pulse.

```mermaid
flowchart TB
    UI["JavaFX-App (normaler Benutzer)"]
    INFO["System-Info (nur lesend)"]
    ROUTER["Input Router (spätere Phase)"]
    ACTION["Typisierte Action Engine"]
    NETWORK["NetworkManager-Adapter (nmcli)"]
    HELPER["D-Bus/Polkit Helper (spätere Phase)"]
    AI["Online-KI (nur Text, keine Executor-Referenz)"]
    OS["Linux-Systemdienste"]

    UI --> INFO
    UI --> ACTION
    ACTION --> NETWORK
    NETWORK --> OS
    UI -. später .-> ROUTER
    ROUTER -. registrierte Action-ID .-> ACTION
    ROUTER -. Frage .-> AI
    ACTION -. Allowlist .-> HELPER
    HELPER -. feste Methoden .-> OS
    INFO -. bereinigter Kontext .-> AI
```

## Modulgrenzen

- `app` ist der Composition Root. Nur hier werden konkrete Implementierungen verbunden.
- `core` enthält plattformneutrale Modelle und Infrastruktur.
- `ui` kennt darstellbare, unveränderliche Daten, aber keine Prozess- oder Root-Schnittstelle.
- `system-info` liest lokale Daten und betreibt den lastarmen Dashboard-Monitor.
- `modules/network` enthält ausschließlich Netzwerkmodelle, Validierung und den Manager-Vertrag.
- `modules/audio` enthält ausschließlich Audiomodelle, Mixer-Validierung und den Manager-Vertrag.
- `ui` erhält den Netzwerkmanager als Fachschnittstelle und kennt weder `nmcli` noch Prozess-APIs.
- `platform-linux` enthält typisierte Adapter. Der aktuelle Prozessadapter akzeptiert eine absolute
  Executable und eine getrennte Argumentliste. `nmcli` wird nur über feste Argumentformen und
  validierte Bezeichner verwendet; freie Shell-Schnittstellen sind verboten.
- `ai` darf später weder `core.action`-Executor noch `platform-linux` oder den Helper als
  Abhängigkeit erhalten.
- `helper` ist kein Bestandteil des GUI-Prozesses und wird erst in Phase 13 implementiert.

## Nebenläufigkeit

Systemzugriffe und Netzwerkoperationen dürfen den JavaFX Application Thread nicht blockieren.
Spätere Adapter liefern abbrechbare asynchrone Ergebnisse; UI-Aktualisierungen werden auf den
JavaFX-Thread zurückgeführt. Polling wird capability- und sichtbarkeitsabhängig begrenzt.

## Konfiguration und Daten

`XdgPaths` bildet diese Basisverzeichnisse ab:

```text
$XDG_CONFIG_HOME/cachyos-control-center
$XDG_DATA_HOME/cachyos-control-center
$XDG_CACHE_HOME/cachyos-control-center
```

Fehlende oder relative XDG-Pfade fallen sicher auf die entsprechenden Verzeichnisse unter dem
Benutzer-Home zurück. Secrets gehören später in Secret Service/KDE Wallet, nie in SQLite.

## Versionsbasis (30. Juli 2026)

- Java 21 LTS, lokal geprüft mit Eclipse Temurin 21.0.12
- Gradle 9.6.1
- JavaFX 21.0.12
- JUnit 5.14.4
- Spotless 8.9.0
- Checkstyle 13.9.0
- Jackson 2.22.0
