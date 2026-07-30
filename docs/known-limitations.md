# Bekannte Einschränkungen in Version 1.0

- AUR-Verwaltung ist nicht aktiviert.
- Bootdateien, Kernel und Partitionstabellen werden nicht direkt verändert; der CachyOS Kernel
  Manager kann nur über einen fest erwarteten Eintrag geöffnet werden.
- Das lokale KI-Budget ist eine Warnschwelle und keine serverseitige Kostensperre.
- Vosk-Sprachmodelle werden aus Lizenz- und Größen­gründen nicht automatisch heruntergeladen.
- Wayland-/KDE-Funktionen hängen von den lokal vorhandenen Portalen und Werkzeugen ab.
- Arch besitzt keinen getrennten Security-Updatekanal; die Sicherheitsseite zeigt daher den
  allgemeinen Paketstand.
- Hardware- und Firmwarewerte bleiben „unbekannt“, wenn Kernel oder Hersteller sie nicht liefern.
- Die abschließende Paket-, KDE-, Polkit- und D-Bus-Systemprüfung muss auf einem realen
  CachyOS-Zielsystem erfolgen; der Windows-Entwicklungsrechner kann diese Integration nicht
  zertifizieren.
