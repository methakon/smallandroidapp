package com.pifauna.app.audio

import android.media.AudioDeviceInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioRoutePreflightClassifierTest {

    @Test
    fun builtInSpeakerAloneIsAllowed() {
        val types = listOf(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
        assertFalse(AudioRoutePreflightClassifier.isExternalRouteActive(types, isA2dpActive = false))
        assertFalse(AudioRoutePreflightClassifier.isExternalRouteActive(types, isA2dpActive = true))
    }

    @Test
    fun connectedButUnroutedBluetoothDoesNotBlockPreflight() {
        // Bluetooth SCO (calls/watch) without active media streaming
        val scoOnly = listOf(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER, AudioDeviceInfo.TYPE_BLUETOOTH_SCO)
        assertFalse(AudioRoutePreflightClassifier.isExternalRouteActive(scoOnly, isA2dpActive = false))

        // Bluetooth A2DP device is paired, but A2DP media audio is inactive/off
        val a2dpOff = listOf(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER, AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)
        assertFalse(AudioRoutePreflightClassifier.isExternalRouteActive(a2dpOff, isA2dpActive = false))
    }

    @Test
    fun activeBluetoothMediaRouteBlocksPreflight() {
        val a2dpOn = listOf(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER, AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)
        assertTrue(AudioRoutePreflightClassifier.isExternalRouteActive(a2dpOn, isA2dpActive = true))

        val bleHeadsetOn = listOf(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER, AudioDeviceInfo.TYPE_BLE_HEADSET)
        assertTrue(AudioRoutePreflightClassifier.isExternalRouteActive(bleHeadsetOn, isA2dpActive = true))
    }

    @Test
    fun physicalExternalOutputsAlwaysBlockPreflight() {
        // Wired headphones and headsets
        assertTrue(
            AudioRoutePreflightClassifier.isExternalRouteActive(
                listOf(AudioDeviceInfo.TYPE_WIRED_HEADPHONES),
                isA2dpActive = false
            )
        )
        assertTrue(
            AudioRoutePreflightClassifier.isExternalRouteActive(
                listOf(AudioDeviceInfo.TYPE_WIRED_HEADSET),
                isA2dpActive = false
            )
        )

        // USB audio
        assertTrue(
            AudioRoutePreflightClassifier.isExternalRouteActive(
                listOf(AudioDeviceInfo.TYPE_USB_HEADSET),
                isA2dpActive = false
            )
        )
        assertTrue(
            AudioRoutePreflightClassifier.isExternalRouteActive(
                listOf(AudioDeviceInfo.TYPE_USB_DEVICE),
                isA2dpActive = false
            )
        )

        // Hearing aids
        assertTrue(
            AudioRoutePreflightClassifier.isExternalRouteActive(
                listOf(AudioDeviceInfo.TYPE_HEARING_AID),
                isA2dpActive = false
            )
        )

        // Line out and HDMI
        assertTrue(
            AudioRoutePreflightClassifier.isExternalRouteActive(
                listOf(AudioDeviceInfo.TYPE_LINE_ANALOG),
                isA2dpActive = false
            )
        )
        assertTrue(
            AudioRoutePreflightClassifier.isExternalRouteActive(
                listOf(AudioDeviceInfo.TYPE_HDMI),
                isA2dpActive = false
            )
        )
    }

    @Test
    fun routeTransitionsReflectedAccurately() {
        // State 1: Built-in speaker only -> allowed
        var deviceList = listOf(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
        assertFalse(AudioRoutePreflightClassifier.isExternalRouteActive(deviceList, isA2dpActive = false))

        // State 2: User plugs in wired headphones -> blocked
        deviceList = listOf(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER, AudioDeviceInfo.TYPE_WIRED_HEADPHONES)
        assertTrue(AudioRoutePreflightClassifier.isExternalRouteActive(deviceList, isA2dpActive = false))

        // State 3: User unplugs headphones -> allowed again
        deviceList = listOf(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
        assertFalse(AudioRoutePreflightClassifier.isExternalRouteActive(deviceList, isA2dpActive = false))

        // State 4: Bluetooth connects but media audio is disabled -> allowed
        deviceList = listOf(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER, AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)
        assertFalse(AudioRoutePreflightClassifier.isExternalRouteActive(deviceList, isA2dpActive = false))

        // State 5: Media audio is enabled for Bluetooth -> blocked
        assertTrue(AudioRoutePreflightClassifier.isExternalRouteActive(deviceList, isA2dpActive = true))
    }
}
