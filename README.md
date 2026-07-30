# CachyOS Control Center AI

Eine sichere, deutschsprachige Verwaltungszentrale für CachyOS mit JavaFX. Das Projekt wird
phasenweise nach dem verbindlichen Masterauftrag entwickelt.

## Aktueller Stand

Phase 7 ergänzt einen sicheren XDG-Anwendungsmanager. Installierte `.desktop`-Anwendungen werden
durchsucht, mit Name, Kommentar und verfügbaren Icons angezeigt, gefiltert, favorisiert und über
eine katalogisierte ID gestartet. Exec-Felder werden ohne Shell in Argumente zerlegt; Shellstarter,
Symlinks und nicht ausführbare Programme werden abgelehnt. Pacman ordnet Pakete bei Bedarf lesend zu.

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

Die aktuelle Oberfläche zeigt reale lokale Systemdaten. Schnellbuttons senden ausschließlich feste
Action-IDs an den lokalen Dispatcher. Die Erkennung verwendet sichere Java- und Kernel-Schnittstellen;
es gibt keine freie Shellausführung und keine erhöhten Rechte.

## Projektmodule

| Modul | Verantwortung |
|---|---|
| `app` | Bootstrap, JavaFX-Lebenszyklus und Composition Root |
| `core` | Gemeinsame Kernmodelle und XDG-Infrastruktur |
| `ui` | JavaFX-Shell, Navigation, Statuskarten und Benachrichtigungen |
| `input` | Spätere Text-, Voice- und Intent-Grenze |
| `ai` | Spätere, strikt nur lesende KI-Grenze |
| `system-info` | Plattform-, Hardware-, Netzwerk-, Boot- und Capability-Erkennung |
| `platform-linux` | Spätere typisierte Linux-Adapter |
| `persistence` | Spätere SQLite-Persistenz |
| `modules/*` | Fachmodule der nachfolgenden Phasen |

Architektur, Sicherheitsgrenzen und Entwicklungsablauf stehen unter [`docs/`](docs/).

## Lizenz

GPL-3.0-or-later, siehe [`LICENSE`](LICENSE).
