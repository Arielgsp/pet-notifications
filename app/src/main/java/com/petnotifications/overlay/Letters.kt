package com.petnotifications.overlay

import android.content.Context
import com.petnotifications.Prefs
import com.petnotifications.R
import com.petnotifications.alerts.Insist
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import kotlin.random.Random

/**
 * Cartas que la mascota deja a sus pies:
 * - [RED]: el aviso se cerró tocando la mascota, sin "Listo" ni "Más tarde" (queda sin atender);
 * - [YELLOW]: aviso "sin interrumpir": llegó callado, sin aparecer en pantalla;
 * - [SKY]: frase amigable de la mascota ("tomá agua", "éxito en tu jornada").
 */
enum class LetterColor { RED, YELLOW, SKY }

data class Letter(val id: Long, val color: LetterColor, val alert: PetAlert, val at: Long)

/** Las cartas se guardan en las preferencias: siguen ahí aunque Android cierre la mascota. */
object LetterStore {
    /** Tope de cartas guardadas (a la vista hay como mucho 3 sobres y un número). */
    private const val MAX = 20

    fun load(prefs: Prefs): List<Letter> = runCatching {
        val arr = JSONArray(prefs.letters)
        (0 until arr.length()).mapNotNull { i -> arr.getJSONObject(i).toLetter() }
    }.getOrDefault(emptyList())

    fun save(prefs: Prefs, letters: List<Letter>) {
        val arr = JSONArray()
        letters.takeLast(MAX).forEach { arr.put(it.toJson()) }
        prefs.letters = arr.toString()
    }

    /** Las cartas del mismo aviso (sin contar repeticiones ni posposiciones). */
    fun sameAlert(a: PetAlert, b: PetAlert) = Insist.baseKey(a.key) == Insist.baseKey(b.key)

    private fun Letter.toJson() = JSONObject().apply {
        put("id", id)
        put("color", color.name)
        put("at", at)
        put("key", alert.key)
        put("title", alert.title)
        put("body", alert.body)
        put("source", alert.source.name)
        alert.reminderId?.let { put("reminderId", it) }
        alert.eventId?.let { put("eventId", it) }
        alert.eventBegin?.let { put("eventBegin", it) }
        put("repeating", alert.repeating)
    }

    private fun JSONObject.toLetter(): Letter? {
        val color = LetterColor.entries.firstOrNull { it.name == optString("color") } ?: return null
        return Letter(
            id = getLong("id"),
            color = color,
            at = getLong("at"),
            alert = PetAlert(
                key = getString("key"),
                title = optString("title"),
                body = optString("body"),
                source = PetAlert.Source.entries.firstOrNull { it.name == optString("source") } ?: PetAlert.Source.LOCAL,
                reminderId = if (has("reminderId")) getLong("reminderId") else null,
                eventId = if (has("eventId")) getLong("eventId") else null,
                eventBegin = if (has("eventBegin")) getLong("eventBegin") else null,
                repeating = optBoolean("repeating"),
            ),
        )
    }
}

/**
 * Frases amigables (carta celeste). Solo de [START_MIN] a [END_MIN], fuera del horario de No molestar,
 * repartidas en el día según "Frases amigables" (Pocas: 1, Normal: 3, Muchas: 6 por día).
 */
object FriendlyPhrases {
    private const val START_MIN = 9 * 60
    private const val END_MIN = 21 * 60

    fun perDay(level: Int) = when (level) {
        Prefs.FRIENDLY_FEW -> 1
        Prefs.FRIENDLY_NORMAL -> 3
        Prefs.FRIENDLY_MANY -> 6
        else -> 0
    }

    /**
     * Se llama una vez por minuto. Decide al azar si toca una frase ahora, de modo que las del día
     * queden repartidas: la probabilidad es lo que falta dar sobre los minutos que quedan.
     */
    fun due(prefs: Prefs, now: Long): Boolean {
        val n = perDay(prefs.friendlyLevel)
        if (n == 0) return false
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        val minute = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        if (minute < START_MIN || minute >= END_MIN) return false
        val day = cal.get(Calendar.YEAR) * 1000L + cal.get(Calendar.DAY_OF_YEAR)
        if (prefs.friendlyDay != day) {
            prefs.friendlyDay = day
            prefs.friendlyCount = 0
        }
        val left = n - prefs.friendlyCount
        if (left <= 0) return false
        // Nunca dos seguidas: por lo menos la mitad del intervalo parejo entre una y otra.
        val gap = (END_MIN - START_MIN) * 60_000L / n / 2
        if (now - prefs.friendlyAt < gap) return false
        return Random.nextDouble() < left.toDouble() / (END_MIN - minute)
    }

    fun markShown(prefs: Prefs, now: Long) {
        prefs.friendlyAt = now
        prefs.friendlyCount = prefs.friendlyCount + 1
    }

    /** Una frase al azar, distinta de la última. */
    fun pick(ctx: Context, prefs: Prefs): String {
        val all = ctx.resources.getStringArray(R.array.friendly_phrases)
        val options = all.indices.filter { it != prefs.friendlyLast }.ifEmpty { all.indices.toList() }
        val i = options.random()
        prefs.friendlyLast = i
        return all[i]
    }
}
