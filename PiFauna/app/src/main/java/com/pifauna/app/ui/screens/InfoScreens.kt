package com.pifauna.app.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pifauna.app.safety.SafetyLimits
import com.pifauna.app.ui.PfCard
import com.pifauna.app.ui.PfScreen
import com.pifauna.app.ui.PfTextCard
import com.pifauna.app.ui.PiFaunaColors
import com.pifauna.app.ui.SectionLabel

/** Figma 07 · Safety */
@Composable
fun SafetyScreen() {
    PfScreen(title = "Sound & hearing safety", subtitle = "Important information") {
        SectionLabel("PLEASE READ")
        PfTextCard(
            heading = "Keep the volume moderate",
            body = "Start at the lowest volume you can hear and raise it slowly. The 8% " +
                "digital cap inside the app cannot protect your hearing if the system " +
                "volume is at maximum or an equaliser boosts the output."
        )
        PfTextCard(
            heading = "Children and pets may hear sounds you cannot",
            body = "Hearing range varies. A tone that is inaudible to an adult can be " +
                "clearly audible, or unpleasant, to a child, a dog or a cat. Never point " +
                "the speaker at an ear, a child, a pet or a neighbour."
        )
        PfTextCard(
            heading = "Effectiveness is not established",
            body = "Electronic and ultrasonic pest repellents have a weak evidence base. " +
                "Controlled reviews have not shown that they prevent bites. Layaswarna makes " +
                "no repellency claim of any kind."
        )
        PfTextCard(
            heading = "Stop if discomfort occurs",
            body = "Stop immediately if you, anyone nearby or an animal shows signs of " +
                "discomfort. Prefer physical exclusion, sanitation and approved repellents " +
                "for actual pest problems."
        )
    }
}

/** Figma 09 · Device Protection */
@Composable
fun DeviceProtectionScreen() {
    PfScreen(title = "Device protection", subtitle = "Safe playback defaults") {
        PfTextCard(
            heading = "Output limiter",
            body = "Peak amplitude is capped at ${(SafetyLimits.MAX_GAIN * 100).toInt()}% of " +
                "digital full scale (${(SafetyLimits.MAX_GAIN * 32767).toInt()} of 32767), " +
                "with a 25 ms fade in and out. Rounding is clamped, so no sample can wrap " +
                "and no block can clip."
        )
        PfTextCard(
            heading = "Battery threshold",
            body = "Playback cannot start at or below " +
                "${SafetyLimits.BATTERY_CUTOFF_PERCENT}% and stops if the charge falls to " +
                "that level during a session."
        )
        PfTextCard(
            heading = "Thermal guard",
            body = "A PowerManager thermal status of SEVERE or worse blocks new sessions and " +
                "stops a running one. Supported on Android 10 and later."
        )
        PfTextCard(
            heading = "Audio interruptions",
            body = "Any audio-focus loss — an incoming call, a voice assistant, a competing " +
                "media app — stops playback instead of ducking it."
        )
        PfTextCard(
            heading = "Headphone safety",
            body = "Layaswarna plays through the built-in speaker only. Wired headsets, " +
                "Bluetooth, USB audio, hearing aids and line outputs are refused before " +
                "playback and interrupt a session the moment they appear."
        )
    }
}

/** Figma 10 · Battery Saver */
@Composable
fun BatterySaverScreen() {
    PfScreen(title = "Battery saver", subtitle = "Efficient audio operation") {
        PfTextCard(
            heading = "Display sleep",
            body = "The app holds no wake lock and never forces the screen on. When the " +
                "screen turns off the activity stops, and playback stops with it — that is " +
                "intended, not a bug."
        )
        PfTextCard(
            heading = "Playback duration",
            body = "Default session is 10 minutes and the hard ceiling is " +
                "${SafetyLimits.MAX_DURATION_MS / 60_000} minutes. The audio thread stops " +
                "itself on a monotonic clock, so a stalled UI cannot extend a session."
        )
        PfTextCard(
            heading = "Background audio",
            body = "There is no service and no foreground-audio mode. Nothing plays once the " +
                "app leaves the foreground."
        )
        PfTextCard(
            heading = "CPU and wake locks",
            body = "One audio thread at THREAD_PRIORITY_AUDIO, one 1024-frame PCM buffer " +
                "reused for the whole session, and no per-sample allocation. Stopping " +
                "joins the thread and releases the AudioTrack before returning."
        )
        PfTextCard(
            heading = "Battery monitoring",
            body = "The battery level arrives as a system broadcast and the thermal status " +
                "as a listener. There is no polling loop anywhere in the app."
        )
    }
}

/** Figma 11 · Playback Safety */
@Composable
fun PlaybackSafetyScreen() {
    PfScreen(title = "Ready to play?", subtitle = "Check your device first") {
        PfTextCard(
            heading = "Device capability",
            body = "High frequencies may be inaudible even at full volume, and phone " +
                "speakers cannot produce ultrasound at all. Treat every setting as a " +
                "digital value, not a physical promise."
        )
        PfTextCard(
            heading = "Sound level",
            body = "Start low. Playback is refused above " +
                "${(SafetyLimits.MEDIA_VOLUME_RATIO_LIMIT * 100).toInt()}% of maximum media " +
                "volume, so lower the volume with the hardware keys first."
        )
        PfTextCard(
            heading = "People and pets",
            body = "Stop if discomfort is observed. Do not use sound to punish, frighten, " +
                "herd or exclude an animal."
        )
        PfTextCard(
            heading = "No proven repellent claim",
            body = "Effectiveness varies and is unverified. Layaswarna is an acoustic " +
                "experiment, not a pest-control device."
        )
        PfTextCard(
            heading = "Start with a timer",
            body = "The default 10-minute session is the recommended first run. Never leave " +
                "playback unattended."
        )
    }
}

/** Figma 08 · Settings */
@Composable
fun SettingsScreen(
    onOpenSafety: () -> Unit,
    onOpenDeviceProtection: () -> Unit,
    onOpenBatterySaver: () -> Unit,
    onOpenPlaybackSafety: () -> Unit,
    onClearData: () -> Unit,
    clearMessage: String?
) {
    PfScreen(title = "Preferences", subtitle = "Personalize Layaswarna") {
        SectionLabel("EXPLORE")
        PfCard(
            title = "Dark appearance",
            subtitle = "Always on — the Figma design defines a single dark canvas",
            enabled = false
        )
        PfCard(
            title = "Audio output",
            subtitle = "Built-in speaker only. External routes are refused by policy.",
            enabled = false
        )
        PfCard(
            title = "Background playback",
            subtitle = "Off — there is no service, so sound cannot outlive the screen",
            enabled = false
        )
        PfCard(
            title = "About & privacy",
            subtitle = "No network, no account, no analytics, no permissions",
            enabled = false
        )

        SectionLabel("SAFETY")
        PfCard(title = "Sound & hearing safety", onClick = onOpenSafety)
        PfCard(title = "Device protection", onClick = onOpenDeviceProtection)
        PfCard(title = "Battery saver", onClick = onOpenBatterySaver)
        PfCard(title = "Playback safety", onClick = onOpenPlaybackSafety)

        SectionLabel("LOCAL DATA")
        PfCard(
            title = "Clear saved settings and presets",
            subtitle = "Deletes preferences stored on this device",
            onClick = onClearData
        )
        clearMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, fontSize = 13.sp, color = PiFaunaColors.Muted)
        }

        Spacer(Modifier.height(20.dp))
        PfTextCard(
            heading = "About Layaswarna",
            body = "Version 0.5.0. An offline acoustic experiment for Android 8.0 and " +
                "later. Not a medical device and not a pest-control product."
        )
    }
}
