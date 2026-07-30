# Paketierung

Die Paketierung erzeugt vier Split-Packages:

- `cachyos-control-center`
- `cachyos-control-center-helper`
- `cachyos-control-center-stt-de`
- `cachyos-control-center-doc-index`

Für einen lokalen CachyOS/Arch-Build:

```bash
cd packaging
makepkg --syncdeps --cleanbuild
```

Der Repository-PKGBUILD baut bewusst den ausgecheckten Baum eine Ebene oberhalb. Ein
Distributionsrelease muss daraus zuerst ein unveränderliches, checksummiertes Quellarchiv erzeugen
und `source`/`sha256sums` entsprechend setzen. Paketentfernung lässt XDG-Nutzerdaten absichtlich
unangetastet; die Anwendung kann sie auf ausdrücklichen Wunsch löschen.

Das STT-Paket bezieht das offizielle `vosk-model-small-de-0.15` direkt von Alphacephei. Makepkg
prüft das Archiv gegen den fest eingetragenen SHA-256-Wert und installiert es unter
`/usr/share/vosk/models/`. Das Modell wird nicht unversioniert aus dem Netz nachgeladen.
