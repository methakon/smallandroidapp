package com.pifauna.app.audio

/**
 * Android A2DP does not distinguish a loudspeaker from in-ear headphones.
 * An explicit one-session speaker declaration is NOT an acoustic measurement.
 */
enum class OutputMode { PHONE_SPEAKER, EXTERNAL_BLUETOOTH_SPEAKER }

enum class RoutedOutput {
    PHONE_SPEAKER,
    BLUETOOTH_A2DP_UNVERIFIED,
    BLUETOOTH_LE_SPEAKER,
    HEADPHONES,
    HEARING_AID,
    OTHER_EXTERNAL,
    UNKNOWN
}

object AudioOutputPolicy {
    /** No headphone override. A2DP requires the user's free-standing-speaker declaration. */
    fun matches(mode: OutputMode, route: RoutedOutput): Boolean = when (mode) {
        OutputMode.PHONE_SPEAKER -> route == RoutedOutput.PHONE_SPEAKER
        OutputMode.EXTERNAL_BLUETOOTH_SPEAKER ->
            route == RoutedOutput.BLUETOOTH_A2DP_UNVERIFIED ||
                route == RoutedOutput.BLUETOOTH_LE_SPEAKER
    }

    const val SPEAKER_MAX_DURATION_MINUTES = 5
    const val SPEAKER_MAX_GAIN = 0.02f
    const val SPEAKER_MAX_VOLUME_RATIO = 0.30f
}
