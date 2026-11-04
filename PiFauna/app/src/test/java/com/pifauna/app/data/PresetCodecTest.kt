package com.pifauna.app.data

import com.pifauna.app.audio.ToneMode
import com.pifauna.app.safety.SafetyLimits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PresetCodecTest {

    private fun preset(name: String = "Evening", gain: Float = 0.05f) = Preset(
        name = name,
        startHz = 1_000,
        endHz = 4_000,
        mode = ToneMode.SWEEP,
        gain = gain
    )

    @Test
    fun `round trips through the stored format`() {
        val original = listOf(preset("Evening"), preset("Moths").copy(mode = ToneMode.PULSE))
        val decoded = PresetCodec.decode(PresetCodec.encode(original))
        assertEquals(original, decoded)
    }

    @Test
    fun `empty input decodes to an empty list`() {
        assertTrue(PresetCodec.decode(null).isEmpty())
        assertTrue(PresetCodec.decode("").isEmpty())
        assertTrue(PresetCodec.decode("garbage").isEmpty())
    }

    @Test
    fun `add rejects duplicate names regardless of case`() {
        val existing = listOf(preset("Evening"))
        val error = assertThrows(PresetRejected::class.java) {
            PresetCodec.add(existing, preset("evening"))
        }
        assertTrue(error.message!!.contains("already exists"))
    }

    @Test
    fun `add enforces the storage cap`() {
        val full = (1..SafetyLimits.MAX_PRESETS).map { preset("Preset $it") }
        val error = assertThrows(PresetRejected::class.java) {
            PresetCodec.add(full, preset("One more"))
        }
        assertTrue(error.message!!.contains("${SafetyLimits.MAX_PRESETS}"))
    }

    @Test
    fun `add rejects unusable names`() {
        assertThrows(PresetRejected::class.java) { PresetCodec.add(emptyList(), preset("   ")) }
        assertThrows(PresetRejected::class.java) { PresetCodec.add(emptyList(), preset("")) }
        assertThrows(PresetRejected::class.java) {
            PresetCodec.add(emptyList(), preset("a".repeat(PresetCodec.MAX_NAME_LENGTH + 1)))
        }
        assertThrows(PresetRejected::class.java) {
            PresetCodec.add(emptyList(), preset("bad\u001Fname"))
        }
    }

    @Test
    fun `add rejects out-of-range frequencies and gain`() {
        assertThrows(PresetRejected::class.java) {
            PresetCodec.add(emptyList(), preset().copy(startHz = SafetyLimits.MAX_HZ + 1))
        }
        assertThrows(PresetRejected::class.java) {
            PresetCodec.add(emptyList(), preset().copy(startHz = 5_000, endHz = 1_000))
        }
        assertThrows(PresetRejected::class.java) {
            PresetCodec.add(emptyList(), preset(gain = SafetyLimits.MAX_GAIN + 0.01f))
        }
    }

    @Test
    fun `add trims the accepted name`() {
        val result = PresetCodec.add(emptyList(), preset("  Evening  "))
        assertEquals("Evening", result.single().name)
    }

    @Test
    fun `decode drops records that would widen a safety limit`() {
        val encoded = PresetCodec.encode(listOf(preset(gain = 0.05f)))
        val corrupted = encoded.replace("0.05", "0.9")
        assertTrue(PresetCodec.decode(corrupted).isEmpty())
    }
}
