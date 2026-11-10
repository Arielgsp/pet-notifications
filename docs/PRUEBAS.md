# Pet Notifications — pruebas pendientes en el celular

Lista de lo que hay que probar en un celular real por cada cambio. Se actualiza en cada mejora.
Marcá cada prueba con ✅ (anda), ❌ (falla: anotá qué pasó) o ➖ (no se pudo probar).

**Antes de empezar**
- Instalá la última versión (`app-debug.apk`) encima de la anterior. No hace falta desinstalar.
- En la app, la tarjeta de arriba tiene que mostrar ✔ en: Mostrar sobre otras apps, Notificaciones y
  Alarmas exactas. Si alguna tiene ⚠, tocá **Permitir**.
- Para no esperar tanto: en "Al tocar el globo" elegí **Posponer** y poné **5 min**.
- Anotá la marca y el modelo del celular: ______________________

---

## 0. Pendiente de antes de esta tanda

Cambios anteriores que todavía no se probaron en un celular.

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 0.1 | Mirá la mascota quieta unos 10 segundos. | Parpadea sola cada tanto. | |
| 0.2 | Un toque a la mascota. | Salta y pone ojitos felices (^ ^). | |
| 0.3 | Dos toques. | Suelta un corazón. | |
| 0.4 | Tres toques. | Se da la vuelta despacito: perfil, espalda, perfil y frente, meneándose. | |
| 0.5 | En Ajustes › Hora de dormir, activá "En mi horario de sueño" con un horario que incluya la hora actual. No la toques 5 minutos. | Se echa, al rato cierra los ojos y salen "zzz". | |
| 0.6 | Con la mascota dormida, tocala una vez. | Se despierta y se para (ese toque no hace nada más). | |
| 0.7 | Movés el deslizador de tamaño al mínimo y al máximo. | El trazo del dibujo y el globo se ven bien en los dos extremos. | |

---

## 1. Globo que quedaba pegado

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 1.1 | Tocá **Probar un aviso** y dejá que el globo se oculte solo (8 s). | El globo desaparece por completo; no queda nada pegado ni semitransparente. | |
| 1.2 | Tocá **Probar un aviso** y tocá el globo. | Desaparece por completo. | |
| 1.3 | Tocá **Probar un aviso** y mantené apretado el globo. | Se cierra y desaparece por completo. | |
| 1.4 | Mientras el globo está abierto, tocá justo encima de él (en el espacio vacío). | Efecto secundario conocido: ese toque no llega a la app de abajo. Solo mientras dura el globo. | |

## 2. Nombre de la app en un solo lugar

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 2.1 | Mirá el nombre debajo del ícono y el título grande dentro de la app. | Dicen lo mismo ("Pet Notifications"). | |

## 3. Tarea 1a — recordatorios con alarma

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 3.1 | Con la mascota en pantalla, creá un recordatorio para dentro de 3 minutos y esperá con el celular desbloqueado. | La mascota lo dice a horario. En la barra de notificaciones queda el aviso **sin sonido**. | |
| 3.2 | Apagá la mascota (interruptor de arriba). Creá un recordatorio para dentro de 3 minutos y bloqueá el celular. | A horario suena una notificación con el título y los botones **Hecho** y **Posponer**. | |
| 3.3 | En esa notificación, tocá **Hecho**. | La notificación desaparece. | |
| 3.4 | Repetí 3.2 y tocá **Posponer**. | La notificación desaparece y vuelve a sonar a los minutos configurados. | |
| 3.5 | Tocá el cuerpo de una notificación de recordatorio. | Se abre la app con ese recordatorio. | |
| 3.6 | Creá un recordatorio para dentro de 10 minutos y **reiniciá el celular**. | Suena igual a horario. | |
| 3.7 | Creá un recordatorio para dentro de 10 minutos y **cambiá la hora del celular** una hora para adelante y después volvé a la hora automática. | No se pierde: suena a la hora correcta. | |
| 3.8 | Creá un recordatorio y **borralo** antes de que suene. | No suena. | |
| 3.9 | Creá un recordatorio y **editalo** para 2 minutos más tarde. | Suena a la hora nueva, no a la vieja. | |

## 4. Tarea 1b — eventos del calendario con alarma

Necesita: en la app, **Google Calendar / Gmail** activado, con el permiso aceptado.

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 4.1 | En "Avisarme antes" elegí **Al empezar**. Cerrá la app. En Google Calendar creá un evento que empiece en 5 minutos. Bloqueá el celular. | A la hora del evento suena una notificación con el nombre del evento. | |
| 4.2 | Tocá esa notificación. | Se abre el evento en Google Calendar. | |
| 4.3 | Elegí **5 min** en "Avisarme antes" y creá un evento para dentro de 15 minutos. | Avisa 5 minutos antes: "Empieza a las … (en 5 min)". | |
| 4.4 | Creá un evento para dentro de 10 minutos y después **cambiale la hora** en Google Calendar a 5 minutos. | Avisa a la hora nueva. | |
| 4.5 | Creá un evento para dentro de 10 minutos y **borralo** en Google Calendar. | No avisa. | |
| 4.6 | Apagá el celular, esperá a que pase la hora de un evento (hasta 12 h) y prendelo. | Al rato avisa igual, con el texto "Empezó a las …". | |
| 4.7 | Un evento de **todo el día**. | No avisa (a propósito). | |
| 4.8 | Un calendario desmarcado en la lista de calendarios de la app. | Sus eventos no avisan. | |

## 5. Sonido de avisos seguidos

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 5.1 | Con la mascota apagada, creá dos recordatorios separados por 2 minutos. **No toques** la primera notificación. | **Los dos suenan.** Antes el segundo llegaba mudo. | |

## 6. Tarea 1c — posponer que no se pierde y repetir al desbloquear

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 6.1 | Con la mascota prendida, creá un recordatorio para dentro de 2 minutos y bloqueá el celular. Cuando suene la notificación, **no la toques**. Desbloqueá. | La mascota dice el aviso al desbloquear. | |
| 6.2 | Después de 6.1, bloqueá y desbloqueá otra vez. | No lo repite de nuevo. | |
| 6.3 | Con la mascota apagada, creá un recordatorio **"Todos los días"** para dentro de 2 minutos. Cuando suene, tocá **Posponer**. | Vuelve a sonar a los minutos configurados, y al día siguiente suena a su hora normal. | |
| 6.4 | Repetí 6.3, pero después de tocar **Posponer** **reiniciá el celular**. | El aviso pospuesto suena igual. | |
| 6.5 | Con un evento de calendario, tocá **Posponer** en su notificación. | Vuelve a avisar a los minutos configurados. | |
| 6.6 | Con la mascota en pantalla, cuando diga un aviso tocá el globo (acción **Posponer** configurada). | Vuelve a avisar a los minutos configurados, aunque cierres la app. | |
| 6.7 | Después de instalar esta versión, mirá tu lista de recordatorios. | Siguen todos (la base de datos se actualizó sin perder nada). | |

## 7. Tarea 1d — registro de entregas (para el diagnóstico)

La app anota cada aviso entregado: a qué hora tenía que sonar, a qué hora sonó y si lo dijo la
mascota o solo la notificación. **Todavía no se ve en ninguna pantalla**: lo va a mostrar la
pantalla de diagnóstico (tarea 12). Por ahora solo hay que confirmar que nada se rompió.

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 7.1 | Instalá esta versión encima de la anterior y abrí la app. | Abre normalmente y tus recordatorios siguen ahí (la base de datos se actualizó otra vez). | |
| 7.2 | Repetí rápido las pruebas 3.1 y 3.2. | Siguen funcionando igual que antes. | |

## 8. Opción "Al desbloquear" (repetir avisos sin atender)

Está en Ajustes › **Cuánto dura el globo** › **Al desbloquear**. Viene en **Los últimos 3**.

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 8.1 | Con la mascota prendida y la opción en **Solo el último**, creá dos recordatorios separados por 2 minutos y bloqueá el celular. Dejá que suenen los dos sin tocarlos. Desbloqueá. | La mascota dice **solo el segundo**. | |
| 8.2 | Igual que 8.1, con la opción en **Todos**. | Dice **los dos**, uno detrás del otro. | |
| 8.3 | Igual que 8.1, con la opción en **No repetir**. | No dice ninguno (las notificaciones siguen en la barra). | |

## 9. Tarea 2 — mascota en Android 15+ y "Pausar 1 hora"

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 9.1 | Bajá la barra de notificaciones y abrí la notificación fija de la mascota ("Tu perro está atento…"). | Tiene dos botones: **Pausar 1 hora** y **Ocultar mascota**. | |
| 9.2 | Tocá **Pausar 1 hora**. | La mascota desaparece y aparece el mensaje "La mascota vuelve en 1 hora…". | |
| 9.3 | Con la mascota pausada, creá un recordatorio para dentro de 2 minutos. | Suena como notificación (los avisos no se pausan). | |
| 9.4 | Abrí la app con la mascota pausada. | Dice "Tu perro está en pausa" y "Pausada hasta las HH:MM", con el botón **Mostrar ahora**. La mascota **no** vuelve sola por abrir la app. | |
| 9.5 | Tocá **Mostrar ahora**. | La mascota vuelve enseguida. | |
| 9.6 | Pausala otra vez y esperá la hora. | A la hora exacta vuelve sola. | |
| 9.7 | Pausala y **reiniciá el celular**. | Después del reinicio sigue pausada y vuelve sola a la hora indicada. | |
| 9.8 | Con la mascota visible, **reiniciá el celular**. | Al terminar de arrancar, la mascota aparece sola (puede tardar un minuto). | |
| 9.9 | Tocá **Ocultar mascota** en la notificación fija y abrí la app. | El interruptor de la mascota aparece apagado. | |

## 10. Tarea 3 — voz de la mascota

Necesita: en Ajustes › Cuánto dura el globo, **Decir la onomatopeya en voz alta** activado.

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 10.1 | Con el celular en sonido normal, tocá **Probar un aviso**. | La mascota dice su sonido en voz alta, casi al mismo tiempo que aparece el "¡Guau!". | |
| 10.2 | Bajá el **volumen de notificaciones** (no el multimedia) al mínimo y repetí 10.1. | La voz se escucha muy baja o nada. Subilo y vuelve a escucharse. | |
| 10.3 | Poné el celular en **silencio** y repetí 10.1. | La mascota muestra el aviso pero **no habla**. | |
| 10.4 | Poné el celular en **vibración** y repetí 10.1. | No habla. | |
| 10.5 | Activá **No Molestar** y repetí 10.1. | No habla. | |
| 10.6 | Activá **Hablar aunque esté en silencio** (aparece debajo de la voz) y repetí 10.3. | Ahora sí habla, con el volumen **multimedia**. | |
| 10.7 | Con el **modo avión** activado y sonido normal, repetí 10.1. | Habla igual (usa una voz que no necesita internet). | |
| 10.8 | Dejá el celular un buen rato sin usar la app (por ejemplo, una noche) y después tocá **Probar un aviso**. | Habla. Si la primera vez tarda unos segundos está bien; si no habla nunca, anotalo. | |
| 10.9 | Tocá **Probar un aviso** en medio de una llamada. | Ver sección 15: durante la llamada la mascota no aparece ni habla. | |
| 10.10 | Con la voz activada, tocá **Probar un aviso** varias veces seguidas (5 o 6) y usá la app mientras tanto. | La app **nunca** se congela ni aparece "no responde". Si la voz tarda o falla, la mascota muestra el aviso igual. | |

## 11. Tarea 4 — configuración guiada por la mascota y sección "Permisos"

Para verla desde cero: Ajustes del celular › Apps › Pet Notifications › Almacenamiento ›
**Borrar datos** (se borran tus recordatorios de prueba), o en la app tocá
**Ver la configuración guiada de nuevo** (sección Permisos).

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 11.1 | Abrí la app por primera vez (después de borrar datos). | Aparece la mascota: "¡Hola! Soy tu perro", con la lista de mascotas para elegir. | |
| 11.2 | Elegí otra mascota en esa pantalla. | El dibujo grande cambia a la mascota elegida. | |
| 11.3 | Tocá **Empezar**. | Paso "Para estar siempre en tu pantalla", con la explicación y qué pasa si decís que no. | |
| 11.4 | Tocá **Dar permiso**, activá el permiso en la pantalla de Android y volvé. | Pasa solo al paso siguiente. | |
| 11.5 | En "Para avisarte con el celular bloqueado", tocá **Dar permiso**. | Android pregunta por las notificaciones; elijas lo que elijas, pasa al siguiente paso. | |
| 11.6 | En "Para avisarte justo a horario", tocá **Dar permiso**, activalo y volvé. | Pasa solo al siguiente paso. | |
| 11.7 | En "¿Te aviso de tus eventos?", tocá **Conectar mi calendario** y aceptá. | Pasa al siguiente; después, en Ajustes, el calendario aparece activado. | |
| 11.8 | En "Para que no me cierren", tocá **Abrir ajustes de batería**. | Se abre la lista de optimización de batería de Android. Buscá la app, elegí "Sin restricciones" y volvé: pasa al último paso. | |
| 11.9 | En "¡Listo!", tocá **Ir a la app**. | Se abre la app normal y la mascota aparece en pantalla. | |
| 11.10 | Repetí desde 11.1 diciendo **Ahora no** a todo. | La app funciona igual: sin mascota en pantalla, pero un recordatorio de prueba suena como notificación (si diste el permiso de notificaciones). | |
| 11.11 | Los pasos de permisos que ya tenías dados. | No aparecen: se saltean solos. | |
| 11.12 | En la app, mirá la sección **Permisos**. | Lista cada permiso con ✔ o ⚠ y un botón **Permitir** en los que faltan. | |
| 11.13 | Si ya usabas la app antes de esta versión, actualizala. | **No** aparece la configuración guiada; entra directo a la app. | |

## 12. Tarea 5 — guía de batería por marca

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 12.1 | En la app, sección **Permisos**, mirá debajo de "Batería sin restricciones". | Dice "Pasos para tu Samsung" (o Motorola / Xiaomi / tu marca) con los pasos. Si tu marca no es ninguna de esas, dice "Pasos para tu celular". | |
| 12.2 | Tocá **Abrir ajustes de la app**. | Se abre la ficha de la app en los ajustes del celular (ahí está "Batería"). | |
| 12.3 | Seguí los pasos de tu marca y volvé a la app. | "Batería sin restricciones" pasa a ✔. | |
| 12.4 | Tocá **Ver guía en la web**. | Se abre el navegador en dontkillmyapp.com, en la página de tu marca. | |
| 12.5 | En la configuración guiada, paso "Para que no me cierren". | Aparecen los mismos pasos para tu marca. | |
| 12.6 | Anotá si los pasos de la guía coinciden con los menús de tu celular (los nombres cambian según el modelo). | Si algún menú no existe o se llama distinto, anotá cómo se llama en tu celular. | |

## 13. Tarea 6 — sin internet

No hay nada que probar en el celular: Android no muestra el permiso de internet en sus menús.
Quedó verificado al compilar: la versión final pide solo 7 permisos y ninguno es de red, y hay un
control automático que frena la compilación si alguna librería futura agrega uno.

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 13.1 | (Opcional) Activá el **modo avión** y usá la app un rato: crear recordatorios, probar avisos, cambiar de mascota. | Todo funciona igual. | |

## 14. Tarea 7a — modo fantasma

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 14.1 | Bajá la barra y abrí la notificación fija de la mascota. | Tiene los botones **Modo fantasma**, **Pausar 1 hora** y **Ocultar mascota**. | |
| 14.2 | Tocá **Modo fantasma**. | La mascota queda semitransparente. El botón pasa a decir **Salir de fantasma**. | |
| 14.3 | Poné la mascota fantasma encima de un ícono y tocá justo sobre ella. | Se abre la app del ícono de abajo (el toque la atraviesa). | |
| 14.4 | Con la mascota fantasma, tocá **Probar un aviso** en la app. | El aviso sale igual y el globo se puede tocar. | |
| 14.5 | En la cortina de ajustes rápidos (deslizá dos veces hacia abajo), tocá el lápiz de editar y agregá **Mascota fantasma**. | El botón aparece en los ajustes rápidos. | |
| 14.6 | Tocá **Mascota fantasma** en los ajustes rápidos. | Activa y desactiva el modo; el botón se ve encendido o apagado según corresponda. | |
| 14.7 | En la app, sección **Elegí tu mascota**, usá el interruptor **Modo fantasma**. | Hace lo mismo, y se mantiene sincronizado con la notificación y los ajustes rápidos. | |
| 14.8 | Activá el modo fantasma y reiniciá el celular. | La mascota vuelve en modo fantasma. | |

## 15. Tarea 7b — esconderse en llamadas

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 15.1 | Con la mascota en pantalla, pedile a alguien que te llame. | Mientras suena, la mascota desaparece (no tapa los botones de atender). | |
| 15.2 | Atendé y, durante la llamada, volvé a la pantalla de inicio. | La mascota sigue escondida. | |
| 15.3 | Cortá. | La mascota vuelve sola. | |
| 15.4 | Hacé una videollamada o llamada de **WhatsApp** o **Meet**. | Mismo comportamiento que 15.1 a 15.3. | |
| 15.5 | Creá un recordatorio que suene en medio de una llamada. | Suena como notificación (la mascota no aparece ni habla). Al cortar, la mascota lo repite (según la opción "Al desbloquear"). | |
| 15.6 | Llamada entrante que **no atendés** (dejá que se corte sola). | Al terminar de sonar, la mascota vuelve. | |

## 16. Tareas 7c y 7d — pantalla completa y opacidad

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 16.1 | Con la mascota en pantalla, poné un video de YouTube a **pantalla completa**. | La mascota desaparece mientras dura la pantalla completa. | |
| 16.2 | Salí de la pantalla completa. | La mascota vuelve sola. | |
| 16.3 | Abrí un juego que ocupe toda la pantalla, o la cámara. | La mascota se esconde (depende de si la app oculta la barra de arriba). | |
| 16.4 | En Ajustes › Elegí tu mascota, apagá **Esconderse en pantalla completa** y repetí 16.1. | La mascota se queda visible sobre el video. | |
| 16.5 | Con un video a pantalla completa, dejá que suene un recordatorio. | Suena como notificación; al salir de la pantalla completa, la mascota lo repite. | |
| 16.6 | Movés el deslizador **Opacidad** entre 40 % y 100 %. | La mascota se ve más o menos transparente. | |
| 16.7 | Con opacidad baja (40 %), tocá la mascota. | Sigue respondiendo a los toques (salta). | |
| 16.8 | Abrí el teclado en cualquier app (por ejemplo, WhatsApp). | **Conocido:** la mascota NO se esconde con el teclado. Android no deja detectarlo sin el permiso de Accesibilidad, que decidimos no pedir. | |

## 17. Tarea 8 — insistir hasta "Hecho" y festejo

Para no esperar tanto: en Ajustes › Al tocar el globo › **Insistir si no respondés**, elegí
**Cada 5 min** y **3 veces**.

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 17.1 | Con la mascota en pantalla, creá un recordatorio para dentro de 2 minutos. | El globo trae los botones **Hecho** y **Posponer**. | |
| 17.2 | Tocá **Hecho**. | La mascota salta, pone ojitos felices y suelta un corazón. No vuelve a avisar. | |
| 17.3 | Otro recordatorio: **no toques nada** y dejá que el globo se oculte solo. | A los 5 minutos la mascota vuelve a avisar. En la barra de notificaciones hay **una sola** notificación de ese aviso (no se amontonan). | |
| 17.4 | Seguí sin responder. | Avisa hasta 3 veces en total y después deja de insistir. | |
| 17.5 | En una repetición, tocá **Posponer** en el globo. | Deja de insistir y vuelve a avisar a los minutos de "Posponer". | |
| 17.6 | Con la mascota apagada: un recordatorio suena como notificación; **deslizala** para descartarla. | No vuelve a insistir. | |
| 17.7 | Con la mascota apagada: tocá **Hecho** en la notificación. | Desaparece y no insiste. | |
| 17.8 | Tocá el cuerpo de la notificación (abre la app en el recordatorio). | No vuelve a insistir. | |
| 17.9 | Poné **No insistir** en Ajustes y repetí 17.3. | No vuelve a avisar. | |
| 17.10 | Con un evento del calendario, tocá **Hecho**. | Desaparece; tu calendario de Google **no** cambia. | |
| 17.11 | Tocá **Probar un aviso**. | El globo de prueba **no** trae los botones (no hay nada que marcar). | |

## 18. Tarea 9 — compatibilidad

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 18.1 | En la configuración guiada, avanzá un paso y usá el gesto o el botón **atrás**. | Vuelve al paso anterior (no se cierra la app). En Android 14 o superior se ve una pequeña animación de "atrás" mientras deslizás. | |
| 18.2 | Recorré todas las pantallas de la app, en vertical. | Nada queda tapado por la barra de arriba (hora, batería) ni por la barra de gestos de abajo. | |

### Lista corta para testers (Samsung, Motorola, Xiaomi)

Para quien tenga uno de estos celulares. Son las pruebas donde las marcas suelen comportarse
distinto; anotá marca, modelo y versión de Android (Ajustes › Acerca del teléfono).

| # | Prueba | Qué tiene que pasar |
|---|---|---|
| M1 | Instalar, pasar la configuración guiada aceptando todo y seguir los **pasos de batería de tu marca** (Ajustes › Permisos). | La mascota queda en pantalla y "Batería sin restricciones" pasa a ✔. Anotá si algún menú de la guía se llama distinto. |
| M2 | Crear un recordatorio para dentro de **30 minutos**, bloquear el celular y dejarlo quieto. | Suena a horario (anotá cuántos minutos tarde, si tarda). |
| M3 | Dejar la app sin abrir **una noche entera** con la mascota prendida. | A la mañana la mascota sigue en pantalla (en algunas marcas el sistema la cierra: anotalo). |
| M4 | **Reiniciar** el celular. | La mascota vuelve sola y los recordatorios siguen programados. |
| M5 | Tocar **Probar un aviso** varias veces. | El globo aparece arriba de la mascota y desaparece del todo al ocultarse (sin quedar pegado). |
| M6 | Con la voz activada, **Probar un aviso**. | Se oye el sonido y la app no se congela. |
| M7 | (Xiaomi/Redmi/POCO) Revisar en Ajustes › Apps › Pet Notifications › Otros permisos que esté activado **"Mostrar ventanas emergentes mientras se ejecuta en segundo plano"**. | Con eso activado, la mascota aparece también sobre otras apps. |
| M8 | (Samsung) Activar el **modo de ahorro de energía** y crear un recordatorio para 10 minutos. | Suena igual (anotá si se atrasa). |

## 19. Tarea 10 — idiomas (español e inglés)

La app se ve en el idioma del celular: español si el celular está en español, inglés en
cualquier otro idioma. En Android 13 o superior también se puede elegir solo para esta app.

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 19.1 | Con el celular en español, recorré la app y tocá **Probar un aviso**. | Todo igual que antes, en español: "¡Guau!", "¡Hola! Soy tu perro", "Posponer", etc. | |
| 19.2 | Android 13+: Ajustes › Apps › Pet Notifications › **Idioma** › English. Volvé a la app. | Toda la pantalla pasa a inglés ("Try an alert", "Choose your pet", "Reminder"). | |
| 19.3 | En inglés, tocá **Try an alert**. | La mascota dice "Woof!" (o el sonido de tu mascota en inglés) y el globo dice "Hi! I am your dog". | |
| 19.4 | En inglés, con la voz activada, **Try an alert**. | La voz dice el sonido **en inglés** (con pronunciación inglesa). Si no se oye, puede faltar la voz inglesa en el celular: anotalo. | |
| 19.5 | En inglés, creá un recordatorio para dentro de 2 minutos. | La fecha se ve en inglés ("Sun, Sep 27"); al sonar, el globo y la notificación dicen "It is 5:30 PM". | |
| 19.6 | En inglés, mirá la notificación fija de la mascota. | Dice "🐶 Your dog is watching for your reminders", con botones "Ghost mode", "Pause 1 hour", "Hide pet". | |
| 19.7 | Volvé a poner **Español** (o "Predeterminado del sistema") en el idioma de la app. | Todo vuelve a español, también la voz. | |
| 19.8 | (Si tenés el calendario conectado) en inglés, esperá un aviso de evento. | Dice "Starts at … (in … min)" o "Starting now". | |

## 20. Tarea 11 — Pro, prueba gratis y comentarios

La compra real **no se puede probar con el APK suelto**: Google Play solo vende si la app está
subida a la Play Console (prueba interna). Estas pruebas cubren todo lo demás.

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 20.1 | Instalá la app y abrila. | Arriba dice **"Prueba de Pro: te quedan 7 días"** y todo está desbloqueado (las 10 mascotas, voz, calendario, etc.). | |
| 20.2 | Tocá **Ver planes**. | Aparece la tabla Gratis / Pro, el botón **Comprar Pro** y **Restaurar compras**. | |
| 20.3 | Tocá **Comprar Pro** (con el APK suelto). | Aparece un mensaje de que no se pudo conectar con Google Play o se abre Play con un error. La app **no se cierra** ni se congela. | |
| 20.4 | Tocá **Restaurar compras**. | Igual que arriba: un mensaje, sin cierres. | |
| 20.5 | Bajá hasta el final y tocá **Enviar comentarios**. | Se ve un resumen técnico (versión, Android, permisos, avisos). Al tocar **Abrir el correo** se abre tu app de mail con ese texto. Revisá que **no** aparezcan títulos de tus recordatorios. | |
| 20.6 | Usá la app normalmente varios días con la mascota prendida. | Nada se cierra ni se congela (la librería de compras funciona sin permiso de internet). | |
| 20.7 | Al pasar los 7 días, abrí la app. | Aparece **"Elegí tus 2 mascotas gratis"** con tu mascota ya marcada. Elegí otra y tocá **Listo**. | |
| 20.8 | Después del 20.7, mirá la pantalla. | Dice **"Plan gratis"**. Las otras 8 mascotas, tamaño, opacidad, modo fantasma, horario de sueño, voz, calendario e "insistir" tienen la etiqueta **PRO**; tocarlas abre los planes. | |
| 20.9 | Con plan gratis, probá un aviso y no respondas. | La mascota insiste cada 10 minutos, hasta 3 veces. No habla en voz alta. | |

### Cuando la app esté en la Play Console (prueba interna)

| # | Prueba | Qué tiene que pasar |
|---|---|---|
| 20.10 | Comprar Pro con una cuenta de **tester de licencias** (no cobra). | Se abre la ventana de pago de Google Play; al terminar, dice "Tenés Pro". |
| 20.11 | Desinstalar, reinstalar y tocar **Restaurar compras**. | Vuelve a decir "Tenés Pro". |
| 20.12 | Comprar con "pago pendiente" (opción de tester). | Dice que el pago está pendiente; Pro se activa cuando se confirma. |
| 20.13 | Reembolsar la compra desde la Play Console y volver a abrir la app. | Vuelve al plan gratis. |

## 21. Tarea 12 — diagnóstico

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 21.1 | Bajá hasta el final de la pantalla principal y tocá **Diagnóstico**. | Se ve: avisos programados, avisos entregados en los últimos 30 días (total, cuántos dijo la mascota, cuántos solo por notificación, demora promedio y máxima, cuántos llegaron tarde) y el estado de los permisos. | |
| 21.2 | Creá un recordatorio para dentro de 2 minutos y volvé a abrir **Diagnóstico**. | "Recordatorios pendientes" sube en 1. | |
| 21.3 | Cuando suene, volvé a abrir **Diagnóstico**. | "Total" sube en 1 y la demora es de pocos segundos. | |
| 21.4 | Dejá la app unos días con uso normal y mirá **Diagnóstico**. | Si "Más de 1 minuto tarde" o "Solo por notificación" son altos, anotalo: ayuda a encontrar problemas de batería de tu marca. | |
| 21.5 | En **Diagnóstico**, tocá **Mandar por mail**. | Se abre "Enviar comentarios" con los mismos números. | |

## 22. Globo con la mascota arriba de la pantalla (arreglo del 27/09)

Antes: con la mascota cerca del borde de arriba, el globo y los corazones aparecían debajo de ella, y la colita apuntaba hacia abajo, lejos de la mascota.

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 22.1 | Arrastrá la mascota arriba, como en tus capturas (a la altura del título de la app), y tocá **Probar un aviso**. | El "¡Guau!" y el globo salen **arriba** de la mascota y la colita apunta a su cabeza. | |
| 22.2 | Arrastrá la mascota **pegada al borde de arriba** (justo debajo de la hora y la batería) y tocá **Probar un aviso**. | Como arriba no entra, el globo sale **debajo** y la colita apunta **hacia arriba**, a la mascota. El globo no tapa a la mascota. | |
| 22.3 | En la posición del 22.2, tocá el globo. | Responde normal: se cierra o abre, según tu opción. | |
| 22.4 | En la posición del 22.2, tocá dos veces la mascota. | Los corazones se ven (salen hacia abajo) y no tapan a la mascota. | |
| 22.5 | Con la mascota en el medio de la pantalla, **Probar un aviso**. | Todo igual que siempre: arriba de la mascota. | |

## 23. La mascota al girar la pantalla (arreglos del 27/09, versiones 1.0.2 y 1.0.3)

Antes: al volver de un juego a pantalla completa, la mascota reaparecía más abajo. El juego gira la pantalla a horizontal, la app la acomodaba para que entrara y se quedaba con esa posición.

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 23.1 | Dejá la mascota arriba (como en tus capturas), abrí un juego que se ponga horizontal, jugá un poco y salí. | La mascota reaparece **exactamente en el mismo lugar**. | |
| 23.2 | Mirá un video de YouTube girando el celular y volvé. | Igual: vuelve a su lugar. | |
| 23.3 | Con el celular horizontal (en una app que gire), arrastrá la mascota a otro lugar y volvé a vertical. | Queda donde la dejaste por última vez, acomodada para que entre en la pantalla. | |
| 23.4 | Dejá la mascota en una esquina (por ejemplo, arriba a la derecha) y girá el celular en una app que gire (YouTube, Chrome o un juego). | En horizontal aparece en la **misma esquina**; al volver a vertical, exactamente donde estaba. | |

## 24. Cambios pedidos por Cowork (28/09, versión 1.0.4)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 24.1 | **Modo fantasma gratis.** Con el plan gratis (después de la prueba), andá a Ajustes › Modo fantasma. | Se puede prender y apagar (ya no tiene "PRO"). También desde la notificación fija y desde los ajustes rápidos. Tamaño y transparencia siguen con "PRO". | |
| 24.2 | **Borrar uno que se repite.** Creá un recordatorio diario y tocá el tacho. | Pregunta "¿Qué querés borrar?": **Saltear solo el próximo** (pasa al día siguiente y lo avisa abajo) o **Borrar todos**. | |
| 24.3 | **Deshacer.** Creá un recordatorio de una sola vez y tocá el tacho. | Abajo aparece "Recordatorio borrado · **Deshacer**" durante unos segundos; al tocar Deshacer, vuelve. | |
| 24.4 | En el globo de un recordatorio diario, con la opción "Al tocar el globo: Borrar", tocá el globo. | No borra la serie: se cierra y dice cómo borrarlo desde la app. | |
| 24.5 | **Prueba de Pro.** Instalá la versión de la prueba cerrada en un celular sin la app. | Arriba dice "Prueba de Pro: te quedan **7** días" (en la versión publicada van a ser 14). | |
| 24.6 | **Aviso del día anterior.** El día 6 de la prueba (a la hora en que abriste la app por primera vez, o a las 10 si era de noche). | La mascota dice "Mañana termina tu prueba de Pro" y aclara qué se conserva gratis. Llega una sola vez, sin botones de Hecho/Posponer. | |
| 24.7 | **Cambiar mascotas gratis.** Con el plan gratis, debajo de las mascotas tocá "Cambiar mis 2 mascotas gratis". | Se pueden elegir otras 2; al confirmar, el botón desaparece (es un solo cambio). | |
| 24.8 | **Celular apagado.** Creá 3 o más recordatorios para dentro de pocos minutos, apagá el celular hasta que pasen y prendelo. | Llega **un solo aviso**: "Tenías N recordatorios atrasados". Al tocarlo se ve la lista. | |
| 24.9 | Lo mismo con 1 o 2 recordatorios. | Llegan uno por uno ("Era para las…"). | |
| 24.10 | (Con calendario) Apagá el celular mientras pasa un evento corto y prendelo después de que termine. | Un aviso "Mientras el celular estuvo apagado pasó 1 evento"; los eventos que siguen en curso se avisan aparte. | |
| 24.11 | **Cambio de celular.** Con la copia de seguridad de Android activada, pasá la app a otro celular (o desinstalala y reinstalala después de una copia). | Vuelven tus recordatorios, se pide de nuevo la configuración guiada (permisos del celular nuevo) y los recordatorios **suenan** (las alarmas se reprograman al abrir la app). La prueba de Pro no se reinicia. | |
| 24.12 | En la configuración guiada, primera pantalla. | Dice "La app no tiene acceso a internet: tus datos nunca llegan a nosotros". | |

## 25. La mascota desaparece en Xiaomi al llegar al 100 % de batería (versión 1.0.5, sin probar en emulador)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 25.1 | Instalá la 1.0.5 y abrí la app una vez. Dejá el celular cargando hasta el 100 %. | Si la mascota desaparece, tiene que **volver sola en 15 minutos como máximo** (sin abrir la app). | |
| 25.2 | Después, andá a **Diagnóstico** (final de la pantalla principal). | En "Cierres inesperados de la mascota" aparece cuándo se cerró y cómo volvió: "volvió sola", "volvió al abrir la app" o "volvió al reiniciar". Mandame una captura. | |
| 25.3 | Si dice **"volvió al abrir la app"**: creá un recordatorio para 20 minutos después, cargá hasta el 100 % y no abras la app. | Anotá si el recordatorio **suena**. Si no suena, Xiaomi también está borrando las alarmas (es más grave: avisame). | |
| 25.4 | Revisá en Ajustes del celular › Apps › Pet Notifications: "Inicio automático" activado y "Ahorro de batería: Sin restricciones". | Con eso Xiaomi debería dejar de cerrarla. | |

## 26. Tirones rápidos sobre la mascota (versión 1.0.6, ajustados en 1.0.7)

Un "tirón" es mover el dedo **rápido y corto** sobre la mascota, como sacudiendo una miga (menos de un cuarto de segundo). Si movés el dedo despacio, la arrastrás como siempre.

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 26.1 | Tirón rápido **hacia la izquierda** sobre la mascota. | La mascota vuelve a su lugar y aparece un globo **"Próximos avisos"** con hasta 6 recordatorios y eventos de los próximos 7 días (con día y hora). | |
| 26.2 | Tocá el panel. | Se cierra. Si no lo tocás, se cierra solo a los 12 segundos. | |
| 26.3 | Sin recordatorios ni eventos próximos, tirón a la izquierda. | Dice "No tenés avisos en los próximos 7 días". | |
| 26.4 | Tirón rápido **hacia abajo**. | Se abre la app directamente en "Nuevo recordatorio". | |
| 26.5 | Tirón hacia arriba o hacia la derecha. | La mascota da un saltito y no hace nada (quedan libres para más adelante). | |
| 26.6 | Arrastrá la mascota despacio a otro lugar. | Se mueve como siempre y queda donde la dejaste. | |
| 26.7 | Con la mascota arriba de todo, tirón a la izquierda. | El panel sale debajo de la mascota, con la colita hacia arriba. | |
| 26.9 | (1.0.7) Dejá la mascota pegada al borde izquierdo y hacé un tirón corto hacia ese borde. | Igual abre el panel de próximos avisos. | |
| 26.10 | (1.0.8) Arrastrá la mascota despacio. | Sigue al dedo casi enseguida, sin saltos. | |
| 26.11 | (1.0.8) Arrastrá la mascota arrancando rápido. | Al principio se inclina apenas y enseguida alcanza al dedo con un deslizamiento suave (sin salto). | |
| 26.12 | (1.0.8) Hacé un tirón. | La mascota se inclina apenas hacia ese lado y vuelve suave a su lugar mientras hace la acción. | |

## 27. Tocar la mascota para cerrar y recordatorio rápido flotante (versión 1.0.9)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 27.1 | Tocá **Probar un aviso** y, mientras está el globo, tocá **la mascota**. | El globo se cierra (como mantenerlo apretado: deja de insistir). | |
| 27.2 | Tirón a la izquierda (próximos avisos) y después tocá la mascota. | El panel se cierra. | |
| 27.3 | Sin nada en pantalla, tocá dos veces la mascota. | Corazones, como siempre. | |
| 27.4 | Tirón rápido **hacia abajo**. | **No** se abre la app: aparece una tarjeta flotante "Recordatorio rápido" con el teclado listo, un campo "¿Qué te recuerdo?" y opciones: En 30 min, En 1 hora, En 3 horas, Mañana 9:00. | |
| 27.5 | Escribí algo, elegí "En 30 min" y tocá **Guardar** (o la tecla Listo del teclado). | Se cierra, la mascota salta y dice "Listo: te aviso a las HH:MM". El recordatorio aparece en la app y suena a esa hora. | |
| 27.6 | Abrí la tarjeta y tocá afuera, o **Cancelar**, o el botón atrás. | Se cierra sin guardar. | |

## 28. Nuevo comportamiento de la mascota (versión 1.0.10)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 28.1 | Dejá el celular con la pantalla prendida **10 minutos sin tocar nada**. | La mascota se acuesta a dormir (zzz **del lado izquierdo** de la cabeza). | |
| 28.2 | Con la mascota dormida, apagá y prendé la pantalla. | Se levanta. | |
| 28.3 | Con la mascota dormida, tocala. | Se levanta (sin corazones). | |
| 28.4 | Usá otras apps (tocando la pantalla) durante más de 10 minutos. | **No** se duerme mientras estés tocando la pantalla. | |
| 28.5 | Ajustes › **No molestar**: activalo con un horario que incluya la hora actual. | Al ratito se duerme y queda dormida aunque prendas la pantalla. Si llega un aviso, se despierta, avisa y al minuto vuelve a dormir. | |
| 28.6 | **1 toque** en la mascota. | Corazones (desde la 1.0.35: salta y tira corazones, ver 46). | |
| 28.7 | **2 toques**. | Salta. | |
| 28.8 | **3 toques**. | Da una vuelta 360°. | |
| 28.9 | Tirón **hacia arriba**. | Se esconde 1 hora ("La mascota vuelve en 1 hora"). Vuelve sola. | |
| 28.10 | Tirón **a la derecha**. | "Avisos en silencio". Los avisos siguen llegando (globo y notificación) pero sin sonido ni voz. En la app aparece "🔕 Avisos en silencio · Activar sonido". | |
| 28.11 | Otro tirón a la derecha (o "Activar sonido" en la app). | "Avisos con sonido otra vez". | |
| 28.12 | Tirón **hacia abajo**, tocá **"Elegir día y hora"**. | Aparecen la fecha y la hora para escribir. Si elegís un momento que ya pasó, avisa y no deja guardar. Al guardar, avisa a esa hora. | |
| 28.13 | (1.0.11) Tirón hacia abajo › **Elegir día y hora**. | Aparece una fila "‹ Hoy ›" (las flechas cambian de día: Mañana, Vie 2 oct…) y dos casilleros chicos: Hora y Min. Todo entra en la tarjeta sin tapar el resto. | |

## 29. Ocultar el aviso fijo de Android “se muestra sobre otras apps” (versión 1.0.12)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 29.1 | Ajustes › Permisos › “Aviso de Android…” › **Ocultar**. | Se abre la pantalla de Android de **ese aviso** (o, en algunas marcas, las notificaciones de “Sistema Android”). Apagá “Mostrar notificaciones”. | |
| 29.2 | Volvé y bajá la cortina. | El aviso “Pet Notifications se muestra sobre otras apps” ya no aparece. La mascota sigue funcionando igual. | |
| 29.3 | “Ver la configuración guiada de nuevo”: antes del último paso aparece “Un aviso de Android”. | “Ocultar el aviso” abre el mismo ajuste; “Ahora no” sigue sin cambios. | |

## 30. Pantallas nuevas con pestañas: Avisos, Mascota y Ajustes (versión 1.0.15)

Incluye también lo de 1.0.13 y 1.0.14: nombre propio para cada mascota, colores crema, botones **Aplazar** (izquierda) y **Listo** (derecha), y sin la ayuda “Tocá: abrir”.

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 30.1 | Abrí la app. | Abajo hay 3 pestañas: **Avisos**, **Mascota**, **Ajustes**. Todo en tonos crema con letra redondeada. | |
| 30.2 | Pestaña **Avisos**, tarjeta de arriba. | “(Tu mascota) está en pantalla”, con **Pausar 1 h** y **Silenciar**. Al pausar, la tarjeta se ve punteada y aparece **Mostrar ahora**. | |
| 30.3 | Si falta algún permiso. | Tarjeta amarilla “Me falta(n) … permiso(s)” con **Arreglar**, que lleva a Permisos y batería. | |
| 30.4 | Sin recordatorios. | La mascota grande, “Todavía no tengo nada para recordarte”, **Crear el primero** y **Probar un aviso**. | |
| 30.5 | Creá 2 o 3 recordatorios (botón marrón **+ Recordatorio**). | Arriba “Lo próximo” con el más cercano destacado (“en 2 h”); abajo agrupados en Hoy / Mañana / Esta semana / Más adelante. Tocar uno abre su detalle. | |
| 30.6 | Con el calendario activo (Pro). | Los eventos de los próximos 7 días aparecen en la lista con la etiqueta “Calendario”. | |
| 30.7 | Después de que suene uno. | Aparece “Ya avisados hoy (1)”, que se abre y se cierra. | |
| 30.8 | Consejo “¿Sabías?” al final de Avisos. | Cambia cada día; con la **X** desaparece hasta mañana. | |
| 30.9 | Pestaña **Mascota**. | Arriba tu mascota en un círculo rosa, su nombre y **Cambiar nombre / Ponerle nombre**. Debajo “Tus 2 mascotas” y, con candado, “Más amigos con Pro”. | |
| 30.10 | “Cómo se ve”. | Tamaño y Transparencia con deslizadores finos (bloqueados con PRO en el plan gratis). Modo fantasma con su interruptor. **Probar un aviso** al final. | |
| 30.11 | Pestaña **Ajustes**. | Tarjeta verde del plan (Prueba de Pro / Plan gratis / Tenés Pro) y filas: Mascota en pantalla (interruptor, dice **Prendida**), Cómo te aviso, Cuándo no molestar, Calendario, Permisos y batería, Ayuda. Cada fila muestra un resumen. | |
| 30.12 | Entrá a cada fila y volvé con la flecha o el botón Atrás del celular. | Se abre su página y vuelve a Ajustes (Atrás no cierra la app). | |
| 30.13 | **Permisos y batería**. | Cada permiso con “✓ Listo” o **Permitir**; abajo la guía de batería y el aviso de Android. | |
| 30.14 | Ayuda › **Cómo se usa**. | Explica los toques y tirones sobre la mascota. | |
| 30.15 | Al hacer scroll en Mascota o Ajustes. | El contenido no se mete debajo de la hora y la batería. | |
| 30.16 | Tema oscuro del celular. | Las pantallas se ven en marrones oscuros y se leen bien. | |

## 31. Tutorial “Lo que sabe hacer” la mascota (versión 1.0.16)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 31.1 | Instalá la versión y abrí la app. | Aparece el tutorial una sola vez (también a quien ya tenía la app). Arriba dice “Lo que sabe hacer (nombre de tu mascota)” y **Saltar**. | |
| 31.2 | Pasá las tarjetas con **Siguiente** o deslizando de costado. | 7 tarjetas, cada una con una animación de tu mascota: Tocame, Llevame a donde quieras, Tirones rápidos, Cuando te aviso, También duermo, Cambiá cómo me veo, Probalo vos. Los puntitos de abajo marcan en cuál estás. | |
| 31.3 | Botón Atrás del celular dentro del tutorial. | Vuelve a la tarjeta anterior; en la primera, cierra el tutorial. | |
| 31.4 | En **Probalo vos**, tocá a la mascota flotante 1, 2 y 3 veces. | Arriba de la lista dice “¡Bien! Eso fue: 1 toque” (o 2, o 3) y se tilda en la lista. La mascota hace su animación. | |
| 31.5 | En **Probalo vos**, hacé los 4 tirones sobre la mascota. | Cada uno se tilda. **No pasa nada de verdad**: no se esconde, no se silencia, no se abre el recordatorio rápido ni la lista. Con los 7 tildados: “¡Los hiciste todos!”. | |
| 31.6 | Tocá **Listo** y después hacé un tirón hacia arriba sobre la mascota. | Ahora sí funciona normal (se esconde 1 hora). Volvé a mostrarla desde la app. | |
| 31.7 | Volvé a abrir el tutorial desde: pestaña **Mascota** › “Lo que sé hacer”; **Ajustes › Ayuda › Cómo se usa**; y el consejo “¿Sabías?” de Avisos (“Ver tutorial”). | Los tres abren el mismo tutorial. | |
| 31.8 | Con la mascota apagada o en pausa, entrá a **Probalo vos**. | Dice “Para practicar tengo que estar en pantalla” y el botón **Mostrar mascota** la hace aparecer. | |
| 31.9 | Instalación nueva desde cero. | Primero la configuración guiada (permisos, mascota y nombre) y, al terminar, el tutorial. | |

## 32. Avisos en la pantalla de bloqueo (versión 1.0.17)

Antes, si el aviso llegaba con la pantalla **prendida pero bloqueada**, la app creía que lo iba a mostrar la mascota y mandaba la notificación sin sonido; Android esconde esas notificaciones en la pantalla de bloqueo, así que no se veía nada.

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 32.1 | Creá un recordatorio para dentro de 2 minutos y bloqueá el celular (pantalla apagada). | Al llegar la hora suena y la notificación aparece en la pantalla de bloqueo, con **Aplazar** y **Listo**. | |
| 32.2 | Igual, pero un poco antes de la hora prendé la pantalla **sin desbloquear**. | También suena y aparece en la pantalla de bloqueo (este era el caso que fallaba). | |
| 32.3 | Desbloqueá sin tocar la notificación. | La mascota repite el aviso. | |
| 32.4 | Si en el Xiaomi sigue sin aparecer: Ajustes › Aplicaciones › Pet Notifications › Notificaciones › canal **Avisos** › **Pantalla de bloqueo** (o “Mostrar en pantalla de bloqueo”) › activar. | Xiaomi a veces trae esto apagado para las apps nuevas. Contame si estaba apagado. | |
| 32.5 | (Opcional) Con “Ocultar contenido sensible” activado en el celular. | En la pantalla de bloqueo dice “(Tu mascota) tiene un aviso para vos · Desbloqueá el celular para verlo”, sin el texto del recordatorio. | |

## 33. Vínculo: corazones, niveles y trucos (versión 1.0.18)

Cada “Listo” suma un corazón (hasta 10 por día) y el primer mimo del día suma otro. Con los corazones la mascota sube de nivel y aprende trucos. Nunca se pierde lo ganado. Quien ya tenía la app arranca con los “Listo” que ya había marcado.

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 33.1 | Instalá la versión y abrí la app. | Si ya habías marcado varios “Listo”, aparece una sola vez “¡Ya nos conocíamos!” con tu nivel (por ejemplo “Somos amigos”) y **Ver nuestro camino**. | |
| 33.2 | Pestaña **Mascota**. | Debajo del nombre, la tarjeta del vínculo: título del nivel, chip “Nivel N”, 10 corazones de avance, “Faltan N «Listo» para el nivel…”, lo próximo que aprende y **Ver el camino**. | |
| 33.3 | **Ver el camino**. | “Nuestro camino”: los 8 niveles; los ganados con tilde, el actual resaltado en rosa y los que faltan con los corazones que piden. Los accesorios dicen “Muy pronto”. | |
| 33.4 | Tocá **Listo** en el globo de un aviso (recordatorio o calendario). | Sube un “❤ +1” desde la mascota y se desvanece. En la tarjeta del vínculo hay un corazón más. “Probar un aviso” no suma. | |
| 33.5 | Tocá **Listo** desde la notificación con la mascota a la vista. | También sale el “+1”. Con el celular bloqueado no se ve, pero el corazón suma igual. | |
| 33.6 | Tocá a la mascota por primera vez en el día. | Suma un corazón (solo el primer mimo del día). | |
| 33.7 | Cuando un “Listo” te hace subir de nivel. | La mascota se pone contenta, le salen corazoncitos y arriba aparece una tarjeta “Nivel 3 · Amigos — ¡Aprendí a dar la patita!” con **Mostrame** y **Después**. Se va sola a los 10 s. Nunca aparece encima de un aviso: espera a que lo resuelvas. | |
| 33.8 | **Mostrame** en esa tarjeta. | Hace el truco en el momento (patita, baile o saludo de buenos días). | |
| 33.9 | Si subís de nivel con la app abierta. | La misma tarjeta sale como ventana dentro de la app. | |
| 33.10 | Desde el **nivel 3**: tocá a la mascota 3 veces, varias veces seguidas. | Una vez da la vuelta y la otra **levanta la patita**, alternando. | |
| 33.11 | Desbloqueá el celular. | La mascota **mueve la cola** (como mucho una vez por hora). | |
| 33.12 | Desde el **nivel 4**: el primer desbloqueo del día a la mañana (después de tu horario de No molestar, o de las 6:00). | Dice su sonido y aparece “¡Buen día! Hoy tenemos N cosas.” (o “no tenemos nada pendiente”). Tocarla abre la app; se va sola a los 6 s. Una sola vez por día. | |
| 33.13 | Desde el **nivel 7**: marcá varios “Listo”. | Más o menos 1 de cada 5 veces, la mascota **baila**. También baila sola de vez en cuando (máximo 2 veces por día). | |
| 33.14 | Con No molestar o con los avisos en silencio (tirón a la derecha). | Los festejos esperan al siguiente desbloqueo. | |
| 33.15 | Tutorial (Ayuda › Cómo se usa). | Tiene una tarjeta nueva, “Nos hacemos amigos”. | |

Para probar sin esperar semanas: decime y te armo una versión de prueba que empiece con más corazones.

## 34. Vida propia: la mascota hace cosas sola (versión 1.0.19)

Cada tanto, sin que la toques, la mascota hace algo corto: mira a un lado y al otro, se da vuelta y vuelve, se estira, bosteza, se sienta un ratito, salta, se sacude, mueve la cola, se persigue la cola o da unos pasitos hacia un costado y vuelve a su lugar. Solo con la pantalla prendida y desbloqueada, despierta y sin ningún aviso abierto.

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 34.1 | Dejá el celular desbloqueado unos minutos, sin tocar la mascota. | Cada 30 a 60 segundos hace alguna de esas cosas, sin repetir la misma dos veces seguidas. | |
| 34.2 | Esperá a que dé unos pasitos. | Camina de perfil, a saltitos, unos centímetros hacia un costado; a veces mira alrededor; y vuelve caminando al lugar donde la dejaste. | |
| 34.3 | Tocala mientras camina. | Se frena. Si no la arrastrás, al ratito vuelve sola a su lugar. Si la arrastrás, se queda donde la dejes. | |
| 34.4 | Pestaña **Mascota** › “Cómo se ve” › **Qué tan inquieta es**: probá **Tranquila** y **Inquieta**. | Tranquila: algo cada 1 a 2 minutos y camina menos. Inquieta: algo cada 15 a 30 segundos y camina más. | |
| 34.5 | Con un aviso en el globo o el panel de “lo próximo” abierto. | No hace nada sola hasta que se cierre. | |
| 34.6 | Dejala 10 minutos sin usar el celular (pantalla prendida). | Igual se duerme (lo que hace sola no la mantiene despierta). Dormida no hace nada. | |
| 34.7 | Que nunca quede “finita”, de costado congelada o estirada después de una acción. | Siempre vuelve a su postura normal. | |
| 34.8 | Batería: usala un día normal. | No debería notarse más gasto que antes. Si lo notás, contame. | |

## 35. Giro continuo del perro (versión 1.0.20, de prueba)

Solo el **perro** usa el giro nuevo por ahora (se dibuja en cada ángulo, como en el prototipo). Las otras mascotas siguen como antes.

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 35.1 | Con el perro, tocalo 3 veces. | Da una vuelta completa pareja, sin “afinarse” ni cambiar de dibujo de golpe, con saltitos suaves, y termina contento de frente. | |
| 35.2 | Dejalo solo un rato (mejor en “Inquieta”). | Cuando **mira a los costados**, gira la cabeza a medias hacia un lado y el otro. Cuando **se da vuelta**, gira hasta quedar de espaldas y vuelve girando. | |
| 35.3 | Cuando da **pasitos**. | Gira suave hasta quedar de perfil, camina y al terminar vuelve a mirarte girando. | |
| 35.4 | Mirá bien durante el giro. | Nada se sale de lugar (panza, orejas, cola, hocico), no parpadea ni salta la imagen al empezar o terminar. Si algo se ve raro, mandame una captura o un video. | |
| 35.5 | Cambiá a otra mascota y tocala 3 veces. | Sigue girando como antes (con los 4 dibujos). | |

## 36. Mascota sentada en reposo (versión 1.0.21)

Incluye también el giro continuo del perro de la 1.0.20 (sección 35).

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 36.1 | Mirala sin tocarla. | Está **sentada** (patas de atrás plegadas a los costados, patitas de adelante juntas). Sigue respirando, parpadeando y a veces pone cara contenta, sentada. Las aves están esponjadas; la rana, igual que antes. | |
| 36.2 | Tocala 2 o 3 veces. | Se **para** para saltar o girar y, unos 3 segundos después de terminar, se vuelve a sentar con un rebotito. | |
| 36.3 | Dejala hacer cosas sola (vida propia). | Para caminar, mirar a los costados, darse vuelta, estirarse o saltar se para; para mover la cola, bostezar o sacudirse queda sentada. | |
| 36.4 | Cuando avisa algo. | Se para para dar el aviso; con el globo abierto sigue parada y al cerrarse, al ratito, se sienta. | |
| 36.5 | Que nunca quede a medias. | Siempre termina sentada o parada entera, nunca cortada ni estirada. Si ves algo raro, mandame una captura o un video. | |
| 36.6 | Dormida. | Se acuesta igual que antes y al despertar queda sentada. | |

## 37. Herramientas de dueño (versión 1.0.22, solo de prueba)

Existen solo en las versiones de prueba que te paso; la que se sube a Google Play no las tiene.

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 37.1 | Ajustes › tocá **7 veces** la línea “Versión 1.0.22” (abajo de todo). | Se abre “Herramientas de dueño”. | |
| 37.2 | Elegí **Pro de dueño** y cerrá. | Todo lo Pro queda desbloqueado (tamaño, transparencia, calendario, las 10 mascotas, etc.). La versión dice “· dueño”. Sigue así aunque termine la prueba gratis. | |
| 37.3 | Elegí **Simular plan gratis**. | La app se comporta como alguien sin Pro y sin prueba (2 mascotas, candados PRO). | |
| 37.4 | Elegí **Normal**. | Vuelve a depender de la compra y de la prueba gratis, como un usuario real. | |
| 37.5 | **+10 corazones** varias veces y cerrá. | Sube de nivel y aparecen los festejos (“¡Aprendí a dar la patita!”, etc.), así podés probar los trucos sin esperar. | |
| 37.6 | **Reiniciar vínculo**. | Vuelve al nivel 1 con 0 corazones. | |

## 38. Pose “peluche”, gato nuevo, vida propia en su lugar y globos al costado (versión 1.0.23)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 38.1 | Mirala en reposo. | Sentada como un peluche: patas estiradas hacia adelante con las plantitas rosadas y bracitos al costado (perro, gato, vaca, chancho, león, mono). Ya no parece una araña. | |
| 38.2 | Elegí el **gato**. | Cara más amigable: orejas redondeadas, rayitas suaves, hocico claro, ojos grandes y sonrisa abierta; igual al estar parado, sentado, dormido, de perfil y de espaldas. | |
| 38.3 | Dejala sola unos minutos (mejor en “Inquieta”). | **Ya no se desplaza** por la pantalla. Hace cosas en su lugar: mira a los costados, da la vuelta de 360°, tira un corazón, mueve la cola, bosteza, se estira, se sacude. | |
| 38.4 | Con el **perro**, esperá a que gire solo la cabeza. | Sentado, gira la cabeza hacia un costado (a veces al otro) y vuelve; el cuerpo queda quieto. | |
| 38.5 | Poné la mascota **arriba de todo** de la pantalla y tocá “Probar un aviso”. | El sonido y el globo aparecen **al costado** de la mascota (del lado con más lugar), con la colita apuntando a su cabeza. Lo mismo con el panel de lo próximo (tirón a la izquierda). | |
| 38.6 | Arriba de todo, tocala una vez. | Los corazones salen al costado. | |
| 38.7 | Con la mascota en el medio o abajo. | Todo sigue apareciendo arriba, como antes. | |

## 39. Giro con el modelo 2,5D en todas las mascotas (versión 1.0.24)

Antes solo el perro giraba “de verdad”; las demás cambiaban entre 4 dibujos. Ahora todas usan el modelo que aprobaste en la vista previa.

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 39.1 | Con cada mascota, tocala **3 veces**. | Da la vuelta de 360° de forma pareja (sin cambiar de dibujo de golpe), con saltitos suaves, y termina de frente contenta. | |
| 39.2 | Mirá el **principio y el final** del giro. | No tiene que verse un salto feo al pasar del dibujo quieto al modelo (puede cambiar un poquito la cola). Anotá en cuál se nota más. | |
| 39.3 | Dejala sola un rato (mejor en “Inquieta”). | Al mirar a los costados gira el cuerpo a medias, y a veces se da vuelta y se queda de espaldas; en todas las mascotas. | |
| 39.4 | Con **gato, vaca, chancho, león y mono**, esperá a que gire solo la cabeza. | Sentado, gira la cabeza hacia un costado (a veces al otro) y vuelve, con el cuerpo quieto, como ya hacía el perro. | |
| 39.5 | Con el **león**, mirá el giro. | La melena se ve bien redondeada (festones) en todos los ángulos. | |
| 39.6 | Usala un rato normal. | Que no se trabe ni se ponga lenta al girar. | |

---

## 40. Pelajes (Pro) y conejo (versión 1.0.25; probar con la 1.0.28)

Los pelajes y el conejo no tienen dibujos fijos: todo (quieta, sentada, acostada, dormida, de perfil, de espaldas, dando la patita y las pantallas de la app) se dibuja con el modelo 2,5D. Para probar sin Pro y con Pro, usá las herramientas de dueño (forzar plan).

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 40.1 | Con Pro, pestaña **Mascota**: mirá la fila **Pelaje** debajo de las mascotas. | Una ficha por pelaje con el dibujo de la mascota en ese color (dálmata, tricolor, con manchas, etc.); se desliza de costado. El original está marcado. | |
| 40.2 | Elegí otro pelaje. | La mascota flotante cambia de color enseguida, y también el dibujo de arriba de la pestaña, el de Avisos y la ficha en la grilla de mascotas. | |
| 40.3 | Con ese pelaje, mirala **quieta, sentada, girando, mirando a los costados y girando solo la cabeza**. | Siempre del mismo color, sin saltitos al empezar o terminar el giro. | |
| 40.4 | Con ese pelaje, dejala dormirse (o pausala desde la notificación) y despertala. | Acostada y dormida (con zetas) en ese pelaje, de costado como siempre: cabeza de frente a la izquierda y el cuerpo estirado a la derecha. | |
| 40.5 | Tocala hasta que **dé la patita** (o sumá corazones). | Levanta la patita con la cara contenta, en su pelaje. | |
| 40.6 | Cambiá de mascota y volvé. | Cada mascota recuerda su pelaje. | |
| 40.7 | Sin Pro (forzar plan gratis): mirá la fila **Pelaje** y tocá uno con candado. | Todos menos el original tienen candado y te llevan a los planes. La mascota vuelve al pelaje original. En la comparación de planes aparece **Pelajes: El original / Todos**. | |
| 40.8 | Con Pro, elegí el **conejo**. | Aparece en la grilla (es la mascota 11). Dice “¡Hop hop!”. Se ve bien quieto, sentado, girando, girando la cabeza, de espaldas, acostado y dormido. | |
| 40.9 | Conejo con el pelaje **Orejas caídas**. | Las orejas caen a los costados de la cabeza en todas las poses. | |
| 40.10 | Con la **rana**, el **pato** y el **búho** en otro pelaje, dejalos dormir. | La rana se acuesta de costado como las de cuatro patas; el pato y el búho duermen esponjados en el lugar, como siempre. | |
| 40.11 | Abrí el tutorial y el diálogo para ponerle nombre, con un pelaje distinto. | La mascota aparece en su pelaje. | |
| 40.12 | Con el **conejo**, mirá las puntas de las orejas: quieto, girando (3 toques) y en las pantallas de la app. | Las puntas se ven redondeadas, nunca cortadas en línea recta (1.0.27: el conejo se dibuja un poco más chico para que entren). Decime si lo ves demasiado chico al lado de las demás. | |
| 40.13 | Con la **vaca, el león y el mono** en otro pelaje, dejalos dormir. | La cola se ve entera (en la 1.0.25 se salía un poco por la derecha). | |
| 40.14 | (1.0.32) Mirá los pelajes de la **vaca** y de la **rana** en la fila de Pelaje. | Ninguno se parece a otro: la vaca 2 ahora es negra con manchas blancas y la rana 5 es lila con lunares. |  |

---

## 41. Probar mascotas y pelajes Pro sin Pro (versión 1.0.29; probar con la 1.0.30)

Para hacerla sin Pro: en Ajustes tocá 7 veces la versión y elegí "Simular plan gratis". Al terminar, volvé a "Normal".

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 41.1 | En la solapa **Mascota**, mirá debajo de "Más amigos con Pro". | Dice "Tocá una para probarla 5 minutos." | |
| 41.2 | Tocá una mascota Pro (por ejemplo, el león). | **La app se queda en Mascota** (1.0.30). La ficha del león queda marcada, la fila de pelajes muestra los del león y debajo aparece la tarjeta "Probando: León" con la etiqueta PRO, la cuenta regresiva (5:00) y tres botones. La mascota flotante pasa a ser el león. La misma tarjeta está arriba en Avisos. | |
| 41.2b | Tocá varias mascotas Pro seguidas (león, chancho, búho). | Cada toque cambia la mascota flotante al instante, sin cambiar de pantalla. | |
| 41.3 | Con el león flotando: tocalo 1, 2 y 3 veces, arrastralo y tiralo. | Hace todo como una mascota propia (corazones, salto, giro, etc.). | |
| 41.4 | En la tarjeta, tocá **Probar un aviso**. | El león muestra el globo de prueba ("¡Hola! Soy tu león…"). | |
| 41.5 | Tocá **Volver a la mía**. | La tarjeta desaparece y vuelve tu mascota de siempre. | |
| 41.6 | En **Pelaje**, tocá uno con la etiqueta PRO (no el primero). | Igual que 41.2, pero la tarjeta dice "Probando: Perro, pelaje 3" (o la que tengas) y la mascota flotante cambia de color. | |
| 41.7 | Empezá una prueba y esperá 5 minutos (con la app abierta o cerrada). | Al terminar, vuelve sola tu mascota y la tarjeta desaparece. | |
| 41.8 | Con la mascota pausada ("Pausar 1 h"), probá una mascota Pro. | La pausa se corta y aparece la mascota Pro flotando. | |
| 41.9 | Tocá **Ver planes** en la tarjeta. | Se abre la pantalla de Pro. | |

## 42. Pestañas, fichas Pro y una sola mascota gratis (versión 1.0.30)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 42.1 | En la app, deslizá el dedo de derecha a izquierda y al revés sobre la pantalla. | Pasa entre Avisos, Mascota y Ajustes, y la barra de abajo marca la pestaña. Los botones de abajo siguen andando. | |
| 42.2 | En Mascota, mové la fila de pelajes y los deslizadores de tamaño y transparencia (con Pro). | Se mueven ellos; no se cambia de pestaña. | |
| 42.3 | Sin Pro, mirá las mascotas de "Más amigos con Pro" y los pelajes. | Se ven a todo color, como las tuyas, con una etiqueta roja **PRO** en la esquina de arriba a la derecha. Sin candados ni bordes punteados. | |
| 42.4 | Desinstalá la app (o borrá sus datos) e instalala de nuevo. En el primer paso, elegí una mascota. | Debajo de las mascotas dice "Esta va a ser tu mascota gratis. Durante la prueba de Pro (7 días) podés usar las 11; después, la podés cambiar una sola vez." | |
| 42.5 | Con "Simular plan gratis" (7 toques en la versión), andá a Mascota. | Arriba dice "Tu mascota" con **una sola**: la que elegiste al instalar. | |
| 42.6 | Debajo está el botón con borde "Cambiar mi mascota gratis" ("Te queda 1 cambio…"). Tocalo, elegí otra y tocá Listo. | Se elige una sola (tocar otra reemplaza la anterior). Queda la nueva y el botón desaparece. | |

## 43. Tema claro u oscuro (versión 1.0.31)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 43.1 | En **Ajustes**, debajo de "Mascota en pantalla", está **Tema**: Como el celular / Claro / Oscuro. Tocá **Claro**. | La app se pone clara en el momento (aunque el celular esté en oscuro) y los íconos de la barra de arriba se ven oscuros. | |
| 43.2 | Tocá **Oscuro**. | La app se pone oscura y los íconos de arriba, blancos. | |
| 43.3 | Cerrá la app y abrila de nuevo. | Sigue con el tema que elegiste. | |
| 43.4 | Elegí **Como el celular** y cambiá el modo oscuro del Xiaomi. | La app sigue al celular. | |

La mascota flotante y sus globos no cambian con el tema: tienen sus propios colores.

## 44. Globo nuevo, tarjeta y cartas de colores (versión 1.0.33)

Para estas pruebas conviene apagar "Se esconde sola" (Ajustes → Avisos), salvo en la 44.5.

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 44.1 | En **Ajustes → Avisos**, arriba está **Globo del aviso** con los dos globos dibujados. Tocá cada uno. | Se ven el **Caramelo** (rosa festoneado, viene elegido) y la **Nubecita** (nube blanca con borde marrón y sombra verde), con el sonido de tu mascota en minúsculas. El elegido queda marcado. | |
| 44.2 | Tocá **Probar un aviso** y salí al inicio del celular. | Al **costado** de la mascota aparece el globo elegido con "guau" (o el sonido de tu mascota) en minúsculas; las letras saltan una por una, con un corazoncito y dos destellos. Ya no aparece la estrella amarilla que tiembla. | |
| 44.3 | Tocá el globo. | Se abre la **tarjeta** al costado: arriba "Tu perro te dice" (o su nombre), el título y el texto. | |
| 44.4 | Creá un recordatorio para dentro de 2 minutos. Cuando avise, tocá el globo. | La tarjeta dice "(nombre) te avisa · hora", con **Listo** (rosa) y **Aplazar** (con borde). Listo festeja como antes. | |
| 44.5 | Otro recordatorio. Cuando avise, **tocá la mascota** (no el globo). | El globo se cierra y aparece un **sobre rojo** chiquito pegado a sus pies. No vuelve a llamar. | |
| 44.6 | Tocá el sobre rojo. | Se abre la tarjeta en **rosa**, apenas inclinada, "Quedó sin atender · hora", con Listo y Aplazar. Tocar la mascota la cierra y el sobre sigue ahí; Listo o Aplazar lo sacan. | |
| 44.7 | En Ajustes → Avisos prendé **Avisos sin interrumpir**. Creá un recordatorio para dentro de 2 minutos. | A la hora no aparece nada ni suena: queda un **sobre amarillo** a sus pies (y una notificación callada). Al abrirlo: "Llegó sin interrumpir · hora". Apagalo después. | |
| 44.8 | Con esa opción apagada, creá un recordatorio y marcá **Sin interrumpir** en el editor. | Solo ese llega como sobre amarillo. En la lista dice "· Sin interrumpir". | |
| 44.9 | En Ajustes → Avisos, **Frases amigables**: Nunca / Pocas / Normal / Muchas. Elegí **Muchas** y usá el celular un rato entre las 9 y las 21. | Cada tanto la mascota mueve la cola y deja un **sobre celeste**. Al abrirlo, una frase ("No te olvides de tomar agua", "Éxito en tu jornada") y el botón **Gracias**, que lo saca. Nunca suena ni aparece en el horario de No molestar. | |
| 44.10 | Con varios sobres, mirá la pila. | Como mucho se ven 3 sobres; si hay más, un número "+N". Se abre el de arriba (el último que llegó). | |
| 44.11 | Arrastrá la mascota a otro lado y apagá/prendé el celular. | Los sobres la acompañan y siguen ahí después de reiniciar el celular. | |
| 44.12 | Con "Se esconde sola" prendido, dejá que un aviso se vaya solo. | Queda un sobre rojo; si el aviso vuelve a sonar (repetición o aplazado), el sobre se saca y avisa de nuevo. | |


## 45. Cartas nuevas, PRO en rojo suave, prueba de 7 días y MimuPet (versión 1.0.34)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 45.1 | Instalá encima de la 1.0.33 sin borrar nada. | La app se llama **MimuPet** (ícono, ajustes del celular). Si tenías cartas, siguen ahí con los colores nuevos. | |
| 45.2 | Dejá un aviso pendiente (tocá la mascota cuando avisa). | Sobre **rosa** con un sello y una **campanita**. Al abrirlo: tarjeta rosa, "Quedó pendiente · hora" y la campanita arriba a la derecha. | |
| 45.3 | Un recordatorio **Sin interrumpir**. | Sobre **crema** con una **luna**; la tarjeta, color papel. | |
| 45.4 | Frases amigables en **Muchas**. | Sobre **verde suave** con una **hojita**. A la mañana no dice "que descanses"; a la noche no dice "¿ya desayunaste?". | |
| 45.5 | Ajustes → Avisos: leé las ayudas del globo, de "sin interrumpir", de frases y de "Las cartitas". | Ningún texto dice "roja", "amarilla" ni "celeste". | |
| 45.6 | Mirá las etiquetas **PRO** (fichas de mascotas y pelajes, interruptores Pro de Ajustes, Tu plan). | Todas iguales: "PRO" claro sobre **rojo suave**, esquinas apenas redondeadas. En tema oscuro, rojo más claro con letras oscuras. | |
| 45.7 | Pestaña Mascota sin Pro: probá 2 mascotas Pro. Después Ajustes → Diagnóstico. | En "Prueba de mascotas Pro": "Veces que probó…: 2" y "Todavía no compró Pro". | |
| 45.8 | Instalación nueva: mirá la tarjeta de la mascota en Avisos. | "Prueba de Pro · te quedan **7** días" (antes 14 en la versión publicada). | |
| 45.9 | Con el globo **Caramelo**, hacé que avise sobre **WhatsApp, Instagram y el navegador**. | Anotá si molesta o tapa demasiado (pedido de Cowork, C11). | |
| 45.10 | Fin de la prueba (con Herramientas de dueño o esperando 7 días, sin comprar). Abrí la app. | Una sola vez: "Terminó tu prueba de Pro", lo que se va (la mascota Pro que usabas, pelajes, calendario, voz, solo lo que usaste) y que el vínculo queda igual, con **Comprar Pro** y **Seguir gratis**. No vuelve a aparecer. | |
| 45.11 | El día antes del fin de la prueba, con una mascota Pro en pantalla. | La mascota avisa "Mañana vuelvo a ser (tu mascota gratis). Si te gustó cómo estamos, podés quedarte con Pro". | |


## 46. Gestos nuevos (versión 1.0.35)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 46.1 | **1 toque** en la mascota. | Salta y tira corazones, las dos cosas juntas. | |
| 46.2 | **2 toques**. | Da la vuelta 360°. Si ya aprendió la patita (nivel 3), una vez da la vuelta y la siguiente la patita. | |
| 46.3 | **3 toques**. | No hace nada (queda libre por ahora). | |
| 46.4 | Mascota → Tutorial, tarjeta **Tocame**. | La animación muestra 1 toque (salto con corazones) y 2 toques (vuelta). El texto dice "1 toque: salto y te tiro corazones. 2 toques: doy una vuelta." | |
| 46.5 | En **Probalo vos**, tocá la mascota 1 y 2 veces. | La lista tiene solo "1 toque" y "2 toques" y se tildan. | |
| 46.6 | Ajustes → Ayuda → Cómo se usa. | Dice lo mismo que el tutorial. | |

## 47. Cierre de la prueba de Pro con la lista completa (versión 1.0.36)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 47.1 | Herramientas de dueño (7 toques en la versión) → **Ver el cierre de la prueba de Pro**. | El cartel muestra siempre todo lo que se va con Pro: las 11 mascotas, pelajes, calendario, voz, No molestar, tamaño y transparencia, y cuánto insisten los avisos. | |
| 47.2 | Antes, con Pro, prendé el calendario y cambiá el tamaño. Abrí de nuevo el cartel. | Esas dos líneas salen **en negrita** con "(lo usabas)". | |
| 47.3 | Leé la primera línea. | "…y tu perro sigue con vos" (o el nombre que le pusiste), con minúscula. | |

## 48. Globo corto y tarjeta sostenida con las patitas (versión 1.0.37)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 48.1 | **Probar un aviso** y salí al inicio. | El globo con el sonido aparece **más chico** (un tercio menos que antes), al costado, con la **punta del pico tocando el cachete** de la mascota. Dura alrededor de un segundo. | |
| 48.2 | Sin tocar nada. | Sola, aparece la **tarjeta debajo de la mascota**: la mascota queda arriba, la tarjeta le tapa las patas de abajo y sus **dos patitas** quedan apoyadas sobre el borde, como sosteniéndola. | |
| 48.3 | Probalo con perro, gato, pollito y rana. | Las patitas tienen el color del pelaje de cada una (claro con borde oscuro). Anotá si en alguna se ven raras. | |
| 48.4 | Poné la mascota bien abajo de la pantalla y probá un aviso. | Como abajo no entra, la tarjeta sale al costado (como antes), sin patitas. | |
| 48.5 | Recordatorio real: cuando aparece la tarjeta, tocá **Listo**. Otro: tocá la **mascota**. | Listo festeja; tocar la mascota cierra el aviso y deja la carta pendiente a sus pies. | |
| 48.6 | Tocá el globo antes de que pase el segundo. | Abre la tarjeta enseguida. | |
| 48.7 | Ajustes → Cómo te aviso, y la tarjeta de "Probando" una mascota Pro. | El globo de muestra se ve más chico. La ayuda dice que el aviso aparece solo, sostenido con las patitas. "Probando" y los consejos dicen "2 veces para que gire". | |

## 49. La mascota grande arriba en Avisos (versión 1.0.38)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 49.1 | Abrí la app en **Avisos**, con y sin recordatorios. | Arriba siempre está la mascota grande en un círculo rosa, con su nombre y una línea (sonido, prueba de Pro o "vuelve a las…"). Ya no está la tarjeta "Perrito está en pantalla". | |
| 49.2 | Tocá la mascota grande. | Pone cara contenta y da un saltito. | |
| 49.3 | Sin recordatorios. | Debajo dice "todavía no…" con "Crear el primero" y "Probar un aviso", sin repetir la mascota. | |
| 49.4 | Ajustes, arriba. | Debajo de "Mascota en pantalla" están **Pausar la mascota** (botón "Pausar 1 h") y **Avisos en silencio** (interruptor). | |
| 49.5 | Pausá 1 hora y volvé a Avisos. | La mascota grande duerme, dice "Vuelve a las…" y tiene el botón **Mostrar ahora**. En Ajustes, el botón también dice "Mostrar ahora". | |
| 49.6 | Prendé **Avisos en silencio** y volvé a Avisos. | Debajo de la mascota aparece **Activar sonido**. | |

## 50. La pantalla de Avisos nueva (versiones 1.0.39 y 1.0.40)

Reemplaza la 49.1 a 49.3 y la 49.5 y 49.6 (la 49.4, Ajustes, sigue igual).

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 50.1 | Abrí la app en **Avisos**. | Arriba, una tarjeta rosa con un piso claro: la fecha grande ("Sábado 31 de octubre"), debajo "N cosas para hoy" (o "Nada pendiente para hoy"), la mascota grande abajo a la derecha y el chip **Vestir** abajo a la izquierda. | |
| 50.2 | Bajá por la lista despacio. | La escena se achica de a poco: la mascota grande se achica y se va, aparece una chiquita a la izquierda de la fecha, y queda un encabezado fijo con una línea abajo. Nada se pisa en el medio. | |
| 50.3 | Volvé a subir hasta arriba de todo. | Recién cuando la lista llega arriba, la escena se agranda otra vez. | |
| 50.4 | Tocá la mascota grande o **Vestir**. | Abre la pestaña Mascota (por ahora la de siempre; el vestidor viene después). | |
| 50.5 | Pausá la mascota 1 h, después activá **Avisos en silencio**, después apagá la mascota. | Debajo de "cosas para hoy" aparece una pastilla: "Vuelve a las…" (con la mascota dormida), "Avisos en silencio" o "Mostrar mascota". Tocarla lo arregla. | |
| 50.6 | Teléfono en inglés. | La fecha dice "Saturday, October 31". | |
| 50.7 | Debajo de la escena (y del cartel amarillo, si falta un permiso). | La **semana**: 7 días desde hoy; hoy en marrón con letras claras, el resto blancos. Los días con algo tienen un puntito. Ya no hay cartel amarillo por "mascota apagada" (lo dice la pastilla de la escena). | |
| 50.8 | Sin nada para hoy. | "Hoy no tenés nada pendiente", "Empezá con uno de estos; se repiten solos" y 6 botones: Tomar agua, Pastilla, Regar las plantas, Sacar la basura, Cumpleaños y Otro (punteado). | |
| 50.9 | Tocá **Tomar agua**. | Abre "Nuevo recordatorio" con el título puesto y "Todos los días" marcado, y enseguida el reloj para elegir la hora. Guardalo. **Cumpleaños** abre el formulario sin reloj (falta la fecha). **Otro** lo abre vacío. | |
| 50.10 | Con 1 o 2 recordatorios que se repiten. | Debajo de la lista de Hoy, una fila "Ideas:" con las plantillas que todavía no usaste. Con 3 o más que se repiten, desaparece. | |
| 50.11 | Mirá los puntitos de la semana. | Un recordatorio de todos los días pone punto en los 7 días; uno semanal, solo en su día. | |
| 50.12 | Tocá otro día de la semana. | Queda con borde marrón y la lista muestra ese día ("Martes 3"), o "Nada para ese día". Tocá hoy para volver. Con hoy elegido, abajo aparecen también Mañana y Más adelante (lo que pasa de la semana). | |
| 50.13 | (1.0.43) Más abajo, después de Mañana y Más adelante. | **No** hay frase del día ni línea del vínculo (se sacaron para que Avisos no quede cargada; la frase es de la mascota flotante y el nivel está en el Vestidor). Sigue "Ya avisados hoy (N)" plegado y el consejo. | |
| 50.15 | Con el reloj del teléfono en 12 horas. | La hora "6:40 p. m." entra en una sola línea. | |

## 51. El Vestidor (versión 1.0.41)

Para probar sin esperar: Herramientas de dueño (7 toques en la versión, en Ajustes) → **+10 corazones**. Nivel 2 = 10, nivel 5 = 100, nivel 6 = 150, nivel 8 = 300.

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 51.1 | Pestaña **Mascota**. | Arriba, la mascota grande en un círculo rosa; debajo su nombre y el chip "Nivel N" (tocarlo abre Nuestro camino); los lugares **Cabeza · Cuello · Camita** ("Vacío"); el selector **Accesorios \| Mascotas**. | |
| 51.2 | En Accesorios, en nivel 1. | 4 fichas: Pañuelo (nivel 2), Camita (5), Gorrito de lana (6), Corona de flores (8), atenuadas con candado. Tocar una dice en qué nivel se gana. No hay nada a la venta. | |
| 51.3 | Subí al nivel 2. | Festejo "¡Gané un pañuelo para el cuello!" con "Ya me lo puse. Lo cambiás en el Vestidor." Al cerrarlo, **la mascota de la pantalla y la del Vestidor tienen el pañuelo puesto**. | |
| 51.4 | Tocá el Pañuelo. | Se lo saca (sin borde ni tilde, dice "Ganado"; Cuello "Vacío"). Tocalo de nuevo y se lo pone. También cambia en la mascota de la pantalla. | |
| 51.5 | Subí al nivel 8 y probá el **Gorrito** y la **Corona**. | Van en la cabeza: poner uno saca el otro. Probalo con varias mascotas (Mascotas → otra): el gorrito y la corona quedan en la cabeza de cada una, el pañuelo en el cuello (en pollito, pato, búho y rana, como baberito). | |
| 51.6 | Poné la **Camita**. | El escenario muestra la mascota durmiendo en la camita un par de segundos. Camita dice "Al dormir". Cuando la mascota de la pantalla se acuesta o duerme, está sobre la camita. | |
| 51.7 | Tocá 2 veces la mascota de la pantalla (vuelta 360°). | Gira con todo puesto: de espaldas se ve la banda del pañuelo, sin la punta. | |
| 51.8 | Avisos. | La mascota de la escena (grande y chiquita al bajar) tiene lo puesto. Tocar "Vestir" abre el Vestidor. | |
| 51.9 | Mascotas (el selector). | Lo de antes: especies, pelajes, tamaño, transparencia, fantasma, vida propia, tutorial y Probar un aviso; arriba, "León · dice …" con **Ponerle nombre**. | |
| 51.10 | Herramientas de dueño → **Reiniciar vínculo**. | Se saca todo lo que tenía (vuelve a nivel 1). | |

## 52. Probador y compra de accesorios (versión 1.0.42)

Los de temporada solo aparecen en su fecha: **Calabacita** del 15/10 al 2/11 y **Gorro navideño** del 1/12 al 6/1. En esta versión de prueba, si Play no tiene cargado el producto, el precio dice "$ 2 (prueba)" y Comprar es de mentira. Para ver el Gorro fuera de fecha: Herramientas de dueño → Vestidor → **Mostrar todas las temporadas**.

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 52.1 | Mascota → Accesorios (hasta el 2/11). | Está la **Calabacita** con el precio en marrón debajo del nombre. El Gorro navideño **no** aparece. | |
| 52.2 | Tocá la Calabacita. | La mascota grande se la pone; arriba "Probando: Calabacita" punteado; la ficha con borde punteado; Cabeza dice Calabacita. Abajo la barra "¿Te gusta cómo me queda?", "Solo se ve acá hasta que lo compres · a la venta hasta el 2 de noviembre", **Sacar** y **Comprar · precio**. La mascota de la pantalla **no** la tiene. | |
| 52.3 | Tocá **Sacar** (o la ficha de nuevo, o andá a Avisos y volvé). | Se la saca y vuelve lo que tenía puesto. | |
| 52.4 | Probala y tocá **Comprar**. | (Prueba) "¡Ya es tuyo! Me lo dejo puesto."; la ficha pasa a "Puesto" con tilde, y la mascota de la pantalla también la tiene. Tocarla la saca ("Comprado") y la vuelve a poner. | |
| 52.5 | Herramientas de dueño → **Mostrar todas las temporadas**. | Aparece el Gorro navideño; al probarlo dice "a la venta hasta el 6 de enero". Probalo con varias mascotas (Mascotas → otra): queda en la cabeza de cada una. | |
| 52.6 | Herramientas de dueño → **Borrar compras de prueba**. | La Calabacita vuelve a mostrar el precio y se le saca a la mascota. | |
| 52.7 | (Con los productos cargados en Play Console, X13) Comprá con una cuenta de testers de licencias. | El botón dice el precio de Play en tu moneda; se abre la pantalla de pago de Google; al terminar, se lo pone solo. En otro celular con la misma cuenta aparece comprado. | |

## 53. Accesorios con el arte de Cowork (versión 1.0.44)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 53.1 | Vestidor: poné a la mascota el gorrito de lana, la corona, el pañuelo y (en fecha o con "Mostrar todas las temporadas") la calabacita y el gorro navideño. | Se ven con el dibujo nuevo, con el borde del mismo grosor que la mascota. Nada queda cortado arriba (el pompón y la hoja de la calabacita). | |
| 53.2 | Probá con **rana, león y conejo**. | Rana: el gorro va arriba de los ojos y no se los tapa. León: sobre la melena. Conejo: entre las orejas. | |
| 53.3 | Probá con **pollito y búho** el pañuelo. | Queda justo debajo de la cara. | |
| 53.4 | Mirá a la mascota flotante cuando camina de costado, hacia los dos lados. | Usa el dibujo de perfil, centrado en la cabeza, y mira para el mismo lado que la mascota. El gorro navideño cae hacia atrás. | |
| 53.5 | Mirala cuando gira y cuando queda de espaldas. | Los gorros se ven como de frente; del pañuelo solo se ve el nudo. Al girar, el dibujo cambia de frente a perfil sin saltos raros. | |
| 53.6 | Con camita puesta, dejala echarse y dormirse. | La camita queda detrás, sin borde delante de las patas. Echada se ve lo de la cabeza; dormida se ven los gorros, pero no la corona ni la calabacita. | |
| 53.7 | Usá la app un rato con la mascota vestida. | Se mueve fluida y no aparece "MimuPet no responde". | |

## 54. Accesorios por especie (versión 1.0.45)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 54.1 | Mirá de cerca a la mascota vestida, en el Vestidor y en la pantalla. | El borde de los accesorios y de las orejas del conejo se ve liso, no serruchado. En el emulador se ve serruchado: hay que confirmarlo en el teléfono. | |
| 54.2 | Vaca y pato: poné y sacá el pañuelo, parados y caminando de costado. | Con pañuelo no se ven la campana ni el moñito; sin pañuelo vuelven. La vaca conserva las manchas. | |
| 54.3 | Vaca y pato con pañuelo: dejalos echarse. | Acostados el pañuelo no se ve, y vuelven la campana y el moñito. | |
| 54.4 | Pollito: poné cada cosa de la cabeza, parado, de costado y de espaldas. | Sin cáscara mientras tenga algo en la cabeza. Con solo el pañuelo, la cáscara sigue. Dormido con corona o calabacita, que no se ven, vuelve la cáscara. | |
| 54.5 | Rana de costado con cada cosa de la cabeza. | El gorro va chico e inclinado, detrás del ojo. | |
| 54.6 | León con gorrito de lana y calabacita. | Sobre la melena, sin cortarse arriba. Quedan un poco achatados (al 74 %) para entrar en el lienzo: decidir si gusta. | |
| 54.7 | Elegí un pelaje que no sea el original y hacela girar con pañuelo y gorro. | Los accesorios siguen a la mascota sin saltos raros, y de espaldas se ve el nudo a la altura del cuello. | |
| 54.8 | Vaca sentada girando solo la cabeza, con gorro y pañuelo. | El gorro gira con la cabeza, el pañuelo queda quieto y no aparece la campana. | |

## 55. Pañuelo coral (versión 1.0.46)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 55.1 | Poné el pañuelo a varias mascotas, sobre todo rana, chancho y pollito. | El pañuelo es coral con lunares crema, se distingue bien y combina con la mascota. | |
| 55.2 | Mirá el pañuelo de costado y de espaldas, y su cuadradito en el Vestidor. | Coral en todos lados, también el nudo y el ícono. | |
| 55.3 | Mirá la calabacita, la corona y la camita. | Siguen con sus hojas y su tela en verde. | |

## 56. Aviso en dos pasos (versión 1.0.47)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 56.1 | Esperá un recordatorio con la mascota parada. | El "guau" dura unos 2 segundos, aparece y se va suave, y la mascota no se menea. | |
| 56.2 | Seguí mirando después del "guau". | Sale un globito crema al costado, solo con el mensaje. Se puede seguir usando la app de abajo, incluso justo arriba y abajo del globito. | |
| 56.3 | Tocá el globito. | Se abre la tarjeta completa en crema, con "Listo" y "Aplazar" más chicos. | |
| 56.4 | Mirá la tarjeta de un recordatorio sin nota que llegó a tiempo. | La hora aparece una sola vez, al lado de "te avisa". La notificación de la barra muestra solo el mensaje (ver 58.1). | |
| 56.5 | Mirá un aviso atrasado o un evento que empieza en unos minutos. | Sigue el texto "Era para las..." o "Empieza a las... (en N min)", porque es otra hora. | |
| 56.6 | No toques el globito (con "Cuánto dura el globo" en un tiempo fijo). | Al terminar el tiempo se va y queda una carta pendiente a sus pies. | |
| 56.7 | Con el globito a la vista, tocá la mascota o mantené apretado el globito. | Se cierra y queda la carta pendiente. | |
| 56.8 | Mové la mascota a la izquierda, a la derecha y al medio, y probá un aviso. | El globito sale siempre del lado con lugar y entero en la pantalla. | |
| 56.9 | Con la mascota sentada, abrí un aviso. | Se queda sentada y las patitas siguen sobre la tarjeta sin verse raras. | |

## 57. Tarjeta compacta (versión 1.0.48)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 57.1 | Abrí un aviso de texto corto, como "Tomar agua". | La tarjeta es angosta, con letra más chica y sin espacio vacío a la derecha. | |
| 57.2 | Abrí un aviso de título y texto largos. | La tarjeta se ensancha hasta su máximo y el texto entra en menos líneas que antes. | |
| 57.3 | Con la mascota a la derecha, a la izquierda y al medio, abrí un aviso corto. | Las patitas quedan apoyadas sobre la tarjeta y la tarjeta entra entera en la pantalla. | |
| 57.4 | Abrí una carta pendiente y una de frase. | Se ven bien, con su sello y su color. | |

## 58. Notificación de la barra sin "Son las..." (versión 1.0.49)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 58.1 | Con la mascota oculta, esperá un recordatorio sin nota que llegue a tiempo y mirá la barra de notificaciones. | Se ve solo el mensaje, sin "Son las...". La hora queda chiquita en el encabezado, y están "Aplazar" y "Listo". | |
| 58.2 | Mirá la notificación de un recordatorio con nota. | Se ven el mensaje y la nota. | |
| 58.3 | Mirá la de un aviso atrasado y la de un evento que empieza en unos minutos. | Siguen "Era para las..." y "Empieza a las... (en N min)", porque es otra hora. | |

## 59. Carta pegada a la mascota y botones de la prueba (versión 1.0.50)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 59.1 | Con una carta pendiente, poné la mascota a la izquierda y después contra el borde derecho. | La carta queda igual de pegada de los dos lados, apoyada sobre el cuerpo. | |
| 59.2 | Repetí con la mascota en su tamaño mínimo y máximo. | La carta sigue igual de cerca y se puede tocar. | |
| 59.3 | Dejá tres cartas o más con la mascota contra el borde derecho. | La pila se abre hacia afuera, sin tapar la cara, y se ve el "+N" si hay más de tres. | |
| 59.4 | Probá una mascota Pro y mirá su tarjeta. | "Probar un aviso" se lee entero, y abajo están "Volver a la mía" y "Ver planes". | |

## 60. La mascota no se mueve con un scroll (versión 1.0.51)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 60.1 | Hacé un scroll normal en una página empezando con el dedo sobre la mascota. | Se inclina apenas y vuelve a su lugar. No abre nada ni se esconde. | |
| 60.2 | Arrastrala arrancando despacio. | Sigue al dedo y queda donde la soltás. | |
| 60.3 | Agarrala y movela rápido de entrada. | Vuelve a su lugar. Anotá si esto molesta: es el costo de este cambio. | |
| 60.4 | Mantenela apretada un momento, sin llegar a abrir la app, y movela rápido. | La arrastra. | |
| 60.5 | Hacé los cuatro tirones cortos. | Hacen lo mismo de siempre. | |
| 60.6 | Mantenela apretada sin moverla. | Abre la app. | |

## 61. Giro más lento y casco de calabaza (versión 1.0.52)

Para ver el casco fuera de temporada: Ajustes, 7 toques en la versión, "Mostrar todas las temporadas".

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 61.1 | Tocá dos veces la mascota. | Da la vuelta apenas más lento que antes (2,2 s en vez de 1,8 s), sin saltos. | |
| 61.2 | Abrí el Vestidor y mirá la calabaza nueva. | Aparece "Casco de calabaza" junto a la "Calabacita", con su ícono y su precio. | |
| 61.3 | Probate el casco. | Se ven los ojos y la boca por los agujeros, y parpadea normalmente. | |
| 61.4 | Con el casco, tocá dos veces la mascota. | De perfil tiene un solo ojo y de espaldas la calabaza está entera, sin agujeros. | |
| 61.5 | Con el casco, sentala y hacela dormir. | Sentada se ve bien; dormida el casco no aparece. | |
| 61.6 | Mirá el casco en cada una de las 11 especies. | Queda centrado y entero, sin cortarse en los bordes. | |

## 62. Casco de calabaza más angosto (versión 1.0.53)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 62.1 | Mirá el casco de frente en las 11 especies. | Se ve más angosto (86 %; perro y león 90 %), con los ojos y la boca en los agujeros. | |
| 62.2 | Tocá dos veces la mascota con el casco. | De perfil el ojo sigue en su agujero y el agujero queda dentro de la calabaza. | |
| 62.3 | Mirá el perro y el león de cerca. | Casi no asoman los costados de la cabeza ni la melena. | |

## 63. Pantalla Mascota en una sola pantalla (versión 1.0.54)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 63.1 | Abrí la pestaña Mascota. | No hay pestañas internas. Arriba está la mascota con Cabeza, Cuello y Camita a la derecha; después nombre, nivel y 10 corazones, "Lo próximo" y la tira de Mascotas. | |
| 63.2 | Tocá la línea del nombre. | Se abre Nuestro camino. | |
| 63.3 | Tocá Cabeza, Cuello y Camita. | Se abre la hoja Vestir en ese lugar y la mascota se sigue viendo arriba. Con los chips cambiás de lugar sin cerrarla. | |
| 63.4 | En la hoja, poné y sacá un accesorio, y usá "Sin nada". | El escenario y la mascota de la pantalla cambian. | |
| 63.5 | En temporada (o con "Mostrar todas las temporadas"), tocá uno a la venta. | Se lo prueba y abajo aparecen Sacar y Comprar. Cerrar la hoja o tocar otro lo saca. | |
| 63.6 | Tocá un accesorio bloqueado. | Explica: "Lo gano en el nivel N, con X corazones. Faltan N «Listo»." | |
| 63.7 | Con plan gratis, tocá una mascota Pro. | Tu mascota no cambia. El escenario muestra "Vista previa" y aparece la tarjeta con Probar 5 min y Ver Pro. Tocar la tuya vuelve. | |
| 63.8 | Tocá Probar 5 min. | Empieza la prueba en la pantalla, con su cuenta regresiva y los puntitos de pelaje. | |
| 63.9 | Con Pro o en la prueba de Pro, tocá una mascota y un puntito de pelaje. | Sin candados: cambia la mascota y su pelaje. | |
| 63.10 | En Ajustes, abrí "Cómo se ve la mascota". | Están el nombre, tamaño y transparencia (grises con PRO en plan gratis, abren los planes), modo fantasma y qué tan inquieta es. | |
| 63.11 | En Ayuda, tocá "Probar un aviso". | Llega un aviso de prueba. | |
| 63.12 | En Avisos, tocá Vestir. | Se abre la pestaña Mascota con la hoja Vestir. | |

## 64. Pantalla Mascota como la maqueta (versión 1.0.55)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 64.1 | Abrí la pestaña Mascota. | La mascota va sobre un panel rosa, sin círculo, con tres fichas cuadradas adentro a la derecha. | |
| 64.2 | Mirá las mascotas. | Son chicas y juntas, en filas una debajo de la otra, sin deslizar. | |
| 64.3 | Abrí la hoja Vestir en Cuello y en Camita. | "Sin nada" mide lo mismo que la otra ficha. La ficha del lugar abierto tiene borde cocoa. | |
| 64.4 | Tocá un accesorio a la venta. | Abajo aparecen Sacar y Comprar, uno al lado del otro. | |
| 64.5 | Con pocos corazones en el nivel actual, mirá la línea del nombre. | Hay al menos un corazón lleno. | |
| 64.6 | Tocá una mascota Pro. | Se ve con "Vista previa" arriba a la izquierda, con los botones Probar 5 min y Ver Pro. | |

## 65. Vista previa compacta (versión 1.0.56)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 65.1 | En plan gratis, tocá una mascota Pro y después la tuya. | Las mascotas de abajo no se mueven: la tarjeta ocupa lo mismo que el nivel y "Lo próximo". | |
| 65.2 | Mirá la tarjeta. | Se leen el nombre, PRO, el texto y los botones Probar 5 min y Ver Pro, sin cortes. | |
| 65.3 | Tocá Probar 5 min y Ver Pro. | Empieza la prueba y se abren los planes, respectivamente. | |

## 66. Vestir una mascota Pro y prueba compacta (versión 1.0.57)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 66.1 | En plan gratis, tocá una mascota Pro y abrí Cabeza. | Se abre la hoja Vestir sobre la vista previa. | |
| 66.2 | Tocá un accesorio a la venta. | Se lo ves puesto a la mascota Pro, con Sacar y Comprar. | |
| 66.3 | Poné y sacá uno que ya tengas y usá "Sin nada". | Cambia solo en el escenario: tu mascota real y la de la pantalla siguen vestidas igual. | |
| 66.4 | Volvé a tu mascota. | Tiene puesto lo de siempre. | |
| 66.5 | Tocá Probar 5 min. | La tarjeta ocupa lo mismo que el nivel y "Lo próximo", con la cuenta regresiva, Volver a la mía y Ver planes. | |
| 66.6 | Durante la prueba, vestila desde la hoja. | Se ve en el escenario y lo que ya tenés también en la mascota de la pantalla. | |
| 66.7 | Con una prueba activa, mirá Avisos. | La tarjeta se ve bien, sin cortes. | |

## 67. Pelajes a la vista en plan gratis (versión 1.0.58)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 67.1 | En plan gratis, mirá debajo de las mascotas. | Aparece "Pelaje" con la etiqueta PRO y puntitos de color; todos menos el primero con candado. | |
| 67.2 | Tocá un puntito con candado. | Tu mascota se ve con ese pelaje en "Vista previa", con Probar 5 min y Ver Pro. Las filas no se mueven. | |
| 67.3 | Tocá el primer puntito o tu mascota. | Vuelve a como estaba. | |
| 67.4 | Tocá una mascota Pro y uno de sus puntitos. | Se ve con ese pelaje. | |
| 67.5 | Tocá Probar 5 min con un pelaje elegido. | La prueba empieza con ese pelaje. | |
| 67.6 | Con Pro, mirá los puntitos. | No tienen candado ni etiqueta PRO, y tocarlos cambia el pelaje. | |

## 68. Nubecita, próximos avisos y Ajustes ordenados (versión 1.0.59)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 68.1 | Con una instalación nueva o sin haber elegido globo, recibí un aviso. | La onomatopeya sale en la Nubecita. | |
| 68.2 | Si ya habías elegido Caramelo, actualizá la app. | Sigue en Caramelo: tu elección se respeta. | |
| 68.3 | Con la mascota en el medio de la pantalla, hacé un tirón a la izquierda. | "Próximos avisos" sale como la tarjeta del aviso: crema, borde suave, letra de la app, íconos en vez de emojis. | |
| 68.4 | Hacelo con la mascota arriba de todo, a la izquierda y a la derecha. | El panel va al costado, con la colita hacia la mascota, y entra entero. | |
| 68.5 | Probalo sin avisos en los próximos 7 días. | Dice que no tenés avisos y mide lo justo. | |
| 68.6 | Tocá el panel. | Se cierra. | |
| 68.7 | Abrí Ajustes. | Primero el plan, después Avisos, Mascota y General. | |
| 68.8 | Entrá a Cómo te aviso. | Globo, Avisos sin interrumpir y voz, sin tener que bajar. | |
| 68.9 | Entrá a Si no respondés. | Insistir, cuánto dura el globo y repetir al desbloquear. Lo que cambies se ve en el resumen de la fila. | |
| 68.10 | Entrá a Al tocar el globo y a Frases amigables. | Mantienen sus opciones y los cambios quedan guardados. | |

## 69. Español neutro y voseo (versión 1.0.60)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 69.1 | Con el celular en Español (Argentina) o (Uruguay), recorré la app. | Todo sigue en voseo: "Tocá", "querés". | |
| 69.2 | Con el celular en Español (México) u otro país, recorré la app. | Aparece tuteo: "Toca", "quieres", "Si no respondes". | |
| 69.3 | En ese idioma, recorré la guía inicial, el tutorial, Ajustes y la pantalla Pro. | Ningún texto queda cortado ni mezcla voseo y tuteo. | |
| 69.4 | Con el celular en inglés. | Todo sigue en inglés. | |
| 69.5 | En Android 13 o más, abrí Ajustes del sistema › Apps › MimuPet › Idioma. | Ofrece Español, Español (Argentina) y Español (Uruguay), además de inglés. | |

## 70. Permisos que faltan, arriba (versión 1.0.61)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 70.1 | Con todos los permisos, abrí Ajustes. | "Permisos y batería" está en General y dice "Todo en orden". | |
| 70.2 | Quitale a la app un permiso, por ejemplo Mostrar sobre otras apps, y volvé a Ajustes. | "Permisos y batería" pasa a ser lo primero de Avisos, en amarillo, y ya no está en General. | |
| 70.3 | Devolvele el permiso y volvé. | Regresa a General. | |

## 71. Siete mascotas, ícono nuevo y Cerdito (versión 1.0.62)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 71.1 | Mirá el ícono de la app en la pantalla de inicio del celular y en la lista de apps. | Es el perrito contento con los ojos cerrados, sobre fondo crema rosado, sin cortarse en los bordes. | |
| 71.2 | Si tu celular usa íconos temáticos (Android 13 o más), activalos. | El perrito aparece en líneas, del color del tema. | |
| 71.3 | Abrí la pestaña Mascota. | Arriba van las 7 (perro, gato, pato, vaca, chancho, pollito y conejo). Al final están león, mono, rana y búho como sombras grises con "Pronto". | |
| 71.4 | Tocá una de las sombras. | Aparece "Pronto vas a poder jugar con nuevos amigos" y no cambia nada más. | |
| 71.5 | Si estabas usando león, mono, rana o búho, actualizá a esta versión. | La mascota flotante pasa a ser la gratis que habías elegido o, si no habías elegido ninguna, el perro. Si tu mascota gratis era una de esas cuatro, la app te pide elegir otra entre las 7. | |
| 71.6 | Con el celular en Español (México), mirá el nombre del chancho. | Dice "Cerdito". En Español (Argentina) sigue "Chancho". | |

## 72. Pelajes en packs (versión 1.0.63)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 72.1 | Durante la prueba de Pro, cambiá el pelaje de tu mascota. | Se cambia en el momento, sin candados. | |
| 72.2 | Herramientas de dueño → Plan gratis. Mirá los pelajes. | Todos menos el original tienen candado; ya no hay etiqueta PRO al lado de "Pelaje". | |
| 72.3 | Con Plan gratis, tocá un pelaje con candado de tu mascota. | La tarjeta dice "Pack con todos los pelajes de perro" con "Probar 5 min" y "Comprar · $ 2 (prueba)", sin PRO. | |
| 72.4 | Tocá Comprar. | Se pone ese pelaje, aparece "¡Listo! Ya tenés todos mis pelajes." y los candados de esa mascota desaparecen. Las otras mascotas siguen con candado. | |
| 72.5 | Herramientas de dueño → Borrar compras de prueba. | Esa mascota vuelve al pelaje original, con candados. | |
| 72.6 | Tocá una mascota Pro (sin Pro). | Igual que antes: PRO, "Probar 5 min" y "Ver Pro". | |
| 72.7 | Abrí Ver Pro. | En "Pelajes" dice Gratis: El original, Pro: En packs. | |

## 73. Papelito, cartita y recordatorio rápido nuevos (versión 1.0.64)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 73.1 | Esperá un recordatorio con la mascota a la derecha de la pantalla. | Primero el globo "guau", como siempre. Después, a la izquierda de la mascota, un papelito crema inclinado con la esquina de arriba doblada: la hora arriba y el aviso abajo, en una línea. La patita de la mascota se apoya en su borde. | |
| 73.2 | Lo mismo con la mascota a la izquierda. | Todo espejado: el papel a la derecha y la esquina doblada arriba a la derecha. | |
| 73.3 | Tocá el papelito. | Se despliega en la cartita: "(nombre) te avisa · hora", el título, "Listo" (marrón) y "Más tarde" (con borde). | |
| 73.4 | Tocá Listo y, en otro aviso, Más tarde. | Funcionan como antes (Más tarde = aplazar). | |
| 73.5 | En otro aviso, tocá la mascota en vez del papelito. | Se cierra y deja la carta pendiente a sus pies, como antes. Al tocar la carta, la cartita sale debajo de la mascota, sostenida con las patitas. | |
| 73.6 | Tirón hacia abajo sobre la mascota, con ella arriba de la pantalla. | Aparece la cartita "Tu perro anota" a su lado (o debajo, si no entra), con el teclado abierto. "1 hora" viene elegida; "Anotado" está apagado hasta que escribas. | |
| 73.7 | Escribí algo, elegí 10 min y tocá Anotado (o el Enter del teclado). | Se guarda y avisa "Listo: te aviso a las…". | |
| 73.8 | Repetí con la mascota en la mitad de abajo de la pantalla. | La cartita se abre arriba de la mascota, sin que la tape el teclado. | |
| 73.9 | Tocá el calendario de la cartita. | Aparecen el día (con flechas) y la hora para escribir. | |
| 73.10 | Tocá afuera de la cartita. | Se cierra sin guardar. | |

---

## 74. Pack sorpresa, globo Caramelo y adelantar el camino (versión 1.0.65)

Para probar con y sin Pro: Herramientas de dueño → plan.

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 74.1 | Pantalla Mascota, lista de mascotas. | Solo las 7 (perro, gato, pato, vaca, cerdito, pollito, conejo). Ninguna silueta ni "Pronto". | |
| 74.2 | Sin Pro: Ajustes → Cómo te aviso. | Caramelo apagado con la etiqueta PRO; tocarlo abre los planes. Nubecita elegida. | |
| 74.3 | Con Pro: elegí Caramelo y esperá un aviso. | El "guau" sale en el globo rosa. | |
| 74.4 | Con Caramelo elegido, pasá a plan gratis. | El próximo aviso sale en Nubecita, y en Ajustes figura Nubecita. Al volver a Pro, vuelve el Caramelo. | |
| 74.5 | Sin Pro: Mascota → tocá el nivel → al final de Nuestro camino. | "Adelantar todo el camino" con PRO; tocarlo abre los planes. | |
| 74.6 | Con Pro: tocá "Adelantar todo el camino". | Pregunta "¿Adelantar todo el camino?". Cancelar no cambia nada. Adelantar: nivel 8, festejo de la corona y el botón desaparece. | |
| 74.7 | Planes (Ver Pro). | Dos filas nuevas: "Globo Caramelo (el rosa)" y "Adelantar el camino de amistad", solo en Pro. | |

## 75. Solo Pro a la venta y camino rehecho (versión 1.0.66)

Reemplaza a 74.5, 74.6 y la fila «Adelantar el camino» de 74.7. Para sumar corazones: Ajustes → tocar 7 veces la versión → Herramientas de dueño → «+1 corazón» o «+10 corazones»; «Reiniciar vínculo» vuelve a cero.

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 75.1 | Pantalla Mascota, con y sin Pro (también en la prueba). | No hay fila de pelajes. Las mascotas se ven con su pelaje original. | |
| 75.2 | Planes (Ver Pro) y Herramientas de dueño → «Ver el cierre de la prueba de Pro». | Sin filas de pelajes ni de «Adelantar el camino». | |
| 75.3 | Vestir → Cabeza. | Solo gorrito y corona; ni gorro navideño ni calabazas. | |
| 75.4 | Pantalla Mascota, tarjeta del camino. | Sin niveles. Título «Lo próximo: …» o, si faltan 10 o menos, «Un poco más y (nombre) estrena …». Barra rosa, «Faltan N corazones», «¿Cómo gano corazones?» y 8 fichas: ganadas en verde, la próxima en rosa con borde, las demás apagadas. | |
| 75.5 | «Ver todo». | «Lo que (nombre) aprende contigo», los 8 premios en dos columnas con «Ya lo tiene», «Lo próximo · faltan N» o «A los N corazones», las 4 formas de ganar corazones y la frase final. | |
| 75.6 | Reiniciar el vínculo y sumar 3 corazones con la app abierta. | «¡Estreno pañuelo!» con la mascota vestida y corazones. «Ponérselo» se lo pone; «Ahora no» no. | |
| 75.7 | Con 9 corazones, salir de la app y sumar 1 (marcar «Listo» en un aviso). | Sobre la mascota flotante, un papelito «¡Aprendí a dar la patita!». «¡A ver!»: da la patita. | |
| 75.8 | Llegar a 22, 40, 95, 135 y 180. | Festejo de cada uno. La camita se ve con la mascota acostada. | |
| 75.9 | Ponerle la pelotita (Vestir → Juguete). | Queda en el piso a la derecha en la app. En la mascota flotante solo aparece cuando juega (desde la 1.0.70, ver 78). | |
| 75.10 | Ponerle la chapita (Vestir → Cuello) a cada mascota: de frente, de perfil, girando y dormida. | Cordón y medalla debajo del mentón; reemplaza al pañuelo. Acostada no se ve. | |
| 75.11 | Con el gorrito sin ganar y nada en la cabeza. | El gorrito se ve transparente sobre la mascota, con «¿para mí?». Desaparece al ganarlo. | |
| 75.12 | Primer «Listo» del día. | Suma 2 corazones (los siguientes, 1). | |
| 75.13 | Abrir la app un día nuevo. | Suma 1 corazón (una sola vez por día). | |
| 75.14 | Actualizar desde la 1.0.65 con corazones. | Conserva los corazones. Si gana premios nuevos de una vez (por ejemplo, pelotita y chapita), un solo festejo «¡Ya nos conocíamos!» que los nombra. | |

## 76. Regalo de Navidad (versión 1.0.67)

Para probarlo antes del 1/12: Herramientas de dueño → «Recibir el gorro navideño» («Sacar el regalo» lo deja como antes).

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 76.1 | Abrir la app antes del 1/12 (sin tocar las herramientas). | No hay regalo; el gorro navideño no está en Vestir. | |
| 76.2 | «Recibir el gorro navideño». | Festejo con la mascota con el gorro y corazones: «¡Mirá lo que me trajeron!», «Ahora no» y «Ponérselo». | |
| 76.3 | «Ahora no» y Vestir → Cabeza. | El gorro navideño figura como «Regalo», sin precio ni candado. Tocarlo se lo pone. | |
| 76.4 | Con el gorro puesto, salir de la app. | La mascota flotante lo lleva puesto. | |
| 76.5 | Del 1/12 al 6/1 (o cambiando la fecha del celular), abrir la app. | Llega el regalo sola, una sola vez. Después del 6/1 lo sigue teniendo. | |
| 76.6 | Plan gratis y con Pro. | Igual en los dos. | |
| 76.7 | (1.0.68) «Sacar el regalo», «Recibir el gorro navideño» y enseguida salir al inicio sin que la app lo festeje. | En unos segundos, papelito sobre la mascota: «¡Me trajeron un regalo!», «Abrí la app para verlo», «¡A ver!» y «Después». Una sola vez. | |
| 76.8 | Tocar «¡A ver!». | Se abre la app y festeja el gorro. | |

## 77. Aviso: mascota asomada y sin manito (versión 1.0.69)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 77.1 | Ayuda → «Probar un aviso» y salir al inicio. | Globo con el sonido y después el papelito al costado, **sin la manito** en el borde. | |
| 77.2 | Tocar el papelito, con la mascota arriba o en el medio de la pantalla. | La cartita se abre **debajo de la mascota**, que se asoma por arriba con las patitas sobre el borde. | |
| 77.3 | Lo mismo con la mascota bien abajo. | Si debajo no entra, la cartita va al costado (sin manito). | |

---
## 78. La pelotita aparece solo cuando juega (versión 1.0.70)

| # | Prueba | Qué tiene que pasar | Resultado |
|---|---|---|---|
| 78.1 | Con la pelotita puesta (Vestir → Juguete), salir al inicio. | La mascota flotante **no** tiene la pelotita al lado. En la app (Vestir) sí se ve, para saber que la tiene puesta. | |
| 78.2 | Esperar con la mascota en pantalla (cada tanto juega; puede tardar varios minutos). | La pelotita llega rebotando desde la derecha y la mascota la mira. Cuando le llega a los pies, la mascota da un saltito, la pelotita sale rebotando y desaparece, y la mascota mueve la cola. | |
| 78.3 | Lo mismo con la mascota pegada al borde derecho. | La pelotita entra y sale por el borde sin cortes raros. | |

---

## Si algo falla

Anotá el número de prueba, qué pasó y a qué hora. Si podés, sacá una captura de pantalla.
