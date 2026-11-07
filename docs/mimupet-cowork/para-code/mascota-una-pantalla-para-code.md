> **Hecho en 1.0.54–1.0.57.** Desde el 07/11/2026, la línea de nivel y la tira de 11 mascotas quedan reemplazadas por la tarjeta del camino (`2026-11-07-camino-de-la-mascota.md`) y la tira de 7 (sin siluetas). Los pelajes no están en el lanzamiento.

# Pantalla Mascota en una sola pantalla — para Claude Code

Pedido de Ariel: sacar las dos pestañas internas (Accesorios | Mascotas) y dejar todo en una pantalla más simple. El detalle visual está en el Design System, en PantallaMascota, Vestidor (la hoja Vestir) y PantallaAjustes.

## 1. Pestaña Mascota

Va de arriba a abajo:

1. **Escenario** con la mascota grande a la izquierda y, a la derecha, una columna con Cabeza, Cuello y Camita.
   - Cada lugar es una ficha con el dibujo de lo puesto, o un "+" si está vacío.
   - Camita dice "Al dormir".
   - Al tocar un lugar se abre la hoja Vestir en ese lugar.
2. **Una sola línea con el nombre, el chip de nivel y 10 corazones.** Al tocarla se abre Nuestro camino.
3. **Tarjeta "Lo próximo"**, por ejemplo: «Faltan 4 «Listo» para el nivel 3 · aprende a dar la patita».
4. **"Mascotas"** con la etiqueta PRO una sola vez en el título y *Ver Pro* a la derecha. Debajo va una tira horizontal con las 11, que se puede deslizar:
   - la tuya, primera y con borde cocoa;
   - las demás con borde punteado y candado.

Se quita la grilla de especies con su selector.

## 2. Tocar una mascota Pro en plan gratis

No cambia la mascota del usuario: la muestra en el escenario.

- **Escenario:** se ve la mascota elegida, con la etiqueta "Vista previa". La columna de lugares se esconde.
- **Tarjeta:** en lugar de la línea de nivel y de Lo próximo aparece una tarjeta con el nombre + PRO, "Probalo 5 minutos en tu pantalla", y los botones *Probar 5 min* y *Ver Pro*.
- **Tira:** la ficha tocada queda con borde punteado cocoa.
- **Para volver:** se toca la propia o se sale de la pantalla.

Con Pro o durante la prueba no hay candados, y tocar una mascota la cambia.

**Pelajes (Pro):** van en una fila de puntitos de color debajo del nombre, solo con Pro o en prueba. No está dibujada en la maqueta.

## 3. Hoja Vestir

Es una bottom sheet sobre la pantalla Mascota; la mascota sigue visible arriba.

- **Arriba:** chips Cabeza · Cuello · Camita, para cambiar de lugar sin cerrar la hoja.
- **Grilla de 3 columnas:** Sin nada, Puesto (borde cocoa), ganado, a la venta ("$ precio") y bloqueado ("Nivel N").
- **Accesorio a la venta:** al tocarlo se lo prueba. Abajo aparecen *Sacar* y *Comprar · $ precio*. Cerrar la hoja o tocar otro accesorio lo saca.
- **Accesorio bloqueado:** al tocarlo se explica cómo se gana: «Lo gano en el nivel 6, con 150 corazones. Faltan N «Listo»».
- **Cabeza:** gorro navideño, gorrito de lana, calabacita, corona de flores y casco de calabaza. Los dibujos están en `accesorios-11-especies.zip`.

## 4. Lo que se mueve a Ajustes

- **Tamaño, transparencia y modo fantasma:** pasan a una fila nueva, "Cómo se ve la mascota", debajo de "Mascota en pantalla". Tamaño y transparencia siguen siendo Pro: en plan gratis se ven grises, con la etiqueta PRO, y abren los planes.
- ***Probar un aviso*:** pasa a Ayuda.

## 5. Accesibilidad

- Cada lugar se anuncia como "Cabeza: vacío, tocar para vestir".
- Cada ficha de la tira se anuncia como "Gato, Pro, tocar para ver".
- Las fichas tienen un área táctil mínima de 48dp.

---

Estado: hecho entre 1.0.54 y 1.0.58, sin el Design System (no estaba disponible), solo con esta descripción. En 1.0.62 la tira muestra las 7 del lanzamiento y, al final, las 4 del pack como silueta con «Pronto». Falta probar en el Xiaomi (PRUEBAS 63 a 66). Anotado por Code el 05/11/2026.
