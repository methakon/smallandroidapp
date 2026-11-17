package com.pifauna.app.mantra

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.IBinder
import android.os.PowerManager
import com.pifauna.app.MainActivity
import `in`.berhampore.dhar.layaswarna.R

/**
 * Foreground service for Mantra Japa counted replay while the screen is locked.
 * Displays live chant progress and provides lock-screen Play, Pause, and Stop controls.
 */
class MantraPlaybackService : Service() {
    private lateinit var notifications: NotificationManager
    private lateinit var session: MediaSession
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        notifications = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifications.createNotificationChannel(
            NotificationChannel(
                CHANNEL, "Layaswarna mantra playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active, user-started mantra japa session with Play, Pause, and Stop controls"
                setSound(null, null)
            }
        )
        session = MediaSession(this, "Layaswarna mantra playback").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onStop() {
                    MantraRuntime.stop(this@MantraPlaybackService, "Stopped from lock screen.")
                }
                override fun onPause() {
                    MantraRuntime.pause(this@MantraPlaybackService)
                }
                override fun onPlay() {
                    MantraRuntime.resume(this@MantraPlaybackService)
                }
            })
            isActive = true
        }
        val power = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "layaswarna:mantra_japa").apply {
            setReferenceCounted(false)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                MantraRuntime.stop(this, "Stopped from notification.")
                return START_NOT_STICKY
            }
            ACTION_PAUSE -> {
                MantraRuntime.pause(this)
                return START_NOT_STICKY
            }
            ACTION_PLAY -> {
                MantraRuntime.resume(this)
                return START_NOT_STICKY
            }
            ACTION_START -> {
                wakeLock?.acquire(35 * 60 * 1000L) // 35 min max safety boundary
                startForeground(NOTIFICATION_ID, buildNotification())
                updateSessionState()
            }
            ACTION_UPDATE -> {
                updateNotification()
            }
            else -> finishService()
        }
        return START_NOT_STICKY
    }

    private fun buildNotification(): Notification {
        val state = MantraRuntime.sessionState.value
        val isPaused = MantraRuntime.isPaused.value
        val title = state?.title?.ifBlank { null } ?: "Mantra Japa Practice"
        val total = state?.totalChants ?: 108
        val completed = state?.completedChants ?: 0
        val currentSet = state?.currentSet ?: 1
        val sets = state?.sets ?: 1

        val pauseSuffix = if (isPaused) " (Paused)" else ""
        val text = "Chant ${completed + 1} of $total (Set $currentSet of $sets) · built-in speaker only$pauseSuffix"

        val stopIntent = Intent(this, MantraPlaybackService::class.java).setAction(ACTION_STOP)
        val stopPending = PendingIntent.getService(
            this, 101, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val openIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("route", "MantraStudio")
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val openPending = PendingIntent.getActivity(
            this, 102, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseAction = if (isPaused) {
            val playIntent = Intent(this, MantraPlaybackService::class.java).setAction(ACTION_PLAY)
            val playPending = PendingIntent.getService(
                this, 103, playIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            Notification.Action.Builder(
                Icon.createWithResource(this, R.drawable.ic_media_play), "Play", playPending
            ).build()
        } else {
            val pauseIntent = Intent(this, MantraPlaybackService::class.java).setAction(ACTION_PAUSE)
            val pausePending = PendingIntent.getService(
                this, 104, pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            Notification.Action.Builder(
                Icon.createWithResource(this, R.drawable.ic_media_pause), "Pause", pausePending
            ).build()
        }

        val stopAction = Notification.Action.Builder(
            Icon.createWithResource(this, R.drawable.ic_media_stop), "Stop", stopPending
        ).build()

        return Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_audio)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(openPending)
            .setOnlyAlertOnce(true)
            .setOngoing(!isPaused)
            .setCategory(Notification.CATEGORY_TRANSPORT)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .addAction(playPauseAction)
            .addAction(stopAction)
            .setStyle(
                Notification.MediaStyle()
                    .setMediaSession(session.sessionToken)
                    .setShowActionsInCompactView(0, 1)
            ).build()
    }

    @SuppressLint("NotificationPermission")
    fun updateNotification() {
        if (!MantraRuntime.isPlaying.value && !MantraRuntime.isPaused.value) {
            finishService()
            return
        }
        updateSessionState()
        notifications.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun updateSessionState() {
        val state = MantraRuntime.sessionState.value
        val isPaused = MantraRuntime.isPaused.value
        session.setMetadata(
            MediaMetadata.Builder().putString(
                MediaMetadata.METADATA_KEY_TITLE,
                state?.title ?: "Mantra Japa Practice"
            ).build()
        )
        val playbackState = if (isPaused) {
            PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_STOP)
                .setState(PlaybackState.STATE_PAUSED, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 0f)
                .build()
        } else {
            PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_STOP)
                .setState(PlaybackState.STATE_PLAYING, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1f)
                .build()
        }
        session.setPlaybackState(playbackState)
    }

    private fun finishService() {
        wakeLock?.let { if (it.isHeld) it.release() }
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
        MantraRuntime.stop(this, "Stopped: the app task was dismissed.")
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        wakeLock?.let { if (it.isHeld) it.release() }
        if (::session.isInitialized) {
            session.isActive = false
            session.release()
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "in.berhampore.dhar.layaswarna.START_MANTRA_PLAYBACK"
        const val ACTION_STOP = "in.berhampore.dhar.layaswarna.STOP_MANTRA_PLAYBACK"
        const val ACTION_PAUSE = "in.berhampore.dhar.layaswarna.PAUSE_MANTRA_PLAYBACK"
        const val ACTION_PLAY = "in.berhampore.dhar.layaswarna.PLAY_MANTRA_PLAYBACK"
        const val ACTION_UPDATE = "in.berhampore.dhar.layaswarna.UPDATE_MANTRA_PLAYBACK"
        private const val CHANNEL = "layaswarna_mantra_audio"
        private const val NOTIFICATION_ID = 1028
    }
}
