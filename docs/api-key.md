# API-Key-Hilfe

Die lokale Systemverwaltung benötigt keinen API-Key. Nur der optionale OpenAI-Chat verwendet einen
Schlüssel. Die Anwendung liest ihn unter KDE über Secret Service/KWallet und speichert ihn weder in
Einstellungen noch Exporten oder Logs.

## Sicher hinterlegen

In Version 1.2 wird der Schlüssel bevorzugt direkt unter **Einstellungen → OpenAI-Zugang**
gespeichert oder gelöscht. KWallet beziehungsweise Secret Service zeigt dabei gegebenenfalls einen
eigenen Freigabedialog.

Als Terminal-Fallback installiere unter CachyOS bei Bedarf `libsecret` und führe aus:

```bash
secret-tool store --label="CachyOS Control Center OpenAI" \
  application cachyos-control-center key openai-api-key
```

`secret-tool` fragt den Wert verdeckt ab. Gib den Schlüssel nicht als zusätzliches
Befehlszeilenargument an. Bei externer Änderung die Anwendung neu starten; Änderungen über die
Oberfläche gelten sofort.

Für eine isolierte Entwicklungssitzung ist `OPENAI_API_KEY` als Prozess-Umgebungsvariable möglich.
Das ist kein empfohlener Desktop-Dauerbetrieb. Modell und Ausgabelimit lassen sich mit
`CACHYOS_CC_OPENAI_MODEL` und `CACHYOS_CC_OPENAI_MAX_OUTPUT_TOKENS` festlegen.

## Entfernen und prüfen

Ein Eintrag kann mit folgendem Befehl gelöscht werden:

```bash
secret-tool clear application cachyos-control-center key openai-api-key
```

Ist der Schlüssel nicht vorhanden, zeigt der Chat einen Offlinezustand. Alle lokalen Manager
arbeiten weiter. Das lokale USD-Monatslimit stoppt weitere Anfragen anhand der gemeldeten
Texttokens und der dokumentierten Modellpreise. Kontingente, Tool-Gebühren und die endgültige Abrechnung
werden weiterhin vom API-Konto verwaltet.

## Modellprofil wählen

- **Beste Qualität – GPT-5.6 Sol:** komplexe technische Analysen und schwierige Fragen.
- **Ausgewogen – GPT-5.6 Terra:** guter Alltagspunkt zwischen Qualität und Kosten.
- **Sparsam & schnell – GPT-5.6 Luna:** kurze oder häufige Anfragen mit niedrigerem Preis.

Die Anwendung akzeptiert nur diese drei fest eingebauten Modell-IDs. Freie Modellnamen werden weder
importiert noch an die API weitergereicht.
