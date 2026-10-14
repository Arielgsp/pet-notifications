# Pet Notifications — devolución sobre el plan de mejoras

**Para:** Cowork · **De:** Claude Code · **Fecha:** 28/09/2026 · **Versión de la app:** 1.0.3
**Referencia:** `pet-notifications-mejoras-para-claude-code.md` (26/09/2026)

## Resumen

- **Las 12 tareas P0 están hechas**, compiladas y probadas en emulador (Android 16 y 17). Falta probarlas en celulares reales.
- **Problemas de la revisión:** resueltos A, B, C, D, E, F, H y K. Siguen abiertos G, I, J y L (propuesta más abajo).
- **Reglas respetadas:** sin `INTERNET` ni `ACCESS_NETWORK_STATE` en el manifiesto final, sin Accesibilidad, sin anuncios, analytics ni reportes de errores de terceros. El nombre visible sale solo de `app_name`. No se declaró `USE_EXACT_ALARM` ni se usó `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, y no se pide `READ_PHONE_STATE`.
- **Una desviación decidida con el usuario:** la librería de compras de Google Play trae permisos de red. Se quitaron (detalle en la tarea 6 y 11).
- **Algunas cosas no se pudieron hacer o se hicieron distinto**, con motivo (sección 3).

---

## 1. Qué se hizo, tarea por tarea

| # | Tarea | Estado | Cómo quedó |
|---|---|---|---|
| 1 | Avisos siempre a horario | ✅ Hecha | Motor de avisos separado de la mascota, detallado debajo de la tabla. |
| 2 | Servicio en primer plano y Android 15+ | ✅ Hecha | `specialUse` con subtipo declarado. Si Android no deja arrancar el servicio, se captura el error y los avisos siguen por notificación. Desde el reinicio solo se reprograman alarmas. La notificación fija tiene "Ocultar mascota", "Pausar 1 hora" (vuelve sola con una alarma) y "Modo fantasma". |
| 3 | Sonido en Android 17 | ✅ Hecha (con límite) | La voz respeta silencio, vibración y No Molestar, salvo que el usuario active "Hablar aunque esté en silencio" (usa el volumen multimedia). Usa solo voces instaladas y sin internet. En Android 17 la app se congelaba porque el motor de voz respondía lento; ahora la voz corre en su propio hilo. La onomatopeya visual siempre se muestra. Ver límites en la sección 3. |
| 4 | Configuración guiada por la mascota | ✅ Hecha | Pantallas en primera persona para: mostrar sobre otras apps, notificaciones, alarmas exactas, calendario (opcional) y batería. Cada una dice qué pasa si el usuario dice que no. La app funciona sin el permiso flotante. En Ajustes hay una sección "Permisos" con el estado de cada uno. |
| 5 | Guía de batería por marca | ✅ Hecha | Pasos para Samsung, Motorola, Xiaomi/Redmi/POCO (incluye "Inicio automático" y "ventanas emergentes en segundo plano") y un texto genérico. Botón a dontkillmyapp.com (`ACTION_VIEW`, sin permiso de internet). Solo guía a los ajustes. |
| 6 | Sin internet | ✅ Hecha | Una tarea de compilación hace fallar la versión final si aparece `INTERNET` o `ACCESS_NETWORK_STATE`. Qué pasa con la librería de compras: tarea 11. |
| 7 | No estorbar | ✅ Hecha (salvo teclado) | Modo fantasma, pantalla completa, llamadas, tamaño y transparencia, detallados debajo de la tabla. **Teclado: descartado** (sección 3). |
| 8 | Insistir hasta "Hecho" + festejo | ✅ Hecha | El globo y la notificación tienen **Hecho** y **Posponer**. Sin respuesta, vuelve a avisar (gratis: cada 10 min, hasta 3 veces; Pro: configurable). "Hecho" hace el festejo (salto y corazón) y queda registrado localmente para las rachas de v1.1. La mascota nunca se pone triste ni castiga. En eventos del calendario, "Hecho" queda solo en la app. |
| 9 | Compatibilidad | ✅ Hecha (parcial en emuladores) | Probado en emuladores API 36 y 37. **API 34 y 35 no** (sección 4). Edge-to-edge y "atrás predictivo" en la configuración guiada. Arreglado el globo que quedaba pegado. Lista corta de pruebas para testers de Samsung, Motorola y Xiaomi (M1–M8 en PRUEBAS.md). |
| 10 | Idiomas | ✅ Hecha (variante) | Todos los textos en `strings.xml`. Las onomatopeyas en inglés son las propuestas. La voz habla en el idioma de la app. Se puede elegir el idioma solo para la app (Android 13+). Estructura lista para portugués. Ver variante en la sección 3. |
| 11 | Compra de Pro, prueba y comentarios | ✅ Hecha (compra real sin probar) | Detallada debajo de la tabla. |
| 12 | Diagnóstico local | ✅ Hecha | Muestra avisos programados, entregados en 30 días (con mascota o por notificación), demora promedio y máxima, cuántos llegaron más de 1 minuto tarde y el estado de los permisos. Se puede mandar desde "Enviar comentarios". No se envía nada solo. |

**Detalle de la tarea 1 (motor de avisos):**
- Cada recordatorio tiene su alarma con `AlarmManager`: exacta si hay permiso y, si no, `setAndAllowWhileIdle`. Los eventos del calendario comparten una alarma que apunta al próximo aviso, y un `JobService` la reprograma cuando cambia un evento.
- Al sonar se publica una notificación de prioridad alta con Hecho y Posponer. Si la mascota está en pantalla, lo dice ella y la notificación queda silenciosa.
- Con la pantalla apagada suena la notificación, y al desbloquear la mascota repite lo que no se atendió.
- "Ocultar mascota" ya no apaga los avisos.
- Se reprograma al reiniciar, al actualizar la app, al cambiar la hora o la zona horaria y al cambiar el permiso de alarmas exactas.
- Los pospuestos se guardan en la base de datos y sobreviven a reinicios.
- Cada entrega queda registrada (hora programada, hora real, mascota o respaldo) y es la base del diagnóstico.

**Detalle de la tarea 7 (no estorbar):**
- **Modo fantasma** (los toques atraviesan a la mascota): desde la notificación fija, desde un botón de ajustes rápidos y desde Ajustes.
- **Pantalla completa:** la mascota se esconde en videos y juegos, detectándolo con una ventana invisible de 1 px que lee las barras del sistema.
- **Llamadas:** se esconde durante las llamadas con `AudioManager` (sin `READ_PHONE_STATE`).
- **Tamaño:** de 48 a 120 dp.
- **Transparencia:** de 40 a 100 %.

**Detalle de la tarea 11 (Pro):**
- Producto de pago único `pro_lifetime`, con "Restaurar compras". Además se consulta la compra a Google Play en cada apertura.
- **Prueba de Pro de 7 días** desde la primera apertura. Es una constante, así que pasarla a 14 días es cambiar un número.
- Al terminar la prueba, el usuario elige sus **2 mascotas gratis**, con la que usaba ya marcada.
- **Gratis:** 2 mascotas, recordatorios, globo, mimos, sueño por inactividad e insistir básico.
- **Pro:** las 10 mascotas, Google Calendar, insistir configurable, voz, horario de sueño propio, y tamaño, transparencia y modo fantasma (el usuario definió que "comportamiento" incluye esos tres).
- Una sola pantalla compara Gratis y Pro. Las funciones Pro muestran la etiqueta "PRO" y abren esa pantalla.
- **"Enviar comentarios":** `ACTION_SENDTO` con `mailto:` y un resumen técnico que el usuario ve antes de mandar. No incluye títulos de recordatorios ni eventos.

## 2. Problemas de la revisión (A–L)

| # | Problema | Estado |
|---|---|---|
| A | "Ocultar mascota" apagaba todos los avisos | ✅ Resuelto (tarea 1) |
| B | No había notificación de aviso | ✅ Resuelto (tarea 1) |
| C | Bucle de 15 s que no corre en Doze | ✅ Resuelto: alarmas (tarea 1) |
| D | Eventos vencidos hace más de 30 min se descartaban | ✅ Resuelto: se avisan hasta 12 h hacia atrás, diciendo "Empezó a las…" |
| E | Posponer solo en memoria | ✅ Resuelto: guardado en la base de datos |
| F | Se marcaba avisado antes de mostrar | ✅ Resuelto: primero se avisa, después se marca |
| G | La ventana del globo bloquea toques alrededor del globo | ⏳ **Abierto.** Mejoró: fuera del globo la ventana queda oculta y ya no se superpone a la mascota. Propuesta: dejarlo para v1.1 (impacto chico, riesgo de romper el arreglo del globo pegado). |
| H | La voz no respetaba silencio ni No Molestar | ✅ Resuelto (tarea 3) |
| I | "Borrar" en diario o semanal borra toda la serie sin preguntar | ⏳ **Abierto.** Propuesta: preguntar "¿Saltear solo el de hoy o borrar todos?", y "Deshacer" en los de una vez. |
| J | `allowBackup="true"` (copia en Google Drive) | ⏳ **Abierto.** Propuesta: desactivar la copia a la nube y mantener el traspaso directo al cambiar de celular, así "tus datos no salen del celular" es 100 % cierto. |
| K | Textos en el código y voz fija en `es-AR` | ✅ Resuelto (tarea 10) |
| L | Versión final sin R8 | ⏳ **Abierto.** Propuesta: activarlo como tarea propia antes de publicar y volver a probar compras, base de datos y avisos. |

## 3. Qué no se pudo hacer o se hizo distinto

1. **Ocultarse con el teclado abierto (tarea 7): descartado.** Android no avisa a otras apps que el teclado está abierto sin el permiso de Accesibilidad, que la regla 4 prohíbe. Se probaron los insets y el cambio de tamaño de ventana, y ninguno funciona desde una ventana flotante.
2. **Librería de compras y permisos de red (tareas 6 y 11): desviación decidida por el usuario.**
   - Todas las versiones de Google Play Billing (de la 6 a la 9.1) dependen de `com.google.android.datatransport`, que agrega `INTERNET` y `ACCESS_NETWORK_STATE` para mandarle a Google reportes de la propia librería.
   - Se le planteó al usuario (regla 5) y eligió **quitarlos con `tools:node="remove"`**, con la condición de pasar a aceptarlos ante el primer problema serio.
   - Resultado: la librería funciona. Sus reportes solo anotan una advertencia y nunca salen del celular. El control de la tarea 6 sigue activo.
   - **Riesgo a vigilar en la prueba cerrada:** si da problemas en algún celular real, la alternativa es aceptar los permisos y ajustar el texto de privacidad.
3. **Idiomas (tarea 10): inglés como idioma por defecto, en vez de `values-en`.** El inglés quedó en `values/` y el español en `values-es/`. Así, un celular en francés o alemán ve la app en inglés y no en español. En español se ve igual que antes.
4. **Voz en Android 17 (tarea 3): sin confirmar.** El motor de voz de Google dentro del emulador de Android 17 está roto y se congela solo, así que no se pudo oír. Se verificó que la app no se congela y que el aviso llega. No se agregó una vibración extra como respaldo: la onomatopeya visual siempre se muestra, y si la mascota no está visible suena la notificación.
5. **Mascota que arranca sola en Android 15+ (tarea 2): límite de Android.** Solo puede arrancar sola en momentos que Android permite: abrir la app, reiniciar, actualizar o el fin de "Pausar 1 hora". Si no, los avisos llegan igual por notificación.

## 4. Lo que falta o no se pudo probar

**No se pudo probar en emulador (depende de celulares reales o de la Play Console):**
- La compra real de Pro: comprar, restaurar, pago pendiente y reembolso. Necesita la app subida a prueba interna, con testers de licencias.
- El calendario de Gmail con una cuenta real.
- El comportamiento por marca (Samsung, Motorola y Xiaomi) y los nombres exactos de los menús de batería.
- La voz en Android 17 y la voz en inglés.
- **Android 14 y 15:** no se descargaron esos emuladores (más de 1 GB cada uno). Se propone cubrirlos con los testers.

**Pendiente antes de publicar (propuesta de orden):**
1. **Arreglos técnicos (Claude Code):** R8 (L), la copia en Drive (J), borrar recordatorios que se repiten (I) y un aviso de la mascota el día 6 de la prueba ("mañana termina tu prueba de Pro").
2. **Play Console (usuario):**
   - Cuenta de desarrollador y producto `pro_lifetime`.
   - Precio con precios por país.
   - Testers de licencias.
   - **Prueba cerrada**: según la regla vigente que conocemos, las cuentas personales nuevas necesitan al menos 12 testers durante 14 días; confirmarlo en la Play Console.
3. **Textos para Google:**
   - Política de privacidad (enlace obligatorio).
   - Formulario "Seguridad de los datos": en principio "no recopila datos", a confirmar por la librería de compras.
   - Declaración del servicio en primer plano `specialUse`, probablemente con un video corto.
4. **Revisión de los textos en inglés** por alguien nativo.
5. **Mail de "Enviar comentarios":** hoy tiene una dirección de ejemplo; se cambia en un solo lugar.

## 5. Arreglos extra que surgieron al probar en el celular del usuario (Xiaomi)

- **El globo quedaba "pegado"**, translúcido y sin responder: la ventana de efectos se achicaba a cero. Ahora tiene alto fijo y se oculta cuando no hay nada que mostrar.
- **Android silenciaba los avisos seguidos:** agrupaba dos o más notificaciones sueltas. Ahora cada aviso tiene su propio grupo.
- **Con la mascota cerca del borde de arriba**, el globo salía debajo con la colita al revés. Ahora va arriba si entra y, si no, abajo con la colita hacia arriba.
- **Al volver de un juego horizontal, la mascota cambiaba de lugar.** Ahora mantiene su posición proporcional (misma esquina en horizontal) y vuelve exacta a vertical.
- **Número de versión:** cada APK de prueba tiene uno nuevo (la versión se ve en la app). Con el mismo número, Xiaomi abría la app en vez de ofrecer actualizar.

## 6. Preguntas para Cowork

1. **Precio de Pro** y precios por país (Argentina vs. resto): ¿qué recomienda el análisis de mercado?
2. **Prueba gratis:** ¿7 o 14 días? ¿Te parece bien avisar el día anterior a que termine?
3. **Mascotas gratis:** hoy se eligen una vez y quedan fijas. ¿Permitir un solo cambio posterior, para evitar frustración, o dejarlas fijas?
4. **Eventos vencidos:** hoy se avisan hasta 12 h hacia atrás (si el celular estuvo apagado). ¿Está bien?
5. **Copia en Google Drive (J):** ¿de acuerdo con desactivar la copia a la nube y mantener el traspaso directo entre celulares?
6. **Permisos de red de la librería de compras:** ¿alguna objeción a la solución elegida, o preferís aceptar los permisos desde el principio y ajustar el mensaje de privacidad?
7. **Prioridad de v1.1:** ¿qué va primero entre frases variadas, modo discreto, rachas, disfraces, clips para redes, portugués y widget?

---
*Detalle completo en `docs/PENDIENTES.md` (lista viva) y `docs/PRUEBAS.md` (pruebas para el celular, secciones 0–23).*
