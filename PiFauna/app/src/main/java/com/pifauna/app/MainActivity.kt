package com.pifauna.app

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pifauna.app.data.PresetStore
import com.pifauna.app.data.SettingsStore
import com.pifauna.app.playback.PlayerController
import com.pifauna.app.safety.SafetyMonitor
import com.pifauna.app.ui.PiFaunaApp
import com.pifauna.app.ui.PiFaunaTheme
import com.pifauna.app.ui.Route

/**
 * The only activity.
 *
 * It hosts the Compose tree and owns the lifecycle contract that makes playback
 * safe: the safety monitor is registered only while started, and leaving the
 * foreground stops audio unconditionally. There is no service, so sound cannot
 * outlive the visible app.
 */
class MainActivity : ComponentActivity() {

    private lateinit var controller: PlayerController
    private lateinit var settings: SettingsStore
    private lateinit var presets: PresetStore
    private lateinit var safetyMonitor: SafetyMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android 16 enforces edge-to-edge for apps targeting API 36. Opting in
        // explicitly keeps the layout identical on older releases instead of
        // shifting once the device is updated. The UI is always dark, so the
        // system bar icons must stay light on the transparent bars.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        controller = PlayerController(this)
        settings = SettingsStore(this)
        presets = PresetStore(this)
        safetyMonitor = SafetyMonitor(this) { controller.refreshSnapshot() }

        val startRoute = if (settings.welcomeSeen) Route.Home else Route.Welcome

        setContent {
            PiFaunaTheme {
                PiFaunaApp(
                    controller = controller,
                    settings = settings,
                    presetStore = presets,
                    startRoute = startRoute
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        safetyMonitor.begin()
        controller.refreshSnapshot()
    }

    override fun onStop() {
        // Foreground-only by design; also covers screen-off and app switching.
        controller.onAppBackgrounded()
        safetyMonitor.end()
        super.onStop()
    }

    override fun onDestroy() {
        safetyMonitor.end()
        controller.release()
        super.onDestroy()
    }
}
