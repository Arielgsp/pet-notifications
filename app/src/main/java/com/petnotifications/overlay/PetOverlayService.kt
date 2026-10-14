package com.petnotifications.overlay

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.provider.CalendarContract
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.petnotifications.MainActivity
import com.petnotifications.PetType
import com.petnotifications.Prefs
import com.petnotifications.R
import com.petnotifications.Repeat
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import com.petnotifications.Bond
import com.petnotifications.calendar.CalendarSource
import com.petnotifications.TapAction
import com.petnotifications.alerts.AlertNotifier
import com.petnotifications.alerts.AlertReceiver
import com.petnotifications.alerts.AlertScheduler
import com.petnotifications.alerts.Insist
import com.petnotifications.alerts.toBundle
import com.petnotifications.alerts.toPetAlert
import com.petnotifications.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.hypot
import kotlin.math.abs
import kotlin.coroutines.resume
import kotlin.random.Random
import kotlin.math.roundToInt

/**
 * Mantiene a la mascota siempre en pantalla (ventanas superpuestas) y muestra los avisos que le
 * pasa el motor de alarmas (`alerts/`). Cada [TICK_MS] solo revisa si le toca dormirse.
 *
 * Secuencia de aviso: onomatopeya saliendo de la boca → globo de cómic con el texto.
 */
class PetOverlayService : Service(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var prefs: Prefs
    private lateinit var wm: WindowManager
    private val owner = OverlayOwner()
    private val state = OverlayState()
    private val main = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val dao by lazy { AppDatabase.get(this).reminders() }

    private var petView: ComposeView? = null
    private var fxView: ComposeView? = null
    private lateinit var petParams: LayoutParams
    private lateinit var fxParams: LayoutParams

    private val queue = ArrayDeque<PetAlert>()
    private var current: PetAlert? = null
    private var showJob: Job? = null

    /** Último aviso o mimo; con eso se decide cuándo se duerme. */
    private var lastActivity = System.currentTimeMillis()

    private val voice by lazy { PetVoice(this) }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        wm = getSystemService(WindowManager::class.java)
        startInForeground()
        if (!Settings.canDrawOverlays(this)) {
            Log.w(TAG, "Sin permiso para dibujar sobre otras apps")
            stopSelf()
            return
        }
        running = true
        // Si la vez anterior no pasó por onDestroy, el sistema la cerró a la fuerza: se anota al
        // recibir el primer comando (ahí se sabe quién la volvió a mostrar).
        killedAt = if (!prefs.petCleanStop && prefs.petAliveAt > 0) prefs.petAliveAt else 0L
        prefs.petCleanStop = false
        prefs.petAliveAt = System.currentTimeMillis()
        AlertScheduler.schedulePetWatchdog(this)
        lastPro = prefs.isPro
        state.pet = prefs.pet
        owner.start()
        addWindows()
        prefs.sp.registerOnSharedPreferenceChangeListener(this)
        ContextCompat.registerReceiver(
            // Exportado a propósito: USER_PRESENT es una señal protegida (solo la puede mandar el sistema)
            // y con RECEIVER_NOT_EXPORTED no llegaba.
            this, unlockReceiver, IntentFilter(Intent.ACTION_USER_PRESENT), ContextCompat.RECEIVER_EXPORTED,
        )
        ContextCompat.registerReceiver(
            this, screenOnReceiver, IntentFilter(Intent.ACTION_SCREEN_ON), ContextCompat.RECEIVER_EXPORTED,
        )
        unlockRegistered = true
        watchCalls()
        state.bounceTick++
        if (prefs.voice) voice.warmUp()
        scope.launch {
            withContext(Dispatchers.IO) { Bond.seedIfNeeded(this@PetOverlayService) }
            scheduleCelebrate(3_000)
        }
        startLife()

        scope.launch {
            while (isActive) {
                // Los avisos llegan por alarmas (alerts/); acá solo se decide si se duerme.
                evaluateSleep()
                prefs.petAliveAt = System.currentTimeMillis()
                // Terminó la prueba de Pro: se vuelve a las opciones del plan gratis.
                if (prefs.isPro != lastPro) onProChanged()
                // Antes de Android 12 no hay aviso de cambio de modo de audio: se revisa acá.
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) onAudioMode(audio.mode)
                maybeCelebrate()
                maybeRandomDance()
                delay(TICK_MS)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (killedAt > 0) {
            val by = intent?.getStringExtra(EXTRA_STARTED_BY) ?: if (intent == null) STARTED_BY_SYSTEM else STARTED_BY_APP
            prefs.petKills = prefs.petKills + "$killedAt|${System.currentTimeMillis()}|$by"
            killedAt = 0L
        }
        when (intent?.action) {
            ACTION_STOP -> {
                prefs.petEnabled = false
                stopSelf()
            }
            ACTION_TOGGLE_GHOST -> prefs.ghostMode = !prefs.ghostMode // aplica onSharedPreferenceChanged
            ACTION_PAUSE -> pausePet()
            ACTION_TEST -> if (running) enqueue(
                PetAlert(
                    key = "test:${System.currentTimeMillis()}",
                    title = getString(R.string.ob_welcome_title, state.pet.mention(this)),
                    body = getString(R.string.test_alert_body),
                    source = PetAlert.Source.TEST,
                ),
            )
            ACTION_SHOW -> if (running) intent.extras?.toPetAlert()?.let(::enqueue)
            ACTION_PLUS_ONE -> if (running) plusOne()
            ACTION_TRICK -> if (running) intent.getStringExtra(EXTRA_REWARD)
                ?.let { name -> Bond.Reward.entries.firstOrNull { it.name == name } }
                ?.let(::showReward)
            ACTION_DISMISS -> intent.getStringExtra(EXTRA_KEY)?.let { key ->
                queue.removeAll { it.key == key }
                if (current?.key == key) finishCurrent()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        running = false
        // Cierre normal (ocultar, pausar, apagar): no cuenta como cierre inesperado.
        if (::prefs.isInitialized) prefs.petCleanStop = true
        if (::prefs.isInitialized) prefs.sp.unregisterOnSharedPreferenceChangeListener(this)
        if (unlockRegistered) {
            unregisterReceiver(unlockReceiver)
            runCatching { unregisterReceiver(screenOnReceiver) }
            unwatchCalls()
        }
        probeView?.let { runCatching { wm.removeView(it) } }
        probeView = null
        hiddenNow = false
        main.removeCallbacksAndMessages(null)
        scope.cancel()
        petView?.let { runCatching { wm.removeView(it) } }
        fxView?.let { runCatching { wm.removeView(it) } }
        closeQuickNote()
        closeCard()
        walkAnim?.cancel()
        petView = null
        fxView = null
        owner.stop()
        voice.release()
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (petView == null) return
        wanderJob?.cancel()
        homeX = null
        // Al girar la pantalla (por ejemplo, un juego horizontal) se parte siempre de la posición que
        // eligió el usuario: si se acomodara la posición actual, al volver a vertical quedaría corrida.
        restorePosition()
        wm.updateViewLayout(petView, petParams)
        layoutFx()
    }

    override fun onSharedPreferenceChanged(sp: SharedPreferences?, key: String?) {
        when (key) {
            Prefs.KEY_PET -> {
                state.pet = prefs.pet
                state.bounceTick++
                layoutFx()
                updateNotification()
            }
            Prefs.KEY_VOICE -> if (prefs.voice) voice.warmUp()
            Prefs.KEY_HIDE_FULLSCREEN -> updateHidden()
            Prefs.KEY_OPACITY,
            Prefs.KEY_GHOST -> {
                applyGhost()
                petView?.let { wm.updateViewLayout(it, petParams) }
                updateNotification()
            }
            Prefs.KEY_SIZE -> {
                val size = dp(prefs.petSizeDp)
                petParams.width = size
                petParams.height = petHeight(size)
                clampPet()
                petView?.let { wm.updateViewLayout(it, petParams) }
                layoutFx()
            }
            in Prefs.SLEEP_KEYS -> evaluateSleep()
            Prefs.KEY_PRO, Prefs.KEY_FREE_PETS -> onProChanged()
            // Cambió el nombre de la mascota: se actualiza la notificación fija.
            else -> when {
                key?.startsWith(Prefs.KEY_PET_NAME) == true -> updateNotification()
                // Sumó corazones (por un "Listo" desde la notificación o la app): quizás hay que festejar.
                key?.startsWith(Bond.KEY_PREFIX) == true -> scheduleCelebrate()
            }
        }
    }

    private var lastPro = false

    /** Momento aproximado en que el sistema cerró la mascota la vez anterior (0 = no pasó). */
    private var killedAt = 0L

    /** Compró Pro, terminó la prueba o eligió sus mascotas gratis: se reaplica todo lo que depende de Pro. */
    private fun onProChanged() {
        lastPro = prefs.isPro
        listOf(Prefs.KEY_PET, Prefs.KEY_GHOST, Prefs.KEY_SIZE, Prefs.KEY_BEDTIME).forEach { onSharedPreferenceChanged(null, it) }
    }

    // ---------------------------------------------------------------- ventanas

    private fun addWindows() {
        val size = dp(prefs.petSizeDp)
        val (sw, _) = screenSize()
        val type = LayoutParams.TYPE_APPLICATION_OVERLAY
        petParams = LayoutParams(size, petHeight(size), type, BASE_FLAGS, PixelFormat.TRANSLUCENT).apply {
            // Coordenadas desde abajo-izquierda: así el globo (WRAP_CONTENT) crece hacia arriba.
            gravity = Gravity.BOTTOM or Gravity.START
            windowAnimations = R.style.PetWindowNoAnimation
        }
        applyGhost()
        restorePosition()
        fxParams = LayoutParams(
            // Alto fijo (no WRAP_CONTENT): ver FxContent.
            minOf(dp(FX_WIDTH_DP), sw - dp(16)), dp(FX_HEIGHT_DP), type,
            FX_FLAGS or LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.START
            windowAnimations = R.style.PetWindowNoAnimation
        }

        petView = ComposeView(this).apply {
            owner.attach(this)
            setContent { PetContent(state) }
            setOnTouchListener(PetTouchListener())
        }
        fxView = ComposeView(this).apply {
            owner.attach(this)
            setContent {
                FxContent(
                    state,
                    bubble = BubbleActions(
                        onTap = ::onBubbleTap,
                        onLongPress = { closeCurrent() },
                        onDone = ::onBubbleDone,
                        onSnooze = ::onBubbleSnooze,
                    ),
                    onHeartDone = {
                        state.hearts.remove(it)
                        layoutFx()
                    },
                    onBubbleHeight = { h ->
                        if (h != bubbleHeightPx) {
                            bubbleHeightPx = h
                            layoutFx()
                        }
                    },
                    onPlusOneDone = {
                        state.plusOnes.remove(it)
                        layoutFx()
                    },
                )
            }
        }
        addFullscreenProbe()
        wm.addView(petView, petParams)
        wm.addView(fxView, fxParams) // después, para quedar por encima de la mascota
        layoutFx()
    }

    /** Alto medido del globo que se está mostrando (0 = todavía no se midió). */
    private var bubbleHeightPx = 0

    /**
     * Ubica la ventana de efectos pegada a la boca (sonido/corazones) o a la cabeza (globo). Si lo
     * que se muestra no entra arriba de la mascota, va debajo de ella (con la colita hacia arriba).
     */
    private fun layoutFx() {
        val fx = fxView ?: return
        val size = petParams.width
        val height = petParams.height
        val (sw, sh) = screenSize()
        val pet = state.pet
        // El panel de próximos avisos se ubica y recibe toques igual que el globo.
        val bubble = state.phase is FxPhase.Bubble || state.phase is FxPhase.Upcoming
        fxParams.x = (petParams.x + size / 2 - fxParams.width / 2).coerceIn(0, (sw - fxParams.width).coerceAtLeast(0))
        // Arriba: el globo apunta a la coronilla (el dibujo de cuerpo entero deja ~15 % arriba de la
        // cabeza) y el sonido sale de la boca. Abajo: todo sale de los pies. Medido desde abajo.
        val anchorAbove = if (bubble) petParams.y + (height * 0.85f).roundToInt()
        else petParams.y + ((1f - pet.mouthY) * height).roundToInt()
        val anchorBelow = petParams.y + (height * 0.05f).roundToInt()
        val needed = when {
            bubble -> bubbleHeightPx.takeIf { it > 0 } ?: dp(BUBBLE_GUESS_DP)
            state.phase is FxPhase.Sound -> dp(SOUND_HEIGHT_DP)
            else -> dp(HEARTS_HEIGHT_DP)
        }
        val spaceAbove = sh - statusBarHeight() - anchorAbove
        val spaceBelow = anchorBelow - dp(NAV_MARGIN_DP)
        val below = needed > spaceAbove && spaceBelow > spaceAbove
        state.fxBelow = below
        fxParams.y = if (below) anchorBelow - fxParams.height else anchorAbove
        val anchor = if (bubble) petParams.x + size / 2f else petParams.x + pet.mouthX * size
        state.anchorX = anchor - fxParams.x
        // Solo el globo recibe toques; el resto deja pasar los toques a la app de abajo.
        fxParams.flags = (if (bubble) FX_FLAGS else FX_FLAGS or LayoutParams.FLAG_NOT_TOUCHABLE)
        // Sin nada que mostrar, la ventana queda oculta: Android 12+ bloquea los toques que pasan
        // a través de una ventana flotante visible aunque esté vacía y sea transparente.
        val empty = state.phase is FxPhase.None && state.hearts.isEmpty() && state.plusOnes.isEmpty()
        fx.visibility = if (empty || hiddenNow) View.INVISIBLE else View.VISIBLE
        wm.updateViewLayout(fx, fxParams)
    }

    /**
     * Modo fantasma: los toques atraviesan a la mascota. Desde Android 12 el sistema bloquea los
     * toques que pasan a través de una ventana flotante de otra app salvo que sea bastante
     * transparente (opacidad ≤ 0,8), así que además se la deja al 50 %.
     */
    private fun applyGhost() {
        val ghost = prefs.ghostMode
        // FLAG_WATCH_OUTSIDE_TOUCH: avisa (sin datos ni posición) que se tocó la pantalla fuera de la
        // mascota; sirve para saber si el usuario está usando el celular. No bloquea esos toques.
        petParams.flags = if (ghost) BASE_FLAGS or LayoutParams.FLAG_NOT_TOUCHABLE
        else BASE_FLAGS or LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        val opacity = prefs.petOpacity.coerceIn(40, 100) / 100f
        petParams.alpha = if (ghost) minOf(GHOST_ALPHA, opacity) else opacity
    }

    /**
     * Pone la mascota donde la dejó el usuario (o en el lugar inicial). Si la pantalla está girada
     * respecto de cuando la dejó, va al lugar equivalente: arriba a la derecha sigue arriba a la
     * derecha. Solo cambia la ventana: la posición guardada solo cambia al arrastrarla, así que al
     * volver a la orientación original queda exactamente donde estaba.
     */
    private fun restorePosition() {
        val (sw, sh) = screenSize()
        val px = prefs.petX
        val py = prefs.petY
        if (px < 0 || py < 0) {
            petParams.x = sw - petParams.width - dp(12)
            petParams.y = sh / 4
        } else {
            var ow = prefs.petScreenW
            var oh = prefs.petScreenH
            if (ow <= 0 || oh <= 0) { // guardada antes de esta versión: se asume en vertical
                ow = minOf(sw, sh)
                oh = maxOf(sw, sh)
            }
            if (ow == sw && oh == sh) {
                petParams.x = px
                petParams.y = py
            } else {
                petParams.x = (px.toFloat() / maxPetX(ow) * maxPetX(sw)).roundToInt()
                petParams.y = (py.toFloat() / maxPetY(oh) * maxPetY(sh)).roundToInt()
            }
        }
        clampPet()
    }

    private fun maxPetX(screenW: Int) = (screenW - petParams.width).coerceAtLeast(1)
    private fun maxPetY(screenH: Int) = (screenH - petParams.height - dp(32)).coerceAtLeast(1)

    private fun clampPet() {
        val (sw, sh) = screenSize()
        petParams.x = petParams.x.coerceIn(0, maxPetX(sw))
        petParams.y = petParams.y.coerceIn(0, maxPetY(sh))
    }

    private fun statusBarHeight(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            wm.currentWindowMetrics.windowInsets.getInsets(android.view.WindowInsets.Type.statusBars()).top
        } else dp(24)

    private fun screenSize(): Pair<Int, Int> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            wm.currentWindowMetrics.bounds.let { it.width() to it.height() }
        } else {
            val m = DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getRealMetrics(m)
            m.widthPixels to m.heightPixels
        }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).roundToInt()

    /** Alto de la ventana para un ancho dado: los dibujos son un poco más anchos que altos. */
    private fun petHeight(width: Int) = (width / PET_ASPECT).roundToInt()

    // ---------------------------------------------------------------- interacción con la mascota

    private var taps = 0
    /** Cuántas veces se la tocó 3 veces: alterna la vuelta y la patita. */
    private var tripleTaps = 0
    private val resolveTaps = Runnable {
        val n = taps
        taps = 0
        practice?.invoke(if (n >= 3) Gesture.TAP3 else if (n == 2) Gesture.TAP2 else Gesture.TAP1)
        // El primer mimo del día suma un corazón al vínculo.
        scope.launch(Dispatchers.IO) {
            Bond.seedIfNeeded(this@PetOverlayService)
            Bond.addPetting(this@PetOverlayService)
        }
        when {
            // Tres toques: vuelta 360°; desde que aprendió a dar la patita, una vez cada cosa.
            n >= 3 -> if (Bond.knows(this, Bond.Reward.PAW) && tripleTaps++ % 2 == 1) state.play(Trick.PAW) else state.spinTick++
            n == 2 -> state.bounceTick++ // dos toques: salta
            else -> { // un toque: corazones
                state.hearts.add(SystemClock.uptimeMillis())
                layoutFx()
            }
        }
    }

    private fun registerTap() {
        taps++
        main.removeCallbacks(resolveTaps)
        if (taps >= 3) resolveTaps.run() else main.postDelayed(resolveTaps, MULTI_TAP_WINDOW_MS)
    }

    /**
     * Tirón rápido sobre la mascota (no es un arrastre: la mascota vuelve a su lugar). Izquierda:
     * próximos avisos; abajo: recordatorio nuevo. Arriba y derecha quedan libres por ahora.
     */
    private fun onFlick(dx: Float, dy: Float) {
        wake()
        state.bounceTick++
        practice?.let { report ->
            // Práctica del tutorial: solo se cuenta el gesto, sin esconderse, silenciar ni abrir nada.
            report(
                if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) (if (dx < 0) Gesture.LEFT else Gesture.RIGHT)
                else if (dy > 0) Gesture.DOWN else Gesture.UP,
            )
            return
        }
        if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
            if (dx < 0) showUpcoming() else toggleMute()
        } else if (dy > 0) {
            showQuickNote()
        } else {
            pausePet()
        }
    }

    /** Tirón a la derecha: avisos de la app sin sonido ni voz, hasta otro tirón a la derecha. */
    private fun toggleMute() {
        prefs.muted = !prefs.muted
        toast(getString(if (prefs.muted) R.string.muted_on else R.string.muted_off))
        updateNotification()
    }

    /** "Pausar 1 hora" (notificación o tirón hacia arriba): se esconde y vuelve sola. */
    private fun pausePet() {
        val until = System.currentTimeMillis() + PAUSE_MS
        prefs.petPausedUntil = until
        AlertScheduler.schedulePetResume(this, until)
        toast(getString(R.string.pet_paused_toast))
        stopSelf()
    }

    /** Se levanta (o sigue despierta) y vuelve a contar el tiempo sin uso. */
    private fun wake() {
        lastActivity = System.currentTimeMillis()
        if (state.sleeping) state.sleeping = false
    }

    private var quickView: ComposeView? = null

    /**
     * Recordatorio rápido flotante (tirón hacia abajo): una ventana propia que sí recibe el teclado,
     * con un texto y cuándo avisar. No abre la app.
     */
    private fun showQuickNote() {
        if (quickView != null) return
        closeCurrent()
        val params = LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, LayoutParams.TYPE_APPLICATION_OVERLAY,
            // Sin FLAG_NOT_FOCUSABLE: tiene que poder escribir.
            LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT,
        ).apply {
            softInputMode = LayoutParams.SOFT_INPUT_STATE_VISIBLE or LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            windowAnimations = android.R.style.Animation_Dialog
        }
        val view = ComposeView(this).apply {
            owner.attach(this)
            setContent {
                QuickNoteCard(
                    onSave = { title, at -> saveQuickNote(title, at) },
                    onCancel = ::closeQuickNote,
                )
            }
        }
        quickView = view
        runCatching { wm.addView(view, params) }.onFailure { quickView = null }
    }

    private fun closeQuickNote() {
        quickView?.let { runCatching { wm.removeView(it) } }
        quickView = null
    }

    private fun saveQuickNote(title: String, at: Long) {
        closeQuickNote()
        wake()
        state.bounceTick++
        scope.launch {
            withContext(Dispatchers.IO) {
                val r = com.petnotifications.data.Reminder(title = title, note = "", timeMillis = at, repeat = Repeat.NONE.name, fired = false)
                AlertScheduler.schedule(this@PetOverlayService, r.copy(id = dao.insert(r)))
            }
            val time = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(java.util.Date(at))
            toast(getString(R.string.quick_saved, time))
        }
    }

    private var upcomingJob: Job? = null

    /** Muestra el panel de próximos avisos (si no hay un aviso en el globo). Se cierra solo a los 12 s. */
    private fun showUpcoming() {
        if (current != null || hiddenNow) return
        upcomingJob?.cancel()
        upcomingJob = scope.launch {
            val items = withContext(Dispatchers.IO) { upcomingItems() }
            if (current != null) return@launch
            state.phase = FxPhase.Upcoming(items, SystemClock.uptimeMillis())
            layoutFx()
            delay(UPCOMING_MS)
            closeUpcoming()
        }
    }

    private fun closeUpcoming() {
        if (state.phase !is FxPhase.Upcoming) return
        state.phase = FxPhase.None
        layoutFx()
    }

    /** Recordatorios y eventos de los próximos 7 días, los 6 primeros. */
    private suspend fun upcomingItems(limit: Int = UPCOMING_MAX): List<UpcomingItem> {
        val now = System.currentTimeMillis()
        val until = now + 7 * Prefs.DAY_MS
        val reminders = dao.pending().filter { it.timeMillis in now..until }
            .map { UpcomingItem(it.timeMillis, it.title, calendar = false) }
        val events = if (prefs.calendarEnabled && CalendarSource.hasPermission(this)) {
            runCatching {
                CalendarSource.alertsBetween(this, now, until, 0, prefs.calendarIds, now)
                    .map { UpcomingItem(it.eventBegin ?: now, it.title, calendar = true) }
            }.getOrDefault(emptyList())
        } else emptyList()
        return (reminders + events).sortedBy { it.at }.take(limit)
    }

    /**
     * Arrastrar = mover; tirón rápido = gesto; toques = gestos; mantener apretado = abrir la app.
     *
     * Para que el tirón no mueva a la mascota sin frenar el arrastre:
     * - movimiento lento: es un arrastre y la mascota sigue al dedo casi enseguida ([DRAG_DECIDE_MS]);
     * - movimiento rápido: la mascota apenas se inclina hacia ese lado ([FLICK_LEAN] del recorrido,
     *   hasta [FLICK_LEAN_MAX_DP]) y, al soltar, vuelve suave a su lugar y hace la acción;
     * - rápido pero sigue apoyado pasado [FLICK_MAX_MS]: arrastre, y la mascota alcanza al dedo con un
     *   deslizamiento suave en vez de un salto.
     */
    private inner class PetTouchListener : View.OnTouchListener {
        private var downX = 0f
        private var downY = 0f
        private var startX = 0
        private var startY = 0
        private var dragging = false
        /** El dedo se movió más que un toque, pero todavía no se decidió si es un tirón o un arrastre. */
        private var moved = false
        private var longFired = false
        private var velocity: VelocityTracker? = null
        /** Desplazamiento que se le está aplicando a la mascota (px), respecto de donde empezó el toque. */
        private var curX = 0f
        private var curY = 0f
        /** Recién empezó el arrastre después de un movimiento rápido: la mascota alcanza al dedo de a poco. */
        private var catchingUp = false
        private var settle: android.animation.ValueAnimator? = null
        private var view: View? = null

        /** La velocidad se mide con las coordenadas de la pantalla (la ventana de la mascota se mueve). */
        private fun track(e: MotionEvent) {
            val copy = MotionEvent.obtain(e).apply { setLocation(e.rawX, e.rawY) }
            (velocity ?: VelocityTracker.obtain().also { velocity = it }).addMovement(copy)
            copy.recycle()
        }

        private fun speedDp(): Float {
            val vt = velocity ?: return 0f
            vt.computeCurrentVelocity(1000)
            return hypot(vt.xVelocity, vt.yVelocity) / resources.displayMetrics.density
        }

        private fun place(x: Float, y: Float) {
            curX = x
            curY = y
            petParams.x = startX + x.roundToInt()
            petParams.y = startY - y.roundToInt()
            clampPet()
            view?.let { wm.updateViewLayout(it, petParams) }
            layoutFx()
        }

        /** Vuelve suave a donde estaba (después de inclinarse en un tirón). */
        private fun settleBack() {
            val fromX = curX
            val fromY = curY
            if (fromX == 0f && fromY == 0f) return
            settle = android.animation.ValueAnimator.ofFloat(1f, 0f).apply {
                duration = SETTLE_MS
                interpolator = android.view.animation.DecelerateInterpolator()
                addUpdateListener { place(fromX * (it.animatedValue as Float), fromY * (it.animatedValue as Float)) }
                start()
            }
        }

        private val slop = ViewConfiguration.get(this@PetOverlayService).scaledTouchSlop
        private val longPress = Runnable {
            longFired = true
            state.bounceTick++
            openApp(null)
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, e: MotionEvent): Boolean {
            view = v
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    settle?.cancel()
                    touching = true
                    wanderJob?.cancel()
                    lastActivity = System.currentTimeMillis()
                    downX = e.rawX
                    downY = e.rawY
                    startX = petParams.x
                    startY = petParams.y
                    curX = 0f
                    curY = 0f
                    dragging = false
                    moved = false
                    catchingUp = false
                    longFired = false
                    velocity?.clear()
                    track(e)
                    main.postDelayed(longPress, LONG_PRESS_MS)
                }
                MotionEvent.ACTION_MOVE -> {
                    track(e)
                    val dx = e.rawX - downX
                    val dy = e.rawY - downY
                    if (!moved && hypot(dx, dy) > slop) {
                        moved = true
                        main.removeCallbacks(longPress)
                    }
                    if (moved && !dragging) {
                        val elapsed = e.eventTime - e.downTime
                        val slow = elapsed >= DRAG_DECIDE_MS && speedDp() < FLICK_MIN_SPEED_DP
                        if (slow || elapsed >= FLICK_MAX_MS) {
                            dragging = true
                            catchingUp = true
                        } else {
                            // Todavía puede ser un tirón: solo se inclina un poquito hacia ese lado.
                            val max = dp(FLICK_LEAN_MAX_DP).toFloat()
                            place((dx * FLICK_LEAN).coerceIn(-max, max), (dy * FLICK_LEAN).coerceIn(-max, max))
                        }
                    }
                    if (dragging) {
                        if (catchingUp) {
                            // Alcanza al dedo en unos pocos cuadros, sin salto.
                            val nx = curX + (dx - curX) * CATCH_UP
                            val ny = curY + (dy - curY) * CATCH_UP
                            if (hypot(dx - nx, dy - ny) < dp(2)) catchingUp = false
                            place(if (catchingUp) nx else dx, if (catchingUp) ny else dy)
                        } else {
                            place(dx, dy)
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    main.removeCallbacks(longPress)
                    track(e)
                    val dx = e.rawX - downX
                    val dy = e.rawY - downY
                    if (moved && !dragging) {
                        // Tirón: se reconoce por la velocidad, no por el recorrido, así funciona también
                        // con la mascota pegada al borde (el dedo tiene poco lugar hacia ese lado).
                        val flick = speedDp() >= FLICK_MIN_SPEED_DP && hypot(dx, dy) >= dp(FLICK_MIN_DP)
                        settleBack()
                        if (flick) onFlick(dx, dy)
                    } else if (dragging) {
                        wake()
                        homeX = null // la dejó en otro lugar: ese es su lugar ahora
                        val (sw, sh) = screenSize()
                        prefs.petX = petParams.x
                        prefs.petY = petParams.y
                        prefs.petScreenW = sw
                        prefs.petScreenH = sh
                    } else if (!longFired) {
                        // Dormida, el primer toque solo la despierta.
                        // Con un aviso o el panel en pantalla, tocar la mascota los cierra.
                        if (closeCurrent()) Unit
                        else if (state.sleeping) wake() else registerTap()
                    }
                    touching = false
                    returnHomeLater()
                }
                MotionEvent.ACTION_CANCEL -> {
                    main.removeCallbacks(longPress)
                    velocity?.clear()
                    if (!dragging) settleBack()
                    touching = false
                    returnHomeLater()
                }
                // Toque en cualquier otro lado de la pantalla: el usuario está usando el celular, así
                // que no se acuesta (no la levanta si ya duerme; para eso hay que tocarla).
                MotionEvent.ACTION_OUTSIDE -> if (!state.sleeping) lastActivity = System.currentTimeMillis()
            }
            return true
        }
    }

    // ---------------------------------------------------------------- avisos

    /** Avisos que la mascota ya dijo en esta sesión: al desbloquear no se repiten dos veces. */
    private val shownKeys = mutableSetOf<String>()
    private var unlockRegistered = false

    /**
     * Al desbloquear, la mascota repite los avisos que sonaron con la pantalla apagada y siguen sin
     * atender (su notificación sigue ahí). Tocar "Hecho" o el globo los saca.
     */
    private val unlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!inDnd()) wake()
            repeatUnattended()
            scope.launch {
                delay(UNLOCK_GREET_DELAY_MS)
                greetOnUnlock()
            }
            // Lo que se ganó con el celular bloqueado (por ejemplo, "Listo" en la notificación) se festeja ahora.
            scheduleCelebrate(UNLOCK_CELEBRATE_DELAY_MS)
        }
    }

    /** Repite (según la opción "Al desbloquear") los avisos que sonaron sin que la mascota los dijera. */
    private fun repeatUnattended() {
        if (hiddenNow) return
        getSystemService(NotificationManager::class.java).activeNotifications
                .filter { it.notification.channelId == AlertNotifier.CHANNEL_LOUD }
                .sortedBy { it.postTime }
                .mapNotNull { it.notification.extras.toPetAlert() }
                .filter { it.key !in shownKeys }
                .let { pending ->
                    val limit = prefs.unlockRepeat
                    if (limit == Prefs.UNLOCK_REPEAT_ALL) pending else pending.takeLast(limit.coerceAtLeast(0))
                }
                .forEach(::enqueue)
    }

    // ---------------------------------------------------------------- vida propia

    /**
     * Para que no parezca un dibujo quieto, cada tanto hace algo sola: mira a los costados, se da
     * vuelta, se estira, bosteza, se sienta, salta, se sacude, mueve la cola o da unos pasitos y
     * vuelve a su lugar. Cada cuánto lo elige el usuario ("Qué tan inquieta es"). Solo con la pantalla
     * prendida y desbloqueada, despierta y sin nada abierto; no cuenta como uso (igual se duerme).
     */
    private fun startLife() {
        scope.launch {
            while (isActive) {
                val (from, to) = when (prefs.liveliness) {
                    Prefs.LIVELY_CALM -> 60_000L to 120_000L
                    Prefs.LIVELY_ACTIVE -> 15_000L to 30_000L
                    else -> 30_000L to 60_000L
                }
                delay(Random.nextLong(from, to))
                if (canLive()) live()
            }
        }
    }

    private var touching = false
    private var lastLife: Trick? = null

    private fun canLive(): Boolean =
        petView?.visibility == View.VISIBLE && !hiddenNow && !state.sleeping && !touching &&
            current == null && queue.isEmpty() && state.phase is FxPhase.None && state.card == null &&
            quickView == null && wanderJob?.isActive != true && homeX == null && canShowAlert(this)

    private fun live() {
        val options = buildList {
            addAll(listOf(Trick.LOOK, Trick.LOOK, Trick.PEEK, Trick.STRETCH, Trick.YAWN, Trick.SIT, Trick.HOP, Trick.SHAKE, Trick.WAG))
            if (Bond.knows(this@PetOverlayService, Bond.Reward.PAW)) add(Trick.PAW)
        }.filter { it != lastLife }
        // Pasitos: más seguido cuanto más inquieta.
        val walkOdds = when (prefs.liveliness) {
            Prefs.LIVELY_CALM -> 6
            Prefs.LIVELY_ACTIVE -> 3
            else -> 4
        }
        if (Random.nextInt(walkOdds) == 0) {
            lastLife = null
            wander()
            return
        }
        // De vez en cuando se persigue la cola (la vuelta de los 3 toques).
        if (Random.nextInt(12) == 0) {
            state.spinTick++
            return
        }
        val t = options.random()
        lastLife = t
        state.play(t)
    }

    private var wanderJob: Job? = null
    private var walkAnim: ValueAnimator? = null
    /** Dónde estaba antes de salir a caminar (null: está en su lugar). */
    private var homeX: Int? = null

    /** Da unos pasitos hacia el lado con más lugar, mira un poco y vuelve a su lugar. */
    private fun wander() {
        val (sw, _) = screenSize()
        val home = petParams.x
        val left = home
        val right = maxPetX(sw) - home
        val want = dp(Random.nextInt(WALK_MIN_DP, WALK_MAX_DP))
        val dir = when {
            left >= want && right >= want -> if (Random.nextBoolean()) 1 else -1
            right > left -> 1
            else -> -1
        }
        val dist = minOf(want, if (dir > 0) right else left)
        if (dist < dp(WALK_MIN_DP / 2)) {
            state.play(Trick.LOOK)
            return
        }
        homeX = home
        wanderJob = scope.launch {
            walkTo(home + dir * dist)
            delay(Random.nextLong(600, 1_400))
            if (Random.nextBoolean()) {
                state.play(Trick.LOOK) // dura hasta ~2,6 s; se espera a que termine antes de volver
                delay(3_200)
            }
            walkTo(home)
            homeX = null
        }
    }

    /** Si quedó lejos de su lugar (un toque cortó el paseo), vuelve caminando al ratito. */
    private fun returnHomeLater() {
        val home = homeX ?: return
        wanderJob?.cancel()
        wanderJob = scope.launch {
            delay(RETURN_HOME_MS)
            if (touching) return@launch
            walkTo(home)
            homeX = null
        }
    }

    /** Camina hasta [targetX] moviendo la ventana, de perfil y a saltitos (~60 dp/s). */
    private suspend fun walkTo(targetX: Int) {
        val view = petView ?: return
        val from = petParams.x
        if (from == targetX) return
        state.walkDir = if (targetX > from) 1 else -1
        try {
            suspendCancellableCoroutine { cont ->
                val anim = ValueAnimator.ofInt(from, targetX).apply {
                    duration = (abs(targetX - from) / resources.displayMetrics.density / WALK_DP_PER_S * 1000).toLong()
                    interpolator = android.view.animation.LinearInterpolator()
                    addUpdateListener {
                        petParams.x = it.animatedValue as Int
                        runCatching { wm.updateViewLayout(view, petParams) }
                        layoutFx()
                    }
                    addListener(object : android.animation.AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: android.animation.Animator) {
                            if (cont.isActive) cont.resume(Unit)
                        }
                    })
                }
                walkAnim = anim
                cont.invokeOnCancellation { main.post { anim.cancel() } }
                anim.start()
            }
        } finally {
            walkAnim = null
            state.walkDir = 0
        }
    }

    // ---------------------------------------------------------------- vínculo

    /** "+1" que sube desde la mascota al sumar un corazón con "Listo". */
    private fun plusOne() {
        if (hiddenNow) return
        state.plusOnes.add(SystemClock.uptimeMillis())
        layoutFx()
    }

    private var celebrateJob: Job? = null
    private var cardJob: Job? = null
    private var cardView: ComposeView? = null
    private var lastWagAt = 0L

    /** Revisa en un ratito si hay algo para festejar (así primero se ve el "+1"). */
    private fun scheduleCelebrate(delayMs: Long = CELEBRATE_DELAY_MS) {
        celebrateJob?.cancel()
        celebrateJob = scope.launch {
            delay(delayMs)
            maybeCelebrate()
        }
    }

    /**
     * Festeja lo pendiente del vínculo si se puede ahora: nunca encima de un aviso, ni con la
     * pantalla bloqueada, en No molestar o con los avisos en silencio (espera al siguiente
     * desbloqueo). Con la app abierta lo muestra la app.
     */
    private fun maybeCelebrate() {
        if (state.card != null || current != null || queue.isNotEmpty() || state.phase !is FxPhase.None) return
        if (!canShowAlert(this) || inDnd() || prefs.muted || appVisible) return
        val c = Bond.pending(this) ?: return
        Bond.markCelebrated(this, c)
        wake()
        val level = when (c) {
            is Bond.Celebration.LevelUp -> c.level
            is Bond.Celebration.Welcome -> c.level
            is Bond.Celebration.Arrived -> c.level
        }
        val chip = getString(R.string.bond_chip, level.number, getString(level.name))
        val card = when (c) {
            is Bond.Celebration.Welcome -> BondCard.Celebrate(
                SystemClock.uptimeMillis(), chip, getString(R.string.bond_welcome_title), getString(level.title),
                getString(R.string.bond_see_path), getString(R.string.bond_later),
            )
            else -> BondCard.Celebrate(
                SystemClock.uptimeMillis(), chip, getString(level.reward.learned), getString(level.reward.howTo),
                getString(R.string.bond_show_me), getString(R.string.bond_later),
            )
        }
        // Se pone contenta y le salen corazoncitos en abanico.
        state.play(Trick.CHEER)
        heartFan(5)
        showCard(card, CARD_MS) { primary ->
            if (!primary) return@showCard
            if (c is Bond.Celebration.Welcome) openApp(null, bondPath = true) else showReward(level.reward)
        }
    }

    private fun heartFan(n: Int) {
        val now = SystemClock.uptimeMillis()
        repeat(n) { i ->
            main.postDelayed({
                state.hearts.add(now + i)
                layoutFx()
            }, i * 120L)
        }
    }

    /** "Mostrame": hace el truco, saluda, o festeja el premio si es un accesorio. */
    private fun showReward(reward: Bond.Reward) {
        if (hiddenNow) return
        wake()
        when (reward) {
            Bond.Reward.WAG -> state.play(Trick.WAG)
            Bond.Reward.PAW -> state.play(Trick.PAW)
            Bond.Reward.DANCE -> state.play(Trick.DANCE)
            Bond.Reward.MORNING -> scope.launch { morning(force = true) }
            else -> {
                state.play(Trick.CHEER)
                heartFan(3)
            }
        }
    }

    /** Al desbloquear: saludo de buenos días (una vez por día) o, si no, mueve la cola (como mucho cada 1 h). */
    private suspend fun greetOnUnlock() {
        if (hiddenNow || inDnd() || current != null || queue.isNotEmpty() || !canShowAlert(this)) return
        if (Bond.knows(this, Bond.Reward.MORNING) && morningDue()) {
            morning(force = false)
            return
        }
        val now = System.currentTimeMillis()
        if (now - lastWagAt >= WAG_EVERY_MS) {
            lastWagAt = now
            state.play(Trick.WAG)
        }
    }

    /** Primer desbloqueo del día después del horario de No molestar (o de las 6:00), antes del mediodía. */
    private fun morningDue(): Boolean {
        if (Bond.greetedToday(this)) return false
        val cal = Calendar.getInstance()
        val minute = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val start = if (prefs.sleepAtBedtime) prefs.bedtimeEndMin else MORNING_START_MIN
        val end = if (start < NOON_MIN) NOON_MIN else start + 3 * 60
        return minute in start until end
    }

    /**
     * "¡Buen día!": la onomatopeya (sin voz si los avisos están en silencio) y una tarjeta chica con
     * lo que hay hoy. Tocarla abre la app; se va sola a los 6 s.
     */
    private suspend fun morning(force: Boolean) {
        if (!force) Bond.markGreeted(this)
        val now = System.currentTimeMillis()
        val endOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis
        val today = withContext(Dispatchers.IO) { upcomingItems(limit = Int.MAX_VALUE) }.count { it.at in now..endOfDay }
        state.talking = true
        state.phase = FxPhase.Sound(state.pet.sound(this), SystemClock.uptimeMillis())
        layoutFx()
        speak(state.pet)
        delay(SOUND_MS)
        state.talking = false
        if (state.phase is FxPhase.Sound) {
            state.phase = FxPhase.None
            layoutFx()
        }
        val text = when (today) {
            0 -> getString(R.string.bond_morning_none)
            1 -> getString(R.string.bond_morning_one)
            else -> getString(R.string.bond_morning_some, today)
        }
        showCard(BondCard.Morning(SystemClock.uptimeMillis(), text), MORNING_CARD_MS, onTap = { openApp(null) }) { }
    }

    /** Baile suelto de vez en cuando (máximo 2 por día), nunca con un aviso abierto. */
    private fun maybeRandomDance() {
        if (!Bond.knows(this, Bond.Reward.DANCE) || state.sleeping || current != null || queue.isNotEmpty()) return
        if (state.phase !is FxPhase.None || state.card != null || !canShowAlert(this) || inDnd()) return
        if (Bond.dancesToday(this) >= 2 || Random.nextInt(RANDOM_DANCE_ODDS) != 0) return
        Bond.markDance(this)
        state.play(Trick.DANCE)
    }

    /** Tarjeta flotante centrada arriba (ventana propia que solo recibe toques sobre la tarjeta). */
    private fun showCard(card: BondCard, autoCloseMs: Long, onTap: () -> Unit = {}, onButton: (primary: Boolean) -> Unit) {
        closeCard()
        state.card = card
        val view = ComposeView(this).apply {
            owner.attach(this)
            setContent {
                state.card?.let {
                    BondCardContent(
                        it,
                        // Se cierra después de terminar de procesar el toque (sacar la ventana en
                        // medio de su propio clic puede perder la acción).
                        CardActions(
                            onPrimary = { main.post { closeCard(); onButton(true) } },
                            onSecondary = { main.post { closeCard(); onButton(false) } },
                            onTap = { main.post { closeCard(); onTap() } },
                        ),
                    )
                }
            }
        }
        val lp = LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, LayoutParams.TYPE_APPLICATION_OVERLAY,
            LayoutParams.FLAG_NOT_FOCUSABLE or LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = statusBarHeight() + dp(8)
        }
        runCatching { wm.addView(view, lp) }.onSuccess { cardView = view }
        cardJob = scope.launch {
            delay(autoCloseMs)
            closeCard()
        }
    }

    private fun closeCard() {
        cardJob?.cancel()
        cardJob = null
        state.card = null
        cardView?.let { runCatching { wm.removeView(it) } }
        cardView = null
    }

    // ---------------------------------------------------------------- llamadas

    /**
     * Durante una llamada (o mientras suena una entrante) la mascota se esconde para no tapar la
     * pantalla de atender ni la de la llamada, y no habla. Se detecta con el modo de audio del
     * sistema, que no requiere el permiso del teléfono (READ_PHONE_STATE).
     */
    private var inCall = false
    private val audio by lazy { getSystemService(AudioManager::class.java) }
    private val modeListener: Any? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AudioManager.OnModeChangedListener { mode -> main.post { onAudioMode(mode) } }
        } else null
    }

    private fun watchCalls() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audio.addOnModeChangedListener(mainExecutor, modeListener as AudioManager.OnModeChangedListener)
        }
        onAudioMode(audio.mode)
    }

    private fun unwatchCalls() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runCatching { audio.removeOnModeChangedListener(modeListener as AudioManager.OnModeChangedListener) }
        }
    }

    private fun onAudioMode(mode: Int) {
        val call = mode == AudioManager.MODE_IN_CALL ||
            mode == AudioManager.MODE_IN_COMMUNICATION ||
            mode == AudioManager.MODE_RINGTONE
        if (call == inCall) return
        inCall = call
        updateHidden()
    }

    // ---------------------------------------------------------------- pantalla completa

    /**
     * Otra app a pantalla completa (video, juego, cámara): la mascota se esconde. Se detecta con una
     * ventana "sonda" invisible de 1 px de ancho y todo el alto, que recibe del sistema si la barra
     * de estado está visible. Es totalmente transparente y no recibe toques, así que no molesta.
     * (El teclado abierto de otra app no se puede detectar así: Android solo se lo avisa a esa app.)
     */
    private var fullscreen = false
    private var probeView: View? = null

    private fun addFullscreenProbe() {
        val probe = View(this)
        probe.setOnApplyWindowInsetsListener { _, insets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                onFullscreen(!insets.isVisible(android.view.WindowInsets.Type.statusBars()))
            }
            insets
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            @Suppress("DEPRECATION")
            probe.setOnSystemUiVisibilityChangeListener { vis ->
                onFullscreen(vis and View.SYSTEM_UI_FLAG_FULLSCREEN != 0)
            }
        }
        val lp = LayoutParams(
            1, LayoutParams.MATCH_PARENT, LayoutParams.TYPE_APPLICATION_OVERLAY,
            BASE_FLAGS or LayoutParams.FLAG_NOT_TOUCHABLE or LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.START or Gravity.TOP
            alpha = 0f
            windowAnimations = R.style.PetWindowNoAnimation
        }
        runCatching { wm.addView(probe, lp) }.onSuccess { probeView = probe }
    }

    private fun onFullscreen(value: Boolean) {
        if (value == fullscreen) return
        fullscreen = value
        updateHidden()
    }

    /** Escondida por una llamada o por otra app a pantalla completa (si el usuario lo dejó activado). */
    private fun updateHidden() {
        val hide = inCall || (fullscreen && prefs.hideInFullscreen)
        if (hide == hiddenNow) return
        hiddenNow = hide
        petView?.visibility = if (hide) View.INVISIBLE else View.VISIBLE
        layoutFx()
        // Al volver, repite lo que sonó mientras estaba escondida y nadie atendió.
        if (!hide) repeatUnattended()
    }

    private fun enqueue(alert: PetAlert) {
        if (current?.key == alert.key || queue.any { it.key == alert.key }) return
        lastActivity = System.currentTimeMillis()
        queue.addLast(alert)
        if (current == null) showNext()
    }

    private fun showNext() {
        val alert = queue.removeFirstOrNull() ?: return
        current = alert
        shownKeys += alert.key
        showJob = scope.launch {
            // 0) Si estaba dormida, primero se despierta y se para
            if (state.sleeping) {
                state.sleeping = false
                delay(WAKE_MS)
            }
            // 1) Onomatopeya desde la boca
            state.talking = true
            state.phase = FxPhase.Sound(state.pet.sound(this@PetOverlayService), SystemClock.uptimeMillis())
            layoutFx()
            speak(state.pet)
            delay(SOUND_MS)
            // 2) Globo de cómic
            state.talking = false
            val autoHideMs = if (prefs.autoHide) prefs.autoHideSeconds * 1000L else null
            state.phase = FxPhase.Bubble(alert, autoHideMs, hint())
            layoutFx()
            if (autoHideMs != null) {
                delay(autoHideMs)
                finishCurrent()
            }
        }
    }

    // ---------------------------------------------------------------- hora de dormir

    /**
     * Se duerme tras [Prefs.sleepIdleHours] sin avisos ni mimos, o en el horario de sueño del
     * usuario (ahí basta un ratito quieta, así un toque la despierta sin que se vuelva a dormir enseguida).
     */
    /**
     * Fuera del horario de No molestar se acuesta a dormir tras [IDLE_SLEEP_MS] sin uso (sin tocarla,
     * sin tocar la pantalla, sin avisos). En el horario de No molestar duerme siempre: un aviso o un
     * toque la despiertan y al ratito ([DND_GRACE_MS]) se vuelve a dormir.
     */
    private fun evaluateSleep() {
        if (current != null || queue.isNotEmpty()) return
        val now = System.currentTimeMillis()
        val idle = now - lastActivity
        val sleepy = if (inDnd(now)) idle >= DND_GRACE_MS else idle >= IDLE_SLEEP_MS
        if (sleepy != state.sleeping) state.sleeping = sleepy
    }

    private fun inDnd(now: Long = System.currentTimeMillis()) = prefs.sleepAtBedtime && isBedtime(now)

    /** Prender la pantalla la levanta, salvo en el horario de No molestar. */
    private val screenOnReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!inDnd()) wake()
        }
    }

    private fun isBedtime(now: Long): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        val minute = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val start = prefs.bedtimeStartMin
        val end = prefs.bedtimeEndMin
        return when {
            start == end -> false
            start < end -> minute in start until end
            else -> minute >= start || minute < end // cruza la medianoche (ej. 22:00 a 07:00)
        }
    }

    private fun hint(): String {
        val action = prefs.tapAction
        return if (action == TapAction.DISMISS) getString(R.string.hint_close)
        else getString(R.string.hint_action, getString(action.labelRes).lowercase())
    }

    private fun finishCurrent() {
        val job = showJob
        showJob = null
        current = null
        state.talking = false
        state.phase = FxPhase.None
        layoutFx()
        scope.launch {
            delay(700)
            if (current == null) showNext()
            if (current == null) scheduleCelebrate()
        }
        job?.cancel()
    }

    /** "Hecho": festeja (salto, ojitos felices y corazón) y deja de insistir. */
    private fun onBubbleDone() {
        val alert = current ?: return
        AlertNotifier.cancel(this, alert)
        finishCurrent()
        state.hearts.add(SystemClock.uptimeMillis())
        layoutFx()
        state.bounceTick++
        scope.launch {
            val gained = withContext(Dispatchers.IO) { Insist.done(this@PetOverlayService, alert) }
            if (gained) plusOne()
            // Recompensa variable: 1 de cada 5 "Listo" baila (si ya aprendió).
            if (Bond.knows(this@PetOverlayService, Bond.Reward.DANCE) && Random.nextInt(5) == 0) {
                delay(700)
                state.play(Trick.DANCE)
            }
        }
    }

    private fun onBubbleSnooze() {
        val alert = current ?: return
        AlertNotifier.cancel(this, alert)
        finishCurrent()
        stopInsisting(alert)
        snooze(alert)
    }

    private fun stopInsisting(alert: PetAlert) {
        scope.launch(Dispatchers.IO) { Insist.cancel(this@PetOverlayService, alert.key) }
    }

    /**
     * Cierra lo que esté mostrando la mascota: el aviso (onomatopeya o globo, como mantener apretado
     * el globo: deja de insistir) o el panel de próximos avisos. Devuelve false si no había nada.
     */
    private fun closeCurrent(): Boolean {
        if (state.phase is FxPhase.Upcoming) {
            upcomingJob?.cancel()
            closeUpcoming()
            return true
        }
        val alert = current ?: return false
        AlertNotifier.cancel(this, alert)
        stopInsisting(alert)
        finishCurrent()
        return true
    }

    private fun onBubbleTap() {
        if (state.phase is FxPhase.Upcoming) return closeUpcoming()
        val alert = current ?: return
        AlertNotifier.cancel(this, alert)
        stopInsisting(alert)
        val action = prefs.tapAction
        finishCurrent()
        // Los mensajes de la app (fin de la prueba, resumen de lo atrasado) siempre se abren.
        if (alert.source == PetAlert.Source.APP) {
            open(alert)
            return
        }
        when (action) {
            TapAction.OPEN -> open(alert)
            TapAction.DELETE -> delete(alert)
            TapAction.SNOOZE -> snooze(alert)
            TapAction.DISMISS -> Unit
        }
    }

    private fun open(alert: PetAlert) {
        if (alert.source == PetAlert.Source.CALENDAR && alert.eventId != null) {
            val intent = Intent(
                Intent.ACTION_VIEW,
                ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, alert.eventId),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            alert.eventBegin?.let { intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, it) }
            if (runCatching { startActivity(intent) }.isSuccess) return
        }
        openApp(alert.reminderId, showMissed = com.petnotifications.alerts.MissedSummary.isSummary(alert))
    }

    private fun openApp(reminderId: Long?, showMissed: Boolean = false, bondPath: Boolean = false) {
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        reminderId?.let { intent.putExtra(EXTRA_REMINDER_ID, it) }
        if (showMissed) intent.putExtra(EXTRA_SHOW_MISSED, true)
        if (bondPath) intent.putExtra(EXTRA_BOND_PATH, true)
        startActivity(intent)
    }

    private fun delete(alert: PetAlert) {
        when (alert.source) {
            // Uno que se repite no se borra desde el globo (sería borrar toda la serie sin preguntar):
            // se cierra este aviso y se explica cómo borrarlo desde la app.
            PetAlert.Source.LOCAL -> if (alert.repeating) toast(getString(R.string.toast_series_kept)) else {
                val id = alert.reminderId ?: return
                AlertScheduler.cancel(this, id)
                scope.launch(Dispatchers.IO) { dao.delete(id) }
                toast(getString(R.string.toast_deleted))
            }
            PetAlert.Source.CALENDAR -> toast(getString(R.string.toast_event_dismissed))
            PetAlert.Source.TEST, PetAlert.Source.APP -> Unit
        }
    }

    private fun snooze(alert: PetAlert) {
        val minutes = prefs.snoozeMinutes
        // Con alarma y guardado: sobrevive aunque se cierre el servicio, la app o se reinicie el teléfono.
        scope.launch(Dispatchers.IO) { AlertReceiver.snoozeAlert(this@PetOverlayService, alert, minutes) }
        toast(getString(R.string.toast_snoozed, minutes))
    }

    private fun speak(pet: PetType) {
        if (!prefs.voice || hiddenNow || prefs.muted) return
        val quiet = phoneIsQuiet()
        if (quiet && !prefs.voiceIgnoreSilent) return
        voice.say(pet.sound(this).filter { it.isLetter() || it == ' ' }, pet.voicePitch, quiet)
    }

    /** Silencio, vibración o No Molestar: la mascota no habla (salvo que el usuario lo permita). */
    private fun phoneIsQuiet(): Boolean {
        val audio = getSystemService(AudioManager::class.java)
        val nm = getSystemService(NotificationManager::class.java)
        return audio.ringerMode != AudioManager.RINGER_MODE_NORMAL ||
            nm.currentInterruptionFilter > NotificationManager.INTERRUPTION_FILTER_ALL
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    // ---------------------------------------------------------------- notificación del servicio

    private fun buildNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_stat_paw)
        .setContentTitle(getString(R.string.notif_pet_title, prefs.pet.emoji, prefs.pet.subject(this)))
        .setContentText(getString(R.string.notif_pet_text))
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_MIN)
        .setContentIntent(
            PendingIntent.getActivity(
                this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE,
            ),
        )
        .addAction(
            0, getString(if (prefs.ghostMode) R.string.action_ghost_off else R.string.action_ghost_on),
            PendingIntent.getService(
                this, 3, Intent(this, PetOverlayService::class.java).setAction(ACTION_TOGGLE_GHOST),
                PendingIntent.FLAG_IMMUTABLE,
            ),
        )
        .addAction(
            0, getString(R.string.action_pause_pet),
            PendingIntent.getService(
                this, 2, Intent(this, PetOverlayService::class.java).setAction(ACTION_PAUSE),
                PendingIntent.FLAG_IMMUTABLE,
            ),
        )
        .addAction(
            0, getString(R.string.action_hide_pet),
            PendingIntent.getService(
                this, 1, Intent(this, PetOverlayService::class.java).setAction(ACTION_STOP),
                PendingIntent.FLAG_IMMUTABLE,
            ),
        )
        .build()

    private fun startInForeground() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, getString(R.string.channel_pet), NotificationManager.IMPORTANCE_MIN),
        )
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else 0
        ServiceCompat.startForeground(this, NOTIFICATION_ID, buildNotification(), type)
    }

    private fun updateNotification() {
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, buildNotification())
    }

    /** Gestos sobre la mascota que el tutorial reconoce en la práctica. */
    enum class Gesture { TAP1, TAP2, TAP3, LEFT, RIGHT, UP, DOWN }

    companion object {
        /**
         * Mientras el tutorial está en "Probalo vos", recibe cada gesto (en el hilo principal) y los
         * tirones no hacen su acción real. Mismo proceso que la app, así que alcanza con una variable.
         */
        @Volatile
        var practice: ((Gesture) -> Unit)? = null

        private const val TAG = "PetOverlay"
        private const val CHANNEL_ID = "pet"
        private const val NOTIFICATION_ID = 1
        private const val TICK_MS = 60_000L
        private const val SOUND_MS = 1_600L
        private const val WAKE_MS = 700L
        /** Sin uso con la pantalla prendida: se acuesta a dormir. */
        private const val IDLE_SLEEP_MS = 10 * 60_000L
        /** En el horario de No molestar, cuánto queda despierta después de un aviso o un toque. */
        private const val DND_GRACE_MS = 60_000L
        private const val MULTI_TAP_WINDOW_MS = 320L
        private const val LONG_PRESS_MS = 650L
        private const val FX_WIDTH_DP = 260
        private const val FX_HEIGHT_DP = 300
        /** Alto aproximado de lo que se muestra, para decidir si entra arriba de la mascota. */
        private const val BUBBLE_GUESS_DP = 170
        private const val SOUND_HEIGHT_DP = 140
        private const val HEARTS_HEIGHT_DP = 150
        /** Margen para la barra de gestos de abajo. */
        private const val NAV_MARGIN_DP = 24
        private const val BASE_FLAGS = LayoutParams.FLAG_NOT_FOCUSABLE or LayoutParams.FLAG_NOT_TOUCH_MODAL
        /**
         * Ventana de efectos: puede salirse de la pantalla. Si Android la "empujara" para que entre,
         * el globo taparía a la mascota o quedaría debajo de ella con la colita al revés.
         */
        private const val FX_FLAGS = BASE_FLAGS or LayoutParams.FLAG_LAYOUT_NO_LIMITS

        const val ACTION_STOP = "com.petnotifications.STOP"
        const val ACTION_TEST = "com.petnotifications.TEST"
        const val ACTION_PAUSE = "com.petnotifications.PAUSE"
        private const val ACTION_TOGGLE_GHOST = "com.petnotifications.TOGGLE_GHOST"
        private const val GHOST_ALPHA = 0.5f
        private const val PAUSE_MS = 60 * 60_000L
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_STARTED_BY = "started_by"
        const val STARTED_BY_APP = "app"
        const val STARTED_BY_WATCHDOG = "watchdog"
        const val STARTED_BY_BOOT = "boot"
        /** Android la reinició solo (START_STICKY). */
        const val STARTED_BY_SYSTEM = "system"
        const val EXTRA_SHOW_MISSED = "show_missed"
        const val EXTRA_NEW_REMINDER = "new_reminder"
        /**
         * Un tirón: el dedo se suelta antes de [FLICK_MAX_MS], a [FLICK_MIN_SPEED_DP] dp/s o más, y se
         * movió al menos [FLICK_MIN_DP]. Si el dedo sigue apoyado pasado ese tiempo, es un arrastre.
         */
        private const val FLICK_MAX_MS = 200L
        /** Pasado este tiempo, un movimiento lento ya se toma como arrastre. */
        private const val DRAG_DECIDE_MS = 60L
        /** Cuánto se inclina la mascota durante un posible tirón (parte del recorrido del dedo, con tope). */
        private const val FLICK_LEAN = 0.25f
        private const val FLICK_LEAN_MAX_DP = 10
        /** Qué parte de la distancia al dedo recupera en cada movimiento al empezar un arrastre rápido. */
        private const val CATCH_UP = 0.35f
        private const val SETTLE_MS = 180L
        private const val FLICK_MIN_SPEED_DP = 500f
        private const val FLICK_MIN_DP = 16
        private const val UPCOMING_MS = 12_000L
        /** Vínculo: espera antes de festejar (para que primero se vea el "+1"), duración de las tarjetas. */
        private const val CELEBRATE_DELAY_MS = 1_800L
        private const val CARD_MS = 10_000L
        private const val MORNING_CARD_MS = 6_000L
        private const val UNLOCK_GREET_DELAY_MS = 900L
        /** Paseos cortos: cuánto camina, a qué velocidad y cuánto espera para volver tras un toque. */
        private const val WALK_MIN_DP = 40
        private const val WALK_MAX_DP = 90
        private const val WALK_DP_PER_S = 60f
        private const val RETURN_HOME_MS = 1_500L
        private const val UNLOCK_CELEBRATE_DELAY_MS = 3_000L
        private const val WAG_EVERY_MS = 3_600_000L
        private const val MORNING_START_MIN = 6 * 60
        private const val NOON_MIN = 12 * 60
        /** Probabilidad por minuto de un baile suelto: con ~16 h despierta salen unos 3 por día (tope 2). */
        private const val RANDOM_DANCE_ODDS = 300
        private const val ACTION_PLUS_ONE = "com.petnotifications.PLUS_ONE"
        private const val ACTION_TRICK = "com.petnotifications.TRICK"
        private const val EXTRA_REWARD = "reward"
        const val EXTRA_BOND_PATH = "bond_path"
        private const val UPCOMING_MAX = 6
        private const val ACTION_SHOW = "com.petnotifications.SHOW"
        private const val ACTION_DISMISS = "com.petnotifications.DISMISS"
        private const val EXTRA_KEY = "key"

        @Volatile
        var running = false
            private set

        /** La app está en primer plano: los festejos del vínculo los muestra ella, como diálogo. */
        @Volatile
        var appVisible = false

        /**
         * La mascota está escondida (llamada o pantalla completa): los avisos van por notificación
         * con sonido.
         */
        @Volatile
        var hiddenNow = false
            private set

        /**
         * La mascota puede dar un aviso ahora: está en pantalla, la pantalla prendida y el celular
         * **desbloqueado**. Sobre la pantalla de bloqueo no se dibuja, así que ahí el aviso tiene que
         * ir por la notificación con sonido (y la mascota lo repite al desbloquear).
         */
        fun canShowAlert(ctx: Context): Boolean =
            running && !hiddenNow &&
                ctx.getSystemService(android.os.PowerManager::class.java).isInteractive &&
                !ctx.getSystemService(android.app.KeyguardManager::class.java).isKeyguardLocked

        /**
         * Arranca la mascota. Android 12+ puede negarse a arrancar un servicio en primer plano desde
         * segundo plano (y Android 15 es más estricto con las ventanas flotantes): en ese caso no se
         * cae la app; los avisos siguen llegando por notificación y la mascota vuelve al abrir la app.
         */
        fun start(ctx: Context, action: String? = null, startedBy: String = STARTED_BY_APP): Boolean = try {
            ContextCompat.startForegroundService(
                ctx, Intent(ctx, PetOverlayService::class.java).setAction(action).putExtra(EXTRA_STARTED_BY, startedBy),
            )
            true
        } catch (e: IllegalStateException) {
            // ForegroundServiceStartNotAllowedException (API 31+) extiende IllegalStateException.
            Log.w(TAG, "No se pudo mostrar la mascota desde segundo plano", e)
            false
        }

        /**
         * Le pasa un aviso a la mascota que ya está en pantalla. Con el servicio en primer plano
         * la app cuenta como activa, así que alcanza con startService.
         */
        fun show(ctx: Context, alert: PetAlert) {
            val intent = Intent(ctx, PetOverlayService::class.java).setAction(ACTION_SHOW).putExtras(alert.toBundle())
            runCatching { ctx.startService(intent) }
        }

        /** Saca el aviso del globo o de la cola (por ejemplo, tras tocar "Hecho" en la notificación). */
        fun dismiss(ctx: Context, key: String) {
            runCatching { ctx.startService(Intent(ctx, PetOverlayService::class.java).setAction(ACTION_DISMISS).putExtra(EXTRA_KEY, key)) }
        }

        /** "+1" del vínculo sobre la mascota que ya está en pantalla. */
        fun plusOne(ctx: Context) {
            runCatching { ctx.startService(Intent(ctx, PetOverlayService::class.java).setAction(ACTION_PLUS_ONE)) }
        }

        /** "Mostrame": la mascota que ya está en pantalla hace el truco o festeja el premio. */
        fun showReward(ctx: Context, reward: Bond.Reward) {
            runCatching {
                ctx.startService(Intent(ctx, PetOverlayService::class.java).setAction(ACTION_TRICK).putExtra(EXTRA_REWARD, reward.name))
            }
        }

        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, PetOverlayService::class.java))
        }
    }
}
