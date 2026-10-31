package com.petnotifications.alerts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.petnotifications.Prefs
import com.petnotifications.R
import com.petnotifications.Repeat
import com.petnotifications.data.AppDatabase
import com.petnotifications.data.Delivery
import com.petnotifications.data.Reminder
import com.petnotifications.data.nextOccurrence
import com.petnotifications.overlay.PetAlert
import com.petnotifications.overlay.PetOverlayService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

/** Recibe las alarmas de los recordatorios y los botones "Hecho" / "Posponer" de la notificación. */
class AlertReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val ctx = context.applicationContext
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_FIRE -> onFire(ctx, intent)
                    ACTION_SNOOZE_FIRE -> onSnoozeFire(ctx, intent)
                    ACTION_DONE -> {
                        onDone(ctx, intent)
                        intent.extras?.toPetAlert()?.let {
                            // Con la mascota a la vista, el "+1" sale de ella aunque se tocó en la notificación.
                            if (Insist.done(ctx, it) && PetOverlayService.canShowAlert(ctx)) {
                                PetOverlayService.plusOne(ctx)
                            }
                        }
                    }
                    ACTION_DISMISSED -> intent.extras?.toPetAlert()?.let { Insist.cancel(ctx, it.key) }
                    ACTION_SNOOZE -> onSnooze(ctx, intent)
                    ACTION_CALENDAR -> CalendarAlarms.sync(ctx)
                    ACTION_TRIAL_NOTICE -> showTrialNotice(ctx)
                    ACTION_PET_WATCHDOG -> {
                        val prefs = Prefs(ctx)
                        if (prefs.petEnabled && !prefs.petPaused() && Settings.canDrawOverlays(ctx) && !PetOverlayService.running) {
                            PetOverlayService.start(ctx, startedBy = PetOverlayService.STARTED_BY_WATCHDOG)
                        }
                        AlertScheduler.schedulePetWatchdog(ctx)
                    }
                    ACTION_RESUME_PET -> {
                        val prefs = Prefs(ctx)
                        prefs.petPausedUntil = 0L
                        if (prefs.petEnabled && Settings.canDrawOverlays(ctx)) PetOverlayService.start(ctx)
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun onFire(ctx: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        val time = intent.getLongExtra(EXTRA_TIME, 0L)
        val dao = AppDatabase.get(ctx).reminders()
        val r = dao.get(id) ?: return
        // Alarma vieja (el recordatorio se editó o ya se avisó): la nueva ya está programada.
        if (r.fired || r.timeMillis != time) return
        val now = System.currentTimeMillis()
        // Primero se avisa y recién después se marca como avisado: si algo se corta en el medio,
        // lo peor es un aviso repetido, nunca uno perdido.
        deliver(ctx, r.toAlert(ctx, now), scheduledAt = r.timeMillis)
        val repeat = Repeat.from(r.repeat)
        val next = if (repeat == Repeat.NONE) r.copy(fired = true)
        else r.copy(timeMillis = nextOccurrence(r.timeMillis, repeat, now))
        dao.update(next)
        AlertScheduler.schedule(ctx, next)
    }

    private suspend fun onSnoozeFire(ctx: Context, intent: Intent) {
        val dao = AppDatabase.get(ctx).snoozes()
        val s = dao.get(intent.getLongExtra(EXTRA_SNOOZE_ID, -1L)) ?: return
        deliver(
            ctx,
            PetAlert(
                key = "${s.alertKey}:snooze:${s.id}",
                title = s.title,
                body = s.body,
                source = PetAlert.Source.entries.firstOrNull { it.name == s.source } ?: PetAlert.Source.LOCAL,
                reminderId = s.reminderId,
                eventId = s.eventId,
                eventBegin = s.eventBegin,
                repeating = s.repeating,
            ),
            scheduledAt = s.atMillis,
        )
        dao.delete(s.id)
    }

    /**
     * Un día antes de que termine la prueba de Pro, la mascota lo avisa una sola vez, aclarando qué
     * se conserva gratis. Sin culpa ni presión: es información.
     */
    private fun showTrialNotice(ctx: Context) {
        val prefs = Prefs(ctx)
        if (prefs.proPurchased || !prefs.trialActive() || prefs.trialNoticeShown) return
        prefs.trialNoticeShown = true
        val alert = PetAlert(
            key = "app:trial_notice",
            title = ctx.getString(R.string.trial_notice_title),
            body = ctx.getString(R.string.trial_notice_body),
            source = PetAlert.Source.APP,
        )
        val petShows = PetOverlayService.canShowAlert(ctx)
        AlertNotifier.post(ctx, alert, loudRequested = !petShows)
        if (petShows) PetOverlayService.show(ctx, alert)
    }

    private fun onDone(ctx: Context, intent: Intent) {
        NotificationManagerCompat.from(ctx).cancel(intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0))
        intent.extras?.toPetAlert()?.let { if (PetOverlayService.running) PetOverlayService.dismiss(ctx, it.key) }
    }

    private suspend fun onSnooze(ctx: Context, intent: Intent) {
        onDone(ctx, intent)
        val alert = intent.extras?.toPetAlert() ?: return
        Insist.cancel(ctx, alert.key)
        snoozeAlert(ctx, alert, Prefs(ctx).snoozeMinutes)
    }

    companion object {
        const val ACTION_FIRE = "com.petnotifications.alert.FIRE"
        const val ACTION_SNOOZE_FIRE = "com.petnotifications.alert.SNOOZE_FIRE"
        const val ACTION_DONE = "com.petnotifications.alert.DONE"
        const val ACTION_SNOOZE = "com.petnotifications.alert.SNOOZE"
        const val ACTION_DISMISSED = "com.petnotifications.alert.DISMISSED"
        const val ACTION_CALENDAR = "com.petnotifications.alert.CALENDAR"
        const val ACTION_RESUME_PET = "com.petnotifications.alert.RESUME_PET"
        const val ACTION_TRIAL_NOTICE = "com.petnotifications.alert.TRIAL_NOTICE"
        const val ACTION_PET_WATCHDOG = "com.petnotifications.alert.PET_WATCHDOG"
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_TIME = "time"
        const val EXTRA_SNOOZE_ID = "snooze_id"
        const val EXTRA_NOTIFICATION_ID = "notification_id"

        /**
         * La mascota lo muestra si está en pantalla y la pantalla prendida; si no, suena la notificación.
         * Queda anotado en el registro de entregas ([scheduledAt] = cuándo tenía que sonar).
         */
        suspend fun deliver(ctx: Context, alert: PetAlert, scheduledAt: Long) {
            val petShows = PetOverlayService.canShowAlert(ctx)
            if (isQuiet(ctx, alert)) {
                // Sin interrumpir: notificación callada, no insiste, y la mascota (si está, aunque el
                // celular esté bloqueado) deja una carta amarilla a sus pies.
                AlertNotifier.post(ctx, alert, loudRequested = false)
                if (PetOverlayService.running) PetOverlayService.show(ctx, alert, quiet = true)
            } else {
                AlertNotifier.post(ctx, alert, loudRequested = !petShows)
                if (petShows) PetOverlayService.show(ctx, alert)
                Insist.scheduleNext(ctx, alert)
            }
            val now = System.currentTimeMillis()
            runCatching {
                val log = AppDatabase.get(ctx).deliveries()
                log.insert(
                    Delivery(
                        alertKey = alert.key, source = alert.source.name, scheduledAt = scheduledAt,
                        deliveredAt = now, byPet = petShows, exactAlarms = AlertScheduler.canScheduleExact(ctx),
                    ),
                )
                log.deleteOlderThan(now - 90 * 24 * 3_600_000L)
            }
        }

        /** Va "sin interrumpir": todos los avisos (Ajustes) o ese recordatorio en particular. */
        private suspend fun isQuiet(ctx: Context, alert: PetAlert): Boolean {
            if (alert.source != PetAlert.Source.LOCAL && alert.source != PetAlert.Source.CALENDAR) return false
            if (Prefs(ctx).quietAlerts) return true
            val id = alert.reminderId ?: return false
            return AppDatabase.get(ctx).reminders().get(id)?.quiet == true
        }

        /**
         * Posponer que sobrevive a que se cierre la app o se reinicie el teléfono: un recordatorio de
         * una vez cambia de hora; uno que se repite o un evento del calendario queda guardado aparte.
         */
        suspend fun snoozeAlert(ctx: Context, alert: PetAlert, minutes: Int) {
            val id = alert.reminderId
            if (alert.source == PetAlert.Source.LOCAL && !alert.repeating && id != null) {
                val dao = AppDatabase.get(ctx).reminders()
                val r = dao.get(id) ?: return
                val moved = r.copy(timeMillis = System.currentTimeMillis() + minutes * 60_000L, fired = false)
                dao.update(moved)
                AlertScheduler.schedule(ctx, moved)
            } else {
                AlertScheduler.snooze(ctx, alert.copy(key = Insist.baseKey(alert.key)), minutes)
            }
        }
    }
}

/** El aviso que muestran la mascota y la notificación para un recordatorio propio. */
fun Reminder.toAlert(ctx: Context, now: Long, keySuffix: String? = null): PetAlert {
    val time = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(timeMillis))
    val late = now - timeMillis > 5 * 60_000
    return PetAlert(
        key = "local:$id:$timeMillis" + (keySuffix?.let { ":$it" } ?: ""),
        title = title,
        body = note.ifBlank { ctx.getString(if (late) R.string.alert_late else R.string.alert_now, time) },
        source = PetAlert.Source.LOCAL,
        reminderId = id,
        repeating = repeat != Repeat.NONE.name,
    )
}
