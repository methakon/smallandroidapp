package com.pifauna.app.safety

/** Machine-readable reasons a start request can be refused. */
enum class BlockCode {
    LOW_BATTERY,
    THERMAL,
    EXTERNAL_ROUTE,
    VOLUME_TOO_HIGH,
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
    val mediaVolumeRatio: Float = 0f
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
        if (snapshot.batteryPercent in 0..SafetyLimits.BATTERY_CUTOFF_PERCENT || snapshot.batteryPercent < 0) {
            val message = if (snapshot.batteryPercent < 0) {
                "Playback blocked: battery status unknown or unreadable. Charge the phone first."
            } else {
                "Playback blocked: battery at ${snapshot.batteryPercent}% " +
                    "(limit ${SafetyLimits.BATTERY_CUTOFF_PERCENT}%). Charge the phone first."
            }
            return SafetyDecision.Blocked(
                BlockCode.LOW_BATTERY,
                message
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
            return SafetyDecision.Blocked(
                BlockCode.EXTERNAL_ROUTE,
                "Playback blocked: unsafe active output ($route). Layaswarna requires " +
                    "a verified built-in phone speaker route."
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
            BlockCode.INVALID_REQUEST -> null
        }
    }
}
