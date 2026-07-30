# CachyOS Control Center AI

Eine sichere, deutschsprachige Verwaltungszentrale für CachyOS mit JavaFX. Das Projekt wird
phasenweise nach dem verbindlichen Masterauftrag entwickelt.

## Aktueller Stand

Phase 9 ergänzt einen deterministischen, lokalen Intent-Router. Text und bewusst übernommene
Sprachtranskripte werden normalisiert und ausschließlich auf registrierte Aktionen, Navigation,
Fragen, unbekannte oder mehrdeutige Eingaben abgebildet. Freier Shelltext bleibt wirkungslos;
sitzungsverändernde Befehle benötigen eine zusätzliche Bestätigung.

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
| `input` | Vosk-Push-to-Talk und gemeinsamer lokaler Text-/Voice-Intent-Router |
| `ai` | Spätere, strikt nur lesende KI-Grenze |
| `system-info` | Plattform-, Hardware-, Netzwerk-, Boot- und Capability-Erkennung |
| `platform-linux` | Spätere typisierte Linux-Adapter |
| `persistence` | Spätere SQLite-Persistenz |
| `modules/*` | Fachmodule der nachfolgenden Phasen |

Architektur, Sicherheitsgrenzen und Entwicklungsablauf stehen unter [`docs/`](docs/).

## Lizenz

GPL-3.0-or-later, siehe [`LICENSE`](LICENSE).
