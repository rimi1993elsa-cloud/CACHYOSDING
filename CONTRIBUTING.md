# Mitwirken

## Entwicklungsablauf

1. Eine kleine, klar abgegrenzte Änderung erstellen.
2. Sicherheitsgrenzen aus `docs/architecture.md` und `docs/threat-model.md` einhalten.
3. `./gradlew spotlessApply` ausführen.
4. `./gradlew build quality` ausführen.
5. Tests und Dokumentation gemeinsam mit dem Code aktualisieren.

Java-Code verwendet englische Bezeichner, die Oberfläche deutsche Texte. Neue Systemadapter müssen
Argumentlisten statt zusammengesetzter Shellstrings verwenden. Eine Schnittstelle für freie
Shellausführung ist nicht zulässig.

## Commit-Stil

Commits verwenden kurze Conventional-Commit-Präfixe, zum Beispiel:

```text
feat: add system capability detection
fix: reject relative xdg paths
docs: clarify ai trust boundary
```

