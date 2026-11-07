package com.pifauna.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Sober, lower-saturation Modern-mystic Layaswarna palette (v13 design pass). Visual effects never relax audio-safety policy. */
object PiFaunaColors {
    val Background = Color(0xFF0E0D17)
    val BackgroundBottom = Color(0xFF181523)
    val Hero = Color(0xFF292435)
    val Card = Color(0xFF231F2E)
    val Divider = Color(0xFF625C70)
    val Accent = Color(0xFFDEC588)
    val BodyText = Color(0xFFF5F2F7)
    val Muted = Color(0xFFBEB6CA)
    val HeroSubtitle = Color(0xFFBEB6CA)
    val Warning = Color(0xFFFFD390)
    val Danger = Color(0xFFFFA9B2)
    val Action = Color(0xFF51475E)
    val OnAction = Color(0xFFF5F2F7)
    val Lotus = Color(0xFFC99AA7)
    val Glow = Color(0xFF947FCD)
    val Selected = Color(0xFF484057)
    val Chakra = listOf(
        Color(0xFFC8483E), Color(0xFFE07B39), Color(0xFFDEC588),
        Color(0xFF5FCB9A), Color(0xFF5BB6E8), Color(0xFF6F63E0),
        Color(0xFFB58CF0)
    )
}

/**
 * Android system serif is used for headings until a release-cleared
 * Cormorant Garamond asset is available; body uses the device's sans serif.
 */
private val PiFaunaTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 27.sp, lineHeight = 34.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 26.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp)
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
