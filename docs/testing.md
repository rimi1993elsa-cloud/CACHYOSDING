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

## Automatisierte Zielsystemprüfung ab Version 1.2

Auf CachyOS oder Arch Linux:

```bash
./scripts/verify-linux.sh
```

Die Prüfung führt Gradle-Build, alle Tests, Formatierung, Checkstyle, Packaging-Gate,
Vosk-Quellprüfsumme, optionale Desktop-/AppStream-Validatoren und Sicherheitsgreps aus. Nach der
Paketinstallation prüft `cachyos-control-center-verify` die installierten Programm-, Desktop-,
D-Bus-, Polkit- und Vosk-Dateien.

## Manuelle Release-Checkliste

1. App aus dem KDE-Menü starten.
2. Unter Einstellungen den Linux-Systemcheck kontrollieren.
3. API-Key in KWallet speichern, Chatstatus prüfen und den Key wieder löschen.
4. Eine kurze deutsche Push-to-Talk-Aufnahme testen; nach Loslassen darf kein Mikrofonstream laufen.
5. Netzwerk- und Audiostatus gegen KDE vergleichen.
6. Eine Pakettransaktion nur nach korrekter Vorschau und Polkit-Abfrage ausführen.
7. Einen erlaubten Dienst neu starten und Audit-Eintrag nach App-Neustart prüfen.
8. Suspend/Resume durchführen und CPU-/RAM-Leerlauf messen.
