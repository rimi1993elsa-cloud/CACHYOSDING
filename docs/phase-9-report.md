# Abschlussbericht Phase 9

Datum: 30. Juli 2026

## Implementiert

- Gemeinsamer deterministischer Router für Text und bewusst übernommene Sprachtranskripte
- Deutsche Kleinschreibung, Umlaut-, Satzzeichen- und Leerraumnormalisierung
- Lokale Synonyme für sichere Standardaktionen und Navigation
- Getrennte Ergebnisse für Aktion, Navigation, Frage, Mehrdeutigkeit und unbekannte Eingabe
- Konfidenzwert und explizites Bestätigungsmerkmal
- Start exakt katalogisierter Anwendungen ausschließlich über die interne Anwendungs-ID
- Zusätzlicher Bestätigungsdialog für das Sperren der Sitzung
- Aktives Texteingabefeld und expliziter Auswertungsbutton auf der Sprachseite
- Ausführungsfreie Behandlung von Fragen, Shelltext und unklaren Eingaben

## Geänderte Dateien

- Intent-Modelle, Katalogprojektion und deutscher Router in `input`
- Router-Erzeugung im Composition Root `app`
- Gemeinsame Eingabeverarbeitung, Bestätigung und Navigation in `ui`
- Unit- und UI-Tests sowie Projektdokumentation

## Tests

- Umlaute, Satzzeichen und Leerraum werden stabil normalisiert
- Standardaktion und Navigationsziel werden offline erkannt
- Sperrbefehl ist bestätigungspflichtig
- Exakter Anwendungsname erzeugt nur die katalogisierte ID
- Mehrdeutiger Anwendungsname erzeugt keine Action
- Frage und Shelltext erzeugen keine Action
- Texteingabe verwendet den typisierten Dispatcher mit Quelle `TEXT`
- Unbekannte UI-Eingabe dispatcht nichts
- Vollständiger Build, Spotless und Checkstyle

Gesamtstand: 60 Tests, 0 Fehler, 0 übersprungen.

## Manuelle Prüfung

Die Anwendung wurde auf dem Windows-Entwicklungsrechner gestartet. Texteingabe, Navigation und
sichere Fallback-Meldungen bleiben responsiv. Reale Anwendungsnamen und Voice-to-Intent sind auf
dem CachyOS-Zielsystem mit dessen XDG-Katalog und deutschem Vosk-Modell praktisch abzunehmen.

## Sicherheitsprüfung

- Der Router besitzt keine Dispatcher-Referenz und kann sich nicht selbst ausführen.
- Nur im Code registrierte Action-IDs werden erzeugt.
- Anwendungsnamen werden nie als Prozessargument verwendet; nur katalogisierte IDs passieren.
- Unbekannte, mehrdeutige und als Frage erkannte Eingaben bleiben ausführungsfrei.
- Shellsyntax und freie Befehle werden nicht interpretiert.
- Sitzungsverändernde Befehle verlangen eine sichtbare Bestätigung.
- Sprachtext wird erst nach bewusstem Klick ausgewertet.

## Bekannte Einschränkungen

- Der absichtlich kleine Regelkatalog deckt nur eindeutig definierte Standardbefehle ab.
- Fragen werden bis Phase 10 nur klassifiziert, nicht beantwortet.
- Mehrdeutige Anwendungen werden nicht interaktiv zur Auswahl angeboten.

## Nächster Schritt

Phase 10 implementiert eine strikt lesende KI-Provider-Grenze, Secret-Store-Anbindung, Streaming-
Chat, konfigurierbares Modell, Kostenhinweise und vollständigen Offline-Fallback ohne Bezug zum
Action Dispatcher.
