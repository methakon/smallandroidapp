package com.pifauna.app.playback

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import com.pifauna.app.audio.AudioRouteMonitor
import com.pifauna.app.audio.FinishReason
import com.pifauna.app.audio.SoundEngine
import com.pifauna.app.audio.StartResult
import com.pifauna.app.audio.ToneConfig
import com.pifauna.app.audio.ToneMode
import com.pifauna.app.data.SettingsStore
import com.pifauna.app.data.SpeciesProfiles
import com.pifauna.app.safety.BlockCode
import com.pifauna.app.safety.SafetyDecision
import com.pifauna.app.safety.SafetyLimits
import com.pifauna.app.safety.SafetyPolicy
import com.pifauna.app.safety.SafetySnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PlaybackPhase { Idle, Playing, Completed, Refused }

data class PlayerState(
    val phase: PlaybackPhase = PlaybackPhase.Idle,
    val profileId: String = SpeciesProfiles.all.first().id,
    val displayName: String? = null,
    val startHz: Int = SpeciesProfiles.all.first().firstHz,
    val endHz: Int = SpeciesProfiles.all.first().lastHz,
    val mode: ToneMode = ToneMode.FIXED,
    val gain: Float = SettingsStore.DEFAULT_GAIN,
    val durationMinutes: Int = 10,
    val startedAtElapsed: Long = 0L,
    val statusMessage: String = "Ready — nothing is playing.",
    val snapshot: SafetySnapshot = SafetySnapshot()
) {
    val isPlaying: Boolean get() = phase == PlaybackPhase.Playing
    val durationMs: Long get() = durationMinutes * 60_000L

    /** Milliseconds left, evaluated against the monotonic clock. */
    fun remainingMs(nowElapsed: Long = SystemClock.elapsedRealtime()): Long {
        if (!isPlaying) return 0L
        return (startedAtElapsed + durationMs - nowElapsed).coerceAtLeast(0L)
    }
}

/**
 * Owns playback policy for the single activity.
 *
 * Everything that can stop sound lives here: the safety gate before start,
 * audio-focus loss, route changes, battery and thermal events, and the
 * foreground lifecycle. The synthesis engine itself knows nothing about policy.
 */
class PlayerController(context: Context) {

    private val appContext = context.applicationContext
    private val engine = SoundEngine()
    private val routeMonitor = AudioRouteMonitor(appContext)
    private val audioManager =
        appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val powerManager =
        appContext.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _state = MutableStateFlow(
        PlayerState(
            profileId = SpeciesProfiles.all.first().id,
            startHz = SpeciesProfiles.all.first().firstHz,
            endHz = SpeciesProfiles.all.first().lastHz
        )
    )
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var focusRequest: AudioFocusRequest? = null
    private var activeToken = 0L
    private var monitoringRoute = false

    // ---------------------------------------------------------------- readiness

    fun snapshot(): SafetySnapshot {
        val thermal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            powerManager.currentThermalStatus
        } else {
            0
        }
        return SafetySnapshot(
            batteryPercent = readBatteryPercent(),
            thermalStatus = thermal,
            externalAudioRoute = routeMonitor.externalRouteName(),
            mediaVolumeRatio = routeMonitor.mediaVolumeRatio()
        )
    }

    private fun readBatteryPercent(): Int {
        val sticky = appContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return 100
        val level = sticky.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = sticky.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        return if (level >= 0 && scale > 0) level * 100 / scale else 100
    }

    /** Re-reads device state; call when a safety broadcast arrives or a screen opens. */
    fun refreshSnapshot() {
        val snapshot = snapshot()
        _state.value = _state.value.copy(snapshot = snapshot)
        maybeStopForHazard(snapshot)
    }

    // ------------------------------------------------------------------ editing

    fun selectProfile(profileId: String) {
        if (_state.value.isPlaying) return
        val profile = SpeciesProfiles.byId(profileId)
        _state.value = _state.value.copy(
            profileId = profile.id,
            displayName = null,
            startHz = profile.firstHz,
            endHz = profile.lastHz,
            phase = PlaybackPhase.Idle,
            statusMessage = "Ready — ${profile.name} band selected."
        )
    }

    /**
     * Prepare an audible meditation/relaxation tone without starting playback.
     * Playback still runs through the same safety policy, audio focus and timer.
     */
    fun prepareExperimentalTone(label: String, startHz: Int, endHz: Int, mode: ToneMode) {
        if (_state.value.isPlaying) return
        val low = minOf(startHz, endHz).coerceIn(SafetyLimits.MIN_HZ, SafetyLimits.MAX_HZ)
        val high = maxOf(startHz, endHz).coerceIn(SafetyLimits.MIN_HZ, SafetyLimits.MAX_HZ)
        _state.value = _state.value.copy(
            profileId = "custom",
            displayName = label.take(60),
            startHz = low,
            endHz = high,
            mode = mode,
            phase = PlaybackPhase.Idle,
            statusMessage = "Ready — experimental audible tone prepared; not yet playing."
        )
    }

    fun setRange(startHz: Int, endHz: Int) {
        if (_state.value.isPlaying) return
        val low = minOf(startHz, endHz).coerceIn(SafetyLimits.MIN_HZ, SafetyLimits.MAX_HZ)
        val high = maxOf(startHz, endHz).coerceIn(SafetyLimits.MIN_HZ, SafetyLimits.MAX_HZ)
        _state.value = _state.value.copy(startHz = low, endHz = high)
    }

    fun setMode(mode: ToneMode) {
        if (_state.value.isPlaying) return
        _state.value = _state.value.copy(mode = mode)
    }

    fun setGain(gain: Float) {
        if (_state.value.isPlaying) return
        _state.value = _state.value.copy(
            gain = gain.coerceIn(0.03f, SafetyLimits.MAX_GAIN)
        )
    }

    fun setDurationMinutes(minutes: Int) {
        if (_state.value.isPlaying) return
        val allowed = minutes.takeIf { it in SafetyLimits.DURATION_CHOICES_MIN } ?: 10
        _state.value = _state.value.copy(durationMinutes = allowed)
    }

    // ----------------------------------------------------------------- playback

    fun start() {
        val current = _state.value
        if (current.isPlaying) return

        val snapshot = snapshot()
        val decision = SafetyPolicy.evaluate(snapshot)
        if (decision is SafetyDecision.Blocked) {
            refuse(decision)
            return
        }

        if (!requestAudioFocus()) {
            refuse(
                SafetyDecision.Blocked(
                    BlockCode.INVALID_REQUEST,
                    "Playback blocked: another app is using audio output."
                )
            )
            return
        }

        val config = try {
            ToneConfig(
                startHz = current.startHz,
                endHz = current.endHz,
                mode = current.mode,
                gain = current.gain,
                durationMs = current.durationMinutes * 60_000L
            )
        } catch (error: IllegalArgumentException) {
            abandonAudioFocus()
            refuse(SafetyDecision.Blocked(BlockCode.INVALID_REQUEST, error.message ?: "Invalid request."))
            return
        }

        when (val result = engine.start(config) { reason, token -> onEngineFinished(reason, token) }) {
            is StartResult.Started -> {
                activeToken = result.token
                monitoringRoute = true
                routeMonitor.start { refreshSnapshot() }
                _state.value = _state.value.copy(
                    phase = PlaybackPhase.Playing,
                    startedAtElapsed = SystemClock.elapsedRealtime(),
                    snapshot = snapshot,
                    statusMessage = "Playing ${current.startHz}–${current.endHz} Hz " +
                        "for up to ${current.durationMinutes} min through the built-in speaker."
                )
            }
            StartResult.Busy -> {
                abandonAudioFocus()
                refuse(
                    SafetyDecision.Blocked(
                        BlockCode.INVALID_REQUEST,
                        "Playback blocked: the previous session is still shutting down. Try again."
                    )
                )
            }
            StartResult.Unavailable -> {
                abandonAudioFocus()
                _state.value = _state.value.copy(
                    phase = PlaybackPhase.Refused,
                    snapshot = snapshot,
                    statusMessage = "Audio output unavailable on this device right now."
                )
            }
        }
    }

    fun stop(reason: String) {
        val wasPlaying = _state.value.isPlaying
        teardownAudio()
        _state.value = _state.value.copy(
            phase = if (wasPlaying) PlaybackPhase.Idle else _state.value.phase,
            startedAtElapsed = 0L,
            statusMessage = reason
        )
    }

    /** Called from Activity.onStop: playback is foreground-only by design. */
    fun onAppBackgrounded() {
        if (_state.value.isPlaying) {
            stop("Stopped: the app left the foreground.")
        }
    }

    fun release() {
        teardownAudio()
        routeMonitor.stop()
    }

    private fun teardownAudio() {
        engine.stop()
        activeToken = 0L
        abandonAudioFocus()
        if (monitoringRoute) {
            routeMonitor.stop()
            monitoringRoute = false
        }
    }

    private fun maybeStopForHazard(snapshot: SafetySnapshot) {
        if (!_state.value.isPlaying) return
        val code = SafetyPolicy.mustStop(snapshot) ?: return
        val decision = SafetyPolicy.evaluate(snapshot) as? SafetyDecision.Blocked ?: return
        Log.i(TAG, "stopping for hazard $code")
        stop(decision.message)
    }

    private fun onEngineFinished(reason: FinishReason, token: Long) {
        mainHandler.post {
            // A stop() already moved the state out of Playing and owns the message;
            // this callback is then just the worker finishing its teardown.
            if (_state.value.phase != PlaybackPhase.Playing) return@post
            // Ignore a callback from a session that has already been replaced.
            if (token != activeToken) return@post
            teardownAudio()
            _state.value = _state.value.copy(
                phase = when (reason) {
                    FinishReason.COMPLETED -> PlaybackPhase.Completed
                    FinishReason.STOPPED -> PlaybackPhase.Idle
                    FinishReason.ERROR -> PlaybackPhase.Refused
                },
                startedAtElapsed = 0L,
                statusMessage = when (reason) {
                    FinishReason.COMPLETED -> "Session finished — playback stopped automatically."
                    FinishReason.STOPPED -> "Stopped."
                    FinishReason.ERROR -> "Playback stopped: the audio output reported an error."
                }
            )
        }
    }

    private fun refuse(decision: SafetyDecision.Blocked) {
        _state.value = _state.value.copy(
            phase = PlaybackPhase.Refused,
            statusMessage = decision.message,
            snapshot = snapshot()
        )
    }

    // -------------------------------------------------------------- audio focus

    private fun requestAudioFocus(): Boolean {
        abandonAudioFocus()
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(attributes)
            .setWillPauseWhenDucked(true)
            .setOnAudioFocusChangeListener({ change -> onFocusChanged(change) }, mainHandler)
            .build()
        val granted = audioManager.requestAudioFocus(request) ==
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        if (granted) focusRequest = request
        return granted
    }

    private fun onFocusChanged(change: Int) {
        if (!_state.value.isPlaying) return
        if (change <= AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            stop("Stopped: another app took over audio output.")
        }
    }

    private fun abandonAudioFocus() {
        focusRequest?.let { runCatching { audioManager.abandonAudioFocusRequest(it) } }
        focusRequest = null
    }

    private companion object {
        const val TAG = "PiFaunaPlayer"
    }
}
