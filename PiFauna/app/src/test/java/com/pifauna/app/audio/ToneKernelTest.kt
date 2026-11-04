package com.pifauna.app.audio

import com.pifauna.app.safety.SafetyLimits
import kotlin.math.abs
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Signal-level tests. These prove what the synthesiser emits; they prove nothing
 * about radiated sound or any effect on animals.
 */
class ToneKernelTest {

    private fun config(
        start: Int,
        end: Int = start,
        mode: ToneMode = ToneMode.FIXED,
        gain: Float = SafetyLimits.MAX_GAIN,
        seconds: Int = 5
    ) = ToneConfig(
        startHz = start,
        endHz = end,
        mode = mode,
        gain = gain,
        durationMs = seconds * 1000L
    )

    @Test
    fun `every mode stays inside the digital ceiling`() {
        val modes = ToneMode.entries
        for (mode in modes) {
            val rendered = ToneKernel.render(
                config(start = 1_000, end = 16_000, mode = mode), 0, 48_000
            )
            val peak = rendered.maxOf { abs(it.toInt()) }
            assertTrue(
                "$mode exceeded the ${(SafetyLimits.MAX_GAIN * 32767).toInt()} ceiling: $peak",
                peak <= (SafetyLimits.MAX_GAIN * Short.MAX_VALUE).toInt()
            )
        }
    }

    @Test
    fun `fixed tone produces the requested frequency`() {
        val rendered = ToneKernel.render(config(start = 1_000, gain = 0.08f), 0, 48_000)
        // Measure away from the 25 ms fades so rounding cannot hide a crossing.
        val window = rendered.copyOfRange(4_800, 43_200) // 0.8 s
        val crossings = (1 until window.size).count { window[it - 1] < 0 && window[it] >= 0 }
        assertTrue("expected ~800 cycles over 0.8 s, measured $crossings", crossings in 797..803)
    }

    @Test
    fun `rendering does not depend on how the stream is chunked`() {
        val cfg = config(start = 300, end = 3_600, mode = ToneMode.HOP, gain = 0.06f)
        val single = ToneKernel.render(cfg, 0, 48_000)

        val chunked = ShortArray(48_000)
        var frame = 0L
        var phase = 0.0
        while (frame < 48_000) {
            val frames = minOf(480L, 48_000 - frame).toInt()
            phase = ToneKernel.renderInto(cfg, frame, frames, phase, chunked, frame.toInt())
            frame += frames
        }

        assertArrayEquals(single, chunked)
    }

    @Test
    fun `session fades in and out instead of clicking`() {
        val cfg = config(start = 1_000, gain = 0.08f, seconds = 1)
        val rendered = ToneKernel.render(cfg, 0, cfg.totalFrames.toInt())

        assertEquals("first sample must start at silence", 0, rendered.first().toInt())
        val fadeFrames = 1_200 // 25 ms at 48 kHz
        // Compare peak levels rather than single samples, which can land on a zero crossing.
        val earlyPeak = rendered.copyOfRange(0, 240).maxOf { abs(it.toInt()) } // first 5 ms
        val settledPeak = rendered.copyOfRange(fadeFrames, fadeFrames + 1_200)
            .maxOf { abs(it.toInt()) }
        assertTrue(
            "fade-in should ramp: $earlyPeak in the first 5 ms vs $settledPeak after 25 ms",
            settledPeak > earlyPeak * 2
        )
        assertEquals("last sample must end at silence", 0, rendered.last().toInt())
    }

    @Test
    fun `pulse mode is silent outside the on window`() {
        val cfg = config(start = 500, mode = ToneMode.PULSE, gain = 0.06f)
        val rendered = ToneKernel.render(cfg, 0, 48_000)

        assertTrue("on window should contain signal", rendered.sliceArray(3_000..8_000).any { it != 0.toShort() })
        assertTrue(
            "off window must be silent",
            rendered.sliceArray(14_400..23_999).all { it == 0.toShort() }
        )
    }

    @Test
    fun `hop mode fades to silence on every step boundary`() {
        val cfg = config(start = 300, end = 3_600, mode = ToneMode.HOP, gain = 0.06f)
        val rendered = ToneKernel.render(cfg, 0, 48_000)
        assertEquals("0.5 s boundary must be faded out", 0, rendered[24_000].toInt())
        assertTrue(rendered.any { it != 0.toShort() })
    }

    @Test
    fun `samples past the end of the session are silent`() {
        val cfg = config(start = 1_000, seconds = 1)
        val rendered = ToneKernel.render(cfg, cfg.totalFrames - 100, 1_000)
        assertTrue(
            "the tail beyond totalFrames must be zero",
            rendered.sliceArray(100 until 1_000).all { it == 0.toShort() }
        )
    }

    @Test
    fun `invalid configurations are rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            config(start = SafetyLimits.MIN_HZ - 1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            config(start = SafetyLimits.MAX_HZ + 1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            config(start = 1_000, gain = SafetyLimits.MAX_GAIN + 0.05f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ToneConfig(1_000, 1_000, ToneMode.FIXED, 0.05f, durationMs = 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ToneConfig(
                1_000, 1_000, ToneMode.FIXED, 0.05f,
                durationMs = SafetyLimits.MAX_DURATION_MS + 1
            )
        }
    }

    @Test
    fun `peak amplitude matches the configured gain`() {
        val rendered = ToneKernel.render(config(start = 1_000, gain = 0.05f), 0, 24_000)
        val expected = (0.05f * Short.MAX_VALUE).toInt()
        val peak = rendered.maxOf { abs(it.toInt()) }
        assertTrue("peak $peak should approach $expected", peak in (expected - 2)..expected)
    }
}
