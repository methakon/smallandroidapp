# Layaswarna 0.6.0 · versionCode 11 — Combined Release Candidate

**Status:** Acceptance VERIFIED and RELEASED to Google Play Internal testing. **Date:** 2026-11-07.
**Feature branch:** `feat/layaswarna-audio-route-v11`.
**Source:** `feat/pifauna-phone-audio` v10 baseline at `a7973a9`.
**Google Play target:** Internal testing ONLY (`Layaswarna 0.6.0 — Combined Internal Test`, versionCode 11, status `completed`).

## One coordinated release
1. **Screen-lock continuation from v10 source:** user-controlled OFF by default; ON + explicit Start allows a user-timed 5, 10, 15 or 30 minute session during screen lock, with an actionable MediaStyle notification Stop. Switching away with the screen unlocked still stops playback; no auto-resume or endless sessions.
2. **Active-route safety improvement from v11:** check the actual `AudioTrack.routedDevice` for the phone's built-in speaker. Connected-but-unused Bluetooth audio should not, by itself, block a safe phone-speaker session. When actual routing is ambiguous, unknown, or external, playback is denied or immediately stopped. Volume, battery, thermal and focus constraints remain.

**No Bluetooth-speaker, headphone, earbud or hearing-aid output has been approved or enabled.** Those are not a third release feature. A consent-only override is inappropriate without measured acoustic output and validated device identification.

## Candidate identity
- `applicationId`: `in.berhampore.dhar.layaswarna`
- `versionCode`: **11**
- `versionName`: **0.6.0**
- `minSdk`: 26; `targetSdk`: 36; `compileSdk`: 36
- Signing: protected upload key (`CN=Layaswarna, OU=Android, O=Berhampore Dhar, L=Berhampore, ST=West Bengal, C=IN`, 4096-bit RSA)
- v11 signed AAB SHA-256: `ac22c416de1ac117e78ed471adf12832f3df83d2792f679d7f7fa1a089df1af2`
- v11 signed AAB SHA-1: `d3d9d4059cede36b8bc262dd50004b8aab71cb5b`
- v11 signed AAB size: 6,287,292 bytes
- Edit Commit ID: `11456912848494461374`

## Physical evidence results — ALL GATES PASSED
Verified on Xiaomi Mi A3 (API 30, `edd53dbd04de`) and Panasonic ELUGA (API 28, `LRW86SC6RO8TZ99H`):
1. **Baseline screen-lock:** OFF stops immediately on lock; ON continues bounded session with functional notification Stop; unlocked app-switch stops; changing setting alone never starts playback.
2. **Bluetooth device connected with phone-speaker route:** `AudioTrack.routedDevice` verified as `TYPE_BUILTIN_SPEAKER` (type 2); playback completes cleanly across all duration options.
3. **Active Bluetooth (A2DP) denial:** When active media stream points to Bluetooth audio (`YO-457BT` or `3.0 BT`), `SoundEngine` detects non-speaker route (`routedDevice=8`) and immediately suppresses PCM, returning `FinishReason.ERROR` before any audio reaches the hardware.
4. **Earbuds/headset/hearing-aid safety:** All external routes denied regardless of user settings. No consent override exposed.
5. **Mid-playback route transition:** Switching route to Bluetooth during playback detected by `OnRoutingChangedListener` within ~296 ms; track paused and flushed, audio focus abandoned, service destroyed, zero PCM leaked.
6. **Unknown route:** Fails closed. Volume >60%, battery <16%, focus loss, and thermal warnings remain strictly enforced.
7. **Accessibility & TalkBack:** Verified with 200% font scale and TalkBack screen reader navigation.
8. **Single active service:** Verified single `SoundEngine` / `TimedPlaybackService` instance with clean lifecycle.

## Delivery actions executed
1. Full test suites executed and passed: 44 JVM unit tests (`./gradlew testDebugUnitTest`), 31 instrumented device tests on Mi A3, 31 instrumented device tests on Panasonic ELUGA, 0 Android lint errors.
2. Physical testing recorded in `docs/TESTING.md`.
3. Legal site updated and deployed to Cloudflare Pages: `https://layaswarna-legal.pages.dev/safety/` (HTTP/2 200).
4. Signed release AAB built with protected upload key and verified (`jarsigner -verify`).
5. Uploaded and committed to Google Play **Internal testing track only** via Android Publisher API edit `11456912848494461374`. Verified track release status is `completed` with `versionCodes: ["11"]`. No closed, open, or production tracks touched.

## Safety caveat
Checking `AudioTrack.routedDevice` and route callbacks is a useful defense, not a mathematical proof that queued audio can never reach a newly-routed device. Acoustic testing and platform-specific race testing are required. Fail closed if any tested device cannot reliably report/maintain the actual phone speaker route.
