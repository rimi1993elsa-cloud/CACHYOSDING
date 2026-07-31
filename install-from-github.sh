#!/usr/bin/env bash
set -euo pipefail

readonly version="1.2.0"
readonly archive_name="CachyOS-Control-Center-${version}.tar.gz"
readonly archive_sha256="5da8a82e5cb133dc19f739db533734bbcda7390ff8a61e61b6dc9c5cd9a829c9"
readonly download_url="https://github.com/rimi1993elsa-cloud/CACHYOSDING/releases/download/v${version}/${archive_name}"

for command_name in curl tar sha256sum; do
  if ! command -v "${command_name}" >/dev/null 2>&1; then
    echo "Benötigtes Programm fehlt: ${command_name}" >&2
    echo "Auf CachyOS installieren mit: sudo pacman -S curl tar coreutils" >&2
    exit 2
  fi
done

if [[ ! -r /etc/arch-release ]]; then
  echo "Dieser Installer unterstützt CachyOS und Arch Linux." >&2
  exit 2
fi

work_dir="$(mktemp -d -t cachyos-control-center.XXXXXXXX)"
archive_path="${work_dir}/${archive_name}"
bundle_path="${work_dir}/CachyOS-Control-Center-${version}"

cleanup() {
  local result=$?
  if (( result == 0 )); then
    rm -rf -- "${work_dir}"
  else
    echo "Der Arbeitsordner mit dem Fehlerprotokoll bleibt erhalten: ${work_dir}" >&2
  fi
}
trap cleanup EXIT

echo "Lade CachyOS Control Center ${version} herunter …"
curl \
  --fail \
  --location \
  --proto '=https' \
  --tlsv1.2 \
  --output "${archive_path}" \
  "${download_url}"

echo "${archive_sha256}  ${archive_path}" | sha256sum --check --status
echo "SHA-256-Prüfung erfolgreich."

tar -xzf "${archive_path}" -C "${work_dir}"
chmod +x "${bundle_path}/install.sh" "${bundle_path}/scripts/"*.sh

bash "${bundle_path}/install.sh"
