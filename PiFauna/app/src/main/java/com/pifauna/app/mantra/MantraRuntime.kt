package com.pifauna.app.mantra

import android.content.Context
import android.content.Intent
import com.pifauna.app.playback.PlaybackRuntime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class MantraSessionState(
    val recordingId: String,
    val title: String,
    val file: File,
    val sets: Int,
    val totalChants: Int,
    val completedChants: Int,
    val currentSet: Int,
    val gapMs: Long,
    val statusText: String? = null
)

/**
 * Process-local coordinator for Mantra audio operations, keeping UI and
 * background service in lockstep across screen-off and screen-lock events.
 */
object MantraRuntime {
    @Volatile private var engineInstance: MantraAudioEngine? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _sessionState = MutableStateFlow<MantraSessionState?>(null)
    val sessionState: StateFlow<MantraSessionState?> = _sessionState.asStateFlow()

    @Synchronized
    fun engine(context: Context): MantraAudioEngine {
        return engineInstance ?: MantraAudioEngine(context.applicationContext).also { engineInstance = it }
    }

    fun startReplay(
        context: Context,
        file: File,
        recordingId: String,
        title: String,
        sets: Int,
        gapMs: Long,
        resumeFrom: Int = 0,
        store: MantraStore,
        onCompletion: ((completed: Int, error: String?) -> Unit)? = null
    ) {
        val appContext = context.applicationContext
        // 1. Stop synthesized tone playback if running
        PlaybackRuntime.stop(appContext, "Stopped before mantra replay.")

        val total = MantraRepetitionPolicy.totalRepetitions(sets)
        val initialProgress = MantraRepetitionPolicy.progress(resumeFrom.coerceIn(0, total), sets)

        _isPlaying.value = true
        _sessionState.value = MantraSessionState(
            recordingId = recordingId,
            title = title,
            file = file,
            sets = sets,
            totalChants = total,
            completedChants = resumeFrom,
            currentSet = initialProgress.currentSet,
            gapMs = gapMs
        )

        // 2. Start Foreground Service
        try {
            appContext.startForegroundService(
                Intent(appContext, MantraPlaybackService::class.java).setAction(MantraPlaybackService.ACTION_START)
            )
        } catch (e: Exception) {
            _isPlaying.value = false
            _sessionState.value = null
            onCompletion?.invoke(resumeFrom, "Could not start lock-screen service.")
            return
        }

        // 3. Start Audio Engine Replay
        val eng = engine(appContext)
        eng.startReplay(file, sets, gapMs, { done, error ->
            val progress = MantraRepetitionPolicy.progress(done.coerceIn(0, total), sets)
            if (done in 1 until total) {
                store.saveCheckpoint(recordingId, sets, gapMs, done)
            } else if (done >= total) {
                store.clearCheckpoint(recordingId)
            }

            _sessionState.value = _sessionState.value?.copy(
                completedChants = done,
                currentSet = progress.currentSet,
                statusText = error
            )

            if (error != null || done >= total) {
                _isPlaying.value = false
                appContext.stopService(Intent(appContext, MantraPlaybackService::class.java))
                onCompletion?.invoke(done, error)
            } else {
                appContext.startService(
                    Intent(appContext, MantraPlaybackService::class.java).setAction(MantraPlaybackService.ACTION_UPDATE)
                )
                onCompletion?.invoke(done, null)
            }
        }, resumeFrom)
    }

    fun startPreview(
        context: Context,
        file: File,
        onCompletion: (Int, String?) -> Unit
    ) {
        val appContext = context.applicationContext
        PlaybackRuntime.stop(appContext, "Stopped before mantra preview.")
        _isPlaying.value = true
        engine(appContext).startPreview(file) { done, error ->
            _isPlaying.value = false
            onCompletion(done, error)
        }
    }

    fun pause(context: Context) {
        val appContext = context.applicationContext
        engine(appContext).pausePlayback()
        _isPaused.value = true
        appContext.startService(
            Intent(appContext, MantraPlaybackService::class.java).setAction(MantraPlaybackService.ACTION_UPDATE)
        )
    }

    fun resume(context: Context) {
        val appContext = context.applicationContext
        engine(appContext).resumePlayback()
        _isPaused.value = false
        appContext.startService(
            Intent(appContext, MantraPlaybackService::class.java).setAction(MantraPlaybackService.ACTION_UPDATE)
        )
    }

    fun stop(context: Context, reason: String = "Stopped by user.") {
        val appContext = context.applicationContext
        engine(appContext).stopPlayback()
        _isPlaying.value = false
        _isPaused.value = false
        appContext.stopService(Intent(appContext, MantraPlaybackService::class.java))
    }
}
