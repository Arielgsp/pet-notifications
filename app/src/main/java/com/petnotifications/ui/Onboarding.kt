package com.petnotifications.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.petnotifications.PetView
import com.petnotifications.PetType
import com.petnotifications.Prefs
import com.petnotifications.R
import com.petnotifications.alerts.AlertScheduler
import com.petnotifications.alerts.CalendarAlarms
import com.petnotifications.petPainter
import com.petnotifications.calendar.CalendarSource
import com.petnotifications.overlay.PetOverlayService

/** Pasos de la configuración guiada. Los que no aplican o ya están resueltos se saltean. */
private enum class Step { WELCOME, OVERLAY, NOTIFICATIONS, EXACT_ALARMS, CALENDAR, BATTERY, OVERLAY_NOTICE, DONE }

/**
 * Configuración inicial guiada por la mascota: antes de que Android pida cada permiso, la mascota
 * explica para qué es y qué pasa si el usuario dice que no. Todo es opcional: sin el permiso
 * flotante la app igual avisa con notificaciones comunes.
 */
@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    var pet by remember { mutableStateOf(prefs.pet) }
    var index by remember { mutableIntStateOf(0) }

    fun granted(step: Step): Boolean = when (step) {
        Step.OVERLAY -> Settings.canDrawOverlays(ctx)
        Step.NOTIFICATIONS -> notificationsGranted(ctx)
        Step.EXACT_ALARMS -> AlertScheduler.canScheduleExact(ctx)
        Step.CALENDAR -> CalendarSource.hasPermission(ctx) && prefs.calendarEnabled
        Step.BATTERY -> batteryUnrestricted(ctx)
        else -> false
    }

    fun applies(step: Step) = when (step) {
        Step.NOTIFICATIONS -> Build.VERSION.SDK_INT >= 33
        Step.EXACT_ALARMS -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        else -> true
    }

    // Solo los pasos que hacen falta; se calcula una vez para que el contador no salte.
    val steps = remember { Step.entries.filter { applies(it) && !granted(it) } }
    val step = steps[index.coerceAtMost(steps.lastIndex)]

    // Atrás (con la animación de "atrás predictivo") vuelve al paso anterior; en el primero, sale.
    BackHandler(enabled = index > 0) { index-- }

    fun next() {
        if (index < steps.lastIndex) index++ else finish(ctx, prefs, onDone)
    }

    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { next() }
    val calendarPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) {
            prefs.calendarEnabled = true
            prefs.lastCalendarCheck = System.currentTimeMillis()
            CalendarAlarms.syncAsync(ctx)
        }
        next()
    }

    // Si el usuario volvió de los ajustes del sistema con el permiso dado, se avanza solo.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (step in setOf(Step.OVERLAY, Step.EXACT_ALARMS, Step.BATTERY) && granted(step)) next()
    }

    var naming by remember { mutableStateOf<PetType?>(null) }
    var nameAfter by remember { mutableStateOf(false) }
    var namesVersion by remember { mutableIntStateOf(0) }
    val name = remember(pet, namesVersion) { pet.mention(ctx) }
    val (title, body, ifNo, yes) = when (step) {
        Step.WELCOME -> Texts(
            stringResource(R.string.ob_welcome_title, name),
            stringResource(R.string.ob_welcome_body),
            null,
            stringResource(R.string.ob_start),
        )
        Step.OVERLAY -> Texts(
            stringResource(R.string.ob_overlay_title),
            stringResource(R.string.ob_overlay_body),
            stringResource(R.string.ob_overlay_no),
            stringResource(R.string.ob_allow),
        )
        Step.NOTIFICATIONS -> Texts(
            stringResource(R.string.ob_notif_title),
            stringResource(R.string.ob_notif_body),
            stringResource(R.string.ob_notif_no),
            stringResource(R.string.ob_allow),
        )
        Step.EXACT_ALARMS -> Texts(
            stringResource(R.string.ob_exact_title),
            stringResource(R.string.ob_exact_body),
            stringResource(R.string.ob_exact_no),
            stringResource(R.string.ob_allow),
        )
        Step.CALENDAR -> Texts(
            stringResource(R.string.ob_calendar_title),
            stringResource(R.string.ob_calendar_body),
            stringResource(R.string.ob_calendar_no),
            stringResource(R.string.ob_connect),
        )
        Step.BATTERY -> Texts(
            stringResource(R.string.ob_battery_title),
            stringResource(R.string.ob_battery_body),
            stringResource(R.string.ob_battery_no),
            stringResource(R.string.ob_open_settings),
        )
        Step.OVERLAY_NOTICE -> Texts(
            stringResource(R.string.ob_notice_title),
            stringResource(R.string.ob_notice_body),
            stringResource(R.string.ob_notice_no),
            stringResource(R.string.ob_notice_yes),
        )
        Step.DONE -> Texts(
            stringResource(R.string.ob_done_title),
            stringResource(if (Settings.canDrawOverlays(ctx)) R.string.ob_done_body else R.string.ob_done_body_no_overlay),
            null,
            stringResource(R.string.ob_finish),
        )
    }

    fun onYes() {
        when (step) {
            // Antes de seguir, el nombre de la mascota elegida (si todavía no se le preguntó).
            Step.WELCOME -> if (!prefs.petNameAsked(pet)) {
                naming = pet
                nameAfter = true
            } else next()
            Step.DONE -> next()
            Step.OVERLAY -> ctx.startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${ctx.packageName}")),
            )
            Step.NOTIFICATIONS -> notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            Step.EXACT_ALARMS -> ctx.startActivity(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${ctx.packageName}")),
            )
            Step.CALENDAR -> calendarPermission.launch(Manifest.permission.READ_CALENDAR)
            // Por defecto solo se lleva al usuario a los ajustes; no se usa REQUEST_IGNORE_BATTERY_OPTIMIZATIONS.
            Step.BATTERY -> openAppSettings(ctx)
            // No se puede saber si lo apagó: se abre el ajuste y se sigue.
            Step.OVERLAY_NOTICE -> {
                openOverlayNoticeSettings(ctx)
                next()
            }
        }
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Text(
                stringResource(R.string.ob_step, index + 1, steps.size),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Image(
                petPainter(pet, if (step == Step.DONE) PetView.HAPPY else PetView.FRONT, prefs.coat(pet)),
                contentDescription = null,
                modifier = Modifier.size(170.dp),
            )
            // Globo de la mascota: lo que explica en primera persona.
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(body, style = MaterialTheme.typography.bodyLarge)
                    if (ifNo != null) {
                        Text(
                            ifNo,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            if (step == Step.BATTERY) BatteryGuide(Modifier.fillMaxWidth())
            if (step == Step.WELCOME) {
                PetPicker(pet) {
                    pet = it
                    prefs.pet = it
                    prefs.freePets = setOf(it)
                    if (!prefs.petNameAsked(it)) naming = it
                }
                Text(
                    stringResource(R.string.ob_free_pet_hint, com.petnotifications.BuildConfig.TRIAL_DAYS),
                    style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(4.dp))
            Button(onClick = ::onYes, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text(yes) }
            if (ifNo != null) {
                TextButton(onClick = ::next, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(if (step == Step.CALENDAR) R.string.ob_not_now_calendar else R.string.ob_not_now))
                }
            }
            naming?.let { p ->
                NamingDialog(p, prefs) {
                    naming = null
                    namesVersion++
                    if (nameAfter) {
                        nameAfter = false
                        next()
                    }
                }
            }
            if (step == Step.WELCOME) {
                TextButton(onClick = { finish(ctx, prefs, onDone) }) {
                    Text(stringResource(R.string.ob_skip), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun NamingDialog(pet: PetType, prefs: Prefs, onDone: () -> Unit) =
    PetNameDialog(pet, prefs.petName(pet).orEmpty()) {
        prefs.setPetName(pet, it)
        onDone()
    }

private data class Texts(val title: String, val body: String, val ifNo: String?, val yes: String)

/** Cierra la configuración: si hay permiso flotante, la mascota aparece; los avisos quedan programados. */
private fun finish(ctx: android.content.Context, prefs: Prefs, onDone: () -> Unit) {
    prefs.onboardingDone = true
    // La mascota elegida al instalar es la del plan gratis (si no la cambió, la que venía).
    if (prefs.freePets.isEmpty()) prefs.freePets = setOf(prefs.pet)
    if (Settings.canDrawOverlays(ctx)) {
        prefs.petEnabled = true
        PetOverlayService.start(ctx)
    }
    onDone()
}
