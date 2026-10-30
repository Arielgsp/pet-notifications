package com.petnotifications.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.petnotifications.R

/**
 * Colores de Design (`pet-notifications-design/tokens.json`), con los mismos nombres. Se usan
 * directamente en las pantallas de la app (además de mapearse al esquema de Material).
 */
@Immutable
data class PetColors(
    val surface: Color,
    val surfaceRaised: Color,
    val paper: Color,
    val line: Color,
    val ink: Color,
    val inkMuted: Color,
    val cocoa: Color,
    val fur: Color,
    val blush: Color,
    val blushSoft: Color,
    val sage: Color,
    val sageSoft: Color,
    val honey: Color,
    val honeyDeep: Color,
    val honeySoft: Color,
)

private val LightTokens = PetColors(
    surface = Color(0xFFFBF6EE), surfaceRaised = Color(0xFFFFFDF9), paper = Color(0xFFFFF6E6),
    line = Color(0xFFE8DCCD), ink = Color(0xFF3A2E27), inkMuted = Color(0xFF6E5E52), cocoa = Color(0xFF8A5536),
    fur = Color(0xFFC98A55), blush = Color(0xFFEFA9A0), blushSoft = Color(0xFFFBE7E3),
    sage = Color(0xFF7FA88B), sageSoft = Color(0xFFE4F0E6),
    honey = Color(0xFFEBB854), honeyDeep = Color(0xFF8C5F0E), honeySoft = Color(0xFFFDF2D8),
)

private val DarkTokens = PetColors(
    surface = Color(0xFF1E1A17), surfaceRaised = Color(0xFF2A2420), paper = Color(0xFF332B24),
    line = Color(0xFF433A32), ink = Color(0xFFF4ECE2), inkMuted = Color(0xFFC0B1A3), cocoa = Color(0xFFE7B98E),
    fur = Color(0xFFC98A55), blush = Color(0xFFE79C92), blushSoft = Color(0xFF3A2826),
    sage = Color(0xFF8DB598), sageSoft = Color(0xFF203027),
    honey = Color(0xFFE6B24E), honeyDeep = Color(0xFFE6B24E), honeySoft = Color(0xFF3A301B),
)

val LocalPetColors = staticCompositionLocalOf { LightTokens }

/** Atajo: `Pet.colors.cocoa`. */
object Pet {
    val colors: PetColors @Composable get() = LocalPetColors.current
}

private fun scheme(t: PetColors, dark: Boolean) = if (dark) darkColorScheme(
    primary = t.cocoa, onPrimary = t.surface,
    primaryContainer = t.blushSoft, onPrimaryContainer = t.ink,
    secondaryContainer = t.blushSoft, onSecondaryContainer = t.cocoa,
    background = t.surface, onBackground = t.ink,
    surface = t.surface, onSurface = t.ink, onSurfaceVariant = t.inkMuted,
    surfaceContainer = t.surfaceRaised, surfaceContainerLow = t.surfaceRaised, surfaceContainerHigh = t.paper,
    surfaceContainerHighest = t.paper, surfaceContainerLowest = t.surface,
    outline = t.line, outlineVariant = t.line,
) else lightColorScheme(
    primary = t.cocoa, onPrimary = t.surface,
    primaryContainer = t.blushSoft, onPrimaryContainer = t.ink,
    secondaryContainer = t.blushSoft, onSecondaryContainer = t.cocoa,
    background = t.surface, onBackground = t.ink,
    surface = t.surface, onSurface = t.ink, onSurfaceVariant = t.inkMuted,
    surfaceContainer = t.surfaceRaised, surfaceContainerLow = t.surfaceRaised, surfaceContainerHigh = t.paper,
    surfaceContainerHighest = t.paper, surfaceContainerLowest = t.surface,
    outline = t.line, outlineVariant = t.line,
)

/** Nunito (fuente variable, licencia OFL) en los pesos que usa Design. */
@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private fun nunito(weight: Int) = Font(
    R.font.nunito, FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Nunito = FontFamily(nunito(400), nunito(600), nunito(700), nunito(800), nunito(900))

private val PetTypography = Typography().let { base ->
    fun TextStyle.n() = copy(fontFamily = Nunito)
    Typography(
        displayLarge = base.displayLarge.n(), displayMedium = base.displayMedium.n(), displaySmall = base.displaySmall.n(),
        headlineLarge = base.headlineLarge.n().copy(fontWeight = FontWeight(800)),
        headlineMedium = base.headlineMedium.n().copy(fontWeight = FontWeight(800)),
        headlineSmall = base.headlineSmall.n().copy(fontWeight = FontWeight(800)),
        titleLarge = base.titleLarge.n().copy(fontWeight = FontWeight(800)),
        titleMedium = base.titleMedium.n().copy(fontWeight = FontWeight(800)),
        titleSmall = base.titleSmall.n().copy(fontWeight = FontWeight(800)),
        bodyLarge = base.bodyLarge.n().copy(fontWeight = FontWeight(600)),
        bodyMedium = base.bodyMedium.n().copy(fontWeight = FontWeight(600)),
        bodySmall = base.bodySmall.n().copy(fontWeight = FontWeight(600), fontSize = 12.sp),
        labelLarge = base.labelLarge.n().copy(fontWeight = FontWeight(800)),
        labelMedium = base.labelMedium.n().copy(fontWeight = FontWeight(800)),
        labelSmall = base.labelSmall.n().copy(fontWeight = FontWeight(800), fontSize = 12.sp),
    )
}

/** [dark]: oscuro o claro; por defecto, como el celular (Prefs.theme elige en MainActivity). */
@Composable
fun PetTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val tokens = if (dark) DarkTokens else LightTokens
    CompositionLocalProvider(LocalPetColors provides tokens) {
        MaterialTheme(colorScheme = scheme(tokens, dark), typography = PetTypography, content = content)
    }
}
