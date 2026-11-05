package com.petnotifications.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petnotifications.Accessory
import com.petnotifications.Outfit
import com.petnotifications.PetType
import com.petnotifications.PetView
import com.petnotifications.R
import com.petnotifications.Slot
import com.petnotifications.petPainter

/*
 * El Vestidor (Design: Vestidor): el escenario de la pestaña Mascota, con la mascota grande y los tres
 * lugares, y la hoja Vestir, con la grilla de accesorios (del vínculo y de temporada) y el probador: lo
 * que está a la venta se ve puesto solo acá hasta que se compra.
 */

/**
 * Escenario: un panel rosa con la mascota grande y, adentro a la derecha, los lugares ([onSlot]: null los
 * esconde, en la vista previa de otra mascota; [activeSlot]: el que se está vistiendo). [banner]: "Vista previa".
 */
@Composable
fun WardrobeStage(
    pet: PetType, coat: Int, outfit: Outfit, view: PetView, description: String, banner: String?, activeSlot: Slot?, onTap: () -> Unit,
    onSlot: ((Slot) -> Unit)?,
) {
    val c = Pet.colors
    val shape = RoundedCornerShape(24.dp)
    Box(Modifier.fillMaxWidth().height(270.dp).background(c.blushSoft, shape).padding(12.dp)) {
        Image(
            petPainter(pet, view, coat, outfit),
            contentDescription = description,
            modifier = Modifier
                .align(if (onSlot != null) Alignment.CenterStart else Alignment.Center)
                .padding(end = if (onSlot != null) 72.dp else 0.dp)
                .size(226.dp)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onTap),
        )
        if (banner != null) {
            Text(
                banner, fontSize = 12.sp, fontWeight = FontWeight(800), color = c.cocoa,
                modifier = Modifier.align(Alignment.TopStart).background(c.surfaceRaised, RoundedCornerShape(50))
                    .dashedBorder(c.cocoa, 50.dp).padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
        if (onSlot != null) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.CenterEnd),
            ) {
                listOf(Slot.HEAD to R.string.slot_head, Slot.NECK to R.string.slot_neck, Slot.BED to R.string.slot_bed).forEach { (slot, name) ->
                    SlotTile(stringResource(name), outfit[slot], slot == activeSlot) { onSlot(slot) }
                }
            }
        }
    }
}

/** Un lugar del escenario, cuadrado: el dibujo de lo puesto o un "+", y abajo su nombre. Tocarlo abre la hoja Vestir ahí. */
@Composable
private fun SlotTile(name: String, worn: Accessory?, active: Boolean, onClick: () -> Unit) {
    val c = Pet.colors
    val shape = RoundedCornerShape(16.dp)
    val a11y = stringResource(R.string.slot_a11y, name, worn?.let { stringResource(it.label) } ?: stringResource(R.string.slot_empty))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .size(76.dp)
            .clip(shape)
            .background(c.surfaceRaised)
            .border(if (active) 2.dp else 1.dp, if (active) c.cocoa else c.line, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .clearAndSetSemantics { contentDescription = a11y }
            .padding(2.dp),
    ) {
        if (worn != null) AccessoryIcon(worn, Modifier.size(46.dp, 38.dp))
        else Icon(Icons.Outlined.Add, contentDescription = null, tint = c.inkMuted, modifier = Modifier.size(30.dp))
        Text(name, fontSize = 11.sp, lineHeight = 13.sp, fontWeight = FontWeight(800), color = c.ink)
    }
}

/** Orden de la hoja Vestir; en cada lugar va primero "Sin nada". */
private val HEAD_ORDER = listOf(Accessory.XMAS_HAT, Accessory.PUMPKIN, Accessory.PUMPKIN_HELMET, Accessory.HAT, Accessory.CROWN)

/** Los accesorios de [slot] que se muestran, de [available], en el orden de la hoja. */
fun dressItems(slot: Slot, available: List<Accessory>): List<Accessory> =
    available.filter { it.slot == slot }.sortedBy { HEAD_ORDER.indexOf(it) }

/**
 * Hoja Vestir: sobre la pantalla Mascota, con la mascota visible arriba. Chips Cabeza · Cuello · Camita para
 * cambiar de lugar sin cerrarla, [grid] con lo de ese lugar y abajo [note] (cómo se gana uno bloqueado) y [footer].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DressSheet(slot: Slot, onSlot: (Slot) -> Unit, onDismiss: () -> Unit, note: String?, grid: @Composable () -> Unit, footer: @Composable () -> Unit) {
    val c = Pet.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = c.surface,
        scrimColor = Color.Transparent,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(Slot.HEAD to R.string.slot_head, Slot.NECK to R.string.slot_neck, Slot.BED to R.string.slot_bed).forEach { (s, name) ->
                    val on = s == slot
                    Text(
                        stringResource(name), fontSize = 13.sp, fontWeight = FontWeight(800), textAlign = TextAlign.Center,
                        color = if (on) c.surface else c.inkMuted,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (on) c.cocoa else c.surfaceRaised)
                            .border(1.dp, if (on) c.cocoa else c.line, RoundedCornerShape(50))
                            .clickable(role = Role.Tab) { onSlot(s) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
            }
            grid()
            if (note != null) Text(note, fontSize = 13.sp, fontWeight = FontWeight(700), color = c.inkMuted)
            footer()
        }
    }
}

/**
 * Grilla de 3 columnas, con "Sin nada" primero: puesto (borde cocoa y tilde), ganado o comprado (tocar lo
 * pone o lo saca), a la venta (precio de Google Play; tocarlo lo prueba, con borde punteado) y bloqueado por
 * el vínculo (atenuado, candado y "Nivel N"; tocarlo explica cómo se gana).
 */
@Composable
fun AccessoryGrid(
    slot: Slot, items: List<Accessory>, outfit: Outfit, level: Int, bought: Set<Accessory>, prices: Map<Accessory, String>, trying: Accessory?,
    onNone: () -> Unit, onTap: (Accessory) -> Unit,
) {
    val c = Pet.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        (listOf<Accessory?>(null) + items).chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(IntrinsicSize.Min)) {
                row.forEach { a ->
                    val isTrying = a != null && a == trying
                    val on = if (a == null) outfit[slot] == null && trying == null else outfit.wearing(a) && !isTrying
                    val locked = a != null && !a.forSale && a.level > level
                    val sale = a != null && a.forSale && a !in bought
                    val shape = RoundedCornerShape(16.dp)
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(shape)
                            .background(c.surfaceRaised)
                            .then(
                                if (isTrying) Modifier.dashedBorder(c.cocoa, 16.dp)
                                else Modifier.border(if (on) 2.dp else 1.dp, if (on) c.cocoa else c.line, shape),
                            )
                            .clickable { if (a == null) onNone() else onTap(a) }
                            .padding(top = 10.dp, bottom = 8.dp, start = 4.dp, end = 4.dp),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            if (a != null) AccessoryIcon(a, Modifier.size(54.dp, 44.dp).alpha(if (locked) 0.5f else 1f))
                            else Box(contentAlignment = Alignment.Center, modifier = Modifier.size(54.dp, 44.dp)) {
                                Icon(Icons.Outlined.Block, contentDescription = null, tint = c.inkMuted, modifier = Modifier.size(28.dp))
                            }
                            Text(
                                stringResource(a?.label ?: R.string.slot_none), fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight(800), color = c.ink,
                                textAlign = TextAlign.Center, maxLines = 2, modifier = Modifier.padding(top = 4.dp).alpha(if (locked) 0.55f else 1f),
                            )
                            if (a != null) Row(verticalAlignment = Alignment.CenterVertically) {
                                if (locked) Icon(Icons.Outlined.Lock, contentDescription = null, tint = c.inkMuted, modifier = Modifier.size(11.dp))
                                Text(
                                    when {
                                        locked -> stringResource(R.string.bond_level_chip, a.level)
                                        sale -> prices[a].orEmpty()
                                        on -> stringResource(R.string.acc_worn)
                                        a.forSale -> stringResource(R.string.acc_bought)
                                        else -> stringResource(R.string.acc_owned)
                                    },
                                    fontSize = 11.sp, fontWeight = FontWeight(if (sale) 900 else 700), color = if (sale) c.cocoa else c.inkMuted,
                                    modifier = Modifier.padding(start = if (locked) 3.dp else 0.dp),
                                )
                            }
                        }
                        if (on) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.align(Alignment.TopEnd).padding(end = 2.dp).size(18.dp).background(c.cocoa, CircleShape),
                            ) { Icon(Icons.Filled.Check, contentDescription = null, tint = c.surface, modifier = Modifier.size(12.dp)) }
                        }
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** Probador: Sacar y Comprar · precio. [note]: un aviso arriba (comprando, pendiente, sin conexión con Play). */
@Composable
fun TryBar(price: String, note: String?, busy: Boolean, onRemove: () -> Unit, onBuy: () -> Unit, modifier: Modifier = Modifier) {
    val c = Pet.colors
    Column(modifier.fillMaxWidth()) {
        if (note != null) Text(note, fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight(600), color = c.inkMuted, modifier = Modifier.padding(bottom = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onRemove, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.shop_remove), color = c.cocoa, fontWeight = FontWeight(800))
            }
            Button(
                onClick = onBuy, enabled = !busy, modifier = Modifier.weight(1.7f),
                colors = ButtonDefaults.buttonColors(containerColor = c.cocoa, contentColor = c.surface),
            ) { Text(stringResource(R.string.shop_buy, price), fontWeight = FontWeight(800), fontSize = 13.sp, maxLines = 1) }
        }
    }
}

private fun svg(d: String) = PathParser().parsePathString(d).toPath()

/** Dibujo chico de cada accesorio (los de la maqueta, lienzo 46×38). */
private val icons: Map<Accessory, List<Pair<androidx.compose.ui.graphics.Path, Color>>> by lazy {
    val sage = Color(0xFF7FA88B); val honey = Color(0xFFEBB854); val blush = Color(0xFFEFA9A0); val coral = Color(0xFFE8836F)
    val cream = Color(0xFFF7E6CC); val furDeep = Color(0xFF8A5A3B)
    mapOf(
        Accessory.SCARF to listOf(svg("M6 10 Q23 16 40 10 L24 32 Q23 34 22 32 Z") to coral),
        Accessory.HAT to listOf(
            svg("M8 28 Q8 8 23 8 Q38 8 38 28Z") to honey,
            svg("M9.5 25h27a3.5 3.5 0 0 1 0 7h-27a3.5 3.5 0 0 1 0-7z") to furDeep,
            svg("M27.5 7a4.5 4.5 0 1 1-9 0a4.5 4.5 0 1 1 9 0z") to cream,
        ),
        Accessory.BED to listOf(
            svg("M3 26a20 9 0 1 0 40 0a20 9 0 1 0-40 0z") to sage,
            svg("M9 23a14 5 0 1 0 28 0a14 5 0 1 0-28 0z") to cream,
        ),
        Accessory.XMAS_HAT to listOf(
            svg("M6 30 Q20 10 34 26 Q37 14 42 12 Q36 6 26 7 Q12 9 6 30Z") to blush,
            svg("M7.5 26h25a3.5 3.5 0 0 1 0 7h-25a3.5 3.5 0 0 1 0-7z") to cream,
            svg("M46 12a4 4 0 1 1-8 0a4 4 0 1 1 8 0z") to cream,
        ),
        Accessory.PUMPKIN to listOf(svg("M8 24a15 11 0 1 0 30 0a15 11 0 1 0-30 0z") to honey),
        Accessory.PUMPKIN_HELMET to listOf(
            svg("M5 24a18 12 0 1 0 36 0a18 12 0 1 0-36 0z") to honey,
            svg("M16 17 L20 23 L12 23Z") to Color(0xFF4A3228),
            svg("M30 17 L34 23 L26 23Z") to Color(0xFF4A3228),
            svg("M16 26 Q23 29 30 26 Q27 32 23 32 Q19 32 16 26Z") to Color(0xFF4A3228),
        ),
        Accessory.CROWN to listOf(9.0 to 22.0, 17.0 to 25.0, 25.0 to 26.0, 33.0 to 25.0, 39.0 to 22.0).mapIndexed { i, (x, y) ->
            svg("M${x - 4.5} ${y}a4.5 4.5 0 1 0 9 0a4.5 4.5 0 1 0-9 0z") to if (i % 2 == 0) blush else honey
        },
    )
}

@Composable
private fun AccessoryIcon(a: Accessory, modifier: Modifier) {
    val ink = Color(0xFF4A3228)
    Canvas(modifier) {
        val k = minOf(size.width / 46f, size.height / 38f)
        scale(k, k, pivot = androidx.compose.ui.geometry.Offset.Zero) {
            if (a == Accessory.CROWN) {
                drawPath(svg("M6 24 Q23 30 40 24"), ink, style = Stroke(4.6f))
                drawPath(svg("M6 24 Q23 30 40 24"), Color(0xFF7FA88B), style = Stroke(2.6f))
            }
            icons[a].orEmpty().forEach { (p, fill) ->
                drawPath(p, fill)
                drawPath(p, ink, style = Stroke(1.6f))
            }
            if (a == Accessory.PUMPKIN || a == Accessory.PUMPKIN_HELMET) {
                drawPath(svg("M23 13 V7"), ink, style = Stroke(4.4f, cap = StrokeCap.Round))
                drawPath(svg("M23 13 V7"), Color(0xFF8A5A3B), style = Stroke(2.4f, cap = StrokeCap.Round))
            }
            if (a == Accessory.PUMPKIN) {
                drawPath(svg("M17 15 Q16 24 17 33M29 15 Q30 24 29 33"), Color(0xFF8A5A3B).copy(alpha = 0.45f), style = Stroke(1.2f))
            }
            if (a == Accessory.SCARF) {
                for ((x, y) in listOf(18f to 15f, 28f to 15f, 23f to 22f)) drawCircle(Color(0xFFFFFDF9), 1.4f, androidx.compose.ui.geometry.Offset(x, y))
            }
        }
    }
}
