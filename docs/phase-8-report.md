# Abschlussbericht Phase 8

Datum: 30. Juli 2026

## Implementiert

- Austauschbare `SpeechToTextEngine`-Schnittstelle ohne Action- oder KI-Abhängigkeit
- Lokaler Vosk-Adapter für 16 kHz, 16 Bit, Mono
- Auflistung und bewusste Auswahl von Java-Sound-Mikrofonen
- Prüfung und Auswahl eines entpackten lokalen Vosk-Modells
- Push-to-Talk per gedrücktem Button oder Leertaste
- Aufnahme-, Lade-, Stopp- und Fehlerzustände
- Sichtbare Teil- und bestätigte Endtranskripte
- Beenden und Schließen von Aufnahmeleitung, Recognizer und Modell
- Keine Audiodateien und keine automatische Weiterleitung des Transkripts

## Geänderte Dateien

- Vosk-, Mikrofon-, Modell- und Transkriptgrenze in `input`
- Composition Root und Lebenszyklus in `app`
- Sprachseite, Navigation und Tests in `ui`
- Buildkonfiguration und Projektdokumentation

## Tests

- Modellpfad wird nur mit erwarteter Vosk-Struktur akzeptiert
- Fehlendes Modell liefert einen verständlichen Status
- Transkriptereignisse normalisieren Nulltext sicher
- Sprachseite bleibt ohne Modell und Mikrofon bedienbar
- Vollständiger Build, Spotless und Checkstyle

Gesamtstand: 50 Tests, 0 Fehler, 0 übersprungen.

## Manuelle Prüfung

Die Anwendung wurde auf dem Windows-Entwicklungsrechner gestartet. Die Sprachseite und ihr sicherer
Fallback ohne deutsches Modell sind stabil. Eine echte deutsche Erkennung mit Testaudio und internem
Mikrofon muss auf dem CachyOS-Zielgerät anhand des Testplans abgenommen werden.

## Sicherheitsprüfung

- Mikrofon wird beim Auflisten nicht geöffnet
- Aufnahmeleitung wird erst beim bewussten Push-to-Talk geöffnet
- Stoppen und Anwendungsschluss schließen die Aufnahme
- Keine dauerhafte Audiospeicherung
- STT liefert ausschließlich Text und kennt weder Dispatcher noch Online-KI
- Modellpfade werden vor dem Laden auf die erwartete Struktur geprüft

## Bekannte Einschränkungen

- Das deutsche Vosk-Modell wird wegen seiner Größe nicht in das Quellrepository aufgenommen.
- Vosk 0.3.50 ist das aktuelle Upstream-Release; für die Desktop-Java-Bibliothek ist auf Maven
  Central derzeit 0.3.45 verfügbar und deshalb reproduzierbar festgelegt.
- Reale Aufnahme und Erkennungsqualität sind auf CachyOS mit einem deutschen Modell zu prüfen.

## Nächster Schritt

Phase 9 implementiert den gemeinsamen, vollständig lokalen Text-/Voice-Intent-Router mit
Normalisierung, deutschen Synonymen, Mehrdeutigkeitserkennung und ausschließlich registrierten
Action-IDs.
