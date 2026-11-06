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
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petnotifications.PetView
import com.petnotifications.R
import com.petnotifications.petPainter
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
internal val BlushSoft = Color(0xFFFBE7E3) // blush-soft
internal val Line = Color(0xFFE8DCCD) // line
internal val Blush = Color(0xFFEFA9A0) // blush
internal val SurfaceRaised = Color(0xFFFFFDF9) // surface-raised

/** Cuánto se queda parada después de hacer algo antes de volver a sentarse. */
private const val SIT_AFTER_MS = 3_000L

/** Trucos que se hacen parada; el resto (cola, bostezo, sacudirse, festejar) los hace sentada. */
private val STANDING_TRICKS = setOf(Trick.PAW, Trick.DANCE, Trick.LOOK, Trick.PEEK, Trick.STRETCH, Trick.HOP)

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
    /**
     * En reposo está sentada (más tranquila para tenerla siempre a la vista). Se para para girar, saltar,
     * caminar, algunos trucos o avisar, y al rato de terminar se vuelve a sentar.
     */
    var seated by remember { mutableStateOf(true) }
    var activeAt by remember { mutableStateOf(0L) }
    fun standUp() {
        seated = false
        activeAt = System.currentTimeMillis()
    }

    val squash = remember { Animatable(1f) }
    val tilt = remember { Animatable(0f) }
    val hop = remember { Animatable(0f) }
    val bounce = remember { Animatable(1f) }
    val breathe = remember { Animatable(1f) }
    val wiggle = remember { Animatable(0f) }
    /** Giro continuo con el modelo 2,5D (solo las mascotas que lo tienen): ángulo actual y si está girando. */
    val turnAngle = remember { Animatable(0f) }
    var modelTurning by remember { mutableStateOf(false) }
    /** Girando solo la cabeza, sentada (modelo 2,5D sobre el cuerpo sentado sin cabeza). */
    val headAngle = remember { Animatable(0f) }
    var headTurning by remember { mutableStateOf(false) }

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
            standUp()
            // Con modelo: giro continuo y parejo (se dibuja en cada ángulo), con un saltito suave.
            if (PetModel.has(state.pet)) {
                try {
                    squash.snapTo(1f)
                    turnAngle.snapTo(0f)
                    modelTurning = true
                    turnAngle.animateTo(360f, tween(2_200, easing = FastOutSlowInEasing))
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
            standUp()
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
            if (state.trick in STANDING_TRICKS) standUp()
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
                // Gira solo la cabeza hacia un costado (a veces también hacia el otro), sentada.
                Trick.HEAD -> if (PetModel.hasHead(state.pet) && seated) try {
                    headTurning = true
                    val side = if ((0..1).random() == 0) 1f else -1f
                    headAngle.animateTo(55f * side, tween(520, easing = FastOutSlowInEasing))
                    delay((900L..1_600L).random())
                    if ((0..1).random() == 0) {
                        headAngle.animateTo(-50f * side, tween(850, easing = FastOutSlowInEasing))
                        delay((700L..1_200L).random())
                    }
                    headAngle.animateTo(0f, tween(480, easing = FastOutSlowInEasing))
                } finally {
                    withContext(NonCancellable) { headAngle.snapTo(0f) }
                    headTurning = false
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
            standUp()
            wiggle.animateTo(0f, infiniteRepeatable(keyframes {
                durationMillis = 220
                -9f at 55
                9f at 165
            }))
        } else {
            wiggle.snapTo(0f)
        }
    }

    // Parada sin hacer nada por un rato (y sin aviso abierto): se vuelve a sentar con un rebotito.
    LaunchedEffect(seated) {
        if (seated) return@LaunchedEffect
        while (true) {
            delay(500)
            val now = System.currentTimeMillis()
            val busy = turnView != null || modelTurning || state.talking || face == PetView.PAW || lying || state.phase !is FxPhase.None
            if (busy) activeAt = now
            if (now - activeAt >= SIT_AFTER_MS) {
                seated = true
                bounce.animateTo(0.94f, tween(90))
                bounce.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 500f))
                break
            }
        }
    }

    // Sentada: las mismas caras (normal, parpadeo, feliz) con el cuerpo sentado.
    val restFace = if (!seated) face else when (face) {
        PetView.FRONT -> PetView.SIT
        PetView.BLINK -> PetView.SIT_BLINK
        PetView.HAPPY -> PetView.SIT_HAPPY
        else -> face
    }
    val view = turnView ?: when {
        lying -> if (dozing) PetView.SLEEP else PetView.LIE
        else -> restFace
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
            Canvas(petLayer) {
                PetModel.draw(this, state.pet, turnAngle.value, coat = state.coat, outfit = state.outfit)
                PetModel.drawOutfitTurn(this, state.pet, turnAngle.value, state.outfit)
            }
        } else if (headTurning) {
            // Cuerpo sentado fijo y la cabeza dibujada en el ángulo de cada cuadro.
            Box(petLayer) {
                Image(petPainter(state.pet, PetView.SIT_BODY, state.coat), null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                Canvas(Modifier.fillMaxSize()) {
                    PetModel.drawHead(this, state.pet, headAngle.value, coat = state.coat, outfit = state.outfit)
                    PetModel.drawOutfitHead(this, state.pet, headAngle.value, state.outfit)
                }
            }
        } else {
            Image(
                petPainter(state.pet, view, state.coat, state.outfit),
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
    // Al costado de la mascota: el globo lleva la colita de costado, a la altura de la cabeza.
    CompositionLocalProvider(LocalFxSide provides (state.fxSide to state.anchorY)) { Box(modifier.fillMaxWidth()) {
        when (val phase = state.phase) {
            is FxPhase.Sound -> PlacedSticker(callWord(phase.text), phase.id, state, null)
            is FxPhase.Call -> PlacedSticker(phase.text, phase.id, state, bubble.onTap, phase.lifeMs)
            is FxPhase.Peek -> SlipNote(phase, state.fxSide, bubble, PetModel.pawColors(state.pet, state.coat), Modifier.onSizeChanged { onBubbleHeight(it.height) })
            is FxPhase.Bubble -> NoteCard(
                phase, state.who, bubble, Modifier.onSizeChanged { onBubbleHeight(it.height) },
                side = state.fxSide, anchorX = state.anchorX, held = state.held, paw = PetModel.pawColors(state.pet, state.coat),
            )
            is FxPhase.Upcoming -> UpcomingPanel(phase, state.fxSide, state.anchorY, state.anchorX, below, bubble, Modifier.onSizeChanged { onBubbleHeight(it.height) })
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
    } }
}

/**
 * La onomatopeya en su globo, con el pico hacia la mascota: al costado, pegada al borde que da a la
 * mascota; arriba, con la punta del pico sobre [OverlayState.anchorX].
 */
@Composable
private fun PlacedSticker(text: String, id: Long, state: OverlayState, onTap: (() -> Unit)?, lifeMs: Long? = null) {
    val (side, _) = LocalFxSide.current
    BoxWithConstraints(Modifier.fillMaxWidth().height(StickerHeight)) {
        val density = LocalDensity.current
        val w = stickerWidth(text)
        val wPx = with(density) { w.toPx() }
        val maxPx = with(density) { maxWidth.toPx() }
        val tipPx = with(density) { (w - 6.dp).toPx() } // punta del pico, sin espejar
        val mirrored = when {
            side > 0 -> true
            side < 0 -> false
            else -> state.anchorX - tipPx < 0f
        }
        val x = when {
            side > 0 -> 0f
            side < 0 -> maxPx - wPx
            mirrored -> state.anchorX - with(density) { 6.dp.toPx() }
            else -> state.anchorX - tipPx
        }.coerceIn(0f, (maxPx - wPx).coerceAtLeast(0f))
        StickerBubble(
            text, state.bubbleStyle, mirrored, id,
            Modifier.offset { IntOffset(x.roundToInt(), 0) }, onTap, lifeMs,
        )
    }
}

/** Lado del globo respecto de la mascota (1 a la derecha, -1 a la izquierda, 0 arriba/abajo) y altura de la cabeza. */
private val LocalFxSide = androidx.compose.runtime.compositionLocalOf { 0 to 0f }

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
