package com.methakon.pifauna

import kotlin.math.abs

fun main() {
    val fixed = ToneConfig(1_000.0, 4_000.0, ToneMode.FIXED, 0.10, durationSeconds = 5)
    val block = ToneKernel.render(fixed, 0, 48_000, 0.0)
    check(block.samples.size == 48_000)
    check(block.samples.maxOf { abs(it.toInt()) } <= (0.10 * Short.MAX_VALUE).toInt())
    val center = block.samples.slice(2_400 until 45_600)
    val crossings = center.zipWithNext().count { (a, b) -> a < 0 && b >= 0 }
    check(crossings in 895..905) { "Expected 900 Hz cycles over 0.9s, found $crossings" }

    val pulse = ToneConfig(500.0, 500.0, ToneMode.PULSE, 0.06, durationSeconds = 5)
    val p = ToneKernel.render(pulse, 0, 48_000, 0.0).samples
    check(p.slice(14_400 until 20_000).all { it == 0.toShort() }) { "Off-period not silent" }
    check(p.slice(3_000 until 8_000).any { it != 0.toShort() }) { "On-period silent" }

    val sweep = ToneConfig(500.0, 4_000.0, ToneMode.SWEEP, 0.03, durationSeconds = 5)
    check(ToneKernel.render(sweep, 0, 480, 0.0).samples.any { it != 0.toShort() })
    check(runCatching { ToneConfig(30_000.0, 40_000.0, ToneMode.FIXED, 0.1) }.isFailure)
    check(runCatching { ToneConfig(1_000.0, 4_000.0, ToneMode.FIXED, 0.5) }.isFailure)
    val random = ToneConfig(300.0, 3_600.0, ToneMode.RANDOM_HOP, 0.06, durationSeconds = 5)
    val full = ToneKernel.render(random, 0, 48_000, 0.0)
    check(full.samples.any { it != 0.toShort() })
    check(full.samples[24_000] == 0.toShort()) { "Hop should fade to zero at the 500ms boundary" }
    check(full.samples.maxOf { abs(it.toInt()) } <= (0.06 * Short.MAX_VALUE).toInt())
    // Block boundaries must not affect the synthesized output.
    val combined = ShortArray(48_000)
    var next = 0L
    var phase = 0.0
    while (next < 48_000) {
        val chunk = ToneKernel.render(random, next, 480, phase)
        chunk.samples.copyInto(combined, next.toInt())
        phase = chunk.endingPhase
        next += 480
    }
    check(full.samples.contentEquals(combined)) { "Random stepping depends on buffer size" }
    println("PASS: fixed tone / amplitude / pulses / sweeps / randomized hops / validation / block-continuity")
}