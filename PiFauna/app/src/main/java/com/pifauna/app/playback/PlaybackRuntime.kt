package com.pifauna.app.playback

import android.content.Context
import android.content.Intent
import com.pifauna.app.safety.SafetyMonitor

/**
 * One process-local owner for the single PlayerController and safety monitor.
 * Service and Activity deliberately share the same engine; no second AudioTrack
 * is created when the screen locks. No process-death or boot auto-resumption.
 */
object PlaybackRuntime {
    @Volatile private var player: PlayerController? = null
    @Volatile private var monitor: SafetyMonitor? = null
    @Volatile private var activityStarted = false
    @Volatile private var serviceRunning = false
    @Volatile private var lockedSessionRequested = false

    val hasLockedSession: Boolean get() = lockedSessionRequested

    @Synchronized
    fun controller(context: Context): PlayerController {
        return player ?: PlayerController(context.applicationContext).also { player = it }
    }

    @Synchronized
    private fun safetyMonitor(context: Context): SafetyMonitor {
        return monitor ?: SafetyMonitor(context.applicationContext) {
            controller(context).refreshSnapshot()
        }.also { monitor = it }
    }

    fun onActivityStarted(context: Context) {
        activityStarted = true
        safetyMonitor(context).begin()
        controller(context).refreshSnapshot()
    }

    /**
     * A screen-lock opt-in preserves only a deliberately started timed session.
     * Pressing Home/switching apps on an unlocked display still ends playback.
     */
    fun onActivityStopped(context: Context, deviceLockedOrScreenOff: Boolean) {
        activityStarted = false
        if (!lockedSessionRequested || !deviceLockedOrScreenOff) {
            if (lockedSessionRequested) {
                stop(context, "Stopped: the app left the foreground.")
            } else {
                controller(context).onAppBackgrounded()
            }
        }
        if (!serviceRunning && !lockedSessionRequested) monitor?.end()
    }

    fun onActivityDestroyed() {
        if (!serviceRunning && !lockedSessionRequested) player?.release()
    }

    /**
     * Called only from explicit user Start in the visible Activity.
     * Start the mediaPlayback FGS BEFORE leaving the foreground.
     */
    fun start(context: Context, continueWhenLocked: Boolean) {
        val playback = controller(context)
        if (playback.state.value.isPlaying || lockedSessionRequested) return
        if (!continueWhenLocked) {
            playback.start()
            return
        }

        lockedSessionRequested = true
        try {
            context.applicationContext.startForegroundService(
                Intent(context.applicationContext, TimedPlaybackService::class.java)
                    .setAction(TimedPlaybackService.ACTION_START)
            )
        } catch (error: Exception) {
            lockedSessionRequested = false
            playback.stop("Lock-screen mode unavailable; no audio was started.")
            if (!activityStarted) monitor?.end()
        }
    }

    fun stop(context: Context, reason: String = "Stopped by user.") {
        controller(context).stop(reason)
        lockedSessionRequested = false
        if (serviceRunning) {
            context.applicationContext.stopService(
                Intent(context.applicationContext, TimedPlaybackService::class.java)
            )
        } else if (!activityStarted) {
            monitor?.end()
        }
    }

    fun onServiceCreated(context: Context) {
        serviceRunning = true
        safetyMonitor(context).begin()
    }

    fun onServiceDestroyed() {
        serviceRunning = false
        lockedSessionRequested = false
        if (!activityStarted) {
            monitor?.end()
            player?.release()
        }
    }
}
