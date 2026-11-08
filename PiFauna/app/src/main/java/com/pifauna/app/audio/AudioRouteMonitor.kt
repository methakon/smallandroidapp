package com.pifauna.app.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.Log

/**
 * Enumerates externally connected output devices for diagnostics and observes
 * Android output-device inventory changes. This is NOT the AudioTrack route.
 * A connected Bluetooth device may be unused by the media stream.
 * SoundEngine separately checks the actual AudioTrack.routedDevice before
 * writing nonzero PCM and while playing; never authorize output from this list.
 */
class AudioRouteMonitor(context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private var callback: AudioDeviceCallback? = null

    /**
     * Conservative preflight for PHYSICAL outputs only.
     *
     * AudioManager.getDevices() returns *connected* outputs, not the media route.
     * In particular, isBluetoothA2dpOn is not proof that this AudioTrack is
     * routed to Bluetooth; blocking on it can deny the phone speaker even when
     * the user deliberately selected the phone speaker.
     *
     * Wired/USB/HDMI outputs remain conservatively blocked when connected
     * because preflight cannot prove where media will go. Bluetooth alone
     * NEVER causes a preflight refusal; SoundEngine independently checks the
     * specific AudioTrack.routedDevice before writing any nonzero PCM and
     * stops playback when that route becomes unsafe.
     */
    fun activeExternalRoute(): String? {
        val devices = runCatching {
            audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        }.getOrNull() ?: return null

        val physicalExternal = devices.firstOrNull { it.type in PHYSICAL_EXTERNAL_TYPES }
        return physicalExternal?.let(::formatDeviceName)
    }

    /** First available external output (compatibility alias for [activeExternalRoute]). */
    fun externalRouteName(): String? = activeExternalRoute()

    private fun formatDeviceName(device: AudioDeviceInfo): String {
        return when (device.type) {
            AudioDeviceInfo.TYPE_WIRED_HEADSET -> "A wired headset"
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Wired headphones"
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "A Bluetooth audio device"
            AudioDeviceInfo.TYPE_USB_DEVICE,
            AudioDeviceInfo.TYPE_USB_HEADSET -> "A USB audio device"
            AudioDeviceInfo.TYPE_HEARING_AID -> "A hearing aid"
            AudioDeviceInfo.TYPE_HDMI -> "An HDMI output"
            AudioDeviceInfo.TYPE_LINE_ANALOG,
            AudioDeviceInfo.TYPE_LINE_DIGITAL -> "A line output"
            AudioDeviceInfo.TYPE_DOCK -> "An audio dock"
            AudioDeviceInfo.TYPE_BLE_HEADSET,
            AudioDeviceInfo.TYPE_BLE_SPEAKER,
            AudioDeviceInfo.TYPE_BLE_BROADCAST -> "A Bluetooth LE audio device"
            else -> device.productName?.toString()?.takeIf { it.isNotBlank() } ?: "An external audio device"
        }
    }

    /** Ratio of current media volume to maximum, 0.0..1.0. */
    fun mediaVolumeRatio(): Float {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        if (max <= 0) return 1f
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        return (current.toFloat() / max).coerceIn(0f, 1f)
    }

    /**
     * Starts reporting route changes. [onChange] runs on the main thread and is
     * only delivered while monitoring is active.
     */
    fun start(onChange: () -> Unit) {
        stop()
        val deviceCallback = object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
                if (addedDevices.any { isExternal(it.type) }) notify(onChange)
            }

            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
                // Removal can hand output back to the speaker; re-evaluate either way.
                notify(onChange)
            }
        }
        callback = deviceCallback
        runCatching { audioManager.registerAudioDeviceCallback(deviceCallback, mainHandler) }
            .onFailure { Log.w(TAG, "cannot register audio device callback", it) }
    }

    fun stop() {
        callback?.let { runCatching { audioManager.unregisterAudioDeviceCallback(it) } }
        callback = null
    }

    private fun notify(onChange: () -> Unit) {
        mainHandler.post { runCatching(onChange) }
    }

    private fun isExternal(type: Int): Boolean = type in EXTERNAL_TYPES

    internal companion object {
        const val TAG = "PiFaunaRoute"

        /**
         * Device type constants newer than minSdk are inlined by the compiler, so
         * comparing an `AudioDeviceInfo.type` int against them is safe on every
         * supported version: an older platform can never report a newer type.
         */
        @SuppressLint("InlinedApi")
        val PHYSICAL_EXTERNAL_TYPES = setOf(
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            AudioDeviceInfo.TYPE_USB_DEVICE,
            AudioDeviceInfo.TYPE_USB_HEADSET,
            AudioDeviceInfo.TYPE_HEARING_AID,
            AudioDeviceInfo.TYPE_HDMI,
            AudioDeviceInfo.TYPE_LINE_ANALOG,
            AudioDeviceInfo.TYPE_LINE_DIGITAL,
            AudioDeviceInfo.TYPE_DOCK
        )

        @SuppressLint("InlinedApi")
        val BT_MEDIA_TYPES = setOf(
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_BLE_HEADSET,
            AudioDeviceInfo.TYPE_BLE_SPEAKER,
            AudioDeviceInfo.TYPE_BLE_BROADCAST
        )

        @SuppressLint("InlinedApi")
        val EXTERNAL_TYPES = PHYSICAL_EXTERNAL_TYPES + BT_MEDIA_TYPES + setOf(
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO
        )
    }
}

/** Pure helper: connected physical outputs are a conservative early refusal.
 * Bluetooth media sinks are NOT classified as active from device inventory.
 * Only SoundEngine may decide the actual Bluetooth/phone speaker media route.
 */
internal object AudioRoutePreflightClassifier {
    fun isExternalRouteActive(outputDeviceTypes: Collection<Int>): Boolean =
        outputDeviceTypes.any { it in AudioRouteMonitor.PHYSICAL_EXTERNAL_TYPES }
}
