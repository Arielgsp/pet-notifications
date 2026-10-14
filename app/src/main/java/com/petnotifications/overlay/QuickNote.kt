package com.petnotifications.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petnotifications.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

private const val CUSTOM = -1

/** Cuándo avisar en el recordatorio rápido: opciones fijas, o día y hora a elección. */
enum class QuickWhen(val labelRes: Int) {
    MIN_30(R.string.quick_in_30),
    HOUR_1(R.string.quick_in_1h),
    HOURS_3(R.string.quick_in_3h),
    TOMORROW_9(R.string.quick_tomorrow);

    fun at(now: Long = System.currentTimeMillis()): Long = when (this) {
        MIN_30 -> now + 30 * 60_000L
        HOUR_1 -> now + 60 * 60_000L
        HOURS_3 -> now + 3 * 60 * 60_000L
        TOMORROW_9 -> LocalDate.now().plusDays(1).atTime(LocalTime.of(9, 0))
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}

/**
 * Tarjeta flotante del recordatorio rápido (tirón hacia abajo sobre la mascota): un texto y cuándo.
 * Se muestra en una ventana propia que ocupa la pantalla; tocar afuera de la tarjeta la cierra.
 */
@OptIn(ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun QuickNoteCard(onSave: (String, Long) -> Unit, onCancel: () -> Unit) {
    var text by remember { mutableStateOf("") }
    var whenIndex by remember { mutableIntStateOf(QuickWhen.HOUR_1.ordinal) }
    /** -1 = "Elegir día y hora": se muestran la fecha y la hora para escribir. */
    val custom = whenIndex == CUSTOM
    val zone = ZoneId.systemDefault()
    val inOneHour = remember { java.time.LocalDateTime.now().plusHours(1) }
    /** Días desde hoy (0 = hoy). */
    var dayOffset by remember { mutableIntStateOf(if (inOneHour.toLocalDate() == LocalDate.now()) 0 else 1) }
    var hourText by remember { mutableStateOf("%02d".format(inOneHour.hour)) }
    var minuteText by remember { mutableStateOf("00") }
    fun chosenAt(): Long? {
        if (!custom) return QuickWhen.entries[whenIndex].at()
        val h = hourText.toIntOrNull()?.takeIf { it in 0..23 } ?: return null
        val m = minuteText.toIntOrNull()?.takeIf { it in 0..59 } ?: return null
        return LocalDate.now().plusDays(dayOffset.toLong()).atTime(h, m).atZone(zone).toInstant().toEpochMilli()
    }
    val at = chosenAt()
    val valid = at != null && at > System.currentTimeMillis()
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        focus.requestFocus()
        keyboard?.show()
    }
    fun save() {
        if (text.isNotBlank() && valid) onSave(text.trim(), at!!)
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0x66000000))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onCancel)
            .onPreviewKeyEvent { if (it.key == Key.Back) { onCancel(); true } else false },
        contentAlignment = Alignment.TopCenter,
    ) {
        val shape = RoundedCornerShape(20.dp)
        Column(
            Modifier
                .padding(top = 96.dp, start = 16.dp, end = 16.dp)
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                // La tarjeta en sí no cierra al tocarla.
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                .background(Paper, shape)
                .border(3.dp, Ink, shape)
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(stringResource(R.string.quick_title), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.reminder_what)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { save() }),
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                QuickWhen.entries.forEach { w ->
                    FilterChip(
                        selected = whenIndex == w.ordinal,
                        onClick = { whenIndex = w.ordinal },
                        label = { Text(stringResource(w.labelRes)) },
                    )
                }
                FilterChip(
                    selected = custom,
                    onClick = { whenIndex = CUSTOM },
                    label = { Text(stringResource(R.string.quick_custom)) },
                )
            }
            if (custom) {
                // Fecha y hora para escribir (sin ventanas aparte: la tarjeta flota sobre otras apps).
                // Día: flechas para pasar de un día a otro.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { if (dayOffset > 0) dayOffset-- }, enabled = dayOffset > 0) { Text("‹", fontSize = 22.sp) }
                    Text(
                        dayLabel(dayOffset),
                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { if (dayOffset < 365) dayOffset++ }) { Text("›", fontSize = 22.sp) }
                }
                // Hora: dos casilleros chicos (horas y minutos, formato 24 h).
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    TimeField(hourText, stringResource(R.string.quick_hour)) { hourText = it }
                    Text(":", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.padding(horizontal = 6.dp))
                    TimeField(minuteText, stringResource(R.string.quick_minute)) { minuteText = it }
                }
                if (!valid) {
                    Text(stringResource(R.string.quick_past), fontSize = 13.sp, color = Color(0xFFB3261E))
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onCancel) { Text(stringResource(R.string.cancel)) }
                TextButton(onClick = { save() }, enabled = text.isNotBlank() && valid) { Text(stringResource(R.string.save)) }
            }
        }
    }
}

/** "Hoy", "Mañana" o el día de la semana con la fecha ("Vie 2 oct"), en el idioma del teléfono. */
@Composable
private fun dayLabel(offset: Int): String = when (offset) {
    0 -> stringResource(R.string.today)
    1 -> stringResource(R.string.tomorrow)
    else -> {
        val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
        val pattern = android.text.format.DateFormat.getBestDateTimePattern(locale, "EEEdMMM")
        LocalDate.now().plusDays(offset.toLong())
            .format(java.time.format.DateTimeFormatter.ofPattern(pattern, locale))
            .replaceFirstChar { it.uppercase(locale) }
    }
}

/** Casillero chico de dos dígitos (hora o minutos). */
@Composable
private fun TimeField(value: String, label: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { v -> onChange(v.filter { it.isDigit() }.take(2)) },
        label = { Text(label, fontSize = 11.sp) },
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center),
        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number, imeAction = ImeAction.Next),
        modifier = Modifier.width(76.dp),
    )
}
