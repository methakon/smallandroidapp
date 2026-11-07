package com.pifauna.app.mantra

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MantraRepetitionPolicyTest {
    @Test fun supportedCountsAndGaps() {
        assertEquals(listOf(11, 21, 58, 108), MantraRepetitionPolicy.counts)
        assertEquals(listOf(500L, 1000L, 2000L, 3000L), MantraRepetitionPolicy.gapsMs)
        for (count in MantraRepetitionPolicy.counts) {
            for (gap in MantraRepetitionPolicy.gapsMs) {
                assertTrue(MantraRepetitionPolicy.permitted(count, gap))
            }
        }
    }

    @Test fun refusesInfiniteOrInvalidCounts() {
        for (count in listOf(-1, 0, 1, 10, 22, 109, Int.MAX_VALUE)) {
            assertFalse(MantraRepetitionPolicy.permitted(count, 1000L))
        }
        assertFalse(MantraRepetitionPolicy.permitted(108, 0L))
        assertFalse(MantraRepetitionPolicy.permitted(108, 8000L))
    }

    @Test fun estimatedDurationIncludesOnlyInterRepeatGaps() {
        assertEquals(11_000L + 10_000L,
            MantraRepetitionPolicy.estimatedMs(1000L, 11, 1000L))
        assertEquals(108_000L + 107 * 500L,
            MantraRepetitionPolicy.estimatedMs(1000L, 108, 500L))
        assertEquals(30L * 60 * 1000, MantraRepetitionPolicy.MAX_SESSION_MS)
    }
}
