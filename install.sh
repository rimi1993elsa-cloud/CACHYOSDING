#!/usr/bin/env bash
set -euo pipefail

bundle_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
log_file="${bundle_root}/install-cachyos.log"

pause_before_exit() {
  if [[ -t 0 ]]; then
    echo
    read -r -p "Zum Schließen Enter drücken …" _
  fi
}

if [[ ! -r /etc/arch-release ]]; then
  echo "Dieser Installer unterstützt CachyOS und Arch Linux." >&2
  pause_before_exit
  exit 2
fi

if [[ ${EUID} -eq 0 ]]; then
  echo "Bitte als normaler Benutzer starten. Das sudo-Passwort wird bei Bedarf abgefragt." >&2
  pause_before_exit
  exit 2
fi

if [[ ! -f "${bundle_root}/scripts/install-cachyos.sh" ]]; then
  echo "Das Installationspaket ist unvollständig: scripts/install-cachyos.sh fehlt." >&2
  pause_before_exit
  exit 2
fi

chmod +x \
  "${bundle_root}/scripts/install-cachyos.sh" \
  "${bundle_root}/scripts/verify-linux.sh" \
  "${bundle_root}/scripts/verify-installed.sh"

echo "CachyOS Control Center – One-Click-Installation"
echo "================================================"
echo
echo "Der Installer lädt benötigte Pakete und das deutsche Vosk-Modell,"
echo "prüft den vollständigen Quellstand und installiert vier Arch-Pakete."
echo "Das sudo-Passwort wird ausschließlich von pacman/makepkg abgefragt."
echo
echo "Installationsprotokoll: ${log_file}"
echo

set +e
"${bundle_root}/scripts/install-cachyos.sh" 2>&1 | tee "${log_file}"
result=${PIPESTATUS[0]}
set -e

if (( result != 0 )); then
  echo
  echo "Die Installation wurde mit Fehlercode ${result} beendet." >&2
  echo "Bitte sende bei Rückfragen die Datei: ${log_file}" >&2
  pause_before_exit
  exit "${result}"
fi

echo
echo "Fertig. Die App ist jetzt im KDE-Anwendungsmenü verfügbar."
echo "Alternativ im Terminal starten: cachyos-control-center"
echo "Installationsprüfung: cachyos-control-center-verify"
pause_before_exit
