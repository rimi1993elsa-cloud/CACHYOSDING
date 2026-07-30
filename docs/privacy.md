# Datenschutz

Lokale Funktionen übertragen keine Daten. Audio wird nur während Push-to-Talk im Arbeitsspeicher
verarbeitet und nicht gespeichert. Plattformdaten werden nur im laufenden Prozess dargestellt.
Lokale technische Logs enthalten keine Benutzereingaben oder Secrets.

Der KI-Chat ist optional. Erst „Frage senden“ übermittelt den sichtbaren Fragetext an OpenAI; lokale
Systemdaten werden in Phase 10 nicht beigefügt. Der Chatverlauf bleibt nur im Arbeitsspeicher und
wird auf 20 Nachrichten begrenzt. Das Modell und die Ausgabegrenze sind vor dem Versand sichtbar.
API-Nutzung kann Kosten verursachen.

Der API-Schlüssel wird bevorzugt über Secret Service/KDE Wallet mittels `secret-tool` gelesen. Für
Entwicklung ist `OPENAI_API_KEY` als nicht persistenter Fallback möglich. Schlüssel werden nicht
geloggt, nicht in SQLite geschrieben und nicht als Prozessargument übergeben.
