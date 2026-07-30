# Changelog

Alle nennenswerten Änderungen werden in dieser Datei dokumentiert. Das Format orientiert sich an
[Keep a Changelog](https://keepachangelog.com/de/1.1.0/).

## Unreleased

### Added

- Separater, per System-D-Bus aktivierbarer privilegierter Helper
- Acht typisierte administrative Methoden ohne generische Shell- oder Executable-Schnittstelle
- Polkit-Autorisierung anhand des eindeutigen System-Bus-Absenders
- Doppelte Parameterprüfung an Transport- und privilegierter Fachgrenze
- Absolute Executable-Allowlist für Pacman, systemd, Snapper und Prozesssignale
- Zeitlimits, begrenzte Fehlercodes und parameterfreies strukturiertes Audit
- D-Bus-Introspection, restriktive Bus-Policy und fünf minimale Polkit-Aktionen
- Manipulations-, Autorisierungs-, Protokoll- und Timeout-Tests für den Helper
- Lokaler Diagnosemanager für Netzwerk, Audio, Dienste, Boot, Grafik und Pakete
- Serielle, abbrechbare Diagnoseausführung außerhalb des JavaFX-Threads
- Zentraler Sanitizer für Kontaktdaten, Pfade, IPs, Secrets und Hardwarekennungen
- Bereinigter Diagnosebericht mit optionaler, zweistufiger KI-Erklärung
- Ausschließlich feste lokale Abhilfemaßnahmen für passende Warnungen
- Allowlist-basierte CachyOS-/ArchWiki-Quellenregistry
- Explizit ausgelöster, begrenzter HTTPS-Abruf mit siebentägiger Cache-Strategie
- Symlink-resistenter XDG-Wissenscache mit atomaren Ersetzungen
- Textbereinigung und Neutralisierung eingebetteter Prompt-Injection-Phrasen
- Lokales Chunking und lexikalisches Retrieval mit Quellen-URL und Abrufstand
- Sichtbare RAG-Quellen und als untrusted data markierter KI-Kontext
- Optionale, streamende OpenAI-Responses-API-Anbindung über das offizielle Java-SDK 4.43.0
- Rein textbasierte `AiProvider`-Grenze ohne Action- oder Plattformabhängigkeit
- Chatseite mit Abbruch, Offlinezustand, Modell-/Tokenanzeige und Kostenhinweis
- Secret-Service/libsecret-Lookup mit nicht persistentem Umgebungsfallback
- Konfigurierbares Modell und Ausgabegrenze über validierte Umgebungsvariablen
- Sicherheits-Systemprompt gegen Ausführungsbehauptungen und Prompt Injection aus Kontextdaten
- Deterministischer deutscher Offline-Intent-Router für Aktion, Navigation, Frage und Unklarheit
- Gemeinsame Auswertung für Texteingabe und bewusst übernommene Sprachtranskripte
- Synonyme, Normalisierung, Konfidenz und Mehrdeutigkeitserkennung
- Sicheres Starten exakt katalogisierter Anwendungen über deren interne ID
- Zusätzliche Bestätigung für sitzungsverändernde Spracheingaben
- Austauschbare Vosk-Speech-to-Text-Grenze mit deutschem 16-kHz-Mono-Profil
- Mikrofon- und lokale Modellwahl mit expliziter Verfügbarkeitsprüfung
- Push-to-Talk per Button oder Leertaste mit sichtbaren Teil- und Endtranskripten
- Aufnahme nur während aktiver Bedienung und ohne dauerhafte Audiospeicherung
- XDG-Anwendungsmanager mit Suche, Icons und Sitzungsfavoriten
- Sicherer Desktop-Exec-Parser mit Feldcodebehandlung und expliziter Shell-Ablehnung
- ID-only Anwendungsstart und verständliche Startfehler
- Bedarfsgesteuerte Paketzuordnung über `pacman -Qoq`
- PipeWire-Audiomanager für Ausgaben, Mikrofone, Standardgeräte und Streams
- Validierte Lautstärke- und Mute-Aktionen bis maximal 150 Prozent
- Ereignisbasierte Audioaktualisierung über `pactl subscribe`
- Optionaler Testton über fest erkanntes `pw-play` und eine feste Systemklangdatei
- Jackson 2.22.0 für strukturierte, lokalisierungsunabhängige Adapterdaten
- NetworkManager-Seite für Geräte, WLANs, Profile, VPNs, Gateway und DNS
- Ereignisbasierte Netzwerkaktualisierung über den festen `nmcli monitor`-Aufruf
- Validierte Aktionen für WLAN, Scan, gespeicherte Profile und Gerätetrennung
- Sicherer Offline- und Fehlende-`nmcli`-Fallback ohne Passwortverarbeitung
- Lastarm aktualisiertes Dashboard für CPU, RAM, Speicher, Netzwerk und Akku
- Gecachter Update- und fehlgeschlagener-Dienste-Status über feste read-only Argumentlisten
- Schwellenwertbasierte Warnungen und Anzeige der letzten lokalen Aktionen
- Schreibgeschützter System-Snapshot für Distribution, Kernel, Sitzung und Hardware
- Dynamische Erkennung von Speicher, Akku, Netzwerk und Bootmanager
- Zentrale Capability Registry mit Gründen und Installationshinweisen für fehlende Werkzeuge
- Reale, scrollbare Systemansicht mit sicheren Fallbacks
- Typisierte Action-Requests und -Results mit allowlist-basierter Registry
- Asynchroner Dispatcher mit Fehlerabbildung und parameterfreiem Audit
- Modulregistrierung und gemeinsame Manager-Schnittstelle
- Sichere Desktop-Schnellaktionen für Firefox, Dateimanager, Terminal und Bildschirmsperre
- Responsive Anwendungsshell mit Sidebar, Topbar, Status- und Eingabebereich
- Tastaturbedienbare Navigation mit ehrlicher Modulverfügbarkeit
- Light-, Dark- und System-Theme
- Wiederverwendbare Statuskarten und nicht blockierende Toast-Benachrichtigungen
- Gradle-Multi-Modul-Grundgerüst mit Java-21-Toolchain
- Minimales JavaFX-Hauptfenster
- Sichere Plattform-Erkennung ohne Prozess- oder Dateizugriff
- XDG-konforme Anwendungspfade
- JUnit 5, Spotless und Checkstyle
- Rotierendes, lokales Anwendungs-Logging
- Architektur-, Sicherheits- und Entwicklungsdokumentation
- GitHub-Actions-Build
