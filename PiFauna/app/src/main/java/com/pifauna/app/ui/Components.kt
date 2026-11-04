package com.pifauna.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Route names for the bottom navigation, matching the Figma footer labels. */
enum class Tab(val label: String, val glyph: String) {
    Home("Home", "⌂"),
    Profiles("Profiles", "◫"),
    Settings("Settings", "⚙")
}

/**
 * Standard page frame: wordmark, optional hero card, scrollable body.
 * Mirrors the 390x844 Figma artboard with 20 dp side margins.
 */
@Composable
fun PfScreen(
    title: String? = null,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Layaswarna",
            color = PiFaunaColors.Accent,
            fontWeight = FontWeight.Bold,
            fontSize = 21.sp
        )
        Spacer(Modifier.height(24.dp))
        if (title != null && subtitle != null) {
            HeroHeader(title = title, subtitle = subtitle)
        }
        content()
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
fun HeroHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(152.dp)
            .background(PiFaunaColors.Hero, RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = PiFaunaColors.BodyText
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            fontSize = 15.sp,
            color = PiFaunaColors.HeroSubtitle
        )
    }
}

/** Small uppercase eyebrow above a group of cards, e.g. "EXPLORE". */
@Composable
fun SectionLabel(text: String) {
    Spacer(Modifier.height(22.dp))
    Text(
        text = text.uppercase(),
        modifier = Modifier.padding(start = 5.dp, bottom = 12.dp),
        color = PiFaunaColors.Accent,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 1.sp
    )
}

/**
 * A 76 dp rounded card. The whole card is the tap target (>= 48 dp), and the
 * click role is exposed so TalkBack announces it as a button.
 */
@Composable
fun PfCard(
    title: String,
    subtitle: String? = null,
    enabled: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
    chakraPetals: Int? = null,
    onClick: (() -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    val clickModifier = if (onClick != null) {
        Modifier
            .semantics {
                role = Role.Button
                contentDescription = if (subtitle != null) "$title. $subtitle" else title
            }
            .clickable(enabled = enabled) { onClick() }
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 76.dp)
            .background(PiFaunaColors.Card, RoundedCornerShape(20.dp))
            .then(clickModifier)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = if (enabled) PiFaunaColors.BodyText
                else PiFaunaColors.Muted.copy(alpha = 0.55f)
            )
            if (subtitle != null) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = if (enabled) PiFaunaColors.Muted
                    else PiFaunaColors.Muted.copy(alpha = 0.45f)
                )
            }
            if (content != null) {
                Spacer(Modifier.height(10.dp))
                content()
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        } else if (chakraPetals != null) {
            Spacer(Modifier.width(12.dp))
            ChakraMark(petals = chakraPetals, modifier = Modifier.size(40.dp))
        }
    }
    Spacer(Modifier.height(16.dp))
}

/** Non-interactive paragraph card used by the informational screens. */
@Composable
fun PfTextCard(heading: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PiFaunaColors.Card, RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(heading, fontSize = 16.sp, color = PiFaunaColors.BodyText)
        Spacer(Modifier.height(6.dp))
        Text(body, fontSize = 13.sp, lineHeight = 19.sp, color = PiFaunaColors.Muted)
    }
    Spacer(Modifier.height(16.dp))
}

/** Single-line status message, coloured by severity. */
@Composable
fun StatusBanner(message: String, danger: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (danger) PiFaunaColors.Danger.copy(alpha = 0.12f)
                else PiFaunaColors.Accent.copy(alpha = 0.10f),
                RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = message,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = if (danger) PiFaunaColors.Danger else PiFaunaColors.Accent
        )
    }
}

/** Bottom navigation matching the Figma footer: text glyphs, no icon pack needed. */
@Composable
fun PfBottomBar(selected: Tab, onSelect: (Tab) -> Unit) {
    Column(Modifier.fillMaxWidth().background(PiFaunaColors.Background)) {
        HorizontalDivider(color = PiFaunaColors.Divider)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Tab.entries.forEach { tab ->
                val active = tab == selected
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .clickable { onSelect(tab) }
                        .padding(vertical = 6.dp)
                        .semantics { role = Role.Tab },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tab.glyph,
                        fontSize = 16.sp,
                        color = if (active) PiFaunaColors.Accent else PiFaunaColors.Muted
                    )
                    Text(
                        text = tab.label,
                        fontSize = 12.sp,
                        color = if (active) PiFaunaColors.Accent else PiFaunaColors.Muted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/** Original geometry drawn at runtime; no uploaded reference artwork or third-party asset. */
@Composable
fun ChakraMark(petals: Int, modifier: Modifier = Modifier) {
    val ink = PiFaunaColors.Accent
    val secondary = PiFaunaColors.Lotus
    Canvas(modifier = modifier.semantics { contentDescription = "Decorative lotus symbol" }) {
        val radius = size.minDimension * 0.43f
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(color = ink, radius = radius, center = center, style = Stroke(width = 1.5.dp.toPx()))
        drawCircle(color = secondary, radius = radius * 0.63f, center = center, style = Stroke(width = 1.dp.toPx()))
        val count = petals.coerceIn(2, 24)
        repeat(count) { index ->
            val angle = 2.0 * Math.PI * index / count
            val px = center.x + cos(angle).toFloat() * radius * 0.76f
            val py = center.y + sin(angle).toFloat() * radius * 0.76f
            drawCircle(color = secondary, radius = radius * 0.14f, center = Offset(px, py), style = Stroke(width = 1.dp.toPx()))
        }
        drawCircle(color = ink, radius = radius * 0.17f, center = center)
    }
}
