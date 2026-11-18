# Ícono rosa: hecho en 1.0.75 (18/11/2026)

Pedido: `para-code/2026-11-18-icono-rosa.md`. Las pruebas para el celular están en `docs/PRUEBAS.md`, sección 83.

- **Adaptive icon:** las tres capas (background, foreground y monochrome) quedaron como VectorDrawable, con los degradés y las transparencias reales. No hizo falta usar los PNG.
- **Ícono viejo (legacy):** no aplica. La app pide Android 8 o más, así que siempre usa el adaptive icon.
- **`ic_launcher_round.xml`:** no existe. El ícono redondo usa el mismo `ic_launcher`.
- **Play Store:** `play-icon-512.png` y `play-icon.svg` están en `tools/icon/svg/`. Lo sube Ariel en la Play Console.
- **Dentro de la app:** el ícono no se usa como imagen en ninguna pantalla.

## A revisar

En Ajustes de Android el ícono se ve completo. En la pantalla de inicio del emulador (Android 16) se ve más chico y con un aro rosa claro alrededor. Falta ver cómo queda en el Xiaomi.

## Ropa nueva

`2026-11-18-ropa-nueva-9-accesorios.md` queda sin empezar, para el pack posterior al lanzamiento.
