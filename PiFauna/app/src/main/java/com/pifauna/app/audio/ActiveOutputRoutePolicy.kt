package com.pifauna.app.audio

import android.annotation.SuppressLint
import android.media.AudioDeviceInfo

/**
 * Routes are identified from the actual AudioTrack.routedDevice, never merely
 * from AudioManager's connected-device inventory. Unknown always fails closed.
 *
 * Device TYPE_BUILTIN_SPEAKER is the only allowed route for v11 development.
 * Bluetooth A2DP, BLE, hearing aids, USB, wired headsets and all other output
 * types are deliberately refused, regardless of user-provided device names.
 */
internal object ActiveOutputRoutePolicy {
    @SuppressLint("InlinedApi")
    fun isTrusted(type: Int?): Boolean =
        type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
}
