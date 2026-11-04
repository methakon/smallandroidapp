# Layaswarna (formerly PiFauna)

An offline Android tone generator for short, supervised listening experiments.
Kotlin + Jetpack Compose, minSdk 26, targetSdk 35.

Layaswarna is **not** a pest repellent, a medical device or an ultrasonic emitter.
It produces short, low-level digital tones through the phone's own speaker and
tells you plainly what that does and does not mean.

---

## What the app does

- Fourteen navigable screens: the original eleven routes plus Chakra Sound Console,
  Seven Chakra Library and Anahata Nada. Navigation uses separate Material 3
  targets; selection screens share one circular regulator with original vector icons.
- Wheel gestures: single tap or drag/rotate to select, stationary long-press or
  double-tap to activate, plus a labeled accessible button. The selected item is
  highlighted; the center is an information readout, not an Open button.
- Seven symbolic chakra-tone associations (396–963 Hz) with context and evidence
  cautions. Selecting a meditation tone only prepares the Player; sound is never
  started automatically.
- Anahata Nada currently supports preparing short **audible** soft-tone/sweep
  experiments. Pink/white noise remain explicitly marked unavailable until
  audio synthesis and power/thermal validation; they are not mislabeled as tones.
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
adb shell am start -n in.berhampore.dhar.layaswarna/com.pifauna.app.MainActivity
```

## Application ID and verification

Android application ID/Gradle namespace: `in.berhampore.dhar.layaswarna`.
The current Kotlin source code retains its `com.pifauna.app` packages. This is a
different installed app ID from the original PiFauna; user data is **not**
automatically migrated.

CI workflow: [Layaswarna Android validation](../.github/workflows/layaswarna-android.yml)
runs unit tests, debug APK assembly and lint. The first core redesign run passed:
[GitHub Actions run 41890856104](https://github.com/methakon/smallandroidapp/actions/runs/41890856104).
Later changes require their own successful run. Real-device navigation, gesture
and TalkBack testing still needs a physical Android device.

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

## Launcher icon

The Layaswarna launcher icon is an original lotus and audible soundwave design,
using the same visual language as the modern-mystic app theme. The editable
512px master is in [Figma](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=62-25),
and the scalable source is [design/layaswarna-lotus-soundwave.svg](design/layaswarna-lotus-soundwave.svg).
Android uses a separate gradient background, scalable full-color foreground and
an outlined monochrome icon for themed launchers. See
[icon specifications and provenance](docs/LAYASWARNA-APP-ICON.md).
The launcher mask and Android device rendering still require visual QA.

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Test plan and recorded results](docs/TESTING.md)
- [Device validation report](docs/DEVICE-VALIDATION.md)
- [Safety contract and claims policy](docs/SAFETY.md)
- [Figma implementation status](docs/FIGMA-IMPLEMENTATION.md)
- [Lotus + Soundwave app icon — Figma, SVG and adaptive Android resources](docs/APP-ICON.md)

## Design source

Figma: <https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX>. The modern-mystic redesign set has 14 screens in addition to earlier concepts and archives.
Current theme uses dark violet, accessible light text, gold/rose accents and muted seven-chakra node colors. See [design audit and implementation plan](docs/LAYASWARNA-DESIGN-AUDIT-AND-DELIVERY-PLAN.md).
