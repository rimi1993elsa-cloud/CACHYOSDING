# Benutzerhandbuch – Phase 18

Nach `./gradlew :app:run` erscheint die Grundoberfläche mit Topbar, Navigation, Inhaltsbereich und
Statusbereich. Die Seiten „Übersicht“, „System“ und „Einstellungen“ enthalten reale lokale Inhalte.
Noch nicht implementierte Manager sind deaktiviert und zeigen ihre geplante Entwicklungsphase im
Tooltip.

Tastaturkürzel:

- `Alt+1`: Übersicht
- `Alt+2`: System
- `Alt+3`: Einstellungen
- `Strg+L`: späteres Eingabefeld fokussieren

Unter „Einstellungen“ kann zwischen System-, hellem und dunklem Farbschema gewechselt werden.

Die Seite „System“ zeigt erkannte Angaben zu Distribution, CachyOS, Kernel, Desktop, Sitzung,
Prozessor, Arbeitsspeicher, Speicherplatz, Akku, Netzwerk, Bootmanager und optionalen Werkzeugen.
Nicht verfügbare Werte werden nicht geschätzt. Der Inhalt ist auf kleinen Displays scrollbar.

Die Übersicht aktualisiert CPU, Arbeitsspeicher, Systemspeicher, Netzwerk und Akku automatisch.
Update- und Dienstestatus werden nur angezeigt, wenn `pacman` beziehungsweise `systemctl`
verfügbar waren und die Abfrage erfolgreich war. Warnungen entstehen ausschließlich aus erkannten
Schwellenwerten. Die fünf letzten lokalen Aktionen erscheinen nach der nächsten Aktualisierung.

Unter „Netzwerk“ werden NetworkManager-Geräte, erreichbare WLANs sowie gespeicherte Verbindungs-
und VPN-Profile angezeigt. Die Seite aktualisiert sich bei `nmcli monitor`-Ereignissen. WLAN lässt
sich ein- oder ausschalten und neu scannen. Ein gespeichertes Profil kann nach Auswahl aktiviert,
ein ausgewähltes Gerät getrennt werden.

Neue geschützte WLANs werden bewusst nicht mit einem Passwort als Prozessargument verbunden.
Lege sie über den NetworkManager-Secret-Agent von KDE an; anschließend kann das gespeicherte Profil
in der Anwendung aktiviert werden. Fehlt `nmcli`, bleibt die Seite nutzbar und erklärt den Grund.

Unter „Audio“ zeigt die Anwendung Ausgabegeräte, Mikrofone und laufende Wiedergabestreams. Ein Gerät
wird durch Auswahl bedient. Danach lassen sich Lautstärke, Stummschaltung und Standardgerät ändern.
Der Testton ist nur aktiv, wenn `pw-play` und die feste freedesktop-Systemklangdatei verfügbar sind.
Die Mikrofonseite verändert ausschließlich Mixerwerte und zeichnet kein Audio auf.

Unter „Programme“ werden sicher startbare Einträge aus den XDG-Anwendungsverzeichnissen angezeigt.
Die Suche filtert Name und Beschreibung. Favoriten stehen während der laufenden Sitzung oben.
Nach Auswahl zeigt die App die Paketzuordnung, sofern `pacman` verfügbar ist. „Starten“ sendet nur
die interne Katalog-ID; der sichtbare Name und der Suchtext werden niemals als Befehl interpretiert.

Unter „Sprache“ wird ein vorhandenes Java-Sound-Mikrofon ausgewählt. Über „Modell auswählen“ wird
ein entpacktes deutsches Vosk-Modell festgelegt; erwartet werden unter anderem `am/final.mdl`,
`conf/` und `graph/`. Halte den Aufnahmebutton oder die Leertaste gedrückt und sprich. Währenddessen
zeigt die Seite den Aufnahmezustand und Teiltranskripte, nach dem Loslassen das bestätigte
Transkript. Ohne Modell oder Mikrofon bleibt Push-to-Talk deaktiviert und nennt den Grund.

Die Aufnahme wird nur während Push-to-Talk geöffnet. Audiodaten werden nicht in Dateien geschrieben.
Ein Transkript löst nicht automatisch eine Aktion aus. Erst „Transkript lokal auswerten“ übergibt
den sichtbaren Text an denselben lokalen Router wie das Eingabefeld am unteren Fensterrand.

Das Eingabefeld erkennt feste deutsche Formulierungen wie „Öffne Firefox“, „Zeige Netzwerk“,
„WLAN suchen“ oder „Testton abspielen“. Exakt katalogisierte Anwendungen können mit
„Starte GIMP“ geöffnet werden. Sind mehrere Programmnamen möglich, wird nichts gestartet.
„Bildschirm sperren“ verlangt vor der Ausführung eine Bestätigung. Fragen werden als Entwurf in den
KI-Assistenten übernommen, aber erst ein bewusster Klick auf „Frage senden“ startet die
Online-Anfrage. Freie Terminalbefehle werden nicht ausgeführt.

Unter „KI-Assistent“ steht der optionale OpenAI-Chat. Ohne API-Schlüssel ist Senden deaktiviert und
alle lokalen Manager bleiben vollständig nutzbar. Unter KDE kann ein Schlüssel beispielsweise mit
dem Secret-Service-Werkzeug hinterlegt werden:

```bash
secret-tool store --label="CachyOS Control Center OpenAI" \
  application cachyos-control-center key openai-api-key
```

Der Schlüssel wird dabei interaktiv eingelesen und erscheint nicht in der Befehlszeile. Für eine
reine Entwicklungssitzung kann alternativ `OPENAI_API_KEY` gesetzt werden. Das Modell ist über
`CACHYOS_CC_OPENAI_MODEL`, die Ausgabegrenze über
`CACHYOS_CC_OPENAI_MAX_OUTPUT_TOKENS` konfigurierbar. Voreingestellt sind `gpt-5.6-sol` und 2048
Ausgabetokens. Prüfe vor Nutzung die aktuellen API-Preise. Der Chat besitzt keine lokale
Ausführungsfunktion.

Mit „Offizielle Quellen aktualisieren“ werden fünf fest eingebaute CachyOS-/ArchWiki-Seiten in den
lokalen Cache geladen. Dieser Netzwerkzugriff geschieht nie automatisch beim Start. Ein Cacheeintrag
gilt sieben Tage als aktuell; bei Offlinebetrieb bleibt der letzte Stand nutzbar. Bei einer Frage
zeigt „Lokale Wissensquellen“ die verwendeten Treffer samt URL und Abrufzeit. Gibt es keinen
passenden Treffer, wird dies offen angezeigt.

Unter „Diagnose“ startet „Lokale Diagnose starten“ sechs feste, lesende Prüfungen. Je nach
verfügbaren Werkzeugen erscheinen Befunde für NetworkManager, PipeWire-Pulse, fehlgeschlagene
systemd-Dienste, Kernel-Bootparameter, PCI-/Grafikdaten und die Pacman-Datenbank. Fehlende Werkzeuge
werden als „nicht verfügbar“ gemeldet und stoppen die übrigen Prüfungen nicht.

Warnungen zu Netzwerk oder Audio können einen festen lokalen Maßnahmenbutton anbieten. Dieser nutzt
dieselbe typisierte Action Engine wie die jeweilige Managerseite und interpretiert keinen
Diagnosetext. „Bereinigten Bericht im KI-Chat erklären“ übernimmt den redigierten Bericht nur als
Entwurf. Prüfe ihn sichtbar und klicke im Chat separat auf Senden, wenn du ihn wirklich übertragen
möchtest.

Auf der Übersicht stehen vier Schnellaktionen bereit:

- Firefox öffnen
- Dateimanager öffnen
- Terminal öffnen
- Bildschirm sperren

Jeder Button ist fest mit einer registrierten Action-ID verbunden. Wenn das benötigte Programm
nicht installiert oder nicht im sicheren Suchpfad auffindbar ist, erscheint eine verständliche
Meldung. Die Anwendung fordert für diese Aktionen keine erhöhten Rechte an.
# Pakete

Die Paketseite zeigt installierte Pakete sowie Zähler für Updates, verwaiste Pakete und den
Pacman-Cache. Eine Repository-Suche akzeptiert Paketnamen und sichere Suchbestandteile.

Vor „Installieren“ oder „Entfernen“ zeigt die Anwendung die von Pacman berechneten Änderungen und
Größen. Erst der Button „Authentifizieren und ausführen“ öffnet Polkit. Wenn eine andere
Pacman-Anwendung die Datenbank sperrt oder die Vorschau älter als zwei Minuten ist, wird die Aktion
verworfen. AUR-Unterstützung ist in Version 1 optional und derzeit nicht aktiviert.
# Sicherheit

Die Sicherheitsseite prüft lokal Firewall, lauschende Ports, SSH, fehlgeschlagene Loginversuche,
den vorhandenen Pacman-Updatestand, Secure Boot, AppArmor und Schreibrechte kritischer Dateien.
„Unbekannt“ bedeutet ausdrücklich, dass Werkzeug, Berechtigung oder Evidenz fehlen; es ist keine
Entwarnung. Arch-Pakete besitzen keinen separaten Security-Updatekanal, daher wird die gesamte
Updateliste gezeigt.

Die Anwendung berechnet keinen Gesamtscore. Firewall-Änderungen zeigen ihre Wirkung vorab, benötigen
eine Bestätigung und anschließend Polkit. Andere Befunde enthalten nur Empfehlungen und lösen keine
automatische Änderung aus.

# Hardware

Die Hardwareseite zeigt Modell, CPU, RAM, Akku, Grafik, PCI-/USB-Geräte, Treiber und erkannte
Sensorwerte. Fehlen `lspci`, `lsusb` oder `lm_sensors`, bleibt der übrige Bericht nutzbar.
Seriennummern und Machine-ID werden nicht erhoben. „Anonymisierten Bericht kopieren“ wendet
zusätzlich eine Redaction auf unerwartete Identifier an.

# Speicher und Snapshots

„Speicher“ zeigt Laufwerke, Partitionen, Mounts, SMART-Evidenz und optional Btrfs-Nutzung. Die Suche
nach großen Dateien startet nur per Button, bleibt im Benutzer-Home, folgt keinen Symlinks und ist
auf Tiefe, Treffer und Mindestgröße begrenzt. Die Anwendung bietet bewusst keine Mount- oder
Partitionsänderung an.

„Snapshots“ erscheint auch ohne Btrfs/Snapper mit einer verständlichen Nichtverfügbarkeit.
Erstellung benötigt eine validierte Beschreibung und Polkit. Vor einer Löschung muss die angezeigte
Snapshot-ID exakt eingegeben werden; erst danach wird der Helper aufgerufen.

# Dienste und Prozesse

Die Diensteseite kennzeichnet jede Unit als `SYSTEM` oder `USER`. Start, Stop und Restart einer
System-Unit benötigen Polkit; User-Units laufen ohne Root. Logs bleiben lokal und sind auf die
letzten 200 Zeilen begrenzt.

Die Prozessseite zeigt PID, Benutzer, kumulierte CPU-Zeit, residenten Speicher und Nice-Wert.
PID 1/2, bekannte zentrale Prozesse sowie Prozesse mit verborgener Identität sind als `GESCHÜTZT`
markiert. TERM, KILL und Priorität akzeptieren nur PIDs aus der zuletzt aktualisierten Liste.
Für KILL muss die PID zusätzlich exakt eingegeben werden.
