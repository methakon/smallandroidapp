package com.pifauna.app.playback

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pifauna.app.MainActivity
import com.pifauna.app.data.SettingsStore
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimedPlaybackServiceDeviceTest {

    private lateinit var settings: SettingsStore

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        settings = SettingsStore(context)
        PlaybackRuntime.stop(context, "Setup reset")
    }

    @After
    fun tearDown() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        PlaybackRuntime.stop(context, "Teardown")
    }

    @Test
    fun changingSettingAloneNeverStartsPlayback() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val controller = PlaybackRuntime.controller(context)

        assertFalse("Playback should be idle initially", controller.state.value.isPlaying)

        settings.continueWhenScreenLocked = true
        assertFalse("Enabling screen-lock setting must not start audio", controller.state.value.isPlaying)

        settings.continueWhenScreenLocked = false
        assertFalse("Disabling screen-lock setting must not start audio", controller.state.value.isPlaying)
    }

    @Test
    fun defaultOffModeStopsWhenScreenLocks() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val controller = PlaybackRuntime.controller(context)

        controller.refreshSnapshot()
        PlaybackRuntime.start(context, continueWhenLocked = false)

        var isPlaying = false
        for (i in 0 until 30) {
            Thread.sleep(100)
            if (controller.state.value.isPlaying) {
                isPlaying = true
                break
            }
        }
        assertTrue("Playback should have started", isPlaying)

        // Simulate screen locking with setting OFF
        PlaybackRuntime.onActivityStopped(context, deviceLockedOrScreenOff = true)

        var isStopped = false
        for (i in 0 until 30) {
            Thread.sleep(100)
            if (!controller.state.value.isPlaying) {
                isStopped = true
                break
            }
        }
        assertTrue("When setting is OFF, screen lock must immediately stop playback", isStopped)
    }

    @Test
    fun explicitOnModeAllowsPlaybackToContinueWhenScreenLocks() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val controller = PlaybackRuntime.controller(context)

        controller.refreshSnapshot()
        PlaybackRuntime.start(context, continueWhenLocked = true)

        var isPlaying = false
        for (i in 0 until 30) {
            Thread.sleep(100)
            if (controller.state.value.isPlaying) {
                isPlaying = true
                break
            }
        }
        assertTrue("Playback should have started in locked-screen mode", isPlaying)
        assertTrue("PlaybackRuntime should report locked session requested", PlaybackRuntime.hasLockedSession)

        // Simulate screen locking with setting ON
        PlaybackRuntime.onActivityStopped(context, deviceLockedOrScreenOff = true)

        Thread.sleep(1000)
        assertTrue("When setting is ON, playback must continue through screen lock", controller.state.value.isPlaying)

        // Stop cleanly
        PlaybackRuntime.stop(context, "Test completed")

        var isStopped = false
        for (i in 0 until 30) {
            Thread.sleep(100)
            if (!controller.state.value.isPlaying) {
                isStopped = true
                break
            }
        }
        assertTrue("Stop must halt active playback", isStopped)
        assertFalse("Locked session must be cleared after stop", PlaybackRuntime.hasLockedSession)
    }

    @Test
    fun switchingAppsWithScreenUnlockedStillStopsPlaybackEvenIfOptedIn() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val controller = PlaybackRuntime.controller(context)

        controller.refreshSnapshot()
        PlaybackRuntime.start(context, continueWhenLocked = true)

        var isPlaying = false
        for (i in 0 until 30) {
            Thread.sleep(100)
            if (controller.state.value.isPlaying) {
                isPlaying = true
                break
            }
        }
        assertTrue("Playback should have started", isPlaying)

        // Simulate switching apps while display is UNLOCKED (deviceLockedOrScreenOff = false)
        PlaybackRuntime.onActivityStopped(context, deviceLockedOrScreenOff = false)

        var isStopped = false
        for (i in 0 until 30) {
            Thread.sleep(100)
            if (!controller.state.value.isPlaying) {
                isStopped = true
                break
            }
        }
        assertTrue("Switching apps with display unlocked must stop playback even if screen-lock opt-in was enabled", isStopped)
    }
}
