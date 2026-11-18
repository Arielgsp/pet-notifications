package com.petnotifications.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Egg
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petnotifications.R
import java.text.Normalizer
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** Cómo se ve un aviso en la línea del día. */
internal enum class DayState { DONE, MISSED, NEXT, LATER }

/** Lo que muestra una fila de la línea del día. */
internal data class LineItem(
    val key: String,
    val at: Long,
    val title: String,
    val repeatLabel: String? = null,
    val calendar: Boolean = false,
    val done: Boolean = false,
    /** Se puede dejar para mañana (un aviso diario ya vuelve solo). */
    val canMoveToTomorrow: Boolean = true,
)

/** Estado de cada aviso, en el orden dado. Solo hoy hay "Se te pasó" y "próximo". */
internal fun dayStates(items: List<LineItem>, now: Long, isToday: Boolean): List<DayState> {
    var nextTaken = false
    return items.map { item ->
        when {
            item.done -> DayState.DONE
            !isToday -> DayState.LATER
            item.at <= now -> DayState.MISSED
            !nextTaken -> {
                nextTaken = true
                DayState.NEXT
            }
            else -> DayState.LATER
        }
    }
}

/** Dónde va la marca "Ahora": antes del primer aviso que todavía no llegó. */
internal fun nowIndex(items: List<LineItem>, now: Long): Int =
    items.indexOfFirst { it.at > now }.let { if (it < 0) items.size else it }

/** La próxima hora en punto, al menos 30 minutos después de [now]; null si ya sería otro día. */
internal fun nextHourSlot(now: Long, zone: ZoneId = ZoneId.systemDefault()): Long? {
    val start = Instant.ofEpochMilli(now).atZone(zone)
    val soonest = start.plusMinutes(30)
    val hour = soonest.truncatedTo(ChronoUnit.HOURS)
    val slot = if (hour == soonest) hour else hour.plusHours(1)
    return slot.takeIf { it.toLocalDate() == start.toLocalDate() }?.toInstant()?.toEpochMilli()
}

/** Horas y minutos que faltan, redondeando hacia arriba (nunca "En 0 min"). */
internal fun countdownParts(at: Long, now: Long): Pair<Int, Int> {
    val minutes = ((at - now + 59_999) / 60_000).coerceAtLeast(1).toInt()
    return minutes / 60 to minutes % 60
}

/** Color del círculo del ícono. */
internal enum class IconTone { AFFECTION, HOME, ERRAND }

/** Ícono de cada aviso, elegido por las palabras del título (sin internet). */
internal enum class DayIcon(val tone: IconTone, val words: Set<String>) {
    PHONE(IconTone.AFFECTION, setOf("llamar", "llamada", "call")),
    TRASH(IconTone.HOME, setOf("basura", "residuos", "trash")),
    PILL(IconTone.HOME, setOf("pastilla", "remedio", "medicamento", "pill")),
    WATER(IconTone.HOME, setOf("agua", "water")),
    PLANT(IconTone.HOME, setOf("regar", "planta", "plant")),
    CAKE(IconTone.AFFECTION, setOf("cumple", "cumpleanos", "birthday")),
    BULB(IconTone.ERRAND, setOf("luz", "pagar", "factura", "bill")),
    SCALE(IconTone.ERRAND, setOf("mediacion", "audiencia", "juzgado", "abogado")),
    EGG(IconTone.HOME, setOf("huevo")),
    CART(IconTone.ERRAND, setOf("comprar", "super", "supermercado", "shop")),
    MEETING(IconTone.AFFECTION, setOf("reunion", "meeting")),
    PAW(IconTone.AFFECTION, emptySet()),
    CALENDAR(IconTone.ERRAND, emptySet()),
}

internal fun dayIconFor(title: String, calendar: Boolean = false): DayIcon {
    if (calendar) return DayIcon.CALENDAR
    val plain = Normalizer.normalize(title.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")
    val tokens = plain.split(Regex("[^a-z0-9]+")).filter { it.isNotEmpty() }
    return DayIcon.entries.firstOrNull { icon ->
        tokens.any { it in icon.words || it.removeSuffix("s") in icon.words || it.removeSuffix("es") in icon.words }
    } ?: DayIcon.PAW
}

private val DayIcon.vector: ImageVector
    get() = when (this) {
        DayIcon.PHONE -> Icons.Outlined.Call
        DayIcon.TRASH -> Icons.Outlined.DeleteOutline
        DayIcon.PILL -> Icons.Outlined.Medication
        DayIcon.WATER -> Icons.Outlined.WaterDrop
        DayIcon.PLANT -> Icons.Outlined.LocalFlorist
        DayIcon.CAKE -> Icons.Outlined.Cake
        DayIcon.BULB -> Icons.Outlined.Lightbulb
        DayIcon.SCALE -> Icons.Outlined.Balance
        DayIcon.EGG -> Icons.Outlined.Egg
        DayIcon.CART -> Icons.Outlined.ShoppingCart
        DayIcon.MEETING -> Icons.Outlined.Groups
        DayIcon.PAW -> Icons.Outlined.Pets
        DayIcon.CALENDAR -> Icons.Outlined.CalendarMonth
    }

private val HOUR_WIDTH = 62.dp
private val RAIL_WIDTH = 36.dp

/** "En 25 min", "En 1 h 8 min", "En 3 h". */
@Composable
private fun countdownText(at: Long, now: Long): String {
    val (h, m) = countdownParts(at, now)
    return when {
        h == 0 -> stringResource(R.string.day_in_min, m)
        m == 0 -> stringResource(R.string.day_in_h, h)
        else -> stringResource(R.string.day_in_h_min, h, m)
    }
}

/**
 * La lista de un día como una línea de tiempo (pedido de Cowork, 18/11): hora, línea con un punto por
 * aviso y su tarjeta. Hoy marca "Ahora", el próximo aviso y lo que se pasó; los demás días, solo la lista.
 */
@Composable
internal fun DayLine(
    items: List<LineItem>,
    now: Long,
    isToday: Boolean,
    petHead: Painter,
    timeText: (Long) -> String,
    onOpen: (LineItem) -> Unit,
    onDone: (LineItem) -> Unit,
    onMove: (LineItem, Long) -> Unit,
) {
    val states = dayStates(items, now, isToday)
    val marker = if (isToday) nowIndex(items, now) else -1
    val total = items.size + if (isToday) 1 else 0
    Column(Modifier.fillMaxWidth()) {
        var row = 0
        items.forEachIndexed { i, item ->
            if (i == marker) {
                NowRow(now, petHead, timeText, row == 0, row == total - 1)
                row++
            }
            ItemLine(item, states[i], now, timeText, row == 0, row == total - 1, onOpen, onDone, onMove)
            row++
        }
        if (marker == items.size) NowRow(now, petHead, timeText, row == 0, row == total - 1)
    }
}

/** Hora, tramo de línea y contenido de una fila. */
@Composable
private fun LineRow(
    hour: String?,
    hourColor: Color,
    first: Boolean,
    last: Boolean,
    dotY: Dp,
    dot: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val line = Pet.colors.line
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Box(Modifier.width(HOUR_WIDTH).padding(top = (dotY - 3.dp).coerceAtLeast(0.dp))) {
            if (hour != null) {
                Text(hour, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight(800), color = hourColor, maxLines = 1, softWrap = false)
            }
        }
        Box(
            Modifier
                .width(RAIL_WIDTH)
                .fillMaxHeight()
                .drawBehind {
                    val x = size.width / 2
                    val y = dotY.toPx() + 6.dp.toPx()
                    drawLine(line, Offset(x, if (first) y else 0f), Offset(x, if (last) y else size.height), 2.dp.toPx())
                },
            contentAlignment = Alignment.TopCenter,
        ) { Box(Modifier.padding(top = dotY)) { dot() } }
        Box(Modifier.weight(1f).padding(bottom = 10.dp)) { content() }
    }
}

@Composable
private fun NowRow(now: Long, petHead: Painter, timeText: (Long) -> String, first: Boolean, last: Boolean) {
    val c = Pet.colors
    LineRow(
        null, c.ink, first, last, 0.dp,
        dot = {
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier.size(36.dp).clip(CircleShape).background(c.blushSoft).border(1.5.dp, c.blush, CircleShape),
            ) {
                Image(petHead, contentDescription = null, modifier = Modifier.offset(y = (-3).dp).requiredSize(54.dp))
            }
        },
    ) {
        Box(Modifier.heightIn(min = 36.dp), contentAlignment = Alignment.CenterStart) {
            Text(
                stringResource(R.string.day_now, timeText(now)), fontSize = 13.sp, fontWeight = FontWeight(800), color = c.ink,
                modifier = Modifier.background(c.blush, RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun Dot(fill: Color?, ring: Color, check: Boolean = false, halo: Color? = null) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(12.dp)) {
        if (halo != null) Box(Modifier.requiredSize(20.dp).background(halo, CircleShape))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .requiredSize(if (check) 16.dp else 12.dp)
                .background(fill ?: Pet.colors.surface, CircleShape)
                .border(2.dp, ring, CircleShape),
        ) {
            if (check) Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
        }
    }
}

@Composable
private fun IconCircle(icon: DayIcon, background: Color, size: Dp) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size).background(background, CircleShape)) {
        Icon(icon.vector, contentDescription = null, tint = Pet.colors.cocoa, modifier = Modifier.size(size * 0.56f))
    }
}

@Composable
private fun RepeatLine(label: String?) {
    if (label == null) return
    val c = Pet.colors
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
        Icon(Icons.Outlined.Repeat, contentDescription = null, tint = c.inkMuted, modifier = Modifier.size(12.dp))
        Text(label, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight(600), color = c.inkMuted, modifier = Modifier.padding(start = 4.dp))
    }
}

/** Botón de pastilla con 48 dp de área táctil. */
@Composable
private fun PillButton(text: String, filled: Boolean, onClick: () -> Unit) {
    val c = Pet.colors
    val shape = RoundedCornerShape(50)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.heightIn(min = 48.dp).clip(shape).clickable(onClick = onClick),
    ) {
        Text(
            text, fontSize = 13.sp, fontWeight = FontWeight(800), color = if (filled) c.surfaceRaised else c.cocoa, maxLines = 1,
            modifier = Modifier
                .background(if (filled) c.cocoa else Color.Transparent, shape)
                .border(1.dp, if (filled) c.cocoa else c.line, shape)
                .padding(horizontal = 12.dp, vertical = 7.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ItemLine(
    item: LineItem,
    state: DayState,
    now: Long,
    timeText: (Long) -> String,
    first: Boolean,
    last: Boolean,
    onOpen: (LineItem) -> Unit,
    onDone: (LineItem) -> Unit,
    onMove: (LineItem, Long) -> Unit,
) {
    val c = Pet.colors
    val hour = timeText(item.at)
    val icon = dayIconFor(item.title, item.calendar)
    val toneColor = when (icon.tone) {
        IconTone.AFFECTION -> c.blushSoft
        IconTone.HOME -> c.sageSoft
        IconTone.ERRAND -> c.honeySoft
    }
    val rowText = "$hour, ${item.title}"
    val hourColor = if (state == DayState.DONE) c.inkMuted else c.ink
    when (state) {
        DayState.DONE -> {
            val spoken = stringResource(R.string.day_a11y_done, hour, item.title)
            LineRow(hour, hourColor, first, last, 9.dp, dot = { Dot(c.sage, c.sage, check = true) }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { onOpen(item) }
                        .semantics { contentDescription = spoken }.padding(vertical = 2.dp),
                ) {
                    IconCircle(icon, c.sageSoft, 32.dp)
                    Text(
                        item.title, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight(700), color = c.inkMuted,
                        textDecoration = TextDecoration.LineThrough, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    )
                    Text(stringResource(R.string.day_done), fontSize = 12.sp, fontWeight = FontWeight(800), color = c.inkMuted)
                }
            }
        }
        DayState.MISSED -> {
            val spoken = stringResource(R.string.day_a11y_missed, hour, item.title)
            val shape = RoundedCornerShape(18.dp)
            LineRow(hour, hourColor, first, last, 14.dp, dot = { Dot(null, c.honey) }) {
                Column(
                    Modifier.fillMaxWidth().clip(shape).background(c.paper).dashedBorder(c.honey, 18.dp).padding(start = 12.dp, end = 10.dp, top = 10.dp, bottom = 4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { onOpen(item) }.semantics { contentDescription = spoken },
                    ) {
                        IconCircle(icon, c.honeySoft, 32.dp)
                        Column(Modifier.padding(start = 8.dp)) {
                            Text(item.title, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight(800), color = c.inkMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(stringResource(R.string.day_missed), fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight(600), color = c.inkMuted)
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PillButton(stringResource(R.string.day_done), filled = true) { onDone(item) }
                        nextHourSlot(now)?.let { slot ->
                            PillButton(stringResource(R.string.day_today_at, timeText(slot)), filled = false) { onMove(item, slot) }
                        }
                        if (item.canMoveToTomorrow) {
                            val tomorrow = Instant.ofEpochMilli(item.at).atZone(ZoneId.systemDefault()).plusDays(1).toInstant().toEpochMilli()
                            PillButton(stringResource(R.string.tomorrow), filled = false) { onMove(item, tomorrow) }
                        }
                    }
                }
            }
        }
        DayState.NEXT -> {
            val left = countdownText(item.at, now)
            val spoken = stringResource(R.string.day_a11y_next, hour, item.title, left)
            val mark = stringResource(R.string.day_mark_done, item.title)
            val shape = RoundedCornerShape(18.dp)
            LineRow(hour, hourColor, first, last, 18.dp, dot = { Dot(c.blush, c.blush, halo = c.blushSoft) }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, shape, ambientColor = c.ink.copy(alpha = 0.12f), spotColor = c.ink.copy(alpha = 0.12f))
                        .background(c.surfaceRaised, shape)
                        .border(1.5.dp, c.blush, shape)
                        .clip(shape)
                        .clickable { onOpen(item) }
                        .padding(start = 10.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                ) {
                    IconCircle(icon, toneColor, 40.dp)
                    Column(Modifier.weight(1f).padding(horizontal = 8.dp).semantics(mergeDescendants = true) { contentDescription = spoken }) {
                        Text(item.title, fontSize = 16.sp, lineHeight = 21.sp, fontWeight = FontWeight(800), color = c.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(
                            left, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight(800), color = c.cocoa,
                            modifier = Modifier.padding(top = 3.dp).background(c.blushSoft, RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                        RepeatLine(item.repeatLabel)
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(48.dp).clip(CircleShape).clickable { onDone(item) }.semantics { contentDescription = mark },
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(44.dp).border(1.5.dp, c.cocoa, CircleShape)) {
                            Icon(Icons.Outlined.Check, contentDescription = null, tint = c.cocoa, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }
        DayState.LATER -> LineRow(hour, hourColor, first, last, 10.dp, dot = { Dot(null, c.line) }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { onOpen(item) }
                    .semantics { contentDescription = rowText }.padding(vertical = 2.dp),
            ) {
                IconCircle(icon, toneColor, 32.dp)
                Column(Modifier.padding(start = 8.dp)) {
                    Text(item.title, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight(800), color = c.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    RepeatLine(item.repeatLabel)
                }
            }
        }
    }
}
