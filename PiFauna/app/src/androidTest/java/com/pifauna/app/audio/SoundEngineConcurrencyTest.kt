package com.pifauna.app.audio

import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Concurrency contract of [SoundEngine], exercised on a real device.
 *
 * These exist because the teardown used to be racy: `stop()` cleared the running
 * flag and then slept for a fade, which let the worker release the AudioTrack
 * during that sleep. Every case below drives the engine through a window where
 * the old code could double-release, lose the completion callback, or leave the
 * engine permanently busy.
 *
 * Sessions are short (60–400 ms) and quiet (3% of full scale) so nothing is left
 * playing; set the media volume low before running them.
 */
@RunWith(AndroidJUnit4::class)
class SoundEngineConcurrencyTest {

    private lateinit var engine: SoundEngine
    private val callbacks = AtomicInteger(0)
    private val lastReason = AtomicReference<FinishReason?>(null)
    private lateinit var finished: CountDownLatch

    private fun config(durationMs: Long) = ToneConfig(
        startHz = 500,
        endHz = 500,
        mode = ToneMode.FIXED,
        gain = 0.03f,
        durationMs = durationMs
    )

    @Before
    fun setUp() {
        engine = SoundEngine()
        callbacks.set(0)
        lastReason.set(null)
        finished = CountDownLatch(1)
    }

    private fun startSession(durationMs: Long): StartResult =
        engine.start(config(durationMs)) { reason, _ ->
            callbacks.incrementAndGet()
            lastReason.set(reason)
            finished.countDown()
        }

    private fun awaitFinish(seconds: Long = 5): Boolean =
        finished.await(seconds, TimeUnit.SECONDS)

    /** Stop while the worker is still writing. */
    @Test
    fun stopDuringWritingReportsStoppedAndReleases() {
        assertTrue(startSession(2_000) is StartResult.Started)
        Thread.sleep(120)

        engine.stop()

        assertTrue("finished callback never arrived", awaitFinish())
        assertEquals(FinishReason.STOPPED, lastReason.get())
        assertFalse("engine still reports playing after stop()", engine.isPlaying)
        assertEquals("callback must fire exactly once", 1, callbacks.get())
    }

    /**
     * Stop while the worker is in the drain window: a very short tone is written
     * in one chunk, so the worker spends its time waiting for the hardware to
     * consume the tail. A stop here must be reported as STOPPED, not COMPLETED —
     * the tail was cut, so the session did not finish cleanly.
     */
    @Test
    fun stopDuringDrainIsReportedAsStopped() {
        assertTrue(startSession(60) is StartResult.Started)
        Thread.sleep(5)

        engine.stop()

        assertTrue("finished callback never arrived", awaitFinish())
        assertEquals(FinishReason.STOPPED, lastReason.get())
        assertFalse(engine.isPlaying)
    }

    /** A session left alone must complete and report COMPLETED. */
    @Test
    fun naturalCompletionReportsCompleted() {
        assertTrue(startSession(250) is StartResult.Started)

        assertTrue("finished callback never arrived", awaitFinish())
        assertEquals(FinishReason.COMPLETED, lastReason.get())
        assertFalse(engine.isPlaying)
        assertEquals(1, callbacks.get())
    }

    /** Completion and stop arriving together must not double-release or double-report. */
    @Test
    fun nearSimultaneousCompletionAndStop() {
        assertTrue(startSession(150) is StartResult.Started)
        Thread.sleep(150)

        engine.stop()

        assertTrue(awaitFinish())
        // Either thread may legitimately win the race; what must not happen is a
        // second callback, a crash, or a stuck engine.
        Thread.sleep(60)
        assertEquals("callback must fire exactly once", 1, callbacks.get())
        assertFalse(engine.isPlaying)
        assertNotEquals(null, lastReason.get())
    }

    /** Rapid start/stop cycles must always leave the engine restartable. */
    @Test
    fun rapidRestartIsNeverStuckBusy() {
        repeat(10) { round ->
            val result = startSession(400)
            assertTrue("round $round refused to start: $result", result is StartResult.Started)
            engine.stop()
            assertFalse("round $round left the engine playing", engine.isPlaying)

            // A finished session must release the track before the next start,
            // otherwise the caller would see Busy for seconds.
            val next = startSession(120)
            assertTrue("round $round could not restart: $next", next is StartResult.Started)
            engine.stop()
        }
        assertTrue("callbacks were lost across restarts", callbacks.get() >= 10)
    }

    /** stopImmediately must not block on a fade and must still release. */
    @Test
    fun immediateStopReleasesWithoutWaiting() {
        assertTrue(startSession(2_000) is StartResult.Started)
        Thread.sleep(80)

        val before = System.nanoTime()
        engine.stopImmediately()
        val elapsedMs = (System.nanoTime() - before) / 1_000_000

        assertTrue("stopImmediately waited ${elapsedMs}ms, expected well under the fade budget",
            elapsedMs < 1_000)
        assertTrue(awaitFinish())
        assertEquals(FinishReason.STOPPED, lastReason.get())
        assertFalse(engine.isPlaying)
    }
}
