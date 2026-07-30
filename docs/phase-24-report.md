# Phase 24 – Abschlussbericht

## Ergebnis

Der lokale PKGBUILD erzeugt `cachyos-control-center`, `cachyos-control-center-helper`,
`cachyos-control-center-stt-de` und `cachyos-control-center-doc-index`. Desktop, AppStream, SVG,
D-Bus und Polkit sind vollständig enthalten.

## Rechte und Trennung

- App unter `/usr/lib/cachyos-control-center`, Startlink unter `/usr/bin`
- Helper separat unter `/usr/lib/cachyos-control-center-helper`
- nur Root darf den D-Bus-Namen besitzen
- D-Bus erlaubt ausschließlich Helper-, Introspection- und Peer-Interface
- Polkit verweigert inaktive und beliebige Sitzungen; aktive Aktionen verlangen Adminauthentisierung
- installierte Daten sind regulär 0644; Startskripte stammen ausführbar aus `installDist`
- kein Setuid und keine weltbeschreibbaren Dateien
- Deinstallation löscht keine XDG-Benutzerdaten

## Verifikation

`verifyPackaging` prüft Assets, Exec-Pfade, Desktopstart, Polkit-Defaults, fehlende gefährliche
chmod-/Löschbefehle und Drift der Helper-Ressourcen. XML und Desktopdatei wurden unter Windows
syntaktisch geprüft; beide Gradle-Distributionen wurden erzeugt.

## Einschränkung

`makepkg`, `desktop-file-validate` und `appstreamcli` sind in der Windows-Entwicklungsumgebung
nicht vorhanden. Eine reale Paketinstallation, KDE-Menüauflösung und D-Bus-/Polkit-Aktivierung auf
CachyOS bleiben daher Bestandteil der Zielsystemmatrix in Phase 25 und dürfen nicht als bereits
ausgeführt behauptet werden.
