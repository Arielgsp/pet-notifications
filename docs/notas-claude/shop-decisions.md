---
name: shop-decisions
description: En el lanzamiento solo se vende Pro; accesorios y pelajes a la venta quedan escondidos para después (temporada y precio por región)
metadata:
  node_type: memory
  type: project
  originSessionId: f40a7889-09a3-4241-ae37-bb84b1e06615
  modified: 2026-10-31T20:15:29.795Z
---

**Vigente desde el 07/11/2026:** en el lanzamiento (probablemente la primera semana de diciembre) solo se vende Pro. Los accesorios de temporada y los packs de pelajes quedan en el código, escondidos con un interruptor, para una actualización (pedido de Cowork `2026-11-07-lanzamiento-solo-pro.md`). Cowork propone el gorro navideño gratis del 1/12 al 6/1; falta que Ariel lo confirme. Lo de abajo vale para cuando se vuelvan a vender.

Decidido el 31/10/2026 (etapa 3 del paquete mimupet-avisos-vestidor, versión 1.0.42):
- Los ítems de temporada se ven y se venden **solo durante su temporada**; lo comprado queda para siempre.
- Precio por región, cargado por país en Play Console: ≈ US$2 en Latinoamérica, ≈ US$4 en EE. UU. y Europa. La app muestra el precio que devuelve Google Play.
- Los accesorios del vínculo nunca se venden.

**Why:** son pedidos explícitos del dueño, y el diseño los dejaba como decisión pendiente.
**How to apply:** cuando se agreguen ítems nuevos, ponerles temporada en `Season`/`Accessory` y anotar el ID del producto en PENDIENTES (X13). Ver [[pending-list]] y [[billing-decision]].
