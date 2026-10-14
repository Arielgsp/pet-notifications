Pet Notifications es una app Android donde una mascota flota sobre el celular y avisa recordatorios y eventos. Cada aviso tiene que sentirse como una visita de la mascota, no como una alarma del sistema: calma, ternura y lectura en un vistazo.

## Principios

- **La mascota trae el mensaje; no lo grita.** El texto del aviso va en un objeto que la mascota sostiene, sobre el que se apoya o que lleva colgado (tarjeta, cartita, plaquita, cápsula). El globo con pico queda solo para la onomatopeya; nunca un estallido.
- **Legible primero, tierno después.** Texto en `ink` sobre `surface-raised`, `paper` o un `*-soft`. Título en `notif-title`, cuerpo en `notif-body`, máximo dos líneas antes de tocar.
- **La mascota avisa que llega, con su propia voz.** Antes de cada aviso normal o importante dice su onomatopeya ("guau", "miau") en un globo tipo sticker con pico: es como golpear la puerta antes de entrar (ver Anuncio).
- **La atención se pide con movimiento lento, no con color estridente.** Un halo que respira o un meneo reemplazan al amarillo saturado y los bordes dentados.
- **Nada de lenguaje de cómic:** el globo de la onomatopeya tiene pico (se ve quién habla), pero curvas en vez de puntas, contorno de color cálido en vez de negro y letras en minúsculas que saltan; nunca un estallido dentado, mayúsculas o sacudidas.

## Estilo por defecto y alternativas

| Componente | Cuándo | Qué transmite |
| --- | --- | --- |
| **Asomado** (por defecto) | Eventos y recordatorios con hora | La mascota se asoma por arriba de la tarjeta y apoya las patitas: "te traje esto". |
| **Cápsula** | Quien quiere lo mínimo; avisos que no deben tapar la pantalla | Una línea que sale desde la mascota y se abre al tocar. |
| **Plaquita** | Rutinas cortas: tomar agua, estirar, medicación | Etiqueta colgada del collar con título y hora. |
| **Cartita** | Avisos afectivos y recordatorios normales: cumpleaños, notas para uno mismo | La mascota deja un sobre a sus pies y te llama ("guau") hasta que lo tocás; si no lo abrís, se abre sola. |
| **Nube suave** | Solo si se quiere cambiar lo mínimo respecto del globo actual | Nube de pensamiento sin pico. |

Usá un solo estilo por aviso. Si la app deja elegir estilo, Asomado es el valor inicial.

## Niveles de aviso

| Nivel | Cuándo | Anuncio previo | Mensaje | Llamado de atención |
| --- | --- | --- | --- | --- |
| Suave | Rutinas sin hora exacta | Ninguno | Plaquita o Cápsula en `surface-raised` / `paper` | Corazoncitos, un ciclo al llegar |
| Normal | Eventos y recordatorios con hora | "guau" una vez | Asomado en `surface-raised` (o Cartita) | Meneo junto con el "guau" |
| Importante | Vencimientos, turnos, lo que no se puede perder | "guau guau" | Asomado en `honey-soft` con borde `honey`; "Importante" + campanita en `honey-deep` | Halo que respira en `honey` hasta que se atiende; Ondas y Anuncio cada 10 minutos si sigue sin atender |

Un nivel nunca se comunica solo con color: siempre va la palabra y el ícono.

## Voz y textos

- La mascota habla en primera persona y en voseo: "Te aviso", "Te traje esto", "¿Lo hacemos ahora?".
- Título = qué pasa ("Turno con la dentista"). Cuerpo = cuándo y qué sigue ("Es hoy a las 16:30. Te vuelvo a avisar media hora antes.").
- La línea de arriba (`notif-meta`, `ink-muted`) lleva el nombre de la mascota y la hora: "Luna te avisa · 16:00".
- Sin emoji en el título: la mascota ya está en pantalla.
- Como mucho un signo de exclamación por aviso, y mejor ninguno. Nada de MAYÚSCULAS sostenidas.
- Acciones con verbos cortos y amables: "Listo", "Más tarde", "Ya lo hice", "En 1 hora", "Anotado". Nunca "Cerrar", "Descartar" ni "Ignorar".
- Onomatopeya según la especie, en minúsculas y sin signos: perro "guau", gato "miau", pájaro "pío pío", pato "cuac"; si el animal no tiene voz conocida, un sonido de acción ("snif snif"). Una palabra en Normal, dos en Importante. Estilo `onomatopeya`.

| En vez de | Mejor |
| --- | --- |
| ¡Hola! Soy tu perro 🐶 | Luna te avisa · 16:00 |
| ¡Guau! en un estallido amarillo | guau, en un globo sticker con pico, un momento antes del aviso |
| Tocá: abrir · Mantené: cerrar | Botones visibles "Listo" y "Más tarde" (la ayuda de gestos se muestra solo las 3 primeras veces) |

## Color

- Tarjetas flotantes en `surface-raised` con borde `line` de 1px y `shadow-soft`; así se separan de cualquier app de fondo, clara u oscura.
- `paper` solo para objetos de papel: Cartita y Plaquita.
- Acción principal: chip con fondo `blush-soft` y texto `cocoa`. Secundaria: borde `line`, texto `cocoa`.
- `honey` / `honey-soft` / `honey-deep` son exclusivos del nivel Importante.
- `blush`, `sage` y `honey` como marcas son decorativos (corazones, collar, halo): no alcanzan 3:1 sobre fondos claros, así que nunca son la única señal.
- La mascota usa `fur`, `fur-deep`, `cream` y `pet-ink`, iguales en ambos temas.
- `backdrop-mock` existe solo para las vistas previas.

## Tipografía

- Una sola familia: Nunito (redondeada, OFL), incluida como archivo variable en `fonts/`. En Android va en `res/font`.
- `onomatopeya` 20/24 900 · `notif-title` 16/22 800 · `notif-body` 14/20 600 · `notif-meta` 12/16 700 · `notif-action` 13/18 800. Nada por debajo de 12px.
- Pantallas de la app: `display`, `heading`, `body`.

## Forma, espacio y sombra

- Sin esquinas rectas ni picos: `radius-lg` para tarjetas, `radius-md` para la Cartita, `radius-sm` para la Plaquita, `radius-pill` para Cápsula y chips.
- Relleno de tarjeta `space-4`; de Cápsula y Plaquita `space-3`; margen al borde de pantalla `space-6`.
- Una sola sombra difusa (`shadow-soft`); objetos chicos sobre la mascota usan `shadow-lift`.

## Movimiento

- Entradas de 380 a 500 ms con `cubic-bezier(.22,1,.36,1)`: sale rápido y llega lento. La mascota puede tener un rebote mínimo con `cubic-bezier(.34,1.35,.64,1)`.
- Anuncio: el globo crece desde el pico en ≈250 ms, letras que saltan con 50–60 ms entre sí, ≈1 s visible y recién ahí el mensaje. Del "guau" al aviso, no más de 1,5 s.
- Salidas de 240 ms: se desvanece y baja 8px.
- Ciclos de 2,4 s o más. Nada que titile, sacuda o vibre.
- Los ciclos se detienen al tocar o a los 30 s, salvo el halo del nivel Importante, que sigue hasta que se atiende.
- Si el sistema tiene las animaciones desactivadas (escala de duración de animación en 0), mostrar el estado final sin movimiento.
- Sonido opcional, suave y corto (menos de 600 ms). Nunca un ladrido fuerte por defecto.

## La mascota

- El perro de las vistas previas es un dibujo de muestra: en la app va el sprite real de cada mascota.
- Poses que pide cada estilo: Asomado (cabeza y hombros + patitas delanteras por separado, para apoyarlas sobre el borde), Cartita (sentado con el sobre en la boca y con el sobre a sus pies), Plaquita (sentado con collar visible), Meneo (cabeza y cola animables por separado), Halo (ojos cerrados, opcional).

## Pantallas de la app

Al mantener apretada la mascota se abre la app con 3 pestañas abajo: **Avisos** (por defecto), **Mascota** y **Ajustes**. Ver PantallaAvisos, PantallaMascota y PantallaAjustes.

- Lo diario arriba (recordatorios, lo próximo, pausar y silenciar); lo de una sola vez (permisos, batería) en Ajustes, y en Avisos solo si falta algo.
- Pro se ve en la prueba (una línea), en las opciones bloqueadas (etiqueta `PRO` en `honey-deep` sobre `honey-soft`) y en "Tu plan"; nunca un banner fijo.
- Botón principal: `cocoa` con texto `surface`. Secundario: borde `line`, texto `cocoa`. Chip suave: `blush-soft`.
- Títulos de sección 12/16 800 en `ink-muted`; tarjetas `surface-raised` con `radius-lg` y `shadow-lift`.

## Vínculo

La mascota crece con cada "Listo": suma corazones, sube de nivel y aprende trucos o gana accesorios (ver Vinculo y SubeDeNivel). Nunca resta ni castiga: sin culpa, sin tristeza, sin rachas que se pierden. Se celebra lo que aprendió, no un puntaje.

## Accesibilidad

- Todo texto cumple 4.5:1 en ambos temas sobre los fondos que nombra su nota de uso.
- Anillo de foco: `focus-ring`, sólido, 2px, con 2px de separación.
- Área táctil mínima de 48dp por acción, aunque el chip se vea más chico.
- TalkBack lee el aviso completo: "Luna te avisa: Turno con la dentista. Es hoy a las 16:30." La onomatopeya es decorativa y no se lee.
