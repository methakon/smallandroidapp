# Test plan and recorded results

Recorded 2026-11-04 on the machine and device listed below. Nothing in this file
is estimated: every number comes from a Gradle run, a test XML or a device
command.

## Layaswarna 0.5.0 — current verification

App ID `in.berhampore.dhar.layaswarna`, versionCode 5. This section supersedes
the PiFauna-era results further down for anything touched by the Layaswarna
redesign; the older sections are kept as the record of that phase.

Command (exactly as specified for release review):

```bash
cd PiFauna
./gradlew --no-daemon \
  :app:clean :app:testDebugUnitTest :app:assembleDebug \
  :app:assembleDebugAndroidTest :app:lintDebug
```

| Check | Result |
|---|---|
| Clean build | **BUILD SUCCESSFUL** |
| JVM unit tests | **33 tests, 0 failures, 0 errors** |
| Debug APK | 8,890,081 bytes, `sha256 18b86f70…`, `in.berhampore.dhar.layaswarna` v0.5.0 |
| Instrumentation compile | **passes** (`assembleDebugAndroidTest`) |
| Android lint | **0 errors, 13 warnings** (8 dependency-currency, 1 unused resource, 1 old target API, 1 obsolete SDK qualifier, 1 backup-rules note, 1 icon note) |
| CI | **passing** — [run 41903358930](https://github.com/methakon/smallandroidapp/actions/runs/41903358930) |

### Instrumented suite — 20 tests, both devices

`adb -s <serial> shell am instrument -w -r in.berhampore.dhar.layaswarna.test/androidx.test.runner.AndroidJUnitRunner`

| Device | Android | Result |
|---|---|---|
| Xiaomi Mi A3 | 11 (API 30) | **OK (20 tests)** |
| Panasonic ELUGA_I7_EE | 9 (API 28) | **OK (20 tests)** |

| Class | Tests | Covers |
|---|---|---|
| `AppLaunchTest` | 2 | launch to RESUMED, activity recreation |
| `MainActivityNavigationTest` | 5 | wordmark, home→player, bottom bar, settings→safety, timer has no continuous mode |
| `RadialGestureTest` | 7 | tap selects only, press highlights immediately, long press activates once, double tap activates once, **drag rotation changes selection without activating**, **rotation cancels a pending long press**, centre readout is not an action |
| `SoundEngineDeviceTest` | 5 | real `AudioTrack` output, session completes on its own, stop returns promptly, 12 rapid start/stop cycles, invalid request rejected |

### Defects found and fixed in this pass

1. **CI red.** Three instrumented-test imports did not resolve
   (`assertDoesNotExist`, `longClick`, `doubleClick`) so `assembleDebugAndroidTest`
   failed on CI while the app itself built. Fixed; the workflow compiles
   instrumentation, which is why the app-only local build had missed it.
2. **Selection latency.** With `onDoubleClick` set, `combinedClickable` cannot
   report a single tap until the double-tap window expires, so every node tap
   applied its highlight roughly 300 ms late. The selector now also selects on
   pointer-down.
3. **Anahata Nada selection reset.** The chosen relaxation sound was held in
   screen-local state and reset on navigation. It is now hoisted beside the
   profile and chakra selections and survives route changes.

### Verified by inspection, not by test

- TalkBack traversal order and spoken labels, 200 % font scale, and contrast
  ratios need a manual accessibility pass; the semantics are present
  (role, selected state, content description, labelled action button) but were
  not assessed with a screen reader.
- Wired-headset, Bluetooth and USB-audio refusal, and a real audio-focus
  interruption, remain untested on hardware.
- Small-screen landscape layout is unverified.

## Environment

| Item | Value |
|---|---|
| OS | Ubuntu, kernel 7.0.0-42-generic |
| JDK | OpenJDK 17.0.20.1 (`/usr/lib/jvm/java-17-openjdk-amd64`) |
| Gradle | 8.9 (committed wrapper) |
| Android Gradle plugin | 8.7.3 |
| Kotlin | 2.0.21 (KGP); Gradle's embedded Kotlin 1.9.23 |
| compileSdk / targetSdk / minSdk | 35 / 35 / 26 |
| Android SDK platforms installed | android-27, 34, 35, 36 |
| Build tools | 34.0.0 and 36.0.0 (AGP selected the platform default) |
| Test device | Xiaomi Mi A3 (`laurel_sprout`), Android 11, API 30, 5.7 GB RAM |
| Device connection | USB ADB (`edd53dbd04de`) |

## Commands actually run

```bash
cd PiFauna
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME="$HOME/Android/Sdk"

./gradlew --no-daemon --max-workers=2 :app:assembleDebug
./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest
./gradlew --no-daemon --max-workers=2 :app:lintDebug
./gradlew --no-daemon --max-workers=2 \
  :app:assembleDebug :app:assembleDebugAndroidTest \
  :app:testDebugUnitTest :app:lintDebug
```

## Results

### Build — PASS

`BUILD SUCCESSFUL` from a `clean` build. Artifact:

```
app/build/outputs/apk/debug/app-debug.apk   8,840,901 bytes
sha256 d9fc3580fd14d7841f13542d0f86f2399f31bfab158f130442fa5c034bbe1145
```

**Reproducibility:** two independent `clean` builds of this revision produced
byte-identical APKs (same size, same SHA-256), and a fresh clone of the previous
revision also built byte-identically, so the committed sources, wrapper and
configuration are sufficient to reproduce the artifact.

Note: an incremental build earlier in the session produced a larger, stale APK
because the previous prototype's outputs were still on disk. Only clean-build
figures are recorded as evidence.

`aapt2 dump badging` confirms:

```
package: name='com.pifauna.app' versionCode='3' versionName='0.3.0'
minSdkVersion:'26'   targetSdkVersion:'35'   compileSdkVersion='35'
launchable-activity: name='com.pifauna.app.MainActivity'
uses-permission: com.pifauna.app.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION
```

The only permission listed is the one the AndroidX activity library injects for
its own non-exported receivers. **No user-facing permission is declared.**

### JVM unit tests — PASS

`:app:testDebugUnitTest`, XML results read directly from
`app/build/test-results/testDebugUnitTest/`:

| Suite | Tests | Failures | Errors | Skipped |
|---|---|---|---|---|
| `ToneKernelTest` (signal synthesis) | 9 | 0 | 0 | 0 |
| `PresetCodecTest` (preset storage) | 8 | 0 | 0 | 0 |
| `SafetyPolicyTest` (safety gate) | 7 | 0 | 0 | 0 |
| `TimeFormatTest` (countdown formatting) | 4 | 0 | 0 | 0 |
| **Total** | **28** | **0** | **0** | **0** |

What these actually prove:

- every mode stays below 8% of full scale and never wraps or clips;
- a 1 kHz request measures 797–803 zero crossings over 0.8 s (≈ 1 kHz);
- chunked rendering is bit-identical to one-shot rendering, so buffer size
  cannot change the output;
- sessions fade in and out and are silent past their end;
- pulse mode is silent outside its on-window; hop mode fades at every step;
- invalid frequency, gain and duration are rejected at construction;
- the safety gate blocks low battery, severe thermal, external routes and high
  volume, and reports the most serious problem first;
- preset storage round-trips, rejects duplicates, enforces the 20-item cap and
  refuses records that would widen a safety limit.

### Android lint — PASS (0 errors, 11 warnings)

`0 errors, 11 warnings`. Warnings are dependency-currency notices
(`GradleDependency`, `OldTargetApi`), one `DataExtractionRules` note on
`allowBackup="false"` (backup is already disabled outright), and
`ObsoleteSdkInt` on the `mipmap-anydpi-v26` folder needed for the adaptive icon.

One real defect was found and fixed: `PowerManager.removeThermalStatusListener`
was called without an API 29 guard, which would have thrown `NoSuchMethodError`
on Android 8–9. Lint reported it as an error and the guard is now in place.

### Instrumented tests — PASS on both devices

Run on the Panasonic ELUGA_I7_EE (Android 9, API 28) and the Xiaomi Mi A3
(Android 11, API 30) with the AndroidJUnitRunner addressed directly, because
Gradle's `connectedDebugAndroidTest` hung on device selection while one device
was reconnecting:

```bash
adb -s <serial> shell am instrument -w -r \
  -e class com.pifauna.app.AppLaunchTest,\
com.pifauna.app.MainActivityNavigationTest,\
com.pifauna.app.audio.SoundEngineDeviceTest \
  com.pifauna.app.test/androidx.test.runner.AndroidJUnitRunner
```

| Suite | Tests | Android 9 (API 28) | Android 11 (API 30) |
|---|---|---|---|
| `AppLaunchTest` | 2 | **OK (2)** | **OK (2)** |
| `MainActivityNavigationTest` | 5 | **OK (5)** | **OK (5)** |
| `SoundEngineDeviceTest` (real `AudioTrack`) | 5 | **OK (5)** | **OK (5)** |

### Physical-device matrix — executed on both devices

Full evidence, measurements and the second device's status are in
[DEVICE-VALIDATION.md](DEVICE-VALIDATION.md).

| ID | Scenario | Result |
|---|---|---|
| D01 | Install and launch | PASS — no crash, UI renders |
| D02 | Home → profile → player | PASS |
| D03 | Start playback, press Stop | PASS — track released, focus abandoned, 0 audio threads |
| D04 | Press Home during playback | PASS — playback stopped |
| D05 | Screen off during playback | PASS — playback stopped |
| D06 | Incoming call / competing audio | Not executed (needs a second audio source) |
| D07 | Connect wired headset or Bluetooth | Not executed (no such device attached) |
| D08 | Media volume above 60% | PASS — refused before playback |
| D09 | Battery at or below 15% | PASS — refused at 12% with an explicit message |
| D10 | Severe thermal status | Not executed (would require heating the device) |
| D11 | Timer expiry | PASS — engine completed a 2 s session unattended |
| D12 | Rapid start/stop cycles | PASS — 12 cycles, no overlap, no crash, no ANR |
| D13 | Rotate during playback | PASS — playback stopped, display settings restored |
| D14 | TalkBack, 200% font scale | Not executed |
| D15 | Airplane mode | Not executed (app declares no network permission) |

## Coverage gaps, stated plainly

- No test measures acoustic output. Nothing here establishes sound pressure,
  frequency response or distortion; that needs a calibrated microphone.
- `AudioRouteMonitor` (external-route refusal) is unit-tested at the policy level
  but never exercised against a real headset or Bluetooth sink.
- A real audio-focus interruption was not reproduced; focus *acquisition* and
  *abandonment* were observed in `dumpsys audio`.
- The thermal stop path runs only on API 29+ and was not triggered on hardware.
- Process death and low-memory recovery are untested.
- Only one of the two attached devices could be reached; the second is documented
  as blocked in DEVICE-VALIDATION.md.
A passing digital test must never be recorded as proof of radiated sound.
