# MimuPet — actualización de diseño: la mascota grande y el vestidor

Complementa el paquete de diseño anterior (mismos tokens y fuente). Cambian dos pantallas y se suma una:

- `componentes/PantallaAvisos`: reemplaza la versión anterior. Arriba, una **escena** con la mascota grande (≈130px) y lo que tenga puesto, la fecha, "3 cosas para hoy" y el chip *Vestir*. Al bajar por la lista se achica hasta un encabezado fijo; al volver arriba se agranda. Debajo: semana, Hoy (o plantillas si está vacío), ideas, frase del día, vínculo y Ya avisados.
- `componentes/Vestidor` (nuevo): la pestaña Mascota pasa a ser el vestidor. Mascota grande en el centro, lugares (cabeza, cuello, camita), selector *Accesorios* | *Mascotas*, grilla de accesorios (puestos, ganados por nivel, a la venta) y **probador**: se ve puesto solo en esa pantalla, con *Sacar* y *Comprar*.
- `componentes/PantallaMascota`: ahora es la sección *Mascotas* del vestidor (especies, pelajes, tamaño, transparencia, modo fantasma).

Cada carpeta tiene su `README.md` con las reglas y su `preview.html` (abrir con doble clic; es solo referencia visual, no se copia a la app).

## Cómo encararlo

1. Primero la escena de Avisos (sin compras): es visible y no depende de nada nuevo.
2. Después el Vestidor con los accesorios del vínculo (pañuelo, gorrito, corona, camita): ponerse y sacarse, sin venta.
3. Por último el probador y la compra por Google Play, cuando el dueño defina precios y packs. Hasta entonces, los ítems a la venta pueden no mostrarse.

## Decisiones pendientes del dueño

- Precio de cada accesorio o pack ("$ precio" en las maquetas).
- Si los ítems de temporada se compran solo en su fecha o quedan a la venta todo el año.

Tokens nuevos desde el paquete anterior: `pro` / `on-pro` (etiqueta PRO) y `card-pending` / `card-quiet` / `card-phrase` (cartas). Valores en `tokens.css` y en la sección 8 del informe.
