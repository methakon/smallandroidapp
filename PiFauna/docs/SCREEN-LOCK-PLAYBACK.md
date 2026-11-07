# Timed screen-lock playback — Layaswarna (versionCode 11 / 12)

**Status:** Developed as a versionCode 10 prototype, integrated with speaker routing verification in **versionCode 11 (0.6.0)**, hardware-tested on Xiaomi Mi A3 and Panasonic ELUGA, and released to Google Play Internal testing. Ongoing development continues in versionCode 12 (0.7.0).

## User interaction

- Default **OFF**. Both Settings and Player show **Continue timed playback with screen locked**. Toggling the option alone never starts sound.
- **ON + Start:** a foreground mediaPlayback service starts while the app is visible and shares the single existing PlayerController/SoundEngine. Screen-off/lock may continue the user-selected 5, 10, 15 or 30 minute session.
- Stop from player or the lock-screen media notification, timer expiry, audio-focus loss, unsafe battery/volume/temperature/route, task removal or process termination ends playback. The UI, controller and notification must not diverge.
- Switching apps while the display is on still stops the session. No automatic restart on reboot, process death or a resume command; Android/OEM power management can interrupt playback.
- Existing 8% peak PCM limit, 60% media-volume gate, 15% battery cutoff, thermal guard (API29+), built-in-speaker-only output and session-limit policy remain.

## Files and design

- `playback/PlaybackRuntime.kt`: one process-local controller and safety monitor shared by Activity and Service.
- `playback/TimedPlaybackService.kt`: unexported, `START_NOT_STICKY` mediaPlayback foreground service; Android platform MediaSession/notification Stop; independent one-second watchdog.
- `MainActivity.kt`: distinguishes lock/screen-off from unlocked app switching at `onStop`.
- `SettingsStore.kt`: persisted opt-in defaults to false.
- `ui/PiFaunaApp.kt`, `screens/InfoScreens.kt`, `screens/PlayerScreen.kt`, `screens/BrowseScreens.kt`: switch, explanation, timer copy.
- Figma OFF [102:48](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=102-48), ON [102:73](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=102-73), notification [102:98](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=102-98). These are editable designs; actual Android media controls vary by OS.

## Android and Play requirements

- Manifest `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, service `foregroundServiceType=mediaPlayback`; no INTERNET or microphone permission.
- TargetSdk 36. Complete Play Console **App content → Foreground service permissions → mediaPlayback** declaration describing the user-initiated timed sound, interruption impact and a **real-device demonstration video** (enable toggle, Start, lock, see notification and Stop). The earlier Figma animated design preview cannot substitute for actual-app evidence.
- Review bundled policy `app/src/main/assets/legal/privacy.html` and public `legal-site/`. The modified static site must be redeployed to Cloudflare Pages; repository edits do not update the live website.
- Do not change production/closed/open testing or expose signing credentials.

## Mandatory acceptance tests — Antigravity

1. Run `./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug` on Ubuntu and CI; fix every compiler/lint error.
2. On Mi A3 API30 and ELUGA API28 confirm OFF mode stops on automatic lock and hardware Power button. ON continues through lock with media notification/Stop, and stops on selected timer.
3. On both devices verify Home with screen ON still stops, reopening app reattaches single audio state, Stop from Player and notification works, and quick lock/unlock/start/stop does not leak audio threads or duplicate the track.
4. Verify focus-loss/incoming call, wired/BT/USB route, volume, battery threshold and severe-thermal stop; API28 has no thermal listener.
5. Use API33 and API36 hardware/emulator for notification visibility, Android foreground-service start restrictions, process removal and accessibility/TalkBack/large fonts.
6. Record actual output and failures. A passing unit test does not prove physically click-free sound. Produce a real-device mediaPlayback declaration video.
7. After acceptance, build/sign v10 AAB with the existing upload key and **request explicit release authorization** before uploading to Internal testing only.
