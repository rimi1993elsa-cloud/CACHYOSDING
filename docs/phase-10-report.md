# Abschlussbericht Phase 10

Datum: 30. Juli 2026

## Implementiert

- Provider-Abstraktion für ausschließlich textbasierte, streamende Antworten
- OpenAI-Adapter über Responses API und offizielles Java-SDK 4.43.0
- Konfigurierbares Modell `gpt-5.6-sol` und validierte Ausgabegrenze
- Fester deutscher Sicherheits-Systemprompt
- Abbruchbarer Hintergrundstream ohne Blockade des JavaFX-Threads
- Verständliche Offline-, Fehler- und Rate-Limit-Meldungen
- API-Key-Lookup über Secret Service/libsecret und `secret-tool`
- Nicht persistenter `OPENAI_API_KEY`-Fallback für Entwicklung
- Chatseite mit bewusstem Senden, Abbruch, flüchtigem Verlauf und Kostenhinweis
- Übergabe erkannter Fragen nur als Entwurf, niemals automatischer Versand

## Geänderte Dateien

- Provider-, Request-, Stream-, Konfigurations-, Prompt- und OpenAI-Code in `ai`
- Secret-Service-Adapter in `platform-linux`
- Chatseite und Navigation in `ui`
- Composition Root und Lebenszyklus in `app`
- Buildkonfiguration, Datenschutz- und Benutzerdokumentation

## Tests

- Sicherheits-Systemprompt deklariert die fehlenden Ausführungsrechte
- Freigegebener Kontext wird ausdrücklich als untrusted data markiert
- Provider bleibt ohne Secret offline und liefert einen verständlichen Fehler
- Modell- und Tokenkonfiguration werden validiert
- Modellnamen mit Shellsyntax werden abgelehnt
- Secret-Service-Fallback und unbekannte Secret-Namen
- Frage wird nur als Chatentwurf übernommen und nicht dispatcht
- Vollständiger Build, Spotless und Checkstyle

Gesamtstand: 68 Tests, 0 Fehler, 0 übersprungen.

## Manuelle Prüfung

Die Anwendung wurde ohne API-Schlüssel gestartet. Der KI-Assistent zeigt den Offlinegrund,
deaktiviert Senden und beeinträchtigt keinen lokalen Manager. Eine kostenpflichtige Live-Anfrage
wurde bewusst nicht simuliert. Sie ist auf dem Zielsystem mit einem eigenen Testschlüssel und
Kostenlimit anhand des Testplans abzunehmen.

## Sicherheitsprüfung

- Das `ai`-Modul hat keine Projektabhängigkeit zum Core-Dispatcher oder zu Plattformadaptern.
- Der Provider erhält nur Frage, begrenzten Verlauf und später explizit freigegebenen Kontext.
- KI-Ausgaben bleiben Text und können keine Action-ID dispatchen.
- Fragen werden nicht automatisch online gesendet.
- API-Key wird weder geloggt, gespeichert noch als Prozessargument verwendet.
- Kontextdaten werden im Systemprompt als untrusted data behandelt.
- Anfragen sind abbrechbar; Providerressourcen werden beim Anwendungsschluss beendet.

## Bekannte Einschränkungen

- Eine Live-Abnahme benötigt einen nutzereigenen API-Schlüssel und kann Kosten verursachen.
- Die automatische Secret-Einrichtung in der UI folgt mit den Einstellungen in Phase 21.
- Lokaler Systemkontext ist standardmäßig leer; Redaction und Freigabekategorien folgen in
  Phase 12 beziehungsweise 21.
- Exakte Kosten können wegen providerabhängiger Preise nicht offline berechnet werden.

## Nächster Schritt

Phase 11 implementiert eine quellenbasierte CachyOS-/Arch-Wissensbasis mit Registry, sicherem Abruf,
Cache, Chunking, Retrieval, Aktualitätsanzeige und Prompt-Injection-Abwehr.
