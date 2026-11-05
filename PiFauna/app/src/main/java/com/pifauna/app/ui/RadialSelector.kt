package com.pifauna.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.ui.res.painterResource
import `in`.berhampore.dhar.layaswarna.R
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

/** Nearest clockwise wheel item; the central readout is never selectable. */
internal fun radialIndexAt(x: Float, y: Float, width: Float, height: Float, count: Int): Int? {
    if (count !in 2..8 || width <= 0f || height <= 0f) return null
    val dx = x - width / 2f
    val dy = y - height / 2f
    if (hypot(dx, dy) < minOf(width, height) * 0.24f) return null
    val angle = atan2(dy, dx).toDouble() + PI / 2
    val turns = ((angle / (2 * PI) + 1.0) % 1.0)
    return (turns * count).roundToInt() % count
}

/** Each symbol is drawn from elementary geometry, not emoji, screenshots or third-party artwork. */
enum class RadialSymbol {
    MOSQUITO, INSECT, RODENT, PETS, BIRD, CUSTOM,
    SINE, SWEEP, PULSE, HOP,
    ROOT, SACRAL, SOLAR, HEART, THROAT, THIRD_EYE, CROWN,
    PINK_NOISE, WHITE_NOISE, SOFT_TONE, GENTLE_SWEEP,
    DURATION
}

data class RadialOption(
    val id: String,
    val label: String,
    val detail: String,
    val symbol: RadialSymbol,
    val tint: Color = PiFaunaColors.Accent
)

/** The centre is an information readout. Activation belongs to the node or labeled button. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LayaswarnaRadialSelector(
    options: List<RadialOption>,
    selectedId: String,
    onSelect: (String) -> Unit,
    onActivate: (String) -> Unit,
    actionLabel: String,
    enabled: Boolean = true,
    showActionButton: Boolean = true,
    modifier: Modifier = Modifier
) {
    require(options.size in 2..8) { "Radial selector supports two to eight options" }
    val active = options.firstOrNull { it.id == selectedId } ?: options.first()
    val positions = remember(options.size) {
        List(options.size) { index -> -PI / 2 + 2 * PI * index / options.size }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val diameter = minOf(maxWidth, 350.dp)
            val nodeSize = 58.dp
            val orbit = diameter * 0.36f
            Box(
                modifier = Modifier
                    .size(diameter)
                    .pointerInput(options, enabled) {
                        if (!enabled) return@pointerInput
                        detectDragGestures { change, _ ->
                            val dx = change.position.x - size.width / 2f
                            val dy = change.position.y - size.height / 2f
                            // Avoid changing selection while interacting with the centre readout.
                            radialIndexAt(
                                change.position.x, change.position.y,
                                size.width.toFloat(), size.height.toFloat(), options.size
                            )?.let { onSelect(options[it].id) }
                            // Drag detector crosses touch slop before consuming movement.
                            // Node combinedClickable long-press is then cancelled by rotation.
                            change.consume()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.matchParentSize()) {
                    val radius = size.minDimension / 2f
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                PiFaunaColors.Glow.copy(alpha = 0.32f),
                                PiFaunaColors.Glow.copy(alpha = 0.11f),
                                PiFaunaColors.Background.copy(alpha = 0f)
                            ),
                            center = center,
                            radius = radius
                        ),
                        radius = radius
                    )
                    drawCircle(
                        PiFaunaColors.Accent.copy(alpha = 0.8f),
                        radius = radius * 0.91f,
                        style = Stroke(1.7.dp.toPx())
                    )
                    drawCircle(
                        PiFaunaColors.Glow.copy(alpha = 0.8f),
                        radius = radius * 0.86f,
                        style = Stroke(1.dp.toPx())
                    )
                    drawCircle(
                        PiFaunaColors.Lotus.copy(alpha = 0.65f),
                        radius = radius * 0.72f,
                        style = Stroke(1.dp.toPx())
                    )
                    drawCircle(PiFaunaColors.Card.copy(alpha = 0.95f), radius * 0.49f)
                    drawCircle(PiFaunaColors.Accent.copy(alpha = 0.32f), radius * 0.49f, style = Stroke(1.dp.toPx()))
                    // Small deterministic starlight pinpoints (decorative, no animation).
                    repeat(25) { index ->
                        val angle = 2 * PI * index / 25.0
                        val spread = 0.62f + (index % 5) * 0.065f
                        drawCircle(
                            PiFaunaColors.Accent.copy(alpha = 0.25f + (index % 3) * 0.15f),
                            radius = (if (index % 4 == 0) 1.7.dp else 1.dp).toPx(),
                            center = Offset(
                                center.x + cos(angle).toFloat() * radius * spread,
                                center.y + sin(angle).toFloat() * radius * spread
                            )
                        )
                    }
                }

                Column(
                    modifier = Modifier.size(diameter * 0.42f).padding(horizontal = 3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("SELECTED", fontSize = 11.sp, color = PiFaunaColors.Muted, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(5.dp))
                    Text(
                        active.label,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = if (active.label.length > 11) 16.sp else 22.sp
                        ),
                        color = PiFaunaColors.BodyText,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(active.detail, fontSize = 14.sp, color = PiFaunaColors.Accent, textAlign = TextAlign.Center, maxLines = 2)
                }

                options.forEachIndexed { index, option ->
                    val theta = positions[index]
                    val x = diameter / 2 + orbit * cos(theta).toFloat() - nodeSize / 2
                    val y = diameter / 2 + orbit * sin(theta).toFloat() - nodeSize / 2
                    val isSelected = option.id == active.id
                    Box(
                        modifier = Modifier
                            .offset(x = x - (diameter - nodeSize) / 2, y = y - (diameter - nodeSize) / 2)
                            .size(nodeSize)
                            .shadow(if (isSelected) 10.dp else 0.dp, CircleShape, clip = false)
                            .background(if (isSelected) PiFaunaColors.Selected else PiFaunaColors.Card, CircleShape)
                            .border(if (isSelected) 3.dp else 1.5.dp, if (isSelected) PiFaunaColors.Accent else option.tint, CircleShape)
                            .semantics {
                                role = Role.Button
                                selected = isSelected
                                contentDescription = "${option.label}, ${option.detail}" + if (isSelected) ", selected" else ""
                            }
                            // Highlight on touch-down. combinedClickable must wait out the
                            // double-tap window before reporting a single tap, which would
                            // make every selection feel ~300 ms late; selecting on press
                            // removes that latency without changing activation behaviour.
                            .pointerInput(option.id, enabled) {
                                if (!enabled) return@pointerInput
                                awaitEachGesture {
                                    awaitFirstDown(
                                        requireUnconsumed = false,
                                        pass = PointerEventPass.Initial
                                    )
                                    onSelect(option.id)
                                }
                            }
                            .combinedClickable(
                                enabled = enabled,
                                onClick = { onSelect(option.id) },
                                onDoubleClick = { onSelect(option.id); onActivate(option.id) },
                                onLongClick = { onSelect(option.id); onActivate(option.id) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (option.symbol == RadialSymbol.DURATION) {
                            Text(option.label.substringBefore(' '), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = option.tint)
                        } else {
                            RadialPictogram(option.symbol, option.tint, Modifier.size(31.dp))
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Tap or rotate to select · hold still or double-tap to open",
            color = PiFaunaColors.Muted,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        if (showActionButton) {
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = { onActivate(active.id) },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PiFaunaColors.Accent,
                contentColor = PiFaunaColors.Background
            )
        ) {
            Text(actionLabel, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        }
    }
}

/** Distinct self-drawn symbols. Original geometry, independent of system glyph fonts. */
@Composable
fun RadialPictogram(symbol: RadialSymbol, tint: Color, modifier: Modifier = Modifier) {
    // These vector resources are translated from the exact Figma Mystic chakra glyph paths.
    val chakraDrawable = when (symbol) {
        RadialSymbol.ROOT -> R.drawable.ic_chakra_muladhara
        RadialSymbol.SACRAL -> R.drawable.ic_chakra_svadhisthana
        RadialSymbol.SOLAR -> R.drawable.ic_chakra_manipura
        RadialSymbol.HEART -> R.drawable.ic_chakra_anahata
        RadialSymbol.THROAT -> R.drawable.ic_chakra_vishuddha
        RadialSymbol.THIRD_EYE -> R.drawable.ic_chakra_ajna
        RadialSymbol.CROWN -> R.drawable.ic_chakra_sahasrara
        else -> null
    }
    if (chakraDrawable != null) {
        Image(
            painter = painterResource(chakraDrawable),
            contentDescription = null, // Parent wheel node supplies spoken name and selection.
            modifier = modifier
        )
        return
    }
    Canvas(modifier) {
        val s = size.minDimension
        val stroke = 1.9.dp.toPx()
        fun line(x: Float, y: Float, xx: Float, yy: Float) {
            drawLine(tint, Offset(x * s, y * s), Offset(xx * s, yy * s), stroke, cap = StrokeCap.Round)
        }
        fun ring(x: Float, y: Float, r: Float) {
            drawCircle(tint, s * r, Offset(x * s, y * s), style = Stroke(stroke))
        }
        fun petalRing(count: Int) {
            repeat(count) { i ->
                val a = 2 * PI * i / count
                ring((0.5 + cos(a) * 0.31).toFloat(), (0.5 + sin(a) * 0.31).toFloat(), 0.085f)
            }
        }
        fun triangle(up: Boolean = true) {
            val path = Path().apply {
                moveTo(0.50f * s, if (up) 0.20f * s else 0.80f * s)
                lineTo(0.18f * s, if (up) 0.78f * s else 0.22f * s)
                lineTo(0.82f * s, if (up) 0.78f * s else 0.22f * s)
                close()
            }
            drawPath(path, tint, style = Stroke(stroke))
        }
        when (symbol) {
            RadialSymbol.MOSQUITO -> {
                line(.50f,.19f,.50f,.85f); ring(.50f,.28f,.08f)
                line(.47f,.40f,.16f,.16f); line(.53f,.40f,.84f,.16f)
                for (y in listOf(.48f,.61f,.73f)) { line(.48f,y,.20f,y+.10f); line(.52f,y,.80f,y+.10f) }
            }
            RadialSymbol.INSECT -> {
                drawOval(tint, Offset(.10f*s,.16f*s), Size(.36f*s,.39f*s), style = Stroke(stroke))
                drawOval(tint, Offset(.54f*s,.16f*s), Size(.36f*s,.39f*s), style = Stroke(stroke))
                drawOval(tint, Offset(.20f*s,.52f*s), Size(.27f*s,.30f*s), style = Stroke(stroke))
                drawOval(tint, Offset(.53f*s,.52f*s), Size(.27f*s,.30f*s), style = Stroke(stroke))
                line(.50f,.25f,.50f,.82f); line(.50f,.26f,.34f,.12f); line(.50f,.26f,.66f,.12f)
            }
            RadialSymbol.RODENT -> {
                ring(.28f,.27f,.18f); ring(.72f,.27f,.18f); ring(.5f,.55f,.31f)
                ring(.39f,.49f,.025f); ring(.61f,.49f,.025f); ring(.50f,.63f,.045f)
                line(.41f,.65f,.10f,.61f); line(.59f,.65f,.90f,.61f)
                line(.41f,.70f,.15f,.77f); line(.59f,.70f,.85f,.77f)
            }
            RadialSymbol.PETS -> {
                drawOval(tint, Offset(.22f*s,.48f*s), Size(.56f*s,.36f*s))
                ring(.19f,.34f,.085f); ring(.40f,.23f,.085f)
                ring(.62f,.23f,.085f); ring(.82f,.34f,.085f)
            }
            RadialSymbol.BIRD -> {
                drawOval(tint, Offset(.17f*s,.32f*s), Size(.58f*s,.47f*s), style = Stroke(stroke))
                ring(.65f,.30f,.14f); ring(.69f,.27f,.025f)
                triangle(); line(.78f,.31f,.95f,.36f); line(.37f,.79f,.30f,.91f)
            }
            RadialSymbol.CUSTOM -> {
                line(.63f,.16f,.63f,.68f); line(.63f,.16f,.88f,.24f)
                drawOval(tint, Offset(.27f*s,.61f*s), Size(.36f*s,.20f*s), style = Stroke(stroke))
            }
            RadialSymbol.SINE, RadialSymbol.SWEEP, RadialSymbol.PULSE, RadialSymbol.HOP,
            RadialSymbol.PINK_NOISE, RadialSymbol.WHITE_NOISE, RadialSymbol.SOFT_TONE,
            RadialSymbol.GENTLE_SWEEP -> {
                val path = Path()
                repeat(45) { i ->
                    val x = i / 44f
                    val y = when (symbol) {
                        RadialSymbol.PULSE -> if ((i / 9) % 2 == 0) .30f else .70f
                        RadialSymbol.HOP -> .5f + .2f * sin(i * (if (i < 22) .28f else .62f))
                        RadialSymbol.WHITE_NOISE -> .5f + .23f * sin(i * 2.11f) * cos(i * .87f)
                        RadialSymbol.PINK_NOISE -> .5f + .15f * sin(i * .38f) + .07f * cos(i * .91f)
                        RadialSymbol.SWEEP, RadialSymbol.GENTLE_SWEEP -> .5f + .24f * sin(i * (.10f + i * .004f))
                        else -> .5f + .26f * sin(i * .3f)
                    }
                    if (i == 0) path.moveTo(.08f*s, y*s) else path.lineTo((.08f + .84f*x)*s, y*s)
                }
                drawPath(path, tint, style = Stroke(stroke, cap = StrokeCap.Round))
            }
            else -> {
                val petals = when (symbol) {
                    RadialSymbol.ROOT -> 4
                    RadialSymbol.SACRAL -> 6
                    RadialSymbol.SOLAR -> 10
                    RadialSymbol.HEART -> 12
                    RadialSymbol.THROAT -> 16
                    RadialSymbol.THIRD_EYE -> 2
                    RadialSymbol.CROWN -> 20
                    else -> 8
                }
                petalRing(petals)
                when (symbol) {
                    RadialSymbol.ROOT -> {
                        line(.32f,.32f,.68f,.32f);line(.68f,.32f,.68f,.68f)
                        line(.68f,.68f,.32f,.68f);line(.32f,.68f,.32f,.32f)
                    }
                    RadialSymbol.SACRAL -> {
                        ring(.50f,.50f,.19f); ring(.57f,.45f,.15f)
                    }
                    RadialSymbol.SOLAR -> triangle()
                    RadialSymbol.HEART -> { triangle(); triangle(false) }
                    RadialSymbol.THROAT -> ring(.50f,.50f,.21f)
                    RadialSymbol.THIRD_EYE -> {
                        drawOval(tint, Offset(.25f*s,.39f*s),Size(.5f*s,.22f*s),style=Stroke(stroke))
                        ring(.5f,.5f,.065f)
                    }
                    RadialSymbol.CROWN -> { ring(.50f,.50f,.22f); ring(.50f,.50f,.09f) }
                    else -> Unit
                }
            }
        }
    }
}
