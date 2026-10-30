# Pet Notifications — pendientes

Lista viva de todo lo que quedó abierto desde que empezamos con el plan de Cowork
(`pet-notifications-mejoras-para-claude-code.md`, 26/09/2026). Se actualiza en cada tarea.
Lo que hay que probar en el celular está en [PRUEBAS.md](PRUEBAS.md); acá van decisiones,
tareas que faltan y limitaciones conocidas.

Última actualización: 30/10/2026, versión 1.0.31: opción de tema (Como el celular / Claro / Oscuro) en Ajustes (D22). En la 1.0.30: pestañas que se deslizan, probar mascotas Pro sin salir de Mascota, fichas Pro con etiqueta roja, una sola mascota gratis elegida al instalar y botón claro para cambiarla (D20, D21). Informe de estética pendiente: [informe-estetica.md](informe-estetica.md).

## 0. Cambios pedidos por Cowork (respuesta al informe 1.0.3, 28/09/2026)

En orden de trabajo, de a uno:

| # | Cambio | Qué hay hoy | Estado |
|---|---|---|---|
| C1 | **Modo fantasma gratis** (ajustes rápidos, notificación fija y Ajustes); tamaño y transparencia siguen Pro; actualizar la tabla Gratis/Pro | Atado a Pro en `Prefs.ghostMode`, `GhostTileService`, la notificación y Ajustes | Hecho (1.0.4) |
| C2 | **Problema I**: al borrar uno que se repite, preguntar "¿Saltear solo el de hoy o borrar todos?"; "Deshacer" en los de una vez | Borra la serie sin preguntar | Hecho (1.0.4) |
| C3 | **Prueba de 14 días en producción y 7 en la prueba cerrada**, según la compilación; **aviso el día anterior**, una vez, dicho por la mascota, aclarando qué se conserva gratis | Constante `TRIAL_DAYS = 7`; sin aviso | Hecho (1.0.4) |
| C4 | **Mascotas gratis: un cambio gratis** después de la primera elección; los siguientes, con Pro | Se eligen una vez y quedan fijas | Hecho (1.0.4) |
| C5 | **Restaurar la copia (5.a)**: en la primera apertura después de una restauración, reprogramar recordatorios, pospuestos y calendario; revisar fecha de prueba y Pro | No se detecta la restauración | Hecho (1.0.4), probado con una restauración real |
| C6 | **Avisos atrasados**: calendario → uno por uno solo los que no terminaron; los terminados en un resumen ("Mientras el celular estuvo apagado pasaron N eventos") que al tocarlo muestra la lista. Recordatorios propios → siempre; si hay más de 2 atrasados, una notificación resumen | Se avisan uno por uno los de las últimas 12 h | Hecho (1.0.4) |
| C7 | **Texto de privacidad** en la app (configuración guiada) y en README: "La app no tiene acceso a internet: tus datos nunca llegan a nosotros. Si tenés activada la copia de seguridad de Android, tus recordatorios se guardan en tu propia cuenta de Google." | La configuración guiada dice "Todo lo tuyo queda en tu celular" | Hecho (app y README; falta publicar la política) |
| C8 | **Emuladores Android 15 (API 35) y 14 (API 34)** y probar ahí antes de la prueba cerrada | Solo API 36 y 37 | Hecho: Android 14 y 15 probados (mascota, aviso, reinicio; sin alarmas exactas el aviso se atrasó 1 min 48 s) |
| C9 | **Versión de publicación (L)**: release firmada con R8 y volver a probar compras, base de datos y avisos | APK de depuración, sin R8 | R8 activado y probado (2,1 MB). **Falta: crear la llave de subida** (decisión tuya) |

Decisiones cerradas por Cowork: precio US$4,99 de lanzamiento y luego US$5,99, Latam ~50 % (D4, lo carga Ariel); J se cierra **manteniendo** `allowBackup`; plan B de la librería de compras aprobado; G queda para v1.1; orden de v1.1: frases variadas, clips (video/GIF generado en la app, sin grabar pantalla), modo discreto, rachas, disfraces, portugués, widget.

**No publicar** hasta tener el mail de comentarios (X6) y el nombre definitivo.

## 0.b Arreglo en curso (30/09): la mascota desaparece en Xiaomi al llegar al 100 % de batería

Versión 1.0.5 compilada: vigilante cada 15 minutos que vuelve a mostrar la mascota si el sistema la cerró, y registro de "cierres inesperados" en Diagnóstico (cuándo y cómo volvió). **Sin probar en el emulador**: la PC se quedó sin memoria libre (navegadores abiertos) y el emulador no llegó a abrir la app. Probar en el Xiaomi real.

## 1. Decisiones que esperan una respuesta tuya

| # | Tema | Contexto |
|---|---|---|
| D7 | **Recordatorios de Google Tasks / Gemini** (28/09) | Gemini guarda los recordatorios en Google Tasks, que no está en el calendario del teléfono: la app no los ve. Leerlos exige internet + cuenta de Google (rompe "sin internet") o acceso a las notificaciones de otras apps (permiso delicado, era la tarea P2 19). Mientras tanto: pedirle a Gemini un **evento de calendario**, o crear el recordatorio en la app. Consultar con Cowork. |
| D8 | **Llave de subida a Google Play** (28/09) | Opción 1: la crea Claude Code en esta PC y vos guardás una copia; opción 2: la creás vos en Android Studio. |
| D9 | **Tirones rápidos sobre la mascota** (30/09) | Elegido: tirón rápido. Izquierda = panel flotante de próximos avisos; abajo = recordatorio nuevo (hecho en 1.0.6, sin probar en emulador por falta de memoria de la PC). Arriba = esconder 1 hora; derecha = avisos en silencio hasta otro tirón (1.0.10). |
| D10 | **Rediseño de la pantalla de inicio** (30/09) | **Primera etapa hecha en 1.0.15 (05/10)**: pestañas Avisos / Mascota / Ajustes según Design, con nuestras mascotas. Faltan las próximas etapas que manden Cowork/Design y probar en el celular (PRUEBAS 30). |
| D11 | **Acciones al azar de la mascota** (30/09) | **Hecho en 1.0.19 (14/10)**: vida propia (mirar, darse vuelta, estirarse, bostezar, sentarse, saltar, sacudirse, cola, perseguirse la cola, pasitos y vuelve). Opción “Qué tan inquieta es” (Tranquila / Normal / Inquieta). Probar en el celular (PRUEBAS 34), incluida la batería. Ideas para más adelante: reaccionar a la carga del celular, pasear por el borde. |
| D12 | **Nuevos estilos de aviso de Design** (02/10) | En `pet-notifications-design/`: Anuncio, Asomado (por defecto), Cápsula, Plaquita, Cartita, Nube suave y Llamados, con tokens claro/oscuro y fuente Nunito. Falta que el usuario diga cuál aplicar y en qué contexto. Implican: niveles de aviso (suave/normal/importante), nombre propio de la mascota, poses nuevas del sprite (Asomado, Cartita, Meneo), textos nuevos ("Listo"/"Más tarde", onomatopeya en minúsculas). |
| D13 | **Tutorial de la mascota** (06/10) | Hecho en 1.0.16: 7 tarjetas animadas + práctica “Probalo vos” sobre la mascota real (los tirones no hacen su acción mientras se practica). Se muestra una vez y queda en Mascota, Ayuda y el consejo del día. Falta probar en el celular (PRUEBAS 31). Si Design quiere dibujarlo, se reemplazan las animaciones. |
| D14 | **Aviso en la pantalla de bloqueo** (08/10) | Corregido en 1.0.17: con la pantalla prendida pero bloqueada el aviso iba sin sonido y no se veía. No se pudo reproducir en el emulador (no mantiene el bloqueo): probar en el Xiaomi (PRUEBAS 32) y revisar si MIUI trae apagado “Pantalla de bloqueo” para la app. |
| D15 | **Vínculo y Sube de nivel** (09/10) | **Etapas 1 y 2 hechas en 1.0.18 (09/10)**: corazones, niveles, “Somos amigos”, “Nuestro camino”, “+1”, festejos (sobre la pantalla y en la app), bienvenida para quien ya tenía la app, y los trucos cola, patita (pose nueva en las 10 mascotas), buenos días y baile. **Etapa 3 (accesorios: pañuelo, camita, gorrito, corona y elegirlos en Mascota) espera los SVG de Design.** Temporada (gorro navideño, cumpleaños) para más adelante. Probar en el celular: PRUEBAS 33. |
| D16 | **Giro continuo (modelo 2,5D)** (14/10) | Perro en la app desde 1.0.20. **26/10: modelos de las 10 mascotas** en `tools/pet_art/model3d*.py` (giro, girar la cabeza, patas: saludar, amasar/aletear, ¡hurra!), vista previa en `docs/prototipos/mascotas-3d.html`. **27/10 (1.0.24): en la app** con un motor Kotlin igual al de Python (`tools/pet_art/gen_kotlin.py` genera `PetModelData.kt` y los casos de `PetModelTest`, que compara los dos motores): giro de 360°, mirar a los costados y darse vuelta con modelo en las 10 mascotas; girar solo la cabeza en las de cuatro patas (perro, gato, vaca, chancho, león, mono; las aves y la rana no, porque su cuerpo sentado fijo incluye la cabeza). Probado con tests; desde el 29/10 también en el emulador (SVM activado, WHPX). **27/10 (1.0.25):** el modelo tiene también la pose acostada (cuerpo de perfil, cabeza de frente, como los dibujos fijos) y dibuja todas las vistas (`PetModel.drawView`, `petPainter`): con un pelaje distinto del original y en el conejo, todo sale del modelo (sin saltito al girar). Falta: probar en el celular (PRUEBAS 39 y 40); usar el modelo también para saludar/amasar/¡hurra! y las orejas; con el pelaje original, quieta sigue con los dibujos fijos (al empezar y terminar el giro puede notarse un saltito, sobre todo en la cola, porque el modelo no es idéntico al dibujo). |
| D17 | **Pose sentada en reposo** (22/10) | 1.0.21 (primera versión, “araña”) → 1.0.23: pose “peluche” (patas estiradas hacia adelante). Gato con cara nueva (opción 3). Vida propia sin desplazamientos (en su lugar; el perro gira solo la cabeza). Globos y corazones al costado si no entran arriba. Probar: PRUEBAS 38. |
| D18 | **Pelajes (Pro)** (26/10) | Paletas por mascota en `tools/pet_art/model3d_pets.py` (PALETTES): 4–6 por mascota, incluidas manchas (dálmata, tricolor, manchado, lunares). Vista previa: `docs/prototipos/pelajes.html`. Aprobado por el usuario: solo Pro, el original gratis. **27/10 (1.0.25): en la app.** Fila “Pelaje” en la pestaña Mascota (dibujos sin nombre, así no hay que traducir los nombres; sin Pro, con candado y llevan a los planes), cada mascota recuerda el suyo (`Prefs.coat`; sin Pro siempre el original), fila “Pelajes” en la comparación de planes. Con otro pelaje, todos los dibujos (quieta, sentada, acostada, dormida, pantallas de la app) se hacen con el modelo. Falta: probar en el celular (PRUEBAS 40). |
| D19 | **Conejo** (26/10) | Conejo kawaii (orejas que suben y bajan) con 7 pelajes, incluido “Orejas caídas”, en `tools/pet_art/model3d_pets.py`. Sonido aprobado: “¡Hop hop!”. Orejas que se mueven también en el perro y el gato. **27/10 (1.0.25): en la app** como mascota número 11 (`PetType.RABBIT`, “Conejo” / “Bunny”, “¡Hop hop!” / “Hop hop!”; los textos de “las 10” pasan a “las 11”). No tiene dibujos fijos: todas sus vistas salen del modelo, con la pose acostada nueva. “Orejas caídas” es uno de sus pelajes (Pro). **29/10 (1.0.27), probado en el emulador:** elegirlo, pelaje negro, giro, dormido en la pantalla de inicio. Las orejas pasaban el borde de arriba del lienzo (y = −16) y la app las cortaba: el conejo se dibuja al 85 % desde el piso (`PetModel.fit`). Falta: probar en el celular (PRUEBAS 40); ver si queda chico al lado de las demás (alternativa: orejas un poco más bajas en el modelo); que mueva las orejas en la app (por ahora solo en la vista previa). |
| D20 | **Probar mascotas Pro sin Pro** (29/10) | Pedido del usuario. **1.0.29:** sin Pro, tocar una mascota Pro o un pelaje con candado hace que la mascota flotante sea esa durante 5 minutos (`Prefs.startTry`, `TRY_MS`; `PetOverlayService.applyShownPet` vuelve sola al terminar). Con la tarjeta “Probando: …” (cuenta regresiva, Probar un aviso, Volver a la mía, Ver planes). Probado en el emulador: león, pelaje 3 del perro, aviso de prueba, volver. La notificación fija sigue nombrando a la mascota propia. **30/10 (1.0.30), pedido del usuario:** ya no cambia de pantalla (para probar varias seguidas): la ficha probada queda marcada, la fila de pelajes muestra los de esa mascota y la tarjeta aparece en Mascota (debajo de los pelajes) y en Avisos. Falta: probar en el celular (PRUEBAS 41); decidir si 5 minutos está bien y si hay que limitar cuántas veces se puede probar. |
| D21 | **Plan gratis con 1 mascota y pantalla Mascota** (30/10) | Pedido del usuario, **1.0.30**: (1) las pestañas se pasan deslizando (`HorizontalPager`); (2) fichas de mascotas y pelajes Pro a todo color con etiqueta roja “PRO” en la esquina (`ProCorner`), en lugar de punteadas con candado (Design había pedido lo contrario: ver informe de estética); (3) el plan gratis tiene **1 mascota** (`Prefs.FREE_PET_COUNT = 1`): la que se elige al instalar, con un texto que lo explica; quien tenía 2 se queda con la que estaba usando; se puede cambiar una vez; (4) “Cambiar mi mascota gratis” es un botón con borde e ícono. Probado en el emulador. Falta: probar en el celular (PRUEBAS 42); revisar los textos de la tienda y de Cowork que todavía digan “2 mascotas”. |
| D22 | **Tema claro u oscuro** (30/10) | Pedido del usuario, **1.0.31**: en Ajustes, “Tema”: Como el celular (por defecto) / Claro / Oscuro (`Prefs.theme`; `MainActivity` lo aplica en el momento con `PetTheme(dark)` y ajusta los íconos de las barras del sistema). La mascota flotante no cambia. Probado en el emulador. Falta: probar en el celular (PRUEBAS 43). |

| # | Tema | Contexto |
|---|---|---|
Todas las decisiones anteriores (D1–D6) quedaron resueltas por Cowork el 28/09: ver sección 0 (C3, C4, C6, C8) y el precio en "Decisiones cerradas".

### Para publicar en Google Play (surgieron al armar el PDF de recomendaciones)

| # | Tema | Recomendación |
|---|---|---|
| P-1 | **Prueba cerrada obligatoria**: Google exige a cuentas personales nuevas al menos 12 testers durante 14 días antes de publicar (confirmar en la Play Console) | Usarla como programa de testers con la lista M1–M8 |
| P-2 | **Política de privacidad** publicada (enlace obligatorio en la ficha) | Texto corto, alojado gratis (por ejemplo, Google Sites); Claude Code lo redacta |
| P-3 | **Declaración del servicio en primer plano** ("uso especial") en la Play Console, con explicación y probablemente un video | Texto de Claude Code + video de 20–30 s de la mascota avisando |

El detalle con recomendaciones para cada tema está en `docs/Pet-Notifications-pendientes.pdf`.

## 2. Tareas del plan de Cowork que faltan

| # | Tarea | Estado |
|---|---|---|
| 10 | Idiomas: todos los textos en `strings.xml`, inglés, onomatopeyas por idioma, estructura para portugués | Hecha (falta probar en el celular: PRUEBAS §19) |
| 11 | Compra de Pro (Google Play Billing), prueba de 7 días, pantalla gratis/Pro, "Enviar comentarios" | Hecha (la compra real se prueba con la Play Console: PRUEBAS §20) |
| 12 | Pantalla de diagnóstico local (usa el registro de entregas de la tarea 1d) | Hecha (PRUEBAS §21) |
| P1 13–18 | Versión 1.1: frases variadas, modo discreto, rachas, disfraces, clips para redes, portugués y widget | Después del lanzamiento |
| P2 19–22 | Mensajes de otras apps, recordatorios compartidos, suscripción, iOS | Validar demanda antes |

## 3. Problemas de la revisión de Cowork (A–L) que siguen abiertos

| # | Problema | Estado |
|---|---|---|
| G | Mientras el globo está abierto, el espacio vacío encima del globo no deja pasar toques a la app de abajo | Para v1.1 (acordado con Cowork) |

Resueltos: A, B, C, D, E, F (tarea 1), H (tarea 3), I (C2), K (tarea 10), L (C9) y J (cerrado por Cowork: se mantiene la copia de Android).

## 4. Cosas que no se pudieron probar acá (dependen de un celular real)

| # | Qué | Dónde |
|---|---|---|
| T1 | Que el arreglo del **globo pegado** funcione en tu celular | PRUEBAS.md §1 |
| T2 | La **voz en Android 17** (el motor de voz del emulador no funciona) | PRUEBAS.md §10 |
| T3 | **Calendario de Gmail** con una cuenta real (el emulador no tiene cuenta de Google) | PRUEBAS.md §4 |
| T4 | Comportamiento por **marca** (Samsung, Motorola, Xiaomi) y nombres exactos de los menús de batería | PRUEBAS.md §12 y lista M1–M8 |
| T5 | Que la mascota vuelva sola tras **actualizar la app** en un celular real | PRUEBAS.md §9 |
| T8 | **Compra real** de Pro, restaurar, pago pendiente y reembolso (necesita la app en la Play Console) | PRUEBAS.md §20 |
| T7 | La **voz en inglés** (necesita la voz inglesa instalada en el celular) | PRUEBAS.md §19 |
| T6 | Toda la lista de PRUEBAS.md | PRUEBAS.md |

## 5. Limitaciones conocidas (decididas o sin solución por ahora)

| # | Limitación | Por qué |
|---|---|---|
| L1 | La mascota **no** se esconde con el **teclado abierto** de otra app | Android no lo avisa a otras apps sin permiso de Accesibilidad, que decidimos no pedir (tarea 7c) |
| L2 | En **Android 11 o anterior**, la mascota tarda hasta 1 minuto en esconderse al empezar una llamada | Esas versiones no avisan el cambio de modo de audio (tarea 7b) |
| L3 | En Android 15+, la mascota solo puede arrancar sola desde momentos que Android permite (abrir la app, reiniciar, actualizar, fin de "Pausar 1 hora") | Regla de Android; si se niega, los avisos siguen por notificación (tarea 2) |
| L4 | Tocar el **cuerpo de la notificación de un evento** de calendario abre el calendario y **no** corta "insistir" | Para cortar: Hecho, Posponer o deslizar la notificación (tarea 8) |
| L5 | Si la mascota está escondida (llamada o pantalla completa), los avisos suenan como notificación común | A propósito: que no se pierdan (tareas 7b y 7c) |
| L6 | En **Android 12 o anterior** no se puede elegir el idioma solo para la app: sigue el del celular | Esa opción de Android existe desde la versión 13 (tarea 10) |

## 6. Deuda técnica / prolijidad

| # | Qué |
|---|---|
| X1 | El título de la notificación fija usa el emoji de la mascota ("🐶 Tu perro está atento…"); los dibujos nuevos no se usan ahí |
| X2 | `tools/pet_art/gen.py` genera una carpeta `canvas/` al correr; hay que borrarla a mano después |
| X3 | Los datos de "Hecho" (tabla `completions`) todavía no se muestran en ningún lado (rachas: v1.1) |
| X4 | Los textos en inglés los escribí yo: conviene que los revise alguien con inglés nativo antes de publicar |
| X5 | Portugués (v1.1): copiar `values-es/strings.xml` a `values-pt/`, traducir y sumar `pt` en `res/xml/locales_config.xml` |
| X6 | **Mail de comentarios**: reemplazar `CAMBIAR@ejemplo.com` en `ui/Feedback.kt` (`FEEDBACK_EMAIL`) cuando crees la casilla |
| X7 | La librería de compras viene sin permisos de red (opción A). Si alguna vez da problemas, pasar a la opción B: borrar las 2 líneas `tools:node="remove"` del manifiesto y ajustar la tarea `verifyNoNetworkPermissions` |
| X8 | Formulario de **Seguridad de los datos** de Play: confirmar con la guía oficial de Google qué declarar por la librería de compras (sin permisos de red, en principio nada) |
| X9 | Play Console: crear el producto `pro_lifetime` (pago único), agregar **testers de licencias** y subir a prueba interna |
| X10 | Si el usuario atrasa el reloj más de un día, la prueba termina (a propósito, para que no se pueda estirar) |
| X11 | El resumen técnico de "Enviar comentarios" está siempre en español (es para quien lee los mails); en inglés el usuario lo ve igual en español |
| X12 | La librería de compras guarda sus reportes en una base propia dentro del celular (`com.google.android.datatransport.events`); como no tiene internet, nunca se mandan. La librería limita su tamaño sola |
