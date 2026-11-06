# Layaswarna architecture — versionCode 10 source candidate

**Not yet validated or released to Google Play.** The last reported Internal testing build is versionCode 9.

## Component ownership

- MainActivity: Compose UI, SettingsStore, user opt-in, screen lock vs unlocked app switch detection.
- PlaybackRuntime: process singleton with one PlayerController and one SafetyMonitor shared by Activity and foreground Service.
- PlayerController: SoundEngine/ToneKernel, audio focus, safety decisions, audio route monitoring and StateFlow.
- TimedPlaybackService: Android mediaPlayback foreground service for user-initiated timed locked sessions only. It shares the same controller, owns MediaSession and a Stop notification, and runs a one-second watchdog.

## Session lifecycle

**Default OFF:** starting a 5/10/15/30-minute session plays only while the Activity is visible. Screen lock and app switch stop immediately.

**Opted in ON:** Start while visible requests a foreground mediaPlayback service. A MediaStyle notification offers Stop. On display lock, the service, existing engine and safety monitoring persist until Stop, timer expiry or an unsafe condition. Switching apps while the display is unlocked still stops. Service is START_NOT_STICKY and does not resume after boot or process death.

The PCM kernel enforces the fixed duration. VolumeShaper controls a user-stop fade; safety stops are immediate. An atomic teardown claim prevents double-release. No wakelock is requested; Android/OEM may still interrupt.

## Security and validation

Manifest requests only FOREGROUND_SERVICE and FOREGROUND_SERVICE_MEDIA_PLAYBACK; no INTERNET, microphone, camera, location or POST_NOTIFICATIONS permission. The Android platform MediaSession is used for notification controls.

Hardware validation on API28/API30 plus platform checks on API33/API36, lock-screen Stop, external route, battery, thermal, timer and audio focus is mandatory before v10 publication. Google Play requires the foreground-service mediaPlayback declaration and a real-device video. See SCREEN-LOCK-PLAYBACK.md.