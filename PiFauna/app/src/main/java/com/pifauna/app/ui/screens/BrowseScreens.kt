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
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.pifauna.app.ui.LayaswarnaRadialSelector
import com.pifauna.app.ui.MysticInsightCard
import com.pifauna.app.ui.MysticActionButton
import com.pifauna.app.ui.RadialOption
import com.pifauna.app.ui.RadialSymbol
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

private fun profileOptions(): List<RadialOption> = SpeciesProfiles.all.map { profile ->
    RadialOption(
        id = profile.id,
        label = profile.name,
        detail = TimeFormat.band(profile.firstHz, profile.lastHz),
        symbol = when (profile.id) {
            "mosquito" -> RadialSymbol.MOSQUITO
            "insects" -> RadialSymbol.INSECT
            "rodent" -> RadialSymbol.RODENT
            "dogcat" -> RadialSymbol.PETS
            "bird" -> RadialSymbol.BIRD
            else -> RadialSymbol.CUSTOM
        }
    )
}

/** Figma 02 · One regulator, selected item highlight and optional alternate action. */
@Composable
fun HomeScreen(
    hourOfDay: Int = LocalTime.now().hour,
    onProfileSelected: (SpeciesProfile) -> Unit,
    selectedProfileId: String,
    onSelectProfile: (String) -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenChakras: () -> Unit,
    onOpenAnahata: () -> Unit
) {
    val greeting = when (hourOfDay) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Good night"
    }
    val profile = SpeciesProfiles.byId(selectedProfileId)
    PfScreen(
        title = "Sound profile regulator",
        subtitle = "$greeting · Tap or rotate to select a sound"
    ) {
        LayaswarnaRadialSelector(
            options = profileOptions(),
            selectedId = selectedProfileId,
            onSelect = onSelectProfile,
            onActivate = { onProfileSelected(SpeciesProfiles.byId(it)) },
            actionLabel = "Open ${profile.name}",
            showActionButton = false
        )
        Spacer(Modifier.height(16.dp))
        MysticInsightCard(
            title = "Selected sound profile",
            sections = listOf(
                profile.name to profile.summary,
                "Experimental frequency" to TimeFormat.band(profile.firstHz, profile.lastHz),
                "About this profile" to profile.detail
            )
        )
        MysticActionButton("Open ${profile.name}") { onProfileSelected(profile) }
        PfCard(
            title = "All profiles and saved sounds",
            subtitle = "Browse preset library",
            onClick = onOpenLibrary
        )
        SectionLabel("MEDITATION")
        PfCard(
            title = "Seven Chakra Library",
            subtitle = "Symbolic tone associations and listening insights",
            onClick = onOpenChakras
        )
        PfCard(
            title = "Anahata Nada",
            subtitle = "Experimental relaxation sound studio",
            onClick = onOpenAnahata
        )
        Text(
            "Experimental audio: no proven repellency or medical effects.",
            color = PiFaunaColors.Muted,
            fontSize = 14.sp
        )
    }
}

/** Figma 04 · A single circular selector plus current profile notes and saved presets. */
@Composable
fun ProfilesScreen(
    presets: List<Preset>,
    selectedProfileId: String,
    onSelectProfile: (String) -> Unit,
    onProfileSelected: (SpeciesProfile) -> Unit,
    onPresetSelected: (Preset) -> Unit,
    onDeletePreset: (String) -> Unit,
    onCreateCustom: () -> Unit
) {
    val profile = SpeciesProfiles.byId(selectedProfileId)
    PfScreen(
        title = "Profile library regulator",
        subtitle = "Tap or rotate to explore experimental sound presets"
    ) {
        LayaswarnaRadialSelector(
            options = profileOptions(),
            selectedId = selectedProfileId,
            onSelect = onSelectProfile,
            onActivate = { onProfileSelected(SpeciesProfiles.byId(it)) },
            actionLabel = "Open ${profile.name}",
            showActionButton = false
        )
        Spacer(Modifier.height(16.dp))
        MysticInsightCard(
            title = "Selected sound profile",
            sections = listOf(
                profile.name to profile.summary,
                "Frequency band" to TimeFormat.band(profile.firstHz, profile.lastHz),
                "Listening information" to profile.detail
            )
        )
        MysticActionButton("Open ${profile.name}") { onProfileSelected(profile) }
        PfCard(
            title = "Create a custom sound",
            subtitle = "Set a frequency band and waveform",
            onClick = onCreateCustom
        )

        SectionLabel("YOUR PRESETS (${presets.size}/${SafetyLimits.MAX_PRESETS})")
        if (presets.isEmpty()) {
            PfTextCard("No saved presets", "Custom bands you save appear here. Presets stay on this device.")
        } else {
            presets.forEach { preset ->
                PfCard(
                    title = preset.name,
                    subtitle = TimeFormat.band(preset.startHz, preset.endHz) +
                        " · " + preset.mode.label() + " · " + (preset.gain * 100).toInt() + "% level",
                    onClick = { onPresetSelected(preset) }
                )
                PfCard(
                    title = "Delete ${preset.name}",
                    subtitle = "Remove saved preset from this device",
                    onClick = { onDeletePreset(preset.name) }
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
        LayaswarnaRadialSelector(
            options = ToneMode.entries.map { candidate ->
                RadialOption(
                    id = candidate.name,
                    label = candidate.label(),
                    detail = "Audio waveform",
                    symbol = when (candidate) {
                        ToneMode.FIXED -> RadialSymbol.SINE
                        ToneMode.SWEEP -> RadialSymbol.SWEEP
                        ToneMode.PULSE -> RadialSymbol.PULSE
                        ToneMode.HOP -> RadialSymbol.HOP
                    }
                )
            },
            selectedId = mode.name,
            onSelect = { selected -> onModeChange(ToneMode.valueOf(selected)) },
            onActivate = { selected -> onModeChange(ToneMode.valueOf(selected)) },
            actionLabel = "Use selected waveform",
            showActionButton = false
        )
        Spacer(Modifier.height(16.dp))
        MysticInsightCard(
            title = "Selected waveform",
            sections = listOf(
                mode.label() to mode.explanation(),
                "Frequency" to TimeFormat.band(startHz, endHz),
                "Output" to "Bounded digital gain; system volume limits still apply."
            )
        )
        MysticActionButton("Use ${mode.label()} waveform") { onModeChange(mode) }

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

/** Figma 06 · Selection stays tentative until confirmed. */
@Composable
fun TimerScreen(selectedMinutes: Int, onSelect: (Int) -> Unit) {
    var tentative by rememberSaveable(selectedMinutes) { mutableStateOf(selectedMinutes.toString()) }
    PfScreen(title = "Session timer regulator", subtitle = "Choose a bounded listening duration") {
        LayaswarnaRadialSelector(
            options = SafetyLimits.DURATION_CHOICES_MIN.map {
                RadialOption(it.toString(), "$it min", "Timed session", RadialSymbol.DURATION)
            },
            selectedId = tentative,
            onSelect = { tentative = it },
            onActivate = { onSelect(it.toInt()) },
            actionLabel = "Confirm ${tentative} minute timer",
            showActionButton = false
        )
        Spacer(Modifier.height(16.dp))
        MysticInsightCard(
            title = "Selected duration",
            sections = listOf(
                "${tentative} minute session" to "Audio stops automatically when this session ends.",
                "Foreground only" to "Playback stops when you leave the app or the activity stops.",
                "Safety checks" to "The battery, volume, route and thermal limits remain active."
            )
        )
        MysticActionButton("Confirm ${tentative} minute timer") {
            onSelect(tentative.toInt())
        }
        Text(
            "Continuous and overnight listening are not available.",
            color = PiFaunaColors.Muted,
            fontSize = 14.sp
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
        danger -> PiFaunaColors.Action
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



