package com.pifauna.app.safety

/**
 * Audio focus callbacks use negative values for all focus-loss types:
 * permanent (-1), transient (-2), and transient-can-duck (-3).
 * This app conservatively stops immediately rather than ducking or resuming.
 * Gains and neutral callbacks must not stop playback.
 */
object AudioFocusInterruptionPolicy {
    fun mustStop(focusChange: Int): Boolean = focusChange < 0
}
