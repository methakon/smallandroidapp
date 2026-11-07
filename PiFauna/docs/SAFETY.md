# Safety contract and claims policy

PiFauna is an acoustic experiment. This document states what the code enforces,
what it cannot enforce, and what the product is not allowed to claim.

## 1. What the code enforces

| Rule | Implementation | Verified by |
|---|---|---|
| Digital peak ≤ 8% of full scale | `ToneKernel.renderInto` clamps every sample; `ToneConfig` rejects `gain > 0.08` | `ToneKernelTest.every mode stays inside the digital ceiling` |
| Digital fades (not an acoustic guarantee) | 25 ms fade in/out, plus 12 ms fades on hop steps and 10 ms on pulse edges | `ToneKernelTest.session fades in and out instead of clicking` |
| Frequency inside 100–18,000 Hz | `ToneConfig` init block; UI sliders share the same constants | `ToneKernelTest.invalid configurations are rejected` |
| Session always ends | `durationMs` validated to ≤ 30 min; the audio loop stops at `totalFrames` | `ToneKernelTest.samples past the end of the session are silent` |
| Battery cutoff | `SafetyPolicy` blocks at ≤ 15%, re-checked on every battery broadcast | `SafetyPolicyTest.battery at or below the cutoff blocks playback` |
| Thermal cutoff | Blocks at `THERMAL_STATUS_SEVERE` and above (API 29+) | `SafetyPolicyTest.severe thermal status blocks playback` |
| Speaker only (preflight & runtime) | Preflight checks via `AudioRouteMonitor` detect wired headphones, USB audio, hearing aids, and active A2DP without blocking unrelated Bluetooth. Runtime verification via `AudioTrack.routedDevice` strictly enforces `TYPE_BUILTIN_SPEAKER` before writing PCM and aborts on route changes. | `SafetyPolicyTest.external audio routes are refused`, `ActiveOutputRoutePolicyTest`, device validation |
| Volume ceiling | Blocked above 60% of maximum media volume | `SafetyPolicyTest.media volume above the limit is refused` |
| Screen-lock opt-in (default OFF) | Normal sessions stop at `Activity.onStop`; explicit opt-in uses an Android `mediaPlayback` foreground service (`TimedPlaybackService`) with ongoing lock-screen Stop control | Verified on Xiaomi Mi A3 (API 30) & Panasonic ELUGA (API 28); CI on API 30 & API 36 |
| Interruption handling | Any audio-focus loss immediately stops playback | Verified in instrumented test suite |
| Offline with narrow FGS permissions | `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_MEDIA_PLAYBACK` only; no `INTERNET`, microphone, camera, contacts, or location | Manifest inspection; release verification |

## 2. What the code cannot enforce

- **Actual sound pressure.** PCM amplitude is not dB SPL. System volume, the
  audio HAL, speaker protection limits and equalisers all sit between this code
  and the air. A phone at maximum volume can be loud regardless of the 8% cap.
- **Actual frequency response.** A phone speaker is not a calibrated transducer.
  Output above roughly 16 kHz is typically attenuated by an unknown amount.
- **Route fidelity.** Android may resample, down-mix or re-route audio. Route
  checks reduce the chance of unintended output, they do not guarantee fidelity.
- **Hearing safety.** No software can make a speaker safe for an ear held against
  it. The app relies on the user keeping the volume low and the phone away from
  ears.

## 3. Claims policy

**Allowed:** "generates a short digital test signal", "offers illustrative study
bands", "plays through the built-in speaker", "saves presets locally".

**Not allowed** in code, UI copy, documentation, store listing or commit
messages:

- "repels mosquitoes", "prevents bites/malaria/dengue", "eliminates rodents";
- "ultrasonic", "produces 20–70 kHz", "ultrasound mode";
- "safe for pets", "inaudible to humans", "harmless";
- "clinically proven", "scientifically validated", "guaranteed coverage".

The Figma source contains a frame labelled with a 12–20 kHz band. The app caps
the setting at 18,000 Hz and never describes any output as ultrasonic; see
`docs/FIGMA-IMPLEMENTATION.md`.

## 4. Operating guidance shown in the app

- Keep media volume moderate; start at the lowest audible level.
- Children and pets may hear sound that is inaudible to an adult.
- Effectiveness is not established for any listed group.
- Stop immediately on any sign of discomfort.
- Do not use sound to punish, frighten, herd or exclude an animal.
- Prefer physical exclusion, sanitation and approved repellents for real pest
  problems.

## 5. Evidence base

The profile notes cite these sources; they describe hearing ranges and research
context, not repellency of this app:

- Electronic mosquito repellents (Cochrane review):
  <https://www.cochrane.org/evidence/CD005434_electronic-mosquito-repellents-preventing-mosquito-bites-and-malaria-infection>
- Mosquito acoustic behaviour: <https://pmc.ncbi.nlm.nih.gov/articles/PMC7820424/>
- Dog and cat hearing: <https://pmc.ncbi.nlm.nih.gov/articles/PMC4212972/>
- Bird hearing: <https://pmc.ncbi.nlm.nih.gov/articles/PMC9104101/>

## 6. Release blockers

Do not publish as a pest-control or health product. Treat these as blockers:

- any external audio route that plays without an explicit user action;
- playback that continues after Stop, timer expiry or focus loss, or when ordinary screen-lock mode is OFF;
- any missing Stop control during playback;
- any efficacy or ultrasound claim;
- a signed release build without a signing configuration.

## Opt-in screen-lock sessions (versionCode 10 — not yet released)

- Default **OFF**; toggling does not start audio, and the setting is disabled during a live session.
- Explicit user Start while the Activity is visible begins a bounded `mediaPlayback` foreground service; locking may continue playback until the 5/10/15/30-minute limit, notification Stop, safety interruption or process termination.
- Switching apps on an unlocked display ends the audio. Removing the app task, audio-focus loss and unsafe output route stop. No boot/process-death automatic restart.
- Android normal permissions are `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_MEDIA_PLAYBACK` only; no INTERNET, microphone, camera or location. A platform MediaSession backs the notification.
- 8% digital gain is not a calibrated sound-pressure guarantee. The Android OS or OEM may interrupt even opted-in playback.
- Lock-screen, notification, thermal, battery, route, volume and timer behavior must pass physical-device validation before versionCode 10 is uploaded.
