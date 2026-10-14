# PantallaAvisos

Pestaña principal de la app (la que abre al mantener apretada la mascota): estado chico arriba, una alerta solo si hace falta, lo próximo destacado y la lista agrupada.

**Orden, de arriba hacia abajo:**
1. **Encabezado** (`surface-raised`, `radius-lg`): mascota de 52px, "Luna está en pantalla" (15/20 800), "Te avisa con un guau" y, solo durante la prueba, "Prueba de Pro · te quedan N días". Chips *Pausar 1 h* y *Silenciar*.
   - En pausa: borde punteado `ink-muted`, mascota con ojos cerrados, "Vuelve a las 15:30" y chip *Mostrar ahora*. Igual para silencio ("Avisos en silencio" + *Activar sonido*).
2. **Alerta** (`honey-soft`, borde `honey`, ícono y palabra): una sola por vez y solo si hace falta. Prioridad: permiso faltante > prueba que termina en ≤3 días > mascota apagada. *Arreglar* lleva a Permisos y batería.
3. **Lo próximo:** tarjeta con `shadow-soft`, chip "en 2 h" (`blush-soft`), título y "Hoy · 16:30".
4. **Lista** agrupada en Hoy · Mañana · Esta semana · Más adelante. Fila: hora (13 800), título (14 700, una línea), meta con ícono: repetición o "Calendario".
5. **Ya avisados hoy (N)**, plegado. Base del historial y de las rachas (v1.1).
6. **+ Recordatorio**: botón flotante `cocoa` con texto `surface`, encima de la barra.

**Vacío:** mascota grande, "Todavía no tengo nada para recordarte", *Crear el primero* (principal) y *Probar un aviso*.

**Consejo del día** (`sage-soft`, se cierra con ×): un gesto por día, dicho por la mascota. Máximo uno; deja de salir cuando ya se mostraron todos.

**Barra inferior:** Avisos · Mascota · Ajustes. Pestaña activa: pastilla `blush-soft` y texto `cocoa` 900.
