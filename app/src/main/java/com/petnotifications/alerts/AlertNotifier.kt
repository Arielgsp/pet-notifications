package com.petnotifications.alerts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.petnotifications.MainActivity
import com.petnotifications.Prefs
import com.petnotifications.R
import com.petnotifications.overlay.PetAlert
import com.petnotifications.overlay.PetOverlayService

/**
 * Notificación de cada aviso, con botones "Hecho" y "Posponer". Es el respaldo cuando la mascota
 * no está en pantalla (oculta, sin permiso, pantalla apagada). Si la mascota sí lo va a mostrar,
 * se publica silenciosa: el sonido lo pone la mascota y la notificación queda como registro.
 */
object AlertNotifier {
    const val CHANNEL_LOUD = "alerts"
    private const val CHANNEL_QUIET = "alerts_quiet"

    fun ensureChannels(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_LOUD, ctx.getString(R.string.channel_alerts), NotificationManager.IMPORTANCE_HIGH),
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_QUIET, ctx.getString(R.string.channel_alerts_quiet), NotificationManager.IMPORTANCE_LOW),
        )
    }

    /** Las repeticiones de "insistir" y los pospuestos reemplazan la notificación del aviso original. */
    fun notificationId(alert: PetAlert) = Insist.baseKey(alert.key).hashCode()

    fun post(ctx: Context, alert: PetAlert, loudRequested: Boolean) {
        // Con los avisos en silencio (tirón a la derecha) todo va por el canal sin sonido.
        val loud = loudRequested && !Prefs(ctx).muted
        ensureChannels(ctx)
        val id = notificationId(alert)
        val open = alert.eventId?.let { eventId ->
            // Evento de calendario: tocar la notificación abre el evento en la app de calendario.
            Intent(Intent.ACTION_VIEW, ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .also { i -> alert.eventBegin?.let { i.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, it) } }
        } ?: Intent(ctx, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .also { i -> alert.reminderId?.let { i.putExtra(PetOverlayService.EXTRA_REMINDER_ID, it) } }
            .also { i -> if (MissedSummary.isSummary(alert)) i.putExtra(PetOverlayService.EXTRA_SHOW_MISSED, true) }
        val n = NotificationCompat.Builder(ctx, if (loud) CHANNEL_LOUD else CHANNEL_QUIET)
            .setSmallIcon(R.drawable.ic_stat_paw)
            .setContentTitle(alert.title)
            .setContentText(alert.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(alert.body))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            // En la pantalla de bloqueo se ve completa salvo que el usuario oculte el contenido
            // sensible; en ese caso aparece esta versión sin el texto del recordatorio.
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(
                NotificationCompat.Builder(ctx, if (loud) CHANNEL_LOUD else CHANNEL_QUIET)
                    .setSmallIcon(R.drawable.ic_stat_paw)
                    .setContentTitle(ctx.getString(R.string.notif_public_title, Prefs(ctx).pet.subject(ctx)))
                    .setContentText(ctx.getString(R.string.notif_public_text))
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .build(),
            )
            .setPriority(if (loud) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            // Grupo propio por aviso: si no, con dos o más notificaciones sueltas Android las agrupa
            // solo y las nuevas llegan calladas (comprobado en Android 16). Cada aviso tiene que sonar.
            .setGroup("alert:${alert.key}")
            // El aviso entero, para que la mascota lo repita al desbloquear si nadie lo atendió.
            .addExtras(alert.toBundle())
            .setContentIntent(PendingIntent.getActivity(ctx, id, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            .apply {
                // Los mensajes de la app (fin de la prueba) no tienen nada que marcar como hecho.
                if (alert.source == PetAlert.Source.LOCAL || alert.source == PetAlert.Source.CALENDAR) {
                    addAction(0, ctx.getString(R.string.action_snooze), action(ctx, AlertReceiver.ACTION_SNOOZE, alert, id))
                    addAction(0, ctx.getString(R.string.action_done), action(ctx, AlertReceiver.ACTION_DONE, alert, id))
                    // Descartarla (deslizar) cuenta como respuesta: no se insiste más.
                    setDeleteIntent(action(ctx, AlertReceiver.ACTION_DISMISSED, alert, id))
                }
            }
            .build()
        // Sin permiso de notificaciones (Android 13+) no se puede publicar; la mascota avisa igual si está.
        if (NotificationManagerCompat.from(ctx).areNotificationsEnabled()) {
            runCatching { NotificationManagerCompat.from(ctx).notify(id, n) }
        }
    }

    fun cancel(ctx: Context, alert: PetAlert) {
        NotificationManagerCompat.from(ctx).cancel(notificationId(alert))
    }

    private fun action(ctx: Context, action: String, alert: PetAlert, notificationId: Int): PendingIntent {
        val intent = Intent(ctx, AlertReceiver::class.java)
            .setAction(action)
            .putExtras(alert.toBundle())
            .putExtra(AlertReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        // Un requestCode distinto por aviso y por botón, para que no se pisen los PendingIntent.
        return PendingIntent.getBroadcast(
            ctx, notificationId * 31 + action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
