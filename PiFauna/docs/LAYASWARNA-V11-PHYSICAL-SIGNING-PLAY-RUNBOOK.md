# Layaswarna v11 — Physical-device & signed Internal release execution checklist

**Status: NOT YET EXECUTED on physical devices.** This is a runbook, not evidence.
Branch `feat/layaswarna-audio-route-v11`, package `in.berhampore.dhar.layaswarna`, versionCode 11 / versionName 0.6.0.

## Authorized local Ubuntu host required
This work **cannot** be completed by GitHub-hosted CI. Connect the authorized Xiaomi Mi A3 (Android 11/API 30) and Panasonic ELUGA (Android 9/API 28) using USB ADB on the trusted machine. Never factory-reset, flash firmware, or touch unrelated devices.

```bash
cd /home/swarna-sekhar-dhar/projects/smallandroidapp
git status --short
git fetch origin feat/layaswarna-audio-route-v11
# Preserve all unrelated changes; use a separate worktree for testing:
git worktree add ../layaswarna-v11-acceptance origin/feat/layaswarna-audio-route-v11
cd ../layaswarna-v11-acceptance/PiFauna
adb devices -l
./gradlew --no-daemon :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug
```

Confirm the installed app **is this exact candidate**; do not confuse older v10 APK results with v11 evidence. For each device serial from `adb devices -l`, use:

```bash
adb -s DEVICE_SERIAL install -r app/build/outputs/apk/debug/app-debug.apk
adb -s DEVICE_SERIAL shell am instrument -w -r in.berhampore.dhar.layaswarna.test/androidx.test.runner.AndroidJUnitRunner
```

Record timestamp, device model/API, git commit, test XML/logcat, actual route and audio observations. Device tests may emit tones; keep speakers away from ears and use a low volume. Never wear headphones or place hearing aids in ears during routing tests. Do not simulate hearing safety by turning up an inaudible tone.

## Acceptance scenarios (observe, do not assume)
- UI default screen-lock OFF: user Start then Power/auto lock must Stop.
- Explicit ON + user Start: 5-minute session stays bounded while locked and notification Stop works.
- Switching to another app while unlocked stops audio regardless of ON setting.
- Route: Bluetooth earbud *connected but Android media output set to built-in speaker* permits supervised speaker output only when `AudioTrack.routedDevice` reports `TYPE_BUILTIN_SPEAKER`.
- Active Bluetooth A2DP, wired headset, USB audio, hearing aids and unknown route never receive synthesized test sound. Verify nonzero sound does **not** leak on route transitions.
- Disconnect/switch output during a short supervised 500 Hz session: immediate Stop, no audio leak/duplicate track.
- Verify low battery (<16%), volume (>60%), thermal/focus interruptions; don't deliberately overheat or damage a device. Log scenarios not safely reproducible as **not tested**.
- API 33/36 notification FGS tests may be emulator-backed, but are **not** substitutes for actual headset or route acoustic measurements.
- Check Play mediaPlayback declaration, safety text, TalkBack, 200% font, large/landscape screen.

## Signing, Play Internal-only gate
Proceed **only if all mandatory release safety tests pass** and latest v11 CI is green. Use the existing authorized upload key through the already configured protected properties; never export, print or commit keys/passwords.

```bash
./gradlew --no-daemon :app:bundleRelease
AAB=app/build/outputs/bundle/release/app-release.aab
test -s "$AAB" || exit 1
jarsigner -verify -verbose -certs "$AAB"
sha256sum "$AAB"
```

Also validate with `bundletool validate --bundle="$AAB"`, inspect `versionCode=11` and `versionName=0.6.0` in bundle manifest, and check upload certificate fingerprint against the verified earlier internal release. If signing is missing, the AAB is **NOT** ready for upload.

Deploy source `legal-site/` through the already authorized Cloudflare Pages workflow only after verifying exact legal text; confirm live HTTPS content matches source. Review mandatory mediaPlayback FGS declaration/video and tester list.

With authorized Play Publisher credentials and only after all gates pass: upload versionCode **11** to Google Play **Internal testing only**; read the track back in a *new* publisher edit and record versionCode, release name, AAB digest and testers. Never touch Production, Open or Closed testing. Do not claim successful upload from local file existence.

## Record outcomes
Append signed evidence, actual tester link and run status to `docs/TESTING.md` and `docs/PLAY-INTERNAL-DEPLOYMENT.md`. If any gate fails or cannot be tested, **STOP** and document the blocker; do not upload.
