package com.pifauna.app.data

import com.pifauna.app.safety.SafetyLimits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChakraProfilesTest {
    @Test fun sevenPresetsHaveSafeDistinctAudibleTones() {
        assertEquals(7, ChakraProfiles.all.size)
        assertEquals(7, ChakraProfiles.all.map { it.id }.toSet().size)
        assertEquals(7, ChakraProfiles.all.map { it.toneHz }.toSet().size)
        assertTrue(ChakraProfiles.all.all { it.toneHz in SafetyLimits.MIN_HZ..SafetyLimits.MAX_HZ })
        assertEquals(639, ChakraProfiles.byId("anahata").toneHz)
    }

    @Test fun descriptiveContentDoesNotClaimProvenChakraActivation() {
        assertTrue(ChakraProfiles.evidenceNote.contains("No frequency has been shown"))
        assertTrue(ChakraProfiles.all.all { it.listeningPractice.isNotBlank() })
    }
}
