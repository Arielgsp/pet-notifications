package com.petnotifications.calendar

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.petnotifications.R
import com.petnotifications.overlay.PetAlert
import java.text.DateFormat
import java.util.Date

data class DeviceCalendar(val id: Long, val name: String, val account: String, val color: Int)

/**
 * Lee los calendarios sincronizados en el teléfono. Una cuenta de Gmail agregada en Android
 * sincroniza su Google Calendar acá, así que no hace falta OAuth ni claves de API.
 */
object CalendarSource {

    fun hasPermission(ctx: Context) =
        ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED

    fun calendars(ctx: Context): List<DeviceCalendar> {
        if (!hasPermission(ctx)) return emptyList()
        val out = mutableListOf<DeviceCalendar>()
        ctx.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.ACCOUNT_NAME,
                CalendarContract.Calendars.CALENDAR_COLOR,
            ),
            "${CalendarContract.Calendars.VISIBLE} = 1", null,
            "${CalendarContract.Calendars.ACCOUNT_NAME}, ${CalendarContract.Calendars.CALENDAR_DISPLAY_NAME}",
        )?.use { c ->
            while (c.moveToNext()) {
                out += DeviceCalendar(c.getLong(0), c.getString(1) ?: ctx.getString(R.string.cal_no_name), c.getString(2) ?: "", c.getInt(3))
            }
        }
        return out
    }

    /**
     * Eventos cuyo momento de aviso (inicio − [leadMinutes]) cae en (fromExclusive, toInclusive].
     * Se ignoran los eventos de todo el día.
     */
    fun alertsBetween(
        ctx: Context,
        fromExclusive: Long,
        toInclusive: Long,
        leadMinutes: Int,
        calendarIds: Set<Long>,
        now: Long = toInclusive,
    ): List<PetAlert> {
        if (!hasPermission(ctx) || toInclusive <= fromExclusive) return emptyList()
        val lead = leadMinutes * 60_000L
        val beginMin = fromExclusive + lead
        val beginMax = toInclusive + lead
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().also {
            ContentUris.appendId(it, beginMin)
            ContentUris.appendId(it, beginMax + 1)
        }.build()
        val out = mutableListOf<PetAlert>()
        val timeFmt = DateFormat.getTimeInstance(DateFormat.SHORT)
        ctx.contentResolver.query(
            uri,
            arrayOf(
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.ALL_DAY,
                CalendarContract.Instances.CALENDAR_ID,
                CalendarContract.Instances.EVENT_LOCATION,
                CalendarContract.Instances.END,
            ),
            null, null, CalendarContract.Instances.BEGIN,
        )?.use { c ->
            while (c.moveToNext()) {
                val begin = c.getLong(2)
                if (begin <= beginMin || begin > beginMax) continue
                if (c.getInt(3) == 1) continue
                if (calendarIds.isNotEmpty() && c.getLong(4) !in calendarIds) continue
                val eventId = c.getLong(0)
                val location = c.getString(5).orEmpty()
                val at = timeFmt.format(Date(begin))
                val whenText = when {
                    // Aviso atrasado (teléfono apagado o dormido): decir la verdad sobre la hora.
                    begin < now - 60_000 -> ctx.getString(R.string.cal_started, at)
                    leadMinutes == 0 || begin <= now + 60_000 -> ctx.getString(R.string.cal_now, at)
                    else -> ctx.getString(R.string.cal_in, at, ((begin - now + 30_000) / 60_000).toInt())
                }
                out += PetAlert(
                    key = "cal:$eventId:$begin",
                    title = c.getString(1)?.takeIf { it.isNotBlank() } ?: ctx.getString(R.string.cal_untitled),
                    body = if (location.isBlank()) whenText else "$whenText\n📍 $location",
                    source = PetAlert.Source.CALENDAR,
                    eventId = eventId,
                    eventBegin = begin,
                    eventEnd = c.getLong(6),
                )
            }
        }
        return out
    }

    /** Momento del próximo aviso (inicio − [leadMinutes]) posterior a [fromExclusive], hasta [toInclusive]. */
    fun nextAlertTime(
        ctx: Context,
        fromExclusive: Long,
        toInclusive: Long,
        leadMinutes: Int,
        calendarIds: Set<Long>,
    ): Long? {
        if (!hasPermission(ctx)) return null
        val lead = leadMinutes * 60_000L
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().also {
            ContentUris.appendId(it, fromExclusive + lead)
            ContentUris.appendId(it, toInclusive + lead + 1)
        }.build()
        ctx.contentResolver.query(
            uri,
            arrayOf(CalendarContract.Instances.BEGIN, CalendarContract.Instances.ALL_DAY, CalendarContract.Instances.CALENDAR_ID),
            null, null, CalendarContract.Instances.BEGIN,
        )?.use { c ->
            while (c.moveToNext()) {
                val alertAt = c.getLong(0) - lead
                if (alertAt <= fromExclusive || c.getInt(1) == 1) continue
                if (calendarIds.isNotEmpty() && c.getLong(2) !in calendarIds) continue
                return alertAt
            }
        }
        return null
    }
}
