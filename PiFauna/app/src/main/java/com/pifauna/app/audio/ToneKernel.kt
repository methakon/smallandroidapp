package com.pifauna.app.audio

import com.pifauna.app.safety.SafetyLimits
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

/** Signal shape. Every mode is a pure function of absolute frame index. */
enum class ToneMode {
    /** Constant frequency. */
    FIXED,

    /** Triangle sweep between start and end, 2.5 s per full cycle. */
    SWEEP,

    /** 250 ms audible / 750 ms silent, 10 ms transitions. */
    PULSE,

    /** Deterministic pseudorandom frequency every 500 ms, 12 ms edge fades. */
    HOP
}

/**
 * Validated synthesis request.
 *
 * Constructing an invalid configuration throws; the UI must validate before it
 * reaches the engine, so a thrown exception here is a programming error, not a
 * user-facing failure path.
 */
data class ToneConfig(
    val startHz: Int,
    val endHz: Int,
    val mode: ToneMode,
    val gain: Float,
    val durationMs: Long,
    val sampleRate: Int = SafetyLimits.SAMPLE_RATE
) {
    init {
        require(startHz in SafetyLimits.MIN_HZ..SafetyLimits.MAX_HZ) {
            "startHz $startHz outside ${SafetyLimits.MIN_HZ}..${SafetyLimits.MAX_HZ}"
        }
        require(endHz in SafetyLimits.MIN_HZ..SafetyLimits.MAX_HZ) {
            "endHz $endHz outside ${SafetyLimits.MIN_HZ}..${SafetyLimits.MAX_HZ}"
        }
        require(gain > 0f && gain <= SafetyLimits.MAX_GAIN) {
            "gain $gain outside 0..${SafetyLimits.MAX_GAIN}"
        }
        require(sampleRate >= 44_100) { "sampleRate $sampleRate too low" }
        require(maxOf(startHz, endHz) < sampleRate * 0.45) {
            "frequencies must stay below 0.45 * sampleRate"
        }
        require(durationMs in 1L..SafetyLimits.MAX_DURATION_MS) {
            "durationMs $durationMs must be positive and at most ${SafetyLimits.MAX_DURATION_MS}"
        }
    }

    /** Total samples for the whole session. */
    val totalFrames: Long = durationMs * sampleRate / 1000L

    /** Peak digital amplitude in 16-bit units. */
    val peakAmplitude: Double get() = gain.toDouble() * Short.MAX_VALUE
}

/**
 * Android-free PCM synthesiser.
 *
 * Guarantees:
 *  - output is bounded by [ToneConfig.peakAmplitude] (never reaches full scale);
 *  - 25 ms fade-in and fade-out, plus per-transition fades, so no clicks;
 *  - the rendered samples do not depend on how the caller chunks the stream,
 *    because every value is derived from the absolute frame index.
 *
 * It does not model speaker response. Rendering a 16 kHz sine proves a digital
 * signal exists; it proves nothing about radiated sound or animal behaviour.
 */
object ToneKernel {

    private const val TWO_PI = 2.0 * PI
    private const val FADE_MS = 25.0
    private const val SWEEP_PERIOD_S = 2.5
    private const val PULSE_ON_S = 0.25
    private const val PULSE_TRANSITION_S = 0.010
    private const val HOP_STEP_S = 0.5
    private const val HOP_FADE_S = 0.012

    /**
     * Fills [frames] samples starting at [outOffset] and returns the phase to
     * pass into the next call. Samples past the end of the session are zero,
     * so callers may keep a fixed buffer size.
     */
    fun renderInto(
        config: ToneConfig,
        frameStart: Long,
        frames: Int,
        initialPhase: Double,
        out: ShortArray,
        outOffset: Int = 0
    ): Double {
        require(frames > 0) { "frames must be positive" }
        require(frameStart >= 0) { "frameStart must be non-negative" }
        require(outOffset >= 0 && outOffset + frames <= out.size) {
            "buffer too small: need ${outOffset + frames}, have ${out.size}"
        }

        val rate = config.sampleRate.toDouble()
        val total = config.totalFrames
        val fadeFrames = maxOf(1L, (rate * FADE_MS / 1000.0).toLong())
        val peak = config.peakAmplitude
        val lowHz = minOf(config.startHz, config.endHz).toDouble()
        val highHz = maxOf(config.startHz, config.endHz).toDouble()
        val span = highHz - lowHz

        var phase = initialPhase
        var written = 0

        while (written < frames) {
            val frame = frameStart + written
            if (frame >= total) {
                // Remaining samples of this buffer belong to no session.
                var pad = written
                while (pad < frames) {
                    out[outOffset + pad] = 0
                    pad++
                }
                return phase
            }

            val second = frame / rate
            val frequency = when (config.mode) {
                ToneMode.FIXED, ToneMode.PULSE -> config.startHz.toDouble()
                ToneMode.SWEEP -> {
                    val cycle = (second / SWEEP_PERIOD_S) % 1.0
                    val triangle = if (cycle < 0.5) cycle * 2.0 else (1.0 - cycle) * 2.0
                    lowHz + span * if (config.startHz <= config.endHz) triangle else (1.0 - triangle)
                }
                ToneMode.HOP -> {
                    // Reproducible per 500 ms step: same tick always yields the same
                    // frequency, independent of buffer partitioning.
                    val step = (second / HOP_STEP_S).toLong()
                    val hash = (step * 1_103_515_245L + 12_345L) and 0x7fff_ffffL
                    val fraction = hash.toDouble() / 0x7fff_ffffL.toDouble()
                    lowHz + span * fraction
                }
            }

            val sample = sin(phase) * envelopeAt(config, frame, total, fadeFrames, second) * peak

            phase += TWO_PI * frequency / rate
            if (phase >= TWO_PI) phase -= TWO_PI

            out[outOffset + written] = Math.round(sample).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
            written++
        }

        return phase
    }

    /** Convenience wrapper for tests and one-shot renders. */
    fun render(config: ToneConfig, frameStart: Long, frames: Int, initialPhase: Double = 0.0): ShortArray {
        val out = ShortArray(frames)
        renderInto(config, frameStart, frames, initialPhase, out)
        return out
    }

    private fun envelopeAt(
        config: ToneConfig,
        frame: Long,
        total: Long,
        fadeFrames: Long,
        second: Double
    ): Double {
        val fadeIn = min(1.0, frame.toDouble() / fadeFrames)
        val fadeOut = min(1.0, (total - 1 - frame).toDouble() / fadeFrames)
        var envelope = min(fadeIn, fadeOut)

        when (config.mode) {
            ToneMode.PULSE -> {
                val pulsePhase = second % 1.0
                val shape = when {
                    pulsePhase >= PULSE_ON_S -> 0.0
                    pulsePhase < PULSE_TRANSITION_S -> pulsePhase / PULSE_TRANSITION_S
                    pulsePhase > PULSE_ON_S - PULSE_TRANSITION_S ->
                        (PULSE_ON_S - pulsePhase) / PULSE_TRANSITION_S
                    else -> 1.0
                }
                envelope = min(envelope, shape)
            }
            ToneMode.HOP -> {
                val stepPhase = second % HOP_STEP_S
                val edge = min(stepPhase, HOP_STEP_S - stepPhase) / HOP_FADE_S
                envelope = min(envelope, edge.coerceIn(0.0, 1.0))
            }
            ToneMode.FIXED, ToneMode.SWEEP -> Unit
        }

        return envelope.coerceIn(0.0, 1.0)
    }
}
