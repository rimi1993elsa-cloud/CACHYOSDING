# Abschlussbericht Phase 11

Datum: 30. Juli 2026

## Implementiert

- Feste Registry für offizielle CachyOS- und ArchWiki-Quellen
- Strikte HTTPS-, Host-, Port- und Userinfo-Validierung
- Begrenzter HTTP-Abruf mit Zeitlimits, fester User-Agent-Kennung und ohne Redirects
- Bewusste Aktualisierung per UI-Button, kein stiller Abruf beim Start
- Siebentägige Aktualitätsstrategie mit sauberem Offline-Fallback
- XDG-JSON-Cache mit Größenlimit, Quellenabgleich und Symlink-Ablehnung
- Atomare Cache-Ersetzung mit sicherem Dateisystem-Fallback
- HTML-Reduktion auf Text und Entfernung aktiver Inhalte
- Neutralisierung typischer eingebetteter Prompt-Injection-Phrasen
- Begrenztes Chunking und deterministisches lexikalisches Retrieval
- Sichtbare Quellen-URLs, Abrufzeitpunkte und Kennzeichnung als untrusted data

## Geänderte Dateien

- Quellen-, Fetch-, Cache-, Safety-, Chunking- und Retrieval-Code in `ai.knowledge`
- Knowledge-Service-Lebenszyklus im Composition Root `app`
- Quellenaktualisierung und -anzeige in der Chatseite `ui`
- Buildkonfiguration und Datenschutz-/Benutzerdokumentation

## Tests

- Nicht-HTTPS- und fremde Hosts werden abgelehnt
- Eingebettete Prompt-Anweisung wird neutralisiert
- Quelle wird abgerufen, gecacht und mit URL wiedergefunden
- Frischer Cache verhindert einen erneuten Netzwerkabruf
- Bestehende Provider-, Router-, UI- und Sicherheitsprüfungen bleiben grün
- Vollständiger Build, Spotless und Checkstyle

Gesamtstand: 71 Tests, 0 Fehler, 0 übersprungen.

## Manuelle Prüfung

Die Anwendung wurde ohne API-Key und ohne automatischen Quellenabruf gestartet. Chat und lokaler
Cache-Fallback bleiben stabil. Die offiziellen CachyOS-Seiten wurden am 30. Juli 2026 auf
Erreichbarkeit und Inhalt geprüft. ArchWiki kann automatisierte Abrufe zeitweise abweisen; in diesem
Fall bleibt der bestehende Cache erhalten und die übrigen Quellen funktionieren weiter.

## Sicherheitsprüfung

- Keine benutzerdefinierten URLs und keine Redirect-Verfolgung
- Nur `wiki.cachyos.org` und `wiki.archlinux.org` über HTTPS
- Antwortgröße, Textgröße, Cachegröße, Chunkgröße und Trefferzahl sind begrenzt
- Aktive HTML-Bestandteile werden verworfen
- Dokumentanweisungen werden neutralisiert und der verbleibende Text bleibt untrusted data
- Cache akzeptiert weder Symlink-Verzeichnis noch Symlink-Zieldatei
- Quelleninhalte haben weiterhin keinen Bezug zum Action Dispatcher
- Kein versteckter Netzwerkabruf beim Appstart

## Bekannte Einschränkungen

- Das lexikalische Retrieval verwendet bewusst keine lokalen Embeddings und ist semantisch
  begrenzter als ein Vektorindex.
- Die kleine Registry deckt Kernbereiche ab und wird später kontrolliert erweitert.
- ArchWiki kann den Java-User-Agent zeitweise mit einem HTTP-Fehler ablehnen.
- Ein cacheloser Offlinebetrieb liefert transparent keinen Dokumentkontext.

## Nächster Schritt

Phase 12 implementiert lokale Diagnoseabläufe für Netzwerk, Audio, Dienste, Boot, Grafik und Pakete,
einen Datenschutz-Sanitizer sowie optionale KI-Erklärung und feste lokale Aktionsbuttons.
