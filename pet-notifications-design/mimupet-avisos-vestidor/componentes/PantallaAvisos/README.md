# PantallaAvisos

Pestaña principal de la app (la que abre al mantener apretada la mascota): la mascota grande en su escena, qué día es, qué viene en la semana y qué hay para hoy; cuando no hay nada, propone plantillas de un toque en lugar de dejar un hueco.

**Orden, de arriba hacia abajo:**
1. **Escena:** tarjeta de 200px de alto en `blush-soft` con un piso suave; la mascota grande (≈130px) abajo a la derecha, con lo que tenga puesto; arriba a la izquierda la fecha (20/24 800) y "3 cosas para hoy"; abajo a la izquierda el chip *Vestir* (percha). Tocar la mascota o *Vestir* abre el Vestidor (pestaña Mascota). Si está en pausa o en silencio, la pastilla de estado va debajo de "3 cosas para hoy".
   - **Al bajar por la lista,** la escena se achica de a poco hasta un encabezado fijo: mascota de 40px, fecha (16/20) y "3 cosas para hoy", con una línea `line` abajo. Al volver arriba, se agranda otra vez.
2. **Alerta** (`honey-soft`, borde `honey`): solo si falta un permiso o la prueba termina en ≤3 días. Una por vez.
3. **Semana:** 7 días desde hoy; hoy en `cocoa` con texto `surface`, el resto `surface-raised` con borde `line`; un punto `cocoa` en los días con recordatorios. Tocar un día muestra su lista.
4. **Hoy:**
   - **Con recordatorios:** lista en una tarjeta: hora (13 800), título (14 700), repetición con ícono.
   - **Vacía:** "Hoy no tenés nada pendiente" + "Empezá con uno de estos; se repiten solos" y 5 plantillas de un toque (Tomar agua, Pastilla, Regar las plantas, Sacar la basura, Cumpleaños) + "Otro". Cada plantilla abre el formulario ya completo (título, repetición sugerida), solo falta la hora.
5. **Ideas:** con 1 o 2 recordatorios que se repiten, las plantillas pasan a una fila chica de chips "Ideas:" debajo de la lista. Con 3 o más, desaparecen.
6. **Frase del día** (`sage-soft`, hojita): la frase amigable del día, sin interrumpir. No aparece si las frases están en "Nunca".
7. **Vínculo en una línea:** nivel, "Faltan N «Listo» para el nivel X" y barra `blush`; toca y va a Mascota.
8. **Ya avisados hoy (N)**, plegado.
9. **+ Recordatorio**, botón flotante `cocoa` con texto `surface`.

**Barra inferior:** Avisos · Mascota · Ajustes. Pestaña activa: pastilla `blush-soft`, texto `cocoa` 900.

**Texto:** la onomatopeya va en minúsculas y sin signos ("guau"), como en el globo; nunca "Guau!".
