package com.methakon.pifauna

import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

/** Pure Kotlin, Android-independent synthesizer. No proof of biological repellency. */
enum class ToneMode { FIXED, SWEEP, PULSE, RANDOM_HOP }

data class ToneConfig(
    val startHz: Double,
    val endHz: Double,
    val mode: ToneMode,
    val pcmGain: Double,
    val sampleRate: Int = 48_000,
    val durationSeconds: Int = 10
) {
    init {
        require(sampleRate >= 44_100)
        require(startHz in 100.0..18_000.0)
        require(endHz in 100.0..18_000.0)
        require(maxOf(startHz, endHz) < sampleRate * 0.45)
        require(pcmGain in 0.0..0.10)
        require(durationSeconds in 1..20)
    }
}

/** Caller passes phase from previous block, preventing discontinuity/clicking. */
data class SynthBlock(val samples: ShortArray, val endingPhase: Double)

object ToneKernel {
    fun render(config: ToneConfig, frameStart: Long, frames: Int, initialPhase: Double): SynthBlock {
        require(frames > 0)
        require(frameStart >= 0)
        var phase = initialPhase
        val output = ShortArray(frames)
        val sampleRate = config.sampleRate.toDouble()
        val totalFrames = config.durationSeconds.toLong() * config.sampleRate
        val fadeFrames = (sampleRate * 0.025).toLong() // 25 ms fades

        for (i in 0 until frames) {
            val frame = frameStart + i
            if (frame >= totalFrames) break
            val second = frame / sampleRate
            val frequency = when (config.mode) {
                ToneMode.SWEEP -> {
                    // Triangle sweep, 2.5-second full cycle.
                    val cycle = (second / 2.5) % 1.0
                    val triangle = if (cycle < 0.5) cycle * 2.0 else (1.0 - cycle) * 2.0
                    config.startHz + (config.endHz - config.startHz) * triangle
                }
                ToneMode.RANDOM_HOP -> {
                    // Reproducible step-to-step variation, independent of render block size.
                    // 0.5 s steps, with a short fade around every frequency transition.
                    val step = (second / 0.5).toLong()
                    val hash = (step * 1_103_515_245L + 12_345L) and 0x7fff_ffffL
                    val fraction = hash.toDouble() / 0x7fff_ffffL.toDouble()
                    minOf(config.startHz, config.endHz) +
                        (maxOf(config.startHz, config.endHz) - minOf(config.startHz, config.endHz)) * fraction
                }
                ToneMode.FIXED, ToneMode.PULSE -> config.startHz
            }
            phase += (2.0 * PI * frequency) / sampleRate
            if (phase > 2.0 * PI) phase %= 2.0 * PI

            val envelopeStart = min(1.0, frame / fadeFrames.toDouble())
            val envelopeEnd = min(1.0, (totalFrames - 1 - frame) / fadeFrames.toDouble())
            var envelope = min(envelopeStart, envelopeEnd).coerceIn(0.0, 1.0)

            if (config.mode == ToneMode.RANDOM_HOP) {
                // A short fade on each side of the 500 ms step boundary reduces clicks.
                val stepFraction = second % 0.5
                envelope *= min(1.0, min(stepFraction, 0.5 - stepFraction) / 0.012).coerceIn(0.0, 1.0)
            }
            if (config.mode == ToneMode.PULSE) {
                // 250 ms on / 750 ms off, with 10 ms transitions.
                val pulsePhase = second % 1.0
                envelope *= when {
                    pulsePhase >= 0.25 -> 0.0
                    pulsePhase < 0.01 -> pulsePhase / 0.01
                    pulsePhase > 0.24 -> (0.25 - pulsePhase) / 0.01
                    else -> 1.0
                }
            }
            val value = sin(phase) * envelope * config.pcmGain
            output[i] = (value * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return SynthBlock(output, phase)
    }
}