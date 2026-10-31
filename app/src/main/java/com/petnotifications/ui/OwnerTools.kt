package com.petnotifications.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petnotifications.Bond
import com.petnotifications.Prefs
import com.petnotifications.R
import com.petnotifications.pro.ProBilling

/**
 * Herramientas de dueño, para probar sin pagar ni esperar: forzar Pro o el plan gratis y mover los
 * corazones del vínculo. Solo existe en las versiones de prueba (BuildConfig.OWNER_TOOLS); se abre
 * tocando 7 veces la versión en Ajustes.
 */
@Composable
fun OwnerToolsDialog(onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    val c = Pet.colors
    var plan by remember { mutableIntStateOf(prefs.ownerPlan) }
    var hearts by remember { mutableIntStateOf(Bond.hearts(ctx)) }
    var trialEnd by remember { mutableStateOf(false) }
    if (trialEnd) TrialEndDialog(prefs, onBuy = { trialEnd = false }, onStayFree = { trialEnd = false })

    fun setPlan(p: Int) {
        plan = p
        prefs.ownerPlan = p
        ProBilling.notifyChanged() // vuelve a armar las pantallas con el plan nuevo
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surfaceRaised,
        title = { Text(stringResource(R.string.owner_title), fontWeight = FontWeight(800)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.owner_note), fontSize = 13.sp, color = c.inkMuted)
                Text(stringResource(R.string.owner_plan), fontSize = 14.sp, fontWeight = FontWeight(800), color = c.ink, modifier = Modifier.padding(top = 6.dp))
                listOf(
                    Prefs.OWNER_NORMAL to R.string.owner_plan_normal,
                    Prefs.OWNER_PRO to R.string.owner_plan_pro,
                    Prefs.OWNER_FREE to R.string.owner_plan_free,
                ).forEach { (value, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().selectable(selected = plan == value, role = Role.RadioButton) { setPlan(value) },
                    ) {
                        RadioButton(selected = plan == value, onClick = null)
                        Text(stringResource(label), fontSize = 14.sp, color = c.ink, modifier = Modifier.padding(start = 8.dp))
                    }
                }
                HorizontalDivider(color = c.line, modifier = Modifier.padding(vertical = 6.dp))
                Text(stringResource(R.string.owner_bond), fontSize = 14.sp, fontWeight = FontWeight(800), color = c.ink)
                Text(
                    stringResource(R.string.owner_hearts, hearts, Bond.levelFor(hearts).number),
                    fontSize = 13.sp, color = c.inkMuted,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SoftChip(stringResource(R.string.owner_bond_add)) {
                        Bond.ownerAdd(ctx, 10)
                        hearts = Bond.hearts(ctx)
                    }
                    OutlineChip(stringResource(R.string.owner_bond_reset)) {
                        Bond.ownerReset(ctx)
                        hearts = Bond.hearts(ctx)
                    }
                }
                HorizontalDivider(color = c.line, modifier = Modifier.padding(vertical = 6.dp))
                OutlineChip(stringResource(R.string.owner_trial_end)) { trialEnd = true }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.owner_close), color = c.cocoa, fontWeight = FontWeight(800)) } },
    )
}
