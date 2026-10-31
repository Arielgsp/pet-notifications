# Pet Notifications — informe para Cowork

**Para:** Cowork · **De:** Claude Code · **Fecha:** 31/10/2026 · **Versión de la app:** 1.0.33
**Informe anterior:** `informe-para-cowork.md` (28/09/2026, versión 1.0.3)

## Resumen

- **Los cambios C1–C9 que pidió Cowork están hechos.** Solo falta crear la llave de subida a Google Play (C9), que espera una decisión del dueño.
- **Desde la 1.0.3 la app creció mucho:**
  - las pantallas con el rediseño de Design;
  - la mascota con vida propia, vínculo y niveles, y un tutorial;
  - **11 mascotas en 2,5D que giran**, con **pelajes (Pro)**;
  - **plan gratis con 1 mascota** y prueba de mascotas Pro;
  - en la última versión, **una forma nueva de avisar**: globo al costado, tarjeta y cartas de colores.
- **Algunos cambios de producto obligan a revisar los textos de la tienda y la tabla Gratis/Pro** (sección 3).
- **Necesitamos que Cowork decida algunas cosas** (sección 4).
- Casi todo está probado en el emulador. **Falta probarlo en celulares reales** (sección 6).
- La app sigue sin permiso de internet, sin Accesibilidad y sin anuncios ni analytics.

---

## 1. Cambios pedidos por Cowork (C1–C9)

| # | Cambio | Estado |
|---|---|---|
| C1 | Modo fantasma gratis | Hecho (1.0.4) |
| C2 | Al borrar un recordatorio que se repite: "¿Saltear solo el de hoy o borrar todos?" + Deshacer | Hecho (1.0.4) |
| C3 | Prueba Pro de 14 días en producción y 7 en la prueba cerrada, con aviso el día anterior | Hecho (1.0.4) |
| C4 | Un cambio gratis de mascota después de la primera elección | Hecho (1.0.4). Desde la 1.0.30 el plan gratis tiene **1 mascota** (ver 3.1). |
| C5 | Reprogramar todo al restaurar una copia | Hecho y probado con una restauración real |
| C6 | Avisos atrasados: los vigentes uno por uno, los terminados en un resumen | Hecho (1.0.4) |
| C7 | Texto de privacidad en la app y en el README | Hecho. **Falta publicar la política.** |
| C8 | Probar en Android 14 y 15 | Hecho |
| C9 | Versión de publicación con R8 | Hecho (2,1 MB). **Falta crear la llave de subida.** |

## 2. Qué cambió desde la 1.0.3

| Versión | Qué se agregó o cambió |
|---|---|
| 1.0.5 | Si Xiaomi cierra la mascota (pasaba al llegar al 100 % de batería), un vigilante la vuelve a mostrar. Los cierres quedan registrados en Diagnóstico. |
| 1.0.6–1.0.10 | Tirones rápidos sobre la mascota. Izquierda: próximos avisos. Abajo: recordatorio rápido. Arriba: esconderla 1 hora. Derecha: avisos en silencio. |
| 1.0.15 | Pantallas rediseñadas según Design: pestañas Avisos / Mascota / Ajustes, colores y fuente Nunito. |
| 1.0.16 | Tutorial de la mascota: 7 tarjetas animadas y práctica sobre la mascota real. |
| 1.0.17 | Arreglo del aviso con la pantalla prendida pero bloqueada. |
| 1.0.18 | **Vínculo**: corazones, niveles, "Somos amigos", "+1", festejos y trucos que se aprenden (cola, patita, buenos días, baile). |
| 1.0.19 | **Vida propia**: la mascota mira, se estira, bosteza, se sacude, etc. Opción "Qué tan inquieta es". |
| 1.0.20–1.0.27 | **Modelo 2,5D**: las mascotas giran 360°, giran la cabeza y se acuestan. Pose sentada nueva, gato nuevo y **conejo** como mascota número 11 ("¡Hop hop!"). |
| 1.0.25 / 1.0.32 | **Pelajes (Pro)**: de 4 a 7 por mascota (manchas, dálmata, tricolor, lunares). En la 1.0.32 se cambiaron dos que se parecían demasiado al original. |
| 1.0.29 | **Probar mascotas Pro sin Pro**: tocar una mascota o un pelaje Pro lo pone en pantalla 5 minutos, con la opción "Probar un aviso". |
| 1.0.30 | Pestañas que se deslizan con el dedo. Fichas Pro a todo color con la etiqueta roja "PRO". **Plan gratis con 1 mascota** y un botón claro para cambiarla. |
| 1.0.31 | Tema claro, oscuro o como el celular. |
| 1.0.33 | **Avisos nuevos** (detalle en 3.3): globo Caramelo o Nubecita al costado, tarjeta al tocarlo, cartas roja / amarilla / celeste, avisos "sin interrumpir" y frases amigables. |

## 3. Cambios de producto que afectan a Cowork

### 3.1 Plan gratis: de 2 mascotas a 1

Lo pidió el dueño. La mascota gratis se elige al instalar y se puede cambiar una vez. Quien ya tenía 2 se queda con la que estaba usando.
**Hay que revisar los textos de la tienda, la tabla Gratis/Pro y cualquier material que diga "2 mascotas".**

### 3.2 Probar mascotas y pelajes Pro

Sin Pro, tocar una mascota o un pelaje Pro lo muestra en pantalla durante 5 minutos. Después vuelve solo a la mascota del usuario. Se puede probar sin límite de veces. Sirve como vidriera de Pro.

### 3.3 Cómo avisa ahora la mascota (1.0.33)

- **El globo:** la mascota dice su sonido en minúsculas ("guau", "miau") en un globo tipo sticker **al costado**. Hay dos estilos de Design, los dos gratis: **Caramelo** (por defecto) y **Nubecita**.
- **La tarjeta:** al tocar el globo se abre una tarjeta con "Luna te avisa · 16:00", el título, el texto y los botones **Listo** y **Aplazar**.
- **Carta roja:** si el usuario toca la mascota, el aviso se cierra y queda una carta roja a sus pies. No vuelve a llamar.
- **Avisos sin interrumpir** (nuevo): se activa para todos los avisos o para un recordatorio en particular. Esos avisos no aparecen ni suenan; queda una **carta amarilla**.
- **Frases amigables** (nuevo): 30 frases, como "No te olvides de tomar agua" o "Éxito en tu jornada", en una **carta celeste**.
  - El usuario elige la cantidad: Nunca, Pocas (1 por día, por defecto), Normal (3) o Muchas (6).
  - Llegan entre las 9 y las 21, nunca en el horario de No molestar.
- **Para no ser invasivos,** las cartas son sobres chiquitos: se ven como máximo 3 y, si hay más, un número.
- **No se hicieron** los niveles de aviso de Design (como "Importante" con halo) ni la mascota asomada sobre la tarjeta.

### 3.4 Pelajes y 11 mascotas

Los pelajes son solo Pro; el pelaje original es gratis. Con el conejo ya son 11 mascotas, así que los textos que digan "10 mascotas" quedaron viejos.

## 4. Decisiones que necesitamos de Cowork

| # | Pregunta | Propuesta de Claude Code |
|---|---|---|
| Q1 | ¿"Avisos sin interrumpir" y "Frases amigables" son gratis o Pro? | Gratis las dos, porque hacen que la app moleste menos, que es lo que promete. Más frases o frases propias podrían ser Pro en la v1.1. |
| Q2 | Prueba de mascotas Pro: ¿alcanza con 5 minutos? ¿Hay que limitar cuántas veces se puede probar? | 5 minutos, sin límite por ahora. Medirlo en la prueba cerrada. |
| Q3 | La etiqueta "PRO" de las fichas es roja (pedido del dueño), pero en el resto de la app es color miel, como pidió Design | Pedirle a Design un rojo de su paleta, o usar miel en todos lados. |
| Q4 | Niveles de aviso (Suave / Normal / Importante) | Dejarlos para la v1.1. Por ahora alcanza con el aviso normal y el "sin interrumpir". |
| Q5 | Recordatorios de Google Tasks / Gemini (D7) | Seguir sin leerlos, porque romperían la regla de no usar internet. Explicar en la ayuda que conviene crear un evento de calendario. |
| Q6 | Frases amigables | Que Cowork revise o amplíe las 30 actuales, en español y en inglés. |

## 5. Pendientes para publicar

| # | Qué | Quién |
|---|---|---|
| P-0 | **Llave de subida a Google Play** (C9 / D8): la crea Claude Code y el dueño guarda una copia, o la crea el dueño | Decide el dueño |
| P-1 | **Prueba cerrada**: 12 testers durante 14 días, que Google exige a las cuentas personales nuevas | Dueño + Cowork |
| P-2 | **Política de privacidad** publicada (el enlace es obligatorio en la ficha) | Claude Code la redacta, el dueño la publica |
| P-3 | **Declaración del servicio en primer plano** ("uso especial"), con un video de 20 a 30 segundos | Claude Code el texto, el dueño el video |
| P-4 | **Mail de comentarios** (X6) y **nombre definitivo** de la app. No publicar sin esto. | Dueño + Cowork |
| P-5 | Que alguien con inglés nativo revise los textos en inglés | Cowork |
| P-6 | Actualizar la ficha y las capturas: 1 mascota gratis, 11 mascotas, pelajes, avisos nuevos | Cowork |

## 6. Lo que falta probar en un celular real

Todo está en `docs/PRUEBAS.md` (secciones 1 a 44). Lo más importante:

- **Avisos nuevos** (sección 44): globo, tarjeta, cartas, avisos sin interrumpir y frases.
- **Giro 2,5D, pelajes y conejo** (secciones 39–40): que se vean bien y sin saltitos.
- **Plan gratis con 1 mascota, prueba de mascotas Pro, pestañas y tema** (secciones 41–43).
- **Xiaomi** (sección 32): que la mascota no desaparezca al cargar y que el aviso funcione con la pantalla bloqueada.
- **Compra real de Pro** (sección 20): necesita la app cargada en la Play Console.
- **Comportamiento por marca** (Samsung, Motorola, Xiaomi): lista M1–M8.

## 7. Limitaciones conocidas (sin cambios)

- **Teclado:** la mascota no se esconde cuando otra app tiene el teclado abierto. Para eso haría falta el permiso de Accesibilidad, que decidimos no pedir.
- **Llamadas en Android 11 o anterior:** la mascota tarda hasta 1 minuto en esconderse al empezar una llamada.
- **Android 15 y posteriores:** la mascota solo puede volver a aparecer sola en los momentos que Android permite. Si no puede, los avisos llegan por notificación.
- **Aviso abierto (G):** mientras el aviso está abierto, el espacio vacío de su ventana no deja tocar la app de abajo. Quedó acordado para la v1.1.
