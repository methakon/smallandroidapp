package com.pifauna.app.safety

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SafetyPolicyTest {

    private fun snapshot(
        battery: Int = 80,
        thermal: Int = 0,
        route: String? = null,
        volume: Float = 0.3f
    ) = SafetySnapshot(battery, thermal, route, volume)

    @Test
    fun `comfortable device is allowed`() {
        assertEquals(SafetyDecision.Allowed, SafetyPolicy.evaluate(snapshot()))
    }

    @Test
    fun `battery at or below the cutoff blocks playback`() {
        val atCutoff = SafetyPolicy.evaluate(snapshot(battery = SafetyLimits.BATTERY_CUTOFF_PERCENT))
        assertTrue(atCutoff is SafetyDecision.Blocked)
        assertEquals(BlockCode.LOW_BATTERY, (atCutoff as SafetyDecision.Blocked).code)

        val below = SafetyPolicy.evaluate(snapshot(battery = 4))
        assertEquals(BlockCode.LOW_BATTERY, (below as SafetyDecision.Blocked).code)

        val above = SafetyPolicy.evaluate(snapshot(battery = SafetyLimits.BATTERY_CUTOFF_PERCENT + 1))
        assertEquals(SafetyDecision.Allowed, above)
    }

    @Test
    fun `severe thermal status blocks playback`() {
        val severe = SafetyPolicy.evaluate(snapshot(thermal = SafetyLimits.THERMAL_BLOCK_STATUS))
        assertEquals(BlockCode.THERMAL, (severe as SafetyDecision.Blocked).code)

        val hot = SafetyPolicy.evaluate(snapshot(thermal = 5))
        assertEquals(BlockCode.THERMAL, (hot as SafetyDecision.Blocked).code)

        val warm = SafetyPolicy.evaluate(snapshot(thermal = SafetyLimits.THERMAL_BLOCK_STATUS - 1))
        assertEquals(SafetyDecision.Allowed, warm)
    }

    @Test
    fun `external audio routes are refused`() {
        val blocked = SafetyPolicy.evaluate(snapshot(route = "Wired headphones"))
        assertEquals(BlockCode.EXTERNAL_ROUTE, (blocked as SafetyDecision.Blocked).code)
        assertTrue(blocked.message.contains("Wired headphones"))

        assertEquals(SafetyDecision.Allowed, SafetyPolicy.evaluate(snapshot(route = null)))
        assertEquals(SafetyDecision.Allowed, SafetyPolicy.evaluate(snapshot(route = "  ")))
    }

    @Test
    fun `media volume above the limit is refused`() {
        val loud = SafetyPolicy.evaluate(
            snapshot(volume = SafetyLimits.MEDIA_VOLUME_RATIO_LIMIT + 0.01f)
        )
        assertEquals(BlockCode.VOLUME_TOO_HIGH, (loud as SafetyDecision.Blocked).code)

        val atLimit = SafetyPolicy.evaluate(snapshot(volume = SafetyLimits.MEDIA_VOLUME_RATIO_LIMIT))
        assertEquals(SafetyDecision.Allowed, atLimit)
    }

    @Test
    fun `most serious problem is reported first`() {
        val everything = SafetyPolicy.evaluate(
            snapshot(battery = 5, thermal = 6, route = "USB audio", volume = 1f)
        )
        assertEquals(BlockCode.LOW_BATTERY, (everything as SafetyDecision.Blocked).code)
    }

    @Test
    fun `mustStop only fires for blocking conditions`() {
        assertNull(SafetyPolicy.mustStop(snapshot()))
        assertEquals(
            BlockCode.LOW_BATTERY,
            SafetyPolicy.mustStop(snapshot(battery = 10))
        )
        assertEquals(
            BlockCode.THERMAL,
            SafetyPolicy.mustStop(snapshot(thermal = 4))
        )
        assertEquals(
            BlockCode.EXTERNAL_ROUTE,
            SafetyPolicy.mustStop(snapshot(route = "Bluetooth audio device"))
        )
    }

    @Test
    fun `connected-but-unrouted Bluetooth allows playback`() {
        // When route is null (unrouted Bluetooth / watch), snapshot allows playback
        val unroutedSnapshot = snapshot(battery = 80, thermal = 0, route = null, volume = 0.3f)
        assertEquals(SafetyDecision.Allowed, SafetyPolicy.evaluate(unroutedSnapshot))
        assertNull(SafetyPolicy.mustStop(unroutedSnapshot))
    }

    @Test
    fun `active external routes block playback in preflight and mustStop`() {
        val routes = listOf(
            "Wired headphones",
            "A wired headset",
            "A Bluetooth audio device",
            "A USB audio device",
            "A hearing aid",
            "An HDMI output",
            "A line output"
        )
        for (route in routes) {
            val blockedSnapshot = snapshot(route = route)
            val decision = SafetyPolicy.evaluate(blockedSnapshot)
            assertTrue(decision is SafetyDecision.Blocked)
            assertEquals(BlockCode.EXTERNAL_ROUTE, (decision as SafetyDecision.Blocked).code)
            assertEquals(BlockCode.EXTERNAL_ROUTE, SafetyPolicy.mustStop(blockedSnapshot))
        }
    }

    @Test
    fun `route transition from speaker to external route triggers mustStop`() {
        // Initially on built-in speaker
        val initialSession = snapshot(route = null)
        assertNull(SafetyPolicy.mustStop(initialSession))

        // Headset connected during active session
        val transitionedSession = initialSession.copy(externalAudioRoute = "Wired headphones")
        assertEquals(BlockCode.EXTERNAL_ROUTE, SafetyPolicy.mustStop(transitionedSession))

        // Headset removed -> back to safe state
        val restoredSession = transitionedSession.copy(externalAudioRoute = null)
        assertNull(SafetyPolicy.mustStop(restoredSession))
    }
}
