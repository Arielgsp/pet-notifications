package com.petnotifications.ui

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petnotifications.PetType
import com.petnotifications.Prefs
import com.petnotifications.R
import com.petnotifications.pro.ProBilling

/** Texto corto del estado de Pro: comprado, días de prueba que quedan o plan gratis. */
@Composable
fun proStatusText(prefs: Prefs): String {
    val now = System.currentTimeMillis()
    return when {
        prefs.proPurchased -> stringResource(R.string.pro_status_owned)
        prefs.trialActive(now) -> {
            val days = ((prefs.trialEndsAt - now + Prefs.DAY_MS - 1) / Prefs.DAY_MS).toInt()
            if (days <= 1) stringResource(R.string.pro_status_trial_last)
            else stringResource(R.string.pro_status_trial, days)
        }
        else -> stringResource(R.string.pro_status_free)
    }
}

/** La pantalla que muestra qué es gratis y qué es Pro, con los botones de compra. */
@Composable
fun ProDialog(onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    val status by ProBilling.status.collectAsStateWithLifecycle()
    val price by ProBilling.price.collectAsStateWithLifecycle()
    ProBilling.changes.collectAsStateWithLifecycle().value // se redibuja al comprar o restaurar

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pro_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(proStatusText(prefs), fontWeight = FontWeight.Bold)
                Row(Modifier.padding(top = 6.dp)) {
                    Text(stringResource(R.string.pro_col_feature), Modifier.weight(1.4f), style = MaterialTheme.typography.labelLarge)
                    Text(stringResource(R.string.pro_col_free), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                    Text(stringResource(R.string.pro_col_pro), Modifier.weight(0.8f), style = MaterialTheme.typography.labelLarge)
                }
                HorizontalDivider()
                val yes = stringResource(R.string.pro_yes)
                val no = stringResource(R.string.pro_no)
                CompareRow(stringResource(R.string.pro_row_pets), stringResource(R.string.pro_row_pets_free), stringResource(R.string.pro_row_pets_pro))
                CompareRow(stringResource(R.string.pro_row_reminders), yes, yes)
                CompareRow(stringResource(R.string.pro_row_pet_life), yes, yes)
                CompareRow(stringResource(R.string.pro_row_insist), stringResource(R.string.pro_row_insist_free), stringResource(R.string.pro_row_insist_pro))
                CompareRow(stringResource(R.string.pro_row_calendar), no, yes)
                CompareRow(stringResource(R.string.pro_row_voice), no, yes)
                CompareRow(stringResource(R.string.pro_row_bedtime), no, yes)
                CompareRow(stringResource(R.string.pro_row_ghost), yes, yes)
                CompareRow(stringResource(R.string.pro_row_look), no, yes)
                if (!prefs.proPurchased) {
                    Text(
                        stringResource(R.string.pro_one_time),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Button(
                        onClick = { (ctx as? Activity)?.let(ProBilling::buy) },
                        enabled = status != ProBilling.Status.WORKING,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(price?.let { stringResource(R.string.pro_buy, it) } ?: stringResource(R.string.pro_buy_no_price))
                    }
                    TextButton(onClick = { ProBilling.restore(ctx) }, enabled = status != ProBilling.Status.WORKING) {
                        Text(stringResource(R.string.pro_restore))
                    }
                }
                when (status) {
                    ProBilling.Status.WORKING -> R.string.pro_working
                    ProBilling.Status.UNAVAILABLE -> R.string.pro_unavailable
                    ProBilling.Status.PENDING -> R.string.pro_pending
                    ProBilling.Status.ERROR -> R.string.pro_error
                    ProBilling.Status.IDLE -> null
                }?.let { Text(stringResource(it), style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.done_button)) } },
    )
}

@Composable
private fun CompareRow(feature: String, free: String, pro: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(feature, Modifier.weight(1.4f).padding(end = 6.dp), style = MaterialTheme.typography.bodySmall)
        Text(free, Modifier.weight(1f).padding(end = 6.dp), style = MaterialTheme.typography.bodySmall)
        Text(pro, Modifier.weight(0.8f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

/**
 * Envuelve una opción Pro: sin Pro se ve apagada, con la etiqueta "PRO", y tocarla abre los planes
 * en vez de cambiar la opción.
 */
@Composable
fun ProLock(isPro: Boolean, onLocked: () -> Unit, content: @Composable () -> Unit) {
    if (isPro) {
        content()
        return
    }
    val lockedText = stringResource(R.string.pro_locked)
    Box(Modifier.semantics { contentDescription = lockedText }) {
        Box(Modifier.alpha(0.45f)) { content() }
        Box(Modifier.matchParentSize().clickable(onClick = onLocked))
        ProBadge(Modifier.align(Alignment.TopEnd))
    }
}

@Composable
fun ProBadge(modifier: Modifier = Modifier) = ProTag(modifier)

/**
 * Elegir las 2 mascotas gratis. Al terminar la prueba es obligatorio (la que usaba viene marcada) y
 * el botón secundario abre los planes. Para el único cambio gratis ([changing]), se puede cancelar.
 */
@Composable
fun FreePetChooser(
    initial: Set<PetType>,
    onChosen: (Set<PetType>) -> Unit,
    onSeePlans: () -> Unit,
    changing: Boolean = false,
    onCancel: () -> Unit = {},
) {
    var chosen by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = { if (changing) onCancel() },
        title = { Text(stringResource(if (changing) R.string.free_pets_change_title else R.string.free_pets_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    stringResource(if (changing) R.string.free_pets_change_body else R.string.free_pets_body),
                    style = MaterialTheme.typography.bodyMedium,
                )
                MultiPetPicker(chosen) { p ->
                    chosen = when {
                        p in chosen -> chosen - p
                        chosen.size < Prefs.FREE_PET_COUNT -> chosen + p
                        else -> chosen
                    }
                }
                Text(stringResource(R.string.free_pets_count, chosen.size), style = MaterialTheme.typography.labelLarge)
            }
        },
        confirmButton = {
            TextButton(onClick = { onChosen(chosen) }, enabled = chosen.size == Prefs.FREE_PET_COUNT) {
                Text(stringResource(R.string.free_pets_confirm))
            }
        },
        dismissButton = {
            if (changing) TextButton(onClick = onCancel) { Text(stringResource(R.string.cancel)) }
            else TextButton(onClick = onSeePlans) { Text(stringResource(R.string.pro_see_plans)) }
        },
    )
}

@Composable
private fun MultiPetPicker(chosen: Set<PetType>, onToggle: (PetType) -> Unit) {
    // Reusa el selector de la pantalla principal, marcando varias.
    PetPicker(selected = chosen, locked = emptySet(), onSelect = onToggle)
}
