# Aktionsmodell

Phase 0 implementiert keine Systemaktion.

Ab Phase 2 werden ausschließlich typisierte `ActionId`, `ActionRequest` und `ActionResult`-Objekte
registriert. Unbekannte IDs werden abgelehnt. Parameter werden an der Action-Grenze und bei
privilegierten Methoden erneut validiert. Eine API für freien Shelltext wird nicht eingeführt.

