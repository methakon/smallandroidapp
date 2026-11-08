# Testing and recorded results

> **Latest audit — 2026-11-09.** See [FINAL-AUDIT-V13-2026-11-09.md](FINAL-AUDIT-V13-2026-11-09.md). PR #3 has **already merged** into development; v13 was **not** verified as shipped to Play Internal. History below remains intact and scoped to its original commit.

## v13 / 0.8.0 — final-audit baseline

- **Reference commit:** `f4d9b86bfc072d38a7190073e137d1acd8b860a7` (November 8, 2026), prior to follow-up audit fixes.
- **GitHub Actions:** [run #42450085563](https://github.com/methakon/smallandroidapp/actions/runs/42450085563) and [run #42450081906](https://github.com/methakon/smallandroidapp/actions/runs/42450081906) completed successfully.
- **Android Gradle job:** `:app:testDebugUnitTest`, `:app:assembleDebug`, `:app:assembleDebugAndroidTest`, `:app:lintDebug` passed. Commit history reports 71 JVM tests, but the exact total was **not independently derived from the unit XML** in this audit.
- **API30 emulator:** `connectedDebugAndroidTest` **32/32 passed** (per CI log; zero skipped/failed).
- **API36 emulator:** `connectedDebugAndroidTest` **32/32 passed** (per CI log; zero skipped/failed).
- **Physical hardware:** the older Antigravity `ab2a3e6` report states Mi A3 API30 **32/32** and ELUGA API28 **32/32**, plus 15-second captured audio, preview/title/storage/relaunch/delete and 11 single chant replays. These are **historical worker claims**, not new physical results for `f4d9b86`, the merged development commit or newer fixes.
- **Cautions:** 11 replays ≠ one full japa set of 108. Exact 108-boundary, 11/21/58/108-set progress, all seven subsecond/second gaps, multi-session 30-minute continuation and actual route leakage remain explicit acceptance work.

### Newly introduced post-merge safety fixes — pending acceptance

The final source audit found that both `PlayerController` and `MantraAudioEngine` compared focus-change codes incorrectly, stopping only for −3 (CAN_DUCK), not −2 (TRANSIENT) or −1 (LOSS). Both now use shared `AudioFocusInterruptionPolicy.mustStop(change)` with new tests covering all three values. Mantra engine also adds API29+ `PowerManager.currentThermalStatus` checks before and during replay.

The new source must pass **fresh JVM/build/lint/emulator CI and physical negative-focus/thermal tests** before release; the prior green baseline does not certify these changes. No signed versionCode13 release AAB or Google Play upload was verified.

### Minimum final Android / audio test checklist

1. Run all JVM tests/lint/debug assembly and API30/API36 emulator instrumentation at the **exact final SHA**.
2. Repeat on Mi A3 API30 and ELUGA API28 with real route change/focus-loss callbacks; verify permanent/transient/can-duck and Bluetooth/wired/USB output transitions safely.
3. Measure 100/250/500/750/1000/2000/3000ms *silence after recording completion*, including Stop during a gap.
4. Use deterministic multi-set counting test harness for 108/1188/2268/6264/11664 completions plus real-speaker spot checks; do not claim all hours ran physically if they did not.
5. Kill/restart app with private checkpoint, change gap/sets, delete a recording and verify invalidation, missing-media recovery and **explicit-only** Continue.
6. Test 360dp layouts, font scaling, TalkBack, 48dp selections and microphone denial/revocation; validate against the Figma-only 12-screen Master.
7. Validate a signed release build, bundletool/jarsigner/manifest, AAB hash and required Play Console disclosures **only after authorization**.

---


## Verified Release Candidate — versionCode 11 (0.6.0) (recorded 2026-11-07)

**Branch:** `feat/layaswarna-audio-route-v11`  
**Application ID:** `in.berhampore.dhar.layaswarna`  
**Target Release:** versionCode 11 / versionName 0.6.0 (compileSdk 36, targetSdk 36, minSdk 26)  
**Track:** Google Play Internal testing only  

| Check | Result |
|---|---|
| Android SDK | compile 36 / target 36 / minimum 26 |
| JVM Unit Tests | **44 passed, 0 failures, 0 skipped** (`./gradlew testDebugUnitTest`) |
| Android Lint | **0 errors** (`./gradlew lintDebug`) |
| Instrumentation — Xiaomi Mi A3 (API 30, `edd53dbd04de`) | **31 passed, 0 failures** (`AppLaunchTest`: 2, `MainActivityNavigationTest`: 7, `SoundEngineDeviceTest`: 4, `SoundEngineConcurrencyTest`: 7, `TimedPlaybackServiceDeviceTest`: 4, `RadialGestureTest`: 7) |
| Instrumentation — Panasonic ELUGA I7 EE (API 28, `LRW86SC6RO8TZ99H`) | **31 passed, 0 failures** (`AppLaunchTest`: 2, `MainActivityNavigationTest`: 7, `SoundEngineDeviceTest`: 4, `SoundEngineConcurrencyTest`: 7, `TimedPlaybackServiceDeviceTest`: 4, `RadialGestureTest`: 7) |
| Release AAB validation/signature | jarsigner verified PASS (`CN=Layaswarna, OU=Android, O=Berhampore Dhar, L=Berhampore, ST=West Bengal, C=IN`, 4096-bit RSA) |
| Release AAB SHA-256 | `ac22c416de1ac117e78ed471adf12832f3df83d2792f679d7f7fa1a089df1af2` |
| Release AAB Size | 6,287,292 bytes |
| Live legal deployment | `https://layaswarna-legal.pages.dev/safety/` (HTTP/2 200, disclosed v11 active route & hearing protection policy) |
| Play Console Internal Track | Published & Committed: `Layaswarna 0.6.0 — Combined Internal Test`, status `completed`, edit ID `11456912848494461374` |

### Physical Device Hardware Route Acceptance (Verified on Hardware)

1. **Active Bluetooth Denial Gate (Requirement 3):**
   - With Bluetooth audio device actively selected for media playback (`YO-457BT` A2DP on Mi A3, `3.0 BT` A2DP on Panasonic ELUGA, `TYPE_BLUETOOTH_A2DP` = 8), `SoundEngine` detected non-speaker route before emitting any tone:
     `W PiFaunaEngine: route unverified or not the built-in speaker (attempts=0, routedDevice=8, name=...); suppressing PCM`
   - Playback was cleanly refused with `FinishReason.ERROR`. No synthesized audio frames were emitted to the external device.
2. **Connected Bluetooth with Phone Speaker Routing (Requirement 2):**
   - When Bluetooth devices were connected but Android media output was explicitly configured to the built-in phone loudspeaker (`TYPE_BUILTIN_SPEAKER` = 2), `SoundEngine` verified `AudioTrack.routedDevice` and allowed playback.
   - All 31 tests completed cleanly with full duration bounding on both devices.
3. **Mid-Stream Route Transition (Requirement 5):**
   - During active speaker playback, toggling route to Bluetooth immediately triggered `AudioRouting.OnRoutingChangedListener`:
     `W PiFaunaEngine: AudioTrack route changed away from built-in speaker`
   - The engine instantly paused `AudioTrack`, abandoned audio focus, and destroyed `TimedPlaybackService` within ~296 ms latency. Zero sound leaked to Bluetooth.
4. **Screen-Lock Playback (Requirement 1):**
   - Default OFF: screen lock immediately terminates playback.
   - Setting ON: user-started timed session continues across screen lock with active MediaSession notification and functioning Stop action.
   - App switch while unlocked: immediately stops audio.
   - Toggling setting alone: never starts audio.

## Release Candidate — versionCode 10 (recorded 2026-11-07)

| Check | Result |
|---|---|
| Android SDK | compile 36 / target 36 / minimum 26 |
| JVM tests | 37 passed (`./gradlew test`) |
| Instrumentation — Xiaomi Mi A3 (API 30, `edd53dbd04de`) | `TimedPlaybackServiceDeviceTest` (4 passed), `MainActivityNavigationTest` (7 passed) |
| Instrumentation — Panasonic ELUGA I7 EE (API 28, `LRW86SC6RO8TZ99H`) | `TimedPlaybackServiceDeviceTest` (4 passed), `MainActivityNavigationTest` (7 passed) |
| Android lint | 0 errors (`./gradlew lintDebug`) |
| Release AAB validation/signature | jarsigner verified PASS (CN=Layaswarna, OU=Android, O=Berhampore Dhar, L=Berhampore, ST=West Bengal, C=IN) |
| Release AAB SHA-256 | `c8b904e6083077b6476eea2603fde24be3b7b454b5de768167060551c95458c6` |
| Real device demonstration video | `store-assets/layaswarna_v10_screen_lock_demo.mp4` (2.0 MB, physical Mi A3 screen recording) |
| Live legal deployment | `https://layaswarna-legal.pages.dev/privacy/` and `https://layaswarna-legal.pages.dev/safety/` (HTTP 200, disclosed v10 screen-lock permissions & opt-in) |

### Acceptance results verified on hardware

- **OFF (default):** verified `defaultOffModeStopsWhenScreenLocks` — screen lock immediately terminates audio.
- **Toggling setting alone:** verified `changingSettingAloneNeverStartsPlayback` — never starts audio.
- **ON:** verified `explicitOnModeAllowsPlaybackToContinueWhenScreenLocks` — user-started session continues across screen lock with active MediaSession notification and functioning Stop control.
- **App switch:** verified `switchingAppsWithScreenUnlockedStillStopsPlaybackEvenIfOptedIn` — leaving app with screen unlocked still terminates audio immediately.
- **Single active service:** verified no overlapping `SoundEngine` or duplicate foreground service instances.

## Earlier versionCode 9 and legacy test records

# Test plan and recorded results

## Current release — versionCode 9 (reported 2026-11-05)

| Check | Result |
|---|---|
| Android SDK | compile 36 / target 36 / minimum 26 |
| JVM tests | 33 passed |
| Instrumentation — Panasonic ELUGA, API 28 | 27 passed, 0 failures/fatal exceptions |
| Instrumentation — Xiaomi Mi A3, API 30 | 27 passed, 0 failures/fatal exceptions |
| Android lint | 0 errors, 27 warnings |
| Release AAB validation/signature | bundletool PASS; jarsigner PASS |
| Release SHA-256 | `c75661316630e5d8c1f5e18fe94d00ef1d3703360b54cd14a49cc43fa3c5f77e` |

The new version uses a claim-based AudioTrack teardown protocol, an explicit ~25 ms VolumeShaper user-stop fade, a bounded natural-completion drain, immediate stops on safety hazards, and additional concurrency-focused device tests. The tests **do not record or analyze the acoustic output**, so click-free sound is not yet objectively established. Additional QA: Android API 36 hardware/emulator, TalkBack, 200% text, external wired/Bluetooth/USB routes, genuine audio focus/thermal, and real device screen capture.

The remaining sections are historical records and retain their original results.

Recorded 2026-11-04 on the machine and device listed below. Nothing in this file
is estimated: every number comes from a Gradle run, a test XML or a device
command.

## Historical Layaswarna 0.5.0 — versionCode 7, Android API 36

Recorded 2026-11-05. Google Play rejected versionCode 6 because it targeted
API 35; Play now requires API 36. This section covers the rebuild.

### Toolchain change

| Item | Before | After | Why |
|---|---|---|---|
| compileSdk / targetSdk | 35 / 35 | **36 / 36** | Play requirement |
| Android Gradle plugin | 8.7.3 | **8.13.2** | AGP 8.9.1+ is required to compile against API 36 |
| Gradle wrapper | 8.9 | **8.13** | AGP 8.13 requires Gradle 8.13 |
| Kotlin | 2.0.21 | 2.0.21 (unchanged) | no need to move |
| minSdk | 26 | 26 (unchanged) | — |

Gradle was upgraded because AGP versions declare a minimum Gradle; it is not
optional decoration. `--warning-mode all` produced **no** deprecation output on
the new pair.

### Results

| Check | Result |
|---|---|
| `:app:testDebugUnitTest` | **33 tests, 0 failures, 0 errors** |
| `:app:lintDebug` | **0 errors, 23 warnings** (all version-available / KTX / unused-resource notices) |
| `:app:assembleDebug` | success — `app-debug.apk`, 8,909,191 bytes |
| `:app:assembleDebugAndroidTest` | success |
| `:app:bundleRelease` | success — `app-release.aab`, 6,253,463 bytes |
| `bundletool validate` | pass (exit 0) |
| `jarsigner -verify` | pass, 0 unsigned entries |
| Instrumented tests, API 28 (ELUGA) | **OK (20 tests)** |
| Instrumented tests, API 30 (Mi A3) | **OK (20 tests)** |
| Fatal exceptions in logcat | 0 on both devices |

### Android 16 behaviour review

- **Edge-to-edge is no longer optional** for targetSdk 36. `MainActivity` now
  calls `enableEdgeToEdge(...)` with light system-bar icons on transparent
  bars, so the layout does not shift when a device updates to Android 16.
  `Scaffold` already applied `innerPadding`, so content stays clear of the
  bars; screens inside `PfScreen` scroll vertically and survive the shorter
  viewport.
- **Predictive back is on by default** and `onBackPressed` is no longer
  dispatched. The manifest sets `android:enableOnBackInvokedCallback="true"`;
  navigation uses Compose `BackHandler`, which registers through
  `OnBackInvokedDispatcher`, so back still pops the stack.
- **Orientation restrictions are ignored on large screens.** The app never
  declares `screenOrientation`, so nothing to remove. All screens are
  scrollable columns, which keeps content reachable in landscape.
- **Audio behaviour:** no targeting-36 audio change applies. The existing
  focus/lifecycle/route rules are untouched.

### Native symbols and deobfuscation

The bundle ships four `libandroidx.graphics.path.so` entries — prebuilt,
stripped libraries from AndroidX, not code compiled here. The release block now
declares `ndk { debugSymbolLevel = "SYMBOL_TABLE" }`, which is the correct
setting for any native code this project compiles; because the only `.so`
arrive prebuilt, AGP produces no symbol metadata for them and Play's
"add debug symbols" note remains informational, not blocking. Deobfuscation is
not a factor: `isMinifyEnabled = false`, so no obfuscated names exist and no
mapping file is expected.

### What this build could not test

- **No Android 16 / API 36 device is attached.** Both test phones are API 28
  and API 30, so the enforced edge-to-edge and predictive-back behaviour was
  reasoned from the documented rules and the code, not observed on Android 16
  hardware. This is the main residual risk and should be checked on an API 36
  device or emulator before production.
- Thermal-status gating (`POWERManager` thermal listener) needs API 29+, so it
  runs on the Mi A3 but not the ELUGA.

### Harness note

Instrumented tests require the screen to be on. On the Mi A3 the display was
`Dozing` / `state=OFF` at the start of a run and 10 Compose tests failed with
`IllegalStateException: No compose hierarchies found`; the identical APK passed
all 20 once `svc power stayon true` plus `KEYCODE_WAKEUP` held the screen awake.
Treat that message as a device-state signal, not a code defect.

## Layaswarna 0.5.0 — versionCode 5 (historical)

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
| Gradle | 8.13 (committed wrapper) |
| Android Gradle plugin | 8.13.2 |
| Kotlin | 2.0.21 (KGP) |
| compileSdk / targetSdk / minSdk | 36 / 36 / 26 |
| Android SDK platforms installed | android-27, 34, 35, 36 |
| Build tools | 36.0.0 |
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
