# MimuPet 🐶

App Android donde una mascota (perro, gato, pato, rana, vaca, chancho, pollito, león, mono o búho)
vive **siempre en pantalla**, encima de cualquier app, y te avisa de tus recordatorios y eventos.

## Cómo avisa
1. La mascota se sacude y le sale de la boca una **onomatopeya** estilo cómic ("¡Guau!", "¡Miau!", "¡Cuac!"…),
   opcionalmente dicha en voz alta.
2. Aparece un **globo de texto de cómic** con el aviso, con la colita apuntando a la mascota.

## Interacción con la mascota
| Gesto | Resultado |
|---|---|
| Arrastrar | La movés a cualquier lugar (recuerda la posición) |
| 1 toque | Da un saltito y suelta corazones |
| 2 toques | Se da la vuelta despacito (frente, perfil, espalda), meneándose; desde que aprendió la patita, alterna con darla |
| 3 toques | Libre por ahora |
| 1 toque dormida | La despierta |
| Mantener apretado | Abre la app |

## Primera vez
Una **configuración guiada por la mascota** explica cada permiso antes de pedirlo (mostrar sobre otras
apps, notificaciones, alarmas exactas, calendario opcional y batería) y qué pasa si se dice que no.
Todo es opcional: sin el permiso flotante la app avisa con notificaciones comunes. Se puede volver a
ver desde Ajustes › Permisos.

## Configuración
- **Hecho / Posponer** en el globo y en la notificación. "Hecho" festeja (salto, ojitos felices y
  corazón) y queda anotado en el teléfono (tabla `completions`, para rachas). Nunca se escribe en el
  calendario del usuario.
- **Insistir si no respondés:** cada 5–30 min (o nunca), hasta 1, 3 o 5 veces. Cada repetición es un
  aviso pospuesto guardado (`alerts/Insist.kt`) y reemplaza la notificación anterior. Responder de
  cualquier forma (Hecho, Posponer, tocar o cerrar el globo, descartar la notificación, abrir el
  recordatorio) corta la insistencia.
- **Al tocar el globo:** Abrir · Borrar · Posponer (5–60 min) · Cerrar. Mantener apretado el globo siempre lo cierra.
- **Duración del globo:** se oculta solo a los X segundos (3–60, con barra de cuenta regresiva) o queda fijo hasta tocarlo.
- **Mascota y tamaño** (48–120 dp), **voz** on/off. La voz no suena con el teléfono en silencio,
  vibración o No Molestar (salvo "Hablar aunque esté en silencio"), usa el volumen de notificaciones
  y una voz descargada que no necesita internet.
- **Al desbloquear:** cuántos avisos sin atender repite la mascota (ninguno, el último, los últimos 3 o todos).
- **Modo fantasma:** la mascota queda semitransparente y los toques la atraviesan (desde Android 12
  hace falta opacidad ≤ 0,8 para que el sistema deje pasar los toques). Se activa desde Ajustes, la
  notificación fija o el botón "Mascota fantasma" de los ajustes rápidos (`GhostTileService`).
- **Llamadas:** mientras suena una llamada o hay una en curso (también WhatsApp, Meet, etc.) la
  mascota se esconde y no habla; los avisos van por notificación y se repiten al cortar. Se detecta
  con el modo de audio del sistema, sin permiso de teléfono.
- **Pantalla completa:** si otra app oculta la barra de estado (video, juego, cámara) la mascota se
  esconde (desactivable). Se detecta con una ventana "sonda" transparente de 1 px que recibe la
  visibilidad de las barras. El teclado de otra app **no** se puede detectar sin Accesibilidad.
- **Opacidad** de la mascota (40–100 %).
- **Pausar 1 hora** (desde la notificación fija): esconde la mascota y la vuelve a mostrar sola; los
  avisos siguen llegando por notificación. **Ocultar mascota** la apaga hasta volver a prenderla.
- **Batería:** en Ajustes › Permisos, pasos para que el sistema no cierre la app según la marca
  (Samsung, Motorola, Xiaomi/Redmi/POCO o genérico), con accesos a los ajustes de la app y a la
  guía de dontkillmyapp.com (se abre en el navegador; la app no usa internet).
- **Hora de dormir:** se echa y se duerme tras 1, 2 o 4 h sin avisos ni mimos, y/o en tu horario de sueño
  (por ejemplo 22:00 a 07:00). Un aviso nuevo o un toque la despiertan.
- **Recordatorios propios:** título, nota, fecha, hora y repetición (una vez / diario / semanal).
- **Google Calendar / Gmail:** lee los calendarios sincronizados en el teléfono (sin OAuth ni API keys).
  Elegís qué calendarios y con cuánta anticipación (al empezar, 5, 10, 15, 30 o 60 min). Los eventos de
  todo el día se ignoran. "Borrar" sobre un evento de Google solo descarta el aviso; nunca modifica tu calendario.

## Arquitectura
- Kotlin + Jetpack Compose, `minSdk 26`, `targetSdk 36`.
- `overlay/PetOverlayService` — servicio en primer plano (`specialUse`) con dos ventanas
  `TYPE_APPLICATION_OVERLAY`: la mascota (táctil) y la de efectos (sonido, globo, corazones), que solo
  recibe toques mientras se muestra el globo.
- `alerts/` — motor de avisos con `AlarmManager` (exacta si hay permiso), así llegan a horario con la
  pantalla apagada o la mascota oculta. Cada recordatorio propio tiene su alarma; los eventos del
  calendario comparten una que apunta al próximo aviso, y un `JobService` la reprograma cuando cambia
  un evento. Lo que venció con el teléfono dormido (hasta 12 h) se avisa igual. Al sonar publica una
  notificación con "Hecho" y "Posponer" (con sonido si la mascota no está en pantalla, silenciosa si
  la mascota lo dice). Se reprograma al reiniciar, actualizar la app o cambiar la hora. Lo pospuesto
  se guarda en la base (tabla `snoozes`) y sobrevive a reinicios. Si un aviso sonó con la pantalla
  apagada, la mascota lo repite al desbloquear mientras su notificación siga sin atender.
- `data/` — Room: `Reminder`, `Snooze` (avisos pospuestos) y `Delivery` (registro local de cada
  aviso entregado, últimos 90 días, para el diagnóstico; no sale del teléfono). `calendar/CalendarSource` — `CalendarContract.Instances`.
- `BootReceiver` — vuelve a mostrar la mascota tras reiniciar el teléfono.
- Dibujos: `res/drawable/pet_<mascota>_<vista>.xml` (frente, parpadeo, feliz, perfil, espalda, echada,
  dormida) y `PetArt.kt` se generan con `python tools/pet_art/export_android.py` desde los mismos
  diseños del lienzo; no se editan a mano.

## Gratis y Pro
Un solo pago (`pro_lifetime`, Google Play Billing). Los primeros días desde la primera apertura
son Pro de prueba: 14 en la versión `production` y 7 en `closedTest` (sabores en `app/build.gradle.kts`). Gratis: 2 mascotas a elección, recordatorios, globo, mimos,
sueño por inactividad e "insistir" cada 10 min hasta 3 veces. Pro: las 10 mascotas, Google Calendar,
insistir a gusto, voz, horario de sueño, tamaño y opacidad (el modo fantasma es gratis). El límite se aplica en
los getters de `Prefs` (lo elegido queda guardado y vuelve al comprar). `pro/ProBilling` consulta la
compra a Play en cada apertura (sirve también de "Restaurar compras") y la guarda en el teléfono.
"Enviar comentarios" (`ui/Feedback.kt`) abre la app de correo con un resumen técnico que el usuario
ve antes de mandar. "Diagnóstico" (`ui/Diagnostics.kt`) muestra, calculado en el teléfono, los avisos
programados, los entregados en los últimos 30 días (con mascota o solo notificación, demora promedio y
máxima, cuántos tarde) y el estado de los permisos; usa el registro `deliveries`.

## Idiomas
Inglés por defecto (`res/values/strings.xml`) y español (`res/values-es/strings.xml`); ningún texto
visible está escrito en el código. Cada mascota tiene nombre y onomatopeya por idioma ("Woof!" /
"¡Guau!"), y la voz habla en el idioma de los textos (`tts_language`), con una voz instalada en el
teléfono. En Android 13+ se puede elegir el idioma solo para la app (`res/xml/locales_config.xml`).
Para sumar portugués: `values-pt/strings.xml` + `<locale android:name="pt" />` en `locales_config.xml`.
El nombre visible de la app sale solo de `app_name` en `values/strings.xml`.

## Versiones y firma
Dos sabores: `closedTest` (prueba cerrada) y `production` (publicación), los dos con 7 días de prueba de Pro. A
producción se sube una compilación `production` (no promover la de la prueba cerrada). La versión
final usa R8. La llave de subida se lee de `keystore.properties` (fuera del repositorio); sin ese
archivo, la versión final se firma con la llave de prueba y Google Play la rechaza. Subir
`appVersionCode` (gradle.properties) en cada entrega.

## Privacidad
Texto oficial para la ficha y la política de privacidad (acordado con Cowork, 28/09): "La app no tiene acceso a internet: tus datos nunca llegan a nosotros. Si tenés activada la copia de seguridad de Android, tus recordatorios se guardan en tu propia cuenta de Google." (`allowBackup` sigue activado a propósito; al restaurar, `RestoreCheck` reprograma las alarmas.) `app/build.gradle.kts` tiene la tarea
`verifyNoNetworkPermissions`, que hace fallar la compilación de release si alguna librería agrega
`INTERNET` o `ACCESS_NETWORK_STATE` al manifiesto final. La librería de compras de Google Play trae esos dos permisos
(para mandarle a Google reportes de la propia librería); se quitan en el manifiesto con
`tools:node="remove"` y la librería lo tolera (solo anota una advertencia). La compra la hace la app
Play Store.

## Pruebas en el celular
Lo que falta probar en un teléfono real está en [docs/PRUEBAS.md](docs/PRUEBAS.md), por cambio.

## Compilar
Abrir la carpeta en Android Studio y ejecutar, o:
```
gradlew assembleDebug
```
APK: `app/build/outputs/apk/debug/app-debug.apk`.

## Limitaciones
- **Solo Android.** iOS no permite que una app dibuje encima de otras, así que una mascota "siempre en pantalla" no es posible ahí.
- Algunos fabricantes (Xiaomi, Huawei, Samsung) cierran servicios en segundo plano: conviene tocar
  "Batería sin restricciones" en la app.
- Los emojis se ven según la fuente del teléfono. Para una mascota con arte propio se pueden reemplazar
  por sprites o animaciones Lottie en `PetViews.kt`.
