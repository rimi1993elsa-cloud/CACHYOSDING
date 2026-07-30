# Bekannte Einschränkungen in Version 1.2

- AUR-Verwaltung ist nicht aktiviert.
- Bootdateien, Kernel und Partitionstabellen werden nicht direkt verändert; der CachyOS Kernel
  Manager kann nur über einen fest erwarteten Eintrag geöffnet werden.
- Das lokale KI-Budget ist eine USD-Sperre auf Basis der von der Responses API gemeldeten
  Texttokens und der für Version 1.2 dokumentierten Modellpreise. Anbieterpreise, Währungskurse und
  kostenpflichtige Tool-Aufrufe können sich ändern; das OpenAI-Projektbudget bleibt maßgeblich.
- Das mitgelieferte kompakte deutsche Vosk-Modell priorisiert geringe Größe und Offlinebetrieb; für
  höhere Erkennungsgenauigkeit kann weiterhin manuell ein großes kompatibles Modell gewählt werden.
- Wayland-/KDE-Funktionen hängen von den lokal vorhandenen Portalen und Werkzeugen ab.
- Arch besitzt keinen getrennten Security-Updatekanal; die Sicherheitsseite zeigt daher den
  allgemeinen Paketstand.
- Hardware- und Firmwarewerte bleiben „unbekannt“, wenn Kernel oder Hersteller sie nicht liefern.
- Die abschließende Paket-, KDE-, Polkit- und D-Bus-Systemprüfung muss auf einem realen
  CachyOS-Zielsystem erfolgen; der Windows-Entwicklungsrechner kann diese Integration nicht
  zertifizieren.
