# Datenschutz

Lokale Funktionen übertragen keine Daten. Audio wird nur während Push-to-Talk im Arbeitsspeicher
verarbeitet und nicht gespeichert. Plattformdaten werden nur im laufenden Prozess dargestellt.
Lokale technische Logs enthalten keine Benutzereingaben oder Secrets.

Der KI-Chat ist optional. Erst „Frage senden“ übermittelt den sichtbaren Fragetext an OpenAI; lokale
Systemdaten werden in Phase 11 nicht beigefügt. Der Chatverlauf bleibt nur im Arbeitsspeicher und
wird auf 20 Nachrichten begrenzt. Das Modell und die Ausgabegrenze sind vor dem Versand sichtbar.
API-Nutzung kann Kosten verursachen.

Der API-Schlüssel wird bevorzugt über Secret Service/KDE Wallet mittels `secret-tool` gelesen. Für
Entwicklung ist `OPENAI_API_KEY` als nicht persistenter Fallback möglich. Schlüssel werden nicht
geloggt, nicht in SQLite geschrieben und nicht als Prozessargument übergeben.

„Offizielle Quellen aktualisieren“ ruft ausschließlich fest registrierte HTTPS-Seiten von
`wiki.cachyos.org` und `wiki.archlinux.org` ab. Dabei sehen diese Server technisch die öffentliche
IP-Adresse. Es gibt keinen Abruf beim Programmstart. Der bereinigte Textcache liegt unter dem
XDG-Cacheverzeichnis und enthält keine privaten Systemdaten.

Diagnoseberichte werden vor der Anzeige und vor jeder optionalen KI-Übergabe zentral bereinigt.
Maskiert werden insbesondere E-Mail-Adressen, Home-Verzeichnisse, private IPv4-Adressen, Hostnamen,
API-Token, MAC-Adressen, UUIDs und Serienkennungen. „Im KI-Chat erklären“ erstellt lediglich einen
sichtbaren Entwurf; erst ein weiterer Klick auf „Frage senden“ überträgt ihn.
