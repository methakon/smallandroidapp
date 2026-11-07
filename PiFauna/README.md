# Layaswarna (formerly PiFauna)

An offline Android tone generator for short, supervised listening experiments.
Kotlin + Jetpack Compose, minSdk 26, targetSdk 36.

Layaswarna is **not** a pest repellent, a medical device or an ultrasonic emitter.
It produces short, low-level digital tones through the phone's own speaker and
tells you plainly what that does and does not mean.

---

## What the app does

- Fifteen navigable screens: the original eleven routes, About & Privacy, plus Chakra Sound Console,
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

Everything remains offline, without account, internet or analytics. The upcoming screen-lock opt-in uses Android foreground-service/media-playback permissions; no INTERNET, camera or location access. The unreleased v13 Mantra Studio prototype requests microphone permission only when the user explicitly taps Record, and stores accepted voice recordings only in private local app storage.

## Build prerequisites

| Component | Version used for the recorded build |
|---|---|
| JDK | OpenJDK 17 (`/usr/lib/jvm/java-17-openjdk-amd64`) |
| Gradle | 8.13, pinned by the committed wrapper |
| Android Gradle plugin | 8.13.2 |
| Kotlin | 2.0.21 |
| Android SDK Platform | 36 (`compileSdk` / `targetSdk`) |
| Build tools | 36.0.0 |
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

## Release signing and App Bundle

The upload key lives **outside** the repository and is never committed. Gradle
reads it from a properties file located by `LAYASWARNA_KEYSTORE_PROPERTIES`, or
falls back to `~/.config/layaswarna/keystore.properties`:

```properties
storeFile=/absolute/path/to/upload-keystore.jks
storePassword=…
keyAlias=…
keyPassword=…
```

Build the signed bundle:

```bash
./gradlew --no-daemon :app:bundleRelease
# app/build/outputs/bundle/release/app-release.aab
```

If the properties file is absent, the release variant builds **unsigned** and
Gradle logs a warning; it never falls back to a debug key.

Losing the upload key means you can no longer update the app under the same
identity without a Play App Signing key reset. Back up the `.jks` **and** the
properties file (or the password) to at least two separate secure locations, for
example a password manager entry plus an offline encrypted copy. Never store the
keystore in git, in cloud storage without encryption, or in a CI secret that only
exists in one place.

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
runs JVM unit tests, debug APK assembly, instrumentation compilation and lint.
Latest checked marketing-asset commit CI: **passing** — [run 42005250565](https://github.com/methakon/smallandroidapp/actions/runs/42005250565).

The instrumented suite was also executed on two physical devices (Android 11 and
Android 9), 27 instrumented tests reported passing on each device for versionCode 9. Manual TalkBack, contrast and large-font
review, and headset/Bluetooth route testing, still need a hands-on pass. See
[docs/TESTING.md](docs/TESTING.md).

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
- By default, playback stops when the screen turns off or the app is backgrounded.
  Users may explicitly opt in to "Continue timed playback with screen locked" in Settings
  or Sound Player. When enabled, a bounded foreground mediaPlayback service keeps
  playback active with a lock-screen Stop notification until the timer expires or a
  safety limit triggers. Indefinite playback is strictly prohibited.
- Release builds are signed only with existing private, external upload-key properties; missing keystore properties produce an unsigned bundle.

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
├── gradle/wrapper/                         committed Gradle 8.13 wrapper
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

### Mystic UI and Theme implementation

The Android screen layouts are synchronized with the Figma Mystic variants (seven exact Chakra VectorDrawable icons, illuminated wheel, grouped insights, updated headers and actions). See [Figma implementation status](docs/FIGMA-IMPLEMENTATION.md) for exact node IDs and safety-preserving differences. The theme uses the modern-mystic palette in `Theme.kt` with dark violet (`#0B0716`), gold (`#E8C872`), amethyst (`#8B6CFF`), and muted chakra accents.

## Release history and current status

- **versionCode 7 (historical):** Early proof of concept.
- **versionCode 9 / v0.5.0 (historical):** Initial Google Play Internal testing release featuring Mystic UI update, SoundEngine single-owner teardown, VolumeShaper fade, and device safety checks.
- **versionCode 10 (candidate, historical):** Local prototype branch introducing screen-lock timed playback with `TimedPlaybackService`.
- **versionCode 11 / v0.6.0 (active Play Internal release):** Combined release integrating screen-lock foreground media playback and reactive built-in speaker routing verification. Successfully deployed and active on Google Play Internal testing (`edit_id: 11456912848494461374`).
- **versionCode 12 / v0.7.0 (prior development branch):** Reconciles audit gaps: fixes preflight audio routing warnings, captures and commits authentic physical device screenshots to `store-assets/screenshots/`, and extracts hardcoded UI strings into `res/values/strings.xml` in preparation for future Indic localization.

Documentation: [Google Play listing](docs/PLAY-STORE-LISTING.md), [audio routing spec](docs/LAYASWARNA-AUDIO-ROUTING-V11-SPEC.md), [screen-lock playback](docs/SCREEN-LOCK-PLAYBACK.md), [translations](docs/LOCALIZATION.md), [Cloudflare/legal site](docs/LEGAL-SITE-HOSTING.md), [internal release record](docs/PLAY-INTERNAL-DEPLOYMENT.md).

## Security audit status (v12)

The source security audit dated **2026-11-08** documents safety and robustness findings, including a **high-priority suspected media-volume change monitoring gap**. It is **not** a completed live penetration test or a release security sign-off. **Do not publish versionCode 12** until the P0 investigation/remediation and authorized physical-device regression tests are complete. The published v11 Internal Testing release remains untouched.

Read [Security Audit & Penetration-Test Readiness](docs/SECURITY-AUDIT-V12-2026-11-08.md) for findings, priorities, controlled test procedures and evidence requirements.

## Unreleased Mantra Studio prototype — versionCode 13 / 0.8.0

On the isolated branch `feat/layaswarna-mantra-studio-v13`, Mantra Studio adds explicit-permission microphone capture, as-many-as-needed retries, locally titled AAC recordings and counted replay (11, 21, 58, 108) with adjustable quiet gaps (0.5–3 seconds). Playback remains finite (30-minute cap), foreground-only and phone-speaker-routed. This prototype is **not** released or fully device validated. See [Mantra Studio design and QA gates](docs/LAYASWARNA-MANTRA-STUDIO-V13-DESIGN-AND-ACCEPTANCE.md). Published Play Internal v11 is unchanged.
