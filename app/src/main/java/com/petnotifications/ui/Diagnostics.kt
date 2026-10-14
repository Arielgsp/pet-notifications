package com.petnotifications.ui

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.petnotifications.Prefs
import com.petnotifications.R
import com.petnotifications.alerts.AlertScheduler
import com.petnotifications.calendar.CalendarSource
import com.petnotifications.data.AppDatabase
import com.petnotifications.overlay.PetOverlayService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Números del diagnóstico local (tarea 12). Salen del registro de entregas (tabla `deliveries`) y
 * de lo que hay programado; nada se manda afuera del teléfono.
 */
data class DiagReport(
    val pendingReminders: Int,
    val pendingSnoozes: Int,
    /** null = calendario apagado o sin permiso. */
    val calendarNext7Days: Int?,
    val delivered: Int,
    val byPet: Int,
    val avgDelaySec: Long?,
    val maxDelaySec: Long?,
    val late: Int,
    val withoutExact: Int,
    val overlay: Boolean,
    val notifications: Boolean,
    val exactAlarms: Boolean,
    val battery: Boolean,
    val calendar: Boolean,
    /** Cierres inesperados de la mascota en los últimos 30 días: (cuándo se cerró, cómo volvió). */
    val petKills: List<Pair<Long, String>> = emptyList(),
)

const val DIAG_DAYS = 30
private const val LATE_SEC = 60L

suspend fun diagReport(ctx: Context): DiagReport = withContext(Dispatchers.IO) {
    val now = System.currentTimeMillis()
    val prefs = Prefs(ctx)
    val db = AppDatabase.get(ctx)
    val deliveries = runCatching { db.deliveries().all() }.getOrDefault(emptyList())
        .filter { it.deliveredAt > now - DIAG_DAYS * Prefs.DAY_MS }
    val delays = deliveries.map { ((it.deliveredAt - it.scheduledAt) / 1000).coerceAtLeast(0) }
    val calendarOn = prefs.calendarEnabled && CalendarSource.hasPermission(ctx)
    DiagReport(
        pendingReminders = runCatching { db.reminders().pending().size }.getOrDefault(0),
        pendingSnoozes = runCatching { db.snoozes().all().size }.getOrDefault(0),
        calendarNext7Days = if (!calendarOn) null else runCatching {
            CalendarSource.alertsBetween(ctx, now, now + 7 * Prefs.DAY_MS, prefs.calendarLeadMinutes, prefs.calendarIds, now).size
        }.getOrNull(),
        delivered = deliveries.size,
        byPet = deliveries.count { it.byPet },
        avgDelaySec = delays.takeIf { it.isNotEmpty() }?.average()?.toLong(),
        maxDelaySec = delays.maxOrNull(),
        late = delays.count { it > LATE_SEC },
        withoutExact = deliveries.count { !it.exactAlarms },
        overlay = Settings.canDrawOverlays(ctx),
        notifications = notificationsGranted(ctx),
        exactAlarms = AlertScheduler.canScheduleExact(ctx),
        battery = batteryUnrestricted(ctx),
        calendar = CalendarSource.hasPermission(ctx),
        petKills = prefs.petKills.mapNotNull { line ->
            val p = line.split("|")
            val at = p.getOrNull(0)?.toLongOrNull() ?: return@mapNotNull null
            if (at < now - DIAG_DAYS * Prefs.DAY_MS) null else at to (p.getOrNull(2) ?: "")
        },
    )
}

@Composable
fun DiagnosticsDialog(onDismiss: () -> Unit, onSendByEmail: () -> Unit) {
    val ctx = LocalContext.current
    var report by remember { mutableStateOf<DiagReport?>(null) }
    LaunchedEffect(Unit) { report = diagReport(ctx) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.diag_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.diag_intro),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                report?.let { r ->
                    Heading(stringResource(R.string.diag_scheduled))
                    Line(stringResource(R.string.diag_reminders, r.pendingReminders))
                    Line(stringResource(R.string.diag_snoozes, r.pendingSnoozes))
                    Line(
                        r.calendarNext7Days?.let { stringResource(R.string.diag_calendar_next, it) }
                            ?: stringResource(R.string.diag_calendar_off),
                    )

                    Heading(stringResource(R.string.diag_delivered))
                    if (r.delivered == 0) {
                        Line(stringResource(R.string.diag_none))
                    } else {
                        Line(stringResource(R.string.diag_total, r.delivered))
                        Line(stringResource(R.string.diag_by_pet, r.byPet))
                        Line(stringResource(R.string.diag_by_notif, r.delivered - r.byPet))
                        Line(stringResource(R.string.diag_delay, duration(r.avgDelaySec ?: 0), duration(r.maxDelaySec ?: 0)))
                        Line(stringResource(R.string.diag_late, r.late))
                        if (r.withoutExact > 0) Line(stringResource(R.string.diag_no_exact, r.withoutExact))
                    }

                    Heading(stringResource(R.string.diag_kills))
                    if (r.petKills.isEmpty()) {
                        Line(stringResource(R.string.diag_kills_none))
                    } else {
                        Line(stringResource(R.string.diag_kills_count, r.petKills.size))
                        r.petKills.takeLast(3).reversed().forEach { (at, by) ->
                            val how = stringResource(
                                when (by) {
                                    PetOverlayService.STARTED_BY_WATCHDOG, PetOverlayService.STARTED_BY_SYSTEM -> R.string.diag_kill_back_alone
                                    PetOverlayService.STARTED_BY_BOOT -> R.string.diag_kill_back_boot
                                    else -> R.string.diag_kill_back_app
                                },
                            )
                            Line("· " + dateTime(at) + " — " + how)
                        }
                    }

                    Heading(stringResource(R.string.diag_permissions))
                    Permission(stringResource(R.string.permission_overlay), r.overlay)
                    Permission(stringResource(R.string.permission_notifications), r.notifications)
                    Permission(stringResource(R.string.permission_exact), r.exactAlarms)
                    Permission(stringResource(R.string.permission_battery), r.battery)
                    Permission(stringResource(R.string.permission_calendar), r.calendar)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.done_button)) } },
        dismissButton = { TextButton(onClick = onSendByEmail) { Text(stringResource(R.string.diag_send)) } },
    )
}

private fun dateTime(millis: Long): String =
    java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT).format(java.util.Date(millis))

@Composable
private fun duration(seconds: Long): String =
    if (seconds < 60) stringResource(R.string.diag_seconds, seconds.toInt())
    else stringResource(R.string.minutes_n, (seconds / 60).toInt())

@Composable
private fun Heading(text: String) =
    Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))

@Composable
private fun Line(text: String) = Text(text, style = MaterialTheme.typography.bodyMedium)

@Composable
private fun Permission(name: String, granted: Boolean) =
    Line((if (granted) "✓ " else "✗ ") + name)
