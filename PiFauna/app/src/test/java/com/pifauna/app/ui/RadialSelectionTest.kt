package com.pifauna.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RadialSelectionTest {
    @Test fun selectedIndexStartsAtTwelveOClock() {
        assertEquals(0, radialIndexAt(150f, 10f, 300f, 300f, 6))
        assertEquals(3, radialIndexAt(150f, 290f, 300f, 300f, 6))
    }

    @Test fun sevenChakraPositionsAreDistinct() {
        val n = 7
        for (i in 0 until n) {
            val theta = -Math.PI / 2 + 2 * Math.PI * i / n
            val x = 150f + 120f * kotlin.math.cos(theta).toFloat()
            val y = 150f + 120f * kotlin.math.sin(theta).toFloat()
            assertEquals(i, radialIndexAt(x, y, 300f, 300f, n))
        }
    }

    @Test fun centerIsReadOnlyAndInvalidInputsRejected() {
        assertNull(radialIndexAt(150f, 150f, 300f, 300f, 6))
        assertNull(radialIndexAt(1f, 1f, 0f, 300f, 6))
        assertNull(radialIndexAt(150f, 10f, 300f, 300f, 1))
    }
}
