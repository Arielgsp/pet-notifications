package com.petnotifications.overlay

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petnotifications.R
import com.petnotifications.ui.Nunito
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.roundToInt

private const val CUSTOM = -1

/** Cuándo avisar en el recordatorio rápido: opciones fijas, o día y hora a elección (el calendario). */
enum class QuickWhen(val labelRes: Int) {
    MIN_10(R.string.quick_in_10),
    MIN_30(R.string.quick_in_30),
    HOUR_1(R.string.quick_in_1h),
    TOMORROW_9(R.string.quick_tomorrow);

    fun at(now: Long = System.currentTimeMillis()): Long = when (this) {
        MIN_10 -> now + 10 * 60_000L
        MIN_30 -> now + 30 * 60_000L
        HOUR_1 -> now + 60 * 60_000L
        TOMORROW_9 -> LocalDate.now().plusDays(1).atTime(LocalTime.of(9, 0))
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}

/**
 * Dónde está la mascota, en píxeles medidos desde arriba a la izquierda de la pantalla, para poner la
 * cartita a su lado.
 */
data class PetSpot(val x: Int, val top: Int, val width: Int, val height: Int, val mouthX: Float)

/** Cómo se ve la mascota, para dibujarla dentro de la ventana del recordatorio rápido. */
data class PetLook(val pet: com.petnotifications.PetType, val coat: Int, val outfit: com.petnotifications.Outfit, val alpha: Float)

/**
 * Recordatorio rápido (tirón hacia abajo sobre la mascota), pedido de Cowork del 06/11: la mascota
 * sostiene una cartita de papel al costado, con "(nombre) anota", el texto sobre una línea punteada y
 * las horas en fichas (10 min, 30 min, 1 hora, Mañana 9:00 y el calendario, que abre día y hora).
 * "Anotado" (o el Enter del teclado) lo guarda. Si la mascota está en la mitad de abajo de la pantalla,
 * sube con la cartita lo que mide el teclado ([petTop] dice dónde quedó); en la mitad de arriba no se
 * mueve. La cartita va siempre pegada a ella. Tocar afuera la cierra.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickNoteCard(
    who: String,
    pet: PetSpot,
    look: PetLook,
    petTop: androidx.compose.runtime.IntState,
    ime: androidx.compose.runtime.IntState,
    closing: androidx.compose.runtime.State<Boolean>,
    onShown: () -> Unit,
    onResize: (Int) -> Unit,
    onSave: (String, Long) -> Unit,
    onCancel: () -> Unit,
) {
    var text by remember { mutableStateOf("") }
    var whenIndex by remember { mutableIntStateOf(QuickWhen.HOUR_1.ordinal) }
    val custom = whenIndex == CUSTOM
    val zone = ZoneId.systemDefault()
    val inOneHour = remember { java.time.LocalDateTime.now().plusHours(1) }
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
    val canSave = text.isNotBlank() && valid
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        focus.requestFocus()
        keyboard?.show()
    }
    // Con la mascota ya dibujada acá (un cuadro después), se esconde la de verdad.
    LaunchedEffect(Unit) {
        androidx.compose.runtime.withFrameNanos { }
        androidx.compose.runtime.withFrameNanos { }
        onShown()
    }
    // La mascota sube (y vuelve) de a poco, y la cartita con ella.
    val shownTop by androidx.compose.animation.core.animateIntAsState(petTop.intValue, tween(200), label = "petTop")
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) { appear.animateTo(1f, tween(320)) }
    fun save() {
        if (canSave) onSave(text.trim(), at!!)
    }
    val font = TextStyle(fontFamily = Nunito)
    // La ventana puede no empezar arriba de todo: las medidas de la mascota son de la pantalla.
    val window = androidx.compose.ui.platform.LocalView.current
    var windowTop by remember { mutableIntStateOf(0) }
    var fullH by remember { mutableIntStateOf(0) }
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned { windowTop = IntArray(2).also(window::getLocationOnScreen)[1] }
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onCancel)
            .onPreviewKeyEvent { if (it.key == Key.Back) { onCancel(); true } else false },
    ) {
        val density = LocalDensity.current
        val screenW = constraints.maxWidth
        // Lo que queda libre arriba del teclado. Según el celular, la ventana se achica sola o no:
        // si se achica, se avisa cuánto (así la mascota sube igual); si no, se le resta el teclado.
        val windowH = constraints.maxHeight
        LaunchedEffect(windowH) {
            if (windowH > fullH) fullH = windowH
            onResize(fullH - windowH)
        }
        val screenH = minOf(windowH, maxOf(fullH, windowH) - ime.intValue)
        // La mascota donde quedó (sube con el teclado si estaba en la mitad de abajo).
        @Suppress("NAME_SHADOWING")
        val pet = pet.copy(top = shownTop - windowTop)
        // La mascota, dibujada en esta ventana: la de verdad queda invisible mientras tanto.
        androidx.compose.foundation.Image(
            com.petnotifications.petPainter(look.pet, com.petnotifications.PetView.FRONT, look.coat, look.outfit),
            contentDescription = null,
            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
            modifier = Modifier
                .offset { IntOffset(pet.x, pet.top) }
                .size(with(density) { pet.width.toDp() }, with(density) { pet.height.toDp() })
                .graphicsLayer { alpha = look.alpha },
        )
        if (closing.value) return@BoxWithConstraints
        val margin = with(density) { 8.dp.toPx() }.roundToInt()
        val cardW = with(density) { 262.dp.toPx() }.roundToInt().coerceAtMost(screenW - 2 * margin)
        var cardH by remember { mutableIntStateOf(0) }
        // Al costado de la mascota, del lado con más lugar; el borde del papel llega justo a su cuerpo.
        val body = ((0.5f - kotlin.math.abs(pet.mouthX - 0.5f)) * 0.6f * pet.width).roundToInt()
        val petLeft = pet.x + body
        val petRight = pet.x + pet.width - body
        val right = screenW - petRight >= petLeft
        val topMargin = with(density) { 32.dp.toPx() }.roundToInt() // la barra de arriba
        // Si al costado no entra, va arriba de la mascota (o debajo, si arriba tampoco hay lugar), sin taparla.
        val sideFits = maxOf(screenW - petRight, petLeft) >= cardW + margin
        val maxX = (screenW - cardW - margin).coerceAtLeast(margin)
        val x = if (sideFits) (if (right) petRight else petLeft - cardW).coerceIn(margin, maxX)
        else (pet.x + pet.width / 2 - cardW / 2).coerceIn(margin, maxX)
        // Pegada a la mascota, sin hueco: al costado a la altura de su cabeza; si no entra, justo arriba
        // de ella, o justo debajo si arriba no hay lugar.
        val headTop = pet.top + (pet.height * 0.12f).roundToInt()
        val aboveFits = headTop - cardH >= topMargin
        val wantedY = when {
            sideFits -> headTop
            aboveFits -> headTop - cardH
            else -> pet.top + (pet.height * 0.92f).roundToInt()
        }
        val y = wantedY.coerceIn(topMargin, (screenH - cardH - margin).coerceAtLeast(topMargin))
        val shape = RoundedCornerShape(18.dp)
        Column(
            Modifier
                .offset { IntOffset(x, y) }
                .width(with(density) { cardW.toDp() })
                .onSizeChanged { cardH = it.height }
                .graphicsLayer {
                    alpha = appear.value
                    scaleX = 0.9f + 0.1f * appear.value
                    scaleY = 0.9f + 0.1f * appear.value
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(if (!sideFits) 0.5f else if (right) 0f else 1f, if (y < pet.top) 1f else 0f)
                    rotationZ = -1.5f
                }
                // La cartita en sí no cierra al tocarla.
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                .shadow(8.dp, shape, ambientColor = Ink, spotColor = Ink)
                .background(Paper, shape)
                .border(1.dp, Line, shape)
                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Edit, contentDescription = null, tint = InkMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.quick_writes, who), style = font, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold, color = InkMuted)
            }
            // El texto, sin caja: sobre una línea punteada color pelaje.
            val hint = stringResource(R.string.reminder_what)
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                textStyle = font.copy(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Ink),
                cursorBrush = SolidColor(Cocoa),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { save() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focus)
                    .semantics { contentDescription = hint }
                    .drawBehind {
                        val y0 = size.height - 1.dp.toPx()
                        drawLine(
                            Color(0xFFC98A55), Offset(0f, y0), Offset(size.width, y0), 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())),
                        )
                    }
                    .padding(horizontal = 2.dp, vertical = 7.dp),
                decorationBox = { inner ->
                    Box {
                        if (text.isEmpty()) Text(hint, style = font, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = InkMuted)
                        inner()
                    }
                },
            )
            // Las horas; con este ancho se parten en dos filas y "Anotado" queda al final, a la derecha.
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                QuickWhen.entries.forEach { w ->
                    TimeChip(stringResource(w.labelRes), whenIndex == w.ordinal) { whenIndex = w.ordinal }
                }
                TimeChip(null, custom, stringResource(R.string.quick_custom)) { whenIndex = if (custom) QuickWhen.HOUR_1.ordinal else CUSTOM }
                Spacer(Modifier.weight(1f))
                SaveButton(stringResource(R.string.quick_save), canSave) { save() }
            }
            if (custom) {
                // Día y hora para escribir (sin ventanas aparte: la cartita flota sobre otras apps).
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ArrowButton("‹", dayOffset > 0) { dayOffset-- }
                    Text(
                        dayLabel(dayOffset), style = font, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Ink,
                        textAlign = TextAlign.Center, modifier = Modifier.weight(1f),
                    )
                    ArrowButton("›", dayOffset < 365) { dayOffset++ }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    TimeField(hourText, stringResource(R.string.quick_hour)) { hourText = it }
                    Text(":", style = font, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Ink, modifier = Modifier.padding(horizontal = 6.dp))
                    TimeField(minuteText, stringResource(R.string.quick_minute)) { minuteText = it }
                }
                if (!valid) {
                    Text(stringResource(R.string.quick_past), style = font, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB3261E))
                }
            }
        }
    }
}

/**
 * Ficha de hora (Design): píldora de 32 dp, 13/800. Sin elegir, borde suave y letra apagada; elegida,
 * fondo rosa suave, borde rosa y letra cocoa. Con [label] null es el botón del calendario (solo ícono).
 * El área que se toca tiene 48 dp de alto aunque la ficha se vea de 32.
 */
@Composable
private fun TimeChip(label: String?, selected: Boolean, a11y: String? = null, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .sizeIn(minHeight = 48.dp)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, role = Role.Button, onClick = onClick)
            .then(if (a11y != null) Modifier.semantics { contentDescription = a11y } else Modifier),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(32.dp)
                .then(if (label == null) Modifier.width(34.dp) else Modifier)
                .background(if (selected) BlushSoft else Color.Transparent, shape)
                .border(1.dp, if (selected) Blush else Line, shape)
                .padding(horizontal = if (label == null) 0.dp else 9.dp),
        ) {
            if (label == null) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = Cocoa, modifier = Modifier.size(16.dp))
            } else {
                Text(label, fontFamily = Nunito, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = if (selected) Cocoa else InkMuted, maxLines = 1)
            }
        }
    }
}

/** "Anotado": cocoa con letra clara; apagado hasta que haya texto. */
@Composable
private fun SaveButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .sizeIn(minHeight = 48.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(34.dp)
                .background(if (enabled) Cocoa else Cocoa.copy(alpha = 0.35f), shape)
                .padding(horizontal = 14.dp),
        ) {
            Text(text, fontFamily = Nunito, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = SurfaceRaised)
        }
    }
}

@Composable
private fun ArrowButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(48.dp).clickable(enabled = enabled, role = Role.Button, onClick = onClick),
    ) {
        Text(text, fontFamily = Nunito, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = if (enabled) Cocoa else Line)
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

/** Casillero chico de dos dígitos (hora o minutos), en el mismo papel: sin caja, con línea punteada. */
@Composable
private fun TimeField(value: String, label: String, onChange: (String) -> Unit) {
    BasicTextField(
        value = value,
        onValueChange = { v -> onChange(v.filter { it.isDigit() }.take(2)) },
        singleLine = true,
        textStyle = TextStyle(fontFamily = Nunito, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Ink, textAlign = TextAlign.Center),
        cursorBrush = SolidColor(Cocoa),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        modifier = Modifier
            .width(56.dp)
            .semantics { contentDescription = label }
            .drawBehind {
                val y0 = size.height - 1.dp.toPx()
                drawLine(
                    Color(0xFFC98A55), Offset(0f, y0), Offset(size.width, y0), 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())),
                )
            }
            .padding(vertical = 6.dp),
    )
}
