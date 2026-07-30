# CachyOS Control Center AI

Eine sichere, deutschsprachige Verwaltungszentrale für CachyOS mit JavaFX. Das Projekt wird
phasenweise nach dem verbindlichen Masterauftrag entwickelt.

## Aktueller Stand

Phase 16 ergänzt Hardwareerkennung für CPU, GPU, RAM, Akku, Sensoren, PCI, USB und gebundene
Treiber. Optionale Linux-Werkzeuge werden dynamisch behandelt. Der Bericht erhebt keine
Seriennummern und kann vor dem Kopieren zusätzlich anonymisiert werden.

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
| `ai` | Strikt nur lesende Provider-Grenze und OpenAI-Responses-Adapter |
| `system-info` | Plattform-, Hardware-, Netzwerk-, Boot- und Capability-Erkennung |
| `platform-linux` | Typisierte, überwiegend lesende Linux-Adapter |
| `helper/helper-api` | Kleine, typisierte D-Bus-Schnittstelle und Fehlerprotokoll |
| `helper/privileged-helper` | Separater Polkit-geschützter Systemdienst |
| `persistence` | Spätere SQLite-Persistenz |
| `modules/*` | Fachmodule der nachfolgenden Phasen |

Architektur, Sicherheitsgrenzen und Entwicklungsablauf stehen unter [`docs/`](docs/).

## Lizenz

GPL-3.0-or-later, siehe [`LICENSE`](LICENSE).
