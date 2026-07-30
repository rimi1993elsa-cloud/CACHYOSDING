# Abschlussbericht Phase 13

Datum: 30. Juli 2026

## Implementiert

- Separates `helper-api` mit kanonischer D-Bus-Introspection
- Separater, per System-Bus aktivierbarer privilegierter Prozess
- Acht typisierte Methoden für Pakete, Firewall, Dienste, Snapshots und Prozesse
- Polkit-Autorisierung des eindeutigen D-Bus-Absenders je Aktionsgruppe
- Unabhängige Validierung an D-Bus-Adapter und Helper-Service
- Ausschließlich absolute, feste Executables und getrennte Argumente
- Begrenzte Fehlercodes, Helper- und Prozesszeitlimits
- Parameterfreies strukturiertes Audit
- System-Bus-Service, restriktive Bus-Policy und minimale Polkit-Policy

## Sicherheitsreview

- Die API enthält keine generische Shell-, Command-, Script- oder Executable-Methode.
- Aufrufer können keine Executable, Option oder Polkit-Action-ID bestimmen.
- Paket-, Unit-, Snapshot-, PID-, Signal- und Prioritätswerte besitzen enge Allowlists.
- Der Helper vertraut keiner vom Aufrufer behaupteten UID oder PID.
- Polkit prüft direkt den vom D-Bus gelieferten eindeutigen Absender.
- Prozess-Ein-/Ausgaben sind geschlossen beziehungsweise verworfen.
- Audit enthält Aktionsklasse und Ergebnis, aber keine Nutzparameter oder Secrets.
- Die GUI besitzt keine Abhängigkeit auf die Helper-Implementierung und läuft nie als Root.

## Tests

- Protokollantworten werden sicher kodiert und lehnen Separator-Injection ab.
- Shell-Metazeichen in Paket- und Snapshotwerten werden vor Ausführung abgelehnt.
- Traversal-artige Unit-Namen und manipulierte Operationen werden abgelehnt.
- Unzulässige Signale werden abgelehnt.
- Fehlende Polkit-Freigabe verhindert jede Ausführung.
- Nur eine typisierte, autorisierte Aktion erreicht den Executor.
- Hängende Aktionen werden abgebrochen und als Timeout gemeldet.

## Manuelle Prüfung

Kompilierung, Unit-Tests und statische Analyse laufen auf dem Windows-Entwicklungsrechner. Eine
echte System-Bus-Aktivierung und ein KDE-Polkit-Dialog benötigen die spätere Installation des
CachyOS-Pakets und werden in Phase 24/25 auf dem Zielsystem abgenommen.

## Annahmen und Einschränkungen

- `firewalld.service` ist die einzige in Version 1 unterstützte Firewall-Implementierung.
- Pacman-Aktionen setzen eine zuvor in der unprivilegierten UI bestätigte Transaktionsvorschau
  voraus; diese Vorschau ist Bestandteil von Phase 14.
- Der Helper führt pro Instanz administrative Systemwerkzeuge seriell aus.
- Paketierungsdateien installieren Policies und Service erst in Phase 24 an ihre Zielpfade.

## Nächster Schritt

Phase 14 implementiert lesende Pacman-Abfragen, Suche, Details, Updates, verwaiste Pakete,
Lock-Erkennung und eine explizite Transaktionsvorschau vor jeder Helper-Aktion.
