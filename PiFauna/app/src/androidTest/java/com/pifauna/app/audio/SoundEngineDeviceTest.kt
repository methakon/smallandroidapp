package com.pifauna.app.audio

import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pifauna.app.safety.SafetyLimits
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercised on a real device against a real AudioTrack.
 *
 * Sessions here are deliberately short (2 s) and quiet (3–5% of full scale) so
 * the tests never leave an unattended tone running. Set the media volume low
 * before running them.
 */
@RunWith(AndroidJUnit4::class)
class SoundEngineDeviceTest {

    private lateinit var engine: SoundEngine

    private fun config(durationMs: Long, gain: Float = 0.03f) = ToneConfig(
        startHz = 500,
        endHz = 500,
        mode = ToneMode.FIXED,
        gain = gain,
        durationMs = durationMs
    )

    @Before
    fun setUp() {
        engine = SoundEngine()
    }

    @Test
    fun audioOutputIsAvailable() {
        // Proves the device can open a 48 kHz mono stream at all.
        val started = engine.start(config(1_000L)) { _, _ -> }
        assertTrue("engine could not start: $started", started is StartResult.Started)
        engine.stop()
    }

    @Test
    fun sessionCompletesOnItsOwnAtTheRequestedDuration() {
        val latch = CountDownLatch(1)
        val reason = AtomicReference<FinishReason?>(null)
        val elapsedAtFinish = AtomicReference(0L)

        val begin = SystemClock.elapsedRealtime()
        val started = engine.start(config(2_000L)) { why, _ ->
            reason.set(why)
            elapsedAtFinish.set(SystemClock.elapsedRealtime() - begin)
            latch.countDown()
        }
        assertTrue(started is StartResult.Started)

        assertTrue("no completion callback within 8 s", latch.await(8, TimeUnit.SECONDS))
        assertEquals(FinishReason.COMPLETED, reason.get())

        val elapsed = elapsedAtFinish.get()
        assertTrue("finished too early: ${elapsed}ms", elapsed >= 1_800L)
        assertTrue("finished too late: ${elapsed}ms", elapsed <= 5_000L)
        assertFalse("engine must be idle after completion", engine.isPlaying)
    }

    @Test
    fun stopEndsPlaybackPromptly() {
        val latch = CountDownLatch(1)
        val started = engine.start(config(30_000L)) { _, _ -> latch.countDown() }
        assertTrue(started is StartResult.Started)

        SystemClock.sleep(300)

        val beforeStop = SystemClock.elapsedRealtime()
        engine.stop()
        val stopMillis = SystemClock.elapsedRealtime() - beforeStop

        assertTrue("stop() blocked for ${stopMillis}ms", stopMillis < 1_500L)
        assertFalse("engine still playing after stop()", engine.isPlaying)
        assertTrue("worker did not report a finish", latch.await(2, TimeUnit.SECONDS))
    }

    @Test
    fun rapidStartStopCyclesDoNotLeakOrOverlap() {
        val finished = AtomicInteger(0)
        repeat(12) { index ->
            val started = engine.start(config(30_000L)) { _, _ -> finished.incrementAndGet() }
            assertTrue(
                "cycle $index failed to start: $started",
                started is StartResult.Started || started == StartResult.Busy
            )
            engine.stop()
            assertFalse("cycle $index left the engine playing", engine.isPlaying)
        }
        // Every session that did start must report completion exactly once.
        val deadline = SystemClock.elapsedRealtime() + 5_000L
        while (finished.get() < 12 && SystemClock.elapsedRealtime() < deadline) {
            SystemClock.sleep(50)
        }
        assertTrue("only ${finished.get()} of 12 sessions reported a finish", finished.get() >= 10)
    }

    @Test
    fun invalidRequestIsRejectedBeforeAnyAudioStarts() {
        val rejected = runCatching {
            ToneConfig(500, 500, ToneMode.FIXED, SafetyLimits.MAX_GAIN + 0.01f, 2_000L)
        }.isFailure
        assertTrue("gain above the ceiling must not construct", rejected)
        assertFalse(engine.isPlaying)
    }
}
