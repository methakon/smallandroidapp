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
 * Colour tokens lifted from the PiFauna Figma file
 * (https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX).
 *
 * The design defines a single dark canvas, so PiFauna ships one appearance.
 */
object PiFaunaColors {
    val Background = Color(0xFF0C211D)
    val Hero = Color(0xFF174338)
    val Card = Color(0xFF18352F)
    val Divider = Color(0xFF31574C)
    val Accent = Color(0xFF75E2B8)
    val BodyText = Color(0xFFF0FAF5)
    val Muted = Color(0xFFA8C9BE)
    val HeroSubtitle = Color(0xFFB6D9CA)
    val Warning = Color(0xFFFFD08A)
    val Danger = Color(0xFFFF9E9E)
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
