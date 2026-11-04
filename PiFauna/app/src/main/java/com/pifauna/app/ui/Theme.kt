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
