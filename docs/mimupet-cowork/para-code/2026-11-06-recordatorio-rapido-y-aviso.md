# Pedido de Cowork · 06/11/2026 · Recordatorio rápido y aviso con papelito

Lo aprobó Ariel. Diseño en el lienzo (se puede tocar): https://claude.ai/artifact/6AadjKE3sDNdirdK4QVHo7
- Recordatorio rápido: tablero **D · Cartita con horas y calendario**.
- Aviso: tablero **2 · Papelito en la patita**.

La idea de fondo: la mascota siempre te pasa **el mismo papel**, tanto para anotar como para avisar. Los colores salen de los tokens del Design System.

---

## 1. Recordatorio rápido (tirón hacia abajo)

Reemplaza el diálogo actual de Material (violeta, centrado y muy grande).

**Forma**
- Papel `paper` (#fff6e6 claro / #332b24 oscuro), borde `line` de 1 dp, `radius-md` (18 dp), `shadow-soft` e inclinado −1,5°.
- Mide 262 dp de ancho, con relleno de 14/16/12 dp.
- Va **al costado de la mascota**, que lo sostiene (la mascota se dibuja encima del borde del papel).

**Contenido, de arriba abajo**
1. Línea `notif-meta` en `ink-muted`, con un ícono de lápiz de 14 dp: «(nombre de la mascota) anota».
2. Campo de texto sin caja, con una línea punteada abajo (2 dp, `fur` #c98a55). Texto 16/800 en `ink`. Indicación: «¿Qué te recuerdo?».
3. Fichas de hora en una fila que **se parte en dos** cuando no entran:
   - 10 min · 30 min · **1 hora** (elegida por defecto) · Mañana 9:00 · botón de calendario (solo ícono; abre el selector de día y hora que ya existe).
   - Ficha: 32 dp de alto, forma de píldora, 13/800. Sin elegir: borde `line` y texto `ink-muted`. Elegida: fondo `blush-soft`, borde `blush` y texto `cocoa`.
   - **«Anotado»** va al final de la misma fila, alineado a la derecha: fondo `cocoa` y texto `surface-raised`. Con este ancho queda así: primera fila con 10 min, 30 min y 1 hora; segunda fila con Mañana 9:00, el calendario y Anotado.
4. «Anotado» queda desactivado hasta que haya texto. El Enter del teclado también anota.

**Ubicación**
- Al lado de la mascota, del lado donde haya lugar.
- **Si la mascota está en la mitad de abajo de la pantalla, el papel se abre arriba de ella**, para que no lo tape el teclado. (Decisión de Ariel.)
- Área táctil mínima de 48 dp, aunque la ficha se vea de 32.

---

## 2. Aviso: el globito pasa a ser un papelito

Reemplaza al globito con el mensaje. **El globo de la onomatopeya se queda tal como está.**

**Recorrido**
1. **Globo «guau»** con pico, igual que hoy, en el estilo elegido en Ajustes (Caramelo o Nubecita). Es breve y no hay que tocarlo. No cambia.
2. En lugar del globito con el mensaje aparece el **papelito** al costado de la mascota: arriba la hora y abajo el texto del aviso.
3. Al tocarlo, **se despliega** en la cartita: «(nombre) te avisa · 16:00», el título, y los botones **Listo** (`cocoa`) y **Más tarde** (borde `line`). Animación de 380 a 500 ms con `cubic-bezier(.22,1,.36,1)`, creciendo desde el papelito.
- Lo demás no cambia: tocar la mascota cierra el aviso y deja la carta pendiente, y siguen igual las repeticiones y los avisos sin interrumpir.

**Papelito**
- 176 dp de ancho, `paper`, esquinas de 14 dp, inclinado −2°, con sombra difusa.
- Primera línea: la hora («16:00»), 12/800 en `cocoa`. Sin onomatopeya: esa ya la dijo el globo. Segunda: el título, 15/800 en `ink`, una sola línea con «…» si no entra.
- La mascota lo sostiene con la patita: el borde derecho del papel queda debajo de la mascota.
- **Esquina doblada arriba a la izquierda**: la punta está recortada en diagonal (18 dp) y el doblez es un triángulo `#e9cfa6` con la punta redondeada. La sombra tiene que seguir esa forma: dibujar el papel con un `Path` recortado, no tapar la esquina con un cuadrado encima.
- Si la mascota está del lado izquierdo de la pantalla, todo se espeja: el papel va a la derecha y la esquina doblada arriba a la derecha.

**Cartita abierta**
- Mismo papel: 262 dp de ancho, esquinas de 18 dp, inclinada −1,5°, con esquina doblada de 22 dp en el mismo lugar.

**Ajustes**
- La opción del estilo del globo (Caramelo o Nubecita) **se queda**: es el estilo del globo de la onomatopeya. (Confirmado por Ariel.)

---

Cuando esté, anotá al final de este archivo `Estado: hecho en 1.0.xx`. Si hay dudas, dejalas en `docs/mimupet-cowork/para-cowork/`.

---

Estado: hecho en 1.0.64. Papelito y cartita en `overlay/NoteViews.kt` (`SlipNote`, `NoteCard`, `FoldedPaperShape`), recordatorio rápido en `overlay/QuickNote.kt`. Diferencias con el diseño, por ahora:
- La mascota flotante es una ventana aparte, debajo de la ventana del papel. Por eso no se dibuja encima del papel: el borde del papel llega a su cuerpo y lleva dibujada una patita encima.
- El papel queda siempre en claro, como el resto de la mascota flotante, que no tiene modo oscuro.
- Si al costado de la mascota no entran los 262 dp, la cartita del aviso se abre arriba de ella. El recordatorio rápido va arriba (o debajo, si arriba no hay lugar).
- La carta pendiente, al abrirla desde sus pies, sigue saliendo debajo de la mascota, sostenida con las patitas, pero ya con el papel nuevo.
Anotado por Code el 06/11/2026.
