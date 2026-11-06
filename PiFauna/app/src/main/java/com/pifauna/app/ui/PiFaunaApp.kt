package com.pifauna.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.pifauna.app.audio.ToneMode
import com.pifauna.app.data.ChakraProfiles
import com.pifauna.app.data.Preset
import com.pifauna.app.data.PresetStore
import com.pifauna.app.data.SettingsStore
import com.pifauna.app.data.SpeciesProfiles
import com.pifauna.app.data.SpeciesProfile
import com.pifauna.app.playback.PlayerController
import com.pifauna.app.playback.PlaybackRuntime
import androidx.compose.ui.platform.LocalContext
import com.pifauna.app.ui.screens.AnahataNadaScreen
import com.pifauna.app.ui.screens.AboutPrivacyScreen
import com.pifauna.app.ui.screens.ChakraConsoleScreen
import com.pifauna.app.ui.screens.ChakraLibraryScreen
import com.pifauna.app.ui.screens.BatterySaverScreen
import com.pifauna.app.ui.screens.CustomSoundScreen
import com.pifauna.app.ui.screens.DeviceProtectionScreen
import com.pifauna.app.ui.screens.HomeScreen
import com.pifauna.app.ui.screens.PlaybackSafetyScreen
import com.pifauna.app.ui.screens.PlayerScreen
import com.pifauna.app.ui.screens.ProfilesScreen
import com.pifauna.app.ui.screens.SafetyScreen
import com.pifauna.app.ui.screens.SettingsScreen
import com.pifauna.app.ui.screens.TimerScreen
import com.pifauna.app.ui.screens.WelcomeScreen

/** Every destination. Tab routes are the three from the Figma footer. */
enum class Route(val tab: Tab?) {
    Welcome(null),
    Home(Tab.Home),
    Player(null),
    Profiles(Tab.Profiles),
    CustomSound(null),
    Timer(null),
    Safety(null),
    Settings(Tab.Settings),
    DeviceProtection(null),
    BatterySaver(null),
    PlaybackSafety(null),
    ChakraLibrary(null),
    ChakraConsole(null),
    AnahataNada(null),
    AboutPrivacy(null)
}

/**
 * Application shell.
 *
 * Navigation is a plain back stack held in composition state: there are eleven
 * destinations and no deep links, so pulling in a navigation library would add a
 * dependency without buying anything. Back is handled explicitly.
 */
@Composable
fun PiFaunaApp(
    controller: PlayerController,
    settings: SettingsStore,
    presetStore: PresetStore,
    startRoute: Route
) {
    val state by controller.state.collectAsState()
    val context = LocalContext.current
    var continueWhenLocked by remember { mutableStateOf(settings.continueWhenScreenLocked) }
    var stack by remember { mutableStateOf(listOf(startRoute)) }
    var presets by remember { mutableStateOf(presetStore.load()) }
    var clearMessage by remember { mutableStateOf<String?>(null) }
    var selectedChakra by rememberSaveable { mutableStateOf("anahata") }
    var selectedRelaxation by rememberSaveable { mutableStateOf("pink") }
    var selectedSpecies by rememberSaveable { mutableStateOf(settings.lastProfileId) }

    val current = stack.last()

    fun push(route: Route) {
        stack = stack + route
    }

    fun pop() {
        if (stack.size > 1) stack = stack.dropLast(1)
    }

    fun openTab(tab: Tab) {
        val route = Route.entries.first { it.tab == tab }
        stack = listOf(route)
    }

    BackHandler(enabled = stack.size > 1) { pop() }

    fun openProfile(profile: SpeciesProfile) {
        controller.selectProfile(profile.id)
        selectedSpecies = profile.id
        settings.lastProfileId = profile.id
        push(Route.Player)
    }

    Scaffold(
        containerColor = PiFaunaColors.Background,
        bottomBar = {
            val tab = stack.asReversed().firstNotNullOfOrNull { it.tab } ?: Tab.Home
            PfBottomBar(selected = tab, onSelect = ::openTab)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(PiFaunaColors.Background, PiFaunaColors.BackgroundBottom)))
                .padding(innerPadding)
        ) {
            when (current) {
                Route.Welcome -> WelcomeScreen(
                    onGetStarted = {
                        settings.welcomeSeen = true
                        stack = listOf(Route.Home)
                    },
                    onLearnLimits = { push(Route.Safety) }
                )

                Route.Home -> HomeScreen(
                    onProfileSelected = ::openProfile,
                    selectedProfileId = selectedSpecies,
                    onSelectProfile = { selectedSpecies = it },
                    onOpenLibrary = { stack = listOf(Route.Profiles) },
                    onOpenChakras = { push(Route.ChakraLibrary) },
                    onOpenAnahata = { push(Route.AnahataNada) }
                )

                Route.Player -> PlayerScreen(
                    state = state,
                    onStart = {
                        settings.lastFrequencyHz = state.startHz
                        settings.lastDurationMinutes = state.durationMinutes
                        settings.lastMode = state.mode
                        settings.lastGain = state.gain
                        controller.refreshSnapshot()
                        PlaybackRuntime.start(context, continueWhenLocked)
                    },
                    onStop = { PlaybackRuntime.stop(context, "Stopped by user.") },
                    continueWhenLocked = continueWhenLocked,
                    onContinueWhenLockedChange = { enabled ->
                        if (!state.isPlaying) {
                            continueWhenLocked = enabled
                            settings.continueWhenScreenLocked = enabled
                        }
                    },
                    onSelectPhoneSpeaker = { controller.choosePhoneSpeaker() },
                    onApproveExternalSpeaker = { controller.confirmExternalSpeakerForSession() },
                    onStartFrequencyChange = { hz ->
                        val end = if (state.endHz < hz) hz else state.endHz
                        controller.setRange(hz, if (state.mode == ToneMode.FIXED ||
                            state.mode == ToneMode.PULSE
                        ) hz else end)
                    },
                    onEndFrequencyChange = { hz ->
                        controller.setRange(minOf(state.startHz, hz), hz)
                    },
                    onOpenTimer = {
                        settings.lastDurationMinutes = state.durationMinutes
                        push(Route.Timer)
                    }
                )

                Route.Profiles -> ProfilesScreen(
                    presets = presets,
                    selectedProfileId = selectedSpecies,
                    onSelectProfile = { selectedSpecies = it },
                    onProfileSelected = ::openProfile,
                    onPresetSelected = { preset ->
                        controller.selectProfile("custom")
                        controller.setRange(preset.startHz, preset.endHz)
                        controller.setMode(preset.mode)
                        controller.setGain(preset.gain)
                        push(Route.Player)
                    },
                    onDeletePreset = { name ->
                        presets = presetStore.remove(name)
                    },
                    onCreateCustom = { push(Route.CustomSound) }
                )

                Route.CustomSound -> CustomSoundScreen(
                    startHz = state.startHz,
                    endHz = state.endHz,
                    mode = state.mode,
                    gain = state.gain,
                    onRangeChange = { start, end -> controller.setRange(start, end) },
                    onModeChange = { mode ->
                        controller.setMode(mode)
                        if (mode == ToneMode.FIXED || mode == ToneMode.PULSE) {
                            controller.setRange(state.startHz, state.startHz)
                        }
                    },
                    onGainChange = { controller.setGain(it) },
                    onSave = { name ->
                        runCatching {
                            presets = presetStore.add(
                                Preset(
                                    name = name,
                                    startHz = state.startHz,
                                    endHz = state.endHz,
                                    mode = state.mode,
                                    gain = state.gain
                                )
                            )
                        }
                    }
                )

                Route.Timer -> TimerScreen(
                    selectedMinutes = state.durationMinutes,
                    onSelect = { minutes ->
                        controller.setDurationMinutes(minutes)
                        settings.lastDurationMinutes = minutes
                        pop()
                    }
                )

                Route.ChakraLibrary -> ChakraLibraryScreen(
                    selectedId = selectedChakra,
                    onSelect = { selectedChakra = it },
                    onOpen = { selectedChakra = it; push(Route.ChakraConsole) }
                )

                Route.ChakraConsole -> ChakraConsoleScreen(
                    selectedId = selectedChakra,
                    onSelect = { selectedChakra = it },
                    onUseTone = { chakra ->
                        if (!state.isPlaying) {
                            controller.prepareExperimentalTone(
                                label = "${chakra.name} · symbolic meditation tone",
                                startHz = chakra.toneHz,
                                endHz = chakra.toneHz,
                                mode = ToneMode.FIXED
                            )
                            push(Route.Player)
                        }
                    },
                    enabled = !state.isPlaying
                )

                Route.AnahataNada -> AnahataNadaScreen(
                    selectedId = selectedRelaxation,
                    onSelect = { selectedRelaxation = it },
                    onPrepare = { sound ->
                        if (!state.isPlaying && sound.playable) {
                            controller.prepareExperimentalTone(
                                label = "Anahata Nada · ${sound.label}",
                                startHz = requireNotNull(sound.startHz),
                                endHz = requireNotNull(sound.endHz),
                                mode = requireNotNull(sound.mode)
                            )
                            push(Route.Player)
                        }
                    },
                    selectedMinutes = state.durationMinutes,
                    onDurationSelect = { minutes ->
                        if (!state.isPlaying) {
                            controller.setDurationMinutes(minutes)
                            settings.lastDurationMinutes = minutes
                        }
                    },
                    enabled = !state.isPlaying
                )

                Route.Safety -> SafetyScreen()

                Route.AboutPrivacy -> AboutPrivacyScreen(onBack = ::pop)

                Route.Settings -> SettingsScreen(
                    continueWhenLocked = continueWhenLocked,
                    allowPlaybackModeChange = !state.isPlaying,
                    onContinueWhenLockedChange = { enabled ->
                        if (!state.isPlaying) {
                            continueWhenLocked = enabled
                            settings.continueWhenScreenLocked = enabled
                        }
                    },
                    onOpenSafety = { push(Route.Safety) },
                    onOpenDeviceProtection = { push(Route.DeviceProtection) },
                    onOpenBatterySaver = { push(Route.BatterySaver) },
                    onOpenPlaybackSafety = { push(Route.PlaybackSafety) },
                    onOpenPrivacy = { push(Route.AboutPrivacy) },
                    onClearData = {
                        if (state.isPlaying) PlaybackRuntime.stop(context, "Stopped to clear saved settings.")
                        settings.clearAll()
                        continueWhenLocked = false
                        presetStore.save(emptyList())
                        presets = emptyList()
                        selectedSpecies = SpeciesProfiles.all.first().id
                        clearMessage = "Saved settings and presets were deleted."
                    },
                    clearMessage = clearMessage
                )

                Route.DeviceProtection -> DeviceProtectionScreen()
                Route.BatterySaver -> BatterySaverScreen()
                Route.PlaybackSafety -> PlaybackSafetyScreen()
            }
        }
    }
}
