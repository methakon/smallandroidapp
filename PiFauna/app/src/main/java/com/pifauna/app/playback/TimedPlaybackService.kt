package com.pifauna.app.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.pifauna.app.MainActivity
import com.pifauna.app.R
import com.pifauna.app.util.TimeFormat

/**
 * User-initiated, bounded audio service. No restart, no audio after process
 * death, no continuous/overnight play. Notification Stop is always actionable.
 */
class TimedPlaybackService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var session: MediaSession
    private lateinit var notifications: NotificationManager
    private var tickCount = 0

    private val tick = object : Runnable {
        override fun run() {
            val player = PlaybackRuntime.controller(this@TimedPlaybackService)
            val current = player.state.value
            if (!current.isPlaying) {
                finishSession()
                return
            }
            // Extra watchdog; ToneKernel independently enforces totalFrames.
            if (current.remainingMs() <= 0L) {
                PlaybackRuntime.stop(
                    this@TimedPlaybackService, "Session finished — timer elapsed."
                )
                return
            }
            player.refreshSnapshot()
            if (++tickCount % 15 == 0) updateNotification()
            handler.postDelayed(this, 1_000L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        notifications = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifications.createNotificationChannel(
            NotificationChannel(
                CHANNEL, "Layaswarna timed playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active, user-started sound session with Stop control"
                setSound(null, null)
            }
        )
        session = MediaSession(this, "Layaswarna timed playback").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onStop() { stopNow("Stopped from lock screen.") }
                // Play and resume are intentionally not exposed.
            }, handler)
            isActive = true
        }
        PlaybackRuntime.onServiceCreated(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopNow("Stopped from notification.")
                return START_NOT_STICKY
            }
            ACTION_START -> {
                // In particular, never auto-start from a stale or replayed intent.
                if (!PlaybackRuntime.hasLockedSession) {
                    finishSession()
                    return START_NOT_STICKY
                }
                val player = PlaybackRuntime.controller(this)
                // Android requires prompt startForeground on this user-triggered path.
                startForeground(NOTIFICATION_ID, notification())
                if (!player.state.value.isPlaying) player.start()
                if (!player.state.value.isPlaying) {
                    finishSession()
                    return START_NOT_STICKY
                }
                updateNotification()
                handler.removeCallbacks(tick)
                handler.postDelayed(tick, 1_000L)
            }
            else -> finishSession()
        }
        return START_NOT_STICKY
    }

    private fun notification(): Notification {
        val state = PlaybackRuntime.controller(this).state.value
        val title = state.displayName ?: "Layaswarna timed audio"
        val remaining = TimeFormat.clock(
            if (state.isPlaying) state.remainingMs()
            else state.durationMinutes * 60_000L
        )
        val stopIntent = Intent(this, TimedPlaybackService::class.java)
            .setAction(ACTION_STOP)
        val stopPending = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val openPending = PendingIntent.getActivity(
            this, 2,
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_audio)
            .setContentTitle(title)
            .setContentText(remaining + " remaining · built-in speaker only")
            .setContentIntent(openPending)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_TRANSPORT)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .addAction(
                Notification.Action.Builder(
                    R.drawable.ic_stat_audio, "Stop", stopPending
                ).build()
            )
            .setStyle(
                Notification.MediaStyle()
                    .setMediaSession(session.sessionToken)
                    .setShowActionsInCompactView(0)
            ).build()
    }

    private fun updateNotification() {
        val state = PlaybackRuntime.controller(this).state.value
        if (!state.isPlaying) return
        session.setMetadata(
            MediaMetadata.Builder().putString(
                MediaMetadata.METADATA_KEY_TITLE,
                state.displayName ?: "Layaswarna timed audio"
            ).build()
        )
        session.setPlaybackState(
            PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_STOP)
                .setState(PlaybackState.STATE_PLAYING, 0L, 1f)
                .build()
        )
        notifications.notify(NOTIFICATION_ID, notification())
    }

    private fun stopNow(reason: String) {
        PlaybackRuntime.controller(this).stop(reason)
        finishSession()
    }

    private fun finishSession() {
        handler.removeCallbacks(tick)
        if (::session.isInitialized) {
            session.setPlaybackState(
                PlaybackState.Builder().setState(
                    PlaybackState.STATE_STOPPED, 0L, 0f
                ).build()
            )
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopNow("Stopped: the app task was dismissed.")
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        if (::session.isInitialized) {
            session.isActive = false
            session.release()
        }
        PlaybackRuntime.onServiceDestroyed()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "in.berhampore.dhar.layaswarna.START_TIMED_LOCK_AUDIO"
        const val ACTION_STOP = "in.berhampore.dhar.layaswarna.STOP_TIMED_LOCK_AUDIO"
        private const val CHANNEL = "layaswarna_timed_audio"
        private const val NOTIFICATION_ID = 1027
    }
}
