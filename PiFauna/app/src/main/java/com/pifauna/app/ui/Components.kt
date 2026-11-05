package com.pifauna.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.border
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

/** Shared responsive Mystic page. Status/nav insets are supplied by the parent Scaffold. */
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
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Layaswarna",
            color = PiFaunaColors.Lotus,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )
        if (title != null) {
            Spacer(Modifier.height(15.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge.copy(fontFamily = FontFamily.Serif),
                color = PiFaunaColors.BodyText,
                maxLines = 3
            )
        }
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = PiFaunaColors.Muted
            )
        }
        content()
        Spacer(Modifier.height(32.dp))
    }
}

/** Optional hero banner for entry screens that need an explicit invitation. */
@Composable
fun HeroHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PiFaunaColors.Card, RoundedCornerShape(24.dp))
            .border(1.dp, PiFaunaColors.Accent.copy(alpha = 0.20f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge.copy(fontFamily = FontFamily.Serif),
            color = PiFaunaColors.BodyText
        )
        Spacer(Modifier.height(8.dp))
        Text(text = subtitle, fontSize = 14.sp, color = PiFaunaColors.Muted)
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
            .background(PiFaunaColors.Card, RoundedCornerShape(24.dp))
            .border(1.dp, PiFaunaColors.Accent.copy(alpha = 0.16f), RoundedCornerShape(24.dp))
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
                    fontSize = 14.sp,
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
            .background(PiFaunaColors.Card, RoundedCornerShape(24.dp))
            .border(1.dp, PiFaunaColors.Accent.copy(alpha = 0.16f), RoundedCornerShape(24.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(heading, fontSize = 16.sp, color = PiFaunaColors.BodyText)
        Spacer(Modifier.height(6.dp))
        Text(body, fontSize = 14.sp, lineHeight = 21.sp, color = PiFaunaColors.Muted)
    }
    Spacer(Modifier.height(16.dp))
}

/**
 * Glass-like panel used below the radial controls. Keeping all sections inside one
 * card mirrors the Figma Mystic insights panel and avoids repetitive tiles.
 */
@Composable
fun MysticInsightCard(
    title: String,
    sections: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PiFaunaColors.Card, RoundedCornerShape(24.dp))
            .border(1.dp, PiFaunaColors.Accent.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
            .padding(horizontal = 17.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            title.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.7.sp,
            color = PiFaunaColors.Accent
        )
        sections.forEach { (heading, body) ->
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    heading,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PiFaunaColors.BodyText
                )
                Text(
                    body,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = PiFaunaColors.Muted
                )
            }
        }
    }
    Spacer(Modifier.height(14.dp))
}

/** Visible alternative to long press and double tap, placed after the insight panel. */
@Composable
fun MysticActionButton(
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = PiFaunaColors.Accent,
            contentColor = PiFaunaColors.Background
        ),
        shape = RoundedCornerShape(26.dp)
    ) {
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.Bold)
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
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = if (danger) PiFaunaColors.Danger else PiFaunaColors.Accent
        )
    }
}

/** Three independent Material 3 navigation targets with selected state and safe-area insets. */
@Composable
fun PfBottomBar(selected: Tab, onSelect: (Tab) -> Unit) {
    NavigationBar(containerColor = PiFaunaColors.Hero, tonalElevation = 0.dp) {
        Tab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                icon = { NavigationSymbol(tab, selected == tab) },
                label = { Text(tab.label, fontSize = 12.sp) },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PiFaunaColors.Accent,
                    selectedTextColor = PiFaunaColors.Accent,
                    unselectedIconColor = PiFaunaColors.Muted,
                    unselectedTextColor = PiFaunaColors.Muted,
                    indicatorColor = PiFaunaColors.Selected
                )
            )
        }
    }
}

/** Original vector geometry; never depends on Unicode glyph availability. */
@Composable
private fun NavigationSymbol(tab: Tab, active: Boolean) {
    Canvas(Modifier.size(24.dp)) {
        val ink = if (active) PiFaunaColors.Accent else PiFaunaColors.Muted
        val s = size.minDimension
        val width = 2.dp.toPx()
        fun l(x: Float, y: Float, xx: Float, yy: Float) {
            drawLine(ink, Offset(s * x, s * y), Offset(s * xx, s * yy), width, cap = StrokeCap.Round)
        }
        when (tab) {
            Tab.Home -> {
                l(.12f, .48f, .5f, .15f); l(.5f, .15f, .88f, .48f)
                l(.23f, .43f, .23f, .86f); l(.77f, .43f, .77f, .86f)
                l(.23f, .86f, .77f, .86f); l(.44f, .86f, .44f, .63f)
            }
            Tab.Profiles -> {
                drawCircle(ink, radius = s * .32f, style = Stroke(width))
                drawCircle(ink, radius = s * .12f, style = Stroke(width))
                l(.5f, .05f, .5f, .18f); l(.5f, .82f, .5f, .95f)
            }
            Tab.Settings -> {
                drawCircle(ink, radius = s * .23f, style = Stroke(width))
                drawCircle(ink, radius = s * .06f)
                repeat(8) { i ->
                    val angle = 2 * Math.PI * i / 8
                    l(
                        (.5 + .33 * cos(angle)).toFloat(), (.5 + .33 * sin(angle)).toFloat(),
                        (.5 + .44 * cos(angle)).toFloat(), (.5 + .44 * sin(angle)).toFloat()
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
    Canvas(modifier = modifier) {
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

/**
 * Calm circular session control, independently implemented from generic progress-ring geometry.
 * Displayed time is the controller's monotonic countdown, never an independent playback timer.
 * Ring uses a descriptive button label; decorative marks are not interactive.
 */
@Composable
fun LayaswarnaSessionDial(
    title: String,
    subtitle: String,
    progress: Float,
    isPlaying: Boolean,
    enabled: Boolean = true,
    actionLabel: String? = null,
    onClick: () -> Unit
) {
    val fraction = progress.coerceIn(0f, 1f)
    val accent = PiFaunaColors.Accent
    val track = PiFaunaColors.Divider
    val action = actionLabel ?: if (isPlaying) "Stop playback" else "Start timed playback"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(214.dp)
                .semantics { role = Role.Button; contentDescription = "$action. $title" }
                .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 9.dp.toPx()
                val radius = size.minDimension / 2f - stroke
                drawCircle(color = track, radius = radius, style = Stroke(width = stroke))
                drawArc(
                    color = accent,
                    startAngle = -90f,
                    sweepAngle = 360f * fraction,
                    useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                drawCircle(color = PiFaunaColors.Lotus, radius = radius * 0.80f, style = Stroke(width = 1.dp.toPx()))
                // Four quiet, original carved-stone marker dots.
                repeat(4) { index ->
                    val angle = 2.0 * Math.PI * index / 4
                    val marker = Offset(
                        center.x + cos(angle).toFloat() * radius * 0.70f,
                        center.y + sin(angle).toFloat() * radius * 0.70f
                    )
                    drawCircle(color = PiFaunaColors.Lotus, radius = 2.dp.toPx(), center = marker)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = PiFaunaColors.BodyText,
                    maxLines = 1
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = PiFaunaColors.Muted,
                    maxLines = 2,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(9.dp))
                Text(
                    text = actionLabel ?: if (isPlaying) "■ Stop" else "▶ Start",
                    color = PiFaunaColors.Accent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * Reusable circular category button, not a playback or sensor dial.
 * The entire 128dp tile is clickable and announces the category and frequency band.
 * The center symbol is a generic Unicode pictograph, not third-party vector art.
 */
@Composable
fun LayaswarnaProfileCircle(
    label: String,
    symbol: String,
    description: String,
    petals: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 152.dp)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = "$label. $description"
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(108.dp)
                .background(PiFaunaColors.Card, RoundedCornerShape(54.dp))
                .border(2.dp, PiFaunaColors.Divider, RoundedCornerShape(54.dp)),
            contentAlignment = Alignment.Center
        ) {
            ChakraMark(
                petals = petals,
                modifier = Modifier.size(94.dp)
            )
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(PiFaunaColors.Background, RoundedCornerShape(27.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = symbol, fontSize = 29.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = label,
            color = PiFaunaColors.BodyText,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
        Text(
            text = description,
            color = PiFaunaColors.Muted,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}
