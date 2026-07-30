# Entwicklerhandbuch

## Toolchain und Build

Benötigt werden JDK 21 und der eingecheckte Gradle Wrapper:

```bash
./gradlew spotlessApply
./gradlew build quality verifyPackaging
./gradlew :app:installDist :helper:privileged-helper:installDist
```

`quality` umfasst JUnit, Checkstyle, Spotless und die Paketasset-Prüfung. Linux-/CachyOS-
Integrationstests bleiben zusätzlich erforderlich.

## Architekturregeln

`app` ist der Composition Root. `ui` kennt Fachschnittstellen, aber keine Linux-Befehle. Lesende,
typisierte Adapter liegen in `platform-linux`; Fachzustand und Abläufe in `modules/*`. `ai` bleibt
von UI, Plattform, Input und Helper unabhängig. Privilegierte Mutationen passieren nur über die
kleine `helper-api`, System-D-Bus, Polkit und eine zweite Parameterprüfung im Helper.

Neue externe Prozesse benötigen absolute beziehungsweise fest aufgelöste Executables, getrennte
Argumentlisten, begrenzte Ausgabe, harte Zeitlimits und Abbruchbereinigung. Freie Shellstrings,
`sh -c`, geheime Befehlsargumente und UI-seitige Root-Ausführung sind unzulässig.

## Erweiterung eines Managers

1. Unveränderliche Snapshot-/Request-Typen im Fachmodul definieren.
2. Eine lesende Backend-Schnittstelle und bei Bedarf einen eng typisierten Mutation-Gateway bauen.
3. Linux-Erkennung über Capabilities degradierbar machen.
4. Aktionen in UI und Bootstrap verdrahten; schwere Arbeit nie auf dem JavaFX-Thread ausführen.
5. Positiv-, Negativ-, Timeout- und Parametergrenztests ergänzen.
6. Datenschutz-, Threat-Model- und Benutzerdokumentation aktualisieren.

## Paket und Release

`packaging/PKGBUILD` erzeugt getrennte App-, Helper-, STT- und Dokumentindexpakete. Das STT-Paket
verwendet eine feste offizielle Modell-URL und SHA-256-Prüfsumme. D-Bus-Ressourcen
im Helper und unter `packaging/dbus` müssen bytegleich bleiben. Für ein Release auf einem sauberen
CachyOS-System `makepkg --syncdeps --cleanbuild`, Installation, KDE-Menü, D-Bus-Aktivierung,
Polkit-Dialoge, Update und Entfernung prüfen. Benutzerdaten dürfen bei Entfernung nicht automatisch
gelöscht werden.
