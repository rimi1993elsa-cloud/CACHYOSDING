#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${repo_root}"

if [[ "$(uname -s)" != "Linux" ]]; then
  echo "Die Zielsystemprüfung muss unter Linux laufen." >&2
  exit 2
fi

required=(java makepkg)
for command_name in "${required[@]}"; do
  if ! command -v "${command_name}" >/dev/null 2>&1; then
    echo "Fehlendes Pflichtwerkzeug: ${command_name}" >&2
    exit 3
  fi
done

java_major="$(java -version 2>&1 | sed -n '1s/.*version "\([0-9]*\).*/\1/p')"
if [[ -z "${java_major}" || "${java_major}" -lt 21 ]]; then
  echo "Java 21 oder neuer wird benötigt." >&2
  exit 3
fi

echo "[1/5] Gradle Build, Tests und Qualitätsregeln"
./gradlew --no-daemon build quality verifyPackaging \
  :app:installDist :helper:privileged-helper:installDist

echo "[2/5] PKGBUILD-Quellen und Prüfsummen"
(
  cd packaging
  makepkg --verifysource
)

echo "[3/5] Desktop- und AppStream-Metadaten"
if command -v desktop-file-validate >/dev/null 2>&1; then
  desktop-file-validate packaging/desktop/org.cachyos.ControlCenter.desktop
else
  echo "Hinweis: desktop-file-validate ist nicht installiert."
fi
if command -v appstreamcli >/dev/null 2>&1; then
  appstreamcli validate --no-net packaging/appstream/org.cachyos.ControlCenter.metainfo.xml
else
  echo "Hinweis: appstreamcli ist nicht installiert."
fi

echo "[4/5] Sicherheitsgrenzen"
if grep -R -n -E 'Runtime\.getRuntime\(\)\.exec|ProcessBuilder\("sh",[[:space:]]*"-c"' \
    --include='*.java' .; then
  echo "Verbotene freie Shell-Ausführung gefunden." >&2
  exit 4
fi
if grep -R -n -E 'sk-[A-Za-z0-9_-]{20,}' \
    --exclude='*.zip' --exclude='*.pkg.tar.*' --exclude-dir=.git --exclude-dir=build .; then
  echo "Möglicher API-Schlüssel im Projekt gefunden." >&2
  exit 4
fi

echo "[5/5] Zielsystemfähigkeiten"
for command_name in nmcli pactl wpctl pacman systemctl journalctl secret-tool; do
  if command -v "${command_name}" >/dev/null 2>&1; then
    printf '  OK      %s\n' "${command_name}"
  else
    printf '  OPTIONAL %s fehlt; zugehörige Funktionen werden deaktiviert.\n' "${command_name}"
  fi
done

echo "Linux-Zielsystemprüfung erfolgreich."
