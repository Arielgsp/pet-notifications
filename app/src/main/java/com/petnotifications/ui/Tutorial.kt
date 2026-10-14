package com.petnotifications.ui

import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.petnotifications.PetView
import com.petnotifications.Prefs
import com.petnotifications.R
import com.petnotifications.alerts.AlertScheduler
import com.petnotifications.art
import com.petnotifications.overlay.PetOverlayService
import com.petnotifications.overlay.PetOverlayService.Gesture
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Tarjetas del tutorial, en orden. La última es la práctica con la mascota de verdad. */
private enum class Card { TAPS, MOVE, FLICKS, ALERT, SLEEP, LOOK, BOND, TRY }

/**
 * Tutorial de lo que sabe hacer la mascota: tarjetas que se pasan deslizando, cada una con una
 * pequeña animación, y al final "Probalo vos" sobre la mascota flotante (sin acciones reales).
 * Se muestra una vez después de la configuración y se puede volver a ver desde la app.
 */
@Composable
fun TutorialScreen(onDone: () -> Unit) {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    val c = Pet.colors
    val cards = Card.entries
    val pager = rememberPagerState { cards.size }
    val scope = rememberCoroutineScope()
    fun close() {
        prefs.tutorialSeen = true
        onDone()
    }
    BackHandler {
        if (pager.currentPage > 0) scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } else close()
    }

    Column(
        Modifier.fillMaxSize().background(c.surface).safeDrawingPadding().padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.tut_header, prefs.pet.mention(ctx)),
                fontSize = 13.sp, fontWeight = FontWeight(800), color = c.inkMuted, modifier = Modifier.weight(1f),
            )
            TextButton(onClick = ::close) { Text(stringResource(R.string.tut_skip), color = c.cocoa, fontWeight = FontWeight(800)) }
        }
        HorizontalPager(pager, modifier = Modifier.weight(1f).fillMaxWidth()) { page ->
            val card = cards[page]
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(top = 8.dp).size(if (card == Card.TRY) 150.dp else 240.dp).background(c.paper, RoundedCornerShape(32.dp)).border(1.dp, c.line, RoundedCornerShape(32.dp)),
                ) { Demo(card, active = pager.currentPage == page) }
                Text(stringResource(card.title), fontSize = 24.sp, fontWeight = FontWeight(800), color = c.ink, textAlign = TextAlign.Center)
                when (card) {
                    Card.FLICKS -> FlickList()
                    Card.TRY -> Practice(active = pager.currentPage == page)
                    else -> Text(stringResource(card.body), fontSize = 16.sp, fontWeight = FontWeight(600), color = c.inkMuted, textAlign = TextAlign.Center)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 12.dp)) {
            cards.indices.forEach { i ->
                val on = i == pager.currentPage
                Box(Modifier.size(width = if (on) 20.dp else 8.dp, height = 8.dp).background(if (on) c.cocoa else c.line, CircleShape))
            }
        }
        val last = pager.currentPage == cards.lastIndex
        Button(
            onClick = { if (last) close() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
            colors = ButtonDefaults.buttonColors(containerColor = c.cocoa, contentColor = c.surface),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(52.dp),
        ) { Text(stringResource(if (last) R.string.tut_finish else R.string.tut_next), fontSize = 16.sp, fontWeight = FontWeight(800)) }
    }
}

private val Card.title get() = when (this) {
    Card.TAPS -> R.string.tut_taps_title
    Card.MOVE -> R.string.tut_move_title
    Card.FLICKS -> R.string.tut_flicks_title
    Card.ALERT -> R.string.tut_alert_title
    Card.SLEEP -> R.string.tut_sleep_title
    Card.LOOK -> R.string.tut_look_title
    Card.BOND -> R.string.tut_bond_title
    Card.TRY -> R.string.tut_try_title
}

private val Card.body get() = when (this) {
    Card.TAPS -> R.string.tut_taps_body
    Card.MOVE -> R.string.tut_move_body
    Card.FLICKS -> R.string.tut_flicks_body
    Card.ALERT -> R.string.tut_alert_body
    Card.SLEEP -> R.string.tut_sleep_body
    Card.LOOK -> R.string.tut_look_body
    Card.BOND -> R.string.tut_bond_body
    Card.TRY -> R.string.tut_try_body
}

/** Las 4 direcciones de los tirones, en el orden en que las muestra la animación. */
private val flickRows = listOf(
    Gesture.LEFT to R.string.tut_flick_left,
    Gesture.DOWN to R.string.tut_flick_down,
    Gesture.UP to R.string.tut_flick_up,
    Gesture.RIGHT to R.string.tut_flick_right,
)

private fun arrowOf(g: Gesture) = when (g) {
    Gesture.LEFT -> "←"
    Gesture.RIGHT -> "→"
    Gesture.UP -> "↑"
    Gesture.DOWN -> "↓"
    else -> ""
}

@Composable
private fun FlickList() {
    val c = Pet.colors
    Text(stringResource(R.string.tut_flicks_body), fontSize = 16.sp, fontWeight = FontWeight(600), color = c.inkMuted, textAlign = TextAlign.Center)
    PetCard(padding = 4.dp) {
        flickRows.forEachIndexed { i, (g, text) ->
            if (i > 0) RowDivider()
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp).background(c.blushSoft, CircleShape)) {
                    Text(arrowOf(g), fontSize = 18.sp, fontWeight = FontWeight(900), color = c.cocoa)
                }
                Text(stringResource(text), fontSize = 15.sp, fontWeight = FontWeight(700), color = c.ink, modifier = Modifier.padding(start = 12.dp))
            }
        }
    }
}

// ---------------------------------------------------------------- animaciones de cada tarjeta

/** Dedo que toca o desliza: un círculo `cocoa` translúcido. */
@Composable
private fun BoxScope.Finger(x: Float, y: Float, pressed: Boolean, visible: Boolean = true) {
    val c = Pet.colors
    Box(
        Modifier
            .align(Alignment.Center)
            .offset(x.dp, y.dp)
            .size(if (pressed) 30.dp else 36.dp)
            .alpha(if (visible) 1f else 0f)
            .background(c.cocoa.copy(alpha = if (pressed) 0.45f else 0.25f), CircleShape)
            .border(2.dp, c.cocoa.copy(alpha = 0.6f), CircleShape),
    )
}

@Composable
private fun Demo(card: Card, active: Boolean) {
    val ctx = LocalContext.current
    val pet = remember { Prefs(ctx).pet }
    val c = Pet.colors
    // Estado de la escena, animado por un bucle propio de cada tarjeta.
    val petX = remember { Animatable(0f) }
    val petY = remember { Animatable(0f) }
    val spin = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val fade = remember { Animatable(1f) }
    var fingerX by remember { mutableStateOf(0f) }
    var fingerY by remember { mutableStateOf(60f) }
    var pressed by remember { mutableStateOf(false) }
    var finger by remember { mutableStateOf(false) }
    var hearts by remember { mutableIntStateOf(0) }
    var view by remember { mutableStateOf(PetView.FRONT) }
    var label by remember { mutableStateOf<String?>(null) }
    var bubble by remember { mutableStateOf(false) }
    var muted by remember { mutableStateOf(false) }

    suspend fun tap(times: Int) {
        finger = true
        repeat(times) {
            pressed = true; delay(90); pressed = false; delay(140)
        }
    }

    LaunchedEffect(card, active) {
        if (!active) return@LaunchedEffect
        while (true) {
            petX.snapTo(0f); petY.snapTo(0f); spin.snapTo(0f); scale.snapTo(1f); fade.snapTo(1f)
            fingerX = 0f; fingerY = 10f; finger = false; pressed = false; view = PetView.FRONT
            label = null; bubble = false; muted = false
            when (card) {
                Card.TAPS -> {
                    label = "1"; tap(1); finger = false; hearts++; delay(1300)
                    label = "2"; tap(2); finger = false
                    petY.animateTo(-36f, tween(180)); petY.animateTo(0f, tween(220)); delay(1000)
                    label = "3"; tap(3); finger = false
                    spin.animateTo(360f, tween(900, easing = LinearEasing)); delay(1200)
                }
                Card.MOVE -> {
                    finger = true; pressed = true; delay(250)
                    for (target in listOf(-60f, 50f, 0f)) {
                        launch { petX.animateTo(target, tween(700)) }
                        val start = fingerX
                        repeat(14) { i -> fingerX = start + (target - start) * (i + 1) / 14f; delay(50) }
                        delay(200)
                    }
                    pressed = false; finger = false; delay(600)
                    // mantener apretado: abre la app
                    finger = true; pressed = true; label = "⏱"; delay(900)
                    scale.animateTo(1.12f, tween(150)); scale.animateTo(1f, tween(150))
                    pressed = false; finger = false; delay(1000)
                }
                Card.FLICKS -> for ((g, _) in flickRows) {
                    val dx = when (g) { Gesture.LEFT -> -1f; Gesture.RIGHT -> 1f; else -> 0f }
                    val dy = when (g) { Gesture.UP -> -1f; Gesture.DOWN -> 1f; else -> 0f }
                    label = arrowOf(g)
                    finger = true; pressed = true
                    repeat(6) { i -> fingerX = dx * 12f * (i + 1); fingerY = 10f + dy * 12f * (i + 1); delay(25) }
                    pressed = false; finger = false
                    petX.animateTo(dx * 18f, tween(90)); petY.animateTo(dy * 18f, tween(90))
                    launch { petX.animateTo(0f, tween(260)) }; petY.animateTo(0f, tween(260))
                    when (g) {
                        Gesture.UP -> { fade.animateTo(0f, tween(400)); delay(600); fade.animateTo(1f, tween(300)) }
                        Gesture.RIGHT -> { muted = true; delay(900) }
                        else -> delay(900)
                    }
                    muted = false; fingerX = 0f; fingerY = 10f; delay(500)
                }
                Card.ALERT -> {
                    view = PetView.HAPPY; bubble = true
                    petY.animateTo(-14f, tween(160)); petY.animateTo(0f, tween(200)); delay(2600)
                    bubble = false; view = PetView.FRONT; delay(900)
                }
                Card.SLEEP -> {
                    delay(700); view = PetView.LIE; delay(600); view = PetView.SLEEP; label = "z z z"; delay(2200)
                    finger = true; tap(1); finger = false; label = null; view = PetView.HAPPY; hearts++; delay(1300)
                }
                Card.LOOK -> {
                    scale.animateTo(1.3f, tween(700)); delay(300); scale.animateTo(0.75f, tween(900)); delay(300); scale.animateTo(1f, tween(600))
                    delay(400); fade.animateTo(0.45f, tween(600)); label = "👻"; delay(1600); fade.animateTo(1f, tween(500)); delay(600)
                }
                // Vínculo: cada "Listo" suma un corazón y, al subir de nivel, da la patita.
                Card.BOND -> {
                    bubble = true; delay(1200); bubble = false
                    finger = false; label = "+1"; view = PetView.HAPPY; hearts++; delay(1300)
                    label = "+1"; hearts++; delay(1300)
                    label = null; view = PetView.PAW
                    petY.animateTo(-10f, tween(160)); petY.animateTo(0f, tween(220)); delay(1500)
                    view = PetView.FRONT; delay(600)
                }
                Card.TRY -> {
                    view = PetView.HAPPY
                    petY.animateTo(-10f, tween(300)); petY.animateTo(0f, tween(300)); delay(1400)
                }
            }
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painterResource(pet.art(view)), contentDescription = null,
            modifier = Modifier
                .offset(petX.value.dp, petY.value.dp)
                .size(120.dp)
                .graphicsLayer {
                    rotationY = spin.value
                    scaleX = scale.value; scaleY = scale.value
                    alpha = fade.value
                    cameraDistance = 12f * density
                },
        )
        if (bubble) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp)
                    .background(c.surfaceRaised, RoundedCornerShape(16.dp)).border(1.dp, c.line, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(pet.sound(ctx), fontSize = 14.sp, fontWeight = FontWeight(900), color = c.cocoa)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                    MiniChip(stringResource(R.string.action_snooze), c.surfaceRaised)
                    MiniChip(stringResource(R.string.action_done), c.blushSoft)
                }
            }
        }
        if (muted) {
            Icon(
                Icons.AutoMirrored.Outlined.VolumeOff, contentDescription = null, tint = c.cocoa,
                modifier = Modifier.align(Alignment.TopEnd).padding(18.dp).size(28.dp),
            )
        }
        Hearts(hearts)
        label?.let {
            Text(
                it, fontSize = 20.sp, fontWeight = FontWeight(900), color = c.cocoa,
                modifier = Modifier.align(Alignment.TopStart).padding(18.dp),
            )
        }
        Finger(fingerX, fingerY, pressed, visible = finger)
    }
}

@Composable
private fun MiniChip(text: String, bg: androidx.compose.ui.graphics.Color) {
    val c = Pet.colors
    Text(
        text, fontSize = 11.sp, fontWeight = FontWeight(800), color = c.cocoa,
        modifier = Modifier.background(bg, RoundedCornerShape(50)).border(1.dp, c.line, RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

/** Corazones que suben y se desvanecen cada vez que cambia [tick]. */
@Composable
private fun BoxScope.Hearts(tick: Int) {
    if (tick == 0) return
    val rise = remember(tick) { Animatable(0f) }
    LaunchedEffect(tick) { rise.animateTo(1f, tween(1100)) }
    val c = Pet.colors
    listOf(-26f, 0f, 24f).forEachIndexed { i, x ->
        Icon(
            Icons.Outlined.Favorite, contentDescription = null, tint = c.blush,
            modifier = Modifier.align(Alignment.Center)
                .offset(x.dp, (-50 - 60 * rise.value - i * 8).dp)
                .size((18 + i * 2).dp)
                .alpha(1f - rise.value),
        )
    }
}

// ---------------------------------------------------------------- práctica con la mascota real

private val practiceRows = listOf(
    Gesture.TAP1 to R.string.tut_g_tap1,
    Gesture.TAP2 to R.string.tut_g_tap2,
    Gesture.TAP3 to R.string.tut_g_tap3,
    Gesture.LEFT to R.string.tut_g_left,
    Gesture.DOWN to R.string.tut_g_down,
    Gesture.UP to R.string.tut_g_up,
    Gesture.RIGHT to R.string.tut_g_right,
)

@Composable
private fun Practice(active: Boolean) {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    val c = Pet.colors
    val done = remember { mutableStateListOf<Gesture>() }
    var lastGesture by remember { mutableStateOf<Gesture?>(null) }
    var onScreen by remember { mutableStateOf(PetOverlayService.running) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    // Solo escucha mientras la tarjeta está visible y la app en primer plano.
    DisposableEffect(active, lifecycle) {
        val listener: (Gesture) -> Unit = { g ->
            lastGesture = g
            if (g !in done) done.add(g)
        }
        fun attach() { if (active) PetOverlayService.practice = listener }
        fun detach() { if (PetOverlayService.practice === listener) PetOverlayService.practice = null }
        val observer = LifecycleEventObserver { _, e ->
            when (e) {
                Lifecycle.Event.ON_RESUME -> { attach(); onScreen = PetOverlayService.running }
                Lifecycle.Event.ON_PAUSE -> detach()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) attach()
        onDispose {
            lifecycle.removeObserver(observer)
            detach()
        }
    }
    // El servicio tarda un instante en arrancar: se revisa cada tanto mientras se muestra la tarjeta.
    LaunchedEffect(active) {
        while (active) {
            onScreen = PetOverlayService.running
            delay(700)
        }
    }

    if (!onScreen) {
        Text(stringResource(R.string.tut_try_off), fontSize = 16.sp, fontWeight = FontWeight(600), color = c.inkMuted, textAlign = TextAlign.Center)
        if (Settings.canDrawOverlays(ctx)) {
            SoftChip(stringResource(R.string.tut_try_show)) {
                prefs.petEnabled = true
                prefs.petPausedUntil = 0L
                AlertScheduler.cancelPetResume(ctx)
                PetOverlayService.start(ctx)
            }
        }
        return
    }
    Text(stringResource(R.string.tut_try_body), fontSize = 16.sp, fontWeight = FontWeight(600), color = c.inkMuted, textAlign = TextAlign.Center)
    val shape = RoundedCornerShape(16.dp)
    Text(
        lastGesture?.let { g -> stringResource(R.string.tut_try_got, stringResource(practiceRows.first { it.first == g }.second)) }
            ?: stringResource(R.string.tut_try_waiting),
        fontSize = 16.sp, fontWeight = FontWeight(800), color = if (lastGesture != null) c.cocoa else c.inkMuted, textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).background(if (lastGesture != null) c.sageSoft else c.paper, shape).padding(12.dp),
    )
    PetCard(padding = 4.dp) {
        practiceRows.forEachIndexed { i, (g, text) ->
            if (i > 0) RowDivider()
            val ok = g in done
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(24.dp).background(if (ok) c.cocoa else c.surface, CircleShape).border(1.dp, if (ok) c.cocoa else c.line, CircleShape),
                ) { if (ok) Icon(Icons.Outlined.Check, contentDescription = null, tint = c.surface, modifier = Modifier.size(16.dp)) }
                Text(
                    stringResource(text), fontSize = 14.sp, fontWeight = FontWeight(700),
                    color = if (ok) c.ink else c.inkMuted, modifier = Modifier.padding(start = 12.dp),
                )
            }
        }
    }
    if (done.size == practiceRows.size) {
        Text(stringResource(R.string.tut_try_all), fontSize = 16.sp, fontWeight = FontWeight(800), color = c.cocoa, textAlign = TextAlign.Center)
    }
    Spacer(Modifier.width(1.dp))
}
