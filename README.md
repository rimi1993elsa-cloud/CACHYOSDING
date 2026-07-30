# CachyOS Control Center AI

Eine sichere, deutschsprachige Verwaltungszentrale für CachyOS mit JavaFX. Das Projekt wird
phasenweise nach dem verbindlichen Masterauftrag entwickelt.

## Aktueller Stand

Phase 2 stellt eine responsive JavaFX-Shell und ein typisiertes, allowlist-basiertes Action
Framework bereit. Vier unprivilegierte Schnellaktionen können Firefox, Dateimanager und Terminal
öffnen oder die Bildschirmsperre anfordern. Nicht verfügbare Programme werden ehrlich gemeldet;
Manager-Funktionen sind noch nicht freigeschaltet.

## Voraussetzungen

- JDK 21
- Linux für die Zielanwendung; Entwicklung und Unit-Tests sind auch auf anderen Plattformen möglich
- Git

Gradle wird über den eingecheckten Wrapper bereitgestellt.

## Bauen und testen

```bash
./gradlew build
./gradlew quality
```

Unter Windows:

```powershell
.\gradlew.bat build
.\gradlew.bat quality
```

## Anwendung starten

```bash
./gradlew :app:run
```

Die aktuelle Oberfläche zeigt reale JVM-/Sitzungsdaten. Schnellbuttons senden ausschließlich feste
Action-IDs an den lokalen Dispatcher. Es gibt keine freie Shellausführung und keine erhöhten Rechte.

## Projektmodule

| Modul | Verantwortung |
|---|---|
| `app` | Bootstrap, JavaFX-Lebenszyklus und Composition Root |
| `core` | Gemeinsame Kernmodelle und XDG-Infrastruktur |
| `ui` | JavaFX-Komponenten; in Phase 0 nur das Startfenster |
| `input` | Spätere Text-, Voice- und Intent-Grenze |
| `ai` | Spätere, strikt nur lesende KI-Grenze |
| `system-info` | Sichere Plattform- und später Systemerkennung |
| `platform-linux` | Spätere typisierte Linux-Adapter |
| `persistence` | Spätere SQLite-Persistenz |
| `modules/*` | Fachmodule der nachfolgenden Phasen |

Architektur, Sicherheitsgrenzen und Entwicklungsablauf stehen unter [`docs/`](docs/).

## Lizenz

GPL-3.0-or-later, siehe [`LICENSE`](LICENSE).
