package com.petnotifications.alerts

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.petnotifications.Prefs
import com.petnotifications.calendar.CalendarSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Avisos de eventos del calendario con alarmas, como los recordatorios propios: no dependen de
 * que la mascota esté en pantalla ni de la pantalla prendida.
 *
 * Una sola alarma apunta al próximo aviso; al sonar, [sync] entrega lo que venció y programa el
 * siguiente. Si el usuario agrega o cambia un evento, [CalendarChangeJob] vuelve a sincronizar
 * aunque la app esté cerrada.
 */
object CalendarAlarms {
    /** Si el teléfono estuvo apagado o dormido, avisar igual lo que venció hasta este tiempo atrás. */
    private const val CATCH_UP_MS = 12 * 3_600_000L
    /** Hasta dónde mirar para programar el próximo aviso; más allá, se revisa de nuevo en un día. */
    private const val LOOKAHEAD_MS = 7 * 24 * 3_600_000L
    private const val RECHECK_MS = 24 * 3_600_000L
    private const val REQUEST_CODE = -42
    private const val JOB_ID = 4242

    private val lock = Mutex()

    /** [rearmJob] = false solo desde el propio [CalendarChangeJob], que se vuelve a armar al terminar. */
    suspend fun sync(ctx: Context, rearmJob: Boolean = true) = lock.withLock {
        val prefs = Prefs(ctx)
        if (!prefs.calendarEnabled || !CalendarSource.hasPermission(ctx)) {
            cancel(ctx)
            return@withLock
        }
        val now = System.currentTimeMillis()
        val last = prefs.lastCalendarCheck.takeIf { it > 0 } ?: now
        val lead = prefs.calendarLeadMinutes
        val ids = prefs.calendarIds

        // 1) Lo que venció desde la última revisión (incluye lo que cayó con el teléfono dormido).
        //    Los que ya terminaron (celular apagado) no se avisan uno por uno: van en un solo resumen.
        val (ended, current) = CalendarSource.alertsBetween(ctx, last.coerceAtLeast(now - CATCH_UP_MS), now, lead, ids, now)
            .partition { (it.eventEnd ?: it.eventBegin ?: now) <= now }
        current.forEach { AlertReceiver.deliver(ctx, it, scheduledAt = (it.eventBegin ?: now) - lead * 60_000L) }
        MissedSummary.postEvents(ctx, ended)
        prefs.lastCalendarCheck = now

        // 2) Alarma para el próximo aviso.
        val next = CalendarSource.nextAlertTime(ctx, now, now + LOOKAHEAD_MS, lead, ids)
        AlertScheduler.setAlarm(ctx, next ?: (now + RECHECK_MS), pendingIntent(ctx))
        if (rearmJob) CalendarChangeJob.schedule(ctx)
    }

    /** Sincroniza en segundo plano (desde receptores y pantallas que no esperan el resultado). */
    fun syncAsync(ctx: Context, rearmJob: Boolean = true, done: () -> Unit = {}) {
        val app = ctx.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                sync(app, rearmJob)
            } finally {
                done()
            }
        }
    }

    private fun cancel(ctx: Context) {
        ctx.getSystemService(AlarmManager::class.java).cancel(pendingIntent(ctx))
        ctx.getSystemService(JobScheduler::class.java).cancel(JOB_ID)
    }

    private fun pendingIntent(ctx: Context): PendingIntent = PendingIntent.getBroadcast(
        ctx, REQUEST_CODE,
        Intent(ctx, AlertReceiver::class.java).setAction(AlertReceiver.ACTION_CALENDAR),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    /** Se dispara cuando cambia cualquier cosa del calendario del teléfono (sin mantener la app abierta). */
    class CalendarChangeJob : JobService() {
        override fun onStartJob(params: JobParameters): Boolean {
            syncAsync(this, rearmJob = false) {
                jobFinished(params, false)
                if (Prefs(this).calendarEnabled) schedule(this)
            }
            return true
        }

        override fun onStopJob(params: JobParameters) = true

        companion object {
            /** Un trabajo con disparador de contenido sirve para un solo cambio: se vuelve a armar en cada sync. */
            fun schedule(ctx: Context) {
                val job = JobInfo.Builder(JOB_ID, ComponentName(ctx, CalendarChangeJob::class.java))
                    .addTriggerContentUri(
                        JobInfo.TriggerContentUri(
                            CalendarContract.CONTENT_URI,
                            JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS,
                        ),
                    )
                    .setTriggerContentUpdateDelay(5_000)
                    .setTriggerContentMaxDelay(60_000)
                    .build()
                runCatching { ctx.getSystemService(JobScheduler::class.java).schedule(job) }
            }
        }
    }
}
