# Bedrohungsmodell

## Schutzgüter

- Integrität und Verfügbarkeit des CachyOS-Systems
- Zugangsdaten, API-Schlüssel und Wallet-Secrets
- private Benutzerdateien und Diagnoseinhalte
- Auditierbarkeit administrativer Aktionen
- Mikrofon- und Sprachdaten

## Vertrauensgrenzen

| Bereich | Vertrauen | Zulässige Fähigkeiten |
|---|---|---|
| JavaFX-UI | unprivilegiert | Darstellung, harmlose Navigation |
| System-Info | lokal, nur lesend | eng definierte Statusabfragen |
| Action Engine | lokal, validiert | registrierte Action-IDs |
| Helper | privilegiert, minimal | feste D-Bus-Methoden nach Polkit |
| Online-KI | nicht vertrauenswürdig | Textantworten und Empfehlungen |
| Dokument-/Loginhalt | nicht vertrauenswürdig | nur bereinigte Daten |

## Zentrale Bedrohungen und Maßnahmen

### Command Injection

Freier Shelltext und `sh -c` sind verboten. Spätere Prozesse werden nur mit
`ProcessBuilder(List<String>)`, festen Executables, validierten Parametern und Timeouts gestartet.

### Rechteausweitung

Die GUI läuft nie als Root. Der Helper besitzt acht feste Methoden, validiert jeden Parameter
innerhalb der privilegierten Grenze erneut und kann keine unbekannte Operation ausführen. Polkit
prüft den vom System-Bus gelieferten eindeutigen Absender mit optionaler Benutzerinteraktion.

Der Helper nimmt weder PID noch Benutzer-ID als behauptete Identität vom Aufrufer entgegen. Seine
D-Bus-Policy erlaubt nur die eigene Schnittstelle; nur Root darf den Busnamen besitzen. Abgelehnte,
fehlgeschlagene und erfolgreiche Aktionen werden ohne Paket-, Dienst- oder Beschreibungstext
protokolliert. Polkit- und Systemwerkzeugaufrufe besitzen feste Zeitlimits.

### KI- und Prompt-Injection

KI-Ausgaben sind untrusted Text. Sie können keine Action-ID an einen Executor senden und keine
Buttons auslösen. Eingelesene Dokumente und Logs werden als Daten markiert, redigiert und nie als
Systemanweisung behandelt.

### Datenabfluss

Systemkontext wird kategorieweise freigegeben und vor Übertragung bereinigt. Hostname,
Benutzername, private IPs, persönliche Pfade, Seriennummern, Tokens und Secrets werden standardmäßig
maskiert. Ohne API-Schlüssel bleibt der lokale Manager funktionsfähig.

### Pfad- und Symlink-Angriffe

XDG-Basispfade müssen absolut sein. Spätere schreibende Operationen verwenden normalisierte,
erlaubte Wurzeln, sichere Dateirechte und atomare Ersetzungen; Symlink-Ziele werden vor Änderungen
geprüft.

### Ressourcenmissbrauch

Keine dauerhafte Mikrofonverarbeitung. Systemwerte werden in sinnvollen Intervallen und nur bei
Bedarf aktualisiert. Prozesse und HTTP-Anfragen erhalten Timeout und Abbruchpfad.

### Log-Leaks

Logs enthalten keine Secrets oder vollständigen privaten Inhalte. Administrative Aktionen erhalten
Ereignis-IDs; Parameter werden vor dem Logging redigiert. Dateien rotieren und sind größenbegrenzt.

## Phase-0-Risiko

Phase 0 startete keine externen Prozesse, las keine Systemdateien, öffnete keine Netzwerkverbindung
und forderte keine Privilegien an. Seit Phase 13 sind administrative Prozesse ausschließlich im
separat paketierten Helper mit D-Bus/Polkit-Grenze möglich.
