# Entwicklung

## Toolchain

Das Projekt verwendet die Gradle-Java-Toolchain für Java 21. Der Gradle Wrapper ist die einzige
unterstützte Gradle-Einstiegsschnittstelle.

```bash
./gradlew build
./gradlew quality
./gradlew spotlessApply
./gradlew :app:run
```

`build` kompiliert alle Subprojekte und führt Tests, Checkstyle und Spotless aus. `quality` führt die
Verifikation ohne erneutes Packaging aus.

## Schichtenregel

Abhängigkeiten zeigen in Richtung stabiler Modelle. Insbesondere darf `ai` niemals von
`platform-linux`, dem Action Executor oder einem privilegierten Helper abhängen. UI-Handler dürfen
keinen freien Befehl konstruieren.

## Phase-0-Plattformprüfung

Die Plattform-Erkennung ist absichtlich konservativ. Sie wertet `os.name`, `os.version`, `os.arch`
und die Variablen `XDG_CURRENT_DESKTOP`, `XDG_SESSION_DESKTOP`, `DESKTOP_SESSION` und
`XDG_SESSION_TYPE` aus. Detaillierte CachyOS- und Hardware-Erkennung folgt in Phase 3.

