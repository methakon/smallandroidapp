package com.pifauna.app.audio

import android.media.AudioDeviceInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveOutputRoutePolicyTest {
    @Test fun builtInSpeakerOnlyIsTrusted() {
        assertTrue(ActiveOutputRoutePolicy.isTrusted(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER))
        assertFalse(ActiveOutputRoutePolicy.isTrusted(AudioDeviceInfo.TYPE_BUILTIN_EARPIECE))
    }

    @Test fun connectedBluetoothDoesNotEstablishActiveSpeakerRoute() {
        // This pure policy evaluates the route of the AudioTrack, not devices
        // merely connected to Android. A2DP is speaker/headphone ambiguous.
        assertFalse(ActiveOutputRoutePolicy.isTrusted(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP))
        assertFalse(ActiveOutputRoutePolicy.isTrusted(AudioDeviceInfo.TYPE_BLUETOOTH_SCO))
        assertFalse(ActiveOutputRoutePolicy.isTrusted(AudioDeviceInfo.TYPE_BLE_HEADSET))
        assertFalse(ActiveOutputRoutePolicy.isTrusted(AudioDeviceInfo.TYPE_BLE_SPEAKER))
    }

    @Test fun hearingAidsHeadphonesUsbAndUnknownFailClosed() {
        assertFalse(ActiveOutputRoutePolicy.isTrusted(AudioDeviceInfo.TYPE_HEARING_AID))
        assertFalse(ActiveOutputRoutePolicy.isTrusted(AudioDeviceInfo.TYPE_WIRED_HEADPHONES))
        assertFalse(ActiveOutputRoutePolicy.isTrusted(AudioDeviceInfo.TYPE_WIRED_HEADSET))
        assertFalse(ActiveOutputRoutePolicy.isTrusted(AudioDeviceInfo.TYPE_USB_DEVICE))
        assertFalse(ActiveOutputRoutePolicy.isTrusted(AudioDeviceInfo.TYPE_USB_HEADSET))
        assertFalse(ActiveOutputRoutePolicy.isTrusted(AudioDeviceInfo.TYPE_HDMI))
        assertFalse(ActiveOutputRoutePolicy.isTrusted(null))
        assertFalse(ActiveOutputRoutePolicy.isTrusted(-1))
    }
}
