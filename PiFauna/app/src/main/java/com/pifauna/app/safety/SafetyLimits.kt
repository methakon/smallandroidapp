package com.pifauna.app.safety

/**
 * Hard limits shared by the synthesis kernel, the audio engine and the UI.
 *
 * These are DIGITAL signal limits. They are not calibrated acoustic limits and
 * they say nothing about what a phone speaker actually radiates.
 */
object SafetyLimits {

    /** Lowest synthesised frequency, Hz. */
    const val MIN_HZ = 100

    /**
     * Highest synthesised frequency, Hz.
     *
     * 18 kHz is a DIGITAL setting only. Many phone speakers roll off sharply
     * above ~16 kHz and none of them produce ultrasound. This limit exists so
     * the app can never suggest an ultrasonic capability it cannot deliver.
     */
    const val MAX_HZ = 18_000

    /** 48 kHz keeps every allowed frequency below 0.45 * sampleRate. */
    const val SAMPLE_RATE = 48_000

    /**
     * Maximum digital peak as a fraction of full scale (0.08 = 8%, ~ -22 dBFS).
     * Leaves headroom so down-mixing and system effects cannot clip the stream.
     */
    const val MAX_GAIN = 0.08f

    /**
     * Shortest session the UI offers, in milliseconds. This is a product rule
     * enforced by [com.pifauna.app.playback.PlayerController]; the synthesiser
     * itself accepts any positive duration so it stays reusable and testable.
     */
    const val MIN_DURATION_MS = 60_000L

    /** Absolute ceiling for one session; there is no "continuous" mode. */
    const val MAX_DURATION_MS = 30 * 60_000L

    /** Session lengths offered in the UI, in minutes. */
    val DURATION_CHOICES_MIN = listOf(5, 10, 15, 30)

    /** Playback is refused at or below this battery percentage. */
    const val BATTERY_CUTOFF_PERCENT = 15

    /**
     * Playback is refused above this fraction of maximum media volume.
     * A digital amplitude cap cannot protect hearing once the system mixer is loud.
     */
    const val MEDIA_VOLUME_RATIO_LIMIT = 0.60f

    /** PowerManager.THERMAL_STATUS_SEVERE */
    const val THERMAL_BLOCK_STATUS = 3

    /** Number of custom presets that may be stored. */
    const val MAX_PRESETS = 20
}
