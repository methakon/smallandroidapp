# Safety contract and claims policy

PiFauna is an acoustic experiment. This document states what the code enforces,
what it cannot enforce, and what the product is not allowed to claim.

## 1. What the code enforces

| Rule | Implementation | Verified by |
|---|---|---|
| Digital peak ≤ 8% of full scale | `ToneKernel.renderInto` clamps every sample; `ToneConfig` rejects `gain > 0.08` | `ToneKernelTest.every mode stays inside the digital ceiling` |
| No clicks | 25 ms fade in/out, plus 12 ms fades on hop steps and 10 ms on pulse edges | `ToneKernelTest.session fades in and out instead of clicking` |
| Frequency inside 100–18,000 Hz | `ToneConfig` init block; UI sliders share the same constants | `ToneKernelTest.invalid configurations are rejected` |
| Session always ends | `durationMs` validated to ≤ 30 min; the audio loop stops at `totalFrames` | `ToneKernelTest.samples past the end of the session are silent` |
| Battery cutoff | `SafetyPolicy` blocks at ≤ 15%, re-checked on every battery broadcast | `SafetyPolicyTest.battery at or below the cutoff blocks playback` |
| Thermal cutoff | Blocks at `THERMAL_STATUS_SEVERE` and above (API 29+) | `SafetyPolicyTest.severe thermal status blocks playback` |
| Speaker only | External outputs detected via `AudioDeviceInfo`; blocked at start and re-checked on route change | `SafetyPolicyTest.external audio routes are refused` |
| Volume ceiling | Blocked above 60% of maximum media volume | `SafetyPolicyTest.media volume above the limit is refused` |
| Foreground only | `MainActivity.onStop` → `controller.onAppBackgrounded()` | device test, `docs/TESTING.md` |
| Interruption handling | Any audio-focus loss stops playback | device test |
| No permissions | Empty `<manifest>` permission set | `aapt2 dump badging` shows only the app's own dynamic-receiver permission |

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
- playback that continues after Stop, background, timer expiry or focus loss;
- any missing Stop control during playback;
- any efficacy or ultrasound claim;
- a signed release build without a signing configuration.
