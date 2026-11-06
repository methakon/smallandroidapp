package com.pifauna.app.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerStateTest {

    @Test
    fun `default state is idle and not playing`() {
        val state = PlayerState()
        assertEquals(PlaybackPhase.Idle, state.phase)
        assertFalse(state.isPlaying)
        assertEquals(0L, state.remainingMs(1000L))
    }

    @Test
    fun `remainingMs counts down accurately against monotonic clock`() {
        val startElapsed = 10_000L
        val durationMinutes = 10
        val state = PlayerState(
            phase = PlaybackPhase.Playing,
            startedAtElapsed = startElapsed,
            durationMinutes = durationMinutes
        )

        assertTrue(state.isPlaying)
        assertEquals(600_000L, state.durationMs)

        // At start
        assertEquals(600_000L, state.remainingMs(startElapsed))

        // 1 minute in
        assertEquals(540_000L, state.remainingMs(startElapsed + 60_000L))

        // Exactly at end
        assertEquals(0L, state.remainingMs(startElapsed + 600_000L))

        // Past end (must clamp to 0)
        assertEquals(0L, state.remainingMs(startElapsed + 650_000L))
    }

    @Test
    fun `remainingMs returns zero when not playing even if startedAtElapsed is set`() {
        val state = PlayerState(
            phase = PlaybackPhase.Completed,
            startedAtElapsed = 10_000L,
            durationMinutes = 10
        )
        assertFalse(state.isPlaying)
        assertEquals(0L, state.remainingMs(20_000L))

        val idleState = state.copy(phase = PlaybackPhase.Idle)
        assertEquals(0L, idleState.remainingMs(20_000L))

        val refusedState = state.copy(phase = PlaybackPhase.Refused)
        assertEquals(0L, refusedState.remainingMs(20_000L))
    }

    @Test
    fun `supported session durations calculate accurate millisecond limits`() {
        val durations = listOf(5, 10, 15, 30)
        val expectedMs = listOf(300_000L, 600_000L, 900_000L, 1_800_000L)

        for (i in durations.indices) {
            val state = PlayerState(durationMinutes = durations[i])
            assertEquals(expectedMs[i], state.durationMs)
        }
    }
}
