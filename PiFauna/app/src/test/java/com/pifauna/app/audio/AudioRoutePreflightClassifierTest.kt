package com.pifauna.app.audio

import android.media.AudioDeviceInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for conservative preflight. The live AudioTrack routing gate
 * is separately covered by ActiveOutputRoutePolicyTest and device acceptance.
 */
class AudioRoutePreflightClassifierTest {

    @Test fun builtInSpeakerAloneIsAllowed() {
        assertFalse(AudioRoutePreflightClassifier.isExternalRouteActive(
            listOf(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)))
    }

    @Test fun connectedA2dpAndSpeakerNeverCausePreflightRefusal() {
        // Critically, an A2DP connection says nothing about the current AudioTrack
        // media route. This catches the v12 regression caused by isBluetoothA2dpOn.
        assertFalse(AudioRoutePreflightClassifier.isExternalRouteActive(
            listOf(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)))
    }

    @Test fun bluetoothMediaOutputsRequireActualTrackVerification() {
        for (type in listOf(
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_BLE_HEADSET,
            AudioDeviceInfo.TYPE_BLE_SPEAKER,
            AudioDeviceInfo.TYPE_BLE_BROADCAST
        )) {
            assertFalse(AudioRoutePreflightClassifier.isExternalRouteActive(
                listOf(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER, type)))
        }
        // Not a safety bypass: SoundEngine verifies each actual routed device
        // before writing PCM, and refuses Bluetooth for every one of these types.
    }

    @Test fun bluetoothScoCallOnlyAndConnectedWatchDoNotBlock() {
        assertFalse(AudioRoutePreflightClassifier.isExternalRouteActive(
            listOf(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO)))
    }

    @Test fun physicalOutputsAreConservativelyBlockedWhenConnected() {
        for (type in listOf(
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            AudioDeviceInfo.TYPE_USB_DEVICE,
            AudioDeviceInfo.TYPE_USB_HEADSET,
            AudioDeviceInfo.TYPE_HEARING_AID,
            AudioDeviceInfo.TYPE_HDMI,
            AudioDeviceInfo.TYPE_LINE_ANALOG,
            AudioDeviceInfo.TYPE_LINE_DIGITAL,
            AudioDeviceInfo.TYPE_DOCK
        )) {
            assertTrue("physical output $type must block preflight",
                AudioRoutePreflightClassifier.isExternalRouteActive(listOf(type)))
        }
    }

    @Test fun bluetoothTransitionOnlyIsNotAFalsePreflightBlock() {
        val speaker = listOf(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
        val bluetoothConnected = speaker + AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
        assertFalse(AudioRoutePreflightClassifier.isExternalRouteActive(speaker))
        assertFalse(AudioRoutePreflightClassifier.isExternalRouteActive(bluetoothConnected))
        assertFalse(AudioRoutePreflightClassifier.isExternalRouteActive(speaker))
    }

    @Test fun physicalTransitionIsAnEarlyRefusal() {
        val speaker = listOf(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
        assertFalse(AudioRoutePreflightClassifier.isExternalRouteActive(speaker))
        assertTrue(AudioRoutePreflightClassifier.isExternalRouteActive(
            speaker + AudioDeviceInfo.TYPE_WIRED_HEADPHONES))
        assertFalse(AudioRoutePreflightClassifier.isExternalRouteActive(speaker))
    }
}
