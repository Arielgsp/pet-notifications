package com.petnotifications.overlay

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petnotifications.Prefs
import com.petnotifications.R
import com.petnotifications.ui.Nunito
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

// Colores de Design (tokens.json) que usan el globo y las cartas.
private val FurTone = Color(0xFFC98A55) // fur
private val FurDeep = Color(0xFF8A5A3B) // fur-deep
private val Sage = Color(0xFF7FA88B) // sage
private val Honey = Color(0xFFEBB854) // honey

private val Cream = Color(0xFFF7E6CC) // cream (card-quiet)
private val SageSoft = Color(0xFFE4F0E6) // sage-soft

/**
 * Cada carta, con su color y su ícono (Design, Cartas): pendiente en rosa con campanita, sin
 * interrumpir en crema con luna, frase en verde suave con hojita. Nunca rojo ni amarillo.
 */
private class LetterTone(val fill: Color, val card: Color, val cardLine: Color, val icon: Path)

/** Íconos del sello, en el viewBox 84 × 60 del sobre (centrados en 42, 34). */
private fun svgPath(d: String) = PathParser().parsePathString(d).toPath()

private val BellIcon by lazy { svgPath("M37 36a5 5 0 0 1 10 0c0 3 1 4 1.5 4.5h-13c.5-.5 1.5-1.5 1.5-4.5zM40.5 43a1.5 1.5 0 0 0 3 0") }
private val MoonIcon by lazy { svgPath("M46 37.5a6 6 0 1 1-6.5-8 4.8 4.8 0 0 0 6.5 8z") }
private val LeafIcon by lazy { svgPath("M36.5 39.5c0-6 4-9 10-9.5-.5 6-3.5 10-9.5 10zM36.5 39.5l5-5") }

private fun LetterKind.tone() = when (this) {
    LetterKind.PENDING -> LetterTone(Blush, BlushSoft, Blush, BellIcon)
    LetterKind.QUIET -> LetterTone(Cream, Paper, Cream, MoonIcon)
    LetterKind.PHRASE -> LetterTone(Sage, SageSoft, Sage, LeafIcon)
}

// ---------------------------------------------------------------- globo de la onomatopeya

/** Medidas del globo en dp, como el viewBox de Design (118 × 96 para una palabra de 4 letras). */
private const val STICKER_H = 96f
private const val STICKER_PAD_TOP = 10f

/** El globo se dibuja con las medidas de Design y se achica a este tamaño (el de Design tapaba demasiado). */
private const val STICKER_SCALE = 0.68f

/** Ancho del cuerpo del globo según la palabra ("guau" → 96, "pío pío" más ancho). */
private fun bodyWidth(text: String) = maxOf(96f, 30f + text.length * 17f)

/** Ancho total (cuerpo + pico) del globo para [text]. */
fun stickerWidth(text: String): Dp = ((bodyWidth(text) + 22f) * STICKER_SCALE).dp

/** Alto total del globo, con lugar arriba para los destellos. */
val StickerHeight: Dp = ((STICKER_H + STICKER_PAD_TOP + 4f) * STICKER_SCALE).dp

/** Punta del pico: a esta altura desde arriba del globo y a esta distancia de su borde lateral. */
val StickerTipY: Dp = ((STICKER_PAD_TOP + 88f) * STICKER_SCALE).dp
val StickerTipInset: Dp = (6f * STICKER_SCALE).dp

private val StickerHeightRaw: Dp = (STICKER_H + STICKER_PAD_TOP + 4f).dp

/**
 * Onomatopeya del aviso en un globo tipo sticker (Design, Anuncio): C · Caramelo (borde festoneado
 * rosa, sombra pelaje) o B · Nubecita (bordes de nube, sombra salvia). Minúsculas, sin signos, letras
 * que saltan una por una, un corazoncito y dos destellos. El pico sale abajo a la derecha (la mascota
 * a la derecha); [mirrored]: la mascota está a la izquierda.
 */
@Composable
fun StickerBubble(text: String, style: Int, mirrored: Boolean, id: Long, modifier: Modifier = Modifier, onTap: (() -> Unit)? = null) {
    val appear = remember(id) { Animatable(0f) }
    val hops = remember(id, text) { text.map { Animatable(0f) } }
    LaunchedEffect(id) {
        appear.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 420f))
        // Las letras saltan una por una; después, cada tanto, de nuevo (sin vibrar nunca).
        while (true) {
            hops.forEachIndexed { i, a ->
                launch {
                    delay(i * 55L)
                    a.animateTo(1f, tween(140))
                    a.animateTo(0f, spring(dampingRatio = 0.5f, stiffness = 500f))
                }
            }
            delay(3_200)
        }
    }
    val twinkle by rememberInfiniteTransition(label = "twinkle")
        .animateFloat(0.35f, 1f, infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse), label = "a")
    val body = bodyWidth(text)
    val total = body + 22f
    val cloud = style == Prefs.BUBBLE_CLOUD
    val density = LocalDensity.current
    Box(
        modifier
            .size(stickerWidth(text), StickerHeight)
            .graphicsLayer {
                scaleX = appear.value
                scaleY = appear.value
                alpha = appear.value.coerceIn(0f, 1f)
                // Crece desde la punta del pico (la boca de la mascota).
                transformOrigin = TransformOrigin(if (mirrored) 0.08f else 0.92f, 0.95f)
            }
            .then(if (onTap != null) Modifier.pointerInput(id) { detectTapGestures(onTap = { onTap() }) } else Modifier),
    ) {
    // Todo adentro se dibuja con las medidas de Design, achicado a STICKER_SCALE.
    CompositionLocalProvider(LocalDensity provides Density(density.density * STICKER_SCALE, density.fontScale)) {
    Box(Modifier.size(total.dp, StickerHeightRaw)) {
        Canvas(Modifier.fillMaxSize().graphicsLayer { if (mirrored) scaleX = -1f }) {
            translate(top = STICKER_PAD_TOP.dp.toPx()) { drawSticker(body, cloud) }
        }
        // Corazoncito arriba del lado opuesto al pico y dos destellos arriba del lado del pico.
        val heartX = if (mirrored) total - 22f else 0f
        Heart(Modifier.offset(heartX.dp, 2.dp).size(17.dp))
        listOf(body - 16f to 12f, body + 2f to 9f).forEachIndexed { i, (x, s) ->
            val sx = if (mirrored) total - x - s else x
            Canvas(
                Modifier
                    .offset(sx.dp, (if (i == 0) 0f else 8f).dp)
                    .size(s.dp)
                    .graphicsLayer { alpha = if (i == 0) twinkle else 1.35f - twinkle },
            ) { drawSparkle() }
        }
        // La palabra, centrada en el cuerpo del globo.
        val bodyStart = if (mirrored) total - 6f - body else 6f
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .offset(bodyStart.dp, (STICKER_PAD_TOP + 6f).dp)
                .size(body.dp, 58.dp),
        ) {
            text.forEachIndexed { i, ch ->
                Text(
                    ch.toString(),
                    style = TextStyle(
                        fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 25.sp, color = Cocoa,
                        shadow = Shadow(Blush, Offset(0f, 5f), 0f),
                    ),
                    modifier = Modifier.graphicsLayer {
                        translationY = -hops[i].value * 9.dp.toPx()
                        rotationZ = if (i % 2 == 0) -6f else 5f
                    },
                )
            }
        }
    }
    }
    }
}

/** Dibuja el globo (sombra, contorno y relleno, como las tres capas del SVG de Design). */
private fun DrawScope.drawSticker(body: Float, cloud: Boolean) {
    val u = 1.dp.toPx()
    val right = 6f + body // borde derecho del cuerpo, en dp
    val tail = Path().apply {
        moveTo((right - 38f) * u, 58f * u)
        quadraticTo((right - 14f) * u, 70f * u, (right + 10f) * u, 88f * u)
        quadraticTo((right - 8f) * u, 66f * u, (right - 4f) * u, 44f * u)
        close()
    }
    val (shadow, outline, fill) = if (cloud) Triple(Sage, FurDeep, SurfaceRaised) else Triple(FurTone, Blush, BlushSoft)
    fun layer(color: Color, strokeDp: Float) {
        val stroke = if (strokeDp > 0f) Stroke(strokeDp * u, join = StrokeJoin.Round) else null
        fun circle(cx: Float, cy: Float, r: Float) {
            drawCircle(color, r * u, Offset(cx * u, cy * u))
            stroke?.let { drawCircle(color, r * u, Offset(cx * u, cy * u), style = it) }
        }
        if (cloud) {
            val top = 12f
            val h = 48f
            val tl = Offset(10f * u, top * u)
            val sz = Size((body - 8f) * u, h * u)
            drawRoundRect(color, tl, sz, CornerRadius(24f * u))
            stroke?.let { drawRoundRect(color, tl, sz, CornerRadius(24f * u), style = it) }
            // Bultos de nube: arriba, abajo y a los costados.
            val topCount = ((body - 36f) / 26f).toInt() + 1
            val topStep = (body - 44f) / (topCount - 1).coerceAtLeast(1)
            repeat(topCount) { circle(20f + topStep * it + 6f, 17f, 15f) }
            val bottomCount = ((body - 60f) / 28f).toInt() + 1
            val bottomStep = (body - 68f) / (bottomCount - 1).coerceAtLeast(1)
            repeat(bottomCount) { circle(34f + bottomStep * it, 56f, 12f) }
            circle(14f, 36f, 14f)
            circle(right - 8f, 36f, 14f)
        } else {
            val tl = Offset(8f * u, 8f * u)
            val sz = Size((body - 4f) * u, 54f * u)
            drawRoundRect(color, tl, sz, CornerRadius(27f * u))
            stroke?.let { drawRoundRect(color, tl, sz, CornerRadius(27f * u), style = it) }
            // Festón: circulitos de radio 7 cada 13 dp arriba, abajo y a la izquierda (a la derecha va el pico).
            var x = 26f
            while (x <= right - 22f) { circle(x, 9f, 7f); x += 13f }
            x = 32f
            while (x <= right - 16f) { circle(x, 61f, 7f); x += 13f }
            listOf(22f, 35f, 48f).forEach { circle(9f, it, 7f) }
        }
        drawPath(tail, color)
        stroke?.let { drawPath(tail, color, style = it) }
    }
    translate(top = 4f * u) { layer(shadow, 6f) }
    layer(outline, 5f)
    layer(fill, 0f)
}

/** Corazoncito de Design (blush), dibujado: el carácter ♥ sale como emoji rojo en Android. */
@Composable
private fun Heart(modifier: Modifier) = Canvas(modifier) {
    val k = size.width / 24f
    val p = Path().apply {
        moveTo(12f * k, 21f * k)
        cubicTo(12f * k, 21f * k, 3f * k, 14.5f * k, 3f * k, 8.8f * k)
        cubicTo(3f * k, 5.6f * k, 5.5f * k, 3.5f * k, 8.3f * k, 3.5f * k)
        cubicTo(10f * k, 3.5f * k, 11.3f * k, 4.4f * k, 12f * k, 5.6f * k)
        cubicTo(12.7f * k, 4.4f * k, 14f * k, 3.5f * k, 15.7f * k, 3.5f * k)
        cubicTo(18.5f * k, 3.5f * k, 21f * k, 5.6f * k, 21f * k, 8.8f * k)
        cubicTo(21f * k, 14.5f * k, 12f * k, 21f * k, 12f * k, 21f * k)
        close()
    }
    drawPath(p, Blush)
}

/** Destello de puntas redondeadas (Design: honey). */
private fun DrawScope.drawSparkle() {
    val k = size.width / 14f
    val p = Path().apply {
        moveTo(7f * k, 0f)
        quadraticTo(8f * k, 6f * k, 14f * k, 7f * k)
        quadraticTo(8f * k, 8f * k, 7f * k, 14f * k)
        quadraticTo(6f * k, 8f * k, 0f, 7f * k)
        quadraticTo(6f * k, 6f * k, 7f * k, 0f)
        close()
    }
    drawPath(p, Honey)
}

/** "¡Guau!" → "guau", "¡Hop hop!" → "hop hop": minúsculas y sin signos (Design). */
fun callWord(sound: String): String =
    sound.filter { it.isLetter() || it == ' ' }.trim().lowercase()

// ---------------------------------------------------------------- tarjeta del aviso

/**
 * La tarjeta del aviso (Design, Asomado): papel claro, borde suave, esquinas redondeadas, línea de
 * arriba "Luna te avisa · 16:00", título, texto y "Listo" / "Aplazar" en pastillas. Si se abrió desde
 * una carta, toma su color y va apenas inclinada, como un papelito.
 */
@Composable
fun NoteCard(
    phase: FxPhase.Bubble,
    who: String,
    actions: BubbleActions,
    modifier: Modifier = Modifier,
    held: Pair<Float, Float>? = null,
    paw: Pair<Long, Long> = 0xFFFFFFFF to 0xFF3A2E27,
) {
    val ctx = LocalContext.current
    val alert = phase.alert
    val letter = phase.letter
    val tone = letter?.kind?.tone()
    val appear = remember(alert.key, letter?.id) { Animatable(0f) }
    LaunchedEffect(alert.key, letter?.id) { appear.animateTo(1f, spring(dampingRatio = 0.75f, stiffness = 380f)) }
    val countdown = remember(alert.key) { Animatable(1f) }
    LaunchedEffect(alert.key, phase.autoHideMs) {
        phase.autoHideMs?.let { countdown.animateTo(0f, tween(it.toInt(), easing = LinearEasing)) }
    }
    val time = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(letter?.at ?: System.currentTimeMillis()))
    val meta = when (letter?.kind) {
        LetterKind.PENDING -> ctx.getString(R.string.note_unattended, time)
        LetterKind.QUIET -> ctx.getString(R.string.note_quiet, time)
        LetterKind.PHRASE -> ctx.getString(R.string.note_friendly, who)
        null -> when (alert.source) {
            PetAlert.Source.APP, PetAlert.Source.TEST -> ctx.getString(R.string.note_says, who)
            else -> ctx.getString(R.string.note_from, who, time)
        }
    }
    val shape = RoundedCornerShape(22.dp)
    val text = TextStyle(fontFamily = Nunito)
    Column(
        modifier
            .fillMaxWidth()
            .padding(start = 6.dp, end = 6.dp, bottom = 6.dp, top = if (held != null) 16.dp else 6.dp)
            .graphicsLayer {
                alpha = appear.value.coerceIn(0f, 1f)
                translationY = (1f - appear.value) * 14.dp.toPx()
                rotationZ = if (letter != null) -1.5f else 0f
            }
            // Las patitas de la mascota, apoyadas sobre el borde de arriba (por delante de la tarjeta).
            .drawWithContent {
                drawContent()
                held?.let { (x, petW) -> drawPaws(x - 6.dp.toPx(), petW, Color(paw.first), Color(paw.second)) }
            }
            .shadow(8.dp, shape, ambientColor = Ink, spotColor = Ink)
            .background(tone?.card ?: SurfaceRaised, shape)
            .border(1.5.dp, tone?.cardLine ?: Line, shape)
            .pointerInput(alert.key, letter?.id) {
                detectTapGestures(onTap = { actions.onTap() }, onLongPress = { actions.onLongPress() })
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                meta, style = text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = InkMuted,
                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
            )
            // El sello de la carta (con su ícono), para que no dependa solo del color.
            if (tone != null) Canvas(Modifier.padding(start = 6.dp).size(20.dp)) { drawSeal(tone) }
        }
        if (alert.title.isNotBlank()) {
            Text(
                alert.title, style = text,
                fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Ink,
                maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp),
            )
        }
        if (alert.body.isNotBlank()) {
            Text(
                alert.body, style = text,
                fontSize = 15.sp, color = Ink,
                maxLines = if (alert.source == PetAlert.Source.APP) 6 else 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        val actionable = alert.source == PetAlert.Source.LOCAL || alert.source == PetAlert.Source.CALENDAR
        if (actionable || letter?.kind == LetterKind.PHRASE) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                if (actionable) {
                    Pill(stringResource(R.string.action_done), main = true, onClick = actions.onDone)
                    Pill(stringResource(R.string.action_snooze), main = false, onClick = actions.onSnooze)
                } else {
                    Pill(stringResource(R.string.action_thanks), main = true, onClick = actions.onDone)
                }
            }
        }
        if (phase.autoHideMs != null) {
            Box(
                Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth(countdown.value)
                    .height(3.dp)
                    .background(Blush, RoundedCornerShape(2.dp)),
            )
        }
    }
}

/**
 * Dos patitas redondas apoyadas sobre el borde de arriba de la tarjeta, debajo de la cara de la
 * mascota ([cx]: centro, [petW]: ancho de su ventana), con dos rayitas de dedos.
 */
private fun DrawScope.drawPaws(cx: Float, petW: Float, fill: Color, line: Color) {
    val w = petW * 0.16f
    val h = w * 0.82f
    val stroke = Stroke(1.4.dp.toPx())
    for (side in listOf(-1f, 1f)) {
        val x = cx + side * petW * 0.12f
        val tl = Offset(x - w / 2f, -h * 0.45f)
        drawOval(fill, tl, Size(w, h))
        drawOval(line, tl, Size(w, h), style = stroke)
        for (t in listOf(-0.17f, 0.17f)) {
            val tx = x + t * w
            drawLine(line, Offset(tx, tl.y + h * 0.62f), Offset(tx, tl.y + h * 0.92f), 1.2.dp.toPx(), StrokeCap.Round)
        }
    }
}

/** Botón en pastilla (Design: el principal en rosa suave, el otro con borde). */
@Composable
private fun Pill(text: String, main: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(40.dp)
            .background(if (main) BlushSoft else Color.Transparent, shape)
            .then(if (main) Modifier else Modifier.border(1.5.dp, Line, shape))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp),
    ) {
        Text(text, fontFamily = Nunito, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Cocoa)
    }
}

// ---------------------------------------------------------------- cartas a sus pies

/** Medidas de la pila de sobres (la ventana de las cartas). */
val LettersWidth: Dp = 70.dp
val LettersHeight: Dp = 46.dp

/**
 * Las cartas a los pies de la mascota: hasta 3 sobres apilados (la última arriba) y, si hay más, un
 * número. Chiquitos, para que no molesten. Tocarlos abre la de arriba. La carta nueva cae con un
 * rebote y se mueve un poco.
 */
@Composable
fun LettersContent(state: OverlayState, onOpen: () -> Unit) {
    val letters = state.letters
    if (letters.isEmpty()) return
    val shown = letters.takeLast(3)
    val newest = letters.last().id
    val drop = remember(newest) { Animatable(0f) }
    LaunchedEffect(newest) {
        drop.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 300f))
        // Un meneíto para que se note que llegó, y nada más.
        repeat(2) {
            drop.animateTo(1.12f, tween(120))
            drop.animateTo(1f, tween(160))
        }
    }
    // Sin el resaltado de "tocado": en una ventana flotante queda pegado como un recuadro gris.
    Box(Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures(onTap = { onOpen() }) }) {
        shown.forEachIndexed { i, letter ->
            val top = i == shown.lastIndex
            Canvas(
                Modifier
                    .align(Alignment.BottomStart)
                    .offset((4 + i * 7).dp, (-2 - i * 3).dp)
                    .size(38.dp, 27.dp)
                    .graphicsLayer {
                        rotationZ = if (i % 2 == 0) -6f else 4f
                        if (top) {
                            val d = drop.value
                            scaleX = d.coerceAtLeast(0f)
                            scaleY = d.coerceAtLeast(0f)
                            translationY = (1f - d.coerceAtMost(1f)) * -16.dp.toPx()
                        }
                    },
            ) { drawEnvelope(letter.kind.tone()) }
        }
        if (letters.size > 3) {
            Text(
                "+${letters.size - 3}",
                fontFamily = Nunito, fontSize = 10.sp, fontWeight = FontWeight.Black, color = Ink,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(SurfaceRaised, RoundedCornerShape(50))
                    .border(1.dp, Line, RoundedCornerShape(50))
                    .padding(horizontal = 5.dp),
            )
        }
    }
}

/** Sobre de Design (Cartas), en su viewBox de 84 × 60: papel con borde fur-deep, solapa en V y sello con el ícono. */
private fun DrawScope.drawEnvelope(tone: LetterTone) {
    val u = size.width / 84f
    // shadow-lift: una sombra corta y suave debajo del sobre.
    drawRoundRect(Ink.copy(alpha = 0.12f), Offset(2f * u, 4f * u), Size(80f * u, 56f * u), CornerRadius(8f * u))
    drawRoundRect(tone.fill, Offset(2f * u, 2f * u), Size(80f * u, 56f * u), CornerRadius(8f * u))
    drawRoundRect(FurDeep, Offset(2f * u, 2f * u), Size(80f * u, 56f * u), CornerRadius(8f * u), style = Stroke(2f * u))
    val flap = Path().apply {
        moveTo(4f * u, 6f * u)
        lineTo(42f * u, 34f * u)
        lineTo(80f * u, 6f * u)
    }
    drawPath(flap, FurDeep, style = Stroke(2f * u, join = StrokeJoin.Round))
    scale(u, u, pivot = Offset.Zero) { sealAt(tone, 1f) }
}

/** El sello solo (para la tarjeta de la carta), ocupando todo el lienzo. */
private fun DrawScope.drawSeal(tone: LetterTone) {
    val u = size.width / 24f
    scale(u, u, pivot = Offset.Zero) { translate(-30f, -22f) { sealAt(tone, 1.3f) } }
}

/** Sello redondo surface-raised con el ícono, centrado en (42, 34) del sobre. */
private fun DrawScope.sealAt(tone: LetterTone, weight: Float) {
    drawCircle(SurfaceRaised, 11f, Offset(42f, 34f))
    drawCircle(FurDeep, 11f, Offset(42f, 34f), style = Stroke(1.5f * weight))
    drawPath(tone.icon, FurDeep, style = Stroke(1.8f * weight, cap = StrokeCap.Round, join = StrokeJoin.Round))
}
