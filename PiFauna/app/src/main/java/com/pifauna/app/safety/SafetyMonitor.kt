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
import androidx.annotation.RequiresApi
import java.util.concurrent.Executor

/**
 * Watches the device for conditions that must interrupt playback.
 *
 * Design notes:
 *  - event driven: a sticky battery broadcast plus a thermal listener, never a poll loop;
 *  - registered only between [begin] and [end] (activity started/stopped), so it
 *    cannot leak a receiver or keep the process alive;
 *  - [onChanged] is always delivered on the main thread.
 *
 * API 29 types are confined to [ThermalWatcher]. Holding
 * `PowerManager.OnThermalStatusChangedListener` as a field of this class puts it in
 * the class signature, and ART then fails to verify the entire class on Android 8
 * and 9 with NoClassDefFoundError — before any SDK_INT check could run. That is
 * exactly how the app crashed at launch on API 28.
 */
class SafetyMonitor(context: Context, private val onChanged: () -> Unit) {

    private val appContext = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private val mainExecutor = Executor { command -> handler.post(command) }
    private val powerManager =
        appContext.getSystemService(Context.POWER_SERVICE) as PowerManager

    private var receiver: BroadcastReceiver? = null

    /** Non-null only on API 29+; typed as Any to keep the API 29 type out of this class. */
    private var thermalWatcher: Any? = null

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
            val watcher = ThermalWatcher(powerManager, mainExecutor, onChanged)
            runCatching { watcher.start() }
                .onSuccess { thermalWatcher = watcher }
                .onFailure { Log.w(TAG, "thermal listener not available", it) }
        }
    }

    fun end() {
        if (!active) return
        active = false
        receiver?.let { runCatching { appContext.unregisterReceiver(it) } }
        receiver = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            (thermalWatcher as? ThermalWatcher)?.let { runCatching { it.stop() } }
        }
        thermalWatcher = null
    }

    /** Loaded only on API 29+, which is why the API 29 types may live here. */
    @RequiresApi(Build.VERSION_CODES.Q)
    private class ThermalWatcher(
        private val powerManager: PowerManager,
        private val executor: Executor,
        onChanged: () -> Unit
    ) {
        private val listener = PowerManager.OnThermalStatusChangedListener { onChanged() }

        fun start() = powerManager.addThermalStatusListener(executor, listener)

        fun stop() = powerManager.removeThermalStatusListener(listener)
    }

    private companion object {
        const val TAG = "PiFaunaSafety"
    }
}
