package com.pifauna.app.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log

/**
 * Available output devices != the actual media output route.
 * SoundEngine separately checks its live AudioTrack.getRoutedDevice() before
 * writing audible PCM and listens for changes to that live route.
 */
class AudioRouteMonitor(context: Context) {
    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val handler = Handler(Looper.getMainLooper())
    private var callback: AudioDeviceCallback? = null

    private fun outputs(): List<AudioDeviceInfo> = runCatching {
        audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).toList()
    }.getOrDefault(emptyList())

    fun preferredDevice(mode: OutputMode): AudioDeviceInfo? {
        val devices = outputs()
        if (mode == OutputMode.PHONE_SPEAKER) {
            return devices.firstOrNull { classify(it.type) == RoutedOutput.PHONE_SPEAKER }
        }
        // Android 13+ can report the *anticipated* media device, but the live
        // AudioTrack route is always verified again before sound is emitted.
        val mediaPreferred = if (Build.VERSION.SDK_INT >= 33) runCatching {
            audioManager.getAudioDevicesForAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            ).firstOrNull { classify(it.type).isExternalSpeakerCandidate() }
        }.getOrNull() else null
        return mediaPreferred ?: devices.firstOrNull {
            classify(it.type).isExternalSpeakerCandidate()
        }
    }

    /** Advisory information for UI only; this is NOT proof of selected output. */
    fun externalRouteName(): String? {
        val external = outputs().firstOrNull {
            classify(it.type) != RoutedOutput.PHONE_SPEAKER &&
                it.type != AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
        } ?: return null
        return when (classify(external.type)) {
            RoutedOutput.BLUETOOTH_A2DP_UNVERIFIED ->
                "Bluetooth A2DP available (speaker or headphones — unverified)"
            RoutedOutput.BLUETOOTH_LE_SPEAKER -> "Bluetooth LE speaker available"
            RoutedOutput.HEADPHONES -> "Headphones/headset available"
            RoutedOutput.HEARING_AID -> "A hearing aid is available"
            else -> "External audio available"
        }
    }

    fun mediaVolumeRatio(): Float {
        val maximum = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        if (maximum <= 0) return 0f
        return (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() /
            maximum).coerceIn(0f, 1f)
    }

    fun start(onChange: () -> Unit) {
        stop()
        val newCallback = object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
                handler.post { onChange() }
            }
            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
                handler.post { onChange() }
            }
        }
        callback = newCallback
        runCatching { audioManager.registerAudioDeviceCallback(newCallback, handler) }
            .onFailure { Log.w(TAG, "Audio device callback registration failed", it) }
    }

    fun stop() {
        callback?.let { runCatching { audioManager.unregisterAudioDeviceCallback(it) } }
        callback = null
    }

    companion object {
        private const val TAG = "LayaswarnaRoute"

        @SuppressLint("InlinedApi")
        fun classify(type: Int): RoutedOutput = when (type) {
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER_SAFE -> RoutedOutput.PHONE_SPEAKER
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> RoutedOutput.BLUETOOTH_A2DP_UNVERIFIED
            AudioDeviceInfo.TYPE_BLE_SPEAKER -> RoutedOutput.BLUETOOTH_LE_SPEAKER
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
            AudioDeviceInfo.TYPE_BLE_HEADSET -> RoutedOutput.HEADPHONES
            AudioDeviceInfo.TYPE_HEARING_AID -> RoutedOutput.HEARING_AID
            AudioDeviceInfo.TYPE_USB_DEVICE,
            AudioDeviceInfo.TYPE_USB_HEADSET,
            AudioDeviceInfo.TYPE_HDMI,
            AudioDeviceInfo.TYPE_LINE_ANALOG,
            AudioDeviceInfo.TYPE_LINE_DIGITAL,
            AudioDeviceInfo.TYPE_DOCK -> RoutedOutput.OTHER_EXTERNAL
            else -> RoutedOutput.UNKNOWN
        }

        private fun RoutedOutput.isExternalSpeakerCandidate(): Boolean =
            this == RoutedOutput.BLUETOOTH_A2DP_UNVERIFIED ||
                this == RoutedOutput.BLUETOOTH_LE_SPEAKER
    }
}
