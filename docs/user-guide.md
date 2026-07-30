# Benutzerhandbuch – Phase 1

Nach `./gradlew :app:run` erscheint die Grundoberfläche mit Topbar, Navigation, Inhaltsbereich und
Statusbereich. Die Seiten „Übersicht“, „System“ und „Einstellungen“ enthalten reale lokale Inhalte.
Noch nicht implementierte Manager sind deaktiviert und zeigen ihre geplante Entwicklungsphase im
Tooltip.

Tastaturkürzel:

- `Alt+1`: Übersicht
- `Alt+2`: System
- `Alt+3`: Einstellungen
- `Strg+L`: späteres Eingabefeld fokussieren

Unter „Einstellungen“ kann zwischen System-, hellem und dunklem Farbschema gewechselt werden.

Auf der Übersicht stehen vier Schnellaktionen bereit:

- Firefox öffnen
- Dateimanager öffnen
- Terminal öffnen
- Bildschirm sperren

Jeder Button ist fest mit einer registrierten Action-ID verbunden. Wenn das benötigte Programm
nicht installiert oder nicht im sicheren Suchpfad auffindbar ist, erscheint eine verständliche
Meldung. Die Anwendung fordert für diese Aktionen keine erhöhten Rechte an.
