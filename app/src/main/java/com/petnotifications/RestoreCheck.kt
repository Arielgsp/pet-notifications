package com.petnotifications

import android.content.Context
import android.provider.Settings
import com.petnotifications.alerts.AlertScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * Copia de seguridad de Android: al pasar a un celular nuevo (o reinstalar), Android restaura los
 * recordatorios y los ajustes, pero **no** las alarmas, y no llega el aviso de reinicio. Para darse
 * cuenta se guarda una marca en `noBackupFilesDir`, la única carpeta que Android nunca copia: si la
 * marca falta pero hay datos, es una restauración.
 *
 * La prueba de Pro sigue contando desde la primera apertura original (viene en la copia), y Pro se
 * vuelve a confirmar con Google Play al abrir la app.
 */
object RestoreCheck {
    private const val MARKER = "install_marker"

    /** Se llama al abrir la app, antes de mostrar la pantalla. Devuelve true si hubo restauración. */
    fun run(ctx: Context): Boolean {
        val marker = File(ctx.noBackupFilesDir, MARKER)
        if (marker.exists()) return false
        runCatching { marker.writeText(System.currentTimeMillis().toString()) }
        val prefs = Prefs(ctx)
        // Sin datos previos es una instalación nueva: no hay nada que restaurar.
        if (!prefs.sp.contains(Prefs.KEY_ENABLED) && !prefs.sp.contains(Prefs.KEY_ONBOARDING)) return false
        // Hay datos y no hay marca: o se restauró una copia, o se actualizó desde una versión que no
        // ponía la marca. En los dos casos se reprograma todo (no hace daño).
        val app = ctx.applicationContext
        CoroutineScope(Dispatchers.IO).launch { AlertScheduler.rescheduleAll(app) }
        // Restauración: la app se instaló después de la primera apertura guardada (los datos vienen
        // de otra instalación). Al actualizar, en cambio, la instalación es anterior.
        val installedAt = runCatching { ctx.packageManager.getPackageInfo(ctx.packageName, 0).firstInstallTime }.getOrDefault(0L)
        val restored = prefs.firstOpenAt in 1 until installedAt
        if (!restored) return false
        // En el celular nuevo faltan los permisos: se vuelve a pasar por la configuración guiada.
        if (!Settings.canDrawOverlays(ctx)) prefs.onboardingDone = false
        // Los calendarios del celular anterior tienen otros números acá: se vuelven a incluir todos.
        prefs.calendarIds = emptySet()
        prefs.lastCalendarCheck = System.currentTimeMillis()
        return true
    }
}
