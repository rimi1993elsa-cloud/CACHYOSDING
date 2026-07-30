# API-Key-Hilfe

Die lokale Systemverwaltung benötigt keinen API-Key. Nur der optionale OpenAI-Chat verwendet einen
Schlüssel. Die Anwendung liest ihn unter KDE über Secret Service/KWallet und speichert ihn weder in
Einstellungen noch Exporten oder Logs.

## Sicher hinterlegen

Installiere unter CachyOS bei Bedarf `libsecret` und führe in einem Terminal aus:

```bash
secret-tool store --label="CachyOS Control Center OpenAI" \
  application cachyos-control-center key openai-api-key
```

`secret-tool` fragt den Wert verdeckt ab. Gib den Schlüssel nicht als zusätzliches
Befehlszeilenargument an. Anschließend die Anwendung neu starten.

Für eine isolierte Entwicklungssitzung ist `OPENAI_API_KEY` als Prozess-Umgebungsvariable möglich.
Das ist kein empfohlener Desktop-Dauerbetrieb. Modell und Ausgabelimit lassen sich mit
`CACHYOS_CC_OPENAI_MODEL` und `CACHYOS_CC_OPENAI_MAX_OUTPUT_TOKENS` festlegen.

## Entfernen und prüfen

Ein Eintrag kann mit folgendem Befehl gelöscht werden:

```bash
secret-tool clear application cachyos-control-center key openai-api-key
```

Ist der Schlüssel nicht vorhanden, zeigt der Chat einen Offlinezustand. Alle lokalen Manager
arbeiten weiter. Kosten, Kontingente und Sperren werden vom API-Konto verwaltet; die lokale
Budgetangabe ist nur eine Warnschwelle.

## Modellprofil wählen

- **Beste Qualität – GPT-5.6 Sol:** komplexe technische Analysen und schwierige Fragen.
- **Ausgewogen – GPT-5.6 Terra:** guter Alltagspunkt zwischen Qualität und Kosten.
- **Sparsam & schnell – GPT-5.6 Luna:** kurze oder häufige Anfragen mit niedrigerem Preis.

Die Anwendung akzeptiert nur diese drei fest eingebauten Modell-IDs. Freie Modellnamen werden weder
importiert noch an die API weitergereicht.
