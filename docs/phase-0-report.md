# Abschlussbericht Phase 0

Datum: 30. Juli 2026

## Implementiert

- Gradle-9.6.1-Wrapper mit verifizierter SHA-256-Prüfsumme
- Multi-Modul-Struktur für App, Core, UI, Input, KI, Systeminformationen, Linux-Plattform,
  Persistenz und alle vorgesehenen Manager-Module
- Java-21-Toolchain und JavaFX 21.0.12
- Minimales startfähiges JavaFX-Hauptfenster
- Sichere Plattform-Erkennung ausschließlich über JVM-Eigenschaften und ausgewählte
  Sitzungsvariablen
- XDG-konforme Konfigurations-, Daten- und Cachepfade mit sicheren Fallbacks
- SLF4J/Logback mit Rotation, Größenlimit und Ereignis-ID-Feld
- JUnit 5, Spotless, Checkstyle und GitHub-Actions-Build
- README, Lizenz, Sicherheitsrichtlinie, Architektur, Threat Model, Entwicklungs- und
  Testdokumentation

## Geänderte Dateien

Das Repository wurde leer übernommen. Erstellt wurden:

- Root-Builddateien: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`
- Wrapper: `gradlew`, `gradlew.bat`, `gradle/wrapper/*`
- Projektmetadaten: `README.md`, `LICENSE`, `CHANGELOG.md`, `SECURITY.md`, `CONTRIBUTING.md`
- CI: `.github/workflows/build.yml`
- Qualitätskonfiguration: `config/checkstyle/checkstyle.xml`
- Quellcode und Tests in `app`, `core`, `ui` und `system-info`
- reservierte, ausdrücklich als noch nicht implementiert markierte Paketstrukturen in allen
  späteren Modulen
- Dokumentation unter `docs/`

## Ausgeführte Befehle

```text
git status --short --branch
java -version
javac -version
gradle --version
Gradle-/Maven-/Adoptium-Versions- und Prüfsummenabfragen
gradle wrapper --gradle-version 9.6.1 --distribution-type bin
.\gradlew.bat --version
.\gradlew.bat --no-daemon spotlessApply
.\gradlew.bat --no-daemon build quality
.\gradlew.bat --no-daemon :app:run
```

Die fehlende lokale Toolchain wurde temporär als Eclipse Temurin 21.0.12 bereitgestellt. Sie ist
nicht Bestandteil des Repositorys.

## Tests

- `XdgPathsTest`: 2 Tests erfolgreich
- `PlatformDetectorTest`: 2 Tests erfolgreich
- `BootstrapTest`: 1 Test erfolgreich
- Gesamt: 5 Tests, 0 Fehler, 0 Fehlschläge, 0 übersprungen
- Spotless: erfolgreich
- Checkstyle 13.9.0: erfolgreich
- Vollständiger Multi-Modul-Build: erfolgreich

## Manuelle Prüfung

Das JavaFX-Fenster wurde auf dem Entwicklungsrechner unter Windows 11 mit Temurin 21.0.12 gestartet.
Der Prozess blieb stabil aktiv und Logback protokollierte den Start. Die praktische Prüfung unter
CachyOS/KDE/Wayland auf dem Dell Latitude 5440 ist noch auszuführen; der genaue Ablauf steht in
`docs/testing.md`.

## Sicherheitsprüfung

- Keine Prozessausführung oder Systemdateiabfrage in der Plattform-Erkennung
- Keine privilegierte Schnittstelle und keine Root-Anforderung
- Keine KI- oder Netzwerkfunktion im Anwendungscode
- Keine Secrets in Code, Konfiguration oder Tests
- Relative XDG-Basispfade werden verworfen
- Spätere Module werden nicht in der Oberfläche als funktionsfähig dargestellt

## Annahmen

- Java 21 bleibt die minimale Runtime, obwohl neuere LTS-Versionen verfügbar sind, da dies der
  verbindlichen Zielvorgabe entspricht.
- JavaFX 21 wird passend zur Java-21-Laufzeit verwendet.
- GPL-3.0-or-later wurde als Lizenz für die freie Linux-Systemanwendung gewählt.
- Phase 0 wird auf dem vorhandenen Windows-Entwicklungsrechner gebaut; Zielsystemtests folgen auf
  CachyOS.
- Die optionalen Gradle-Configuration-Cache-Einstellungen bleiben deaktiviert, weil der offizielle
  JavaFX-Plugin-`run`-Task damit nicht kompatibel ist. Build-Cache und Parallelisierung bleiben
  aktiv.

## Bekannte Einschränkungen

- Keine Manager-, Navigations-, Aktions-, KI-, STT- oder Helper-Funktion; dies ist die verbindliche
  Grenze von Phase 0.
- CachyOS-spezifische Distributionserkennung und Hardwareabfragen folgen erst in Phase 3.
- GitHub Actions kann lokal nur syntaktisch vorbereitet werden und läuft erstmals nach einem Push.
- Praktischer Zielgerätetest steht aus.

## Nächster Schritt: Phase 1

Phase 1 benötigt eine konkrete UI-Shell mit Sidebar, Topbar, Statusleiste, Inhaltsrouter,
responsivem Layout, Tastaturbedienung, Light-/Dark-Theme, wiederverwendbaren Karten und
Benachrichtigungen. Nicht implementierte Module müssen weiterhin ehrlich gekennzeichnet und dürfen
nicht als fertige Manager-Seiten erscheinen.

