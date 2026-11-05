package com.pifauna.app.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
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
 * Concurrency contract:
 *  - at most one session exists at a time; [start] returns [StartResult.Busy]
 *    rather than overlapping a previous session that has not finished joining;
 *  - [stop] unblocks the writer with `flush()` and joins the worker, so
 *    resources are released before `stop()` returns;
 *  - one render buffer is allocated per engine and reused for every session;
 *  - `onFinished` is invoked on the audio worker thread, never on the caller's;
 *  - a session that reaches its end is DRAINED, not flushed: the queued tail,
 *    which contains the kernel's 25 ms fade-out, is allowed to play before the
 *    track is released;
 *  - [stop] mutes the track and lets the mixer ramp the gain to zero before
 *    pausing, so an interrupted tone fades instead of being cut mid-cycle.
 *
 * This class does not choose the audio route. Deciding whether the current
 * route is acceptable is the caller's job (see [AudioRouteMonitor]).
 */
class SoundEngine {

    private val lock = Any()
    private val running = AtomicBoolean(false)

    @Volatile private var worker: Thread? = null
    @Volatile private var track: AudioTrack? = null
    @Volatile private var renderBuffer: ShortArray? = null

    private var nextToken: Long = 0L
    private var activeToken: Long = 0L

    /** True while a session is actively writing samples. */
    val isPlaying: Boolean
        get() = running.get() && worker?.isAlive == true

    fun start(config: ToneConfig, onFinished: (FinishReason, Long) -> Unit): StartResult =
        synchronized(lock) {
            // Never overlap sessions: if the old worker will not die, refuse.
            if (!shutdownLocked()) {
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
            running.set(true)

            // Start the track here, while holding the same lock that teardown takes.
            // Doing it on the worker raced with an immediate stop(), which paused and
            // flushed the track first and made play() log "start(): ... mState = 4".
            val started = runCatching { audio.play() }.isSuccess
            if (!started) {
                running.set(false)
                track = null
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

    /** Signals the worker and waits for it to release the AudioTrack. */
    fun stop() {
        synchronized(lock) { shutdownLocked() }
    }

    /**
     * Must be called with [lock] held by a single owner. Returns true when no
     * worker is alive afterwards.
     */
    private fun shutdownLocked(): Boolean {
        running.set(false)

        val audio = track
        if (audio != null) {
            // Ramp to silence first. Pausing a sine at an arbitrary phase is an
            // abrupt step in the waveform, which is audible as a click.
            muteWithRamp(audio)
            // pause() stops consumption and flush() discards queued frames, which
            // is what unblocks a writer parked in write(..., WRITE_BLOCKING).
            runCatching { if (audio.playState == AudioTrack.PLAYSTATE_PLAYING) audio.pause() }
            runCatching { audio.flush() }
        }

        val current = worker
        if (current != null && current !== Thread.currentThread() && current.isAlive) {
            runCatching { current.join(JOIN_TIMEOUT_MS) }
                .onFailure { Thread.currentThread().interrupt() }
        }

        val alive = worker?.isAlive == true
        if (!alive) {
            // Worker already released everything; clear any residue defensively.
            track = null
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
            // AudioTrack.play() already ran under the engine lock in start(), so no
            // teardown can have paused this track between play() and the first write.

            session@ while (running.get() && rendered < config.totalFrames) {
                val frames = min(CHUNK_FRAMES.toLong(), config.totalFrames - rendered).toInt()
                phase = ToneKernel.renderInto(config, rendered, frames, phase, buffer)

                var offset = 0
                while (offset < frames) {
                    if (!running.get()) break@session
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

            reason = if (rendered >= config.totalFrames) FinishReason.COMPLETED else reason
        } catch (error: Exception) {
            // Output devices disappear mid-session (unplug, Bluetooth drop, call).
            Log.w(TAG, "session ended with error", error)
            reason = FinishReason.ERROR
        } finally {
            // A natural completion must play out what is still queued. flush() here
            // used to discard up to a full buffer (~40 ms), which contains the
            // fade-out, so every session ended with a hard cut. Stops and errors
            // still tear down immediately.
            val drain = reason == FinishReason.COMPLETED && running.get()
            if (drain) {
                runCatching { awaitDrain(audio, config, rendered) }
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
            if (worker === Thread.currentThread()) worker = null
            if (activeToken == token) activeToken = 0L
            runCatching { onFinished(reason, token) }
        }
    }

    /**
     * Drops the track gain to zero and waits long enough for the mixer to ramp
     * there. AudioFlinger smooths gain changes across a mix cycle, so this turns
     * a hard cut into a short fade. If a device does not ramp, the result is no
     * worse than the previous immediate pause.
     */
    private fun muteWithRamp(audio: AudioTrack) {
        try {
            if (audio.playState != AudioTrack.PLAYSTATE_PLAYING) return
            audio.setVolume(0f)
            Thread.sleep(STOP_RAMP_MS)
        } catch (interrupted: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (ignored: Exception) {
            // Track already released by the worker: nothing left to fade.
        }
    }

    /**
     * Blocks until the hardware has consumed every frame written, or until a
     * deadline derived from the buffer length expires. Returns early if a stop
     * is requested, in which case [shutdownLocked] owns the teardown.
     */
    private fun awaitDrain(audio: AudioTrack, config: ToneConfig, framesWritten: Long) {
        val bufferMs = audio.bufferSizeInFrames * 1000L / config.sampleRate
        val deadline = SystemClock.elapsedRealtime() + bufferMs + DRAIN_MARGIN_MS
        while (running.get() && SystemClock.elapsedRealtime() < deadline) {
            // playbackHeadPosition is an unsigned 32-bit frame counter.
            val played = audio.playbackHeadPosition.toLong() and 0xFFFF_FFFFL
            if (played >= framesWritten) break
            Thread.sleep(DRAIN_POLL_MS)
        }
        // The head position runs slightly ahead of the speaker; cover the output latency.
        if (running.get()) Thread.sleep(DRAIN_TAIL_MS)
    }

    private companion object {
        const val TAG = "PiFaunaEngine"

        /** 1024 frames ≈ 21 ms at 48 kHz: small enough to stop promptly, large enough to be cheap. */
        const val CHUNK_FRAMES = 1024
        const val JOIN_TIMEOUT_MS = 1500L

        /** Time for the mixer to ramp a muted track to silence before pausing. */
        const val STOP_RAMP_MS = 30L

        /** Slack beyond the buffer length before giving up on a drain. */
        const val DRAIN_MARGIN_MS = 250L
        const val DRAIN_POLL_MS = 5L
        const val DRAIN_TAIL_MS = 40L
    }
}
