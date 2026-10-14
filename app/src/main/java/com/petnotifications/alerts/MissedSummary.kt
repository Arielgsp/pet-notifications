package com.petnotifications.alerts

import android.content.Context
import com.petnotifications.Prefs
import com.petnotifications.R
import com.petnotifications.overlay.PetAlert
import com.petnotifications.overlay.PetOverlayService
import java.text.DateFormat
import java.util.Date

/**
 * Lo que se perdió con el celular apagado, en un solo aviso en vez de una catarata (decisión de
 * Cowork, 28/09): los eventos del calendario que ya terminaron y, si son más de 2, los recordatorios
 * propios atrasados. Tocar el aviso abre la app con la lista (se guarda en [Prefs.missedList]).
 */
object MissedSummary {
    const val KEY_PREFIX = "app:missed"
    /** Más de esta cantidad de recordatorios atrasados se agrupan en un resumen. */
    const val MAX_SEPARATE_REMINDERS = 2

    fun isSummary(alert: PetAlert) = alert.key.startsWith(KEY_PREFIX)

    /** Eventos que terminaron mientras el celular estuvo apagado. */
    fun postEvents(ctx: Context, events: List<PetAlert>) {
        if (events.isEmpty()) return
        val title = if (events.size == 1) ctx.getString(R.string.missed_events_one)
        else ctx.getString(R.string.missed_events_many, events.size)
        val lines = events.map { "📅 ${it.title} · ${format(it.eventBegin ?: 0L)}" }
        post(ctx, "events", title, lines)
    }

    /** Recordatorios propios atrasados (solo se llama con más de [MAX_SEPARATE_REMINDERS]). */
    fun postReminders(ctx: Context, reminders: List<Pair<String, Long>>) {
        if (reminders.isEmpty()) return
        val title = ctx.getString(R.string.missed_reminders_many, reminders.size)
        val lines = reminders.map { (title, at) -> "⏰ $title · ${format(at)}" }
        post(ctx, "reminders", title, lines)
    }

    private fun post(ctx: Context, kind: String, title: String, lines: List<String>) {
        val prefs = Prefs(ctx)
        prefs.missedList = (prefs.missedList + lines).takeLast(MAX_LINES)
        val alert = PetAlert(
            key = "$KEY_PREFIX:$kind",
            title = title,
            body = ctx.getString(R.string.missed_tap_to_see),
            source = PetAlert.Source.APP,
        )
        val petShows = PetOverlayService.canShowAlert(ctx)
        AlertNotifier.post(ctx, alert, loudRequested = !petShows)
        if (petShows) PetOverlayService.show(ctx, alert)
    }

    private fun format(millis: Long): String =
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(millis))

    private const val MAX_LINES = 50
}
