# Abschlussbericht Phase 18

Datum: 30. Juli 2026

## Implementiert

- getrennte Listen von systemweiten und benutzereigenen systemd-Units
- Start, Stop, Restart, Enable und Disable
- System-Units über D-Bus/Polkit, User-Units unprivilegiert
- letzte 200 lokale Journalzeilen je Unit
- laufende Prozesse mit PID, Nutzer, CPU-Zeit, RSS und Nice
- TERM, KILL und Prioritätsänderung über typisierte Helper-Methoden

## Schutzmaßnahmen

Unit-Namen besitzen eine enge Allowlist. Der Scope ist ein Enum und kann nicht durch Textargumente
umgangen werden. Prozessaktionen akzeptieren nur PIDs aus dem letzten Snapshot. PID 1/2, bekannte
Kernprozesse, Kernel-/verborgene Prozesse sind gesperrt. SIGKILL verlangt zusätzlich die exakte PID.
Signale sind im Helper auf 9 und 15, Nice auf -20 bis 19 begrenzt.

## Tests

- Manipulierter Unit-Name erreicht keinen Gateway.
- User-Scope bleibt bis zum unprivilegierten Gateway erhalten.
- Kritische, unbekannte und nicht bestätigte Prozesse werden abgelehnt.
- Erst die exakt bestätigte bekannte PID erreicht den Signal-Gateway.
- systemd-Parser erhält den Scope.
- Dienst- und Prozessseiten sind navigierbar.

## Einschränkungen und Zieltest

Journalrechte können Logs einschränken und werden als Nichtverfügbarkeit gezeigt. Momentane
CPU-Prozente werden nicht geschätzt; angezeigt wird die verlässliche kumulierte CPU-Zeit. Reale
systemd-/Polkit- und Prozessaktionen müssen auf CachyOS abgenommen werden.

## Nächster Schritt

Phase 19 ergänzt KDE-/Wayland-kompatible Anzeige-, Grafik- und Energieinformationen sowie
dynamische Helligkeits-, Profil- und Suspend/Hibernate-Fähigkeiten.
