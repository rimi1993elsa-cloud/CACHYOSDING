# Benutzerhandbuch – Phase 6

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

Auf der Übersicht stehen vier Schnellaktionen bereit:

- Firefox öffnen
- Dateimanager öffnen
- Terminal öffnen
- Bildschirm sperren

Jeder Button ist fest mit einer registrierten Action-ID verbunden. Wenn das benötigte Programm
nicht installiert oder nicht im sicheren Suchpfad auffindbar ist, erscheint eine verständliche
Meldung. Die Anwendung fordert für diese Aktionen keine erhöhten Rechte an.
