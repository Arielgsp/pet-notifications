# Vínculo y Sube de nivel — preguntas de Claude Code para Cowork y Design

9/10/2026 · Sobre `componentes/Vinculo` y `componentes/SubeDeNivel` del paquete de diseño.

Gracias por el diseño: se puede hacer tal cual, sin internet y sin conflicto con las reglas de Google Play. La app
ya guarda cada "Listo", así que el conteo de corazones sale de ahí. Antes de programar hay algunas cosas que
no están definidas y que cambian lo que hay que construir.

## Ya decidido por el usuario

- **3 toques desde el nivel 3:** se alternan la vuelta (lo que hace hoy) y dar la patita. No se pierde la vuelta.
- **Voz de los festejos: primera persona**, como ya habla la mascota en el tutorial, los consejos y la
  configuración. Ejemplo: "¡Aprendí a dar la patita!" en vez de "Luna aprendió a dar la patita".
  *Design: por favor ajustar los textos de ejemplo de SubeDeNivel.*
- **Usuarios que ya tenían la app:** arrancan con los corazones de los "Listo" que ya marcaron, respetando el
  tope de 10 por día.
- **Mascotas:** se usan las nuestras (perro, gato, pato, vaca, chancho, rana, pollito, león, mono, búho), no
  las de ejemplo del diseño.

## Preguntas

### Trucos (cuándo los hace y con qué gesto)

1. **"Te saluda con la cola" (nivel 1).** ¿Cuándo la mueve? Opciones: al desbloquear el celular, al tocarla una
   vez (hoy tira corazones) o de vez en cuando sola.
2. **"Saludo de buenos días" (nivel 4).** ¿Qué lo dispara? Propuesta: el primer desbloqueo del día después de
   que termina el horario de No molestar (o después de las 6:00 si no hay horario), una sola vez por día. ¿Qué
   dice o muestra? Un globo "¡Buen día!", ¿o también cuántos avisos hay hoy?
3. **"Bailar" (nivel 7).** Los gestos ya están todos ocupados (1, 2 y 3 toques, 4 tirones, arrastrar y mantener
   apretado). ¿Cómo se pide? Opciones: 4 toques; sola de vez en cuando (se relaciona con las "acciones al azar"
   que estaban pendientes); o al marcar "Listo" en un aviso.
4. **"Mostrame"** en la tarjeta de nivel hace el truco en el momento. Para los accesorios (pañuelo, camita,
   gorrito, corona), ¿qué hace? ¿Se pone el accesorio con un rebote?

### Accesorios (dibujos)

Nuestras mascotas no son imágenes sueltas: se dibujan por código (`tools/pet_art/gen.py`), en 7 posturas cada
una: de frente, parpadeando, contenta, de perfil, de espaldas, echada y dormida. Las aves (pato, pollito, búho)
no se echan: se esponjan en el lugar.

5. **¿Un dibujo por accesorio que sirva para todas**, y yo lo ubico en el cuello o la cabeza de cada especie?
   ¿O Design prefiere dibujar una versión por especie? Con la primera opción, alcanza con un SVG simple de cada
   accesorio, de frente y de perfil.
6. **¿En qué posturas se ve cada uno?** Por ejemplo: de espaldas, ¿se ve el moño del pañuelo o nada? Dormida,
   ¿se saca el gorrito?
7. **Camita (nivel 5):** ¿aparece solo cuando duerme? ¿Las aves también la usan?
8. **¿Se pueden usar juntos** el gorrito y la corona? Propuesta: un accesorio por lugar (uno en el cuello y uno
   en la cabeza).
9. **Accesorios de temporada (después del nivel 8):** ¿cuáles y en qué fechas? Ojo con el hemisferio: el
   usuario está en Argentina. ¿Los dejamos para más adelante?
10. **Colores:** ¿los accesorios usan solo los colores de los tokens (blush, sage, honey, cocoa), o tienen sus
    propios colores?

### Reglas del conteo

11. **El "día" del tope** (10 corazones por día), ¿empieza a la medianoche?
12. **Primer mimo:** ¿cuenta solo 1 toque, o también 2 y 3 toques?
13. **"Listo" desde dónde:** propongo que cuente igual desde el globo de la mascota, desde la notificación y
    desde la app. ¿De acuerdo?
14. **Si la celebración espera al desbloqueo** y en ese momento la app está abierta, ¿se muestra igual sobre la
    app, o en la pestaña Mascota?

## Plan de trabajo (Claude Code)

1. **Etapa 1, sin dibujos nuevos:** el conteo de corazones, la tarjeta "Somos amigos" en Mascota, "Nuestro
   camino", el "+1" al tocar Listo y la tarjeta de subir de nivel. Los premios se anuncian, y lo que todavía no
   está dibujado aparece como "Muy pronto" en "Nuestro camino".
2. **Etapa 2, trucos:** mover la cola, dar la patita, saludo de buenos días y bailar (según las respuestas
   1 a 4).
3. **Etapa 3, accesorios:** pañuelo, camita, gorrito y corona, y la opción de ponerlos y sacarlos en Mascota
   (según las respuestas 5 a 10).
