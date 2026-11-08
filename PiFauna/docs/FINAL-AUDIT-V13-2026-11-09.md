# Layaswarna v13 — Independent Final Source & Release-Readiness Audit

**Audit date:** 2026-11-09 (Asia/Kolkata)  
**Application:** `in.berhampore.dhar.layaswarna` — versionName `0.8.0`, versionCode `13`, minSdk `26`, targetSdk/compileSdk `36`  
**Audited baseline:** commit [`f4d9b86bfc072d38a7190073e137d1acd8b860a7`](https://github.com/methakon/smallandroidapp/commit/f4d9b86bfc072d38a7190073e137d1acd8b860a7) on `feat/layaswarna-mantra-studio-v13`; reviewed live GitHub source, workflow results and available logs.  
**Subsequent audit fixes:** `AudioFocusInterruptionPolicy.kt` and regression tests (shared by tone and mantra playback); additional API29+ mantra thermal safety checks. These **post-baseline changes require fresh CI and device retests**.  
**Method:** GitHub static code inspection, direct review of existing GitHub Actions logs and tests, historical worker evidence review, document reconciliation. **No new hands-on phone testing, signed AAB inspection, APK dynamic exploitation, measured SPL/route-leak test, external dependency CVE scan, Play Developer API read-back or legal certification** was performed by this audit.

## Decision: DEVELOPMENT MERGED; v13 PLAY RELEASE NOT YET APPROVED

- [PR #3](https://github.com/methakon/smallandroidapp/pull/3) is **MERGED**, not draft: merged on **2026-11-08 05:34:55 UTC** into development base `feat/pifauna-phone-audio` at merge commit `d102a3d2efbd8b1ebba2a0d3e1d78c49c000e18b`. Earlier documents stating “PR #3 remains draft” are superseded.
- The original audited feature-head commit `f4d9b86` passed the [GitHub Android validation run #42450085563](https://github.com/methakon/smallandroidapp/actions/runs/42450085563): Android debug build + JVM tests + lint **success**, and **32/32** instrumented tests each on API30 and API36 emulators. Run #42450081906 also succeeded.
- The older `ab2a3e6` worker report stated **65/65 JVM tests**, **32/32** instrumentation on Mi A3 (API30) and ELUGA (API28), and a real **15-second recording plus 11 individual replays**. These are historical reported physical tests; do not elevate them to proof that `f4d9b86` or the post-audit fixes passed device tests.
- **Google Play Internal Testing versionCode 11 / 0.6.0** is the last Play track version recorded by repo documentation. No verified v13 signed bundle, Play Console submission or live-track read-back was found for this audit. Do not infer Play publication from a GitHub merge.
- **Figma alone is the authoritative design source.** The 12 editable v13 Figma artboards and related Compose screen mapping are documented in [FIGMA-V13-SOURCE-OF-TRUTH.md](FIGMA-V13-SOURCE-OF-TRUTH.md). Historical Adobe Express SVG/PNG are **not** accepted v13 design assets or blockers.

## Verification matrix — exact scope of evidence

| Gate | Evidence and result | Audit classification |
|---|---|---|
| Repository identity and Git state | Repo `methakon/smallandroidapp`, merged PR #3, feature-head `f4d9b86`, merged base `d102a3d` | **Verified from GitHub** |
| Release configuration | `versionCode 13`, `versionName 0.8.0`, `minSdk 26`, `targetSdk 36` | **Verified in Gradle** |
| Debug build/JVM tests/lint (pre-audit-fix) | GitHub run #42450085563 `android` job passed | **Verified CI; count of individual JVM cases not independently recounted from XML** |
| Emulator instrumentation (pre-audit-fix) | API30 **32/32** and API36 **32/32**, logs end `BUILD SUCCESSFUL` | **Verified CI** |
| Physical devices, earlier milestone | Worker reported Mi A3 API30 **32/32**, ELUGA API28 **32/32** on `ab2a3e6` | **Historical worker report, not fresh audit** |
| Live microphone / preview / delete | Worker reported 15-second capture, preview, titled save, restart, deletion | **Historical physical workflow report** |
| Long japa sets | Arithmetic/choice unit coverage; no proof of physically completed 108/1,188/2,268/6,264/11,664 plays | **Not accepted as full E2E verification** |
| Seven silence gaps | `100/250/500/750/1000/2000/3000ms` in policy/UI; not all measured on both phones | **Code verified; physical timing pending** |
| Local resume checkpoint | `JapaCheckpoint`, JSON checkpoint saving/restoring and tests added at `f4d9b86` | **Code verified; cross-process and crash-matrix testing pending** |
| Figma ↔ Compose parity | 12 editable Figma artboards, home implemented; Mantra Studio currently uses one long composed screen, not all Figma states | **Partially complete** |
| Digital amplitude & speaker route | Synth tone limits and per-player speaker-route checks exist | **Source-level verification; zero external acoustic leakage NOT certified** |
| Release artifact / Play | No signed v13 AAB/jarsigner/bundletool or Play Internal track read-back in this audit | **Not verified** |

## Findings and required disposition

### LY-V13-01 — HIGH: Audio focus loss classification (FIXED IN SOURCE; retest required)

At audited baseline, **both** `PlayerController.onFocusChanged` and `MantraAudioEngine.requestPlaybackFocus` used `change <= AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK`, where CAN_DUCK = **−3**. Android's other loss codes are **−2** (TRANSIENT) and **−1** (LOSS). That predicate did **not** stop playback on −2/−1. The new shared `AudioFocusInterruptionPolicy.mustStop(change)` treats every negative focus-loss callback as a Stop, and adds unit tests for −1/−2/−3 and nonnegative callbacks.

**Evidence:** audited Java/Kotlin sources and [Android AudioManager reference](https://developer.android.com/reference/android/media/AudioManager). **Disposition:** fixed in follow-up feature-branch source; require green final-head CI and real phone focus-interruption retest. Do not label this physically accepted yet.

### LY-V13-02 — HIGH: Mantra thermal enforcement gap (FIXED IN SOURCE; retest required)

The v13 voice player polled media volume, battery, route and 30-minute duration, but its dedicated mantra safety loop did **not** check API29+ thermal status. Synth playback already used thermal checks through `SafetyPolicy`. Added guarded `PowerManager.currentThermalStatus` checks both before each mantra playback segment and in the ongoing watchdog. API29+ read failure blocks; API26–28 do not provide this API and remain subject to their platform limitations.

**Disposition:** fixed in follow-up source; require CI/physical thermal-state simulation and correct API28 fallback verification. No unsafe heat tests on humans or devices.

### LY-V13-03 — MEDIUM: Checkpoint durability and truthfulness

`MantraStore` writes a *single app-wide* `checkpoint.json`, not independent checkpoints for every saved chant. On every completed chant, the Compose UI calls `saveCheckpoint` synchronously (main-thread file I/O). The store returns `null` on a write failure, but some UI paths display “Progress saved” **without checking the return value**. Checkpoint restore verifies the selected recording ID and valid sets/gap/count and requires an existing audio file; this is a good baseline but not a complete crash-consistency guarantee. Recording deletion explicitly clears a matching checkpoint.

**Disposition:** improve UI error reporting/atomicity, define whether one active checkpoint or per-recording checkpoints is the product requirement, benchmark frequent disk writes, and test save, force-stop, relaunch and deletion on both physical devices. Never claim crash-safe saved progress without the corresponding evidence.

### LY-V13-04 — MEDIUM: Audibility / route transition evidence

`MantraAudioEngine` starts at zero `MediaPlayer` volume until a verified built-in-speaker route is observed and then sets player gain to 0.35. On route changes the engine attempts to stop. The tone path checks actual `AudioTrack.routedDevice`. **Code review alone cannot certify that no samples reach an external device during an asynchronous route transition**. Also, 0.35 MediaPlayer volume and the synthesizer's `0.08` digital peak setting are different controls; neither is calibrated dB SPL.

**Disposition:** test Bluetooth A2DP, wired/USB, phone speaker active while Bluetooth connected, and route handoff at low volume with controlled output capture. Avoid “guaranteed zero voice leakage” or “hearing safe” claims.

### LY-V13-05 — MEDIUM: Japa count/timing physical acceptance

Correct core product invariant: **1 set = 108 chants**; outer choices **1,11,21,58,108 sets**, totaling **108,1,188,2,268,6,264,11,664**. Inner silence values **0.1,0.25,0.5,0.75,1,2,3s**. Estimate `audioDurationMs × totalChants + gapMs × (totalChants − 1)`. A preview plays once. Each user-started foreground session ends within 30 minutes; users must **explicitly** Continue. The worker's earlier 11-play validation is not equivalent to one 108-play set.

**Disposition:** deterministic stress tests across 108-boundary and all set totals, STOP during audio and gap, 100/250ms timing tests on target devices, overnight/non-autostart assertion and exact continuation at next count. Large set validation may use accelerated deterministic replay-count tests plus real physical spot checks; disclose methodology.

### LY-V13-06 — MEDIUM: Figma/UI acceptance and platform compatibility

The Figma-only v13 Master has **12 editable designs** and several reusable components; Compose Home has been redesigned and the Mantra UI has the core feature/permission dialog, but does not yet match every separate Figma state. Verify 360dp layout, landscape, 200% text, TalkBack announcement/semantics for two rings, 48dp targets (seven pause choices also have a 4+3 chip fallback), and delete/permission/settings behavior. `minSdk 26` allows installation on Android 8; **voice replay explicitly fails closed below API28**, so this is a disclosed limitation, not feature parity on API26–27.

### LY-V13-07 — MEDIUM: Play release integrity, privacy & policy

`AndroidManifest.xml` requests `RECORD_AUDIO` and foreground-service media playback; it does **not** declare `INTERNET`, camera or location permission. `allowBackup=false` and app-private storage support offline voice handling. The newly added microphone workflow needs reviewed bundled/published privacy wording and Google Play **Data Safety**/app-content responses before release. The source currently builds **debug** in CI. Release variant has `isMinifyEnabled = false` and can be **unsigned** if upload-key properties are absent; no freshly signed v13 AAB/upload evidence is in scope.

**Official reference:** [Google Play target API guidance](https://developer.android.com/google/play/requirements/target-sdk) (API36 target expected for new updates starting 2026-08-31, satisfied in Gradle). A valid `targetSdk` alone does not certify Play approval. Do not upload or deploy legal-site changes during this audit.

### LY-V13-08 — LOW: Source/documentation drift

Older architecture, README, v12 security audit, Play documentation and v13 design plan still contained: “v12 current branch”, “no microphone permission”, “PR #3 draft”, “11/21/58/108 repetitions”, four gaps, Adobe as an active deliverable or the assertion of guaranteed zero audio leakage. This report supersedes conflicting *status* statements while retaining historical milestone reports for chronology. Documents must explicitly distinguish reported physical tests from verified CI.

### LY-V13-09 — LOW: Tests and source quality remaining

CI lint/build passes at `f4d9b86`; Kotlin emitted nonfatal warnings in `SoundEngine.kt` (constant-false condition) and deprecated `Notification.Action.Builder` constructor in `TimedPlaybackService.kt`. Investigate warnings without changing release behavior accidentally. Review dependency CVEs and release-mode R8/signing separately; neither was independently audited here.

## Release-gate checklist (all REQUIRED before v13 Play Internal)

- [ ] Post-audit fix commit passes complete JVM, lint, debug APK and API30/API36 instrumentation CI.
- [ ] Retest permanent/transient/can-duck audio-focus loss for synth and mantra paths on physical API28/API30 devices.
- [ ] Retest the new mantra thermal check on API29+ and verify API28 fallback.
- [ ] Validate physical route transitions, output muting/zero-leak *as far as observable*, volume escalation, STOP and background lock interruptions.
- [ ] Prove exact boundary counters and small gaps, and crash-safe explicit Continue across relaunch; report where accelerated tests were substituted for hours-long real audio.
- [ ] Resolve checkpoint I/O and misleading “Progress saved” failure status; decide single vs multiple active practices.
- [ ] Complete Figma-to-Compose visual/accessibility acceptance; no need for Adobe Express.
- [ ] Review final bundled and public privacy disclosures, `RECORD_AUDIO` consent, Play Data Safety and store listing.
- [ ] Create and verify release-signed v13 AAB with the existing upload key, verify release mode, manifest, signature and hashes; check Play Console edit status before any action.
- [ ] Obtain **explicit user authorization** for Google Play Internal upload, followed by separately authorized external rollout as needed.

## Branch policy following the historical merge

PR #3 is already merged into **development base** `feat/pifauna-phone-audio`, not Google Play. This audit report and new audio-focus/thermal changes were developed on `feat/layaswarna-mantra-studio-v13` *after* that merge. A **separate follow-up draft pull request [#5](https://github.com/methakon/smallandroidapp/pull/5)** now proposes transferring the post-merge safety fixes and these audit documents to the development base. Keep it in draft until verification; do not automatically merge or release.

## Related documents

- [Architecture](ARCHITECTURE.md) · [Testing](TESTING.md) · [Safety](SAFETY.md)
- [Figma v13 source of truth](FIGMA-V13-SOURCE-OF-TRUTH.md)
- [Historical v12 security audit](SECURITY-AUDIT-V12-2026-11-08.md)
- [Play Internal deployment record](PLAY-INTERNAL-DEPLOYMENT.md)
- [Antigravity issue #4](https://github.com/methakon/smallandroidapp/issues/4) · [Audit remediation draft PR #5](https://github.com/methakon/smallandroidapp/pull/5)
