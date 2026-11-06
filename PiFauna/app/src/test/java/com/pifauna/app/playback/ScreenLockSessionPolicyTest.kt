package com.pifauna.app.playback

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenLockSessionPolicyTest {
    @Test fun offByDefaultStopsOnScreenLock() {
        assertFalse(ScreenLockSessionPolicy.continueAfterActivityStopped(false, true))
    }

    @Test fun ordinaryHomeNavigationStopsEvenIfOptedIn() {
        assertFalse(ScreenLockSessionPolicy.continueAfterActivityStopped(true, false))
    }

    @Test fun optedInLockedSessionMayContinue() {
        assertTrue(ScreenLockSessionPolicy.continueAfterActivityStopped(true, true))
    }

    @Test fun foregroundOnlyStillStopsWhenHomePressed() {
        assertFalse(ScreenLockSessionPolicy.continueAfterActivityStopped(false, false))
    }
}
