# Pet Notifications 🐶

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
| 1 toque | Da un saltito y pone ojitos felices |
| 2 toques | Suelta un ❤️ |
| 3 toques | Se da la vuelta despacito (frente, perfil, espalda), meneándose |
| 1 toque dormida | La despierta |
| Mantener apretado | Abre la app |

## Configuración
- **Al tocar el globo:** Abrir · Borrar · Posponer (5–60 min) · Cerrar. Mantener apretado el globo siempre lo cierra.
- **Duración del globo:** se oculta solo a los X segundos (3–60, con barra de cuenta regresiva) o queda fijo hasta tocarlo.
- **Mascota y tamaño** (48–120 dp), **voz** on/off.
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
  recibe toques mientras se muestra el globo. Cada 15 s revisa recordatorios vencidos y eventos del calendario.
- `data/` — Room (`Reminder`). `calendar/CalendarSource` — `CalendarContract.Instances`.
- `BootReceiver` — vuelve a mostrar la mascota tras reiniciar el teléfono.
- Dibujos: `res/drawable/pet_<mascota>_<vista>.xml` (frente, parpadeo, feliz, perfil, espalda, echada,
  dormida) y `PetArt.kt` se generan con `python tools/pet_art/export_android.py` desde los mismos
  diseños del lienzo; no se editan a mano.

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
