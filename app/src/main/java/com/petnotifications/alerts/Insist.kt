package com.petnotifications.alerts

import android.content.Context
import com.petnotifications.Prefs
import com.petnotifications.data.AppDatabase
import com.petnotifications.data.Completion
import com.petnotifications.overlay.PetAlert

/**
 * "Insistir hasta Hecho": si el usuario no responde un aviso (Hecho, Posponer, tocar o cerrar el
 * globo, descartar la notificación), la mascota vuelve a avisar cada [Prefs.insistMinutes] minutos,
 * hasta [Prefs.insistMax] veces. Cada repetición es un aviso pospuesto guardado en la base (sobrevive
 * a reinicios) cuya clave lleva la marca [MARK] y el número de repetición.
 */
object Insist {
    private const val MARK = "|insist|"

    /** Clave del aviso original, sin marcas de repetición ni de posponer. */
    fun baseKey(key: String) = key.substringBefore(MARK).substringBefore(":snooze:")

    private fun count(key: String) = key.substringAfter(MARK, "0").substringBefore(":").toIntOrNull() ?: 0

    /** Programa la próxima repetición de [alert], si corresponde. */
    suspend fun scheduleNext(ctx: Context, alert: PetAlert) {
        if (alert.source == PetAlert.Source.TEST || alert.source == PetAlert.Source.APP) return
        val prefs = Prefs(ctx)
        val minutes = prefs.insistMinutes
        val n = count(alert.key)
        if (minutes <= 0 || n >= prefs.insistMax) return
        cancel(ctx, alert.key) // nunca más de una repetición pendiente por aviso
        AlertScheduler.snooze(ctx, alert.copy(key = baseKey(alert.key) + MARK + (n + 1)), minutes)
    }

    /** El usuario respondió: se cancelan las repeticiones pendientes de ese aviso. */
    suspend fun cancel(ctx: Context, key: String) {
        val prefix = baseKey(key) + MARK
        val dao = AppDatabase.get(ctx).snoozes()
        dao.all().filter { it.alertKey.startsWith(prefix) }.forEach {
            AlertScheduler.cancelSnooze(ctx, it.id)
            dao.delete(it.id)
        }
    }

    /** Cancela las repeticiones de todos los avisos de un recordatorio (por ejemplo, al abrirlo). */
    suspend fun cancelForReminder(ctx: Context, reminderId: Long) {
        val dao = AppDatabase.get(ctx).snoozes()
        dao.forReminder(reminderId).filter { MARK in it.alertKey }.forEach {
            AlertScheduler.cancelSnooze(ctx, it.id)
            dao.delete(it.id)
        }
    }

    /**
     * "Listo": se cancelan las repeticiones y queda anotado. Suma un corazón al vínculo la primera vez
     * que se marca ese aviso (volver a marcarlo no suma). Devuelve true si sumó.
     */
    suspend fun done(ctx: Context, alert: PetAlert): Boolean {
        cancel(ctx, alert.key)
        // Antes de anotar: si es la primera vez con vínculo, se cuentan los "Listo" anteriores.
        com.petnotifications.Bond.seedIfNeeded(ctx)
        val key = baseKey(alert.key)
        return runCatching {
            val dao = AppDatabase.get(ctx).completions()
            val first = dao.countForKey(key) == 0
            dao.insert(
                Completion(
                    alertKey = key, source = alert.source.name,
                    reminderId = alert.reminderId, eventId = alert.eventId, doneAt = System.currentTimeMillis(),
                ),
            )
            first && com.petnotifications.Bond.counts(alert) && com.petnotifications.Bond.addListo(ctx)
        }.getOrDefault(false)
    }
}
