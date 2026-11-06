package com.pifauna.app.safety

import com.pifauna.app.audio.AudioOutputPolicy
import com.pifauna.app.audio.OutputMode
import com.pifauna.app.audio.RoutedOutput

/** Machine-readable reasons a start request can be refused. */
enum class BlockCode {
    LOW_BATTERY,
    THERMAL,
    EXTERNAL_ROUTE,
    VOLUME_TOO_HIGH,
    EXTERNAL_CONSENT_REQUIRED,
    INVALID_REQUEST
}

data class SafetySnapshot(
    /** Battery charge, 0..100. Values outside that range are treated as unknown and ignored. */
    val batteryPercent: Int = 100,
    /** PowerManager thermal status, 0..6. Unknown/unsupported is reported as 0 (NONE). */
    val thermalStatus: Int = 0,
    /** Non-null description of an external output route (headset, Bluetooth, USB). */
    val externalAudioRoute: String? = null,
    /** Current media volume as a fraction of the stream maximum, 0.0..1.0. */
    val mediaVolumeRatio: Float = 0f,
    /** Advisory available devices; never independently treated as selected route. */
    val availableExternalRoute: String? = null,
    /** Non-null after audible playback starts: the actual AudioTrack route. */
    val activeOutput: RoutedOutput? = null,
    val outputMode: OutputMode = OutputMode.PHONE_SPEAKER,
    val externalSpeakerConsented: Boolean = false,
    val selectedDurationMinutes: Int = 10,
    val selectedGain: Float = 0.04f
)

sealed interface SafetyDecision {
    data object Allowed : SafetyDecision
    data class Blocked(val code: BlockCode, val message: String) : SafetyDecision
}

/**
 * Pure safety gate. No Android types, so every rule is unit-testable.
 *
 * Ordering matters: device-protection rules are checked before convenience
 * rules so the user is told about the most serious problem first.
 */
object SafetyPolicy {

    fun evaluate(snapshot: SafetySnapshot): SafetyDecision {
        if (snapshot.batteryPercent in 0..SafetyLimits.BATTERY_CUTOFF_PERCENT) {
            return SafetyDecision.Blocked(
                BlockCode.LOW_BATTERY,
                "Playback blocked: battery at ${snapshot.batteryPercent}% " +
                    "(limit ${SafetyLimits.BATTERY_CUTOFF_PERCENT}%). Charge the phone first."
            )
        }

        if (snapshot.thermalStatus >= SafetyLimits.THERMAL_BLOCK_STATUS) {
            return SafetyDecision.Blocked(
                BlockCode.THERMAL,
                "Playback blocked: the phone is too hot. Let it cool down."
            )
        }

        val route = snapshot.externalAudioRoute
        if (!route.isNullOrBlank()) {
            // Legacy explicit unsafe route (used by old callers/tests).
            return SafetyDecision.Blocked(
                BlockCode.EXTERNAL_ROUTE,
                "Playback blocked: $route is unsafe. Choose the phone speaker."
            )
        }

        if (snapshot.outputMode == OutputMode.EXTERNAL_BLUETOOTH_SPEAKER) {
            if (!snapshot.externalSpeakerConsented) {
                return SafetyDecision.Blocked(
                    BlockCode.EXTERNAL_CONSENT_REQUIRED,
                    "External speaker playback needs fresh, explicit consent for this session."
                )
            }
            if (snapshot.selectedDurationMinutes > AudioOutputPolicy.SPEAKER_MAX_DURATION_MINUTES ||
                snapshot.selectedGain > AudioOutputPolicy.SPEAKER_MAX_GAIN + 0.0001f) {
                return SafetyDecision.Blocked(
                    BlockCode.INVALID_REQUEST,
                    "External speaker sessions require 5 minutes or less and at most 2% digital gain."
                )
            }
            if (snapshot.mediaVolumeRatio > AudioOutputPolicy.SPEAKER_MAX_VOLUME_RATIO) {
                return SafetyDecision.Blocked(
                    BlockCode.VOLUME_TOO_HIGH,
                    "External speaker mode requires media volume at or below 30%; " +
                        "this is not a measured safe acoustic level."
                )
            }
        }

        if (snapshot.activeOutput != null &&
            !AudioOutputPolicy.matches(snapshot.outputMode, snapshot.activeOutput)) {
            return SafetyDecision.Blocked(
                BlockCode.EXTERNAL_ROUTE,
                "Playback stopped: actual audio output is unsafe or unverified. " +
                    "Headphones, earbuds, hearing aids and unknown routes are not allowed."
            )
        }

        if (snapshot.mediaVolumeRatio > SafetyLimits.MEDIA_VOLUME_RATIO_LIMIT) {
            val percent = (SafetyLimits.MEDIA_VOLUME_RATIO_LIMIT * 100).toInt()
            return SafetyDecision.Blocked(
                BlockCode.VOLUME_TOO_HIGH,
                "Playback blocked: media volume is above $percent% of maximum. " +
                    "Lower the volume with the volume keys and try again."
            )
        }

        return SafetyDecision.Allowed
    }

    /** True when a session must be torn down because the device condition changed. */
    fun mustStop(snapshot: SafetySnapshot): BlockCode? {
        val code = (evaluate(snapshot) as? SafetyDecision.Blocked)?.code ?: return null
        return when (code) {
            BlockCode.LOW_BATTERY,
            BlockCode.THERMAL,
            BlockCode.EXTERNAL_ROUTE -> code
            // Volume can legitimately move during a session; we stop too, because a
            // sudden jump to full scale is exactly the hazard the cap cannot cover.
            BlockCode.VOLUME_TOO_HIGH -> code
            BlockCode.EXTERNAL_CONSENT_REQUIRED,
            BlockCode.INVALID_REQUEST -> null
        }
    }
}
