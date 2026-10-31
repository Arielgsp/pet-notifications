package com.petnotifications.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.petnotifications.Prefs
import com.petnotifications.R

/**
 * Dirección que recibe los comentarios. Es la única que hay que cambiar cuando exista la casilla
 * definitiva de la app.
 */
const val FEEDBACK_EMAIL = "CAMBIAR@ejemplo.com"

/**
 * "Enviar comentarios": muestra el resumen técnico que va a ir en el mail y, si el usuario acepta,
 * abre su app de correo (ACTION_SENDTO con mailto:). La app no manda nada por su cuenta.
 */
@Composable
fun FeedbackDialog(onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    var summary by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { summary = technicalSummary(ctx) }
    val subject = stringResource(R.string.feedback_subject)
    val greeting = stringResource(R.string.feedback_greeting)
    val noApp = stringResource(R.string.feedback_no_email_app)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.feedback_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.feedback_body), style = MaterialTheme.typography.bodyMedium)
                Text(summary, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
                    .putExtra(Intent.EXTRA_EMAIL, arrayOf(FEEDBACK_EMAIL))
                    .putExtra(Intent.EXTRA_SUBJECT, subject)
                    .putExtra(Intent.EXTRA_TEXT, "$greeting\n\n\n\n---\n$summary")
                try {
                    ctx.startActivity(intent)
                    onDismiss()
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(ctx, noApp, Toast.LENGTH_LONG).show()
                }
            }) { Text(stringResource(R.string.feedback_send)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

/** Solo datos técnicos (los del diagnóstico): nada de títulos de recordatorios, eventos ni cuentas. */
private suspend fun technicalSummary(ctx: Context): String {
    val prefs = Prefs(ctx)
    val version = runCatching { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName }.getOrNull() ?: "?"
    val plan = when {
        prefs.proPurchased -> "Pro"
        prefs.trialActive() -> "prueba de Pro"
        else -> "gratis"
    }
    val r = diagReport(ctx)
    fun yn(b: Boolean) = if (b) "sí" else "no"
    return buildString {
        appendLine("App: $version (${ctx.packageName})")
        appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        appendLine("Celular: ${Build.MANUFACTURER} ${Build.MODEL}")
        appendLine("Idioma: ${ctx.resources.configuration.locales[0].toLanguageTag()}")
        appendLine("Plan: $plan")
        appendLine("Mascota prendida: ${yn(prefs.petEnabled)} · pausada: ${yn(prefs.petPaused())}")
        appendLine("Permisos: sobre otras apps ${yn(r.overlay)}, notificaciones ${yn(r.notifications)}, " +
            "alarmas exactas ${yn(r.exactAlarms)}, batería sin restricciones ${yn(r.battery)}, calendario ${yn(r.calendar)}")
        appendLine("Programados: recordatorios ${r.pendingReminders}, pospuestos ${r.pendingSnoozes}, " +
            "eventos 7 días ${r.calendarNext7Days?.toString() ?: "calendario apagado"}")
        appendLine("Avisos últimos $DIAG_DAYS días: ${r.delivered} (con mascota ${r.byPet}, por notificación ${r.delivered - r.byPet})")
        if (r.petKills.isNotEmpty()) {
            appendLine("Cierres inesperados de la mascota (30 días): ${r.petKills.size}; últimos: " +
                r.petKills.takeLast(3).joinToString { (at, by) -> "${java.util.Date(at)} ($by)" })
        }
        appendLine("Pruebas de mascotas Pro: ${r.tries}; compró Pro: " +
            if (r.triesBeforePurchase >= 0) "sí, después de ${r.triesBeforePurchase}" else "no")
        if (r.delivered > 0) {
            appendLine("Demora: promedio ${r.avgDelaySec} s, máxima ${r.maxDelaySec} s, más de 1 min tarde ${r.late}, sin alarmas exactas ${r.withoutExact}")
        }
    }.trimEnd()
}
