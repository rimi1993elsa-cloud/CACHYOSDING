# CachyOS Control Center AI

Eine sichere, deutschsprachige Verwaltungszentrale für CachyOS mit JavaFX. Das Projekt wird
phasenweise nach dem verbindlichen Masterauftrag entwickelt.

## Aktueller Stand

Phase 1 stellt ein reproduzierbares Gradle-Multi-Modul-Projekt und eine responsive JavaFX-Shell mit
Navigation, Themes, Toasts, XDG-Pfaden, rotierendem Logging und sicherer, rein lesender
Plattform-Erkennung bereit. Es sind noch keine Systemaktionen oder Manager-Funktionen
freigeschaltet.

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

Die aktuelle Oberfläche zeigt ausschließlich reale JVM-/Sitzungsdaten. Sie führt keine Befehle aus,
liest keine Systemdateien und fordert keine erhöhten Rechte an.

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
