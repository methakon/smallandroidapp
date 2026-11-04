package com.pifauna.app.safety

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import java.util.concurrent.Executor

/**
 * Watches the device for conditions that must interrupt playback.
 *
 * Design notes:
 *  - event driven: a sticky battery broadcast plus a thermal listener, never a poll loop;
 *  - registered only between [begin] and [end] (activity started/stopped), so it
 *    cannot leak a receiver or keep the process alive;
 *  - [onChanged] is always delivered on the main thread.
 */
class SafetyMonitor(context: Context, private val onChanged: () -> Unit) {

    private val appContext = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private val mainExecutor = Executor { command -> handler.post(command) }
    private val powerManager =
        appContext.getSystemService(Context.POWER_SERVICE) as PowerManager

    private var receiver: BroadcastReceiver? = null
    private var thermalListener: PowerManager.OnThermalStatusChangedListener? = null
    private var active = false

    fun begin() {
        if (active) return
        active = true

        val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                onChanged()
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val registered = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appContext.registerReceiver(batteryReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                appContext.registerReceiver(batteryReceiver, filter)
            }
        }.onFailure { Log.w(TAG, "battery receiver not registered", it) }.isSuccess
        if (registered) receiver = batteryReceiver

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val listener = PowerManager.OnThermalStatusChangedListener { onChanged() }
            runCatching { powerManager.addThermalStatusListener(mainExecutor, listener) }
                .onSuccess { thermalListener = listener }
                .onFailure { Log.w(TAG, "thermal listener not available", it) }
        }
    }

    fun end() {
        if (!active) return
        active = false
        receiver?.let { runCatching { appContext.unregisterReceiver(it) } }
        receiver = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            thermalListener?.let { runCatching { powerManager.removeThermalStatusListener(it) } }
        }
        thermalListener = null
    }

    private companion object {
        const val TAG = "PiFaunaSafety"
    }
}
