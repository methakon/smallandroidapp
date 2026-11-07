package com.pifauna.app.mantra

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.AudioFocusRequest
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.os.BatteryManager
import android.content.Intent
import android.content.IntentFilter
import java.io.File

/**
 * Foreground-only local mantra capture and finite counted replay.
 * No foreground service, boot receiver, network, or automatic restart.
 */
class MantraAudioEngine(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private val main = Handler(Looper.getMainLooper())
    private var pending: Runnable? = null
    private var recordingFile: File? = null
    private var recordingStart = 0L
    private var sessionStart = 0L
    private var count = 0
    private var completed = 0
    private var gapMs = 1000L
    private var callback: ((Int, String?) -> Unit)? = null
    private var currentFile: File? = null
    private var recording = false
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var focusRequest: AudioFocusRequest? = null
    private val safetyPoll = object : Runnable {
        override fun run() {
            if (callback == null) return
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val volume = if (max > 0)
                audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / max else 1f
            if (volume > 0.6f) {
                finish("Stopped: media volume exceeds 60%.")
                return
            }
            val battery = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level < 0 || scale <= 0 || level * 100 / scale <= 15) {
                finish("Stopped: battery status unknown or at 15% or less.")
                return
            }
            val active = player
            if (active != null && !verifiedSpeakerRoute(active)) {
                finish("Stopped: phone speaker route is not verified.")
                return
            }
            if (SystemClock.elapsedRealtime() - sessionStart >= MantraRepetitionPolicy.MAX_SESSION_MS) {
                finish("Stopped: the 30-minute session limit was reached.")
                return
            }
            main.postDelayed(this, 250L)
        }
    }

    private fun requestPlaybackFocus(): Boolean {
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
            .setWillPauseWhenDucked(true)
            .setOnAudioFocusChangeListener({ change ->
                if (change <= AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                    finish("Stopped: audio focus was lost.")
                }
            }, main)
            .build()
        val granted = audioManager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        if (granted) focusRequest = request
        return granted
    }

    @Suppress("DEPRECATION")
    private fun legacyRecorder(): MediaRecorder = MediaRecorder()

    val isRecording: Boolean get() = recording
    val isPlaying: Boolean get() = player != null || pending != null

    fun beginCapture(file: File) {
        stopPlayback()
        discardCapture()
        val capture = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(context)
            else legacyRecorder()
        try {
            capture.setAudioSource(MediaRecorder.AudioSource.MIC)
            capture.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            capture.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            capture.setAudioEncodingBitRate(96_000)
            capture.setAudioSamplingRate(44_100)
            capture.setMaxDuration(90_000)
            capture.setOutputFile(file.absolutePath)
            capture.prepare()
            capture.start()
            recorder = capture
            recordingFile = file
            recordingStart = SystemClock.elapsedRealtime()
            recording = true
        } catch (failure: Exception) {
            runCatching { capture.reset(); capture.release() }
            file.delete()
            throw failure
        }
    }

    /** Returns the accepted take and duration; caller decides to save or retry. */
    fun finishCapture(): Pair<File, Long> {
        val capture = recorder ?: error("No recording started")
        recorder = null
        recording = false
        val file = requireNotNull(recordingFile)
        recordingFile = null
        val duration = (SystemClock.elapsedRealtime() - recordingStart).coerceAtMost(90_000L)
        try {
            capture.stop()
            capture.release()
            require(duration >= 300L && file.length() > 100L) { "Record a longer take." }
            return file to duration
        } catch (failure: Exception) {
            runCatching { capture.reset(); capture.release() }
            file.delete()
            throw failure
        }
    }

    fun discardCapture() {
        recorder?.let { runCatching { it.stop() }; runCatching { it.reset(); it.release() } }
        recorder = null
        recording = false
        recordingFile?.delete()
        recordingFile = null
    }

    /** Preview is exactly one play, not one 108-count set. */
    fun startPreview(file: File, onProgress: (Int, String?) -> Unit) {
        startPlaybackCount(file, 1, 0L, onProgress)
    }

    /** Only a user-approved number of complete sets is interpreted as japa. */
    fun startReplay(
        file: File,
        sets: Int,
        betweenMs: Long,
        onProgress: (completed: Int, error: String?) -> Unit
    ) {
        require(MantraRepetitionPolicy.permitted(sets, betweenMs))
        startPlaybackCount(file, MantraRepetitionPolicy.totalRepetitions(sets), betweenMs, onProgress)
    }

    private fun startPlaybackCount(
        file: File, repeats: Int, betweenMs: Long,
        onProgress: (completed: Int, error: String?) -> Unit
    ) {
        require(repeats >= 1 && repeats <= 11_664 && betweenMs >= 0L)
        require(file.isFile)
        stopPlayback()
        count = repeats
        completed = 0
        gapMs = betweenMs
        currentFile = file
        callback = onProgress
        sessionStart = SystemClock.elapsedRealtime()
        if (!requestPlaybackFocus()) {
            finish("Another app is using the audio output.")
            return
        }
        main.postDelayed(safetyPoll, 250L)
        launchOne()
    }

    /**
     * MediaPlayer routing APIs first became available in Android 9 (API 28).
     * Older versions are denied, never routed based on a guessed output.
     * On Android 16, check every simultaneously reported route.
     */
    private fun verifiedSpeakerRoute(p: MediaPlayer): Boolean {
        if (Build.VERSION.SDK_INT < 28) return false
        return runCatching {
            if (Build.VERSION.SDK_INT >= 36) {
                val routes = p.routedDevices
                routes.size == 1 && routes.first().type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
            } else {
                p.routedDevice?.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
            }
        }.getOrDefault(false)
    }

    private fun launchOne() {
        if (Build.VERSION.SDK_INT < 28) {
            finish("Verified voice replay requires Android 9 or newer.")
            return
        }
        if (SystemClock.elapsedRealtime() - sessionStart >= MantraRepetitionPolicy.MAX_SESSION_MS) {
            finish("Session reached the 30-minute safety limit.")
            return
        }
        val file = currentFile ?: return
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val ratio = if (max > 0) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / max else 1f
        if (ratio > 0.6f) {
            finish("Lower media volume to 60% or less.")
            return
        }
        val p = MediaPlayer()
        try {
            p.setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
            p.setDataSource(file.absolutePath)
            p.prepare()
            // Start silent: no recorded voice is allowed until the real route
            // reports the phone speaker; a connected BT device alone is irrelevant.
            p.setVolume(0f, 0f)
            p.setOnCompletionListener {
                runCatching { p.release() }
                if (player === p) player = null
                completed++
                callback?.invoke(completed, null)
                if (completed >= count) {
                    finish(null)
                } else {
                    val next = Runnable { pending = null; launchOne() }
                    pending = next
                    main.postDelayed(next, gapMs)
                }
            }
            p.setOnErrorListener { _, _, _ ->
                finish("Audio playback error.")
                true
            }
            p.addOnRoutingChangedListener({
                if (!verifiedSpeakerRoute(p)) {
                    finish("Playback stopped: output changed from phone speaker.")
                }
            }, main)
            player = p
            p.start()
            if (!verifiedSpeakerRoute(p)) {
                finish("Only the built-in phone speaker is supported.")
                return
            }
            p.setVolume(0.35f, 0.35f)
        } catch (failure: Exception) {
            runCatching { p.release() }
            if (player === p) player = null
            finish(failure.message ?: "Cannot play this recording.")
        }
    }

    private fun finish(message: String?) {
        val callbackNow = callback
        val total = completed
        stopPlayback()
        callbackNow?.invoke(total, message)
    }

    fun stopPlayback() {
        pending?.let(main::removeCallbacks)
        pending = null
        main.removeCallbacks(safetyPoll)
        focusRequest?.let { runCatching { audioManager.abandonAudioFocusRequest(it) } }
        focusRequest = null
        player?.let { runCatching { it.setVolume(0f, 0f); it.stop() }; runCatching { it.release() } }
        player = null
        callback = null
        currentFile = null
    }

    fun close() {
        discardCapture()
        stopPlayback()
    }
}
