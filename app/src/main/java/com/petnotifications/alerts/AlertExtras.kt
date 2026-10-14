package com.petnotifications.alerts

import android.os.Bundle
import com.petnotifications.overlay.PetAlert

/**
 * Un aviso viaja entero entre la alarma, la notificación, sus botones y la mascota. Se guarda en
 * un Bundle (extras de Intent o de la notificación) con estas claves.
 */
private const val KEY = "alert_key"
private const val TITLE = "alert_title"
private const val BODY = "alert_body"
private const val SOURCE = "alert_source"
private const val REMINDER_ID = "alert_reminder_id"
private const val EVENT_ID = "alert_event_id"
private const val EVENT_BEGIN = "alert_event_begin"
private const val REPEATING = "alert_repeating"

fun PetAlert.toBundle() = Bundle().apply {
    putString(KEY, key)
    putString(TITLE, title)
    putString(BODY, body)
    putString(SOURCE, source.name)
    reminderId?.let { putLong(REMINDER_ID, it) }
    eventId?.let { putLong(EVENT_ID, it) }
    eventBegin?.let { putLong(EVENT_BEGIN, it) }
    putBoolean(REPEATING, repeating)
}

fun Bundle.toPetAlert(): PetAlert? {
    val key = getString(KEY) ?: return null
    return PetAlert(
        key = key,
        title = getString(TITLE).orEmpty(),
        body = getString(BODY).orEmpty(),
        source = PetAlert.Source.entries.firstOrNull { it.name == getString(SOURCE) } ?: PetAlert.Source.LOCAL,
        reminderId = getLong(REMINDER_ID, -1L).takeIf { it >= 0 },
        eventId = getLong(EVENT_ID, -1L).takeIf { it >= 0 },
        eventBegin = getLong(EVENT_BEGIN, -1L).takeIf { it >= 0 },
        repeating = getBoolean(REPEATING, false),
    )
}
