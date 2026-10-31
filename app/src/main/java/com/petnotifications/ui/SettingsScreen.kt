package com.petnotifications.ui

import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import android.content.ContentUris
import android.provider.CalendarContract
import com.petnotifications.overlay.PetAlert
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.AlertDialog
import com.petnotifications.Repeat
import com.petnotifications.data.nextOccurrence
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petnotifications.PetType
import com.petnotifications.Bond
import com.petnotifications.R
import com.petnotifications.PetView
import com.petnotifications.petPainter
import com.petnotifications.Prefs
import com.petnotifications.TapAction
import com.petnotifications.alerts.AlertScheduler
import com.petnotifications.alerts.CalendarAlarms
import com.petnotifications.calendar.CalendarSource
import com.petnotifications.calendar.DeviceCalendar
import com.petnotifications.data.AppDatabase
import com.petnotifications.data.Reminder
import com.petnotifications.overlay.PetOverlayService
import com.petnotifications.overlay.StickerBubble
import com.petnotifications.overlay.callWord
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    openReminderId: Long?,
    onReminderShown: () -> Unit,
    onShowOnboarding: () -> Unit,
    onShowTutorial: () -> Unit = {},
    openBondPath: Boolean = false,
    onBondPathShown: () -> Unit = {},
    showMissed: Boolean = false,
    onMissedShown: () -> Unit = {},
    newReminder: Boolean = false,
    onNewReminderShown: () -> Unit = {},
) {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    val dao = remember { AppDatabase.get(ctx).reminders() }
    val scope = rememberCoroutineScope()

    var canOverlay by remember { mutableStateOf(Settings.canDrawOverlays(ctx)) }
    var hasCalendar by remember { mutableStateOf(CalendarSource.hasPermission(ctx)) }
    var hasNotifications by remember { mutableStateOf(notificationsGranted(ctx)) }
    var batteryFree by remember { mutableStateOf(batteryUnrestricted(ctx)) }
    var exactAlarms by remember { mutableStateOf(AlertScheduler.canScheduleExact(ctx)) }
    var calendars by remember { mutableStateOf(emptyList<DeviceCalendar>()) }

    var enabled by remember { mutableStateOf(prefs.petEnabled) }
    var pet by remember { mutableStateOf(prefs.pet) }
    /** Pelaje de cada mascota (Prefs.coat), para mostrarlas como se ven. */
    var coats by remember { mutableStateOf(PetType.entries.associateWith { prefs.coat(it) }) }
    fun coatOf(p: PetType) = coats[p] ?: 0
    var size by remember { mutableIntStateOf(prefs.petSizeDp) }
    var tapAction by remember { mutableStateOf(prefs.tapAction) }
    var snooze by remember { mutableIntStateOf(prefs.snoozeMinutes) }
    var insistMinutes by remember { mutableIntStateOf(prefs.insistMinutes) }
    var insistMax by remember { mutableIntStateOf(prefs.insistMax) }
    var autoHide by remember { mutableStateOf(prefs.autoHide) }
    var autoSeconds by remember { mutableIntStateOf(prefs.autoHideSeconds) }
    var voice by remember { mutableStateOf(prefs.voice) }
    var voiceIgnoreSilent by remember { mutableStateOf(prefs.voiceIgnoreSilent) }
    var calEnabled by remember { mutableStateOf(prefs.calendarEnabled) }
    var calLead by remember { mutableIntStateOf(prefs.calendarLeadMinutes) }
    var calIds by remember { mutableStateOf(prefs.calendarIds) }
    var muted by remember { mutableStateOf(prefs.muted) }
    /** Mascota a la que se le está poniendo nombre (null = no se muestra el cuadro). */
    var naming by remember { mutableStateOf<PetType?>(null) }
    var namesVersion by remember { mutableIntStateOf(0) }
    var bedtime by remember { mutableStateOf(prefs.sleepAtBedtime) }
    var bedStart by remember { mutableIntStateOf(prefs.bedtimeStartMin) }
    var bedEnd by remember { mutableIntStateOf(prefs.bedtimeEndMin) }
    var unlockRepeat by remember { mutableIntStateOf(prefs.unlockRepeat) }
    var ghost by remember { mutableStateOf(prefs.ghostMode) }
    var liveliness by remember { mutableIntStateOf(prefs.liveliness) }
    var showOwner by remember { mutableStateOf(false) }
    var hideFullscreen by remember { mutableStateOf(prefs.hideInFullscreen) }
    var opacity by remember { mutableIntStateOf(prefs.petOpacity) }
    var pausedUntil by remember { mutableLongStateOf(prefs.petPausedUntil) }

    val petName = remember(pet, namesVersion) { prefs.petName(pet) }
    val petSubject = remember(pet, namesVersion) { pet.subject(ctx) }
    val isPro = prefs.isPro
    var showPro by remember { mutableStateOf(false) }
    var theme by remember { mutableIntStateOf(prefs.theme) }
    var bubbleStyle by remember { mutableIntStateOf(prefs.bubbleStyle) }
    var quietAlerts by remember { mutableStateOf(prefs.quietAlerts) }
    var friendly by remember { mutableIntStateOf(prefs.friendlyLevel) }
    /** Mascota y pelaje Pro que se están probando en la mascota flotante (sin Pro), o null. */
    var trying by remember { mutableStateOf(prefs.trying()) }
    var showFeedback by remember { mutableStateOf(false) }
    var showDiagnostics by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    /** Recordatorio que se repite y el usuario quiere borrar: se pregunta si solo el próximo o todos. */
    var deleting by remember { mutableStateOf<Reminder?>(null) }
    val deletedText = stringResource(R.string.toast_deleted)
    val undoText = stringResource(R.string.undo)

    /** Borra de una. Los de una sola vez se pueden recuperar con "Deshacer" durante unos segundos. */
    fun deleteNow(r: Reminder) {
        AlertScheduler.cancel(ctx, r.id)
        scope.launch {
            withContext(Dispatchers.IO) { dao.delete(r.id) }
            if (Repeat.from(r.repeat) != Repeat.NONE) return@launch
            val result = snackbar.showSnackbar(deletedText, actionLabel = undoText, duration = SnackbarDuration.Long)
            if (result == SnackbarResult.ActionPerformed) {
                withContext(Dispatchers.IO) {
                    dao.insert(r)
                    if (!r.fired) AlertScheduler.schedule(ctx, r)
                }
            }
        }
    }

    fun requestDelete(r: Reminder) {
        if (Repeat.from(r.repeat) == Repeat.NONE) deleteNow(r) else deleting = r
    }
    var needFreePets by remember { mutableStateOf(prefs.needsFreePetChoice()) }
    var canChangeFreePets by remember { mutableStateOf(prefs.canChangeFreePets()) }
    var changingFreePets by remember { mutableStateOf(false) }
    val lockedPets = if (isPro || needFreePets) emptySet() else PetType.entries.toSet() - prefs.freePets

    val reminders by remember { dao.observeAll() }.collectAsStateWithLifecycle(emptyList())
    var editing by remember { mutableStateOf<Reminder?>(null) }
    /** Recordatorio nuevo armado desde una plantilla de Avisos (id 0, sin hora todavía). */
    var draft by remember { mutableStateOf<Reminder?>(null) }
    var creating by remember { mutableStateOf(false) }

    suspend fun reloadCalendars() {
        calendars = withContext(Dispatchers.IO) { CalendarSource.calendars(ctx) }
    }

    /** Eventos de los próximos 7 días para la lista de Avisos (solo si el calendario está activo). */
    var events by remember { mutableStateOf(emptyList<PetAlert>()) }
    suspend fun reloadEvents() {
        events = if (prefs.calendarEnabled && CalendarSource.hasPermission(ctx)) withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            runCatching { CalendarSource.alertsBetween(ctx, now, now + 7 * Prefs.DAY_MS, 0, prefs.calendarIds, now) }.getOrDefault(emptyList())
        } else emptyList()
    }
    LaunchedEffect(calEnabled, calIds, hasCalendar) { reloadEvents() }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        canOverlay = Settings.canDrawOverlays(ctx)
        hasCalendar = CalendarSource.hasPermission(ctx)
        hasNotifications = notificationsGranted(ctx)
        batteryFree = batteryUnrestricted(ctx)
        exactAlarms = AlertScheduler.canScheduleExact(ctx)
        enabled = prefs.petEnabled // pudo cambiar desde la notificación ("Ocultar mascota")
        pausedUntil = prefs.petPausedUntil
        ghost = prefs.ghostMode // pudo cambiar desde la notificación o los ajustes rápidos
        // Por si cambió el permiso de alarmas exactas o hay recordatorios de antes del motor de alarmas.
        scope.launch(Dispatchers.IO) { AlertScheduler.rescheduleAll(ctx) }
        // Si el usuario acaba de volver de dar el permiso, la mascota aparece sola.
        if (prefs.petEnabled && canOverlay && !prefs.petPaused() && !PetOverlayService.running) PetOverlayService.start(ctx)
        scope.launch {
            reloadCalendars()
            reloadEvents()
        }
    }

    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasNotifications = it
    }
    val calendarPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCalendar = granted
        if (granted) {
            calEnabled = true
            prefs.calendarEnabled = true
            prefs.lastCalendarCheck = System.currentTimeMillis()
            CalendarAlarms.syncAsync(ctx)
            scope.launch { reloadCalendars() }
        }
    }

    fun askOverlay() {
        ctx.startActivity(
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${ctx.packageName}")),
        )
    }

    fun resumePet() {
        prefs.petPausedUntil = 0L
        pausedUntil = 0L
        AlertScheduler.cancelPetResume(ctx)
    }

    fun setEnabled(on: Boolean) {
        enabled = on
        prefs.petEnabled = on
        resumePet()
        if (on) {
            if (Build.VERSION.SDK_INT >= 33 && !hasNotifications) {
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (canOverlay) PetOverlayService.start(ctx) else askOverlay()
        } else {
            PetOverlayService.stop(ctx)
        }
    }

    // ================================================================ navegación (Design: 3 pestañas)
    var tab by rememberSaveable { mutableIntStateOf(TAB_ALERTS) }
    var sub by rememberSaveable { mutableStateOf<String?>(null) }
    var detailId by remember { mutableStateOf<Long?>(null) }
    var showFiredToday by remember { mutableStateOf(false) }
    var tipDismissedDay by remember { mutableLongStateOf(prefs.tipDismissedDay) }
    BackHandler(enabled = sub != null) { sub = null }
    BackHandler(enabled = sub == null && tab != TAB_ALERTS) { tab = TAB_ALERTS }
    val pager = rememberPagerState(initialPage = tab) { TAB_COUNT }
    // Tocar la barra de abajo (o ir a una pestaña desde el código) mueve las páginas; deslizar cambia la pestaña.
    LaunchedEffect(tab) { if (pager.currentPage != tab) pager.animateScrollToPage(tab) }
    LaunchedEffect(pager) { snapshotFlow { pager.settledPage }.collect { tab = it } }

    // ================================================================ vínculo
    // Se vuelve a leer cuando cambian sus preferencias (un "Listo" desde la notificación o la mascota).
    var bondVersion by remember { mutableIntStateOf(0) }
    DisposableEffect(Unit) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key?.startsWith(Bond.KEY_PREFIX) == true) bondVersion++
            if (key == Prefs.KEY_TRY_UNTIL) trying = prefs.trying()
        }
        prefs.sp.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.sp.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { bondVersion++ }
    val bondHearts = remember(bondVersion) { Bond.hearts(ctx) }
    val celebration = remember(bondVersion) { Bond.pending(ctx) }
    LaunchedEffect(openBondPath) {
        if (openBondPath) {
            tab = TAB_PET
            sub = SUB_PATH
            onBondPathShown()
        }
    }

    val c = Pet.colors
    val now = System.currentTimeMillis()
    val paused = enabled && pausedUntil > now
    val trialDays = if (!prefs.proPurchased && prefs.trialActive(now)) ((prefs.trialEndsAt - now + Prefs.DAY_MS - 1) / Prefs.DAY_MS).toInt() else null
    val missingPerms = listOf(
        !canOverlay,
        Build.VERSION.SDK_INT >= 33 && !hasNotifications,
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !exactAlarms,
        !batteryFree,
    ).count { it }

    fun testAlert() {
        if (!canOverlay) askOverlay() else {
            if (!enabled) setEnabled(true)
            PetOverlayService.start(ctx, PetOverlayService.ACTION_TEST)
        }
    }

    fun pauseHour() {
        val until = System.currentTimeMillis() + 3_600_000L
        if (PetOverlayService.running) PetOverlayService.start(ctx, PetOverlayService.ACTION_PAUSE)
        else {
            prefs.petPausedUntil = until
            AlertScheduler.schedulePetResume(ctx, until)
        }
        pausedUntil = until
    }

    /**
     * Sin Pro: la mascota flotante pasa a ser [p] con el pelaje [coat] por unos minutos (Prefs.TRY_MS).
     * Se queda en la pantalla en la que está (para probar varias seguidas); la tarjeta de la prueba
     * (TryCard) aparece en Mascota y en Avisos.
     */
    fun startTry(p: PetType, coat: Int) {
        if (!canOverlay) {
            askOverlay()
            return
        }
        prefs.startTry(p, coat)
        trying = prefs.trying()
        if (!enabled) setEnabled(true)
        else if (paused) {
            resumePet()
            PetOverlayService.start(ctx)
        }
    }

    fun endTry() {
        prefs.endTry()
        trying = null
    }

    fun selectPet(p: PetType) {
        if (trying != null) endTry()
        pet = p
        prefs.pet = p
        // Primera vez que elige esta mascota: se le pregunta cómo se llama.
        if (!prefs.petNameAsked(p)) naming = p
    }

    fun openEvent(e: PetAlert) {
        val id = e.eventId ?: return
        val intent = Intent(Intent.ACTION_VIEW, ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id))
        e.eventBegin?.let { intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, it) }
        runCatching { ctx.startActivity(intent) }
    }

    // ---------------------------------------------------------------- Avisos
    /**
     * Arriba de Avisos, la escena: la mascota grande (con lo que tenga puesto), la fecha, cuántas cosas
     * hay para hoy y el chip Vestir. [open] va de 1 (escena entera) a 0 (encabezado fijo de 56 dp con
     * la mascota chiquita): al bajar por la lista se achica de a poco y al volver arriba se agranda.
     * Tocar la mascota o Vestir abre la pestaña Mascota.
     */
    @Composable
    fun PetScene(open: Float, todayCount: Int) {
        val off = !enabled || !canOverlay
        val f = open.coerceIn(0f, 1f)
        fun lerp(a: Float, b: Float) = a + (b - a) * f
        val fade = ((f - 0.5f) / 0.5f).coerceIn(0f, 1f)
        val locale = ctx.resources.configuration.locales[0]
        val date = remember(locale) {
            java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern(ctx.getString(R.string.home_date_pattern), locale))
                .replaceFirstChar { it.titlecase(locale) }
        }
        val toVestidor = { tab = TAB_PET }
        val shape = RoundedCornerShape(lerp(0f, 26f).dp)
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .padding(start = lerp(0f, 16f).dp, end = lerp(0f, 16f).dp, top = lerp(4f, 12f).dp)
                .height(lerp(SCENE_MIN_DP, SCENE_MAX_DP).dp)
                .clip(shape)
                .background(c.blushSoft.copy(alpha = f))
                .drawBehind {
                    // el piso suave, que se va con la escena; abajo, la línea del encabezado fijo
                    val box = this.size // (no el tamaño de la mascota)
                    drawOval(
                        c.surfaceRaised.copy(alpha = 0.7f * f),
                        topLeft = Offset(-box.width * 0.1f, box.height - 40.dp.toPx()),
                        size = Size(box.width * 1.2f, 80.dp.toPx()),
                    )
                    if (f < 1f) drawLine(c.line.copy(alpha = 1f - f), Offset(0f, box.height - 1f), Offset(box.width, box.height - 1f), 1.dp.toPx())
                },
        ) {
            val w = maxWidth.value
            val h = lerp(SCENE_MIN_DP, SCENE_MAX_DP)
            // La grande se achica en su rincón y se va; la chiquita aparece a la izquierda del texto
            // (si una viajara hasta la otra, le pasaría por encima a la fecha).
            val small = 1f - (f / 0.35f).coerceIn(0f, 1f)
            val big = ((f - 0.25f) / 0.35f).coerceIn(0f, 1f)
            val painter = petPainter(pet, if (paused || off) PetView.SLEEP else PetView.FRONT, coatOf(pet))
            val tap = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = toVestidor)
            if (big > 0f) {
                val petSize = 130f * (0.55f + 0.45f * f)
                Image(
                    painter, contentDescription = petSubject,
                    modifier = Modifier.offset((w - 18f - petSize).dp, (h - 10f - petSize).dp).size(petSize.dp).alpha(big).then(tap),
                )
            }
            if (small > 0f) {
                Image(
                    painter, contentDescription = if (big > 0f) null else petSubject,
                    modifier = Modifier.offset(16.dp, ((SCENE_MIN_DP - 40f) / 2).dp).size(40.dp).alpha(small).then(tap),
                )
            }
            Column(
                Modifier
                    .offset((16f + 50f * small).dp, lerp(10f, 16f).dp)
                    .width(lerp(w - 82f, w - 16f - 130f - 30f).dp),
            ) {
                Text(
                    date, fontSize = lerp(16f, 20f).sp, lineHeight = lerp(20f, 24f).sp, fontWeight = FontWeight(800), color = c.ink,
                    maxLines = if (f < 0.5f) 1 else 2, overflow = TextOverflow.Ellipsis,
                )
                Text(
                    when (todayCount) {
                        0 -> stringResource(R.string.home_today_none)
                        1 -> stringResource(R.string.home_today_one)
                        else -> stringResource(R.string.home_today_many, todayCount)
                    },
                    fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight(700), color = c.inkMuted,
                    modifier = Modifier.padding(top = (4f * f).dp),
                )
                // pausa, silencio u oculta: una pastilla que lo arregla de un toque
                val state = when {
                    off -> stringResource(R.string.home_show_pet) to { setEnabled(true) }
                    paused -> stringResource(R.string.home_back_at, hhmm(minuteOfDay(pausedUntil))) to {
                        resumePet()
                        if (canOverlay) PetOverlayService.start(ctx)
                    }
                    muted -> stringResource(R.string.row_mute) to {
                        muted = false
                        prefs.muted = false
                    }
                    else -> null
                }
                if (state != null && fade > 0f) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .alpha(fade)
                            .background(c.surfaceRaised, RoundedCornerShape(50))
                            .border(1.dp, c.line, RoundedCornerShape(50))
                            .clickable(enabled = fade > 0.5f, onClick = state.second)
                            .padding(horizontal = 9.dp, vertical = 3.dp),
                    ) {
                        Icon(
                            when {
                                off -> Icons.Outlined.Visibility
                                paused -> Icons.Outlined.Bedtime
                                else -> Icons.AutoMirrored.Outlined.VolumeOff
                            },
                            contentDescription = null, tint = c.cocoa, modifier = Modifier.size(12.dp),
                        )
                        Text(state.first, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight(800), color = c.cocoa, modifier = Modifier.padding(start = 5.dp))
                    }
                }
            }
            if (fade > 0f) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 14.dp)
                        .alpha(fade)
                        .shadow(2.dp, RoundedCornerShape(50))
                        .background(c.surfaceRaised, RoundedCornerShape(50))
                        .clickable(enabled = fade > 0.5f, onClick = toVestidor)
                        .padding(horizontal = 11.dp, vertical = 6.dp),
                ) {
                    Icon(Icons.Outlined.Checkroom, contentDescription = null, tint = c.cocoa, modifier = Modifier.size(15.dp))
                    Text(stringResource(R.string.home_dress), fontSize = 12.sp, fontWeight = FontWeight(800), color = c.cocoa, modifier = Modifier.padding(start = 5.dp))
                }
            }
        }
    }

    @Composable
    fun AlertCard() {
        // Una sola por vez y solo si hace falta: permiso faltante > prueba que termina. (Si la mascota
        // está apagada lo dice la pastilla de la escena.)
        val (title, body, action, onAction) = when {
            missingPerms > 0 -> Quad(
                if (missingPerms == 1) stringResource(R.string.home_alert_perm_one) else stringResource(R.string.home_alert_perm_many, missingPerms),
                stringResource(R.string.home_alert_perm_body),
                stringResource(R.string.home_fix),
            ) { tab = TAB_SETTINGS; sub = SUB_PERMS }
            trialDays != null && trialDays <= 3 -> Quad(
                if (trialDays <= 1) stringResource(R.string.home_alert_trial_last) else stringResource(R.string.home_alert_trial, trialDays),
                stringResource(R.string.home_alert_trial_body),
                stringResource(R.string.pro_see_plans),
            ) { showPro = true }
            else -> return
        }
        val shape = RoundedCornerShape(18.dp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().background(c.honeySoft, shape).border(1.dp, c.honey, shape).padding(14.dp),
        ) {
            Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = c.honeyDeep, modifier = Modifier.size(20.dp))
            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight(800), color = c.ink)
                Text(body, fontSize = 12.sp, fontWeight = FontWeight(600), color = c.inkMuted)
            }
            SoftChip(action, null, onAction)
        }
    }

    fun relative(at: Long): String? {
        val mins = (at - System.currentTimeMillis()) / 60_000
        return when {
            mins < 0 -> null
            mins < 60 -> ctx.getString(R.string.home_in_min, mins.toInt().coerceAtLeast(1))
            mins < 24 * 60 -> ctx.getString(R.string.home_in_h, (mins / 60).toInt())
            else -> ctx.getString(R.string.home_in_days, (mins / (24 * 60)).toInt())
        }
    }

    @Composable
    fun NextCard(item: HomeItem) {
        PetCard(Modifier.clickable { item.reminder?.let { detailId = it.id } ?: item.event?.let(::openEvent) }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                relative(item.at)?.let {
                    Text(
                        it, fontSize = 12.sp, fontWeight = FontWeight(800), color = c.cocoa,
                        modifier = Modifier.background(c.blushSoft, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 3.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.Filled.Favorite, contentDescription = null, tint = c.blush, modifier = Modifier.size(18.dp))
            }
            Text(item.title, fontSize = 17.sp, fontWeight = FontWeight(800), color = c.ink, modifier = Modifier.padding(top = 8.dp), maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(formatWhen(item.at), fontSize = 13.sp, fontWeight = FontWeight(600), color = c.inkMuted)
        }
    }

    @Composable
    fun ItemRow(item: HomeItem, showDay: Boolean, faded: Boolean = false) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (faded) 0.55f else 1f)
                .clickable { item.reminder?.let { detailId = it.id } ?: item.event?.let(::openEvent) }
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text(hhmmOf(item.at), fontSize = 13.sp, fontWeight = FontWeight(800), color = c.ink, maxLines = 1, softWrap = false, modifier = Modifier.widthIn(min = 52.dp).padding(end = 8.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, fontSize = 14.sp, fontWeight = FontWeight(700), color = c.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val repeat = item.reminder?.let { Repeat.from(it.repeat) } ?: Repeat.NONE
                val meta = listOfNotNull(
                    if (showDay) formatWhen(item.at).substringBefore(" · ") else null,
                    if (item.event != null) stringResource(R.string.home_calendar_meta) else null,
                    if (repeat != Repeat.NONE) stringResource(repeat.labelRes) else null,
                )
                if (meta.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (item.event != null) Icons.Outlined.CalendarMonth else if (repeat != Repeat.NONE) Icons.Outlined.Repeat else Icons.Outlined.Schedule,
                            contentDescription = null, tint = c.inkMuted, modifier = Modifier.size(12.dp),
                        )
                        Text(meta.joinToString(" · "), fontSize = 12.sp, fontWeight = FontWeight(600), color = c.inkMuted, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
        }
    }

    @Composable
    fun TipCard() {
        val today = java.time.LocalDate.now().toEpochDay()
        val firstDay = if (prefs.firstOpenAt > 0) java.time.Instant.ofEpochMilli(prefs.firstOpenAt).atZone(java.time.ZoneId.systemDefault()).toLocalDate().toEpochDay() else today
        val index = (today - firstDay).toInt()
        val tips = listOf(R.string.tip_1, R.string.tip_2, R.string.tip_3, R.string.tip_4, R.string.tip_5, R.string.tip_6, R.string.tip_7, R.string.tip_8)
        if (index !in tips.indices || tipDismissedDay == today) return
        val shape = RoundedCornerShape(18.dp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().background(c.sageSoft, shape).padding(start = 14.dp, top = 6.dp, bottom = 6.dp),
        ) {
            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = c.cocoa, modifier = Modifier.size(16.dp))
            Text(
                androidx.compose.ui.text.buildAnnotatedString {
                    pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight(800)))
                    append(stringResource(R.string.tip_prefix))
                    pop()
                    append(stringResource(tips[index]))
                    append(" ")
                    pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight(800), color = c.cocoa))
                    append(stringResource(R.string.tip_more))
                    pop()
                },
                fontSize = 13.sp, fontWeight = FontWeight(600), color = c.ink,
                modifier = Modifier.weight(1f).clickable(onClick = onShowTutorial).padding(start = 8.dp, top = 6.dp, bottom = 6.dp),
            )
            IconButton(onClick = {
                tipDismissedDay = today
                prefs.tipDismissedDay = today
            }) { Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.tip_close), tint = c.inkMuted) }
        }
    }

    /** Prueba de una mascota o pelaje Pro: cuánto falta, un aviso de prueba, volver a la propia o ver Pro. */
    @Composable
    fun TryCard(p: PetType, coat: Int) {
        var left by remember { mutableLongStateOf(prefs.tryUntil - System.currentTimeMillis()) }
        LaunchedEffect(p, coat) {
            while (true) {
                left = prefs.tryUntil - System.currentTimeMillis()
                if (left <= 0) {
                    trying = null
                    break
                }
                delay(1000)
            }
        }
        val secs = (left.coerceAtLeast(0) + 999) / 1000
        PetCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(petPainter(p, PetView.HAPPY, coat), contentDescription = null, modifier = Modifier.size(56.dp))
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(
                                R.string.try_title,
                                if (coat == 0) stringResource(p.nameRes) else stringResource(R.string.try_coat, stringResource(p.nameRes), coat + 1),
                            ),
                            fontSize = 15.sp, fontWeight = FontWeight(800), color = c.ink, modifier = Modifier.weight(1f, fill = false),
                        )
                        Spacer(Modifier.width(6.dp))
                        ProTag()
                    }
                    Text(
                        stringResource(R.string.try_body, "%d:%02d".format(secs / 60, secs % 60)),
                        fontSize = 13.sp, fontWeight = FontWeight(600), color = c.inkMuted,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp).horizontalScroll(rememberScrollState())) {
                SoftChip(stringResource(R.string.test_alert), Icons.Outlined.NotificationsNone) { testAlert() }
                OutlineChip(stringResource(R.string.try_back)) { endTry() }
                OutlineChip(stringResource(R.string.pro_see_plans)) { showPro = true }
            }
        }
    }

    /** Plantillas de un toque: título y repetición sugerida; el formulario abre pidiendo la hora. */
    val templates = listOf(
        Template(R.string.tpl_water, R.string.tpl_water, Icons.Outlined.WaterDrop, Repeat.DAILY),
        Template(R.string.tpl_pill, R.string.tpl_pill_title, Icons.Outlined.Medication, Repeat.DAILY),
        Template(R.string.tpl_plants, R.string.tpl_plants, Icons.Outlined.LocalFlorist, Repeat.WEEKLY),
        Template(R.string.tpl_trash, R.string.tpl_trash, Icons.Outlined.DeleteOutline, Repeat.DAILY),
        Template(R.string.tpl_bday, R.string.tpl_bday, Icons.Outlined.Cake, Repeat.NONE),
    )

    fun useTemplate(t: Template) {
        draft = Reminder(title = ctx.getString(t.title), timeMillis = 0, repeat = t.repeat.name)
    }

    /** La semana desde hoy: hoy en cocoa, un punto en los días con algo, el elegido con borde. */
    @Composable
    fun WeekStrip(days: List<java.time.LocalDate>, busy: Set<java.time.LocalDate>, selected: java.time.LocalDate, onPick: (java.time.LocalDate) -> Unit) {
        val locale = ctx.resources.configuration.locales[0]
        val fmt = remember(locale) { java.time.format.DateTimeFormatter.ofPattern("EEE", locale) }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            days.forEachIndexed { i, d ->
                val today = i == 0
                val shape = RoundedCornerShape(14.dp)
                val fg = if (today) c.surface else c.ink
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(shape)
                        .background(if (today) c.cocoa else c.surfaceRaised)
                        .border(if (d == selected && !today) 2.dp else 1.dp, if (today || d == selected) c.cocoa else c.line, shape)
                        .clickable { onPick(d) }
                        .padding(top = 6.dp, bottom = 5.dp),
                ) {
                    Text(
                        d.format(fmt).trimEnd('.').take(3).replaceFirstChar { it.titlecase(locale) },
                        fontSize = 11.sp, lineHeight = 13.sp, fontWeight = FontWeight(700), color = if (today) c.surface else c.inkMuted,
                    )
                    Text(d.dayOfMonth.toString(), fontSize = 16.sp, lineHeight = 19.sp, fontWeight = FontWeight(800), color = fg)
                    Box(
                        Modifier.padding(top = 2.dp).size(5.dp)
                            .background(if (d in busy) (if (today) c.surface else c.cocoa) else Color.Transparent, CircleShape),
                    )
                }
            }
        }
    }

    /** Hoy sin nada: las plantillas en dos columnas y "Otro" (formulario vacío). */
    @Composable
    fun TemplatesCard() {
        PetCard {
            Text(stringResource(R.string.home_today_empty_title), fontSize = 15.sp, fontWeight = FontWeight(800), color = c.ink, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text(
                stringResource(R.string.home_today_empty_body), fontSize = 13.sp, fontWeight = FontWeight(600), color = c.inkMuted,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 12.dp),
            )
            val cells: List<Template?> = templates + null
            cells.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    pair.forEach { t ->
                        val shape = RoundedCornerShape(14.dp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clip(shape)
                                .then(if (t == null) Modifier.dashedBorder(c.line, 14.dp) else Modifier.background(c.blushSoft))
                                .clickable { if (t == null) creating = true else useTemplate(t) }
                                .heightIn(min = 44.dp)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                        ) {
                            Icon(t?.icon ?: Icons.Default.Add, contentDescription = null, tint = c.cocoa, modifier = Modifier.size(18.dp))
                            Text(
                                stringResource(t?.label ?: R.string.tpl_other), fontSize = 13.sp, fontWeight = FontWeight(800), color = c.cocoa,
                                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }

    /** Con 1 o 2 recordatorios que se repiten: las plantillas que faltan, en una fila de chips chicos. */
    @Composable
    fun IdeasRow(have: Set<String>) {
        val left = templates.filter { ctx.getString(it.title) !in have }
        if (left.isEmpty()) return
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 2.dp),
        ) {
            Text(stringResource(R.string.home_ideas), fontSize = 12.sp, fontWeight = FontWeight(800), color = c.inkMuted)
            left.forEach { t ->
                Text(
                    stringResource(t.label), fontSize = 12.sp, fontWeight = FontWeight(800), color = c.cocoa,
                    modifier = Modifier.clip(RoundedCornerShape(50)).background(c.blushSoft).clickable { useTemplate(t) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }
    }

    /** La frase amigable del día (la misma todo el día), sin interrumpir. No sale con las frases en "Nunca". */
    @Composable
    fun PhraseOfDay() {
        if (friendly == Prefs.FRIENDLY_OFF) return
        val phrase = remember {
            val all = ctx.resources.getStringArray(R.array.friendly_any)
            all[Math.floorMod(java.time.LocalDate.now().toEpochDay(), all.size.toLong()).toInt()]
        }
        val shape = RoundedCornerShape(18.dp)
        Row(Modifier.fillMaxWidth().background(c.sageSoft, shape).padding(horizontal = 14.dp, vertical = 12.dp)) {
            Icon(Icons.Outlined.Eco, contentDescription = null, tint = c.cocoa, modifier = Modifier.size(18.dp).padding(top = 1.dp))
            Column(Modifier.padding(start = 10.dp)) {
                Text(phrase, fontSize = 14.sp, lineHeight = 19.sp, fontWeight = FontWeight(700), color = c.ink)
                Text(
                    stringResource(R.string.home_phrase_by, prefs.petName(pet) ?: stringResource(pet.nameRes)),
                    fontSize = 12.sp, fontWeight = FontWeight(700), color = c.inkMuted,
                )
            }
        }
    }

    /** El vínculo en una línea: nivel, cuánto falta y la barra; toca y va a Mascota. */
    @Composable
    fun BondLine() {
        val level = Bond.levelFor(bondHearts)
        val next = Bond.next(level)
        val (have, need) = Bond.progress(bondHearts)
        PetCard(Modifier.clickable { tab = TAB_PET }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, tint = c.cocoa, modifier = Modifier.size(20.dp))
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(stringResource(R.string.bond_chip, level.number, stringResource(level.name)), fontSize = 13.sp, fontWeight = FontWeight(800), color = c.ink)
                    Text(
                        when {
                            next == null -> stringResource(R.string.bond_max)
                            next.from - bondHearts == 1 -> stringResource(R.string.bond_missing_one, next.number)
                            else -> stringResource(R.string.bond_missing_many, next.from - bondHearts, next.number)
                        },
                        fontSize = 12.sp, fontWeight = FontWeight(700), color = c.inkMuted,
                    )
                    Box(Modifier.padding(top = 6.dp).fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(c.line)) {
                        Box(Modifier.fillMaxWidth(if (need > 0) have.toFloat() / need else 1f).fillMaxHeight().background(c.blush))
                    }
                }
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = c.inkMuted)
            }
        }
    }

    @Composable
    fun AlertsTab(padding: PaddingValues) {
        val zone = java.time.ZoneId.systemDefault()
        val today = java.time.LocalDate.now(zone)
        fun dayOf(at: Long) = java.time.Instant.ofEpochMilli(at).atZone(zone).toLocalDate()
        val days = remember(today) { (0L..6L).map { today.plusDays(it) } }
        var selected by remember { mutableStateOf(today) }
        if (selected !in days) selected = today
        /** Lo que hay un día: los recordatorios que se repiten aparecen en cada día que les toca. */
        fun itemsOn(day: java.time.LocalDate): List<HomeItem> {
            val mine = reminders.filter { !it.fired }.mapNotNull { r ->
                val first = java.time.Instant.ofEpochMilli(r.timeMillis).atZone(zone)
                val hits = when (Repeat.from(r.repeat)) {
                    Repeat.NONE -> first.toLocalDate() == day
                    Repeat.DAILY -> !day.isBefore(first.toLocalDate())
                    Repeat.WEEKLY -> !day.isBefore(first.toLocalDate()) && day.dayOfWeek == first.dayOfWeek
                }
                if (hits) HomeItem(first.with(day).toInstant().toEpochMilli(), r.title, r, null) else null
            }
            val cal = events.filter { e -> e.eventBegin?.let { dayOf(it) == day } == true }.map { HomeItem(it.eventBegin ?: 0L, it.title, null, it) }
            return (mine + cal).sortedBy { it.at }
        }
        val byDay = remember(reminders, events, today) { days.associateWith(::itemsOn) }
        val later = remember(reminders, events, today) {
            val end = today.plusDays(6)
            (reminders.filter { !it.fired && Repeat.from(it.repeat) == Repeat.NONE }.map { HomeItem(it.timeMillis, it.title, it, null) } +
                events.map { HomeItem(it.eventBegin ?: 0L, it.title, null, it) }).filter { dayOf(it.at).isAfter(end) }.sortedBy { it.at }
        }
        val firedToday = reminders.filter { it.fired && dayOf(it.timeMillis) == today }
        val todayCount = byDay[today].orEmpty().count { it.at >= System.currentTimeMillis() || it.event != null }
        val repeating = reminders.filter { Repeat.from(it.repeat) != Repeat.NONE }
        @Composable
        fun DayList(list: List<HomeItem>) = PetCard(padding = 0.dp) {
            list.forEachIndexed { i, it ->
                if (i > 0) RowDivider()
                ItemRow(it, showDay = false)
            }
        }
        val locale = ctx.resources.configuration.locales[0]
        val dayName = when (selected) {
            today -> stringResource(R.string.today)
            today.plusDays(1) -> stringResource(R.string.tomorrow)
            else -> selected.format(java.time.format.DateTimeFormatter.ofPattern(stringResource(R.string.home_day_pattern), locale))
                .replaceFirstChar { it.titlecase(locale) }
        }
        val chosen = byDay[selected].orEmpty()
        val density = LocalDensity.current
        val range = with(density) { (SCENE_MAX_DP - SCENE_MIN_DP).dp.toPx() }
        // 0 = escena entera; range = encabezado fijo. Se achica antes de que la lista suba y se
        // agranda solo cuando la lista ya volvió arriba.
        var shrunk by remember { mutableFloatStateOf(0f) }
        val connection = remember(range) {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (available.y >= 0f) return Offset.Zero
                    val before = shrunk
                    shrunk = (shrunk - available.y).coerceAtMost(range)
                    return Offset(0f, before - shrunk)
                }

                override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                    if (available.y <= 0f) return Offset.Zero
                    val before = shrunk
                    shrunk = (shrunk - available.y).coerceAtLeast(0f)
                    return Offset(0f, before - shrunk)
                }
            }
        }
        Column(
            Modifier
                .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding())
                .nestedScroll(connection),
        ) {
        PetScene(1f - shrunk / range, todayCount)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            trying?.let { (p, coat) -> item { TryCard(p, coat) } }
            item { AlertCard() }
            item { WeekStrip(days, days.filter { byDay[it].orEmpty().isNotEmpty() }.toSet(), selected) { selected = it } }

            item { SectionLabel(dayName, Modifier.padding(top = 2.dp)) }
            when {
                chosen.isNotEmpty() -> item { DayList(chosen) }
                selected == today -> item { TemplatesCard() }
                else -> item {
                    PetCard {
                        Text(stringResource(R.string.home_day_empty), fontSize = 14.sp, fontWeight = FontWeight(700), color = c.inkMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            if (selected == today) {
                if (chosen.isNotEmpty() && repeating.size < 3) item { IdeasRow(reminders.map { it.title }.toSet()) }
                val tomorrow = byDay[today.plusDays(1)].orEmpty()
                if (tomorrow.isNotEmpty()) {
                    item { SectionLabel(stringResource(R.string.tomorrow), Modifier.padding(top = 4.dp)) }
                    item { DayList(tomorrow) }
                }
                if (later.isNotEmpty()) {
                    item { SectionLabel(stringResource(R.string.home_later), Modifier.padding(top = 4.dp)) }
                    item {
                        PetCard(padding = 0.dp) {
                            later.forEachIndexed { i, it ->
                                if (i > 0) RowDivider()
                                ItemRow(it, showDay = true)
                            }
                        }
                    }
                }
            }
            item { PhraseOfDay() }
            item { BondLine() }
            if (firedToday.isNotEmpty()) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { showFiredToday = !showFiredToday }.padding(vertical = 6.dp),
                    ) {
                        SectionLabel(stringResource(R.string.home_fired_today, firedToday.size), Modifier.weight(1f))
                        Icon(
                            if (showFiredToday) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                            contentDescription = null, tint = c.inkMuted,
                        )
                    }
                }
                if (showFiredToday) item {
                    PetCard(padding = 0.dp) {
                        firedToday.forEachIndexed { i, r ->
                            if (i > 0) RowDivider()
                            ItemRow(HomeItem(r.timeMillis, r.title, r, null), showDay = false, faded = true)
                        }
                    }
                }
            }
            item { TipCard() }
        }
        }
    }

    // ---------------------------------------------------------------- Mascota
    @Composable
    fun PetTab(padding: PaddingValues) {
        val freeMode = !isPro && !needFreePets && prefs.freePets.size >= Prefs.FREE_PET_COUNT
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(92.dp).background(c.blushSoft, CircleShape)) {
                    Image(petPainter(pet, PetView.FRONT, coatOf(pet)), contentDescription = null, modifier = Modifier.size(76.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Text(petName ?: stringResource(pet.nameRes), fontSize = 18.sp, fontWeight = FontWeight(800), color = c.ink)
                    Spacer(Modifier.width(8.dp))
                    OutlineChip(stringResource(if (petName == null) R.string.pet_name_give_chip else R.string.pet_name_chip)) { naming = pet }
                }
                Text(
                    stringResource(R.string.pet_says, stringResource(pet.nameRes), stringResource(pet.soundRes)),
                    fontSize = 13.sp, fontWeight = FontWeight(600), color = c.inkMuted,
                )
            }
            BondSummaryCard(bondHearts) { sub = SUB_PATH }
            if (freeMode) {
                val free = PetType.entries.filter { it in prefs.freePets }
                SectionLabel(stringResource(R.string.pets_your_two))
                PetGrid(free, selected = pet.takeIf { trying == null }, locked = false, coatOf = ::coatOf, onClick = ::selectPet)
                if (canChangeFreePets) {
                    // Botón con borde e ícono (antes era un texto suelto y no se notaba que se podía tocar).
                    OutlinedButton(
                        onClick = { changingFreePets = true },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) {
                        Icon(Icons.Outlined.SwapHoriz, contentDescription = null, tint = c.cocoa, modifier = Modifier.size(20.dp))
                        Column(Modifier.weight(1f).padding(start = 10.dp)) {
                            Text(stringResource(R.string.free_pets_change), color = c.cocoa, fontSize = 14.sp, fontWeight = FontWeight(800))
                            Text(stringResource(R.string.free_pets_change_sub), color = c.inkMuted, fontSize = 12.sp, fontWeight = FontWeight(600))
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionLabel(stringResource(R.string.pets_more_pro), Modifier.weight(1f))
                    OutlineChip(stringResource(R.string.pro_see_plans)) { showPro = true }
                }
                PetGrid(PetType.entries - free.toSet(), selected = trying?.first, locked = true) { startTry(it, 0) }
                Text(stringResource(R.string.try_hint), fontSize = 12.sp, fontWeight = FontWeight(600), color = c.inkMuted)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionLabel(stringResource(R.string.pets_yours), Modifier.weight(1f))
                    if (isPro) ProTag()
                }
                PetGrid(PetType.entries, selected = pet, locked = false, coatOf = ::coatOf, onClick = ::selectPet)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                SectionLabel(stringResource(R.string.coat_title), Modifier.weight(1f))
                if (isPro) ProTag()
            }
            // Probando un pelaje de otra mascota Pro: la fila muestra los de esa mascota.
            val coatPet = trying?.first ?: pet
            CoatRow(coatPet, selected = coatOf(coatPet), locked = !isPro, trying = trying?.second) { i ->
                when {
                    isPro -> {
                        prefs.setCoat(pet, i)
                        coats = coats + (pet to i)
                    }
                    i > 0 || coatPet != pet -> startTry(coatPet, i)
                    else -> endTry()
                }
            }
            trying?.let { (p, coat) -> TryCard(p, coat) }
            SectionLabel(stringResource(R.string.look_title), Modifier.padding(top = 4.dp))
            PetCard {
                ProLock(isPro, onLocked = { showPro = true }) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.size_title), fontSize = 14.sp, fontWeight = FontWeight(700), color = c.ink, modifier = Modifier.weight(1f))
                        }
                        PetSlider(size.toFloat(), { size = it.toInt() }, { prefs.petSizeDp = size }, 48f..120f)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.transparency_title), fontSize = 14.sp, fontWeight = FontWeight(700), color = c.ink, modifier = Modifier.weight(1f))
                        }
                        PetSlider(opacity.toFloat(), { opacity = it.toInt() }, { prefs.petOpacity = opacity }, 40f..100f)
                    }
                }
                HorizontalDivider(color = c.line, modifier = Modifier.padding(vertical = 6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(stringResource(R.string.ghost_title), fontSize = 14.sp, fontWeight = FontWeight(800), color = c.ink)
                        Text(stringResource(R.string.ghost_help), fontSize = 12.sp, fontWeight = FontWeight(600), color = c.inkMuted)
                    }
                    Switch(checked = ghost, onCheckedChange = {
                        ghost = it
                        prefs.ghostMode = it
                    })
                }
                HorizontalDivider(color = c.line, modifier = Modifier.padding(vertical = 6.dp))
                // Vida propia: cada cuánto hace algo sola (gratis para todos).
                Text(stringResource(R.string.lively_title), fontSize = 14.sp, fontWeight = FontWeight(800), color = c.ink)
                Text(stringResource(R.string.lively_help), fontSize = 12.sp, fontWeight = FontWeight(600), color = c.inkMuted)
                Spacer(Modifier.height(6.dp))
                ChipRow(
                    listOf(Prefs.LIVELY_CALM, Prefs.LIVELY_NORMAL, Prefs.LIVELY_ACTIVE), liveliness,
                    label = {
                        when (it) {
                            Prefs.LIVELY_CALM -> ctx.getString(R.string.lively_calm)
                            Prefs.LIVELY_ACTIVE -> ctx.getString(R.string.lively_active)
                            else -> ctx.getString(R.string.lively_normal)
                        }
                    },
                ) {
                    liveliness = it
                    prefs.liveliness = it
                }
            }
            PetCard(padding = 0.dp) {
                SettingRow(Icons.Outlined.TouchApp, stringResource(R.string.tut_open), stringResource(R.string.tut_open_sub), onClick = onShowTutorial)
            }
            OutlinedButton(onClick = ::testAlert, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(stringResource(R.string.test_alert), color = c.cocoa, fontWeight = FontWeight(800))
            }
        }
    }

    // ---------------------------------------------------------------- Ajustes
    @Composable
    fun SettingsTab(padding: PaddingValues) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.tab_settings), fontSize = 24.sp, fontWeight = FontWeight(800), color = c.ink, modifier = Modifier.padding(start = 4.dp))
            // Tu plan
            val planShape = RoundedCornerShape(18.dp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().background(c.sageSoft, planShape).padding(14.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(34.dp).background(c.surfaceRaised, RoundedCornerShape(10.dp))) {
                    Icon(Icons.Outlined.StarOutline, contentDescription = null, tint = c.cocoa, modifier = Modifier.size(18.dp))
                }
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(
                        stringResource(
                            when {
                                prefs.proPurchased -> R.string.plan_pro_title
                                trialDays != null -> R.string.plan_trial_title
                                else -> R.string.plan_free_title
                            },
                        ),
                        fontSize = 14.sp, fontWeight = FontWeight(800), color = c.ink,
                    )
                    Text(
                        when {
                            prefs.proPurchased -> stringResource(R.string.plan_pro_sub)
                            trialDays != null -> stringResource(R.string.plan_trial_sub, trialDays)
                            else -> stringResource(R.string.plan_free_sub)
                        },
                        fontSize = 12.sp, fontWeight = FontWeight(600), color = c.inkMuted,
                    )
                }
                OutlineChip(stringResource(R.string.pro_see_plans)) { showPro = true }
            }
            PetCard(padding = 0.dp) {
                SettingRow(
                    Icons.Outlined.Visibility, stringResource(R.string.row_pet_onscreen),
                    stringResource(if (!enabled) R.string.pet_state_off else if (paused) R.string.pet_state_paused else R.string.pet_state_on),
                    onClick = null,
                    trailing = { Switch(checked = enabled, onCheckedChange = ::setEnabled) },
                )
                if (enabled) {
                    RowDivider()
                    SettingRow(
                        Icons.Outlined.Pause, stringResource(R.string.row_pause),
                        if (paused) stringResource(R.string.home_back_at, hhmm(minuteOfDay(pausedUntil))) else stringResource(R.string.row_pause_sub),
                        onClick = null,
                        trailing = {
                            if (paused) OutlineChip(stringResource(R.string.pet_resume_now)) {
                                resumePet()
                                if (canOverlay) PetOverlayService.start(ctx)
                            } else OutlineChip(stringResource(R.string.home_pause_1h)) { pauseHour() }
                        },
                    )
                }
                RowDivider()
                SettingRow(
                    Icons.AutoMirrored.Outlined.VolumeOff, stringResource(R.string.row_mute), stringResource(R.string.row_mute_sub),
                    onClick = null,
                    trailing = {
                        Switch(checked = muted, onCheckedChange = {
                            muted = it
                            prefs.muted = it
                        })
                    },
                )
                RowDivider()
                SettingRow(Icons.Outlined.Palette, stringResource(R.string.row_theme), null, onClick = null)
                Box(Modifier.padding(start = 14.dp, end = 14.dp, bottom = 12.dp)) {
                    ChipRow(
                        listOf(Prefs.THEME_SYSTEM, Prefs.THEME_LIGHT, Prefs.THEME_DARK), theme,
                        label = {
                            when (it) {
                                Prefs.THEME_LIGHT -> ctx.getString(R.string.theme_light)
                                Prefs.THEME_DARK -> ctx.getString(R.string.theme_dark)
                                else -> ctx.getString(R.string.theme_system)
                            }
                        },
                    ) {
                        theme = it
                        prefs.theme = it
                    }
                }
                RowDivider()
                SettingRow(
                    Icons.Outlined.NotificationsNone, stringResource(R.string.row_notify),
                    listOf(
                        if (autoHide) stringResource(R.string.notify_sum_auto, autoSeconds) else stringResource(R.string.notify_sum_fixed),
                        if (insistMinutes > 0) stringResource(R.string.notify_sum_insist, insistMinutes) else stringResource(R.string.notify_sum_no_insist),
                        stringResource(R.string.friendly_sum, stringResource(friendlyLabel(friendly)).lowercase()),
                    ).joinToString(" · "),
                    onClick = { sub = SUB_NOTIFY },
                )
                RowDivider()
                SettingRow(
                    Icons.Outlined.Bedtime, stringResource(R.string.row_dnd),
                    if (bedtime && isPro) stringResource(R.string.dnd_sum_on, hhmm(bedStart), hhmm(bedEnd)) else stringResource(R.string.dnd_sum_off),
                    onClick = { sub = SUB_DND },
                )
                RowDivider()
                val lead = if (calLead == 0) stringResource(R.string.cal_at_start) else stringResource(R.string.cal_sum_lead, calLead)
                SettingRow(
                    Icons.Outlined.CalendarMonth, stringResource(R.string.row_calendar),
                    when {
                        !(calEnabled && hasCalendar && isPro) -> stringResource(R.string.cal_sum_off)
                        calIds.isEmpty() -> stringResource(R.string.cal_sum_all, lead)
                        else -> stringResource(R.string.cal_sum_some, calIds.size, lead)
                    },
                    onClick = { sub = SUB_CALENDAR },
                    trailing = if (!isPro) ({ ProTag() }) else null,
                )
                RowDivider()
                SettingRow(
                    Icons.Outlined.BatteryChargingFull, stringResource(R.string.row_perms),
                    if (missingPerms == 0) stringResource(R.string.perms_ok) else "⚠ " + stringResource(R.string.perms_missing, missingPerms),
                    summaryColor = if (missingPerms == 0) null else c.honeyDeep,
                    onClick = { sub = SUB_PERMS },
                )
                RowDivider()
                SettingRow(
                    Icons.AutoMirrored.Outlined.HelpOutline, stringResource(R.string.row_help), stringResource(R.string.help_sum),
                    onClick = { sub = SUB_HELP },
                )
            }
            val version = remember {
                runCatching { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName }.getOrNull().orEmpty()
            }
            // Versiones de prueba: 7 toques en la versión abren las herramientas de dueño.
            var versionTaps by remember { mutableIntStateOf(0) }
            Text(
                stringResource(R.string.version_label, version) +
                    if (com.petnotifications.BuildConfig.OWNER_TOOLS && prefs.ownerPlan != Prefs.OWNER_NORMAL) " · " + stringResource(R.string.owner_badge) else "",
                fontSize = 12.sp, color = c.inkMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().then(
                    if (com.petnotifications.BuildConfig.OWNER_TOOLS) Modifier.clickable {
                        if (++versionTaps >= 7) {
                            versionTaps = 0
                            showOwner = true
                        }
                    } else Modifier,
                ),
            )
        }
    }

    @Composable
    fun SubPage(padding: PaddingValues, content: @Composable ColumnScope.() -> Unit) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }

    @Composable
    fun NotifyPage(padding: PaddingValues) = SubPage(padding) {
        SectionLabel(stringResource(R.string.row_bubble))
        PetCard {
            Text(stringResource(R.string.bubble_help), style = MaterialTheme.typography.bodySmall, color = c.inkMuted)
            val word = callWord(pet.sound(ctx))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 10.dp)) {
                listOf(Prefs.BUBBLE_CARAMEL to R.string.bubble_caramel, Prefs.BUBBLE_CLOUD to R.string.bubble_cloud).forEach { (style, label) ->
                    val selected = bubbleStyle == style
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(if (selected) 2.dp else 1.dp, if (selected) c.cocoa else c.line, RoundedCornerShape(16.dp))
                            .clickable {
                                bubbleStyle = style
                                prefs.bubbleStyle = style
                            }
                            .padding(vertical = 8.dp),
                    ) {
                        StickerBubble(word, style, mirrored = false, id = style.toLong())
                        Text(
                            stringResource(label), style = MaterialTheme.typography.labelLarge,
                            color = if (selected) c.cocoa else c.inkMuted,
                        )
                    }
                }
            }
        }
        PetCard {
            SwitchRow(stringResource(R.string.quiet_title), stringResource(R.string.quiet_sub), quietAlerts) {
                quietAlerts = it
                prefs.quietAlerts = it
            }
        }
        SectionLabel(stringResource(R.string.friendly_title))
        PetCard {
            Text(stringResource(R.string.friendly_help), style = MaterialTheme.typography.bodySmall, color = c.inkMuted)
            ChipRow(
                listOf(Prefs.FRIENDLY_OFF, Prefs.FRIENDLY_FEW, Prefs.FRIENDLY_NORMAL, Prefs.FRIENDLY_MANY), friendly,
                label = { ctx.getString(friendlyLabel(it)) },
            ) {
                friendly = it
                prefs.friendlyLevel = it
            }
            HorizontalDivider(color = c.line, modifier = Modifier.padding(vertical = 8.dp))
            Text(stringResource(R.string.letters_help_title), style = MaterialTheme.typography.labelLarge)
            Text(stringResource(R.string.letters_help_body), style = MaterialTheme.typography.bodySmall, color = c.inkMuted)
        }
        SectionLabel(stringResource(R.string.section_tap))
        PetCard {
            TapAction.entries.forEach { action ->
                RadioRow(stringResource(action.labelRes), stringResource(action.descriptionRes), tapAction == action) {
                    tapAction = action
                    prefs.tapAction = action
                }
            }
            Text(stringResource(R.string.action_snooze), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 6.dp))
            val minLabel = stringResource(R.string.minutes_n)
            ChipRow(listOf(5, 10, 15, 30, 60), snooze, { minLabel.format(it) }) {
                snooze = it
                prefs.snoozeMinutes = it
            }
        }
        SectionLabel(stringResource(R.string.insist_title))
        PetCard {
            Text(stringResource(R.string.insist_help), style = MaterialTheme.typography.bodySmall, color = c.inkMuted)
            ProLock(isPro, onLocked = { showPro = true }) {
                Column {
                    val offLabel = stringResource(R.string.insist_off)
                    val everyLabel = stringResource(R.string.insist_every)
                    ChipRow(listOf(0, 5, 10, 15, 30), insistMinutes, { if (it == 0) offLabel else everyLabel.format(it) }) {
                        insistMinutes = it
                        prefs.insistMinutes = it
                    }
                    if (insistMinutes > 0) {
                        Text(stringResource(R.string.insist_times_label), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 6.dp))
                        val onceLabel = stringResource(R.string.insist_once)
                        val timesLabel = stringResource(R.string.insist_times)
                        ChipRow(listOf(1, 3, 5), insistMax, { if (it == 1) onceLabel else timesLabel.format(it) }) {
                            insistMax = it
                            prefs.insistMax = it
                        }
                    }
                }
            }
        }
        SectionLabel(stringResource(R.string.section_duration))
        PetCard {
            RadioRow(stringResource(R.string.autohide_title), stringResource(R.string.autohide_sub, autoSeconds), autoHide) {
                autoHide = true
                prefs.autoHide = true
            }
            if (autoHide) {
                Slider(
                    value = autoSeconds.toFloat(), onValueChange = { autoSeconds = it.toInt() },
                    onValueChangeFinished = { prefs.autoHideSeconds = autoSeconds }, valueRange = 3f..60f,
                    modifier = Modifier.padding(start = 48.dp),
                )
            }
            RadioRow(stringResource(R.string.fixed_title), stringResource(R.string.fixed_sub), !autoHide) {
                autoHide = false
                prefs.autoHide = false
            }
            HorizontalDivider(color = c.line, modifier = Modifier.padding(vertical = 8.dp))
            Text(stringResource(R.string.unlock_repeat_title), style = MaterialTheme.typography.labelLarge)
            Text(stringResource(R.string.unlock_repeat_help), style = MaterialTheme.typography.bodySmall, color = c.inkMuted)
            val repeatLabels = mapOf(
                0 to stringResource(R.string.unlock_repeat_none),
                1 to stringResource(R.string.unlock_repeat_last),
                3 to stringResource(R.string.unlock_repeat_last3),
                Prefs.UNLOCK_REPEAT_ALL to stringResource(R.string.unlock_repeat_all),
            )
            ChipRow(repeatLabels.keys.toList(), unlockRepeat, { repeatLabels.getValue(it) }) {
                unlockRepeat = it
                prefs.unlockRepeat = it
            }
        }
        PetCard {
            ProLock(isPro, onLocked = { showPro = true }) {
                Column {
                    SwitchRow(stringResource(R.string.voice_title), stringResource(R.string.voice_sub), voice) {
                        voice = it
                        prefs.voice = it
                    }
                    if (voice) {
                        SwitchRow(stringResource(R.string.voice_ignore_silent), stringResource(R.string.voice_ignore_silent_help), voiceIgnoreSilent) {
                            voiceIgnoreSilent = it
                            prefs.voiceIgnoreSilent = it
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun DndPage(padding: PaddingValues) = SubPage(padding) {
        PetCard {
            Text(stringResource(R.string.sleep_help), style = MaterialTheme.typography.bodySmall, color = c.inkMuted)
            ProLock(isPro, onLocked = { showPro = true }) {
                Column {
                    SwitchRow(stringResource(R.string.sleep_bedtime_title), stringResource(R.string.sleep_bedtime_sub, hhmm(bedStart), hhmm(bedEnd)), bedtime) {
                        bedtime = it
                        prefs.sleepAtBedtime = it
                    }
                    if (bedtime) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlineChip(stringResource(R.string.time_from, hhmm(bedStart))) {
                                pickTime(ctx, bedStart) {
                                    bedStart = it
                                    prefs.bedtimeStartMin = it
                                }
                            }
                            OutlineChip(stringResource(R.string.time_to, hhmm(bedEnd))) {
                                pickTime(ctx, bedEnd) {
                                    bedEnd = it
                                    prefs.bedtimeEndMin = it
                                }
                            }
                        }
                    }
                }
            }
        }
        PetCard {
            SwitchRow(stringResource(R.string.hide_fullscreen_title), stringResource(R.string.hide_fullscreen_help), hideFullscreen) {
                hideFullscreen = it
                prefs.hideInFullscreen = it
            }
        }
    }

    @Composable
    fun CalendarPage(padding: PaddingValues) = SubPage(padding) {
        PetCard {
            ProLock(isPro, onLocked = { showPro = true }) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.cal_toggle), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Switch(checked = calEnabled && hasCalendar, onCheckedChange = { on ->
                            if (on && !hasCalendar) {
                                calendarPermission.launch(Manifest.permission.READ_CALENDAR)
                            } else {
                                calEnabled = on
                                prefs.calendarEnabled = on
                                prefs.lastCalendarCheck = System.currentTimeMillis()
                                CalendarAlarms.syncAsync(ctx)
                            }
                        })
                    }
                }
            }
            Text(stringResource(R.string.cal_help), style = MaterialTheme.typography.bodySmall, color = c.inkMuted)
        }
        if (calEnabled && hasCalendar && isPro) {
            PetCard {
                Text(stringResource(R.string.cal_lead), style = MaterialTheme.typography.labelLarge)
                val atStart = stringResource(R.string.cal_at_start)
                val minLabel = stringResource(R.string.minutes_n)
                ChipRow(listOf(0, 5, 10, 15, 30, 60), calLead, { if (it == 0) atStart else minLabel.format(it) }) {
                    calLead = it
                    prefs.calendarLeadMinutes = it
                    CalendarAlarms.syncAsync(ctx)
                }
                Text(stringResource(R.string.calendars), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 10.dp))
                if (calendars.isEmpty()) Text(stringResource(R.string.no_calendars), style = MaterialTheme.typography.bodySmall)
                calendars.forEach { cal ->
                    val checked = calIds.isEmpty() || cal.id in calIds
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable {
                            val all = calendars.map { it.id }.toSet()
                            val current = calIds.ifEmpty { all }
                            val next = if (checked) current - cal.id else current + cal.id
                            // Todos marcados se guarda como "vacío" para incluir calendarios nuevos.
                            calIds = if (next == all) emptySet() else next
                            prefs.calendarIds = calIds
                            CalendarAlarms.syncAsync(ctx)
                        },
                    ) {
                        Checkbox(checked = checked, onCheckedChange = null, modifier = Modifier.padding(8.dp))
                        Box(Modifier.size(10.dp).background(Color(cal.color), CircleShape))
                        Column(Modifier.padding(start = 10.dp)) {
                            Text(cal.name, style = MaterialTheme.typography.bodyMedium)
                            Text(cal.account, style = MaterialTheme.typography.bodySmall, color = c.inkMuted)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun PermsPage(padding: PaddingValues) = SubPage(padding) {
        if (missingPerms > 0) {
            val shape = RoundedCornerShape(18.dp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().background(c.honeySoft, shape).border(1.dp, c.honey, shape).padding(14.dp),
            ) {
                Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = c.honeyDeep, modifier = Modifier.size(20.dp))
                Text(
                    if (missingPerms == 1) stringResource(R.string.perms_alert_one) else stringResource(R.string.perms_alert_many, missingPerms),
                    fontSize = 13.sp, fontWeight = FontWeight(700), color = c.ink, modifier = Modifier.padding(start = 10.dp),
                )
            }
        }
        PetCard(padding = 0.dp) {
            PermissionRow(stringResource(R.string.permission_overlay), canOverlay) { askOverlay() }
            if (Build.VERSION.SDK_INT >= 33) {
                RowDivider()
                PermissionRow(stringResource(R.string.permission_notifications), hasNotifications) {
                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                RowDivider()
                PermissionRow(stringResource(R.string.permission_exact), exactAlarms) {
                    ctx.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${ctx.packageName}")))
                }
            }
            RowDivider()
            PermissionRow(stringResource(R.string.permission_calendar), hasCalendar, optional = true) {
                calendarPermission.launch(Manifest.permission.READ_CALENDAR)
            }
            RowDivider()
            PermissionRow(stringResource(R.string.permission_battery), batteryFree) { openAppSettings(ctx) }
        }
        // Aunque la optimización esté desactivada, varias marcas cierran apps por su cuenta.
        PetCard { BatteryGuide(Modifier.fillMaxWidth()) }
        // Aviso fijo de Android ("se muestra sobre otras apps"): se puede apagar solo para esta app.
        PetCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.notice_row_title), fontSize = 14.sp, fontWeight = FontWeight(800), color = c.ink)
                    Text(stringResource(R.string.notice_row_help), style = MaterialTheme.typography.bodySmall, color = c.inkMuted)
                }
                Spacer(Modifier.width(8.dp))
                OutlineChip(stringResource(R.string.notice_row_button)) { openOverlayNoticeSettings(ctx) }
            }
        }
    }

    @Composable
    fun PathPage(padding: PaddingValues) = SubPage(padding) {
        BondPathList(bondHearts)
    }

    @Composable
    fun HelpPage(padding: PaddingValues) = SubPage(padding) {
        PetCard(padding = 0.dp) {
            SettingRow(Icons.Outlined.TouchApp, stringResource(R.string.help_how), stringResource(R.string.tut_open_sub), onClick = onShowTutorial)
            RowDivider()
            SettingRow(Icons.Outlined.Pets, stringResource(R.string.onboarding_again), null, onClick = onShowOnboarding)
            RowDivider()
            SettingRow(Icons.Outlined.MailOutline, stringResource(R.string.feedback), null, onClick = { showFeedback = true })
            RowDivider()
            SettingRow(Icons.Outlined.Insights, stringResource(R.string.diag_open), null, onClick = { showDiagnostics = true })
        }
    }

    val subTitle = when (sub) {
        SUB_NOTIFY -> R.string.row_notify
        SUB_DND -> R.string.row_dnd
        SUB_CALENDAR -> R.string.row_calendar
        SUB_PERMS -> R.string.row_perms
        SUB_HELP -> R.string.row_help
        SUB_PATH -> R.string.bond_path_title
        else -> null
    }
    Scaffold(
        containerColor = c.surface,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            if (subTitle != null) {
                TopAppBar(
                    title = { Text(stringResource(subTitle), fontWeight = FontWeight(800)) },
                    navigationIcon = {
                        IconButton(onClick = { sub = null }) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = c.surface),
                )
            }
        },
        bottomBar = {
            NavigationBar(containerColor = c.surfaceRaised) {
                listOf(
                    Triple(TAB_ALERTS, R.string.tab_alerts, Icons.Outlined.NotificationsNone),
                    Triple(TAB_PET, R.string.tab_pet, Icons.Outlined.Pets),
                    Triple(TAB_SETTINGS, R.string.tab_settings, Icons.Outlined.Tune),
                ).forEach { (id, label, icon) ->
                    NavigationBarItem(
                        selected = tab == id,
                        onClick = {
                            tab = id
                            sub = null
                        },
                        icon = { Icon(icon, contentDescription = null) },
                        label = { Text(stringResource(label), fontWeight = if (tab == id) FontWeight(900) else FontWeight(700)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = c.cocoa, selectedTextColor = c.cocoa, indicatorColor = c.blushSoft,
                            unselectedIconColor = c.inkMuted, unselectedTextColor = c.inkMuted,
                        ),
                    )
                }
            }
        },
        floatingActionButton = {
            if (tab == TAB_ALERTS && sub == null && (reminders.isNotEmpty() || events.isNotEmpty())) {
                ExtendedFloatingActionButton(
                    onClick = { creating = true },
                    containerColor = c.cocoa,
                    contentColor = c.surface,
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text(stringResource(R.string.fab_reminder), fontWeight = FontWeight(800)) },
                )
            }
        },
    ) { padding ->
        when (sub) {
            SUB_NOTIFY -> NotifyPage(padding)
            SUB_DND -> DndPage(padding)
            SUB_CALENDAR -> CalendarPage(padding)
            SUB_PERMS -> PermsPage(padding)
            SUB_HELP -> HelpPage(padding)
            SUB_PATH -> PathPage(padding)
            // Las 3 pestañas se pasan también deslizando el dedo.
            else -> HorizontalPager(pager, modifier = Modifier.fillMaxSize()) { page ->
                when (page) {
                    TAB_PET -> PetTab(padding)
                    TAB_SETTINGS -> SettingsTab(padding)
                    else -> AlertsTab(padding)
                }
            }
        }
    }

    celebration?.let { cel ->
        fun close() {
            Bond.markCelebrated(ctx, cel)
            bondVersion++
        }
        BondCelebrationDialog(
            cel,
            onPrimary = {
                close()
                if (cel is Bond.Celebration.Welcome) {
                    tab = TAB_PET
                    sub = SUB_PATH
                } else {
                    // "Mostrame": la mascota (que está en pantalla, encima de la app) hace el truco.
                    val reward = when (cel) {
                        is Bond.Celebration.LevelUp -> cel.level.reward
                        is Bond.Celebration.Arrived -> cel.level.reward
                        is Bond.Celebration.Welcome -> null
                    }
                    if (reward != null && PetOverlayService.running) {
                        PetOverlayService.showReward(ctx, reward)
                    }
                }
            },
            onDismiss = ::close,
        )
    }

    if (showOwner) OwnerToolsDialog { showOwner = false }

    deleting?.let { r ->
        val next = nextOccurrence(r.timeMillis, Repeat.from(r.repeat), r.timeMillis)
        val nextText = formatWhen(next)
        val skippedText = stringResource(R.string.skipped_next, nextText)
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.delete_repeat_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.delete_repeat_body))
                    OutlinedButton(onClick = {
                        deleting = null
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                val skipped = r.copy(timeMillis = next, fired = false)
                                dao.update(skipped)
                                AlertScheduler.schedule(ctx, skipped)
                            }
                            snackbar.showSnackbar(skippedText)
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.delete_skip_next, formatWhen(r.timeMillis))) }
                    OutlinedButton(onClick = {
                        deleting = null
                        deleteNow(r)
                    }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.delete_all)) }
                }
            },
            confirmButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }

    if (changingFreePets) {
        FreePetChooser(
            initial = prefs.freePets,
            changing = true,
            onCancel = { changingFreePets = false },
            onSeePlans = {},
            onChosen = {
                prefs.freePets = it
                prefs.freePetChanges = prefs.freePetChanges + 1
                changingFreePets = false
                canChangeFreePets = prefs.canChangeFreePets()
                if (pet !in it) {
                    pet = it.first()
                    prefs.pet = pet
                }
            },
        )
    }

    if (showMissed) {
        val lines = remember { prefs.missedList }
        AlertDialog(
            onDismissRequest = onMissedShown,
            title = { Text(stringResource(R.string.missed_list_title)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (lines.isEmpty()) Text(stringResource(R.string.missed_list_empty))
                    lines.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    prefs.missedList = emptyList()
                    onMissedShown()
                }) { Text(stringResource(R.string.done_button)) }
            },
        )
    }

    naming?.let { p ->
        PetNameDialog(p, prefs.petName(p).orEmpty()) { newName ->
            prefs.setPetName(p, newName)
            naming = null
            namesVersion++
        }
    }

    if (showPro) ProDialog(onDismiss = { showPro = false })
    if (showFeedback) FeedbackDialog(onDismiss = { showFeedback = false })
    if (showDiagnostics) {
        DiagnosticsDialog(
            onDismiss = { showDiagnostics = false },
            onSendByEmail = {
                showDiagnostics = false
                showFeedback = true
            },
        )
    }
    // Terminó la prueba de Pro: una sola vez, lo que se va, con Comprar Pro y Seguir gratis.
    var trialEnd by remember { mutableStateOf(prefs.shouldShowTrialEnd()) }
    if (trialEnd && !showPro && !needFreePets) {
        TrialEndDialog(
            prefs,
            onBuy = {
                trialEnd = false
                prefs.trialEndShown = true
                showPro = true
            },
            onStayFree = {
                trialEnd = false
                prefs.trialEndShown = true
            },
        )
    }
    if (!showPro && needFreePets) {
        FreePetChooser(
            initial = setOf(pet),
            onChosen = {
                prefs.freePets = it
                needFreePets = false
                canChangeFreePets = prefs.canChangeFreePets()
                if (pet !in it) {
                    pet = it.first()
                    prefs.pet = pet
                }
            },
            onSeePlans = { showPro = true },
        )
    }

    LaunchedEffect(newReminder) {
        if (newReminder) {
            creating = true
            onNewReminderShown()
        }
    }

    if (creating || editing != null || draft != null) {
        ReminderEditor(
            initial = editing ?: draft,
            onDismiss = {
                creating = false
                editing = null
                draft = null
            },
            onSave = { r ->
                creating = false
                editing = null
                draft = null
                scope.launch {
                    withContext(Dispatchers.IO) {
                        val saved = if (r.id == 0L) r.copy(id = dao.insert(r)) else r.also { dao.update(it) }
                        AlertScheduler.schedule(ctx, saved)
                    }
                }
            },
        )
    }

    val shownDetail = openReminderId ?: detailId
    if (shownDetail != null) {
        ReminderDetail(
            id = shownDetail,
            dao = dao,
            onEdit = {
                onReminderShown()
                detailId = null
                editing = it
            },
            onDelete = {
                onReminderShown()
                detailId = null
                requestDelete(it)
            },
            onDismiss = {
                onReminderShown()
                detailId = null
            },
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.padding(top = 8.dp))
            content()
        }
    }
}

/** Permiso: nombre y, a la derecha, "✓ Listo" (cocoa), "Permitir" (chip) u "Opcional". */
@Composable
private fun PermissionRow(label: String, granted: Boolean, optional: Boolean = false, onRequest: () -> Unit) {
    val c = Pet.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight(700), color = c.ink, modifier = Modifier.weight(1f))
        when {
            granted -> Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Check, contentDescription = null, tint = c.cocoa, modifier = Modifier.size(16.dp))
                Text(stringResource(R.string.perm_ready), fontSize = 13.sp, fontWeight = FontWeight(800), color = c.cocoa, modifier = Modifier.padding(start = 4.dp))
            }
            optional -> TextButton(onClick = onRequest) {
                Text(stringResource(R.string.perm_optional), fontSize = 13.sp, fontWeight = FontWeight(700), color = c.inkMuted)
            }
            else -> SoftChip(stringResource(R.string.allow), null, onRequest)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PetPicker(selected: PetType, onSelect: (PetType) -> Unit) = PetPicker(setOf(selected), emptySet(), onSelect)

/** Selector de mascotas. Las de [locked] (sin Pro) se ven apagadas con la etiqueta "PRO". */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PetPicker(selected: Set<PetType>, locked: Set<PetType>, onSelect: (PetType) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PetType.entries.forEach { p ->
            val isSelected = p in selected
            Box {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(72.dp)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        RoundedCornerShape(16.dp),
                    )
                    .border(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(16.dp),
                    )
                    .selectable(isSelected, role = Role.RadioButton) { onSelect(p) }
                    .padding(vertical = 8.dp),
            ) {
                Image(
                    petPainter(p, PetView.FRONT), contentDescription = null,
                    modifier = Modifier.size(52.dp).alpha(if (p in locked) 0.45f else 1f),
                )
                Text(stringResource(p.nameRes), style = MaterialTheme.typography.labelMedium)
            }
            if (p in locked) ProBadge(Modifier.align(Alignment.TopEnd))
            }
        }
    }
}

@Composable
private fun RadioRow(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 4.dp),
    ) {
        RadioButton(selected = selected, onClick = null, modifier = Modifier.padding(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

private fun hhmm(minutes: Int) = "%02d:%02d".format(minutes / 60, minutes % 60)

private fun minuteOfDay(millis: Long): Int {
    val c = java.util.Calendar.getInstance().apply { timeInMillis = millis }
    return c.get(java.util.Calendar.HOUR_OF_DAY) * 60 + c.get(java.util.Calendar.MINUTE)
}

private fun pickTime(ctx: Context, minutes: Int, onPicked: (Int) -> Unit) {
    TimePickerDialog(ctx, { _, h, m -> onPicked(h * 60 + m) }, minutes / 60, minutes % 60, true).show()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> ChipRow(options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { o ->
            FilterChip(selected = o == selected, onClick = { onSelect(o) }, label = { Text(label(o)) })
        }
    }
}

internal fun notificationsGranted(ctx: Context) =
    Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

internal fun batteryUnrestricted(ctx: Context) =
    ctx.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(ctx.packageName)

private const val TAB_ALERTS = 0
private const val TAB_PET = 1
/** Alto de la escena de Avisos: entera y achicada a encabezado fijo. */
private const val SCENE_MAX_DP = 200f
private const val SCENE_MIN_DP = 56f
private const val TAB_SETTINGS = 2
private const val TAB_COUNT = 3
private const val SUB_NOTIFY = "notify"
private const val SUB_DND = "dnd"
private const val SUB_CALENDAR = "calendar"
private const val SUB_PERMS = "perms"
private const val SUB_HELP = "help"
private const val SUB_PATH = "path"

/** Algo que va a sonar: un recordatorio propio o un evento del calendario. */
private data class HomeItem(val at: Long, val title: String, val reminder: Reminder?, val event: PetAlert?)

/** Plantilla de Avisos: lo que dice el chip, el título que pone y la repetición sugerida. */
private class Template(@androidx.annotation.StringRes val label: Int, @androidx.annotation.StringRes val title: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector, val repeat: Repeat)


private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

private fun hhmmOf(millis: Long): String =
    java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(java.util.Date(millis))

private fun friendlyLabel(level: Int) = when (level) {
    Prefs.FRIENDLY_OFF -> R.string.friendly_off
    Prefs.FRIENDLY_NORMAL -> R.string.friendly_normal
    Prefs.FRIENDLY_MANY -> R.string.friendly_many
    else -> R.string.friendly_few
}
