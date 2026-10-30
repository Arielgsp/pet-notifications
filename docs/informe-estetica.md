# Pet Notifications — informe de cuestiones estéticas pendientes

**Fecha:** 30/10/2026 · **Versión de la app:** 1.0.30

**Fuentes:**
- la carpeta de Design `pet-notifications-design/` (02/10, con `README.md`, `tokens.json` y 11 componentes);
- `docs/guia-de-estilo.md`;
- `docs/PENDIENTES.md`;
- el código actual.

## Resumen

Sí: Design propuso cambios, y la mayoría **no están aplicados**.

- **Pantallas de la app:** se hicieron según Design en la 1.0.15: pestañas Avisos / Mascota / Ajustes, colores, fuente Nunito, tarjetas y chips.
- **Lo que flota sobre otras apps (globo del aviso y onomatopeya):** sigue con el estilo de cómic que Design pidió reemplazar. Esto es lo más visible que falta.
- **Pedidos que esperaban una respuesta tuya:** el estilo de aviso que se elija y los dibujos de accesorios. Esos dibujos nunca llegaron.

Claude Code no tiene un canal directo con Claude Design. Lo que haya que pedirle a Design tenés que llevarlo vos. Al final del informe hay una lista lista para copiar.

---

## 1. Propuestas de Design todavía sin aplicar

### 1.1 El globo del aviso y la onomatopeya (D12): lo más importante

| Hoy en la app | Lo que pidió Design |
|---|---|
| Onomatopeya "¡Guau!" en mayúscula, con signos, en letra cursiva negra con contorno, dentro de una **estrella de 14 puntas** que **tiembla** (gira ±6° cada 90 ms). | **Anuncio:** "guau" en minúsculas y sin signos, en un globo redondeado tipo *sticker* con pico hacia la boca. Las letras saltan una por una. Nunca un estallido dentado ni algo que vibre. |
| Globo blanco de cómic con borde oscuro grueso y pico. | **Asomado** (por defecto): una tarjeta común, y la mascota se asoma por arriba y apoya las patitas en el borde. Hay alternativas: Cápsula, Plaquita, Cartita y Nube suave. |
| Aviso de prueba: "¡Hola! Soy tu león". | Línea de arriba "Luna te avisa · 16:00". Sin emoji. Como mucho un signo de exclamación. |
| Un solo tipo de aviso. | **Tres niveles:** Suave (corazoncitos), Normal ("guau" + meneo) e Importante ("guau guau" + halo color miel que respira hasta que se atiende). |
| Las animaciones se ven siempre igual. | Si el teléfono tiene las animaciones desactivadas, mostrar el estado final sin movimiento. |

Lo que ya se acercó: el amarillo de la estrella se cambió por crema, y el globo tiene los botones "Listo" y "Posponer".

**Decisión pendiente tuya:** qué estilo usar por defecto y si el usuario puede elegirlo. Design recomienda *Asomado* por defecto, más los niveles de aviso. Se puede hacer de a uno; el que menos trabajo da es *Anuncio* + *Nube suave* o *Cápsula*.

**Novedad que ayuda:** el modelo 2,5D ya mueve la cabeza y la cola por separado. Con eso se puede hacer el **Meneo** que pedía Design, que antes necesitaba dibujos nuevos. Las poses de *Asomado* (cabeza y hombros con las patitas aparte), *Cartita* (sobre en la boca) y *Plaquita* (collar) también se podrían sacar del modelo en lugar de dibujarlas.

### 1.2 Accesorios del vínculo (D15, etapa 3)

Los niveles 2, 5, 6 y 8 regalan pañuelo, camita, gorrito y corona. Se esperaban los dibujos de Design, que nunca llegaron. Hoy la app muestra "Pañuelo para el cuello · Próximamente". Hay dos caminos:
- pedirle a Design los dibujos;
- hacerlos como piezas del modelo 2,5D. Así giran con la mascota y salen en las 11 mascotas y en todos los pelajes.

### 1.3 Pantalla Mascota: cambio decidido hoy, contra lo que pedía Design

Design había pedido mostrar las mascotas Pro "en fichas punteadas con candado; nunca 8 etiquetas PRO mezcladas con las propias". Hoy pediste lo contrario, y en la 1.0.30 se ven a todo color con la etiqueta roja "PRO". Queda anotado por si Design lo revisa.

Otros dos detalles de esa pantalla:
- Design usa el color miel para la etiqueta PRO (`honey-deep` sobre `honey-soft`); la de las fichas es roja, como pediste. El rojo no es un color de la paleta de Design. Si querés, se lo podemos pedir para que encaje.
- La propuesta hablaba de "Tus 2 mascotas". Ahora el plan gratis tiene 1.

### 1.4 Tutorial (D13)

Las 7 tarjetas del tutorial tienen animaciones hechas por nosotros. Quedó abierta la posibilidad de que Design las dibuje.

---

## 2. Contradicciones entre documentos

| Tema | `guia-de-estilo.md` (22/09) | Design (02/10) y la app |
|---|---|---|
| Fuentes | Fredoka para títulos, nombres y onomatopeyas; Nunito para textos | **Una sola familia: Nunito** (la app usa solo Nunito) |
| Fondo | Marfil `#FFF8EF` | Crema `#FBF6EE` (`surface`) |
| Acento | Bordó `#8E2F42` | `cocoa` `#8A5536` |
| Onomatopeya | Globo de cómic con estallido amarillo `#FFD54F` | Sticker sin estallido, en minúsculas |

**Recomendación:** que la guía de estilo quede solo para el **dibujo de las mascotas** (proporciones, ojos, colores de cada especie), que sigue vigente. Lo demás se toma de los tokens de Design.

---

## 3. Detalles visuales nuestros (de pruebas y pendientes)

| # | Qué | Dónde está anotado |
|---|---|---|
| 3.1 | Al empezar y al terminar el giro puede notarse un **saltito**, sobre todo en la cola. Pasa porque el dibujo fijo (pelaje original, quieta) y el modelo 2,5D no son idénticos. Arreglo posible: dibujar también la mascota quieta con el modelo. | D16 |
| 3.2 | Saludar, amasar, ¡hurra! y el **movimiento de orejas** existen en la vista previa, pero todavía no en la app. | D16, D19 |
| 3.3 | **Conejo** al 85 % para que no se corten las orejas: falta ver si queda chico al lado de las demás. Alternativa: orejas un poco más bajas. | D19, PRUEBAS 40.12 |
| 3.4 | Durante la prueba de una mascota Pro, la **notificación fija** sigue mostrando el nombre de la tuya. | D20 |
| 3.5 | Con "Simular plan gratis", la tarjeta de estado sigue diciendo "Prueba Pro · quedan N días". Solo se ve con las herramientas de dueño. | Este informe |
| 3.6 | Etiqueta PRO en dos colores: miel en títulos y deslizadores, roja en las fichas. Decidir si se unifica. | Este informe |
| 3.7 | La pantalla de inicio rediseñada (D10) quedó en la primera etapa. Las demás dependen de lo que manden Cowork y Design. | D10 |

---

## 4. Qué le pediría a Design (para copiar)

1. **Estilo de aviso:** confirmar *Asomado* como estilo por defecto y la variante de *Anuncio* (A · Sticker, B · Nubecita o C · Caramelo). Aclarar si el usuario puede elegir otro estilo.
2. **Accesorios** del vínculo (pañuelo, camita, gorrito, corona): en SVG, o una indicación de forma y color para armarlos en el modelo 2,5D.
3. **Etiqueta PRO roja** en las fichas de mascotas y pelajes (pedido del dueño): un tono rojo que combine con la paleta, o el visto bueno para usar el miel también ahí.
4. **Tutorial:** si quieren dibujar las 7 tarjetas.
5. **Pantalla Mascota con 1 mascota gratis** (antes eran 2): revisar si la distribución sigue funcionando.

## 5. Orden que recomiendo

1. *Anuncio* (onomatopeya sticker en minúsculas, sin estallido): poco código, y es lo más "cómic" que queda.
2. *Asomado* + niveles de aviso, con el meneo hecho con el modelo 2,5D.
3. Accesorios hechos con el modelo 2,5D, si Design no manda los dibujos.
4. Mascota quieta dibujada con el modelo, para que no haya saltito al girar.
