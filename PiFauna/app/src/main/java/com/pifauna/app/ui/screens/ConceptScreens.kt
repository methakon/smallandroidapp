package com.pifauna.app.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pifauna.app.audio.ToneMode
import com.pifauna.app.data.ChakraProfile
import com.pifauna.app.data.ChakraProfiles
import com.pifauna.app.ui.LayaswarnaRadialSelector
import com.pifauna.app.ui.PfScreen
import com.pifauna.app.ui.PfTextCard
import com.pifauna.app.ui.PiFaunaColors
import com.pifauna.app.ui.RadialOption
import com.pifauna.app.ui.RadialSymbol
import com.pifauna.app.ui.SectionLabel

private val chakraSymbols = listOf(
    RadialSymbol.ROOT, RadialSymbol.SACRAL, RadialSymbol.SOLAR,
    RadialSymbol.HEART, RadialSymbol.THROAT, RadialSymbol.THIRD_EYE, RadialSymbol.CROWN
)

private fun chakraOptions(): List<RadialOption> = ChakraProfiles.all.mapIndexed { index, entry ->
    RadialOption(
        id = entry.id,
        label = entry.name,
        detail = "${entry.toneHz} Hz",
        symbol = chakraSymbols[index],
        tint = PiFaunaColors.Chakra[index]
    )
}

@Composable
private fun ChakraInsights(profile: ChakraProfile) {
    SectionLabel("CHAKRA INSIGHTS")
    PfTextCard(heading = "Traditional association", body = profile.traditionalAssociation)
    PfTextCard(heading = "Listening practice", body = profile.listeningPractice)
    PfTextCard(
        heading = "Suggested short session",
        body = "Try 5–10 minutes at a comfortable volume, if you enjoy the sound. " +
            "Stop if you or anyone nearby experiences discomfort."
    )
    Text(
        text = ChakraProfiles.evidenceNote,
        color = PiFaunaColors.Muted,
        fontSize = 14.sp
    )
    Spacer(Modifier.height(14.dp))
}

/** The wheel selects a symbolic chakra; a hold/double-tap opens its details. */
@Composable
fun ChakraLibraryScreen(
    selectedId: String,
    onSelect: (String) -> Unit,
    onOpen: (String) -> Unit
) {
    val profile = ChakraProfiles.byId(selectedId)
    PfScreen(
        title = "Seven Chakra Library",
        subtitle = "Modern symbolic sound associations"
    ) {
        SectionLabel("CHAKRA REGULATOR")
        LayaswarnaRadialSelector(
            options = chakraOptions(),
            selectedId = selectedId,
            onSelect = onSelect,
            onActivate = onOpen,
            actionLabel = "Open ${profile.name} details"
        )
        ChakraInsights(profile)
    }
}

/** Prepares an audible tone, but never starts audio without the Player's safety checks. */
@Composable
fun ChakraConsoleScreen(
    selectedId: String,
    onSelect: (String) -> Unit,
    onUseTone: (ChakraProfile) -> Unit,
    enabled: Boolean
) {
    val profile = ChakraProfiles.byId(selectedId)
    PfScreen(
        title = "Chakra Sound Console",
        subtitle = "Choose an optional meditation tone"
    ) {
        SectionLabel("SYMBOLIC TONES")
        LayaswarnaRadialSelector(
            options = chakraOptions(),
            selectedId = selectedId,
            onSelect = onSelect,
            onActivate = { onUseTone(ChakraProfiles.byId(it)) },
            actionLabel = "Prepare ${profile.toneHz} Hz in Sound Player",
            enabled = enabled
        )
        ChakraInsights(profile)
        PfTextCard(
            heading = "Playback remains supervised",
            body = "Preparing the selected tone does not start sound. You must " +
                "start a timed session in the Sound Player, subject to its volume, " +
                "battery, audio-route and thermal checks."
        )
    }
}

/**
 * Only the two existing, audited tone engine modes are offered for playback.
 * Pink and white noise remain visibly unavailable until real synthesis and
 * battery/thermal validation, rather than playing a mislabeled waveform.
 */
data class RelaxationSound(
    val id: String,
    val label: String,
    val description: String,
    val symbol: RadialSymbol,
    val startHz: Int? = null,
    val endHz: Int? = null,
    val mode: ToneMode? = null
) {
    val playable: Boolean get() = startHz != null && endHz != null && mode != null
}

object RelaxationSounds {
    val all = listOf(
        RelaxationSound(
            "pink", "Pink noise",
            "Proposed relaxation sound. Actual filtered-noise synthesis has not been validated.",
            RadialSymbol.PINK_NOISE
        ),
        RelaxationSound(
            "white", "White noise",
            "Proposed relaxation sound. Noise playback is not available in this build.",
            RadialSymbol.WHITE_NOISE
        ),
        RelaxationSound(
            "soft", "Soft tone",
            "An experimental 440 Hz audible sine tone, not a sleep or medical treatment.",
            RadialSymbol.SOFT_TONE, 440, 440, ToneMode.FIXED
        ),
        RelaxationSound(
            "sweep", "Gentle sweep",
            "An audible 400–500 Hz triangle sweep, not a proven relaxation treatment.",
            RadialSymbol.GENTLE_SWEEP, 400, 500, ToneMode.SWEEP
        )
    )
    fun byId(id: String?): RelaxationSound = all.firstOrNull { it.id == id } ?: all.first()
}

@Composable
fun AnahataNadaScreen(
    onPrepare: (RelaxationSound) -> Unit,
    enabled: Boolean
) {
    var selected by remember { mutableStateOf("pink") }
    var notice by remember { mutableStateOf<String?>(null) }
    val current = RelaxationSounds.byId(selected)

    fun open(id: String) {
        val sound = RelaxationSounds.byId(id)
        if (sound.playable && enabled) {
            onPrepare(sound)
        } else {
            notice = if (!enabled) {
                "Stop the current playback session before preparing another sound."
            } else {
                "${sound.label} is a proposed feature, not an available audio mode."
            }
        }
    }

    PfScreen(title = "Anahata Nada", subtitle = "Experimental sound studio") {
        SectionLabel("SOUND REGULATOR")
        LayaswarnaRadialSelector(
            options = RelaxationSounds.all.map {
                RadialOption(it.id, it.label, if (it.playable) "Audible tone" else "Planned", it.symbol)
            },
            selectedId = selected,
            onSelect = { selected = it; notice = null },
            onActivate = ::open,
            actionLabel = if (current.playable) "Prepare ${current.label}" else "View ${current.label} status",
            enabled = true
        )
        SectionLabel("SELECTED SOUND")
        PfTextCard(heading = current.label, body = current.description)
        notice?.let {
            Text(it, color = PiFaunaColors.Warning, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
        }
        PfTextCard(
            heading = "Short sessions only",
            body = "Audible sounds are experiments, not verified sleep stimulation. " +
                "All playback uses the normal Sound Player and its 5/10/15/30-minute limit. " +
                "No overnight or background playback."
        )
    }
}
