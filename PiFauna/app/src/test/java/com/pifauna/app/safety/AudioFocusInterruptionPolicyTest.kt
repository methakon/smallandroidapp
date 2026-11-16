package com.pifauna.app.safety

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioFocusInterruptionPolicyTest {
    @Test fun allAndroidFocusLossCodesStopPlayback() {
        // Android AudioManager: LOSS=-1, LOSS_TRANSIENT=-2, LOSS_TRANSIENT_CAN_DUCK=-3.
        listOf(-1, -2, -3).forEach { assertTrue(AudioFocusInterruptionPolicy.mustStop(it)) }
    }

    @Test fun gainAndNeutralCallbacksDoNotStopPlayback() {
        listOf(0, 1, 2, 3).forEach { assertFalse(AudioFocusInterruptionPolicy.mustStop(it)) }
    }
}
