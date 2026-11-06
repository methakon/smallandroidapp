package com.pifauna.app.audio

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioOutputPolicyTest {
    @Test fun phoneSpeakerDoesNotTreatConnectivityAsRouting() {
        assertTrue(AudioOutputPolicy.matches(OutputMode.PHONE_SPEAKER, RoutedOutput.PHONE_SPEAKER))
        assertFalse(AudioOutputPolicy.matches(OutputMode.PHONE_SPEAKER, RoutedOutput.BLUETOOTH_A2DP_UNVERIFIED))
    }
    @Test fun userDeclaredExternalSpeakerAllowsA2dpOrBleSpeakerOnly() {
        assertTrue(AudioOutputPolicy.matches(OutputMode.EXTERNAL_BLUETOOTH_SPEAKER, RoutedOutput.BLUETOOTH_A2DP_UNVERIFIED))
        assertTrue(AudioOutputPolicy.matches(OutputMode.EXTERNAL_BLUETOOTH_SPEAKER, RoutedOutput.BLUETOOTH_LE_SPEAKER))
        listOf(RoutedOutput.HEADPHONES, RoutedOutput.HEARING_AID,
            RoutedOutput.OTHER_EXTERNAL, RoutedOutput.UNKNOWN).forEach {
            assertFalse(AudioOutputPolicy.matches(OutputMode.EXTERNAL_BLUETOOTH_SPEAKER, it))
        }
    }
}
