# Anuncio

La mascota dice su onomatopeya ("guau", "miau", "pío pío") en un globo con pico justo antes de que llegue el aviso, para que no te agarre de sorpresa y se vea quién habla.

**Recibe:** especie de la mascota (define la palabra), nivel del aviso y, en la Cartita, si sigue sin abrir.

**Cómo se ve (A · Sticker, recomendado):** globo redondeado en `surface-raised` con contorno de 3px en `fur` y una sombra corrida de 4px en `blush`, como un sticker; pico curvo y redondeado que termina en la boca de la mascota. Letras en `onomatopeya` y `cocoa`, cada una apenas girada (-6° / +5°) con una sombra rosa de 2px debajo; un corazoncito `blush` y dos destellos `honey` de puntas redondeadas que titilan. Importante: relleno `honey-soft`, contorno `honey`, letras `honey-deep`.

**Variantes:** B · Nubecita (bordes de nube, contorno `fur-deep`, sombra `sage`) y C · Caramelo (borde festoneado `blush`, relleno `blush-soft`, sombra `fur`). Elegir una sola para toda la app.

**Qué lo hace tierno y no agresivo:** curvas en vez de puntas, contorno de color cálido en vez de negro, minúsculas, letras que saltan en vez de vibrar, adornos chicos (corazón, destellos) en vez de un estallido.

**Secuencia:** el globo crece desde la punta del pico (la boca), con un rebote mínimo (≈250 ms) → las letras saltan una por una (50–60 ms entre letras) → queda visible ≈1 s → sube 10px y se desvanece → recién ahí entra el mensaje. Del "guau" al aviso pasan ≈1,5 s en total.

| Nivel | Anuncio |
| --- | --- |
| Suave | Sin anuncio: solo corazoncitos |
| Normal | Una vez: "guau" + meneo de cabeza |
| Importante | Dos veces: "guau guau" + halo que respira en `honey` |
| Cartita sin abrir | Repite cada 5 min, con el sobre moviéndose (ver Cartita) |

**Voz por especie** (ejemplos; se ajusta a las mascotas que tenga la app): perro "guau", gato "miau", pájaro "pío pío", pato "cuac". Si el animal no tiene voz conocida, usar un sonido de acción ("snif snif" para un conejo).

**Sí:** minúsculas, sin signos de exclamación, una palabra (dos en Importante). **No:** mayúsculas, "¡GUAU!", bordes dentados, amarillo saturado, sacudir la mascota.

**Sonido (opcional):** el ladrido o maullido real, suave y corto (menos de 600 ms), respetando el modo silencio.

**Accesibilidad:** la onomatopeya es decorativa: TalkBack lee "Luna te avisa: …", nunca "guau".

**Esfuerzo:** bajo. Es una vista con texto y dos animaciones; la palabra sale de una tabla por especie.
