# Modulstatus

| Modulgruppe | Phase 0 | Geplante Aktivierung |
|---|---|---|
| App/Core/UI | Shell, lokale Texteingabe und sichere Schnellbuttons aktiv | Phase 10 |
| Desktop-Integration | Vier unprivilegierte, feste Aktionen aktiv | Phase 9 |
| System-Info | Snapshot, Capability Registry und Dashboard-Monitor aktiv | Phase 9 |
| Netzwerk | NetworkManager-Status, Ereignisse und sichere Aktionen aktiv | Phase 9 |
| Audio | PipeWire-Pulse-Geräte, Streams und Mixeraktionen aktiv | Phase 9 |
| Anwendungen | XDG-Katalog, Suche, Favoriten, Icons und sicherer Start aktiv | Phase 9 |
| Input/STT/Intent | Vosk und lokaler Text-/Voice-Intent-Router aktiv | Phase 10 |
| KI/Chat | Responses-Streaming, Secret-Service und Offlinezustand aktiv | Phase 12 |
| Knowledge/RAG | offizielle Quellen, Cache, Retrieval und Quellenanzeige aktiv | Phase 12 |
| Diagnose | sechs lokale Probes, Sanitizer und feste Abhilfen aktiv | Phase 13 |
| Helper | D-Bus, Polkit, Allowlist, Audit und Timeouts aktiv | Phase 13 |
| Pakete | Pacman-Lesen, Vorschau und Helper-Mutation aktiv | Phase 14 |
| Weitere Manager | nur Projektstruktur | Phase 15–21 |
| Packaging | nur Verzeichnisstruktur | Phase 24 |

Eine vorhandene Projektstruktur bedeutet ausdrücklich nicht, dass das jeweilige Fachmodul bereits
implementiert oder in der Oberfläche freigeschaltet ist.
# Paketmanager

`modules/packages` koordiniert lesende Snapshots, Suche, Details, Fortschritt und bestätigte
Transaktionen. `platform-linux` implementiert Pacman-Abfragen und den D-Bus-Gateway. Eine
Installation oder Entfernung ist nur mit einer höchstens zwei Minuten alten Vorschau-ID möglich;
bei `/var/lib/pacman/db.lck` wird vor Vorschau, vor D-Bus-Aufruf und nochmals im Helper abgebrochen.
