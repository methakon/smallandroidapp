# Layaswarna (formerly PiFauna)

An offline Android tone generator for short, supervised listening experiments.
Kotlin + Jetpack Compose, minSdk 26, targetSdk 36.

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

Everything remains offline, without account, internet or analytics. The upcoming screen-lock opt-in uses Android foreground-service/media-playback permissions; no INTERNET, microphone, camera or location access.

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
- Playback stops when the screen turns off, because there is no service. This is
  intentional.
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

### Latest Mystic UI implementation

The Android screen layouts have been synchronized with the newer Figma Mystic variants (seven exact Chakra VectorDrawable icons, illuminated wheel, grouped insights, updated headers and actions). See [Figma implementation status](docs/FIGMA-IMPLEMENTATION.md) for exact node IDs and safety-preserving differences. VersionCode 7 is historical; versionCode 9 has since been built, signed and rolled out to Internal testing after the Mystic UI and SoundEngine updates, according to the worker's Play read-back.

## Release and Play listing — 2026-11-05

- **Current reported Internal testing release:** versionCode **9**, v0.5.0, targetSdk 36. Not published to Production.
- **SoundEngine:** single-owner teardown, VolumeShaper fade on user stop, natural queue drain, immediate safety-event stop. Tests do not prove click-free audible output.
- **Validation:** 33 JVM tests, 27 device tests each on API 28/API 30, lint 0 errors/27 warnings (worker report).
- **Play graphics:** Figma app icon and feature banner committed and reported uploaded to `en-IN`. Five real screenshots were reported prepared, but not uploaded; their claimed repository path needs verification.
- **Legal site:** Pages `layaswarna-legal.pages.dev` live; `layaswarna.berhampore.in` needs a DNS CNAME. The Play Privacy Policy field remains unset.
- **Video:** Figma Motion 24-second editable design-preview timeline; exported MP4 is not yet on YouTube.
- **Translations:** English (India), Bengali, Hindi, Assamese listing drafts created; import/review pending.

Documentation: [Google Play listing](docs/PLAY-STORE-LISTING.md), [promo video](docs/MARKETING-VIDEO.md), [translations](docs/LOCALIZATION.md), [Cloudflare/legal site](docs/LEGAL-SITE-HOSTING.md), [internal release record](docs/PLAY-INTERNAL-DEPLOYMENT.md).


## Screen-lock timed playback — versionCode 10 candidate (not on Google Play)

The new default-Off option **Continue timed playback with screen locked** is available in Settings and Player. Toggling does not start audio. An explicit Start begins a bounded 5/10/15/30-minute mediaPlayback foreground service, with a lock-screen Stop notification. Safety and duration restrictions remain; ordinary playback still stops when the display locks. Android/OEM may interrupt. Last reported Internal testing release remains versionCode 9. See docs/SCREEN-LOCK-PLAYBACK.md.


## v11 experimental external Bluetooth speaker consent (separate branch)

Development branch: `feat/layaswarna-audio-output-consent-v11`. The verified signed v10 release candidate and the Internal testing track are **untouched**. v11 adds live AudioTrack output verification so connected but unused Bluetooth devices do not unnecessarily block the phone speaker. A one-session, two-acknowledgement consent option allows a user-declared free-standing Bluetooth loudspeaker under stricter 5-minute, 2%-gain and 30%-volume limits. Classic A2DP may represent headphones, so approval is not proof of device type or safe sound pressure. Known headphone, hearing-aid, USB and unverified routes remain blocked. No new data collection or Bluetooth pairing permission. **Not released or hardware-validated.** See [Audio output consent](docs/AUDIO-OUTPUT-CONSENT.md).
