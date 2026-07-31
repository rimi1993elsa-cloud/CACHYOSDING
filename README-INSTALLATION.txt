CachyOS Control Center 1.2.0 – Installation
================================================

Ein Befehl für CachyOS
----------------------

In einer normalen Shell als Benutzer ausführen:

  bash <(curl -fsSL https://raw.githubusercontent.com/rimi1993elsa-cloud/CACHYOSDING/master/install-from-github.sh)

Der Befehl lädt das veröffentlichte Paket, prüft dessen SHA-256-Prüfsumme
und startet anschließend die vollständige Installation. Nicht als root starten;
sudo wird bei den erforderlichen Paketoperationen automatisch abgefragt.

Empfohlener Weg
----------------

1. Die Datei CachyOS-Control-Center-1.2.0.tar.gz auf den CachyOS-Rechner kopieren.
2. Das Archiv im Dateimanager Ark entpacken.
3. Den entpackten Ordner öffnen.
4. Auf „Installieren.desktop“ doppelklicken.
5. Falls KDE nachfragt, einmal „Vertrauen und starten“ bestätigen.
6. Das sudo-Passwort eingeben und die Prüfungen abwarten.

Nach erfolgreicher Installation erscheint „CachyOS Control Center“ im
KDE-Anwendungsmenü. Der erste Build kann abhängig von Internetverbindung und
Rechner einige Minuten dauern.

Manueller Ersatzweg
-------------------

Im entpackten Ordner ein Terminal öffnen und ausführen:

  ./install.sh

Falls der Ordner nicht aus dem tar.gz-Archiv stammt und Ausführungsrechte fehlen:

  chmod +x Installieren.desktop install.sh scripts/*.sh
  ./install.sh

Voraussetzungen
---------------

- CachyOS oder Arch Linux auf x86_64
- Internetzugang
- normaler Benutzer mit sudo-Berechtigung
- ausreichend freier Speicher für Build und deutsches Vosk-Sprachmodell

Der Installer
-------------

- installiert die benötigten Arch-Buildabhängigkeiten,
- führt Quell-, Test-, Sicherheits- und Paketprüfungen aus,
- lädt das geprüfte deutsche Vosk-Modell,
- baut vier lokale Arch-Pakete mit makepkg,
- installiert die Anwendung, den Polkit-Helper, Vosk und den Dokumentindex.

Die sudo-Abfrage erfolgt durch pacman/makepkg. Die Anwendung selbst bekommt
keine pauschalen Root-Rechte.

Nach der Installation
---------------------

Start:

  cachyos-control-center

Installations- und Laufzeitprüfung:

  cachyos-control-center-verify

Bei einem Fehler liegt das Protokoll im entpackten Ordner:

  install-cachyos.log

Deinstallation
--------------

  sudo pacman -Rns cachyos-control-center \
    cachyos-control-center-helper \
    cachyos-control-center-stt-de \
    cachyos-control-center-doc-index

Persönliche Einstellungen und Verlauf werden bei der Paketdeinstallation
absichtlich nicht automatisch gelöscht.
