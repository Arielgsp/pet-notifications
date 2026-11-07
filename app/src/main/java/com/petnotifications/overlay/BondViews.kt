package com.petnotifications.overlay

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.ui.text.TextStyle
import com.petnotifications.ui.Nunito
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Acciones de la tarjeta flotante del vínculo; las maneja el servicio. */
class CardActions(
    val onPrimary: () -> Unit,
    val onSecondary: () -> Unit,
    val onTap: () -> Unit,
)

/**
 * Tarjeta flotante centrada arriba (Design: SubeDeNivel): el festejo de un nivel o el saludo de
 * buenos días. Entra con un pequeño rebote.
 */
@Composable
fun BondCardContent(card: BondCard, actions: CardActions) {
    val enter = remember(card.id) { Animatable(0f) }
    LaunchedEffect(card.id) { enter.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = 300f)) }
    if (card is BondCard.Trick) {
        TrickPaper(card, enter.value, actions)
        return
    }
    val shape = RoundedCornerShape(26.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .padding(12.dp)
            .graphicsLayer {
                alpha = enter.value.coerceIn(0f, 1f)
                translationY = (1f - enter.value) * -40f
            }
            .widthIn(max = 360.dp)
            .shadow(10.dp, shape, ambientColor = Ink.copy(alpha = 0.25f), spotColor = Ink.copy(alpha = 0.25f))
            .background(SurfaceRaised, shape)
            .border(1.dp, Line, shape)
            .clickable(role = Role.Button, onClick = actions.onTap)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) { ProvideTextStyle(TextStyle(fontFamily = Nunito)) {
        when (card) {
            is BondCard.Celebrate -> {
                Text(
                    card.chip, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Cocoa,
                    modifier = Modifier.background(BlushSoft, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 3.dp),
                )
                Text(card.title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Ink, textAlign = TextAlign.Center)
                Text(card.body, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = InkMuted, textAlign = TextAlign.Center)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                    CardButton(card.primary, filled = true, onClick = actions.onPrimary)
                    CardButton(card.secondary, filled = false, onClick = actions.onSecondary)
                }
            }
            is BondCard.Trick -> Unit
            is BondCard.Morning -> Text(
                card.text, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Ink, textAlign = TextAlign.Center,
            )
        }
    } }
}

/**
 * El truco nuevo (Design: Camino · Aprendió un truco): el mismo papelito del aviso, con la esquina doblada,
 * "¡Aprendí a dar la patita!", cómo se usa y "¡A ver!" / "Después".
 */
@Composable
private fun TrickPaper(card: BondCard.Trick, enter: Float, actions: CardActions) {
    val shape = remember { FoldedPaperShape(20.dp, 18.dp, mirrored = false) }
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .padding(12.dp)
            .graphicsLayer {
                alpha = enter.coerceIn(0f, 1f)
                translationY = (1f - enter) * 24f
                rotationZ = -2f
            }
            .widthIn(max = 260.dp)
            .drawWithContent {
                drawContent()
                drawFold(20.dp.toPx(), mirrored = false)
            }
            .shadow(8.dp, shape, ambientColor = Ink.copy(alpha = 0.35f), spotColor = Ink.copy(alpha = 0.35f))
            .background(Paper, shape)
            .clickable(role = Role.Button, onClick = actions.onTap)
            .padding(start = 24.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
    ) { ProvideTextStyle(TextStyle(fontFamily = Nunito)) {
        Text(card.title, fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
        Text(card.body, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold, color = InkMuted)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            PaperButton(card.primary, filled = true, onClick = actions.onPrimary)
            PaperButton(card.secondary, filled = false, onClick = actions.onSecondary)
        }
    } }
}

/** Botón del papelito: "¡A ver!" lleno en cocoa y "Después" con borde. */
@Composable
private fun PaperButton(text: String, filled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Text(
        text, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = if (filled) SurfaceRaised else Cocoa, fontFamily = Nunito,
        modifier = Modifier
            .heightIn(min = 36.dp)
            .background(if (filled) Cocoa else androidx.compose.ui.graphics.Color.Transparent, shape)
            .border(1.dp, if (filled) Cocoa else Line, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun CardButton(text: String, filled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Text(
        text, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Cocoa, fontFamily = Nunito,
        modifier = Modifier
            .heightIn(min = 40.dp)
            .background(if (filled) BlushSoft else SurfaceRaised, shape)
            .border(1.dp, if (filled) BlushSoft else Line, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
    )
}
