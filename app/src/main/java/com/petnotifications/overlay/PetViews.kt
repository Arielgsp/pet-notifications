package com.petnotifications.overlay

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petnotifications.PetView
import com.petnotifications.R
import com.petnotifications.art
import com.petnotifications.liesDown
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.coroutines.NonCancellable

// Colores de Design (tokens.json, tema claro): papel crema, tinta marrón cálida en vez de negra.
internal val Ink = Color(0xFF3A2E27) // ink
internal val Paper = Color(0xFFFFF6E6) // paper
internal val InkMuted = Color(0xFF6E5E52) // ink-muted
internal val Cocoa = Color(0xFF8A5536) // cocoa
private val Fur = Color(0xFFC98A55) // fur
internal val BlushSoft = Color(0xFFFBE7E3) // blush-soft
internal val Line = Color(0xFFE8DCCD) // line
internal val Blush = Color(0xFFEFA9A0) // blush
internal val SurfaceRaised = Color(0xFFFFFDF9) // surface-raised
private val BurstYellow = Color(0xFFF7E6CC) // cream

/** Proporción de los dibujos (viewport 256×240): la ventana de la mascota tiene esta forma. */
const val PET_ASPECT = 256f / 240f

/** Pasos del giro despacito: cada vista con su espejado. El frente del final lo pone la cara normal. */
private val TurnSteps = listOf(
    PetView.SIDE to false,
    PetView.BACK to false,
    PetView.SIDE to true,
    PetView.FRONT to false,
)

/**
 * La mascota en sí: respira, parpadea, rebota, se da vuelta meneándose, "habla" y duerme.
 * Los toques los maneja el servicio.
 */
@Composable
fun PetContent(state: OverlayState) {
    // Cara de frente (normal, parpadeo, feliz), vista durante el giro y pose echada.
    var face by remember { mutableStateOf(PetView.FRONT) }
    var turnView by remember { mutableStateOf<PetView?>(null) }
    var mirrored by remember { mutableStateOf(false) }
    var lying by remember { mutableStateOf(false) }
    var dozing by remember { mutableStateOf(false) }

    val squash = remember { Animatable(1f) }
    val tilt = remember { Animatable(0f) }
    val hop = remember { Animatable(0f) }
    val bounce = remember { Animatable(1f) }
    val breathe = remember { Animatable(1f) }
    val wiggle = remember { Animatable(0f) }
    /** Giro continuo con el modelo 2,5D (solo las mascotas que lo tienen): ángulo actual y si está girando. */
    val turnAngle = remember { Animatable(0f) }
    var modelTurning by remember { mutableStateOf(false) }

    /** Gira el modelo hasta [to] grados (sin cambiar de dibujo de golpe). */
    suspend fun turnModel(to: Float, ms: Int) {
        modelTurning = true
        turnAngle.animateTo(to, tween(ms, easing = FastOutSlowInEasing))
    }

    /** Deja el modelo de frente y vuelve a los dibujos fijos (aunque se haya cortado a la mitad). */
    suspend fun endModel() = withContext(NonCancellable) {
        if (modelTurning && turnAngle.value % 360f != 0f) turnAngle.animateTo(0f, tween(220))
        turnAngle.snapTo(0f)
        modelTurning = false
    }

    /** Giros, trucos y caminata usan el mismo afinado: de a uno por vez. */
    val poseLock = remember { Mutex() }
    /** Estirarse o bostezar: se alarga hacia arriba (multiplica la escala vertical). */
    val stretchY = remember { Animatable(1f) }

    /** Cambia de dibujo afinándose de costado, como en el giro (para mirar a un lado o darse vuelta). */
    suspend fun turnTo(view: PetView?, mirror: Boolean) {
        squash.animateTo(0.15f, tween(70, easing = FastOutLinearInEasing))
        turnView = view
        mirrored = mirror
        squash.animateTo(1f, tween(70, easing = LinearOutSlowInEasing))
    }

    // Giro: se afina de costado, cambia de dibujo y se vuelve a abrir, dando un pasito
    // (inclinación ±5° y saltito) en cada vista. Unos 2,4 s en total.
    LaunchedEffect(state.spinTick) {
        if (state.spinTick == 0 || state.sleeping) return@LaunchedEffect
        // Una animación de pose por vez: si dos tocan el afinado del giro a la vez, puede quedar finita.
        poseLock.withLock {
            // Con modelo: giro continuo y parejo (se dibuja en cada ángulo), con un saltito suave.
            if (PetModel.has(state.pet)) {
                try {
                    squash.snapTo(1f)
                    turnAngle.snapTo(0f)
                    modelTurning = true
                    turnAngle.animateTo(360f, tween(1_800, easing = FastOutSlowInEasing))
                } finally {
                    endModel()
                }
                face = PetView.HAPPY
                delay(700)
                if (face == PetView.HAPPY) face = PetView.FRONT
                return@LaunchedEffect
            }
            try {
                squash.snapTo(1f)
                var side = 1f
                for ((view, mirror) in TurnSteps) {
                    squash.animateTo(0.15f, tween(60, easing = FastOutLinearInEasing))
                    turnView = view
                    mirrored = mirror
                    squash.animateTo(1f, tween(60, easing = LinearOutSlowInEasing))
                    coroutineScope {
                        launch {
                            tilt.animateTo(5f * side, tween(240, easing = FastOutSlowInEasing))
                            tilt.animateTo(0f, tween(240, easing = FastOutSlowInEasing))
                        }
                        launch {
                            hop.animateTo(-4f, tween(150, easing = FastOutSlowInEasing))
                            hop.animateTo(0f, spring(dampingRatio = 0.45f, stiffness = 600f))
                        }
                    }
                    side = -side
                }
            } finally {
                turnView = null
                mirrored = false
            }
            face = PetView.HAPPY
            delay(700)
            if (face == PetView.HAPPY) face = PetView.FRONT
        }
    }

    // Mimos: salta, se estira y pone ojitos felices.
    LaunchedEffect(state.bounceTick) {
        if (state.bounceTick > 0) {
            launch {
                hop.animateTo(0f, keyframes {
                    durationMillis = 450
                    -14f at 150
                    0f at 450
                })
            }
            launch {
                bounce.animateTo(1.2f, tween(110))
                bounce.animateTo(1f, spring(dampingRatio = 0.3f, stiffness = 500f))
            }
            face = PetView.HAPPY
            delay(900)
            if (face == PetView.HAPPY) face = PetView.FRONT
        }
    }

    // Trucos del vínculo: mover la cola, dar la patita, bailar o festejar un premio.
    LaunchedEffect(state.trickTick) {
        if (state.trickTick == 0 || state.sleeping) return@LaunchedEffect
        poseLock.withLock {
            if (turnView != null) return@LaunchedEffect
            // Si el truco anterior se cortó a la mitad, se vuelve a la postura normal antes de empezar.
            stretchY.snapTo(1f)
            squash.snapTo(1f)
            tilt.snapTo(0f)
            if (!state.talking) wiggle.snapTo(0f)
            lying = false
            when (state.trick) {
                // Mueve la cola: se menea rápido de un lado al otro, ~1 s, con ojitos felices.
                Trick.WAG -> {
                    face = PetView.HAPPY
                    repeat(4) {
                        tilt.animateTo(5f, tween(110, easing = FastOutSlowInEasing))
                        tilt.animateTo(-5f, tween(110, easing = FastOutSlowInEasing))
                    }
                    tilt.animateTo(0f, tween(90))
                    if (face == PetView.HAPPY) face = PetView.FRONT
                }
                // Da la patita: la levanta, se estira un poquito y la sostiene.
                Trick.PAW -> {
                    face = PetView.PAW
                    bounce.animateTo(1.08f, tween(140))
                    bounce.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 500f))
                    delay(1_500)
                    if (face == PetView.PAW) face = PetView.FRONT
                }
                // Baila: saltitos de perfil, mirando a un lado y al otro, y termina de frente contenta.
                Trick.DANCE -> try {
                    var side = 1f
                    repeat(6) { i ->
                        turnView = PetView.SIDE
                        mirrored = i % 2 == 1
                        coroutineScope {
                            launch {
                                tilt.animateTo(8f * side, tween(160, easing = FastOutSlowInEasing))
                                tilt.animateTo(0f, tween(160, easing = FastOutSlowInEasing))
                            }
                            launch {
                                hop.animateTo(-12f, tween(160, easing = FastOutSlowInEasing))
                                hop.animateTo(0f, tween(160, easing = FastOutLinearInEasing))
                            }
                        }
                        side = -side
                    }
                } finally {
                    turnView = null
                    mirrored = false
                    face = PetView.HAPPY
                    launch {
                        bounce.animateTo(1.15f, tween(120))
                        bounce.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = 450f))
                    }
                    delay(900)
                    if (face == PetView.HAPPY) face = PetView.FRONT
                }
                // Festeja un premio: rebote mínimo y queda contenta ~2 s.
                Trick.CHEER -> {
                    face = PetView.HAPPY
                    bounce.animateTo(1.1f, tween(120))
                    bounce.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 500f))
                    delay(2_000)
                    if (face == PetView.HAPPY) face = PetView.FRONT
                }
                // ---- vida propia: cosas cortas que hace sola cada tanto
                // Curiosea: mira a un lado, al otro y vuelve.
                Trick.LOOK -> if (PetModel.has(state.pet)) try {
                    // Gira la cabeza (y el cuerpo) a medias hacia un lado y hacia el otro.
                    val side = if ((0..1).random() == 0) 1f else -1f
                    turnModel(55f * side, 450)
                    delay((600L..1_000L).random())
                    turnModel(-55f * side, 750)
                    delay((600L..1_000L).random())
                    turnModel(0f, 450)
                } finally {
                    endModel()
                } else try {
                    val first = (0..1).random() == 0
                    turnTo(PetView.SIDE, first)
                    delay((700L..1_100L).random())
                    turnTo(PetView.SIDE, !first)
                    delay((700L..1_100L).random())
                } finally {
                    withContext(NonCancellable) { turnTo(null, false) }
                }
                // Se da vuelta, se queda de espaldas un ratito y vuelve a mirarte.
                Trick.PEEK -> if (PetModel.has(state.pet)) try {
                    turnModel(180f, 900)
                    delay((1_200L..2_000L).random())
                    turnModel(360f, 900)
                } finally {
                    endModel()
                } else try {
                    turnTo(PetView.SIDE, false)
                    delay(250)
                    turnTo(PetView.BACK, false)
                    delay((1_200L..2_000L).random())
                    turnTo(PetView.SIDE, true)
                    delay(250)
                } finally {
                    withContext(NonCancellable) { turnTo(null, false) }
                }
                // Se estira: se alarga y se afina, aguanta y vuelve con un rebotito.
                Trick.STRETCH -> {
                    coroutineScope {
                        launch { stretchY.animateTo(1.12f, tween(550, easing = FastOutSlowInEasing)) }
                        launch { squash.animateTo(0.9f, tween(550, easing = FastOutSlowInEasing)) }
                    }
                    delay(450)
                    coroutineScope {
                        launch { stretchY.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 400f)) }
                        launch { squash.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 400f)) }
                    }
                    face = PetView.HAPPY
                    delay(500)
                    if (face == PetView.HAPPY) face = PetView.FRONT
                }
                // Bosteza: cierra los ojos, se estira despacito y se inclina un poco.
                Trick.YAWN -> {
                    face = PetView.BLINK
                    coroutineScope {
                        launch { stretchY.animateTo(1.07f, tween(800, easing = FastOutSlowInEasing)) }
                        launch { tilt.animateTo(-4f, tween(800, easing = FastOutSlowInEasing)) }
                    }
                    delay(700)
                    coroutineScope {
                        launch { stretchY.animateTo(1f, tween(500, easing = FastOutSlowInEasing)) }
                        launch { tilt.animateTo(0f, tween(500, easing = FastOutSlowInEasing)) }
                    }
                    if (face == PetView.BLINK) face = PetView.FRONT
                }
                // Se sienta (se echa) un ratito y se vuelve a parar con una sacudida.
                Trick.SIT -> {
                    lying = true
                    delay((2_500L..4_000L).random())
                    if (!state.sleeping) {
                        lying = false
                        bounce.animateTo(1.1f, tween(140))
                        bounce.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 450f))
                    }
                }
                // Dos saltitos de alegría.
                Trick.HOP -> {
                    face = PetView.HAPPY
                    repeat(2) {
                        hop.animateTo(-12f, tween(150, easing = FastOutSlowInEasing))
                        hop.animateTo(0f, tween(150, easing = FastOutLinearInEasing))
                    }
                    delay(300)
                    if (face == PetView.HAPPY) face = PetView.FRONT
                }
                // Se sacude o se rasca: meneo rápido y cortito.
                Trick.SHAKE -> {
                    repeat(5) {
                        wiggle.animateTo(7f, tween(45))
                        wiggle.animateTo(-7f, tween(45))
                    }
                    wiggle.animateTo(0f, tween(60))
                    face = PetView.BLINK
                    delay(200)
                    if (face == PetView.BLINK) face = PetView.FRONT
                }
            }
        }
    }

    // Camina unos pasitos (el servicio mueve la ventana): de perfil hacia donde va, con saltitos.
    LaunchedEffect(state.walkDir) {
        if (state.walkDir == 0) return@LaunchedEffect
        // Una animación de pose por vez: si dos tocan el afinado del giro a la vez, puede quedar finita.
        poseLock.withLock {
            val model = PetModel.has(state.pet)
            try {
                if (model) turnModel(if (state.walkDir < 0) -90f else 90f, 260) else turnTo(PetView.SIDE, state.walkDir < 0)
                while (true) {
                    hop.animateTo(-5f, tween(130, easing = FastOutSlowInEasing))
                    hop.animateTo(0f, tween(130, easing = FastOutLinearInEasing))
                }
            } finally {
                turnView = null
                mirrored = false
                withContext(NonCancellable) {
                    hop.snapTo(0f)
                    squash.snapTo(1f)
                }
                if (model) endModel()
            }
        }
    }

    // Se echa, al rato cierra los ojos; al despertar se estira y se para.
    LaunchedEffect(state.sleeping) {
        if (state.sleeping) {
            face = PetView.FRONT
            lying = true
            dozing = false
            delay(2_500)
            dozing = true
        } else if (lying) {
            dozing = false
            lying = false
            face = PetView.HAPPY
            bounce.animateTo(1.15f, tween(160))
            bounce.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = 400f))
            delay(400)
            if (face == PetView.HAPPY) face = PetView.FRONT
        }
    }

    // Parpadeo cada tanto: un solo cambio de dibujo, no redibuja en continuo.
    LaunchedEffect(Unit) {
        while (true) {
            delay((4_000L..7_000L).random())
            if (face == PetView.FRONT && turnView == null && !lying) {
                face = PetView.BLINK
                delay(150)
                if (face == PetView.BLINK) face = PetView.FRONT
            }
        }
    }

    // Nada de animaciones infinitas en reposo: la mascota vive siempre en pantalla y
    // redibujar a 60 fps todo el día gasta batería. Cada tanto "respira" una vez y listo;
    // dormida respira más lento y más seguido.
    LaunchedEffect(Unit) {
        while (true) {
            val asleep = state.sleeping
            delay(if (asleep) 3_000L else (7_000L..12_000L).random())
            val ms = if (asleep) 900 else 450
            breathe.animateTo(if (asleep) 1.04f else 1.06f, tween(ms, easing = FastOutSlowInEasing))
            breathe.animateTo(1f, tween(ms, easing = FastOutSlowInEasing))
        }
    }
    LaunchedEffect(state.talking) {
        if (state.talking) {
            wiggle.animateTo(0f, infiniteRepeatable(keyframes {
                durationMillis = 220
                -9f at 55
                9f at 165
            }))
        } else {
            wiggle.snapTo(0f)
        }
    }

    val view = turnView ?: when {
        lying -> if (dozing) PetView.SLEEP else PetView.LIE
        else -> face
    }
    val stretched = lying && state.pet.liesDown

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val hopPx = with(LocalDensity.current) { hop.value.dp.toPx() }
        // sombrita en el piso (quieta: no gira ni se menea con la mascota)
        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width * if (stretched) 0.547f else 0.5f
            val rx = size.width * if (stretched) 0.38f else 0.21f
            val ry = size.height * 0.03f
            drawOval(
                Color(0xFF4A3228).copy(alpha = 0.14f),
                topLeft = Offset(cx - rx, size.height * 0.95f - ry),
                size = androidx.compose.ui.geometry.Size(rx * 2, ry * 2),
            )
        }
        val petLayer = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val s = bounce.value * breathe.value
                scaleX = s * squash.value * if (mirrored) -1f else 1f
                scaleY = s * stretchY.value
                // Durante el giro continuo: saltitos e inclinación suaves, dos por vuelta.
                val spinPhase = if (modelTurning) Math.toRadians(turnAngle.value * 2.0) else 0.0
                translationY = hopPx - (kotlin.math.abs(kotlin.math.sin(spinPhase)) * 5 * density).toFloat()
                rotationZ = tilt.value + wiggle.value + (kotlin.math.sin(spinPhase) * 3).toFloat()
                // Desde los pies: se menea y respira apoyada en el piso.
                transformOrigin = TransformOrigin(0.5f, 0.95f)
            }
        if (modelTurning) {
            // Se redibuja en cada cuadro del giro (solo mientras gira).
            Canvas(petLayer) { PetModel.draw(this, state.pet, turnAngle.value, happy = false) }
        } else {
            Image(
                painterResource(state.pet.art(view)),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = petLayer,
            )
        }
        if (dozing) Snores(maxWidth, Modifier.align(Alignment.TopStart))
    }
}

/** Las zetas que suben mientras duerme; una tanda cada ~3 s y quieta en el medio. */
@Composable
private fun Snores(petWidth: androidx.compose.ui.unit.Dp, modifier: Modifier) {
    val rise = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            rise.snapTo(0f)
            rise.animateTo(1f, tween(1_800, easing = LinearOutSlowInEasing))
            delay(1_200)
        }
    }
    val density = LocalDensity.current
    val base = with(density) { (petWidth * 0.13f).toSp() }
    // Arriba de la cabeza (dormida, la cabeza queda del lado izquierdo del dibujo).
    Box(modifier.padding(start = petWidth * 0.24f, top = petWidth * 0.30f)) {
        listOf(0f to 1f, 0.25f to 1.35f, 0.5f to 1.7f).forEachIndexed { i, (delayFrac, scale) ->
            val p = ((rise.value - delayFrac) / (1f - delayFrac)).coerceIn(0f, 1f)
            Text(
                if (i == 2) "Z" else "z",
                fontSize = base * scale,
                fontWeight = FontWeight.Black,
                color = Color(0xFF6F97C6),
                modifier = Modifier
                    // Suben y se alejan hacia la izquierda (del lado de la cabeza que queda libre).
                    .offset { IntOffset((-i * 6 * density.density).roundToInt(), (-p * 18 * density.density).roundToInt()) }
                    .graphicsLayer { alpha = if (p == 0f) 0f else 1f - (p - 0.6f).coerceAtLeast(0f) / 0.4f },
            )
        }
    }
}

/**
 * Ventana de efectos arriba de la mascota: onomatopeya, globo de cómic y corazones.
 * Se "pega" por abajo a la mascota, así que todo crece hacia arriba.
 *
 * La ventana tiene alto fijo y el contenido va apoyado abajo: si la ventana cambiara de tamaño
 * (y quedara en alto 0 sin nada que mostrar), algunos teléfonos dejan pegada la última imagen
 * del globo, corrida hacia abajo y sin responder a toques.
 */
/** Lo que se puede hacer con el globo: tocarlo, mantenerlo apretado o sus botones Hecho / Posponer. */
class BubbleActions(
    val onTap: () -> Unit,
    val onLongPress: () -> Unit,
    val onDone: () -> Unit,
    val onSnooze: () -> Unit,
)

@Composable
fun FxContent(
    state: OverlayState,
    bubble: BubbleActions,
    onHeartDone: (Long) -> Unit,
    onBubbleHeight: (Int) -> Unit,
    onPlusOneDone: (Long) -> Unit = {},
) {
    // Normalmente todo va apoyado abajo (pegado a la cabeza de la mascota); si no hay lugar arriba,
    // la ventana se pone debajo de la mascota y todo va apoyado arriba (pegado a los pies).
    Box(Modifier.fillMaxSize()) {
        FxLayers(
            state, bubble, onHeartDone, onBubbleHeight, onPlusOneDone,
            Modifier.align(if (state.fxBelow) Alignment.TopStart else Alignment.BottomStart),
        )
    }
}

@Composable
private fun FxLayers(
    state: OverlayState,
    bubble: BubbleActions,
    onHeartDone: (Long) -> Unit,
    onBubbleHeight: (Int) -> Unit,
    onPlusOneDone: (Long) -> Unit,
    modifier: Modifier,
) {
    val below = state.fxBelow
    Box(modifier.fillMaxWidth()) {
        when (val phase = state.phase) {
            is FxPhase.Sound -> SoundBurst(phase, state.anchorX, below)
            is FxPhase.Bubble -> ComicBubble(phase, state.anchorX, below, bubble, onBubbleHeight)
            is FxPhase.Upcoming -> UpcomingPanel(phase, state.anchorX, below, bubble, onBubbleHeight)
            FxPhase.None -> if (state.hearts.isNotEmpty() || state.plusOnes.isNotEmpty()) Spacer(Modifier.height(150.dp))
        }
        if (state.hearts.isNotEmpty()) {
            Box(Modifier.matchParentSize()) {
                state.hearts.forEach { id -> key(id) { FloatingHeart(id, state.anchorX, below, onHeartDone) } }
            }
        }
        if (state.plusOnes.isNotEmpty()) {
            Box(Modifier.matchParentSize()) {
                state.plusOnes.forEach { id -> key(id) { PlusOne(id, state.anchorX, below, onPlusOneDone) } }
            }
        }
    }
}

@Composable
private fun SoundBurst(phase: FxPhase.Sound, anchorX: Float, below: Boolean) {
    val progress = remember(phase.id) { Animatable(0f) }
    LaunchedEffect(phase.id) { progress.animateTo(1f, spring(dampingRatio = 0.42f, stiffness = 180f)) }
    val shake = rememberInfiniteTransition(label = "shake")
    val tilt by shake.animateFloat(-6f, 6f, infiniteRepeatable(tween(90), RepeatMode.Reverse), label = "tilt")
    var burstW by remember { mutableIntStateOf(0) }
    var burstH by remember { mutableIntStateOf(0) }

    BoxWithConstraints(Modifier.fillMaxWidth().height(140.dp)) {
        val density = LocalDensity.current
        val maxW = with(density) { maxWidth.toPx() }
        val maxH = with(density) { maxHeight.toPx() }
        val rise = with(density) { 58.dp.toPx() }
        Box(
            Modifier
                .onSizeChanged { burstW = it.width; burstH = it.height }
                .offset {
                    val p = progress.value
                    val x = (anchorX - burstW / 2f).coerceIn(0f, (maxW - burstW).coerceAtLeast(0f))
                    val y = if (below) rise * p else maxH - burstH - rise * p
                    IntOffset(x.roundToInt(), y.roundToInt())
                }
                .graphicsLayer {
                    val s = 0.15f + 0.85f * progress.value
                    scaleX = s
                    scaleY = s
                    rotationZ = tilt
                    transformOrigin = TransformOrigin(0.5f, if (below) 0f else 1f)
                    alpha = progress.value.coerceIn(0f, 1f)
                }
                .drawBehind { drawStarBurst() }
                .padding(horizontal = 26.dp, vertical = 20.dp),
        ) {
            ComicText(phase.text, 30.sp)
        }
    }
}

/** Texto de la onomatopeya: relleno color pelaje con contorno marrón. */
@Composable
private fun ComicText(text: String, size: androidx.compose.ui.unit.TextUnit) {
    val base = TextStyle(fontSize = size, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
    Box {
        Text(text, style = base.copy(color = Ink, drawStyle = Stroke(width = 10f, join = StrokeJoin.Round)))
        Text(text, style = base.copy(color = Fur))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStarBurst() {
    val spikes = 14
    val cx = size.width / 2
    val cy = size.height / 2
    val path = Path()
    for (i in 0 until spikes * 2) {
        val angle = PI * i / spikes - PI / 2
        val r = if (i % 2 == 0) 1f else 0.72f
        val x = cx + cos(angle).toFloat() * cx * r
        val y = cy + sin(angle).toFloat() * cy * r
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, BurstYellow)
    drawPath(path, Ink, style = Stroke(width = 3.dp.toPx(), join = StrokeJoin.Round))
}

@Composable
private fun ComicBubble(
    phase: FxPhase.Bubble,
    anchorX: Float,
    below: Boolean,
    actions: BubbleActions,
    onHeight: (Int) -> Unit,
) {
    val appear = remember(phase.alert.key) { Animatable(0f) }
    LaunchedEffect(phase.alert.key) { appear.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 320f)) }
    val countdown = remember(phase.alert.key) { Animatable(1f) }
    LaunchedEffect(phase.alert.key, phase.autoHideMs) {
        phase.autoHideMs?.let { countdown.animateTo(0f, tween(it.toInt(), easing = LinearEasing)) }
    }

    BubbleFrame(phase.alert.key, appear.value, anchorX, below, onHeight, actions.onTap, actions.onLongPress) {
                Text(
                    when (phase.alert.source) {
                        PetAlert.Source.CALENDAR -> stringResource(R.string.bubble_calendar)
                        PetAlert.Source.LOCAL -> stringResource(R.string.bubble_reminder)
                        PetAlert.Source.TEST -> stringResource(R.string.bubble_test)
                        PetAlert.Source.APP -> "🐾 " + stringResource(R.string.app_name)
                    },
                    fontSize = 11.sp, color = InkMuted, fontWeight = FontWeight.Bold,
                )
                Text(
                    phase.alert.title,
                    fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Ink,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                )
                if (phase.alert.body.isNotBlank()) {
                    Text(
                        phase.alert.body,
                        fontSize = 14.sp, color = Ink,
                        // Los mensajes de la app (fin de la prueba) son un poco más largos.
                        maxLines = if (phase.alert.source == PetAlert.Source.APP) 6 else 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                // Los avisos de prueba no tienen nada que marcar como hecho.
                if (phase.alert.source == PetAlert.Source.LOCAL || phase.alert.source == PetAlert.Source.CALENDAR) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    ) {
                        BubbleButton(stringResource(R.string.action_snooze), filled = false, Modifier.weight(1f), actions.onSnooze)
                        BubbleButton(stringResource(R.string.action_done), filled = true, Modifier.weight(1f), actions.onDone)
                    }
                }
                // Sin ayuda de gestos en el globo: tocar la mascota lo cierra y los botones están a la vista.
                if (phase.autoHideMs != null) {
                    Box(
                        Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth(countdown.value)
                            .height(3.dp)
                            .background(Fur, RoundedCornerShape(2.dp)),
                    )
                }
    }
}

/**
 * Marco de historieta (papel, borde de tinta y colita apuntando a la mascota) que comparten el globo
 * de los avisos y el panel de próximos avisos. La colita (18 dp) se superpone 3 dp al borde para
 * "abrirlo": abajo apuntando hacia abajo, o arriba apuntando hacia arriba si va debajo de la mascota.
 */
@Composable
private fun BubbleFrame(
    key: Any,
    appear: Float,
    anchorX: Float,
    below: Boolean,
    onHeight: (Int) -> Unit,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val edge = with(density) { 26.dp.toPx() }
        val tailX = anchorX.coerceIn(edge, widthPx - edge)
        val shape = RoundedCornerShape(20.dp)
        Box(
            Modifier
                .onSizeChanged { onHeight(it.height) }
                .graphicsLayer {
                    scaleX = appear
                    scaleY = appear
                    alpha = appear.coerceIn(0f, 1f)
                    transformOrigin = TransformOrigin(tailX / widthPx, if (below) 0f else 1f)
                }
                .pointerInput(key) {
                    detectTapGestures(onTap = { onTap() }, onLongPress = { onLongPress() })
                },
        ) {
            Column(
                Modifier
                    .padding(top = if (below) 15.dp else 0.dp, bottom = if (below) 0.dp else 15.dp)
                    .fillMaxWidth()
                    .background(Paper, shape)
                    .border(3.dp, Ink, shape)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                content = content,
            )
            // Colita del globo apuntando a la mascota.
            Canvas(
                Modifier
                    .align(if (below) Alignment.TopStart else Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(18.dp),
            ) {
                val half = 11.dp.toPx()
                val baseY = if (below) size.height else 0f
                val tip = Offset(tailX - 4.dp.toPx(), if (below) 0f else size.height)
                val left = Offset(tailX - half, baseY)
                val right = Offset(tailX + half, baseY)
                val fill = Path().apply {
                    moveTo(left.x, left.y); lineTo(tip.x, tip.y); lineTo(right.x, right.y); close()
                }
                drawPath(fill, Paper)
                val stroke = 3.dp.toPx()
                drawLine(Ink, left, tip, stroke)
                drawLine(Ink, tip, right, stroke)
            }
        }
    }
}

/** Panel "próximos avisos": hasta 6 recordatorios y eventos de los próximos 7 días. Tocarlo lo cierra. */
@Composable
private fun UpcomingPanel(
    phase: FxPhase.Upcoming,
    anchorX: Float,
    below: Boolean,
    actions: BubbleActions,
    onHeight: (Int) -> Unit,
) {
    val appear = remember(phase.id) { Animatable(0f) }
    LaunchedEffect(phase.id) { appear.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 320f)) }
    BubbleFrame(phase.id, appear.value, anchorX, below, onHeight, actions.onTap, actions.onLongPress) {
        Text(
            stringResource(R.string.upcoming_title),
            fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Ink,
        )
        if (phase.items.isEmpty()) {
            Text(
                stringResource(R.string.upcoming_empty),
                fontSize = 14.sp, color = Ink, modifier = Modifier.padding(top = 4.dp),
            )
        }
        phase.items.forEach { item ->
            Row(Modifier.padding(top = 5.dp)) {
                Text(if (item.calendar) "📅" else "⏰", fontSize = 13.sp)
                Column(Modifier.padding(start = 6.dp)) {
                    Text(
                        item.title,
                        fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                    Text(com.petnotifications.ui.formatWhen(item.at), fontSize = 12.sp, color = InkMuted)
                }
            }
        }
    }
}

/** Botón del globo: el principal (Listo) en rosa suave con borde cacao; el otro, papel con borde suave. */
@Composable
private fun BubbleButton(text: String, filled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(40.dp)
            .background(if (filled) BlushSoft else Paper, shape)
            .border(2.dp, if (filled) Cocoa else Line, shape)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Cocoa)
    }
}

@Composable
private fun FloatingHeart(id: Long, anchorX: Float, below: Boolean, onDone: (Long) -> Unit) {
    val p = remember { Animatable(0f) }
    val drift = remember { (-18..18).random() }
    LaunchedEffect(id) {
        p.animateTo(1f, tween(1300, easing = FastOutSlowInEasing))
        onDone(id)
    }
    val density = LocalDensity.current
    Box(Modifier.fillMaxSize()) {
        Text(
            "❤️",
            fontSize = 30.sp,
            modifier = Modifier
                .align(if (below) Alignment.TopStart else Alignment.BottomStart)
                .offset {
                    val x = anchorX - with(density) { 18.dp.toPx() } + drift * p.value * density.density
                    // Suben desde la boca; si van debajo de la mascota, bajan desde los pies.
                    val y = with(density) { 110.dp.toPx() } * p.value * (if (below) 1f else -1f)
                    IntOffset(x.roundToInt(), y.roundToInt())
                }
                .graphicsLayer {
                    val s = if (p.value < 0.2f) p.value / 0.2f * 1.3f else 1.3f - 0.3f * p.value
                    scaleX = s
                    scaleY = s
                    alpha = 1f - (p.value - 0.6f).coerceAtLeast(0f) / 0.4f
                },
        )
    }
}

/**
 * "+1" del vínculo (Design: SubeDeNivel): pastilla crema con un corazón que sube un poquito desde la
 * mascota y se desvanece en ~1,5 s. Sin sonido.
 */
@Composable
private fun PlusOne(id: Long, anchorX: Float, below: Boolean, onDone: (Long) -> Unit) {
    val p = remember { Animatable(0f) }
    LaunchedEffect(id) {
        p.animateTo(1f, tween(1500, easing = LinearOutSlowInEasing))
        onDone(id)
    }
    val density = LocalDensity.current
    var w by remember { mutableIntStateOf(0) }
    Box(Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(if (below) Alignment.TopStart else Alignment.BottomStart)
                .onSizeChanged { w = it.width }
                .offset {
                    val x = anchorX - w / 2f
                    val rise = with(density) { (14 + 30 * p.value).dp.toPx() }
                    IntOffset(x.roundToInt(), (if (below) rise else -rise).roundToInt())
                }
                .graphicsLayer { alpha = if (p.value < 0.15f) p.value / 0.15f else 1f - ((p.value - 0.55f) / 0.45f).coerceIn(0f, 1f) }
                .background(Paper, RoundedCornerShape(50))
                .border(1.5.dp, Line, RoundedCornerShape(50))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text("❤", fontSize = 15.sp, color = Blush)
            Spacer(Modifier.width(4.dp))
            Text("+1", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Cocoa)
        }
    }
}
