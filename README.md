# CachyOS Control Center AI

Eine sichere, deutschsprachige Verwaltungszentrale für CachyOS mit JavaFX. Das Projekt wird
phasenweise nach dem verbindlichen Masterauftrag entwickelt.

## Aktueller Stand

Version 1.2 ist ein auf CachyOS installierbarer Release Candidate. Neben lokaler
Systemverwaltung, Vosk, OpenAI-Modellprofilen und dem getrennten Polkit-Helper enthält sie eine
SQLite-Datenbasis, dauerhaftes Audit und Chat-Historie, lokale KI-Verbrauchsgrenzen, sichere
API-Key-Verwaltung über KWallet/Secret Service sowie geführte Linux- und Installationschecks.

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

## Auf CachyOS installieren und testen

Für die fertige Übergabe das Archiv `CachyOS-Control-Center-1.2.0.tar.gz` auf CachyOS
entpacken und im Ordner `Installieren.desktop` doppelklicken. Falls KDE nachfragt, einmal
„Vertrauen und starten“ bestätigen. Der One-Click-Installer protokolliert den gesamten Lauf in
`install-cachyos.log`.

Der manuelle Weg aus dem Projektordner:

```bash
cd /pfad/zum/CachyOS-Projekt
./install.sh
cachyos-control-center-verify
cachyos-control-center
```

Das Installationsskript installiert ausschließlich deklarierte Arch-Buildabhängigkeiten, führt alle
Tests aus, prüft die Vosk-Prüfsumme und baut anschließend die vier Split-Pakete mit `makepkg`.
Alternativ kann zuerst nur `./scripts/verify-linux.sh` ausgeführt werden.

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
| `persistence` | Atomare XDG-Einstellungen und begrenzter opt-in Chatverlauf |
| `modules/*` | Fachmodule der nachfolgenden Phasen |

Einstiegspunkte:

- [Benutzerhandbuch](docs/user-guide.md)
- [API-Key-Hilfe](docs/api-key.md)
- [Datenschutz](docs/privacy.md)
- [Fehlerbehebung](docs/troubleshooting.md)
- [Entwicklerhandbuch](docs/developer-guide.md)
- [Release Notes 1.0](docs/release-notes-1.0.0.md)
- [Release Notes 1.1](docs/release-notes-1.1.0.md)
- [Release Notes 1.2](docs/release-notes-1.2.0.md)
- [Bekannte Einschränkungen](docs/known-limitations.md)

## Lizenz

GPL-3.0-or-later, siehe [`LICENSE`](LICENSE).
