# Teststrategie

## Phase 0

- Unit-Tests prüfen Linux/KDE/Wayland-Erkennung und fehlende Plattformwerte.
- Unit-Tests prüfen XDG-Pfade, Fallbacks und die Ablehnung relativer Konfigurationspfade.
- Ein Bootstrap-Test prüft die Erstellung des unprivilegierten Anwendungskontexts.
- Checkstyle prüft grundlegende Fehler- und Stilregeln.
- Spotless erzwingt deterministische Formatierung.
- GitHub Actions führt den vollständigen Build mit Java 21 und dem Wrapper aus.

## Manuelle Prüfung auf CachyOS

```bash
./gradlew :app:run
```

Zu prüfen:

1. Fenster öffnet sich unter KDE/Wayland.
2. Plattform wird als Linux und Architektur real angezeigt.
3. Start und Beenden erzeugen lokale Logs unter
   `~/.cache/cachyos-control-center/logs/`.
4. Es erscheint keine Polkit-Abfrage und es wird keine Systemaktion angeboten.

Die praktische Prüfung auf dem Zielgerät Dell Latitude 5440 ist erforderlich, sobald das Projekt
dort ausgecheckt ist.

