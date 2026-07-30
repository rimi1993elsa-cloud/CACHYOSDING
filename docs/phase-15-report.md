# Abschlussbericht Phase 15

Datum: 30. Juli 2026

## Implementiert

- firewalld-Laufzeitstatus und bestätigte Aktivierung/Deaktivierung
- TCP-/UDP-Listener aus begrenzter `ss`-Ausgabe
- systemd-Status von OpenSSH
- fehlgeschlagene SSH-/Authentifizierungsereignisse seit dem Boot
- vorhandener lokaler Pacman-Updatestand
- UEFI- und Secure-Boot-Evidenz
- AppArmor-Status mit belegten Profilinformationen
- POSIX-Rechte von `/etc/passwd`, `/etc/shadow` und `/etc/sudoers`
- Einzelstatus, Evidenz, Empfehlung und Unknown-Zustand ohne Gesamtscore

## Sicherheitsgrenzen

- Alle Statusabfragen sind lesend, zeitbegrenzt und ausgabebegrenzt.
- Executables und Optionen sind fest; es existiert keine freie Shell.
- Kritische Dateiprüfungen folgen keinen Symlinks.
- Fehlende Leserechte oder Werkzeuge werden als `UNKNOWN`, nie als sicher, dargestellt.
- Nur die boolesche Firewall-Entscheidung erreicht die typisierte D-Bus-Schnittstelle.
- Der Helper setzt ausschließlich `firewalld.service` und verlangt Polkit.

## Tests

- Inspektion löst keine Mutation aus.
- Firewall-Änderung erreicht ausschließlich den injizierten Gateway.
- IPv4- und IPv6-Listener werden strukturiert geparst.
- Benannte, ungültige und außerhalb des Wertebereichs liegende Ports werden verworfen.
- Sicherheitsnavigation enthält eine reale Seite.
- Nicht-Linux-Systeme erhalten einen ehrlichen Nichtverfügbarkeitszustand.

## Keine falschen Sicherheitsversprechen

Es gibt absichtlich keinen Score. Ein aktiver SSH-Dienst ist nicht automatisch unsicher und ein
aktiver Firewall-Dienst ist keine Garantie für gute Regeln. Updatezahlen basieren auf dem
vorhandenen lokalen Pacman-Datenbankstand. AppArmor und Secure Boot werden nur positiv gemeldet,
wenn lokale Evidenz vorliegt.

## Zielsystemtest

Die Windows-Entwicklungsumgebung kann Journal-, firewalld-, Secure-Boot- und AppArmor-Werte nicht
real prüfen. Diese Probes sowie der KDE-Polkit-Dialog werden nach Paketinstallation auf CachyOS in
Phase 24/25 abgenommen.

## Nächster Schritt

Phase 16 implementiert Hardware-, Sensor-, PCI-, USB- und Treibererkennung sowie einen lokal
anonymisierbaren Hardwarebericht.
