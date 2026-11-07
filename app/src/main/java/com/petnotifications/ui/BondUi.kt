package com.petnotifications.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petnotifications.Accessory
import com.petnotifications.Bond
import com.petnotifications.Outfit
import com.petnotifications.PetType
import com.petnotifications.PetView
import com.petnotifications.R
import com.petnotifications.petPainter
import kotlin.math.roundToInt

/*
 * El camino en la app (pedido de Cowork del 07/11/2026, tableros «Camino · Tarjeta en Mascota» y
 * «Camino · Todo lo que aprende»): la tarjeta de la pestaña Mascota, la página con los 8 premios y el
 * festejo con la app abierta. Sin niveles a la vista: se muestra lo que viene, no el puntaje.
 */

/** Fondo de la barra de avance (Design: camino). */
private val Track = Color(0xFFF1E6D8)

/** El accesorio de un premio del camino (null en los trucos). */
fun Bond.Reward.accessory(): Accessory? = Accessory.of(this)

/** Título de la tarjeta: "Un poco más y Mimu estrena gorrito", "Lo próximo: camita" o "¡Mimu aprendió todo!". */
@Composable
private fun headline(name: String, hearts: Int): String {
    val next = Bond.next(hearts) ?: return stringResource(R.string.bond_all_done, name)
    val phrase = stringResource(next.phrase)
    return when {
        next.at - hearts > Bond.ALMOST -> stringResource(R.string.bond_next, stringResource(next.title).lowercase())
        next.kind == Bond.Kind.TRICK -> stringResource(R.string.bond_almost_trick, name, phrase)
        else -> stringResource(R.string.bond_almost_wear, name, phrase)
    }
}

@Composable
private fun leftText(hearts: Int): String {
    val next = Bond.next(hearts) ?: return stringResource(R.string.bond_path_done)
    val left = next.at - hearts
    return if (left == 1) stringResource(R.string.bond_left_one) else stringResource(R.string.bond_left_many, left)
}

/**
 * Tarjeta del camino en la pestaña Mascota: qué viene, la barra de avance hasta ese premio, cuántos
 * corazones faltan y la tira con los 8 premios. [onHow]: "¿Cómo gano corazones?"; [onAll]: "Ver todo".
 */
@Composable
fun BondPathCard(name: String, hearts: Int, enabled: Boolean = true, onHow: () -> Unit, onAll: () -> Unit) {
    val c = Pet.colors
    val shape = RoundedCornerShape(26.dp)
    val next = Bond.next(hearts)
    val pct = (Bond.progress(hearts) * 100).roundToInt()
    val progressA11y = if (next == null) stringResource(R.string.bond_path_done)
    else stringResource(R.string.bond_progress_a11y, stringResource(next.title), pct)
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(c.surfaceRaised)
            .border(1.dp, c.line, shape)
            .padding(16.dp),
    ) {
        Text(headline(name, hearts), fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight(800), color = c.ink)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clearAndSetSemantics { contentDescription = progressA11y }) {
            Box(Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(50)).background(Track)) {
                Box(Modifier.fillMaxWidth(pct / 100f).height(10.dp).clip(RoundedCornerShape(50)).background(c.blush))
            }
            Spacer(Modifier.size(8.dp))
            HeartMark(18.dp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(leftText(hearts), fontSize = 13.sp, fontWeight = FontWeight(700), color = c.inkMuted, modifier = Modifier.weight(1f))
            LinkText(stringResource(R.string.bond_how_link), enabled, onHow)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.line))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.bond_learns_with_you, name), fontSize = 12.sp, fontWeight = FontWeight(800), color = c.inkMuted,
                modifier = Modifier.weight(1f),
            )
            LinkText(stringResource(R.string.bond_see_all), enabled, onAll, small = true)
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Bond.REWARDS.forEach { r ->
                val earned = hearts >= r.at
                val isNext = r == next
                val a11y = stringResource(
                    when {
                        earned -> R.string.bond_tile_got
                        isNext -> R.string.bond_tile_next
                        else -> R.string.bond_tile_later
                    },
                    stringResource(r.title),
                )
                val tile = RoundedCornerShape(10.dp)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(tile)
                        .background(
                            when {
                                earned -> c.sageSoft
                                isNext -> c.blushSoft
                                else -> c.line.copy(alpha = 0.5f)
                            },
                        )
                        .then(if (isNext) Modifier.border(2.dp, c.blush, tile) else Modifier)
                        .clearAndSetSemantics { contentDescription = a11y },
                ) { RewardArt(r, 26.dp, dim = !earned && !isNext) }
            }
        }
    }
}

/** Un enlace de texto en cocoa, con lugar para tocarlo cómodo. */
@Composable
private fun LinkText(text: String, enabled: Boolean, onClick: () -> Unit, small: Boolean = false) {
    Text(
        text, fontSize = if (small) 12.sp else 13.sp, fontWeight = FontWeight(800), color = Pet.colors.cocoa,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .heightIn(min = 32.dp)
            .padding(horizontal = 4.dp, vertical = 6.dp),
    )
}

/** Corazón rosa con borde cocoa (el del final de la barra y el de "Cómo gano corazones"). */
@Composable
private fun HeartMark(size: Dp) {
    val c = Pet.colors
    Canvas(Modifier.size(size)) {
        val k = this.size.width / 24f
        val p = heartPath(k)
        drawPath(p, c.blush)
        drawPath(p, c.cocoa, style = Stroke(1.5f * k, join = StrokeJoin.Round))
    }
}

private fun heartPath(k: Float) = Path().apply {
    moveTo(12f * k, 20.5f * k)
    lineTo(10.6f * k, 19.2f * k)
    cubicTo(5.4f * k, 14.5f * k, 2f * k, 11.4f * k, 2f * k, 7.6f * k)
    cubicTo(2f * k, 4.5f * k, 4.4f * k, 2f * k, 7.5f * k, 2f * k)
    cubicTo(9.2f * k, 2f * k, 10.9f * k, 2.8f * k, 12f * k, 4.1f * k)
    cubicTo(13.1f * k, 2.8f * k, 14.8f * k, 2f * k, 16.5f * k, 2f * k)
    cubicTo(19.6f * k, 2f * k, 22f * k, 4.5f * k, 22f * k, 7.6f * k)
    cubicTo(22f * k, 11.4f * k, 18.6f * k, 14.5f * k, 13.4f * k, 19.2f * k)
    close()
}

/**
 * Dibujo de un premio: el accesorio o, en los trucos, una patita o una nota musical. [dim]: todavía lejos
 * (al 30 %; los trucos, además, en gris).
 */
@Composable
fun RewardArt(r: Bond.Reward, size: Dp, dim: Boolean = false) {
    val mod = Modifier.size(size).then(if (dim) Modifier.alpha(0.3f) else Modifier)
    val acc = r.accessory()
    if (acc != null) {
        Box(mod, contentAlignment = Alignment.Center) { AccessoryIcon(acc, Modifier.size(size, size * 38f / 46f)) }
        return
    }
    val ink = Color(0xFF4A3228)
    val fill = if (dim) Color(0xFFBDB3AA) else if (r == Bond.Reward.PAW) Color(0xFFC98A55) else Color(0xFFEFA9A0)
    Canvas(mod) {
        val k = this.size.width / 24f
        val line = Stroke(1.4f * k, join = StrokeJoin.Round)
        if (r == Bond.Reward.PAW) {
            val tl = Offset(7f * k, 11.3f * k)
            drawOval(fill, tl, Size(10f * k, 8.4f * k))
            drawOval(ink, tl, Size(10f * k, 8.4f * k), style = line)
            for ((x, y) in listOf(6f to 10f, 9.5f to 6.5f, 14.5f to 6.5f, 18f to 10f)) {
                drawCircle(fill, 2.2f * k, Offset(x * k, y * k))
                drawCircle(ink, 2.2f * k, Offset(x * k, y * k), style = line)
            }
        } else {
            val stem = Path().apply {
                moveTo(9f * k, 17f * k); lineTo(9f * k, 5f * k); lineTo(20f * k, 3f * k); lineTo(20f * k, 15f * k)
            }
            drawPath(stem, ink, style = Stroke(1.6f * k, join = StrokeJoin.Round))
            for ((x, y) in listOf(6.5f to 17.5f, 17.5f to 15f)) {
                drawCircle(fill, 3f * k, Offset(x * k, y * k))
                drawCircle(ink, 3f * k, Offset(x * k, y * k), style = line)
            }
        }
    }
}

// ---------------------------------------------------------------- página del camino

/**
 * "Lo que Mimu aprende contigo": los 8 premios en dos columnas, con su estado, cómo se ganan corazones y
 * que nada se pierde.
 */
@Composable
fun BondPathPage(name: String, hearts: Int) {
    val c = Pet.colors
    val next = Bond.next(hearts)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(horizontal = 4.dp)) {
        Text(stringResource(R.string.bond_learns_with_you, name), fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight(900), color = c.ink)
        Text(
            if (hearts == 1) stringResource(R.string.bond_together_one) else stringResource(R.string.bond_together, hearts),
            fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight(600), color = c.inkMuted,
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Bond.REWARDS.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { r ->
                    val earned = hearts >= r.at
                    val isNext = r == next
                    val shape = RoundedCornerShape(18.dp)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clip(shape)
                            .background(if (isNext) c.blushSoft else c.surfaceRaised)
                            .border(if (isNext) 2.dp else 1.dp, if (isNext) c.blush else c.line, shape)
                            .padding(horizontal = 10.dp, vertical = 12.dp),
                    ) {
                        Box(Modifier.size(64.dp, 52.dp), contentAlignment = Alignment.Center) { RewardArt(r, 48.dp, dim = !earned && !isNext) }
                        Text(stringResource(r.title), fontSize = 14.sp, lineHeight = 19.sp, fontWeight = FontWeight(800), color = c.ink, textAlign = TextAlign.Center)
                        val left = r.at - hearts
                        Text(
                            when {
                                earned -> stringResource(R.string.bond_state_got)
                                isNext && left == 1 -> stringResource(R.string.bond_state_next_one)
                                isNext -> stringResource(R.string.bond_state_next, left)
                                else -> stringResource(R.string.bond_state_at, r.at)
                            },
                            fontSize = 12.sp, fontWeight = FontWeight(800), textAlign = TextAlign.Center,
                            color = if (isNext) c.cocoa else if (earned) c.ink else c.inkMuted,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (earned) c.sageSoft else if (isNext) c.surfaceRaised else Color.Transparent)
                                .padding(horizontal = 10.dp, vertical = 3.dp),
                        )
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
    BondWaysCard()
    Text(
        stringResource(R.string.bond_path_footer, name), fontSize = 13.sp, lineHeight = 19.sp, fontWeight = FontWeight(600),
        color = c.inkMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
    )
}

/** "Cómo gano corazones": las 4 formas, cada una con su "+1". */
@Composable
fun BondWaysCard() {
    val c = Pet.colors
    val shape = RoundedCornerShape(26.dp)
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().clip(shape).background(c.surfaceRaised).border(1.dp, c.line, shape).padding(16.dp),
    ) {
        Text(stringResource(R.string.bond_ways_title), fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight(800), color = c.ink)
        listOf(
            stringResource(R.string.bond_way_listo) to stringResource(R.string.bond_way_listo_note, Bond.DAILY_LISTO_CAP),
            stringResource(R.string.bond_way_first) to stringResource(R.string.bond_way_first_note),
            stringResource(R.string.bond_way_pet) to stringResource(R.string.bond_way_pet_note),
            stringResource(R.string.bond_way_visit) to stringResource(R.string.bond_way_visit_note),
        ).forEach { (title, note) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(36.dp).background(c.blushSoft, RoundedCornerShape(50)), contentAlignment = Alignment.Center) { HeartMark(18.dp) }
                Column(Modifier.weight(1f)) {
                    Text(title, fontSize = 14.sp, lineHeight = 19.sp, fontWeight = FontWeight(800), color = c.ink)
                    Text(note, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight(700), color = c.inkMuted)
                }
                Text(
                    "+1", fontSize = 13.sp, fontWeight = FontWeight(900), color = c.ink,
                    modifier = Modifier.background(c.sageSoft, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 3.dp),
                )
            }
        }
    }
}

// ---------------------------------------------------------------- festejos con la app abierta

/**
 * Festejo con la app abierta.
 * - Accesorio o juguete: la mascota ya vestida, unos corazones alrededor, "¡Estreno gorrito!", "Ahora no" y
 *   "Ponérselo" ([onWear]).
 * - Truco: "¡Aprendí a dar la patita!", cómo se usa, "Después" y "¡A ver!" ([onShow], la mascota lo hace).
 * - Varios de una vez: "¡Ya nos conocíamos!", lo que ganó, "Después" y "Ver todo" ([onAll]).
 * - Regalo de temporada: como un accesorio, con "¡Mirá lo que me trajeron!".
 */
@Composable
fun BondCelebrationDialog(
    c: Bond.Celebration, name: String, pet: PetType, coat: Int, outfit: Outfit,
    onWear: (Accessory) -> Unit, onShow: (Bond.Reward) -> Unit, onAll: () -> Unit, onDismiss: () -> Unit,
) {
    val colors = Pet.colors
    val single = (c as? Bond.Celebration.Earned)?.reward
    val gift = (c as? Bond.Celebration.Gift)?.accessory
    val acc = gift ?: single?.accessory()
    val title: String
    val body: String
    val primary: String
    val secondary: String
    val onPrimary: () -> Unit
    when {
        gift != null -> {
            title = stringResource(R.string.gift_xmas_title)
            body = stringResource(R.string.gift_xmas_body)
            primary = stringResource(R.string.bond_wear_ok)
            secondary = stringResource(R.string.bond_not_now)
            onPrimary = { onWear(gift) }
        }
        acc != null && single != null -> {
            title = stringResource(single.won)
            body = stringResource(R.string.bond_wear_body)
            primary = stringResource(if (acc == Accessory.BALL) R.string.bond_ball_ok else R.string.bond_wear_ok)
            secondary = stringResource(R.string.bond_not_now)
            onPrimary = { onWear(acc) }
        }
        single != null -> {
            title = stringResource(single.won)
            body = stringResource(Bond.trickHow(single))
            primary = stringResource(R.string.bond_see_it)
            secondary = stringResource(R.string.bond_later)
            onPrimary = { onShow(single) }
        }
        else -> {
            title = stringResource(R.string.bond_welcome_title)
            val list = c.rewards.map { stringResource(it.phrase) }.joinToString(", ")
            body = stringResource(R.string.bond_catchup_body, name, list)
            primary = stringResource(R.string.bond_see_all)
            secondary = stringResource(R.string.bond_later)
            onPrimary = onAll
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.paper,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                if (acc != null) {
                    Box(Modifier.size(170.dp, 160.dp)) {
                        // La camita solo se ve acostada: para estrenarla, duerme en ella.
                        val view = if (acc == Accessory.BED) PetView.SLEEP else PetView.HAPPY
                        Image(petPainter(pet, view, coat, outfit.with(acc)), contentDescription = null, modifier = Modifier.fillMaxSize())
                        listOf(8 to 30, 140 to 20, 20 to 112, 146 to 102, 70 to 0, 112 to 140).forEach { (x, y) ->
                            Icon(
                                Icons.Filled.Favorite, contentDescription = null, tint = colors.blush,
                                modifier = Modifier.offset(x.dp, y.dp).size(18.dp),
                            )
                        }
                    }
                } else if (single != null) {
                    Box(Modifier.size(64.dp).background(colors.blushSoft, RoundedCornerShape(50)), contentAlignment = Alignment.Center) { RewardArt(single, 40.dp) }
                }
                Text(
                    title, fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight(900), color = colors.ink, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        },
        text = {
            Text(
                body, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight(600), color = colors.inkMuted, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(onClick = onPrimary, colors = ButtonDefaults.buttonColors(containerColor = colors.cocoa, contentColor = colors.surfaceRaised)) {
                Text(primary, fontWeight = FontWeight(800))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(secondary, color = colors.cocoa, fontWeight = FontWeight(800)) }
        },
    )
}
