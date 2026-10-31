package com.petnotifications.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings

/**
 * Android muestra una notificación fija "MimuPet se muestra sobre otras apps" mientras la
 * mascota está en pantalla. La app no puede sacarla, pero el usuario sí puede apagarla: Android la
 * publica en un canal propio para cada app, dentro de "Sistema Android".
 *
 * Abre, en este orden, lo más cercano que acepte el celular:
 * 1. ese canal (solo el de esta app);
 * 2. las notificaciones de "Sistema Android";
 * 3. los ajustes de notificaciones de esta app.
 */
fun openOverlayNoticeSettings(ctx: Context) {
    val channel = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, SYSTEM_PACKAGE)
        .putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_PREFIX + ctx.packageName)
    val systemApp = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, SYSTEM_PACKAGE)
    val ownApp = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
    for (intent in listOf(channel, systemApp, ownApp)) {
        try {
            ctx.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return
        } catch (_: ActivityNotFoundException) {
        } catch (_: SecurityException) {
        }
    }
}

private const val SYSTEM_PACKAGE = "android"
/** Nombre del canal que usa Android (AlertWindowNotification) para cada app: prefijo + paquete. */
private const val CHANNEL_PREFIX = "com.android.server.wm.AlertWindowNotification - "
