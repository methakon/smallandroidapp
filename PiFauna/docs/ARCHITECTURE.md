# Layaswarna architecture

## Version history & status

- **versionCode 9 (v0.5.0):** Initial Play Internal release. In-process playback stopped whenever the app was backgrounded or the screen turned off.
- **versionCode 10 (candidate):** Architectural prototype introducing `TimedPlaybackService` foreground service.
- **versionCode 11 (v0.6.0):** Fully validated and released to Google Play Internal testing. Merged screen-lock foreground media service with reactive speaker route verification.
- **versionCode 12 (v0.7.0):** Current development branch reconciling audit findings: preflight audio routing warnings, store assets, and string resource extraction for future localization.

---

## Component ownership

- **MainActivity:** Compose UI host, navigation across 15 destinations (`PiFaunaApp`), `SettingsStore` persistence, user opt-in preferences, and screen-lock vs app-switch detection.
- **PlaybackRuntime:** Process singleton managing one `PlayerController` and one `SafetyMonitor` shared seamlessly by the Activity and the foreground `TimedPlaybackService`.
- **PlayerController:** Central audio controller coordinating `SoundEngine`/`ToneKernel`, audio focus management (`AudioManager`), safety rule evaluation (`SafetyPolicy`), audio route monitoring (`AudioRouteMonitor`), and reactive state propagation (`StateFlow<PlayerState>`).
- **TimedPlaybackService:** Android `mediaPlayback` foreground service used exclusively for user-initiated timed locked playback sessions. Shares `PlaybackRuntime`, owns the `MediaSession` and ongoing `MediaStyle` notification with a functioning Stop action, and runs a 1-second safety watchdog.
- **SoundEngine:** Low-level custom PCM audio synthesis using `AudioTrack` and `ToneKernel`. Manages audio-thread lifecycle, single-owner teardown, `VolumeShaper` linear fade on user stop, queue drain on completion, immediate teardown on safety hazard, and continuous runtime verification of `AudioTrack.routedDevice`.
- **AudioRouteMonitor:** Observes audio device topology via `AudioDeviceCallback`. Inspects output inventory for conservative physical-output preflight checks (wired, USB, hearing aids, HDMI). Connected Bluetooth/A2DP alone is never a playback blocker: `isBluetoothA2dpOn` cannot establish the active media route. `SoundEngine` uses the actual `AudioTrack.routedDevice` as the authoritative safety gate.

---

## Navigation & UI architecture

The Compose UI is organized into 15 distinct routes defined in `com.pifauna.app.ui.Route`:
1. `Welcome` (`WelcomeScreen`)
2. `Home` (`HomeScreen`) — radial profile selector with dynamic insights
3. `Player` (`PlayerScreen`) — countdown dial, frequency controls, preflight check cards, and screen-lock toggle
4. `Profiles` (`ProfilesScreen`) — full profile library with radial selection
5. `CustomSound` (`CustomSoundScreen`) — custom tone and waveform generator
6. `Timer` (`TimerScreen`) — bounded 5, 10, 15, 30 minute duration selector
7. `Safety` (`SafetyScreen`) — acoustic and device safety overview
8. `Settings` (`SettingsScreen`) — persistent configuration, screen-lock playback toggle
9. `DeviceProtection` (`DeviceProtectionScreen`) — battery, thermal, and volume safety details
10. `BatterySaver` (`BatterySaverScreen`) — battery cutoff policy explanations
11. `PlaybackSafety` (`PlaybackSafetyScreen`) — hearing safety and speaker-only operation
12. `AboutPrivacy` (`AboutPrivacyScreen`) — local storage, no-data-collection disclosure
13. `ChakraLibrary` (`ChakraLibraryScreen`) — seven symbolic chakra associations and insights
14. `ChakraConsole` (`ChakraConsoleScreen`) — experimental chakra tone preparation
15. `AnahataNada` (`AnahataNadaScreen`) — soft audible sweep and relaxation studio

---

## Session lifecycle

### Default (Screen-Lock OFF)
Starting a 5/10/15/30-minute session plays only while the Activity is visible in the foreground. If the user locks the display or navigates away to another app, playback immediately stops.

### Screen-Lock Opt-in (Screen-Lock ON)
1. The user explicitly enables "Continue timed playback with screen locked" in Settings or Player.
2. Toggling the setting alone never begins playback.
3. When the user taps Start while the Activity is in the foreground, `MainActivity` requests `TimedPlaybackService` with `startForegroundService()`.
4. The service promotes to foreground with a `MediaStyle` notification displaying the active profile, remaining time, and an instant Stop action.
5. When the screen locks, playback continues until the user presses Stop, the timer reaches zero, or a safety condition (battery ≤15%, thermal severity, external audio route, audio focus loss) triggers immediate shutdown.
6. If the user unlocks and switches to another app, playback terminates immediately.
7. The service is `START_NOT_STICKY` and will never automatically restart after process termination, reboot, or app force-stop.

---

## Audio routing & Safety Gates

1. **Conservative preflight evaluation:** `AudioRouteMonitor` detects *connected* physical outputs (wired headphones, USB headset, hearing aid, HDMI). `SafetyPolicy.evaluate()` refuses these before start. Bluetooth connection state is deliberately excluded from preflight blocking: even an A2DP-enabled device may be connected while Android routes media through the phone speaker. Actual Bluetooth routing is denied by the runtime AudioTrack gate, not inferred from inventory.
2. **Runtime authorization gate:** Regardless of preflight checks, `SoundEngine` strictly queries `AudioTrack.routedDevice` (`isBuiltInSpeakerRoute()`) before writing any nonzero PCM and on every routing change callback. Only `TYPE_BUILTIN_SPEAKER` is authorized. Any other route immediately aborts audio output.

---

## Security & Permissions

- Manifest requests: `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_MEDIA_PLAYBACK` (API 34+ requirement).
- No internet permission: `android.permission.INTERNET` is strictly prohibited. The app is 100% offline.
- No hardware surveillance permissions: no microphone, camera, contacts, or location access.
- Notifications: uses standard platform `MediaSession` and notification channel for playback controls.
