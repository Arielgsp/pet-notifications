# Pet Notifications — contexto para rediseñar la interfaz de la app

**Para:** Cowork (qué priorizar) y Claude Design (cómo se ve) · **Fecha:** 05/10/2026 · **Versión de la app:** 1.0.14 (en preparación)

Este documento describe la app tal como funciona hoy, para rediseñar **la pantalla que se abre al mantener apretada la mascota** (que es la misma que se abre desde el ícono de la app). No hace falta haber usado la app para entenderlo.

---

## 1. Qué es la app

Una mascota tierna (perro, gato, pato, rana, vaca, chancho, pollito, león, mono o búho) vive **flotando sobre la pantalla del celular, encima de cualquier app**. Cuando llega la hora de un recordatorio o de un evento del calendario, la mascota lo avisa: primero dice su onomatopeya ("Guau!") y después muestra un globo con el aviso y dos botones, **Aplazar** y **Listo**.

- **Para quién:** gente que ignora las notificaciones comunes y necesita algo más amable y visible. El tono es tierno, nunca de alarma ni de culpa.
- **Promesa de privacidad:** la app no tiene acceso a internet; los datos nunca salen del celular (salvo la copia de seguridad de Android en la cuenta del propio usuario).
- **Idiomas:** español e inglés.
- **Modelo:** gratis con límites + **Pro** de pago único (prueba gratis de 7 días en la prueba cerrada y 14 en la versión publicada).

## 2. Cómo vive la mascota en pantalla (fuera de la app)

Esto **no** es parte del rediseño, pero explica qué controla el usuario desde la app.

| Gesto sobre la mascota | Qué hace |
|---|---|
| Arrastrar | La mueve; queda donde la dejás (también al girar el celular) |
| 1 toque | Salta y tira corazones |
| 2 toques | Da una vuelta |
| 3 toques | Libre por ahora |
| Mantener apretado | **Abre la app** (la pantalla a rediseñar) |
| Tirón rápido ← | Panel flotante con los próximos avisos (7 días) |
| Tirón rápido ↓ | Tarjeta flotante de recordatorio rápido (texto + "En 30 min / 1 h / 3 h / Mañana 9:00 / Elegir día y hora") |
| Tirón rápido ↑ | Se esconde 1 hora |
| Tirón rápido → | Avisos en silencio (sin sonido ni voz) hasta otro tirón |
| Tocarla con un aviso abierto | Cierra el aviso |

- **Sueño:** se acuesta a dormir a los 10 minutos sin uso del celular y se levanta al prender la pantalla o tocarla. En el horario de **No molestar** duerme siempre y solo se despierta para avisar.
- **Se esconde sola** en llamadas y en videos o juegos a pantalla completa.
- **Si la mascota no se ve** (pantalla apagada, sin permiso), el aviso llega como notificación común con los mismos botones.
- **Insistir:** si no respondés un aviso, vuelve a avisar cada X minutos.

## 3. Cómo se llega a la pantalla de la app

1. **Mantener apretada la mascota** (lo más común una vez configurada).
2. El ícono de la app.
3. Tocar una notificación de un recordatorio (abre el detalle de ese recordatorio).
4. La primera vez: la **configuración guiada** (ver sección 6) y después esta pantalla.

## 4. Inventario completo de la pantalla actual

Hoy es **una sola pantalla larga con todo**, en este orden. Este es el problema a resolver: es mucha información junta, y lo primero que se ve ("Probar un aviso") le quita protagonismo a lo importante (los recordatorios).

**1. Tarjeta de estado (arriba de todo)**
- Dibujo de la mascota + "Luna está en pantalla" / "en pausa" / "Mascota oculta", y "Te avisa con un Guau!".
- Interruptor para prender o apagar la mascota.
- Si está pausada: "Pausada hasta las 15:30" + botón "Mostrar ahora".
- Si los avisos están en silencio: "🔕 Avisos en silencio" + "Activar sonido".
- Plan: "Prueba de Pro: te quedan 5 días" / "Plan gratis" / "Tenés Pro" + "Ver planes".
- Botón **"Probar un aviso"**.
- Texto con la ayuda de todos los gestos.

**2. Permisos**
- Estado (✔ o ⚠ + botón "Permitir") de: mostrar sobre otras apps, notificaciones, alarmas exactas, calendario (opcional), batería sin restricciones.
- Guía de batería según la marca del celular (Samsung, Motorola, Xiaomi u otra) con botones "Abrir ajustes de la app" y "Ver guía en la web".
- "Aviso de Android 'se muestra sobre otras apps'" + botón "Ocultar".
- "Ver la configuración guiada de nuevo".

**3. Elegí tu mascota**
- Grilla con las 10 mascotas (en plan gratis, 8 con etiqueta PRO).
- "Cambiar mis 2 mascotas gratis" (un solo cambio permitido).
- "Ponerle un nombre" / "Cambiar el nombre (Luna)".
- Tamaño (deslizador, Pro) y opacidad (deslizador, Pro).
- Modo fantasma (interruptor): los toques la atraviesan.
- Esconderse en pantalla completa (interruptor).

**4. No molestar**
- Explicación del sueño.
- Horario de No molestar (interruptor + "Desde 22:00" / "Hasta 07:00", Pro).

**5. Al tocar el globo**
- Qué hace tocar el globo del aviso: Abrir / Borrar / Aplazar / Cerrar.
- Si es Aplazar: cuántos minutos (5, 10, 15, 30, 60).
- Insistir si no respondés: cada cuántos minutos (No / 5 / 10 / 15 / 30) y cuántas veces (1 / 3 / 5). Configurable solo con Pro; gratis = cada 10 min, 3 veces.

**6. Cuánto dura el globo**
- Se oculta solo (deslizador de segundos) o queda fijo.
- Al desbloquear: repetir los avisos que sonaron con la pantalla apagada (No / el último / los últimos 3 / todos).
- Decir la onomatopeya en voz alta (Pro) + "Hablar aunque esté en silencio".

**7. Google Calendar / Gmail** (Pro)
- Avisar eventos del calendario (interruptor).
- Avisarme antes: al empezar / 5 / 10 / 15 / 30 / 60 min.
- Lista de calendarios del celular con casillas.

**8. Mis recordatorios**
- Lista: título, cuándo ("Hoy · 10:30", "Mañana · 8:00", "Vie 2 oct · 9:00"), si se repite, "✓ avisado"; tacho para borrar (con "Deshacer", o "¿Saltear solo el próximo o borrar todos?" si se repite).
- Botón flotante **"+ Recordatorio"** abajo a la derecha → ventana con: "¿Qué te recuerdo?", nota opcional, fecha, hora, repetición (una vez / todos los días / todas las semanas).

**9. Pie**
- "Enviar comentarios" (abre el correo con un resumen técnico), "Diagnóstico" (avisos entregados, demoras, permisos, cierres inesperados) y el número de versión.

**Ventanas que se abren desde esta pantalla:** planes Gratis/Pro (tabla + Comprar Pro + Restaurar compras), elegir 2 mascotas gratis (al terminar la prueba), nombre de la mascota, diagnóstico, enviar comentarios, lista de "mientras el celular estuvo apagado", detalle de un recordatorio.

## 5. Gratis vs. Pro (hoy)

| | Gratis | Pro |
|---|---|---|
| Mascotas | 2 a elección | Las 10 |
| Recordatorios (una vez, diarios, semanales) | Sí | Sí |
| Globo, mimos, sueño, gestos | Sí | Sí |
| Modo fantasma | Sí | Sí |
| Insistir | Cada 10 min, hasta 3 veces | A tu gusto |
| Google Calendar | No | Sí |
| Voz en voz alta | No | Sí |
| Horario de No molestar | No | Sí |
| Tamaño y transparencia | No | Sí |

Las opciones Pro se ven con una etiqueta "PRO" y al tocarlas abren los planes.

## 6. Configuración guiada (primera vez)

Pantallas donde la mascota habla en primera persona y explica cada permiso **antes** de pedirlo; cada una dice qué pasa si el usuario dice que no:
1. Bienvenida + elegir mascota + ponerle nombre.
2. Mostrar sobre otras apps.
3. Notificaciones.
4. Alarmas exactas.
5. Calendario (opcional).
6. Batería (con la guía de la marca).
7. Ocultar el aviso fijo de Android.
8. Listo.

Se saltean los pasos ya resueltos. **No es parte de este rediseño**, pero conviene que el estilo sea coherente.

## 7. Qué usa el usuario y con qué frecuencia (estimación, a validar con Cowork)

| Frecuencia | Qué |
|---|---|
| Todos los días | Ver y crear recordatorios; ver qué viene; pausar o silenciar |
| De vez en cuando | Cambiar de mascota o nombre; ajustar cuánto insiste; calendario |
| Una vez | Permisos, batería, aviso de Android, configuración de voz y globo |
| Casi nunca | Diagnóstico, enviar comentarios, versión |

## 8. Límites que el diseño tiene que respetar

- **Android:** la app abre en el celular como cualquier otra (pantalla completa). Lo que flota sobre otras apps (mascota, globo, paneles) no es parte de este rediseño.
- **Permisos:** algunos solo se dan en los ajustes de Android (la app abre esa pantalla y espera); conviene que el estado de permisos sea visible pero no protagonista una vez resuelto.
- **Sin internet:** nada de cuentas, inicio de sesión ni contenido descargado.
- **Accesibilidad:** textos de al menos 12 px, contraste 4.5:1, áreas táctiles de 48 dp, funcionamiento con TalkBack.
- **Idiomas:** todos los textos se traducen (español e inglés; portugués más adelante): evitar textos dentro de imágenes.
- **Estilo ya definido por Design:** colores crema (`tokens.json`: surface, paper, ink, cocoa, fur, blush…), fuente Nunito, esquinas redondeadas, sombras suaves; tono tierno, voseo, verbos cortos ("Listo", "Aplazar").
- **Pro:** la pantalla tiene que mostrar qué es Pro sin sentirse como una vidriera de ventas.

## 9. Preguntas para Cowork (antes de Design)

1. ¿Qué tiene que ver primero el usuario al abrir la app: sus recordatorios, lo próximo que viene, su mascota o el estado (en pantalla / en pausa / en silencio)?
2. ¿Conviene separar en pestañas (por ejemplo Recordatorios · Mascota · Ajustes) o una pantalla principal simple con los ajustes en otra pantalla?
3. ¿Dónde va "Probar un aviso"? ¿Sigue siendo útil después de la primera vez?
4. ¿Cómo y dónde mostrar Pro (prueba, plan gratis, comprar) sin presionar?
5. ¿Los permisos van en la pantalla principal solo cuando falta alguno?
6. ¿La ayuda de gestos va en la pantalla, en un "¿Cómo se usa?" aparte, o se enseña de a poco?
7. ¿Debería existir un historial de avisos (lo que ya sonó, lo que marcaste Listo) para alimentar rachas en el futuro (v1.1)?

## 10. Qué le pediríamos a Design (después de Cowork)

- Estructura de navegación y pantalla principal, en tema claro y oscuro.
- Pantallas de ajustes agrupadas según la decisión de Cowork.
- Estados: primera vez sin recordatorios, con permisos faltantes, en pausa, en silencio, plan gratis vs. Pro, prueba por terminar.
- Ventana de crear/editar recordatorio y el detalle de un recordatorio.
- Mantener las 10 mascotas con sus dibujos actuales (están en `docs/mascotas/`).
