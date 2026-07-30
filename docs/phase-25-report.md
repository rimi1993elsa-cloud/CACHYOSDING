# Phase 25 – Release-Abnahme

## Lieferumfang

- Benutzerhandbuch und erstmaliger Einrichtungsassistent
- Secret-Service-basierte API-Key-Hilfe und Datenschutzbeschreibung
- Troubleshooting- und Entwicklerhandbuch
- Release Notes, Changelog, bekannte Einschränkungen und Screenshots
- Versionierung aller Gradle-Artefakte als 1.0.0

## Abnahmematrix

| Prüfung | Ergebnis |
|---|---|
| Gradle Build, Tests, Formatierung, Checkstyle | bestanden; 122 Tests, 0 Fehler, 1 plattformbedingter Symlink-Skip |
| Paketassets, D-Bus-/Polkit-Synchronität | `verifyPackaging` bestanden |
| App- und Helper-Distribution | beide `installDist`-Startskripte erzeugt |
| KI-Modulgrenze und freie Shellmuster | 0 verbotene Abhängigkeiten, 0 Treffer |
| JavaFX-Start und Referenz-Screenshots | bestanden; Hauptfenster und Setup real gerendert |
| Sauberer CachyOS-`makepkg`-Build | ausstehend auf CachyOS |
| KDE-Menü, System-D-Bus und Polkit Ende-zu-Ende | ausstehend auf CachyOS |
| Dell-Latitude-Leistungs-/Suspendtest | ausstehend auf Zielhardware |

## Freigabestatus

Der Quellstand ist Version 1.0.0. Eine produktive Zielsystemzertifizierung wird erst nach den drei
ausstehenden CachyOS-/Hardwareprüfungen erteilt. Diese Einschränkung verhindert keine lokale
Entwicklung, darf aber im Releaseprozess nicht als erfolgreich getestet dargestellt werden.
