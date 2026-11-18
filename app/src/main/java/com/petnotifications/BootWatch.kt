package com.petnotifications

import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import androidx.core.content.edit
import com.petnotifications.alerts.AlertScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

/**
 * Detecta que el celular se reinició sin avisarle a la app. Algunas marcas (Xiaomi, con "Inicio
 * automático" apagado) no entregan el aviso de arranque: la mascota no vuelve y las alarmas de los
 * recordatorios, que no sobreviven al reinicio, quedan sin programar.
 *
 * [BootReceiver] anota el número de arranque del sistema. Si al abrir la app ese número quedó atrás,
 * se reprograma todo y se le ofrece al usuario ir a activar el permiso. Se guarda fuera de la copia de
 * seguridad, porque el número de otro celular no sirve.
 */
object BootWatch {
    const val KEY_MISSED = "boot_missed"
    private const val FILE = "last_boot"

    /** El aviso de arranque puede tardar: antes de esto no se puede decir que no llegó. */
    private const val GRACE_MS = 3 * 60_000L

    @Volatile private var checking = false

    private fun current(ctx: Context) =
        Settings.Global.getInt(ctx.contentResolver, Settings.Global.BOOT_COUNT, -1)

    private fun file(ctx: Context) = File(ctx.noBackupFilesDir, FILE)
    private fun saved(ctx: Context) = runCatching { file(ctx).readText().trim().toInt() }.getOrNull()
    private fun save(ctx: Context, boot: Int) {
        runCatching { file(ctx).writeText(boot.toString()) }
    }

    /** Llegó el aviso de arranque. */
    fun markBoot(ctx: Context) {
        current(ctx).takeIf { it >= 0 }?.let { save(ctx, it) }
    }

    /** Al abrir la app. */
    fun check(ctx: Context) {
        val app = ctx.applicationContext
        val boot = current(app)
        if (boot < 0 || checking) return
        val last = saved(app)
        if (last == null) return save(app, boot) // primera vez con esta versión: solo se toma nota
        if (last == boot) return
        checking = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                delay((GRACE_MS - SystemClock.elapsedRealtime()).coerceAtLeast(0))
                if (saved(app) == boot) return@launch // el aviso llegó mientras tanto
                save(app, boot)
                Prefs(app).sp.edit { putBoolean(KEY_MISSED, true) }
                AlertScheduler.rescheduleAll(app)
            } finally {
                checking = false
            }
        }
    }
}
