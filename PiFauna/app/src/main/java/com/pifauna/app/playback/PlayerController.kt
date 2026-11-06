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
import com.pifauna.app.audio.OutputMode
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
    val snapshot: SafetySnapshot = SafetySnapshot(),
    val outputMode: OutputMode = OutputMode.PHONE_SPEAKER,
    val externalSpeakerConsented: Boolean = false
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
            // A connected output is not necessarily the current playback destination.
            externalAudioRoute = null,
            availableExternalRoute = routeMonitor.externalRouteName(),
            activeOutput = if (_state.value.isPlaying) engine.routedOutput() else null,
            outputMode = _state.value.outputMode,
            externalSpeakerConsented = _state.value.externalSpeakerConsented,
            selectedDurationMinutes = _state.value.durationMinutes,
            selectedGain = _state.value.gain,
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
            outputMode = OutputMode.PHONE_SPEAKER,
            externalSpeakerConsented = false,
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
        _state.value = _state.value.copy(
            startHz = low,
            endHz = high,
            displayName = if (low == _state.value.startHz && high == _state.value.endHz) {
                _state.value.displayName
            } else null
        )
    }

    fun setMode(mode: ToneMode) {
        if (_state.value.isPlaying) return
        _state.value = _state.value.copy(
            mode = mode,
            displayName = if (mode == _state.value.mode) _state.value.displayName else null
        )
    }

    fun setGain(gain: Float) {
        if (_state.value.isPlaying) return
        _state.value = _state.value.copy(
            gain = gain.coerceIn(0.01f, SafetyLimits.MAX_GAIN)
        )
    }

    fun setDurationMinutes(minutes: Int) {
        if (_state.value.isPlaying) return
        val allowed = minutes.takeIf { it in SafetyLimits.DURATION_CHOICES_MIN } ?: 10
        _state.value = _state.value.copy(durationMinutes = allowed)
    }

    /**
     * Explicit, one-session user declaration that this is a free-standing
     * loudspeaker, NOT headphones, earbuds or a hearing aid.
     */
    fun confirmExternalSpeakerForSession() {
        if (_state.value.isPlaying) return
        _state.value = _state.value.copy(
            outputMode = OutputMode.EXTERNAL_BLUETOOTH_SPEAKER,
            externalSpeakerConsented = true,
            durationMinutes = 5,
            gain = com.pifauna.app.audio.AudioOutputPolicy.SPEAKER_MAX_GAIN,
            phase = PlaybackPhase.Idle,
            statusMessage = "External speaker approved for this session only. " +
                "Keep volume very low; acoustic output is unmeasured."
        )
    }

    fun choosePhoneSpeaker() {
        if (_state.value.isPlaying) return
        _state.value = _state.value.copy(
            outputMode = OutputMode.PHONE_SPEAKER,
            externalSpeakerConsented = false,
            phase = PlaybackPhase.Idle,
            statusMessage = "Phone speaker chosen. Actual route will be checked on Start."
        )
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

        val preferred = routeMonitor.preferredDevice(current.outputMode)
        if (preferred == null) {
            abandonAudioFocus()
            refuse(SafetyDecision.Blocked(
                BlockCode.EXTERNAL_ROUTE,
                "Requested audio output unavailable. Choose a phone or Bluetooth speaker."
            ))
            return
        }
        when (val result = engine.start(
            config = config,
            onFinished = { reason, token -> onEngineFinished(reason, token) },
            preferredDevice = preferred,
            requestedOutput = current.outputMode,
            onRoutingChanged = { refreshSnapshot() }
        )) {
            is StartResult.Started -> {
                activeToken = result.token
                monitoringRoute = true
                routeMonitor.start { refreshSnapshot() }
                _state.value = _state.value.copy(
                    phase = PlaybackPhase.Playing,
                    startedAtElapsed = SystemClock.elapsedRealtime(),
                    snapshot = snapshot(),
                    statusMessage = "Playing ${current.startHz}–${current.endHz} Hz for up to " +
                        "${current.durationMinutes} min via " +
                        (if (current.outputMode == OutputMode.PHONE_SPEAKER) "phone speaker." else "consented external speaker.")
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
            StartResult.OutputRouteBlocked -> {
                abandonAudioFocus()
                refuse(SafetyDecision.Blocked(
                    BlockCode.EXTERNAL_ROUTE,
                    "Playback blocked: the actual audio route could not be verified. " +
                        "Select the intended output in Android audio settings and try again."
                ))
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
        stopInternal(reason, graceful = true)
    }

    /**
     * Safety stops must not wait for an acoustic fade: a route change, thermal
     * warning, focus loss or a backgrounded app is obeyed immediately.
     */
    private fun stopForSafety(reason: String) {
        stopInternal(reason, graceful = false)
    }

    private fun stopInternal(reason: String, graceful: Boolean) {
        val wasPlaying = _state.value.isPlaying
        teardownAudio(graceful)
        _state.value = _state.value.copy(
            phase = if (wasPlaying) PlaybackPhase.Idle else _state.value.phase,
            startedAtElapsed = 0L,
            outputMode = OutputMode.PHONE_SPEAKER,
            externalSpeakerConsented = false,
            statusMessage = reason
        )
    }

    /** Called from Activity.onStop: playback is foreground-only by design. */
    fun onAppBackgrounded() {
        if (_state.value.isPlaying) {
            stopForSafety("Stopped: the app left the foreground.")
        }
    }

    fun release() {
        teardownAudio(graceful = false)
        routeMonitor.stop()
    }

    private fun teardownAudio(graceful: Boolean = true) {
        if (graceful) engine.stop() else engine.stopImmediately()
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
        stopForSafety(decision.message)
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
                outputMode = OutputMode.PHONE_SPEAKER,
                externalSpeakerConsented = false,
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
            stopForSafety("Stopped: another app took over audio output.")
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
