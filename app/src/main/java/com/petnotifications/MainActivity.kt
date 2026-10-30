package com.petnotifications

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import android.os.Bundle
import kotlinx.coroutines.launch
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import com.petnotifications.alerts.AlertScheduler
import com.petnotifications.pro.ProBilling
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.petnotifications.overlay.PetOverlayService
import com.petnotifications.ui.OnboardingScreen
import com.petnotifications.ui.TutorialScreen
import com.petnotifications.ui.PetTheme
import com.petnotifications.ui.SettingsScreen

class MainActivity : ComponentActivity() {

    /** Recordatorio a mostrar cuando la mascota abre la app desde un globo. */
    private val openReminderId = mutableStateOf<Long?>(null)

    /** Mostrar la lista de lo que se perdió con el celular apagado (desde el aviso resumen). */
    private val showMissed = mutableStateOf(false)

    /** Abrir directamente el editor de un recordatorio nuevo (tirón hacia abajo sobre la mascota). */
    private val newReminder = mutableStateOf(false)

    /** Abrir "Nuestro camino" (desde el festejo de bienvenida del vínculo). */
    private val bondPath = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProBilling.refresh(this)
        enableEdgeToEdge()
        readIntent(intent)
        val prefs = Prefs(this)
        // Antes de marcar la primera apertura: así se distingue una copia restaurada de una actualización.
        RestoreCheck.run(this)
        prefs.markFirstOpen() // desde acá corre la prueba gratis de Pro
        AlertScheduler.scheduleTrialNotice(this)
        // Vínculo: la primera vez se cuentan los "Listo" que ya había.
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch { Bond.seedIfNeeded(applicationContext) }
        // Si la mascota estaba prendida, vuelve aunque se esté mostrando la configuración guiada.
        if (prefs.petEnabled && !prefs.petPaused() && Settings.canDrawOverlays(this) && !PetOverlayService.running) {
            PetOverlayService.start(this)
        }
        setContent {
            // Tema elegido en Ajustes (o el del celular); cambia en el momento, sin reabrir la app.
            var theme by remember { mutableIntStateOf(prefs.theme) }
            DisposableEffect(Unit) {
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, k ->
                    if (k == Prefs.KEY_THEME) theme = prefs.theme
                }
                prefs.sp.registerOnSharedPreferenceChangeListener(listener)
                onDispose { prefs.sp.unregisterOnSharedPreferenceChangeListener(listener) }
            }
            val dark = when (theme) {
                Prefs.THEME_LIGHT -> false
                Prefs.THEME_DARK -> true
                else -> isSystemInDarkTheme()
            }
            // Íconos de la barra de estado y de navegación del color que corresponde al tema.
            LaunchedEffect(dark) {
                val style = if (dark) SystemBarStyle.dark(Color.TRANSPARENT)
                else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }
            PetTheme(dark) {
                var onboarding by remember { mutableStateOf(!prefs.onboardingDone) }
                // El tutorial sale una vez, al terminar la configuración (o al actualizar desde una versión sin él).
                var tutorial by remember { mutableStateOf(!prefs.tutorialSeen) }
                if (onboarding) {
                    OnboardingScreen(onDone = { onboarding = false })
                } else if (tutorial) {
                    TutorialScreen(onDone = { tutorial = false })
                } else {
                    // Al comprar o restaurar Pro se vuelve a armar la pantalla con las opciones desbloqueadas.
                    val proChanges by ProBilling.changes.collectAsState()
                    key(proChanges) { SettingsScreen(
                        openReminderId = openReminderId.value,
                        onReminderShown = { openReminderId.value = null },
                        showMissed = showMissed.value,
                        onMissedShown = { showMissed.value = false },
                        newReminder = newReminder.value,
                        onNewReminderShown = { newReminder.value = false },
                        onShowOnboarding = {
                            prefs.onboardingDone = false
                            onboarding = true
                        },
                        onShowTutorial = { tutorial = true },
                        openBondPath = bondPath.value,
                        onBondPathShown = { bondPath.value = false },
                    ) }
                }
            }
        }
    }

    // Con la app adelante, los festejos del vínculo los muestra la app (no la mascota encima).
    override fun onResume() {
        super.onResume()
        PetOverlayService.appVisible = true
    }

    override fun onPause() {
        PetOverlayService.appVisible = false
        super.onPause()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        readIntent(intent)
    }

    private fun readIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(PetOverlayService.EXTRA_SHOW_MISSED, false) == true) showMissed.value = true
        if (intent?.getBooleanExtra(PetOverlayService.EXTRA_NEW_REMINDER, false) == true) newReminder.value = true
        if (intent?.getBooleanExtra(PetOverlayService.EXTRA_BOND_PATH, false) == true) bondPath.value = true
        val id = intent?.getLongExtra(PetOverlayService.EXTRA_REMINDER_ID, -1L) ?: -1L
        if (id >= 0) {
            openReminderId.value = id
            // Abrir el recordatorio desde el aviso cuenta como respuesta: no se insiste más.
            val app = applicationContext
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                com.petnotifications.alerts.Insist.cancelForReminder(app, id)
            }
        }
    }
}
