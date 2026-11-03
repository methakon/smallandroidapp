# PiFauna — testing and validation plan

Test scope: pure Kotlin signal tests first; emulator for UI lifecycle; physical Android device for route/output safety. **A passing digital test never proves animal repellency.**

## Automated checks

- `kotlinc app/src/main/java/com/methakon/pifauna/ToneKernel.kt tests/ToneKernelSmokeTest.kt -include-runtime -d /tmp/pifauna-test.jar`
- `java -jar /tmp/pifauna-test.jar`
- `./gradlew :app:assembleDebug` *after a valid Gradle wrapper and Android SDK are installed*.

## Required test matrix

| ID | Scenario | Pass criterion |
|---|---|---|
| T01 | Start 99 Hz or 18,001 Hz | Explicit validation error, no AudioTrack playback |
| T02 | Fixed, sweep, pulse and hop | Generated samples stay in 16-bit bounds; phase continuous across blocks |
| T03 | Stop at each 5/10/20 s option | Hardware playback ends on time within measured tolerance |
| T04 | Press Stop mid-playback | Audible output ceases promptly; controls recover |
| T05 | Home/app switch/incoming call | No background sound; reentry starts stopped |
| T06 | Wired headset/USB audio/Bluetooth available | Start refused or safely stopped; verify device variants |
| T07 | Plug headset or connect Bluetooth mid-test | Route transition does not leak unapproved playback |
| T08 | Media volume above 35% | Playback refused |
| T09 | Save presets | Duplicates/invalid names rejected; max 20 enforced |
| T10 | Journal | Up to 100 entries, newest first; no response can be recorded |
| T11 | CSV export | UTF-8, proper quoting/escaping, user-selected URI; files open in spreadsheet software |
| T12 | Rotation, process death and low-memory | No unintended restart; no crash; journal remains local |
| T13 | TalkBack, font scaling, 320dp screen | Controls read and operate; labels not clipped |
| T14 | Sound measurement | Report amplitude, spectrum and distortion as *uncalibrated* unless lab equipment used |
| T15 | Offline/privacy | Airplane mode works; no INTERNET permission, SDK tracking or analytics |

## Test evidence

Record phone manufacturer/model, OS build/API, physical route, media-volume step, time duration, generated frequency, external calibrated measurement (if available), device logs, app commit SHA, and reviewer. File a separate issue per failure; do not mark device safety verified based only on emulator tests.

## Release-blocking defects

Any accidental headset route, persistent background audio, missing Stop, uncontrolled volume, distress-inducing default settings, incorrect output frequency claim, or health/repellency efficacy claim is a release blocker.