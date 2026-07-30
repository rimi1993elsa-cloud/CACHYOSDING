#!/usr/bin/env bash
set -euo pipefail

required_files=(
  /usr/bin/cachyos-control-center
  /usr/share/applications/org.cachyos.ControlCenter.desktop
  /usr/share/metainfo/org.cachyos.ControlCenter.metainfo.xml
  /usr/share/dbus-1/system-services/org.cachyos.ControlCenter.Helper1.service
  /usr/share/polkit-1/actions/org.cachyos.controlcenter.policy
  /usr/share/vosk/models/vosk-model-small-de-0.15/am/final.mdl
)

failed=0
for file in "${required_files[@]}"; do
  if [[ -e "${file}" ]]; then
    printf 'OK      %s\n' "${file}"
  else
    printf 'FEHLT   %s\n' "${file}" >&2
    failed=1
  fi
done

if command -v busctl >/dev/null 2>&1; then
  busctl --system status org.cachyos.ControlCenter.Helper1 >/dev/null 2>&1 \
    && echo "OK      D-Bus-Helper ist aktiv." \
    || echo "BEREIT  D-Bus-Helper wird erst bei einer administrativen Aktion aktiviert."
fi

if [[ ${failed} -ne 0 ]]; then
  exit 1
fi
echo "Installierte Dateien sind vollständig. Jetzt GUI-, Mikrofon- und Polkit-Test durchführen."
