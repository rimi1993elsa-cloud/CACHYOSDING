#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"

if [[ ! -r /etc/arch-release ]]; then
  echo "Dieses Installationsskript unterstützt CachyOS und Arch Linux." >&2
  exit 2
fi
if [[ ${EUID} -eq 0 ]]; then
  echo "Nicht als root starten. makepkg verwendet sudo nur für Paketoperationen." >&2
  exit 2
fi

echo "Installiere reproduzierbare Build-Abhängigkeiten …"
sudo pacman -S --needed base-devel jdk21-openjdk libsecret

echo "Führe Quell-, Test- und Paketprüfung aus …"
"${repo_root}/scripts/verify-linux.sh"

echo "Baue und installiere die vier lokalen Pakete …"
(
  cd "${repo_root}/packaging"
  makepkg --clean --cleanbuild --force --install --syncdeps
)

echo
echo "Installation abgeschlossen."
echo "Starte die App über das KDE-Menü oder mit: cachyos-control-center"
echo "API-Key, Vosk und Systemstatus lassen sich unter Einstellungen prüfen."
