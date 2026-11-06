package com.pifauna.app.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.AudioRouting
import android.os.Handler
import android.os.Looper
import android.media.VolumeShaper
import android.os.Process
import android.os.SystemClock
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

/** Why a session ended. */
enum class FinishReason { COMPLETED, STOPPED, ERROR }

/** Result of a start request; the token lets callers discard stale callbacks. */
sealed interface StartResult {
    data class Started(val token: Long) : StartResult
    /** A previous session is still shutting down; refusing is safer than overlapping. */
    data object Busy : StartResult
    data object Unavailable : StartResult
}

/**
 * Streams synthesised PCM to the default output.
 *
 * Ownership contract
 * ------------------
 * Exactly one thread tears the [AudioTrack] down. The render worker (on natural
 * completion, error or an external stop) and the stopping thread both race for
 * [teardownClaim]; whoever wins releases the track, the loser only joins and
 * reports. Before this, `stop()` set the running flag first and then slept for
 * its fade, so the worker's `finally` could pause, flush and release the track
 * during that sleep — the fade ran against a dead object and the tone was cut.
 *
 *  - at most one session exists at a time; [start] returns [StartResult.Busy]
 *    rather than overlapping a session that has not finished joining;
 *  - [stop] fades out with an explicit [VolumeShaper] curve, then pauses and
 *    flushes, so a user-initiated stop ends audibly rather than with a click;
 *  - [stopImmediately] skips the fade entirely. Safety events — route change,
 *    thermal warning, focus loss, leaving the foreground — must be obeyed now,
 *    so they never wait for acoustic smoothing;
 *  - a session that reaches its own end is DRAINED, not flushed, so the queued
 *    tail, which carries the kernel's fade-out, actually plays;
 *  - one render buffer is allocated per engine and reused for every session;
 *  - `onFinished` is invoked on the audio worker thread, never on the caller's.
 *
 * This engine refuses nonzero PCM until the AudioTrack's ACTUAL routed device
 * is verified to be the built-in loudspeaker. Connected-output enumeration does
 * not identify the active output. A null or other route always fails closed.
 * The routing check is repeated before every PCM write to detect changes.
 */
class SoundEngine {

    private val lock = Any()
    private val running = AtomicBoolean(false)

    /**
     * One-shot claim on the track. Whoever CASes it from false to true owns the
     * track and is responsible for releasing it. Starts true so that a stop
     * before any session exists is a no-op.
     */
    private val teardownClaim = AtomicBoolean(true)

    @Volatile private var worker: Thread? = null
    @Volatile private var track: AudioTrack? = null
    @Volatile private var routeListener: AudioRouting.OnRoutingChangedListener? = null
    private val routeRejected = AtomicBoolean(false)
    private val routeConfirmed = AtomicBoolean(false)
    @Volatile private var renderBuffer: ShortArray? = null

    private var nextToken: Long = 0L
    private var activeToken: Long = 0L

    /** True while a session is actively writing samples. */
    val isPlaying: Boolean
        get() = running.get() && worker?.isAlive == true

    fun start(config: ToneConfig, onFinished: (FinishReason, Long) -> Unit): StartResult =
        synchronized(lock) {
            // Never overlap sessions: if the old worker will not die, refuse.
            if (!shutdownLocked(graceful = false)) {
                Log.w(TAG, "previous session still alive; refusing to start another")
                return StartResult.Busy
            }

            val minBytes = AudioTrack.getMinBufferSize(
                config.sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBytes <= 0) {
                Log.w(TAG, "getMinBufferSize failed: $minBytes")
                return StartResult.Unavailable
            }
            val bufferBytes = maxOf(minBytes * 2, CHUNK_FRAMES * 2 * 2)

            val audio = try {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(config.sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .setBufferSizeInBytes(bufferBytes)
                    .build()
            } catch (error: Exception) {
                Log.e(TAG, "AudioTrack construction failed", error)
                return StartResult.Unavailable
            }

            if (audio.state != AudioTrack.STATE_INITIALIZED) {
                Log.w(TAG, "AudioTrack not initialised (state=${audio.state})")
                runCatching { audio.release() }
                return StartResult.Unavailable
            }

            val buffer = renderBuffer ?: ShortArray(CHUNK_FRAMES).also { renderBuffer = it }
            val token = ++nextToken
            activeToken = token
            track = audio
            teardownClaim.set(false)
            routeRejected.set(false)
            routeConfirmed.set(false)
            running.set(true)

            // Start the track here, while holding the same lock that teardown takes.
            // Doing it on the worker raced with an immediate stop(), which paused and
            // flushed the track first and made play() log "start(): ... mState = 4".
            val started = runCatching { audio.play() }.isSuccess
            if (!started) {
                running.set(false)
                teardownClaim.set(true)
                track = null
                runCatching { audio.release() }
                return StartResult.Unavailable
            }

            // Routing callback is an urgent safety stop; write-loop checks remain
            // an independent guard against delayed or missing notifications.
            val listener = AudioRouting.OnRoutingChangedListener { routing ->
                val routeType = routing.routedDevice?.type
                // A null callback during silent priming can be normal on some
                // devices. Before the first verified speaker route no tone is
                // written anyway. After verification, null is unsafe.
                val unsafe = when {
                    ActiveOutputRoutePolicy.isTrusted(routeType) -> false
                    routeType == null && !routeConfirmed.get() -> false
                    else -> true
                }
                if (unsafe && track === audio && running.get()) {
                    routeRejected.set(true)
                    running.set(false)
                    runCatching { audio.pause() }
                    Log.w(TAG, "AudioTrack route changed away from built-in speaker")
                }
            }
            routeListener = listener
            val observed = runCatching {
                audio.addOnRoutingChangedListener(listener, Handler(Looper.getMainLooper()))
            }.isSuccess
            if (!observed) {
                running.set(false)
                teardownClaim.set(true)
                routeListener = null
                track = null
                runCatching { audio.pause() }
                runCatching { audio.release() }
                return StartResult.Unavailable
            }

            val session = Thread({ runSession(audio, config, token, buffer, onFinished) }, "PiFaunaAudio")
            session.isDaemon = true
            session.priority = Thread.NORM_PRIORITY + 2
            worker = session
            session.start()

            StartResult.Started(token)
        }

    /** Fades out over ~25 ms, then releases. Used for a user-initiated stop. */
    fun stop() {
        synchronized(lock) { shutdownLocked(graceful = true) }
    }

    /**
     * Tears down with no fade. Safety events use this: a route change or a
     * thermal warning must be obeyed immediately, not after an acoustic nicety.
     */
    fun stopImmediately() {
        synchronized(lock) { shutdownLocked(graceful = false) }
    }

    /**
     * Must be called with [lock] held. Returns true when no worker is alive
     * afterwards.
     */
    private fun shutdownLocked(graceful: Boolean): Boolean {
        running.set(false)
        routeConfirmed.set(false)

        val audio = track
        // Only the claim winner may touch the track: the worker may already have
        // released it, and fading a dead object would be a lie about the output.
        val ownsTeardown = audio != null && teardownClaim.compareAndSet(false, true)

        if (ownsTeardown && graceful) {
            fadeOut(audio!!)
        }
        if (ownsTeardown) {
            unregisterRouteListener(audio!!)
            // pause() stops consumption and flush() discards queued frames, which
            // is what unblocks a writer parked in write(..., WRITE_BLOCKING).
            runCatching { if (audio!!.playState == AudioTrack.PLAYSTATE_PLAYING) audio.pause() }
            runCatching { audio.flush() }
        }

        val current = worker
        if (current != null && current !== Thread.currentThread() && current.isAlive) {
            runCatching { current.join(JOIN_TIMEOUT_MS) }
                .onFailure { Thread.currentThread().interrupt() }
        }

        if (ownsTeardown) {
            runCatching { audio!!.stop() }
            runCatching { audio.release() }
            if (track === audio) track = null
        }

        val alive = worker?.isAlive == true
        if (!alive) {
            worker = null
        }
        return !alive
    }

    private fun runSession(
        audio: AudioTrack,
        config: ToneConfig,
        token: Long,
        buffer: ShortArray,
        onFinished: (FinishReason, Long) -> Unit
    ) {
        var reason = FinishReason.STOPPED
        var rendered = 0L
        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
            var phase = 0.0
            // Only SILENCE may be written before the track reports a verified route.
            // Many Android implementations report null until the first write.
            // Silence primes routing without exposing the experimental tone.
            val silentFrames = ShortArray(CHUNK_FRAMES)
            var attempts = 0
            var primedFrames = 0L
            while (running.get() && audio.routedDevice == null && attempts < ROUTE_PRIME_ATTEMPTS) {
                val count = audio.write(silentFrames, 0, silentFrames.size, AudioTrack.WRITE_BLOCKING)
                if (count <= 0) {
                    reason = FinishReason.ERROR
                    return
                }
                primedFrames += count
                attempts++
            }
            // This gate denies Bluetooth A2DP and BLE (including ambiguous devices),
            // headsets, hearing aids, USB, HDMI, null, and unknown output types.
            if (!isBuiltInSpeakerRoute(audio)) {
                Log.w(TAG, "route unverified or not the built-in speaker; suppressing PCM")
                reason = FinishReason.ERROR
                return
            }
            routeConfirmed.set(true)
            if (!running.get() || !isBuiltInSpeakerRoute(audio)) {
                reason = FinishReason.ERROR
                return
            }

            session@ while (running.get() && rendered < config.totalFrames) {
                val frames = min(CHUNK_FRAMES.toLong(), config.totalFrames - rendered).toInt()
                if (!isBuiltInSpeakerRoute(audio)) {
                    reason = FinishReason.ERROR
                    break@session
                }
                phase = ToneKernel.renderInto(config, rendered, frames, phase, buffer)

                var offset = 0
                while (offset < frames) {
                    if (!running.get()) break@session
                    if (!isBuiltInSpeakerRoute(audio)) {
                        reason = FinishReason.ERROR
                        break@session
                    }
                    val written = audio.write(buffer, offset, frames - offset, AudioTrack.WRITE_BLOCKING)
                    if (written < 0) {
                        Log.w(TAG, "AudioTrack.write returned $written")
                        reason = FinishReason.ERROR
                        break@session
                    }
                    if (written == 0) {
                        // No progress: treat as a device loss instead of spinning.
                        reason = FinishReason.ERROR
                        break@session
                    }
                    offset += written
                }
                rendered += frames
            }

            if (rendered >= config.totalFrames) reason = FinishReason.COMPLETED
        } catch (error: Exception) {
            // Output devices disappear mid-session (unplug, Bluetooth drop, call).
            Log.w(TAG, "session ended with error", error)
            reason = FinishReason.ERROR
        } finally {
            if (teardownClaim.compareAndSet(false, true)) {
                unregisterRouteListener(audio)
                // No stop arrived first, so this thread owns the track.
                if (reason == FinishReason.COMPLETED) {
                    // A natural completion must play out what is still queued.
                    // flush() here used to discard up to a full buffer (~40 ms),
                    // which contains the fade-out, so every session ended with a
                    // hard cut.
                    val drained = awaitDrain(audio, config, rendered + primedFrames)
                    if (!drained) {
                        // A stop arrived mid-drain: the tail was cut, so this
                        // session did not complete cleanly.
                        reason = FinishReason.STOPPED
                    }
                    running.set(false)
                    runCatching { audio.stop() }
                } else {
                    running.set(false)
                    runCatching { audio.pause() }
                    runCatching { audio.flush() }
                    runCatching { audio.stop() }
                }
                runCatching { audio.release() }
                if (track === audio) track = null
            } else {
                // Another thread claimed teardown and is fading or flushing right
                // now. The queued tail was cut short, so report an interruption
                // even when every frame had already been rendered.
                reason = FinishReason.STOPPED
            }

            routeConfirmed.set(false)
            if (routeRejected.get()) reason = FinishReason.ERROR
            if (worker === Thread.currentThread()) worker = null
            if (activeToken == token) activeToken = 0L
            runCatching { onFinished(reason, token) }
        }
    }

    /**
     * Ramps the track to silence along an explicit [VolumeShaper] curve and waits
     * for the ramp to finish. `setVolume(0f)` followed by a sleep only asks the
     * mixer to smooth the change over an unspecified interval; VolumeShaper
     * states both the duration and the curve, and exists from API 26, our
     * minimum.
     *
     * If the platform refuses a shaper (offloaded or differently routed tracks),
     * the gain is zeroed directly, so behaviour is no worse than a hard pause.
     */
    private fun fadeOut(audio: AudioTrack) {
        try {
            if (audio.playState != AudioTrack.PLAYSTATE_PLAYING) return

            val curve = VolumeShaper.Configuration.Builder()
                .setDuration(FADE_OUT_MS)
                .setCurve(floatArrayOf(0f, 1f), floatArrayOf(1f, 0f))
                .setInterpolatorType(VolumeShaper.Configuration.INTERPOLATOR_TYPE_LINEAR)
                .build()

            val shaper = audio.createVolumeShaper(curve)
            if (shaper == null) {
                fallbackMute(audio)
                return
            }
            shaper.use {
                it.apply(VolumeShaper.Operation.PLAY)
                val deadline = SystemClock.elapsedRealtime() + FADE_OUT_MS + FADE_MARGIN_MS
                while (SystemClock.elapsedRealtime() < deadline &&
                    it.volume > FADE_DONE_EPSILON
                ) {
                    Thread.sleep(FADE_POLL_MS)
                }
            }
        } catch (interrupted: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (unsupported: Throwable) {
            // Track already released, or a device that refuses shapers.
            fallbackMute(audio)
        }
    }

    private fun fallbackMute(audio: AudioTrack) {
        runCatching { audio.setVolume(0f) }
        runCatching { Thread.sleep(FADE_OUT_MS) }
    }

    /**
     * Blocks until the hardware has consumed every frame written, or until a
     * deadline derived from the buffer length expires.
     *
     * @return true when the tail finished playing, false when a stop interrupted
     *   it, in which case the caller must report the session as stopped rather
     *   than completed.
     */
    private fun awaitDrain(audio: AudioTrack, config: ToneConfig, framesWritten: Long): Boolean {
        val bufferMs = audio.bufferSizeInFrames * 1000L / config.sampleRate
        val deadline = SystemClock.elapsedRealtime() + bufferMs + DRAIN_MARGIN_MS
        while (running.get() && SystemClock.elapsedRealtime() < deadline) {
            // playbackHeadPosition is an unsigned 32-bit frame counter.
            val played = audio.playbackHeadPosition.toLong() and 0xFFFF_FFFFL
            if (played >= framesWritten) {
                // The head position runs slightly ahead of the speaker; cover the
                // output latency.
                if (running.get()) Thread.sleep(DRAIN_TAIL_MS)
                return running.get()
            }
            Thread.sleep(DRAIN_POLL_MS)
        }
        return false
    }

    private fun unregisterRouteListener(audio: AudioTrack) {
        routeListener?.let { listener ->
            runCatching { audio.removeOnRoutingChangedListener(listener) }
        }
        routeListener = null
    }

    private fun isBuiltInSpeakerRoute(audio: AudioTrack): Boolean =
        runCatching {
            ActiveOutputRoutePolicy.isTrusted(audio.routedDevice?.type)
        }.getOrDefault(false)

    private companion object {
        // Maximum zero-PCM priming: ~210 ms at 48 kHz, then fail closed.
        const val ROUTE_PRIME_ATTEMPTS = 10
        const val TAG = "PiFaunaEngine"

        /** 1024 frames ≈ 21 ms at 48 kHz: small enough to stop promptly, large enough to be cheap. */
        const val CHUNK_FRAMES = 1024
        const val JOIN_TIMEOUT_MS = 1500L

        /** VolumeShaper fade applied on a graceful stop. */
        const val FADE_OUT_MS = 25L
        const val FADE_MARGIN_MS = 15L
        const val FADE_POLL_MS = 2L
        const val FADE_DONE_EPSILON = 0.01f

        /** Slack beyond the buffer length before giving up on a drain. */
        const val DRAIN_MARGIN_MS = 250L
        const val DRAIN_POLL_MS = 5L
        const val DRAIN_TAIL_MS = 40L
    }
}
