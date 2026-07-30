# CachyOS Control Center AI 1.0.0

Version 1.0 bündelt eine deutschsprachige JavaFX-Verwaltungsoberfläche für CachyOS. Sie erkennt
lokale Fähigkeiten, degradiert bei fehlenden Werkzeugen nachvollziehbar und trennt lesende
Desktopfunktionen strikt vom optionalen Root-Helper.

Enthalten sind Dashboard, System-, Netzwerk-, Audio-, Programm-, Paket-, Sicherheits-, Hardware-,
Speicher-, Snapshot-, Dienst-, Prozess-, Anzeige-, Energie- und Bootansichten. Ergänzt werden lokale
Diagnose, Vosk-Push-to-Talk, ein fester deutscher Intent-Router, optionaler OpenAI-Chat und ein nur
manuell aktualisierter Wissenscache offizieller Quellen.

Sicherheitsrelevante Änderungen benötigen Vorschau, Bestätigung, Polkit, feste Action-IDs und eine
zweite Helper-Validierung. Der Chat besitzt keinen Aktionskanal. Einstellungen sind atomar,
symlink-resistent und secret-frei exportierbar.

Die CachyOS-Paketierung liefert getrennte Pakete für Oberfläche, Helper, STT-Hilfe und
Dokumentindex sowie KDE-, AppStream-, D-Bus- und Polkit-Integration. Hinweise zur Zielsystemabnahme
stehen im [Phase-25-Bericht](phase-25-report.md).
