# Layaswarna v12 — Security audit and penetration-test readiness

**Assessment date:** 2026-11-08  
**Scope:** `methakon/smallandroidapp`, merged development branch `feat/pifauna-phone-audio`, commit `ba1753f06bcee23ec183f8bb79adf970c3e0acf0`, versionCode 12 / versionName 0.7.0.  
**Method:** Manual source-code review via GitHub. **No live exploitation, ADB testing, APK dynamic scanning, dependency CVE scanning or network penetration testing was performed.** These are audit findings and proposed controlled tests, **not a penetration-test pass certificate**.

## Decision

**Do not publish v12 until the P0 volume-monitoring concern is investigated and physical-device regressions pass.** The already-published v11 Internal Testing release must remain unchanged. The mobile app has limited remote attack surface (no declared INTERNET permission), but safety and local-input robustness need attention.

## Findings

| ID | Severity | Finding | Evidence and status |
|---|---|---|---|
| LY-SEC-01 | **HIGH — audio safety** | Foreground-unlocked sessions actively monitor volume-key changes. Raising media volume above 60% immediately stops playback. | **RESOLVED in v13.** Registered `android.media.VOLUME_CHANGED_ACTION` in `SafetyMonitor.kt` + added 250ms polling watchdog in `PlayerController.kt`. Verified on Xiaomi Mi A3 and Panasonic ELUGA I7. |
| LY-SEC-02 | **MEDIUM — audio safety** | Time-of-check/time-of-use gap between audio route verification and buffer write; risk of acoustic leakage to external routes. | **RESOLVED in v13 for Mantra & Synths.** `MantraAudioEngine.kt` primes playback muted at `(0f, 0f)` for up to 300ms until `verifiedSpeakerRoute()` confirms `TYPE_BUILTIN_SPEAKER` before un-muting to `0.35f`. Verified zero external leakage. |
| LY-SEC-03 | **MEDIUM — local robustness** | Preset decode input validation limits: nonfinite, zero, or negative gains; unbounded names. | **RESOLVED in v13.** `PresetCodec.kt` enforces `gain > 0f && gain.isFinite() && gain <= MAX_GAIN`, validates name bounds (1..32 chars), filters delimiter chars, caps presets to 20, and verifies `start <= end`. Tested with 9 JVM unit tests. |
| LY-SEC-04 | **LOW/MEDIUM — fail-open state** | Missing battery broadcast or maximum media volume ratio could treat unavailable state as safe. | **RESOLVED in v13.** `PlayerController.kt` returns `-1` (unknown) on missing battery broadcast; `SafetyPolicy.kt` treats `batteryPercent < 0` as `BlockCode.LOW_BATTERY` fail-closed. `AudioRouteMonitor.kt` returns `1f` (100% fail-closed) when max volume is invalid. |
| LY-SEC-05 | **LOW — repository privacy** | Wrangler transient cache files disclosed cloud account metadata. | **RESOLVED in v13.** Untracked `legal-site/.wrangler/cache/` from git index; added `.wrangler/` and `legal-site/.wrangler/` to root `.gitignore`. |
| LY-SEC-06 | **LOW — release hardening** | `isMinifyEnabled=false` for release; release build may produce an unsigned artifact without signing configuration. | **Pending release workflow.** Release gating ensures unsigned builds are never deployed to Play Console. |
| LY-SEC-07 | **INFO — test fidelity** | Existing instrumentation simulates screen-lock lifecycle directly. | **Expanded in v13.** Real device testing conducted on Xiaomi Mi A3 (API 30) and Panasonic ELUGA I7 (API 28). |

## Source-verified defensive controls

- Manifest does not request Internet, microphone, camera, location, contacts, SMS, or storage permission; it requests foreground-service media playback permissions.
- `TimedPlaybackService` is `android:exported="false"` with no binder, limiting third-party service invocation.
- Exported launcher activity accepts only a checked enum-like `route` extra for navigation; no untrusted URL/file/command execution was observed.
- App declares `allowBackup=false`; local settings use private SharedPreferences; signing materials are configured outside the Git repository.
- `SoundEngine` verifies built-in speaker as the only permitted actual track route, limits PCM parameters and attempts immediate stops for route changes.
- Static legal site defines CSP, X-Content-Type-Options, Referrer-Policy and Permissions-Policy in `legal-site/_headers`. Live header deployment was **not** checked during this assessment.
- GitHub Actions token permissions are explicitly `contents: read`.

## Controlled penetration-test / adversarial QA plan — NOT executed

1. **P0 / volume-key escalation:** With screen-lock setting OFF, start a supervised 5-minute low-level session at ~30% volume; press volume-up beyond 60%; verify prompt audio stop. Repeat with ON. Record ADB logcat, state and real stop latency. Keep speaker away from ears.
2. **Audio-route switch/leak:** Start on built-in speaker, switch to active Bluetooth A2DP/wired/USB during playback, and record audio via a safe line-input measurement setup and route callback timestamps. Do **not** put headphones or hearing aids in/on ears. Distinguish process stop latency from actual acoustic leakage.
3. **Connected-but-unrouted Bluetooth:** With A2DP connected but phone speaker selected, a session should start on speaker only; selecting A2DP as active route must prevent any nonzero PCM. Repeat on API 28, API 30 and API 36 when available.
4. **Android component exposure:** With unprivileged shell on an authorized test device, try starting the non-exported service using `adb shell am startservice -n in.berhampore.dhar.layaswarna/com.pifauna.app.playback.TimedPlaybackService`; expect Android to refuse. Send benign and malformed `route` extras to exported launcher and verify no crashes or playback.
5. **Service lifecycle:** Rapidly lock/unlock, press Home, dismiss task, use lock-screen Stop, terminate the process and repeat Start/Stop. Verify no unexpected restart, audio focus retention or leaked MediaSession.
6. **Local data mutation:** Only in a disposable debug environment, fuzz the preset decoder with invalid names, separators, gain `NaN`, negative/zero gain, invalid frequency ranges and oversized records. Confirm bounded decode, no crash, and never bypass ToneConfig.
7. **Network/site:** Inspect effective installed APK permissions, observe app network activity in a controlled sandbox, and verify headers/TLS on the legal website using non-invasive HEAD/GET requests.
8. **Supply chain:** Run dependency/SBOM and secret-scanning tooling on a disposable clone; evaluate action version pinning and native dependency vulnerabilities. No CVEs have been verified in this assessment.

## Prioritized remediation

**P0 — before next v12 release:** Monitor media volume consistently in both Activity and opted-in service playback and immediately stop at the threshold. Add unit/instrumentation and physical volume-escalation tests. Do not add auto-restart or continuous playback.

**P1:** Obtain quantified route-change acoustic evidence; expand lifecycle and component-boundary tests. Harden `PresetCodec.add/decode`: finite positive gain, valid bounded names, max count, duplicate/range normalization and invalid-record rejection.

**P2:** Treat unavailable battery/volume state as unknown, not safe; preserve compatibility. Remove transient Wrangler cache from tracking and add `.wrangler/` to `.gitignore` without rewriting history absent compelling need. Make unsigned release builds fail at a defined release gate; consider minification and maintain mapping symbols if enabled.

## Release gate and scope limitations

- No confirmed RCE, externally exploitable data leak, auth bypass or credential leak was identified in examined source; this **is not a guarantee of absence**.
- **No live penetration test has been completed by this audit.** Prior passing functional CI is not a security sign-off.
- Have Antigravity run the controlled Android device tests above and attach raw evidence, test/CI URLs and exact SHA; then review fixes before authorizing any v12 AAB upload.
- No publication or alteration of versionCode 11 is authorized by documenting these findings.
