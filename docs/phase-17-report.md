# Abschlussbericht Phase 17

Datum: 30. Juli 2026

## Implementiert

- `lsblk`-basierte Laufwerks-/Partitionsstruktur
- `findmnt`-basierte Mountübersicht einschließlich Optionen
- optionale SMART-Gesundheit für streng validierte Blockgeräte
- explizite, begrenzte Großdateisuche im Benutzer-Home
- Btrfs-Root-Erkennung und lesende Dateisystemnutzung
- optionale Snapper-Liste mit robustem CSV-Parser
- Snapshot-Erstellung und -Löschung über den privilegierten Helper

## Schutzmaßnahmen

Die Anwendung mountet und partitioniert nicht. Dateianalyse folgt keinen Symlinks, bleibt unter dem
normalisierten Home und begrenzt Tiefe, Kandidaten und Ergebniszahl. SMART akzeptiert ausschließlich
klassische SATA-, NVMe- und MMC-Blockgerätepfade. Snapshot-Beschreibungen sind allowlist-validiert;
Löschung verlangt exakte ID-Eingabe sowie Polkit.

## Tests

- Normales System ohne Btrfs/Snapper liefert stabile Fallbacks.
- Verschachtelte `lsblk`-Strukturen werden geparst.
- Snapper-CSV mit Komma in Anführungszeichen bleibt korrekt.
- Großdateianalyse erhält ausschließlich das konfigurierte Home.
- Falsche Löschbestätigung erreicht den Gateway nicht.
- Speicher- und Snapshotseiten sind navigierbar.

## Zielsystemtest

SMART-Ausgabe, Btrfs-Subvolumes, Snapper-Konfiguration und echte Polkit-Aktionen müssen auf CachyOS
mit den dort vorhandenen Laufwerken abgenommen werden. Es werden keine Test-Snapshots auf dem
Windows-Entwicklungsrechner simuliert.

## Nächster Schritt

Phase 18 implementiert getrennte System-/User-Units, Logs und Prozessverwaltung mit Markierung
kritischer Prozesse und validierten Signalen/Prioritäten.
