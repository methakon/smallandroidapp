package com.pifauna.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pifauna.app.mantra.MantraRepetitionPolicy
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Original concentric yantra-inspired control. No borrowed film graphics.
 * Outer ring selects *sets of 108*, inner ring selects silence between chants.
 * Semantic alternatives are also provided alongside the dial via standard chips.
 */
@Composable
fun ConcentricJapaDial(
    selectedSets: Int,
    selectedGapMs: Long,
    enabled: Boolean = true,
    onSetsChanged: (Int) -> Unit,
    onGapChanged: (Long) -> Unit
) {
    val choices = MantraRepetitionPolicy.setChoices
    val gaps = MantraRepetitionPolicy.gapsMs
    val density = LocalDensity.current
    val side = 320.dp
    val centerDp = 160f
    fun position(radiusDp: Float, angle: Double): IntOffset {
        val pxX = with(density) { ((centerDp + radiusDp * cos(angle)).dp).roundToPx() }
        val pxY = with(density) { ((centerDp + radiusDp * sin(angle)).dp).roundToPx() }
        return IntOffset(pxX, pxY)
    }

    Box(modifier = Modifier.size(side), contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier.fillMaxSize().pointerInput(enabled) {
                detectTapGestures { tap ->
                    if (!enabled) return@detectTapGestures
                    val c = size.width / 2f
                    val r = hypot(tap.x - c, tap.y - c)
                    val d = r / density.density
                    val angle = (Math.toDegrees(atan2((tap.y - c).toDouble(), (tap.x - c).toDouble())) + 450.0) % 360.0
                    if (d in 119f..160f) {
                        val i = (angle / (360.0 / choices.size)).toInt().coerceIn(0, choices.lastIndex)
                        onSetsChanged(choices[i])
                    } else if (d in 66f..111f) {
                        val i = (angle / (360.0 / gaps.size)).toInt().coerceIn(0, gaps.lastIndex)
                        onGapChanged(gaps[i])
                    }
                }
            }
        ) {
            val outer = 139.dp.toPx()
            val inner = 89.dp.toPx()
            drawCircle(PiFaunaColors.Divider.copy(alpha = .75f), radius = outer + 18.dp.toPx(), style = Stroke(1.dp.toPx()))
            drawCircle(PiFaunaColors.Divider, radius = outer, style = Stroke(42.dp.toPx()))
            drawCircle(PiFaunaColors.Divider.copy(alpha = .7f), radius = inner, style = Stroke(40.dp.toPx()))
            drawCircle(PiFaunaColors.Accent.copy(alpha = .35f), radius = 57.dp.toPx(), style = Stroke(1.5.dp.toPx()))
            drawCircle(PiFaunaColors.Glow.copy(alpha = .45f), radius = 48.dp.toPx(), style = Stroke(1.dp.toPx()))
            for (i in choices.indices) {
                val sweep = 360f / choices.size
                val start = -90f + i * sweep
                val selected = choices[i] == selectedSets
                drawArc(
                    color = if (selected) PiFaunaColors.Accent else PiFaunaColors.Hero,
                    startAngle = start + 2f, sweepAngle = sweep - 4f,
                    useCenter = false,
                    topLeft = Offset(center.x - outer, center.y - outer),
                    size = androidx.compose.ui.geometry.Size(outer * 2, outer * 2),
                    style = Stroke(42.dp.toPx(), cap = StrokeCap.Butt)
                )
            }
            for (i in gaps.indices) {
                val sweep = 360f / gaps.size
                val start = -90f + i * sweep
                val selected = gaps[i] == selectedGapMs
                drawArc(
                    color = if (selected) PiFaunaColors.Glow else PiFaunaColors.Hero,
                    startAngle = start + 2f, sweepAngle = sweep - 4f,
                    useCenter = false,
                    topLeft = Offset(center.x - inner, center.y - inner),
                    size = androidx.compose.ui.geometry.Size(inner * 2, inner * 2),
                    style = Stroke(40.dp.toPx())
                )
            }
        }
        choices.forEachIndexed { index, value ->
            val angle = -PI / 2 + (index + .5) * 2 * PI / choices.size
            val offsetPx = position(139f, angle)
            Text(
                text = value.toString(),
                modifier = Modifier.offset {
                    IntOffset(offsetPx.x - with(density) { 20.dp.roundToPx() },
                        offsetPx.y - with(density) { 11.dp.roundToPx() })
                },
                color = if (value == selectedSets) PiFaunaColors.Background else PiFaunaColors.BodyText,
                fontSize = 14.sp, fontWeight = FontWeight.Bold
            )
        }
        gaps.forEachIndexed { index, value ->
            val angle = -PI / 2 + (index + .5) * 2 * PI / gaps.size
            val offsetPx = position(89f, angle)
            Text(
                text = if (value == 500L) ".5s" else "${value / 1000}s",
                modifier = Modifier.offset {
                    IntOffset(offsetPx.x - with(density) { 16.dp.roundToPx() },
                        offsetPx.y - with(density) { 10.dp.roundToPx() })
                },
                color = PiFaunaColors.BodyText,
                fontSize = 12.sp, fontWeight = FontWeight.Medium
            )
        }
        androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SETS", color = PiFaunaColors.Muted, fontSize = 10.sp)
            Text("${selectedSets} × 108", color = PiFaunaColors.Accent, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text("JAPA", color = PiFaunaColors.Muted, fontSize = 10.sp)
        }
    }
}
