package com.pifauna.app.playback

/** Pure lifecycle decision: screen-off is allowed only for opted-in sessions. */
internal object ScreenLockSessionPolicy {
    fun continueAfterActivityStopped(
        sessionOptedIn: Boolean,
        screenLockedOrOff: Boolean
    ): Boolean = sessionOptedIn && screenLockedOrOff
}
