# Test plan and recorded results

Recorded 2026-11-04 on the machine and device listed below. Nothing in this file
is estimated: every number comes from a Gradle run, a test XML or a device
command.

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

`BUILD SUCCESSFUL`. Artifact:

```
app/build/outputs/apk/debug/app-debug.apk   8,840,901 bytes
sha256 53508a213a8c5a6bad4fc1bccad31db4b2b1c8a0cb13bdc5c5bee1ba4193e4d6
```

**Reproducibility check:** the branch was cloned fresh, built in that clean
checkout, and the resulting APK is byte-identical to a `clean` build in the
working tree (same size, same SHA-256). The committed sources, wrapper and
configuration are therefore sufficient to produce the documented artifact.

Note: an incremental build earlier in the session produced a larger, stale APK
(9,258,649 bytes) because the previous prototype's outputs were still on disk.
Only the clean-build figure above is recorded as evidence.

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

### Instrumented tests — NOT EXECUTED

`:app:connectedDebugAndroidTest` could not run. The device disconnected at USB
level partway through the session (`adb devices` went from `device` to empty and
did not recover after `adb reconnect offline`, an adb server restart and a USB
port reset).

Written and compiled but unexecuted:

- `MainActivityNavigationTest` — 5 tests: launch, home→player, bottom-bar
  navigation, settings→safety, timer screen has no continuous mode.
- `SoundEngineDeviceTest` — 5 tests against a real `AudioTrack`: output
  availability, session completing on its own at 2 s, `stop()` returning in
  under 1.5 s, 12 rapid start/stop cycles without overlap, invalid request
  rejected.

Both compile as part of `:app:assembleDebugAndroidTest` (APK 993,544 bytes).
Marked **not executed**, not "passing".

### Physical-device manual matrix — NOT EXECUTED

The device (Mi A3, Android 11, API 30) was verified reachable earlier in the
session (`adb devices` → `device`, model and API read successfully), but the APK
was never installed because the link dropped before `adb install` completed.

| ID | Scenario | Expected | Result |
|---|---|---|---|
| D01 | Install and launch | No crash, Welcome or Home renders | Not executed |
| D02 | Home → profile → player | Player shows band and Start control | Not executed |
| D03 | Start playback, press Stop | Sound stops, controls recover | Not executed |
| D04 | Press Home during playback | Playback stops, status says so | Not executed |
| D05 | Screen off during playback | Playback stops | Not executed |
| D06 | Incoming call / competing audio | Playback stops on focus loss | Not executed |
| D07 | Connect wired headset or Bluetooth | Start refused, warning shown | Not executed |
| D08 | Media volume above 60% | Start refused | Not executed |
| D09 | Battery at or below 15% | Start refused, running session stops | Not executed |
| D10 | Severe thermal status | Running session stops | Not executed |
| D11 | Timer expiry (5 min) | Playback ends by itself | Not executed |
| D12 | 20 rapid start/stop taps | No crash, no overlap, no ANR | Not executed |
| D13 | Rotate during playback | Playback stops, UI intact | Not executed |
| D14 | TalkBack, 200% font scale | All controls reachable and labelled | Not executed |
| D15 | Airplane mode | App fully functional | Not executed |

## Coverage gaps, stated plainly

- No test executes real acoustic output. Nothing here measures sound pressure,
  frequency response or distortion; that needs an instrumented microphone.
- `AudioRouteMonitor` (external-route detection) has no automated test. It needs
  a device with a headset or Bluetooth sink attached.
- Audio-focus handling is verified only by inspection; simulating a focus loss
  reliably requires a second audio app or a real call.
- Battery and thermal stop paths are unit-tested at the policy level but not
  triggered on hardware.
- Rotation, process death and low-memory recovery are untested.

## Reproducing the device phase

With a device attached and authorised:

```bash
adb devices                       # expect "<serial>  device"
adb shell cmd media_session volume --stream 3 --set 2   # keep it quiet
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb logcat -c && adb shell am start -n com.pifauna.app/.MainActivity
./gradlew --no-daemon --max-workers=2 :app:connectedDebugAndroidTest
```

Then walk the D01–D15 table and replace "Not executed" with the observed result.
A passing digital test must never be recorded as proof of radiated sound.
