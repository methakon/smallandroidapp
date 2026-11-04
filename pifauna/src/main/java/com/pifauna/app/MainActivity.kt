package com.pifauna.app

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private val engine = SoundEngine()
    private var playing by mutableStateOf(false)
    private var status by mutableStateOf("Ready • sound output is experimental")
    private var audioManager: AudioManager? = null
    private var focusRequest: android.media.AudioFocusRequest? = null
    private var thermalListener: PowerManager.OnThermalStatusChangedListener? = null
    private var batteryReceiver: BroadcastReceiver? = null

    private fun batteryPercent(): Int {
        val b = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = b?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = b?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) level * 100 / scale else 100
    }

    private fun stop(message: String) {
        engine.stop()
        playing = false
        status = message
        focusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        focusRequest = null
    }

    private fun play(frequency: Int, minutes: Int) {
        if (batteryPercent() <= 15) { status = "Stopped: battery at or below 15%"; return }
        val power = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (Build.VERSION.SDK_INT >= 29 &&
            power.currentThermalStatus >= PowerManager.THERMAL_STATUS_SEVERE) {
            status = "Stopped: device is too hot"; return
        }
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val request = android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_MEDIA).build())
            .setOnAudioFocusChangeListener { change ->
                if (change <= AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) runOnUiThread {
                    stop("Stopped: audio interrupted")
                }
            }.build()
        if (audioManager?.requestAudioFocus(request) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            status = "Cannot play: audio output is in use"; return
        }
        focusRequest = request
        val started = engine.start(frequency, minutes * 60_000L) {
            runOnUiThread {
                if (playing) stop("Session completed")
            }
        }
        playing = started
        status = if (started) "Playing • $frequency Hz • $minutes min maximum" else "Audio output unavailable"
        if (!started) stop(status)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (playing && batteryPercent() <= 15) stop("Stopped: low battery")
            }
        }
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        if (Build.VERSION.SDK_INT >= 29) {
            val power = getSystemService(Context.POWER_SERVICE) as PowerManager
            val listener = PowerManager.OnThermalStatusChangedListener { level ->
                if (level >= PowerManager.THERMAL_STATUS_SEVERE) runOnUiThread {
                    stop("Stopped: thermal warning")
                }
            }
            power.addThermalStatusListener(mainExecutor, listener)
            thermalListener = listener
        }
        setContent {
            var frequency by remember { mutableFloatStateOf(8000f) }
            var duration by remember { mutableIntStateOf(10) }
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(Modifier.fillMaxSize()) {
                    Column(Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                        Text("PiFauna", style = MaterialTheme.typography.headlineLarge)
                        Spacer(Modifier.height(8.dp))
                        Text("Experimental phone-speaker sound generator")
                        Spacer(Modifier.height(28.dp))
                        Text("Frequency: ${frequency.toInt()} Hz")
                        Slider(value = frequency, onValueChange = { frequency = it },
                            valueRange = 200f..16000f, enabled = !playing)
                        Text("Duration")
                        Row {
                            listOf(5, 10, 20, 30).forEach { m ->
                                FilterChip(selected = duration == m,
                                    onClick = { duration = m },
                                    label = { Text("${m}m") },
                                    enabled = !playing,
                                    modifier = Modifier.padding(end = 8.dp))
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        Button(onClick = {
                            if (playing) stop("Stopped by user")
                            else play(frequency.toInt(), duration)
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (playing) "Stop playback" else "Start timed playback")
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(status)
                        Spacer(Modifier.height(28.dp))
                        Text("Device protection", style = MaterialTheme.typography.titleLarge)
                        Text("• Low digital signal level (8% of full scale)")
                        Text("• Stops when app leaves the foreground")
                        Text("• Stops on low battery, severe heat or audio interruption")
                        Text("• No network, microphone or location permissions")
                        Spacer(Modifier.height(24.dp))
                        Text("Safety notice", style = MaterialTheme.typography.titleLarge)
                        Text("Not proven to repel insects or animals. Phone speakers may not reproduce high frequencies. Begin at low system volume. Stop if anyone or any animal shows discomfort. Do not use headphones.")
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        stop("Stopped: app moved to background")
    }

    override fun onDestroy() {
        stop("Stopped")
        batteryReceiver?.let { unregisterReceiver(it) }
        if (Build.VERSION.SDK_INT >= 29) {
            thermalListener?.let {
                (getSystemService(Context.POWER_SERVICE) as PowerManager).removeThermalStatusListener(it)
            }
        }
        super.onDestroy()
    }
}
