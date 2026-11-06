# Layaswarna 0.6.0 · versionCode 11 — Combined Release Candidate

**Status:** QA / acceptance pending. **Prepared:** 2026-11-07.
**Feature branch:** `feat/layaswarna-audio-route-v11`.
**Source:** `feat/pifauna-phone-audio` v10 baseline at `a7973a9`.
**Google Play target:** Internal testing ONLY; no upload or rollout has been executed for v11.

## One coordinated release
1. **Screen-lock continuation from v10 source:** user-controlled OFF by default; ON + explicit Start allows a user-timed 5, 10, 15 or 30 minute session during screen lock, with an actionable MediaStyle notification Stop. Switching away with the screen unlocked still stops playback; no auto-resume or endless sessions.
2. **Active-route safety improvement from v11:** check the actual `AudioTrack.routedDevice` for the phone's built-in speaker. Connected-but-unused Bluetooth audio should not, by itself, block a safe phone-speaker session. When actual routing is ambiguous, unknown, or external, playback is denied or immediately stopped. Volume, battery, thermal and focus constraints remain.

**No Bluetooth-speaker, headphone, earbud or hearing-aid output has been approved or enabled.** Those are not a third release feature. A consent-only override is inappropriate without measured acoustic output and validated device identification.

## Candidate identity
- `applicationId`: `in.berhampore.dhar.layaswarna`
- `versionCode`: **11** (staged in Gradle on development branch)
- `versionName`: **0.6.0**
- `minSdk`: 26; `targetSdk`: 36
- Signing: never commit keys; reuse authorized existing upload key only *after* acceptance.
- v10 signed AAB SHA-256 `c8b904e6083077b6476eea2603fde24be3b7b454b5de768167060551c95458c6` is historic evidence **not** a v11 artifact.

## Automated checks
- Prior v11 base commit `7267899`: GitHub Actions run [42289353947](https://github.com/methakon/smallandroidapp/actions/runs/42289353947) passed unit tests, APK build and lint. This **precedes** subsequent v11 fixes and version bump.
- Current branch triggers JDK17/SDK36 Gradle unit tests, debug APK, instrumented-test APK and Android lint.
- Additional Android emulator connected tests are configured for API 30 and 36. Emulator results must not be passed off as physical speaker or route-change evidence.
- Check current CI status against the **latest commit SHA**, record job/run URLs and actual test counts. No claim of passing final CI before that outcome is observed.

## Mandatory physical evidence — MUST PASS
On authorized Xiaomi Mi A3 (API 30) and Panasonic ELUGA (API 28), with sound volume low and no headphones worn:
1. Baseline: v11 build installs cleanly and v10 OFF screen lock stops, ON lock continues, notification Stop works, timer expires; app switch while unlocked stops.
2. Bluetooth device connected but media explicitly routed to **phone speaker**: source must allow bounded speaker playback; verify `AudioTrack.routedDevice` says built-in loudspeaker. Never infer from pairing state alone.
3. **Active Bluetooth** (earbuds, headphones, speaker) and wired headphones/USB: start must fail or emit no synthesized tone. Verify not merely a UI refusal but actual acoustic output; test with headphones away from ears.
4. Earbuds/headset/hearing-aid safety: actual output must be denied regardless of checkbox; do not expose a consent override.
5. Connect/disconnect/switch output **during** active playback; audio must stop, not redirect synthesized PCM. Record precise observed latency and whether buffered sound was audible.
6. Unknown/null active route must fail closed. Audio focus loss, battery threshold, volume >60%, severe thermal (API 29+) and rapid Stop/Start must retain safety.
7. Inspect 200% font scale and TalkBack: route warning, playback Stop, timer, screen-lock opt-in and notification Stop.
8. Confirm there is only one AudioTrack/service, no leaked audio thread, and no startup/crash regressions after task dismissal.

API 33 and API 36: verify mediaPlayback foreground-service/notification behavior on emulator or hardware. Capture actual versions, test XML, screenshot/video and logcat; do not invent test results.

## Remaining delivery actions
1. CI green on the **final** versionCode11 commit; instrumented emulator results reviewed.
2. Physical testing recorded in `docs/TESTING.md`, including exact supported route/OS results, Stop/race evidence and acoustic caveats.
3. Publish revised `legal-site/safety/index.html` using authorized Cloudflare credentials, and verify HTTP 200 with the actual updated content; confirm Play privacy/safety statements match.
4. Play Console mediaPlayback FGS declaration and real-device demonstration evidence must match this binary; existing v10 evidence may be reused only where behavior remains identical and permitted by Play.
5. Create a **new** signed `app-release.aab` with existing protected signing credentials; validate bundletool, jarsigner, package/version and SHA-256.
6. Authorize and upload **only** to Google Play Internal testing with release name `Layaswarna 0.6.0 — Combined Internal Test`; independently verify track/versionCode 11, uploaded bundle hash and tester opt-in.
7. Do not publish to Production, Open, or Closed testing, and never publish without all mandatory hardware gates.

## Safety caveat
Checking `AudioTrack.routedDevice` and route callbacks is a useful defense, not a mathematical proof that queued audio can never reach a newly-routed device. Acoustic testing and platform-specific race testing are required. Fail closed if any tested device cannot reliably report/maintain the actual phone speaker route.
