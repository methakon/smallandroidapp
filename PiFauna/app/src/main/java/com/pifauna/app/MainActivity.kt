package com.pifauna.app

import android.app.KeyguardManager
import android.content.Context
import android.os.Bundle
import android.os.PowerManager
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pifauna.app.data.PresetStore
import com.pifauna.app.data.SettingsStore
import com.pifauna.app.playback.PlaybackRuntime
import com.pifauna.app.ui.PiFaunaApp
import com.pifauna.app.ui.PiFaunaTheme
import com.pifauna.app.ui.Route

/**
 * Foreground UI owner, not the owner of an opted-in screen-lock audio session.
 * Without opt-in, onStop retains the original immediate-stop behavior.
 */
class MainActivity : ComponentActivity() {
    private lateinit var settings: SettingsStore
    private lateinit var presets: PresetStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        settings = SettingsStore(this)
        presets = PresetStore(this)

        val routeExtra = intent?.getStringExtra("route")?.let {
            runCatching { Route.valueOf(it) }.getOrNull()
        }
        val startRoute = routeExtra ?: if (settings.welcomeSeen) Route.Home else Route.Welcome
        setContent {
            PiFaunaTheme {
                PiFaunaApp(
                    controller = PlaybackRuntime.controller(this),
                    settings = settings,
                    presetStore = presets,
                    startRoute = startRoute
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        PlaybackRuntime.onActivityStarted(this)
    }

    override fun onStop() {
        val power = getSystemService(Context.POWER_SERVICE) as PowerManager
        val keyguard = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val locked = !power.isInteractive || keyguard.isKeyguardLocked
        PlaybackRuntime.onActivityStopped(this, locked)
        super.onStop()
    }

    override fun onDestroy() {
        PlaybackRuntime.onActivityDestroyed()
        super.onDestroy()
    }
}
