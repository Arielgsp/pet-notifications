package com.petnotifications

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.petnotifications.alerts.AlertScheduler
import com.petnotifications.overlay.PetOverlayService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Reprograma los avisos (las alarmas no sobreviven a un reinicio, y un cambio de hora o de permiso
 * las deja corridas) y vuelve a poner a la mascota tras reiniciar el teléfono o actualizar la app.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action !in HANDLED) return
        val ctx = context.applicationContext
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AlertScheduler.rescheduleAll(ctx)
            } finally {
                pending.finish()
            }
        }
        val restart = action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED
        val prefs = Prefs(ctx)
        if (restart && prefs.petEnabled && Settings.canDrawOverlays(ctx)) {
            // Pausada: las alarmas no sobreviven al reinicio, así que se vuelve a programar el regreso.
            if (prefs.petPaused()) AlertScheduler.schedulePetResume(ctx, prefs.petPausedUntil)
            else PetOverlayService.start(ctx, startedBy = PetOverlayService.STARTED_BY_BOOT)
        }
    }

    private companion object {
        val HANDLED = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED,
        )
    }
}
