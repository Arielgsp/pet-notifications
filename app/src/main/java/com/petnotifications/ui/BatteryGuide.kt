package com.petnotifications.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.petnotifications.R

/**
 * Marcas con pasos propios para que el sistema no cierre la app. [slug] es la página de
 * dontkillmyapp.com (se abre en el navegador: la app no necesita permiso de internet).
 */
enum class PhoneBrand(val displayName: String, val slug: String?, @StringRes val steps: Int) {
    SAMSUNG("Samsung", "samsung", R.string.battery_steps_samsung),
    MOTOROLA("Motorola", "motorola", R.string.battery_steps_motorola),
    XIAOMI("Xiaomi", "xiaomi", R.string.battery_steps_xiaomi),
    OTHER("", null, R.string.battery_steps_other);

    companion object {
        fun current(): PhoneBrand {
            val m = "${Build.MANUFACTURER} ${Build.BRAND}".lowercase()
            return when {
                "samsung" in m -> SAMSUNG
                "motorola" in m || "lenovo" in m -> MOTOROLA
                "xiaomi" in m || "redmi" in m || "poco" in m -> XIAOMI
                else -> OTHER
            }
        }

        /** Página de dontkillmyapp.com de la marca real, aunque no tenga pasos propios en la app. */
        fun pageSlug(): String {
            val manufacturer = Build.MANUFACTURER.lowercase().trim()
            return current().slug ?: manufacturer.replace(Regex("[^a-z]"), "").ifBlank { "" }
        }
    }
}

/** Pasos de batería para la marca de este celular, con accesos a los ajustes y a la guía web. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BatteryGuide(modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val brand = PhoneBrand.current()
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            if (brand == PhoneBrand.OTHER) stringResource(R.string.battery_guide_title_generic)
            else stringResource(R.string.battery_guide_title, brand.displayName),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(stringResource(brand.steps), style = MaterialTheme.typography.bodyMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (brand == PhoneBrand.XIAOMI) {
                OutlinedButton(onClick = { openAutoStartSettings(ctx) }) { Text(stringResource(R.string.battery_open_autostart)) }
            }
            OutlinedButton(onClick = { openAppSettings(ctx) }) { Text(stringResource(R.string.battery_open_app_settings)) }
            OutlinedButton(onClick = { openGuidePage(ctx) }) { Text(stringResource(R.string.battery_open_web_guide)) }
        }
    }
}

/** Ficha de la app en los ajustes del sistema (ahí están Batería, Inicio automático, etc.). */
fun openAppSettings(ctx: Context) {
    ctx.startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${ctx.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}

/** La lista de "Inicio automático" de Xiaomi; si no se puede abrir, la ficha de la app. */
fun openAutoStartSettings(ctx: Context) {
    val opened = PhoneBrand.current() == PhoneBrand.XIAOMI && runCatching {
        ctx.startActivity(
            Intent().setComponent(
                ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }.isSuccess
    if (!opened) openAppSettings(ctx)
}

/** El celular se reinició y no dejó arrancar a la app. */
@Composable
fun BootMissedDialog(onOpen: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.boot_missed_title)) },
        text = { Text(stringResource(R.string.boot_missed_body)) },
        confirmButton = { TextButton(onClick = onOpen) { Text(stringResource(R.string.boot_missed_open)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.boot_missed_later)) } },
    )
}

private fun openGuidePage(ctx: Context) {
    val slug = PhoneBrand.pageSlug()
    val url = if (slug.isBlank()) "https://dontkillmyapp.com/" else "https://dontkillmyapp.com/$slug"
    runCatching {
        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
