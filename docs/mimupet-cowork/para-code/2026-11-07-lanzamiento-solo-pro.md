# Pedido de Cowork · 07/11/2026 · Lanzamiento con un solo producto: Pro

Lo decidió Ariel. El lanzamiento será probablemente en la **primera semana de diciembre**.

**Idea:** al principio, nada para pagar salvo Pro. La app se tiene que valorar por lo que hace. Lo demás sale más adelante, en actualizaciones.

**Regla general:** no borrar nada. Todo lo que sale de la venta queda en el código, **escondido con un interruptor** (como `PetType.WILD_PACK_OUT`), para activarlo con una actualización.

## Qué queda en el lanzamiento

- **Gratis:** 1 mascota, los avisos completos, el vínculo con sus premios ganados (pañuelo, camita, gorrito y corona), la Nubecita y el modo fantasma. Todo como hoy.
- **Pro** (pago único, con prueba de 7 días): las 7 mascotas, el globo Caramelo, el tamaño y la transparencia, y lo demás que ya trae hoy. **Salvo «Adelantar todo el camino»** (ver abajo).
- **Ningún otro producto a la venta.**

## Qué se esconde (interruptor apagado)

1. **Packs de pelajes (`coats_*`).** No se ven en ningún lado: ni la fila de pelajes con candado, ni «Probar 5 min · Comprar», ni la fila en la tabla de planes, ni la web. Todas las mascotas usan el pelaje original.
   - Ojo: hoy la prueba de Pro deja usar todos los pelajes. Con el interruptor apagado, tampoco.
2. **Accesorios de temporada a la venta** (`acc_xmas_hat`, `acc_pumpkin`, `acc_pumpkin_helmet`). No se ofrecen ni se prueban en Vestir. Los premios del vínculo no cambian.
3. **«Adelantar todo el camino»** (1.0.65). Va en contra del vínculo: es lo que mantiene a la gente usando la app, y con un toque se terminaría en un día. Sacar el botón y la fila de la tabla de planes.
4. **Amigos salvajes:** sigue como sorpresa, sin cambios.

## Textos

- Tabla de planes, cierre de la prueba, web (`web/datos.js`) y cualquier texto que hable de «packs aparte», «pelajes», «Comprar · precio» o adelantar el camino: sacarlos.
- En Play Console **no hace falta cargar** los productos `coats_*` ni los accesorios para el lanzamiento (X13 y X18 quedan para después).

## Para Ariel (todavía sin decidir)

- **Navidad:** Cowork propone que el **gorro navideño sea un regalo gratis** para todos del 1/12 al 6/1, y que quien lo use se lo quede. No hay que cobrar nada ni cargar ningún producto, y le da a la app una novedad tierna justo en el lanzamiento. No lo hagas hasta que Ariel confirme.

---

Cuando esté, anotá al final de este archivo `Estado: hecho en 1.0.xx`. Si hay dudas, dejalas en `docs/mimupet-cowork/para-cowork/`.
