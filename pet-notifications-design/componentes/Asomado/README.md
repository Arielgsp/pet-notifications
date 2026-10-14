# Asomado

La mascota se asoma por arriba de una tarjeta redondeada y apoya las patitas en el borde; es el estilo por defecto para eventos y recordatorios con hora.

**Recibe:** nombre de la mascota, hora, título, cuerpo (hasta 2 líneas), 1–2 acciones, nivel (`normal` | `importante`).

**Por qué funciona:** se lee como una tarjeta común (legible sobre cualquier app) pero la mascota la "sostiene", así que se siente un gesto y no una ventana del sistema.

- Tarjeta en `surface-raised`, borde `line`, `radius-lg`, `shadow-soft`, relleno `space-4`.
- La cabeza queda detrás de la tarjeta (recortada en el borde) y las patitas van por delante, apoyadas sobre el borde.
- Importante: fondo `honey-soft`, borde `honey`, línea superior "Importante" + campanita en `honey-deep` y halo que respira detrás de la cabeza.
- Entrada: la tarjeta sube 14px y aparece (420 ms); la mascota sube desde atrás con un rebote mínimo (500 ms, 250 ms después); las patitas se apoyan al final.

**Sí:** ubicar la mascota del lado en que vive en pantalla. **No:** tapar el texto con las patitas; más de 2 acciones.

**Esfuerzo:** medio. Pide una pose nueva del sprite (cabeza y hombros + patitas por separado).
