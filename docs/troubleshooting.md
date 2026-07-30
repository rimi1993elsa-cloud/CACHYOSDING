# Fehlerbehebung

## Die Anwendung startet nicht

Starte `cachyos-control-center` aus einem Terminal und prüfe Java 21 sowie das installierte
App-Paket. Für einen Quellbuild helfen `./gradlew :app:run` und anschließend `./gradlew quality`.

## Administrative Aktion nicht verfügbar

Installiere `cachyos-control-center-helper`. Prüfe, ob System-D-Bus und Polkit laufen und ob
`org.cachyos.ControlCenter.Helper1.service`, die D-Bus-Regel und die Polkit-Policy installiert
sind. Eine abgelehnte Authentisierung wird nicht umgangen. Paketaktionen stoppen außerdem bei
Pacman-Lock oder veralteter Vorschau.

## Netzwerk, Audio oder Hardware sind „nicht verfügbar“

Die Oberfläche erfindet keine Ersatzwerte. Installiere beziehungsweise starte das zuständige
Werkzeug: NetworkManager/`nmcli`, PipeWire-Pulse/`pactl`, `pciutils`, `usbutils`, `lm_sensors`,
`smartmontools`, `snapper`, `kscreen` oder `power-profiles-daemon`. Danach die Seite aktualisieren.

## Sprache funktioniert nicht

Installiere das optionale Paket `cachyos-control-center-stt-de` und starte die Anwendung neu. Sie
erkennt das enthaltene Modell automatisch. Nur für ein eigenes Modell muss dessen Wurzelverzeichnis
gewählt werden; dort müssen `am/final.mdl`, `conf/` und `graph/` existieren. Prüfe außerdem die
Mikrofonfreigabe in KDE.

## KI bleibt offline

Die lokale Verwaltung ist davon nicht betroffen. Prüfe Online-KI, Budget größer null, Netzwerk,
API-Key gemäß [API-Key-Hilfe](api-key.md) und das API-Kontingent. Dokumentquellen werden nur manuell
aktualisiert.

## Zurücksetzen

Nutze zuerst die Löschfunktionen unter Einstellungen. Falls die UI nicht startet, sichere und
entferne gezielt `$XDG_CONFIG_HOME/cachyos-control-center`; lösche nie pauschal das gesamte
XDG-Verzeichnis.
