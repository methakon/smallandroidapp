package com.pifauna.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeFormatTest {

    @Test
    fun `clock rounds up partial seconds`() {
        assertEquals("0:00", TimeFormat.clock(0))
        assertEquals("0:01", TimeFormat.clock(1))
        assertEquals("0:01", TimeFormat.clock(1_000))
        assertEquals("1:01", TimeFormat.clock(61_000))
        assertEquals("10:00", TimeFormat.clock(600_000))
    }

    @Test
    fun `negative remaining time clamps to zero`() {
        assertEquals("0:00", TimeFormat.clock(-5_000))
    }

    @Test
    fun `frequency is grouped for readability`() {
        assertEquals("100 Hz", TimeFormat.frequency(100))
        assertEquals("16,000 Hz", TimeFormat.frequency(16_000))
    }

    @Test
    fun `band shows a single value when both ends match`() {
        assertEquals("500 Hz", TimeFormat.band(500, 500))
        assertEquals("1,000–5,000 Hz", TimeFormat.band(1_000, 5_000))
    }
}
