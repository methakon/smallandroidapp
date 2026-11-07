package com.pifauna.app.mantra

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MantraRepetitionPolicyTest {
    @Test fun allSetChoicesAreMultiplesOf108() {
        assertEquals(listOf(1, 11, 21, 58, 108), MantraRepetitionPolicy.setChoices)
        assertEquals(listOf(108, 1188, 2268, 6264, 11664),
            MantraRepetitionPolicy.setChoices.map { MantraRepetitionPolicy.totalRepetitions(it) })
        assertEquals(listOf(500L, 1000L, 2000L, 3000L),
            MantraRepetitionPolicy.gapsMs)
    }

    @Test fun acceptsOnlySupportedSetsAndGaps() {
        for (sets in MantraRepetitionPolicy.setChoices)
            for (gap in MantraRepetitionPolicy.gapsMs)
                assertTrue(MantraRepetitionPolicy.permitted(sets, gap))
        for (invalid in listOf(-1, 0, 2, 10, 22, 109, Int.MAX_VALUE))
            assertFalse(MantraRepetitionPolicy.permitted(invalid, 1000L))
        assertFalse(MantraRepetitionPolicy.permitted(108, 0L))
    }

    @Test fun estimatedTimeUsesAllRepetitionsAndOnlyInterChantGaps() {
        assertEquals(108_000L + 107_000L,
            MantraRepetitionPolicy.estimatedMs(1000L, 1, 1000L))
        assertEquals(1_188_000L + 1_187 * 500L,
            MantraRepetitionPolicy.estimatedMs(1000L, 11, 500L))
        assertEquals(30 * 60 * 1000L, MantraRepetitionPolicy.MAX_SESSION_MS)
    }

    @Test fun progressRespectsSetBoundaries() {
        assertEquals(MantraProgress(1, 0, 0, 1188), MantraRepetitionPolicy.progress(0, 11))
        assertEquals(MantraProgress(2, 0, 108, 1188), MantraRepetitionPolicy.progress(108, 11))
        assertEquals(MantraProgress(3, 67, 283, 1188), MantraRepetitionPolicy.progress(283, 11))
        assertEquals(MantraProgress(11, 108, 1188, 1188),
            MantraRepetitionPolicy.progress(1188, 11))
    }

    @Test fun longSessionsMustNotSilentlyClaimCompletion() {
        assertTrue(MantraRepetitionPolicy.canFinishInOneSession(1000L, 1, 1000L))
        assertFalse(MantraRepetitionPolicy.canFinishInOneSession(6000L, 11, 1000L))
    }
}
