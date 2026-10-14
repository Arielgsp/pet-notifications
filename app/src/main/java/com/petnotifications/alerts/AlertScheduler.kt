package com.petnotifications.alerts

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.petnotifications.Prefs
import com.petnotifications.Repeat
import com.petnotifications.data.nextOccurrence
import com.petnotifications.data.AppDatabase
import com.petnotifications.data.Reminder
import com.petnotifications.data.Snooze
import com.petnotifications.overlay.PetAlert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Motor de avisos: cada recordatorio tiene su propia alarma en [AlarmManager], así llega a
 * horario aunque el celular esté en reposo (Doze) o el servicio de la mascota esté apagado.
 * La mascota solo se ocupa de mostrar el aviso cuando está en pantalla.
 */
object AlertScheduler {

    /** Android 12+ puede negar las alarmas exactas; sin ellas el aviso puede atrasarse unos minutos. */
    fun canScheduleExact(ctx: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarms(ctx).canScheduleExactAlarms()

    fun schedule(ctx: Context, r: Reminder) {
        if (r.fired) {
            // Solo su alarma: sus pospuestos y repeticiones de "insistir" siguen vigentes.
            alarms(ctx).cancel(fireIntent(ctx, r.id, 0L, snooze = false))
            return
        }
        setAlarm(ctx, r.timeMillis, fireIntent(ctx, r.id, r.timeMillis, snooze = false))
    }

    /**
     * Vuelve a avisar [alert] en [minutes]. Queda guardado en la base, así sobrevive a que se cierre
     * la app o se reinicie el teléfono. Para recordatorios que se repiten y eventos del calendario
     * (un recordatorio de una vez, en cambio, se mueve de hora: ver [AlertReceiver.snoozeAlert]).
     */
    suspend fun snooze(ctx: Context, alert: PetAlert, minutes: Int) {
        val at = System.currentTimeMillis() + minutes * 60_000L
        val row = Snooze(
            atMillis = at, alertKey = alert.key, title = alert.title, body = alert.body,
            source = alert.source.name, reminderId = alert.reminderId, eventId = alert.eventId,
            eventBegin = alert.eventBegin, repeating = alert.repeating,
        )
        val id = AppDatabase.get(ctx).snoozes().insert(row)
        scheduleSnooze(ctx, row.copy(id = id))
    }

    /** Borra la alarma del recordatorio y también sus avisos pospuestos (por ejemplo, al borrarlo). */
    fun cancel(ctx: Context, reminderId: Long) {
        alarms(ctx).cancel(fireIntent(ctx, reminderId, 0L, snooze = false))
        val app = ctx.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            val dao = AppDatabase.get(app).snoozes()
            dao.forReminder(reminderId).forEach {
                alarms(app).cancel(fireIntent(app, it.id, 0L, snooze = true))
                dao.delete(it.id)
            }
        }
    }

    internal fun cancelSnooze(ctx: Context, snoozeId: Long) =
        alarms(ctx).cancel(fireIntent(ctx, snoozeId, 0L, snooze = true))

    private fun scheduleSnooze(ctx: Context, s: Snooze) =
        setAlarm(ctx, s.atMillis, fireIntent(ctx, s.id, s.atMillis, snooze = true))

    /**
     * Reprograma todo lo pendiente. Se llama al arrancar el teléfono, al actualizar la app, al
     * cambiar la hora o la zona horaria, al cambiar el permiso de alarmas exactas y al abrir la app.
     * Los que vencieron con el teléfono apagado suenan enseguida.
     */
    suspend fun rescheduleAll(ctx: Context) {
        val dao = AppDatabase.get(ctx).reminders()
        val now = System.currentTimeMillis()
        val pending = dao.pending()
        // Más de 2 atrasados (por ejemplo, el celular estuvo apagado): un solo aviso resumen en vez de
        // uno por cada uno. Cada uno queda como avisado (o pasa a su próxima repetición).
        val overdue = pending.filter { it.timeMillis < now - OVERDUE_MS }
        if (overdue.size > MissedSummary.MAX_SEPARATE_REMINDERS) {
            overdue.forEach { r ->
                val repeat = Repeat.from(r.repeat)
                val next = if (repeat == Repeat.NONE) r.copy(fired = true)
                else r.copy(timeMillis = nextOccurrence(r.timeMillis, repeat, now))
                dao.update(next)
                schedule(ctx, next)
            }
            MissedSummary.postReminders(ctx, overdue.map { it.title to it.timeMillis })
            (pending - overdue.toSet()).forEach { schedule(ctx, it) }
        } else {
            pending.forEach { schedule(ctx, it) }
        }
        AppDatabase.get(ctx).snoozes().all().forEach { scheduleSnooze(ctx, it) }
        CalendarAlarms.sync(ctx)
        scheduleTrialNotice(ctx)
        schedulePetWatchdog(ctx)
    }

    /**
     * Aviso de "mañana termina tu prueba de Pro": el día anterior, a la misma hora en que se abrió la
     * app por primera vez, pero nunca de noche (si cae antes de las 9 o después de las 21, a las 10).
     */
    /** Un recordatorio que venció hace más que esto cuenta como atrasado. */
    private const val OVERDUE_MS = 60_000L

    fun scheduleTrialNotice(ctx: Context) {
        val prefs = Prefs(ctx)
        val pi = trialNoticeIntent(ctx)
        if (prefs.proPurchased || prefs.trialNoticeShown || prefs.firstOpenAt <= 0) {
            alarms(ctx).cancel(pi)
            return
        }
        val zone = java.time.ZoneId.systemDefault()
        var at = java.time.Instant.ofEpochMilli(prefs.trialEndsAt - Prefs.DAY_MS).atZone(zone)
        if (at.hour < 9 || at.hour >= 21) at = at.withHour(10).withMinute(0).withSecond(0)
        val millis = at.toInstant().toEpochMilli()
        if (millis <= System.currentTimeMillis() || millis >= prefs.trialEndsAt) return
        setAlarm(ctx, millis, pi)
    }

    private fun trialNoticeIntent(ctx: Context): PendingIntent = PendingIntent.getBroadcast(
        ctx, -44, Intent(ctx, AlertReceiver::class.java).setAction(AlertReceiver.ACTION_TRIAL_NOTICE),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    /**
     * Vigilante de la mascota: algunas marcas (Xiaomi, por ejemplo al terminar de cargar) cierran la app
     * a la fuerza y Android no la reinicia. Cada 15 minutos, si la mascota tendría que estar en pantalla
     * y no está, se la vuelve a mostrar (una alarma exacta permite arrancarla desde segundo plano).
     */
    fun schedulePetWatchdog(ctx: Context) {
        val pi = watchdogIntent(ctx)
        if (!Prefs(ctx).petEnabled) {
            alarms(ctx).cancel(pi)
            return
        }
        setAlarm(ctx, System.currentTimeMillis() + WATCHDOG_MS, pi)
    }

    private fun watchdogIntent(ctx: Context): PendingIntent = PendingIntent.getBroadcast(
        ctx, -45, Intent(ctx, AlertReceiver::class.java).setAction(AlertReceiver.ACTION_PET_WATCHDOG),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private const val WATCHDOG_MS = 15 * 60_000L

    /** Vuelve a mostrar la mascota al terminar "Pausar 1 hora" (una alarma exacta permite arrancarla). */
    fun schedulePetResume(ctx: Context, at: Long) = setAlarm(ctx, at, resumeIntent(ctx))

    fun cancelPetResume(ctx: Context) = alarms(ctx).cancel(resumeIntent(ctx))

    private fun resumeIntent(ctx: Context): PendingIntent = PendingIntent.getBroadcast(
        ctx, -43, Intent(ctx, AlertReceiver::class.java).setAction(AlertReceiver.ACTION_RESUME_PET),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    internal fun setAlarm(ctx: Context, at: Long, pi: PendingIntent) {
        val am = alarms(ctx)
        if (canScheduleExact(ctx)) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        } else {
            // Sin permiso de alarmas exactas: el sistema puede demorarlo un poco, pero llega igual.
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        }
    }

    /**
     * [id] es el del recordatorio (alarma normal) o el del pospuesto (alarma de "posponer"); la acción
     * distinta hace que no se pisen aunque coincidan los números.
     */
    private fun fireIntent(ctx: Context, id: Long, time: Long, snooze: Boolean): PendingIntent {
        val intent = Intent(ctx, AlertReceiver::class.java)
            .setAction(if (snooze) AlertReceiver.ACTION_SNOOZE_FIRE else AlertReceiver.ACTION_FIRE)
            .putExtra(if (snooze) AlertReceiver.EXTRA_SNOOZE_ID else AlertReceiver.EXTRA_REMINDER_ID, id)
            .putExtra(AlertReceiver.EXTRA_TIME, time)
        return PendingIntent.getBroadcast(
            ctx, id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun alarms(ctx: Context) = ctx.getSystemService(AlarmManager::class.java)
}
