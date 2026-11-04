# PiFauna Android

A small, offline, foreground-only Android tone generator. Kotlin + Jetpack Compose, min API 26.

## Build
Install Android Studio with Android SDK 35 and JDK 17. Open repository root, allow Gradle sync, and run the `pifauna` app configuration. With a compatible Gradle installation: `gradle :pifauna:assembleDebug`. The repository does not yet include a Gradle wrapper or a signed release APK.

## Features implemented
- Adjustable 200–16,000 Hz sine tone; this is a *digital signal setting*, not a guarantee of speaker output.
- Timed 5/10/20/30-minute sessions; default 10 minutes.
- Digital amplitude capped at ~8% of full-scale.
- Audio focus handling, foreground-only playback, stop button.
- Battery cutoff at <=15%, severe Android thermal warning stop (API 29+).
- No internet, location, microphone, background service or wakelock.

## Important limitations
- Not scientifically validated as a repellent; never claim guaranteed effects.
- Hardware/system volume and audio effects can alter actual sound pressure; a digital limiter cannot guarantee hearing or speaker safety.
- High-frequency output can be inaudible yet uncomfortable; keep distance from ears, children and pets.
- The UI is an initial working implementation, not yet pixel-matched to the full 11-screen [Figma design](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX).
- Build, instrumentation, battery and thermal tests have not yet been executed in this environment.

See [architecture](../docs/ARCHITECTURE.md) and [testing](../docs/TESTING.md).
