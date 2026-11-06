# Layaswarna — audio output routing and external loudspeaker consent

**Status: versionCode 11 development candidate. Not validated on hardware, signed or uploaded to Play.** 7 November 2026.

## Why this exists

The original app refused sound whenever Android listed *any* Bluetooth/headset output as connected. However, available outputs are not the same as the AudioTrack's actual route. Android reports classic Bluetooth A2DP for both loudspeakers and headphones, so neither device type nor a consent checkbox guarantees acoustic safety.

## Policy and UX

- **Phone speaker is default.** The engine requests the phone's built-in speaker and plays silent PCM while checking the actual AudioTrack route before any experimental tone. A Bluetooth device which is connected but is *not* the active output does not itself block phone playback.
- **External loudspeaker is an opt-in experimental mode**, approved only for the current session by two separate checkboxes: the device is a free-standing speaker (not headphones/earbuds/hearing aid), and the user acknowledges hearing risk, agrees to keep volume low and stop on discomfort.
- A2DP does not distinguish physically worn headphones from loudspeakers. User self-classification is an imperfect safeguard; the app cannot guarantee it is correct or that output levels are safe.
- Approved external speaker: max **5 minutes**, **2% digital gain**, **30% Android media volume**. These are conservative software limits, **not calibrated decibels**.
- Wired/LE headsets, Bluetooth SCO, hearing aids, USB/HDMI audio, and unknown/unverified routes remain blocked even after speaker consent. A changing actual route or output ID stops playback.
- Consent does not persist: Stop, completion, new profile or process death resets to phone mode. There is no automatic playback or unlimited session.
- Existing 15% battery cutoff, thermal, audio-focus, screen-lock option, timers and stop controls are retained; app remains offline.

## Editable Figma screens

- [105:48 — phone output default](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=105-48)
- [105:64 — Bluetooth loudspeaker, consent dialog](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=105-64)
- [105:78 — unsafe route blocked](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=105-78)

## Code

- `audio/AudioOutputPolicy.kt`: pure route classification / consent constraints.
- `audio/AudioRouteMonitor.kt`: available outputs are advisory; selected media output is a routing request.
- `audio/SoundEngine.kt`: silent probe, actual AudioTrack route confirmation, active route ID pinning, callback on route changes, no audible PCM on an unverified route.
- `playback/PlayerController.kt`: one-session, process-memory choice and consent.
- `safety/SafetyPolicy.kt`: battery/thermal/route/volume plus external speaker timer and gain gates.
- `ui/screens/PlayerScreen.kt`: Material3 two-check consent dialog.
- `ui/screens/InfoScreens.kt`: settings explanation.
- `app/build.gradle.kts`: versionCode 11, versionName still 0.5.0.

## Mandatory acceptance tests

- Gradle build, JVM tests, instrumentation APK and lint green.
- On Mi A3 Android 11 and Panasonic ELUGA Android 9, test Bluetooth available but audio actually routed to phone; connected headset; active A2DP; device change/unplug; app lock and unlocked Home; notification Stop; timed expiry.
- On API33/36 confirm anticipated media output vs actual `AudioTrack.getRoutedDevice()`, and multi-output `getRoutedDevices()` handling.
- Test consent denial/partial acknowledgement, session reset, >5-minute duration, >2% gain, >30% volume, battery/thermal/focus/audio route safety.
- Verify the user actually has a free-standing speaker, not earbuds/headphones. Headphone acoustic safety cannot be established without external measurement and appropriate testing.
- **Do not upload v11 to Play or deploy amended legal pages until device-level behavior and safety/compliance are reviewed.** VersionCode 10's already signed AAB remains unchanged. Consent cannot exclude non-waivable consumer rights or eliminate liability.

References: Android AudioRouting / AudioTrack / AudioDeviceInfo documentation and WHO safe-listening guidance.
