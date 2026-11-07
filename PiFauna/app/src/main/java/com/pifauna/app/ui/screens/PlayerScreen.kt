package com.pifauna.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pifauna.app.audio.ToneMode
import com.pifauna.app.data.SpeciesProfiles
import com.pifauna.app.playback.PlaybackPhase
import com.pifauna.app.playback.PlayerState
import com.pifauna.app.safety.SafetyLimits
import com.pifauna.app.ui.LayaswarnaSessionDial
import com.pifauna.app.ui.PfCard
import com.pifauna.app.ui.PfScreen
import com.pifauna.app.ui.PfTextCard
import com.pifauna.app.ui.PiFaunaColors
import com.pifauna.app.ui.SectionLabel
import com.pifauna.app.ui.StatusBanner
import com.pifauna.app.util.TimeFormat
import kotlinx.coroutines.delay

/** Figma 03 · Sound Player */
@Composable
fun PlayerScreen(
    state: PlayerState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    continueWhenLocked: Boolean,
    onContinueWhenLockedChange: (Boolean) -> Unit,
    onStartFrequencyChange: (Int) -> Unit,
    onEndFrequencyChange: (Int) -> Unit,
    onOpenTimer: () -> Unit
) {
    val profile = SpeciesProfiles.byId(state.profileId)
    val needsRange = state.mode == ToneMode.SWEEP || state.mode == ToneMode.HOP

    var remaining by remember { mutableLongStateOf(state.remainingMs()) }
    val current by rememberUpdatedState(state)

    // One tick per 500 ms only while playing: this drives the countdown label.
    // Stopping is enforced by the audio thread, never by this timer.
    LaunchedEffect(state.isPlaying, state.startedAtElapsed) {
        if (!state.isPlaying) {
            remaining = 0L
            return@LaunchedEffect
        }
        while (true) {
            remaining = current.remainingMs()
            if (remaining <= 0L) break
            delay(500)
        }
    }

    PfScreen(
        title = state.displayName ?: profile.name,
        subtitle = if (needsRange) {
            "Band • ${TimeFormat.band(state.startHz, state.endHz)}"
        } else {
            "Frequency • ${TimeFormat.frequency(state.startHz)}"
        }
    ) {
        SectionLabel("SOUND OUTPUT")

        PfCard(title = "Frequency • ${TimeFormat.frequency(state.startHz)}") {
            Slider(
                value = state.startHz.toFloat(),
                onValueChange = { onStartFrequencyChange(it.toInt()) },
                enabled = !state.isPlaying,
                valueRange = SafetyLimits.MIN_HZ.toFloat()..SafetyLimits.MAX_HZ.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = PiFaunaColors.Accent,
                    activeTrackColor = PiFaunaColors.Accent,
                    inactiveTrackColor = PiFaunaColors.Divider
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${SafetyLimits.MIN_HZ} Hz", fontSize = 11.sp, color = PiFaunaColors.Muted)
                Text("${SafetyLimits.MAX_HZ} Hz", fontSize = 11.sp, color = PiFaunaColors.Muted)
            }
        }

        if (needsRange) {
            PfCard(title = "End frequency • ${TimeFormat.frequency(state.endHz)}") {
                Slider(
                    value = state.endHz.toFloat(),
                    onValueChange = { onEndFrequencyChange(it.toInt()) },
                    enabled = !state.isPlaying,
                    valueRange = SafetyLimits.MIN_HZ.toFloat()..SafetyLimits.MAX_HZ.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = PiFaunaColors.Accent,
                        activeTrackColor = PiFaunaColors.Accent,
                        inactiveTrackColor = PiFaunaColors.Divider
                    )
                )
            }
        }

        PfCard(
            title = "Duration • ${state.durationMinutes} minutes",
            subtitle = "Tap to change",
            enabled = !state.isPlaying,
            onClick = if (state.isPlaying) null else onOpenTimer
        )

        SectionLabel("SCREEN-LOCK MODE")
        PfCard(
            title = "Continue timed playback with screen locked",
            subtitle = if (continueWhenLocked) {
                "ON — keeps this explicitly started session running until its timer or Stop."
            } else {
                "OFF — automatic screen lock stops the audio (default)."
            },
            enabled = !state.isPlaying,
            trailing = {
                Switch(
                    checked = continueWhenLocked,
                    onCheckedChange = onContinueWhenLockedChange,
                    enabled = !state.isPlaying
                )
            }
        )
        // Status sits above the action: a refusal must be visible at the moment the
        // user taps Start, not below the fold where the tap looks like a no-op.
        StatusBanner(
            message = state.statusMessage,
            danger = state.phase == PlaybackPhase.Refused
        )
        Spacer(Modifier.height(14.dp))

        LayaswarnaSessionDial(
            title = if (state.isPlaying) TimeFormat.clock(remaining) else "${state.durationMinutes}:00",
            subtitle = if (state.isPlaying) "Time remaining" else "Timed sound session",
            progress = if (state.isPlaying) {
                remaining.toFloat() / (state.durationMinutes * 60_000f)
            } else 1f,
            isPlaying = state.isPlaying,
            onClick = if (state.isPlaying) onStop else onStart
        )

        if (state.isPlaying) {
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Time remaining", fontSize = 13.sp, color = PiFaunaColors.Muted)
                Text(
                    text = TimeFormat.clock(remaining),
                    fontSize = 22.sp,
                    color = PiFaunaColors.Accent
                )
            }
        }

        PfTextCard(
            heading = "If enabled",
            body = "A foreground media notification shows a Stop action on the lock screen. " +
                "The battery, volume, thermal, audio-route and audio-focus limits stay active. " +
                "Never leave sound unattended around people or animals."
        )

        Spacer(Modifier.height(22.dp))
        SectionLabel("DEVICE STATE")
        PfTextCard(
            heading = "Checks before every start",
            body = "Battery ${state.snapshot.batteryPercent}% · " +
                "volume ${(state.snapshot.mediaVolumeRatio * 100).toInt()}% of maximum · " +
                (state.snapshot.externalAudioRoute?.let { "external route detected ($it)" } ?: "built-in speaker") +
                ". Playback is refused below ${SafetyLimits.BATTERY_CUTOFF_PERCENT}% battery, " +
                "above ${(SafetyLimits.MEDIA_VOLUME_RATIO_LIMIT * 100).toInt()}% volume, on a " +
                "severe thermal warning, or whenever headphones, Bluetooth or USB audio are connected."
        )
        PfTextCard(
            heading = "What this does not prove",
            body = "A selected digital frequency does not establish measured speaker output, " +
                "repellent effects or chakra-specific physiological effects. Phone " +
                "speakers vary significantly, especially at high frequencies."
        )
    }
}
