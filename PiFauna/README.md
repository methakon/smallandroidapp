# PiFauna

An offline Android tone generator for short, supervised listening experiments.
Kotlin + Jetpack Compose, minSdk 26, targetSdk 35.

PiFauna is **not** a pest repellent, a medical device or an ultrasonic emitter.
It produces short, low-level digital tones through the phone's own speaker and
tells you plainly what that does and does not mean.

---

## What the app does

- Eleven screens matching the Figma design: Welcome, Home, Sound Player, Profile
  Library, Custom Sound, Timer, Safety, Settings, Device Protection, Battery
  Saver, Playback Safety.
- Frequency control from 100 Hz to 18,000 Hz, with four signal shapes
  (steady sine, triangle sweep, pulse, deterministic hop).
- Fixed session lengths of 5, 10, 15 or 30 minutes. There is no continuous mode.
- Six illustrative study bands (mosquito/insect, insects, rodent, dog/cat, bird,
  custom) with per-band cautions and source links.
- Up to 20 locally saved custom presets.
- Digital peak capped at 8% of full scale, with 25 ms fades in and out.

Everything is local. No account, no network, no analytics and **no permissions
at all** — the manifest declares none.

## Build prerequisites

| Component | Version used for the recorded build |
|---|---|
| JDK | OpenJDK 17 (`/usr/lib/jvm/java-17-openjdk-amd64`) |
| Gradle | 8.9, pinned by the committed wrapper |
| Android Gradle plugin | 8.7.3 |
| Kotlin | 2.0.21 |
| Android SDK Platform | 35 (`compileSdk` / `targetSdk`) |
| Build tools | 35.0.0 or newer (34.0.0+ accepted by AGP 8.7) |
| Android device | 8.0 (API 26) or newer |

Point Gradle at your SDK with a machine-local `local.properties` (git-ignored):

```properties
sdk.dir=/absolute/path/to/Android/Sdk
```

## Build and test commands

```bash
cd PiFauna
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME="$HOME/Android/Sdk"

# debug APK
./gradlew --no-daemon --max-workers=2 :app:assembleDebug

# JVM unit tests
./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest

# Android lint
./gradlew --no-daemon --max-workers=2 :app:lintDebug

# instrumented navigation tests (requires a connected device)
./gradlew --no-daemon --max-workers=2 :app:connectedDebugAndroidTest
```

Artifact: `app/build/outputs/apk/debug/app-debug.apk`.

## Install

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.pifauna.app/.MainActivity
```

## Safety behaviour

Playback starts only when all of these hold:

- battery above 15%;
- thermal status below `THERMAL_STATUS_SEVERE` (Android 10+);
- no wired headset, Bluetooth, USB, hearing aid, HDMI or line output connected;
- media volume at or below 60% of maximum.

Playback stops automatically on any of those becoming untrue, and also on audio
focus loss (call, assistant, another media app), when the activity leaves the
foreground, when the timer expires, or when the user presses Stop.

More detail: [docs/SAFETY.md](docs/SAFETY.md).

## Honest limitations

- A rendered 16 kHz sine is a **digital** signal. Many phone speakers roll off
  sharply above ~16 kHz and no phone speaker produces ultrasound. Nothing in
  this app emits or claims ultrasound.
- The 8% digital cap is not a sound-pressure limit. System volume, equalisers and
  hardware gain still govern how loud it is in the room.
- No frequency band here has been shown to repel insects, rodents, birds, dogs or
  cats. The profile names describe study bands, not effects.
- Playback stops when the screen turns off, because there is no service. This is
  intentional.
- Release builds are unsigned (`isMinifyEnabled = false`, no signing config).

## Repository layout

```
PiFauna/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/java/com/pifauna/app/      application code
│       ├── main/res/                       theme, strings, colours
│       ├── test/java/com/pifauna/app/      JVM unit tests
│       └── androidTest/java/com/pifauna/app/  instrumented tests
├── docs/                                   architecture, testing, safety, Figma mapping
├── gradle/wrapper/                         committed Gradle 8.9 wrapper
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── gradlew, gradlew.bat
```

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Test plan and recorded results](docs/TESTING.md)
- [Device validation report](docs/DEVICE-VALIDATION.md)
- [Safety contract and claims policy](docs/SAFETY.md)
- [Figma implementation status](docs/FIGMA-IMPLEMENTATION.md)

## Design source

Figma: <https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX> (11 frames).
Colour and layout tokens are reproduced in `ui/Theme.kt` and `ui/Components.kt`.
