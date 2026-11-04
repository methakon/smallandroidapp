package com.pifauna.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Low-glare Layaswarna palette from docs/LAYASWARNA-COLOR-ACCESSIBILITY.md.
 * Visual theme only: does not change audio or safety behavior.
 */
object PiFaunaColors {
    val Background = Color(0xFF241D28)
    val Hero = Color(0xFF65414A)
    val Card = Color(0xFF40313F)
    val Divider = Color(0xFF725A65)
    val Accent = Color(0xFFD6A080)
    val BodyText = Color(0xFFF4E8E3)
    val Muted = Color(0xFFE6D1CB)
    val HeroSubtitle = Color(0xFFE6D1CB)
    val Warning = Color(0xFFE6BA85)
    val Danger = Color(0xFFF0A6A6)
    val Action = Color(0xFF78444F)
    val OnAction = Color(0xFFFFFFFF)
    val Lotus = Color(0xFFB67D91)
}

/**
 * Figma specifies Inter; Android ships Roboto. Bundling Inter would add font
 * assets for a cosmetic difference, so the system sans-serif is used and the
 * substitution is recorded in docs/FIGMA-IMPLEMENTATION.md.
 */
private val PiFaunaTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 25.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 26.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp)
)

@Composable
fun PiFaunaTheme(content: @Composable () -> Unit) {
    val scheme = darkColorScheme(
        primary = PiFaunaColors.Accent,
        onPrimary = PiFaunaColors.Background,
        secondary = PiFaunaColors.HeroSubtitle,
        onSecondary = PiFaunaColors.Background,
        background = PiFaunaColors.Background,
        onBackground = PiFaunaColors.BodyText,
        surface = PiFaunaColors.Card,
        onSurface = PiFaunaColors.BodyText,
        surfaceVariant = PiFaunaColors.Hero,
        onSurfaceVariant = PiFaunaColors.Muted,
        outline = PiFaunaColors.Divider,
        error = PiFaunaColors.Danger,
        onError = PiFaunaColors.Background
    )
    MaterialTheme(colorScheme = scheme, typography = PiFaunaTypography, content = content)
}
