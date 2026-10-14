package com.petnotifications.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petnotifications.Bond
import com.petnotifications.R
import kotlin.math.roundToInt

/*
 * El vínculo en la app (Design: Vinculo): la tarjeta "Somos amigos" de la pestaña Mascota, la
 * página "Nuestro camino" y el festejo como diálogo cuando la app está abierta.
 */

/** Tarjeta del vínculo: nivel, 10 corazones de avance, lo próximo que aprende y "Ver el camino". */
@Composable
fun BondSummaryCard(hearts: Int, onSeePath: () -> Unit) {
    val c = Pet.colors
    val level = Bond.levelFor(hearts)
    val next = Bond.next(level)
    val (have, need) = Bond.progress(hearts)
    PetCard(padding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(level.title), fontSize = 17.sp, fontWeight = FontWeight(800), color = c.ink, modifier = Modifier.weight(1f))
            Text(
                stringResource(R.string.bond_level_chip, level.number),
                fontSize = 12.sp, fontWeight = FontWeight(800), color = c.cocoa,
                modifier = Modifier.background(c.blushSoft, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 3.dp),
            )
        }
        if (next != null) {
            val filled = if (need > 0) (have * 10f / need).roundToInt().coerceIn(0, 10) else 10
            val a11y = stringResource(R.string.bond_progress_a11y, next.number, if (need > 0) have * 100 / need else 100)
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(top = 10.dp).clearAndSetSemantics { contentDescription = a11y },
            ) {
                repeat(10) { i ->
                    Icon(
                        if (i < filled) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null, tint = if (i < filled) c.blush else c.line, modifier = Modifier.size(24.dp),
                    )
                }
            }
            val missing = next.from - hearts
            Text(
                if (missing == 1) stringResource(R.string.bond_missing_one, next.number)
                else stringResource(R.string.bond_missing_many, missing, next.number),
                fontSize = 13.sp, fontWeight = FontWeight(700), color = c.inkMuted, modifier = Modifier.padding(top = 4.dp),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 12.dp).fillMaxWidth().background(c.surface, RoundedCornerShape(16.dp)).padding(10.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(34.dp).background(c.sageSoft, RoundedCornerShape(10.dp))) {
                    Icon(Icons.Outlined.CardGiftcard, contentDescription = null, tint = c.cocoa, modifier = Modifier.size(18.dp))
                }
                Column(Modifier.padding(start = 12.dp)) {
                    Text(stringResource(R.string.bond_next_label), fontSize = 12.sp, fontWeight = FontWeight(600), color = c.inkMuted)
                    Text(
                        stringResource(next.reward.title) + if (next.reward.ready) "" else " · " + stringResource(R.string.bond_soon),
                        fontSize = 14.sp, fontWeight = FontWeight(800), color = c.ink,
                    )
                }
            }
        } else {
            Text(stringResource(R.string.bond_max), fontSize = 13.sp, fontWeight = FontWeight(700), color = c.inkMuted, modifier = Modifier.padding(top = 8.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
            Text(stringResource(R.string.bond_hint), fontSize = 12.sp, fontWeight = FontWeight(600), color = c.inkMuted, modifier = Modifier.weight(1f))
            OutlineChip(stringResource(R.string.bond_see_path_short), onClick = onSeePath)
        }
    }
}

/** "Nuestro camino": los 8 niveles en un riel; los ganados con tilde y el actual resaltado. */
@Composable
fun BondPathList(hearts: Int) {
    val c = Pet.colors
    val current = Bond.levelFor(hearts)
    Text(stringResource(R.string.bond_path_intro), fontSize = 14.sp, fontWeight = FontWeight(600), color = c.inkMuted, modifier = Modifier.padding(horizontal = 4.dp))
    Column {
        Bond.LEVELS.forEachIndexed { i, level ->
            val done = level.number < current.number
            val isCurrent = level == current
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp)
                    .background(if (isCurrent) c.blushSoft else c.surface, RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp),
            ) {
                // Riel: círculo con línea arriba y abajo (salvo en las puntas).
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
                    Box(Modifier.width(2.dp).height(14.dp).background(if (i == 0) androidx.compose.ui.graphics.Color.Transparent else c.line))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .background(if (done) c.cocoa else c.surfaceRaised, CircleShape)
                            .border(if (isCurrent) 2.dp else 1.5.dp, if (done || isCurrent) c.cocoa else c.line, CircleShape),
                    ) {
                        if (done) Icon(Icons.Outlined.Check, contentDescription = null, tint = c.surface, modifier = Modifier.size(18.dp))
                        else Text("${level.number}", fontSize = 13.sp, fontWeight = FontWeight(800), color = if (isCurrent) c.cocoa else c.inkMuted)
                    }
                    Box(Modifier.width(2.dp).height(14.dp).background(if (i == Bond.LEVELS.lastIndex) androidx.compose.ui.graphics.Color.Transparent else c.line))
                }
                Column(Modifier.weight(1f).padding(start = 12.dp, top = 8.dp, bottom = 8.dp)) {
                    Text(stringResource(level.name), fontSize = 14.sp, fontWeight = FontWeight(800), color = c.ink)
                    val reward = stringResource(level.reward.title) +
                        (if (!level.reward.ready) " · " + stringResource(R.string.bond_soon) else "") +
                        (if (isCurrent && level.number > 1) " · " + stringResource(R.string.bond_now) else "")
                    Text(reward, fontSize = 12.sp, fontWeight = FontWeight(600), color = c.inkMuted)
                }
                if (!done && !isCurrent) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${level.from}", fontSize = 13.sp, fontWeight = FontWeight(800), color = c.inkMuted)
                        Icon(Icons.Filled.Favorite, contentDescription = null, tint = c.blush, modifier = Modifier.padding(start = 2.dp).size(14.dp))
                    }
                }
            }
        }
    }
}

/**
 * Festejo del vínculo con la app abierta: la misma tarjeta que dibuja la mascota, como diálogo.
 * [onPrimary]: "Mostrame" (o "Ver nuestro camino" en el festejo de bienvenida).
 */
@Composable
fun BondCelebrationDialog(c: Bond.Celebration, onPrimary: () -> Unit, onDismiss: () -> Unit) {
    val colors = Pet.colors
    val level = when (c) {
        is Bond.Celebration.LevelUp -> c.level
        is Bond.Celebration.Welcome -> c.level
        is Bond.Celebration.Arrived -> c.level
    }
    val welcome = c is Bond.Celebration.Welcome
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceRaised,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.bond_chip, level.number, stringResource(level.name)),
                    fontSize = 12.sp, fontWeight = FontWeight(800), color = colors.cocoa,
                    modifier = Modifier.background(colors.blushSoft, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 3.dp),
                )
                Text(
                    stringResource(if (welcome) R.string.bond_welcome_title else level.reward.learned),
                    fontSize = 20.sp, fontWeight = FontWeight(800), color = colors.ink, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        },
        text = {
            Text(
                stringResource(if (welcome) level.title else level.reward.howTo),
                fontSize = 15.sp, fontWeight = FontWeight(600), color = colors.inkMuted, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = onPrimary) {
                Text(stringResource(if (welcome) R.string.bond_see_path else R.string.bond_show_me), color = colors.cocoa, fontWeight = FontWeight(800))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.bond_later), color = colors.inkMuted, fontWeight = FontWeight(700)) }
        },
    )
}
