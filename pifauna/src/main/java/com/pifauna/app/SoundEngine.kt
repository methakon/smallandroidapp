package com.pifauna.app

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.SystemClock
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.sin

/**
 * Sine-wave PCM output; no microphone, network, wakelock, or background service.
 * Device acoustic output is NOT verified by this generator.
 */
class SoundEngine {
    private val running = AtomicBoolean(false)
    @Volatile private var worker: Thread? = null
    @Volatile private var track: AudioTrack? = null

    @Synchronized fun start(frequencyHz: Int, durationMs: Long, onFinish: () -> Unit): Boolean {
        stop()
        if (frequencyHz !in 200..16000 || durationMs !in 1000..1_800_000) return false
        val sampleRate = 44100
        val minimum = AudioTrack.getMinBufferSize(
            sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        if (minimum <= 0) return false
        val bufferBytes = maxOf(minimum, 4096)
        val audio = try {
            AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                .setAudioFormat(AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setBufferSizeInBytes(bufferBytes).build()
        } catch (_: Exception) { return false }
        if (audio.state != AudioTrack.STATE_INITIALIZED) { audio.release(); return false }
        track = audio
        running.set(true)
        worker = thread(name = "PiFaunaAudio", isDaemon = true) {
            try {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO)
                val samples = ShortArray(bufferBytes / 2)
                var phase = 0.0
                val step = 2.0 * PI * frequencyHz / sampleRate
                val endAt = SystemClock.elapsedRealtime() + durationMs
                audio.play()
                while (running.get() && SystemClock.elapsedRealtime() < endAt) {
                    for (i in samples.indices) {
                        // 8% digital full-scale to avoid aggressive output and clipping.
                        samples[i] = (sin(phase) * 2621.0).toInt().toShort()
                        phase += step
                        if (phase >= 2.0 * PI) phase -= 2.0 * PI
                    }
                    val count = audio.write(samples, 0, samples.size, AudioTrack.WRITE_BLOCKING)
                    if (count <= 0) break
                }
            } catch (_: Exception) {
                // Audio devices can disappear during playback; always release.
            } finally {
                running.set(false)
                try { audio.pause(); audio.flush(); audio.release() } catch (_: Exception) {}
                if (track === audio) track = null
                onFinish()
            }
        }
        return true
    }

    @Synchronized fun stop() {
        running.set(false)
        // Do not release a track while the worker may be writing to it.
        try { track?.pause() } catch (_: Exception) {}
    }
}
