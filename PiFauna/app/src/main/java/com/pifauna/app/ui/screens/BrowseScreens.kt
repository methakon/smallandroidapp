package com.pifauna.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pifauna.app.audio.ToneMode
import com.pifauna.app.data.Preset
import com.pifauna.app.data.SpeciesProfile
import com.pifauna.app.data.SpeciesProfiles
import com.pifauna.app.safety.SafetyLimits
import com.pifauna.app.ui.PfCard
import com.pifauna.app.ui.PfScreen
import com.pifauna.app.ui.PfTextCard
import com.pifauna.app.ui.PiFaunaColors
import com.pifauna.app.ui.SectionLabel
import com.pifauna.app.util.TimeFormat
import java.time.LocalTime

/** Figma 01 · Welcome */
@Composable
fun WelcomeScreen(onGetStarted: () -> Unit, onLearnLimits: () -> Unit) {
    PfScreen(title = "Gentle sound control", subtitle = "Discover sound profiles") {
        SectionLabel("EXPLORE")
        PfCard(
            title = "Get started",
            subtitle = "Pick a band and run a timed session",
            onClick = onGetStarted
        )
        PfCard(
            title = "Learn about sound limits",
            subtitle = "What a phone speaker can and cannot do",
            onClick = onLearnLimits
        )
        PfTextCard(
            heading = "Before you begin",
            body = "Layaswarna generates short, low-level test tones through the built-in " +
                "speaker. It is not a repellent, it is not medically certified, and no " +
                "frequency here is proven to affect insects or animals. Keep the volume " +
                "low, stay away from ears, and stop if anyone feels discomfort."
        )
    }
}

/** Figma 02 · Home */
@Composable
fun HomeScreen(
    hourOfDay: Int = LocalTime.now().hour,
    onProfileSelected: (SpeciesProfile) -> Unit,
    onOpenLibrary: () -> Unit
) {
    val greeting = when (hourOfDay) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Good night"
    }
    PfScreen(title = greeting, subtitle = "Choose a sound profile") {
        SectionLabel("EXPLORE")
        SpeciesProfiles.all.take(4).forEach { profile ->
            PfCard(
                title = profile.name,
                subtitle = TimeFormat.band(profile.firstHz, profile.lastHz) + " · " + profile.summary,
                chakraPetals = listOf(4, 6, 10, 12)[SpeciesProfiles.all.indexOf(profile) % 4],
                onClick = { onProfileSelected(profile) }
            )
        }
        PfCard(
            title = "Full profile library",
            chakraPetals = 24,
            subtitle = "All bands, cautions and your saved presets",
            onClick = onOpenLibrary
        )
    }
}

/** Figma 04 · Profile Library */
@Composable
fun ProfilesScreen(
    presets: List<Preset>,
    onProfileSelected: (SpeciesProfile) -> Unit,
    onPresetSelected: (Preset) -> Unit,
    onDeletePreset: (String) -> Unit,
    onCreateCustom: () -> Unit
) {
    PfScreen(title = "Explore profiles", subtitle = "Sound presets") {
        SectionLabel("EXPLORE")
        SpeciesProfiles.all.forEach { profile ->
            PfCard(
                title = profile.name,
                subtitle = TimeFormat.band(profile.firstHz, profile.lastHz) + " · " + profile.summary,
                onClick = { onProfileSelected(profile) }
            )
        }

        SectionLabel("PROFILE NOTES")
        SpeciesProfiles.all.forEach { profile ->
            PfTextCard(heading = profile.name, body = profile.detail)
        }

        PfCard(
            title = "Create a custom sound",
            subtitle = "Set any band inside ${SafetyLimits.MIN_HZ}–${SafetyLimits.MAX_HZ} Hz",
            onClick = onCreateCustom
        )

        SectionLabel("YOUR PRESETS (${presets.size}/${SafetyLimits.MAX_PRESETS})")
        if (presets.isEmpty()) {
            PfTextCard(
                heading = "No saved presets",
                body = "Custom bands you save appear here. Presets stay on this device."
            )
        } else {
            presets.forEach { preset ->
                PfCard(
                    title = preset.name,
                    subtitle = TimeFormat.band(preset.startHz, preset.endHz) +
                        " · " + preset.mode.label() +
                        " · " + (preset.gain * 100).toInt() + "% level",
                    onClick = { onPresetSelected(preset) },
                    trailing = {
                        Text(
                            text = "Delete",
                            color = PiFaunaColors.Danger,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clickable { onDeletePreset(preset.name) }
                                .padding(8.dp)
                        )
                    }
                )
            }
        }
    }
}

/** Figma 05 · Custom Sound */
@Composable
fun CustomSoundScreen(
    startHz: Int,
    endHz: Int,
    mode: ToneMode,
    gain: Float,
    onRangeChange: (Int, Int) -> Unit,
    onModeChange: (ToneMode) -> Unit,
    onGainChange: (Float) -> Unit,
    onSave: (String) -> Result<Unit>
) {
    var name by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    PfScreen(title = "Create a sound", subtitle = "Adjust your playback") {
        SectionLabel("FREQUENCY")
        FrequencySliderCard(
            label = "Start frequency",
            value = startHz,
            onChange = { onRangeChange(it, maxOf(it, endHz)) }
        )
        FrequencySliderCard(
            label = "End frequency",
            value = endHz,
            onChange = { onRangeChange(minOf(startHz, it), it) }
        )
        PfTextCard(
            heading = "Digital, not measured",
            body = "These are synthesis settings. Many phone speakers fall away above " +
                "16 kHz, so a high setting can be nearly silent in the room."
        )

        SectionLabel("SIGNAL")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ToneMode.entries.forEach { candidate ->
                FilterChip(
                    selected = candidate == mode,
                    onClick = { onModeChange(candidate) },
                    label = { Text(candidate.label(), fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PiFaunaColors.Action,
                        selectedLabelColor = PiFaunaColors.OnAction,
                        labelColor = PiFaunaColors.BodyText,
                        containerColor = PiFaunaColors.Card
                    )
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        PfTextCard(heading = mode.label(), body = mode.explanation())

        SectionLabel("OUTPUT LEVEL")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(0.03f, 0.05f, SafetyLimits.MAX_GAIN).forEach { option ->
                FilterChip(
                    selected = kotlin.math.abs(gain - option) < 0.001f,
                    onClick = { onGainChange(option) },
                    label = { Text("${(option * 100).toInt()}%", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PiFaunaColors.Accent,
                        selectedLabelColor = PiFaunaColors.Background,
                        labelColor = PiFaunaColors.BodyText,
                        containerColor = PiFaunaColors.Card
                    )
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        PfTextCard(
            heading = "8% is the ceiling",
            body = "The engine caps peak amplitude at " +
                "${(SafetyLimits.MAX_GAIN * 100).toInt()}% of digital full scale and fades " +
                "every session in and out. System volume still governs loudness."
        )

        SectionLabel("SAVE")
        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(24) },
            label = { Text("Preset name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        PfPrimaryCard(title = "Save preset") {
            onSave(name)
                .onSuccess {
                    message = "Saved \"${name.trim()}\"."
                    name = ""
                }
                .onFailure { message = it.message ?: "Could not save the preset." }
        }
        message?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = PiFaunaColors.Muted, fontSize = 13.sp)
        }
    }
}

/** Figma 06 · Timer */
@Composable
fun TimerScreen(
    selectedMinutes: Int,
    onSelect: (Int) -> Unit
) {
    PfScreen(title = "Playback schedule", subtitle = "Choose a duration") {
        SectionLabel("EXPLORE")
        SafetyLimits.DURATION_CHOICES_MIN.forEach { minutes ->
            PfCard(
                title = "$minutes min",
                chakraPetals = 6,
                subtitle = if (minutes == selectedMinutes) "Selected" else "Tap to select",
                onClick = { onSelect(minutes) }
            )
        }
        PfCard(
            title = "Continuous",
            subtitle = "Not offered — every session must end on its own",
            enabled = false
        )
        PfTextCard(
            heading = "Why there is no continuous mode",
            body = "Playback always ends: on the timer, when the app leaves the " +
                "foreground, on audio-focus loss, at 15% battery or on a severe thermal " +
                "warning. Unattended sound is exactly the case this app refuses to support."
        )
    }
}

@Composable
private fun FrequencySliderCard(label: String, value: Int, onChange: (Int) -> Unit) {
    PfCard(title = "$label • ${TimeFormat.frequency(value)}") {
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt()) },
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
}

/** Accent-filled primary action card, used for start/save/stop. */
@Composable
fun PfPrimaryCard(
    title: String,
    enabled: Boolean = true,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    val container = when {
        !enabled -> PiFaunaColors.Divider
        danger -> PiFaunaColors.Danger
        else -> PiFaunaColors.Action
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 76.dp)
            .background(container, RoundedCornerShape(20.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) PiFaunaColors.OnAction else PiFaunaColors.Muted
            )
        }
    }
    Spacer(Modifier.height(16.dp))
}

internal fun ToneMode.label(): String = when (this) {
    ToneMode.FIXED -> "Sine"
    ToneMode.SWEEP -> "Sweep"
    ToneMode.PULSE -> "Pulse"
    ToneMode.HOP -> "Hop"
}

internal fun ToneMode.explanation(): String = when (this) {
    ToneMode.FIXED -> "One steady tone at the start frequency. The simplest way to hear " +
        "what this band sounds like on your phone."
    ToneMode.SWEEP -> "A triangle sweep between the two frequencies, repeating every 2.5 " +
        "seconds. Useful for finding where a speaker starts to roll off."
    ToneMode.PULSE -> "250 ms of tone, then 750 ms of silence. Easier on the ear than a " +
        "continuous tone at the same level."
    ToneMode.HOP -> "The frequency jumps every 500 ms within the band, with a short fade at " +
        "each step. Deterministic, so two runs sound the same."
}


