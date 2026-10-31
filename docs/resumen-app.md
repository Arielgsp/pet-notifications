# Pet Notifications — resumen de la app

## Qué es
App para Android donde una mascota animada vive siempre en pantalla, flotando encima de
cualquier app, y es la que te avisa de tus recordatorios y eventos del calendario. En lugar de
una notificación común, la mascota dice su sonido ("¡Guau!", "¡Miau!", "¡Cuac!") con una
onomatopeya estilo cómic que le sale de la boca, y después muestra el aviso en un globo de
texto de historieta.

## Cómo funciona
- 10 mascotas para elegir: perro, gato, pato, rana, vaca, chancho, pollito, león, mono y búho.
  Dibujos propios de cuerpo entero, estilo tierno (cabeza grande, ojos brillantes, cachetes rosados).
  Ver `lamina-mascotas.png` y la carpeta `mascotas/`.
- Avisa de recordatorios creados en la app (una vez, diario o semanal) y de eventos del
  calendario de Google/Gmail sincronizado en el teléfono (sin cuentas ni claves extra).
- Se configura qué pasa al tocar el globo (abrir, borrar, posponer 5–60 min o cerrar) y si el
  globo se oculta solo (3–60 s) o queda fijo.
- Interacción: arrastrarla para moverla; 1 toque, salta y suelta corazones; 2 toques, se da
  la vuelta despacito meneándose; mantener apretado, abre la app.
- Tiene vida propia: parpadea, respira y se duerme (se echa, cierra los ojos, salen "zzz")
  tras un rato sin avisos o en el horario de sueño del usuario. Un aviso o un toque la despiertan.
- Opcional: dice el sonido en voz alta.
- Cuida la batería: en reposo no está animándose todo el tiempo.

## Qué la diferencia
- Convierte algo aburrido (las notificaciones) en algo simpático y emocional: una mascota
  compañera que "te habla".
- Siempre visible sobre otras apps, algo que la mayoría de las apps de recordatorios no hacen.
- Personalidad propia: cada animal tiene su sonido, su voz y sus poses.

## Limitaciones
- Solo Android. iOS no permite que una app se dibuje encima de las demás, así que la
  mascota "siempre en pantalla" no es posible en iPhone.
- Requiere el permiso "mostrar sobre otras apps", que algunos usuarios miran con desconfianza.

## Estado actual
- Prototipo funcional, probado en el emulador de Android y en un celular real.
- Se corrigió un error del globo de aviso (quedaba pegado en pantalla); falta confirmar el
  arreglo en el celular.
- Falta probar: el calendario de Gmail con una cuenta real, varios modelos de celular y la
  publicación en Google Play.
- Todavía no tiene nombre comercial definitivo, precio, modelo de negocios ni público objetivo definido.

## Recursos incluidos
- `lamina-mascotas.png`: las 10 mascotas juntas, con su nombre y su sonido.
- `mascotas/`: cada mascota de frente, en PNG con fondo transparente (800×960) y en SVG (vectorial, escala sin perder calidad).
- `guia-de-estilo.md`: colores, tipografías y reglas de dibujo.
- `README-tecnico.md`: cómo funciona la app por dentro.
