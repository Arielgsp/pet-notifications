package com.petnotifications.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petnotifications.PetType
import com.petnotifications.PetView
import com.petnotifications.R
import com.petnotifications.overlay.PetModel
import com.petnotifications.petPainter

/*
 * Piezas visuales de las pantallas de la app, según Design (PantallaAvisos / Mascota / Ajustes):
 * tarjetas `surface-raised` redondeadas, chips suaves, títulos de sección chicos en `ink-muted`.
 */

val CardShape = RoundedCornerShape(26.dp)

/** Tarjeta de la app: `surface-raised`, borde `line`, esquinas `radius-lg` y sombra suave. */
@Composable
fun PetCard(modifier: Modifier = Modifier, padding: Dp = 16.dp, content: @Composable ColumnScope.() -> Unit) {
    val c = Pet.colors
    Column(
        modifier
            .fillMaxWidth()
            .shadow(2.dp, CardShape, ambientColor = c.ink.copy(alpha = 0.12f), spotColor = c.ink.copy(alpha = 0.12f))
            .background(c.surfaceRaised, CardShape)
            .border(1.dp, c.line, CardShape)
            .padding(padding),
        content = content,
    )
}

/** Borde punteado (tarjeta de estado en pausa). */
fun Modifier.dashedBorder(color: Color, radius: Dp) = drawBehind {
    val r = radius.toPx()
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(r, r),
        style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))),
    )
}

/** Título de sección: 12/16 800 en `ink-muted`. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, fontSize = 12.sp, fontWeight = FontWeight(800), color = Pet.colors.inkMuted, modifier = modifier.padding(start = 4.dp))
}

/** Chip suave (`blush-soft`, texto `cocoa`): acción secundaria amable. */
@Composable
fun SoftChip(text: String, icon: ImageVector? = null, onClick: () -> Unit) =
    ChipBase(text, icon, Pet.colors.blushSoft, null, onClick)

/** Chip de borde (`line`, texto `cocoa`). */
@Composable
fun OutlineChip(text: String, icon: ImageVector? = null, onClick: () -> Unit) =
    ChipBase(text, icon, Pet.colors.surfaceRaised, Pet.colors.line, onClick)

@Composable
private fun ChipBase(text: String, icon: ImageVector?, bg: Color, border: Color?, onClick: () -> Unit) {
    val c = Pet.colors
    val shape = RoundedCornerShape(50)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .heightIn(min = 36.dp)
            .background(bg, shape)
            .then(if (border != null) Modifier.border(1.dp, border, shape) else Modifier)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = c.cocoa, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, fontSize = 13.sp, fontWeight = FontWeight(800), color = c.cocoa)
    }
}

/** Etiqueta "PRO" (`honey-deep` sobre `honey-soft`). */
@Composable
fun ProTag(modifier: Modifier = Modifier) {
    val c = Pet.colors
    Text(
        stringResource(R.string.pro_badge),
        fontSize = 11.sp, fontWeight = FontWeight(900), color = c.honeyDeep,
        modifier = modifier.background(c.honeySoft, RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

/** Fila de ajustes: ícono en cuadradito, título 13 800, resumen 12 600 y flecha (o algo a la derecha). */
@Composable
fun SettingRow(
    icon: ImageVector,
    title: String,
    summary: String?,
    summaryColor: Color? = null,
    onClick: (() -> Unit)?,
    trailing: (@Composable () -> Unit)? = null,
) {
    val c = Pet.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .heightIn(min = 56.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(34.dp).background(c.surface, RoundedCornerShape(10.dp)),
        ) { Icon(icon, contentDescription = null, tint = c.cocoa, modifier = Modifier.size(18.dp)) }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight(800), color = c.ink)
            if (summary != null) {
                Text(summary, fontSize = 12.sp, fontWeight = FontWeight(600), color = summaryColor ?: c.inkMuted)
            }
        }
        when {
            trailing != null -> trailing()
            onClick != null -> Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = c.inkMuted)
        }
    }
}

@Composable
fun RowDivider() = HorizontalDivider(color = Pet.colors.line, modifier = Modifier.padding(horizontal = 14.dp))

/** Ficha de mascota (72 dp): seleccionada con borde `cocoa` de 2 dp y tilde; bloqueada, punteada con candado. */
@Composable
fun PetTile(pet: PetType, selected: Boolean, locked: Boolean, coat: Int = 0, onClick: () -> Unit) {
    val c = Pet.colors
    val shape = RoundedCornerShape(18.dp)
    Box(Modifier.width(72.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    when {
                        locked -> Modifier.dashedBorder(c.line, 18.dp)
                        selected -> Modifier.background(c.surfaceRaised, shape).border(2.dp, c.cocoa, shape)
                        else -> Modifier.background(c.surfaceRaised, shape).border(1.dp, c.line, shape)
                    },
                )
                .clickable(role = Role.RadioButton, onClick = onClick)
                .padding(vertical = 8.dp),
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(44.dp)) {
                Image(
                    petPainter(pet, PetView.FRONT, coat), contentDescription = null,
                    modifier = Modifier.size(44.dp).alpha(if (locked) 0.35f else 1f),
                )
                if (locked) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(26.dp).background(c.line, CircleShape),
                    ) { Icon(Icons.Outlined.Lock, contentDescription = null, tint = c.inkMuted, modifier = Modifier.size(14.dp)) }
                }
            }
            Text(
                stringResource(pet.nameRes),
                fontSize = 12.sp, fontWeight = FontWeight(800),
                color = if (locked) c.inkMuted else c.ink,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
        if (selected && !locked) {
            Surface(
                shape = CircleShape, color = c.cocoa,
                modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp).size(20.dp),
            ) { Icon(Icons.Outlined.Check, contentDescription = null, tint = c.surface, modifier = Modifier.padding(3.dp)) }
        }
    }
}

/** Grilla simple de 4 columnas de fichas de mascota. */
@Composable
fun PetGrid(
    pets: List<PetType>, selected: PetType?, locked: Boolean, coatOf: (PetType) -> Int = { 0 }, onClick: (PetType) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        pets.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { p -> PetTile(p, selected = p == selected, locked = locked, coat = coatOf(p)) { onClick(p) } }
            }
        }
    }
}

/**
 * Pelajes de la mascota (Pro; el original es gratis): una ficha con su dibujo por pelaje, en una fila
 * que se desliza. Sin Pro, los otros pelajes se ven con candado y llevan a los planes.
 */
@Composable
fun CoatRow(pet: PetType, selected: Int, locked: Boolean, onClick: (Int) -> Unit) {
    val c = Pet.colors
    val shape = RoundedCornerShape(18.dp)
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PetModel.coats(pet).indices.forEach { i ->
            val lock = locked && i > 0
            val sel = i == selected && !lock
            Box(Modifier.width(64.dp)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            when {
                                lock -> Modifier.dashedBorder(c.line, 18.dp)
                                sel -> Modifier.background(c.surfaceRaised, shape).border(2.dp, c.cocoa, shape)
                                else -> Modifier.background(c.surfaceRaised, shape).border(1.dp, c.line, shape)
                            },
                        )
                        .clickable(role = Role.RadioButton, onClickLabel = stringResource(R.string.coat_n, i + 1)) { onClick(i) }
                        .padding(vertical = 8.dp),
                ) {
                    Image(
                        petPainter(pet, PetView.FRONT, i), contentDescription = stringResource(R.string.coat_n, i + 1),
                        modifier = Modifier.size(44.dp).alpha(if (lock) 0.35f else 1f),
                    )
                    if (lock) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(24.dp).background(c.line, CircleShape),
                        ) { Icon(Icons.Outlined.Lock, contentDescription = null, tint = c.inkMuted, modifier = Modifier.size(13.dp)) }
                    }
                }
                if (sel) {
                    Surface(
                        shape = CircleShape, color = c.cocoa,
                        modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp).size(20.dp),
                    ) { Icon(Icons.Outlined.Check, contentDescription = null, tint = c.surface, modifier = Modifier.padding(3.dp)) }
                }
            }
        }
    }
}

/** Deslizador fino de Design: riel de 4 dp (`cocoa` / `line`) y perilla `cocoa` de 18 dp. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetSlider(value: Float, onChange: (Float) -> Unit, onDone: () -> Unit, range: ClosedFloatingPointRange<Float>) {
    val c = Pet.colors
    val colors = SliderDefaults.colors(
        thumbColor = c.cocoa, activeTrackColor = c.cocoa, inactiveTrackColor = c.line,
        disabledThumbColor = c.inkMuted, disabledActiveTrackColor = c.inkMuted, disabledInactiveTrackColor = c.line,
    )
    Slider(
        value = value, onValueChange = onChange, onValueChangeFinished = onDone, valueRange = range, colors = colors,
        thumb = { Box(Modifier.size(18.dp).shadow(1.dp, CircleShape).background(c.cocoa, CircleShape).border(2.dp, c.surfaceRaised, CircleShape)) },
        track = { st ->
            SliderDefaults.Track(
                sliderState = st, colors = colors, modifier = Modifier.height(4.dp),
                drawStopIndicator = null, thumbTrackGapSize = 0.dp,
            )
        },
    )
}
