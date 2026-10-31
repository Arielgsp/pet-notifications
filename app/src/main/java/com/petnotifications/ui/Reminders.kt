package com.petnotifications.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.petnotifications.R
import com.petnotifications.Repeat
import com.petnotifications.data.Reminder
import com.petnotifications.data.ReminderDao
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import android.text.format.DateFormat

private val zone: ZoneId get() = ZoneId.systemDefault()
private val timeFmt = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

private fun Long.toLocal(): LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), zone)

/** Día de la semana, día y mes con el orden y las palabras del idioma del teléfono ("Mié 3 oct", "Wed, Oct 3"). */
@Composable
private fun dayFormatter(): DateTimeFormatter {
    val locale = LocalConfiguration.current.locales[0]
    return remember(locale) { DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "EEEdMMM"), locale) }
}

/** "Hoy · 10:30", "Mañana · 8:00" o "Mié 3 oct · 9:00", en el idioma del teléfono. */
@Composable
fun formatWhen(millis: Long): String {
    val t = millis.toLocal()
    val today = LocalDate.now(zone)
    val dateFmt = dayFormatter()
    val day = when (t.toLocalDate()) {
        today -> stringResource(R.string.today)
        today.plusDays(1) -> stringResource(R.string.tomorrow)
        else -> t.format(dateFmt).replaceFirstChar { it.uppercase() }
    }
    return "$day · ${t.format(timeFmt)}"
}

@Composable
fun ReminderRow(r: Reminder, onClick: () -> Unit, onDelete: () -> Unit) {
    val repeat = Repeat.from(r.repeat)
    val whenText = formatWhen(r.timeMillis)
    val repeatText = stringResource(repeat.labelRes)
    val alertedText = stringResource(R.string.reminder_alerted)
    val quietText = stringResource(R.string.reminder_quiet)
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.alpha(if (r.fired) 0.55f else 1f),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 10.dp, end = 4.dp)) {
            Column(Modifier.weight(1f)) {
                Text(r.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    buildString {
                        append(whenText)
                        if (repeat != Repeat.NONE) append(" · 🔁 ${repeatText.lowercase()}")
                        if (r.fired) append(" · $alertedText")
                        if (r.quiet) append(" · $quietText")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (r.note.isNotBlank()) {
                    Text(r.note, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReminderEditor(initial: Reminder?, onDismiss: () -> Unit, onSave: (Reminder) -> Unit) {
    val start = remember {
        initial?.timeMillis?.toLocal()
            ?: LocalDateTime.now(zone).plusMinutes(30).withSecond(0).withNano(0)
    }
    var title by remember { mutableStateOf(initial?.title.orEmpty()) }
    var note by remember { mutableStateOf(initial?.note.orEmpty()) }
    var date by remember { mutableStateOf(start.toLocalDate()) }
    var time by remember { mutableStateOf(start.toLocalTime()) }
    var repeat by remember { mutableStateOf(Repeat.from(initial?.repeat)) }
    var quiet by remember { mutableStateOf(initial?.quiet ?: false) }
    var pickDate by remember { mutableStateOf(false) }
    var pickTime by remember { mutableStateOf(false) }
    val dateFmt = dayFormatter()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (initial == null) R.string.new_reminder else R.string.edit_reminder)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text(stringResource(R.string.reminder_what)) }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = note, onValueChange = { note = it },
                    label = { Text(stringResource(R.string.reminder_note)) }, maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { pickDate = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.CalendarMonth, null)
                        Text(" " + date.format(dateFmt))
                    }
                    OutlinedButton(onClick = { pickTime = true }) {
                        Icon(Icons.Default.Schedule, null)
                        Text(" " + time.format(timeFmt))
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Repeat.entries.forEach { r ->
                        FilterChip(selected = repeat == r, onClick = { repeat = r }, label = { Text(stringResource(r.labelRes)) })
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { quiet = !quiet },
                ) {
                    Checkbox(checked = quiet, onCheckedChange = { quiet = it })
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.reminder_quiet), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            stringResource(R.string.reminder_quiet_sub),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = {
                    val millis = LocalDateTime.of(date, time).atZone(zone).toInstant().toEpochMilli()
                    onSave(
                        (initial ?: Reminder(title = "", timeMillis = 0)).copy(
                            title = title.trim(),
                            note = note.trim(),
                            timeMillis = millis,
                            repeat = repeat.name,
                            fired = false,
                            quiet = quiet,
                        ),
                    )
                },
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )

    if (pickDate) {
        // El DatePicker trabaja en UTC a medianoche.
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { pickDate = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    pickDate = false
                }) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { pickDate = false }) { Text(stringResource(R.string.cancel)) } },
        ) { DatePicker(state) }
    }

    if (pickTime) {
        val state = rememberTimePickerState(time.hour, time.minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { pickTime = false },
            text = { TimePicker(state) },
            confirmButton = {
                TextButton(onClick = {
                    time = LocalTime.of(state.hour, state.minute)
                    pickTime = false
                }) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { pickTime = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

/** Se muestra cuando la mascota abre la app desde el globo de un recordatorio. */
@Composable
fun ReminderDetail(
    id: Long,
    dao: ReminderDao,
    onEdit: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit,
    onDismiss: () -> Unit,
) {
    var reminder by remember(id) { mutableStateOf<Reminder?>(null) }
    var loaded by remember(id) { mutableStateOf(false) }
    LaunchedEffect(id) {
        reminder = dao.get(id)
        loaded = true
    }
    if (!loaded) return
    val r = reminder
    if (r == null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.reminder_not_found)) },
            text = { Text(stringResource(R.string.reminder_not_found_text)) },
            confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.ok)) } },
        )
        return
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(r.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(formatWhen(r.timeMillis), style = MaterialTheme.typography.labelLarge)
                if (r.note.isNotBlank()) Text(r.note)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.done_button)) } },
        dismissButton = {
            Row {
                TextButton(onClick = { onDelete(r) }) { Text(stringResource(R.string.delete)) }
                TextButton(onClick = { onEdit(r) }) { Text(stringResource(R.string.edit)) }
            }
        },
    )
}
