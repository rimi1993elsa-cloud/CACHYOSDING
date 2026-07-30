# CachyOS Control Center AI 1.1.0

Version 1.1 macht Sprache und Online-KI ohne manuelle technische Konfiguration nutzbar.

Das optionale Paket `cachyos-control-center-stt-de` enthält jetzt das offizielle
`vosk-model-small-de-0.15` vollständig. Makepkg lädt das 45-MB-Archiv von Alphacephei, prüft den
fest hinterlegten SHA-256-Wert und installiert Modell sowie Apache-2.0-Lizenz. Die Anwendung findet
den Systempfad automatisch; eigene kompatible Modelle bleiben auswählbar. Quelle:
[offizielle Vosk-Modellliste](https://alphacephei.com/vosk/models).

Für OpenAI stehen drei Profile zur Auswahl:

- GPT-5.6 Sol – beste Qualität
- GPT-5.6 Terra – ausgewogen
- GPT-5.6 Luna – sparsam und schnell

Die Modellauswahl ist Teil des secret-freien, validierten Einstellungsschemas. Der Provider liest
sie für jede neue Responses-API-Anfrage erneut. Dadurch ist kein Neustart notwendig und freie oder
manipulierte Modell-IDs werden abgelehnt. Die Profile folgen der
[offiziellen OpenAI-Modellauswahl](https://developers.openai.com/api/docs/models).
