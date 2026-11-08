# Layaswarna v13 — Figma-only design master and Compose implementation contract

**Design authority:** Figma only, selected by the user on 2026-11-08.  
**File:** [Layaswarna Figma Design](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX)  
**Page:** [Layaswarna v13 · Figma Master](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=121-48)  
**Version:** 0.8.0 / versionCode 13, draft PR #3; published Play Internal v11 unchanged.  
**Source branch:** `feat/layaswarna-mantra-studio-v13`.

## Scope and Adobe retirement

Adobe Express and the old generated Adobe SVG/PNG exports are **not** selected design options, not release deliverables, and **not the source of truth**. The historical files in `PiFauna/design/adobe-express/` and `PiFauna/docs/ADOBE-EXPRESS-DESIGN-SYSTEM.md` remain for traceability only; do not import, deploy, implement or rely on their outdated japa counts or pause options. Do not work on Adobe Express or gate v13 on Adobe assets. If removal is later requested, do it in a separate archival cleanup rather than overwriting history.

## Figma master — individually editable artboards

| Order | User journey | Figma node | Android state |
|---|---|---|---|
| 01 | New quiet home, Mantra hero and four feature tiles | [123:48](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=123-48) | New `HomeScreen` Compose navigation committed on v13 |
| 02 | Mantra Studio landing and saved-chant preview | [123:84](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=123-84) | Existing `MantraStudioScreen` combined prototype; separate sections require styling pass |
| 03 | Runtime microphone permission explainer | [123:109](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=123-109) | Permission request exists; design explainer screen not fully integrated |
| 04 | Recording with visible stop and 90-second prototype cap | [123:133](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=123-133) | Capture engine exists; state-specific visual implementation pending |
| 05 | Preview the take, title, save/retry | [124:68](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=124-68) | Single take preview, save and retry exist; visual styling pending |
| 06 | Multiple private recording entries | [124:112](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=124-112) | Local `MantraStore` exists; catalog styling pending |
| 07 | Original yantra setup dial, outer sets and inner gap | [122:48](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=122-48) | `ConcentricJapaDial` exists and follows these selectable values |
| 08 | Live progress: sets, within-set chants and overall count | [122:119](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=122-119) | Engine counts, UI progress and Stop present; Figma visual parity pending |
| 09 | Saved confirmation | [124:141](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=124-141) | Save feedback exists; success-state design not fully integrated |
| 10 | 30-minute stop; manual Continue | [124:164](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=124-164) | Explicit Continue exists in Compose state; crash/process-death persistence still a release blocker |
| 11 | Destructive deletion confirmation | [124:189](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=124-189) | Confirmed delete exists; styling pending |
| 12 | Accessible expanded controls for five set values and seven pause choices (4+3 row) | [138:93](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=138-93) | Compose fallback split into 4+3 responsive FilterChip rows; typography/device QA pending |

The pre-existing screens in the Figma file remain historical design references; do not silently replace or remove them. The master page uses editable text, Figma vector geometry, reusable brand/nav/card/button components, **not flattened UI screenshots**. Its original radial geometry is inspired by geometric diagrams; do not copy third-party fictional/motion-picture glyphs.

## Binding product rules (unchanged)

- Outer concentric ring = **1, 11, 21, 58 or 108 sets** of exactly **108** chant plays each.
- Total chant counts = **108, 1,188, 2,268, 6,264, 11,664**.
- Inner ring independent pauses: **0.1, 0.25, 0.5, 0.75, 1, 2 or 3 seconds** (integer milliseconds 100 / 250 / 500 / 750 / 1000 / 2000 / 3000).
- A preview is one playback only, never 108 chants.
- Estimate `totalDurationMs = (recordingMs × selectedSets × 108) + ((selectedSets × 108 - 1) × gapMs)`.
- Counts must advance only when a recording completes; gaps aren't counted. Display current set, 0..108 within-set completions, and total completed / target.
- 30-minute **per user-started foreground session** cap remains in effect; manual Continue only. Never auto-play on relaunch or in the background. Process-restart progress retention and recording ID validation require verification before release.
- Consent-based microphone use; app-private AAC/M4A takes; no INTERNET permission, no covert recording, no unintended external output. Voice playback route verification must be demonstrated on hardware.
- Audio output status must be grounded in observed routing; a static Figma badge is illustrative and not a safety proof.
- Design must account for ≤360dp widths, 200% font scaling, TalkBack and touch targets ≥48dp. Seven silence choices should have two-row accessible chip fallback; do not rely on 7 tiny arc sectors alone.

## Layout tokens — match Android `PiFaunaColors`

| Purpose | Hex |
|---|---|
| Base canvas | `#0E0D17` |
| Secondary background | `#181523` |
| Main card | `#231F2E` |
| Feature hero | `#292435` |
| Accent gold | `#DEC588` |
| Secondary lavender | `#947FCD` |
| Primary text | `#F5F2F7` |
| Secondary text | `#BEB6CA` |
| Border/divider | `#625C70` |
| Warning/recording highlight | `#FFA9B2` |

Keep the Figma screen width at 390dp and enforce responsive Compose layout for smaller device widths. Maintain existing Home / Profiles / Settings bottom navigation as the currently functioning tabs. Mantra Studio and other features are reached from Home; do not invent additional functional bottom tabs.

## Antigravity / developer implementation checklist

1. **Do not use Adobe assets.** Compare Android v13 screens to the above Figma nodes; implement remaining style/state parity in Jetpack Compose.
2. New `HomeScreen` direct code work on v13 already exposes Mantra Studio, Sound Profiles, Chakra Library, Anahata Nada, Custom Sound, and the last selected sound. The sound regulator remains available in Profiles. Verify Compose navigation tests and accessibility.
3. Convert Studio to explicit states or screen subflows: landing, permission rationale, recording, review/save, private library, japa setup, live counted practice, manual continue, delete confirm and save success. Do not imply functions are live that the code does not implement.
4. Implement the **accessible two-row silence chip fallback** for seven options; verify touch target and correct angle selection/hit testing in `ConcentricJapaDial`.
5. Follow existing v12 security findings and v13 engine code: observe media volume while playing, device routing, battery/thermal, focus loss, foreground-only capture; ensure return from screen-lock is safe.
6. Complete physical 108/1,188/2,268/6,264/11,664-count tests or credible accelerated/equivalent deterministic tests plus real-output validation. Test 100/250/500/750/1000/2000/3000ms gaps, a 108-play boundary, STOP during gap, exact Continue from completed count, and process death/relaunch.
7. Add durable checkpoint storage with recording UUID + selection validation and no auto-resume; invalid checkpoints must fail closed when recordings are removed or altered.
8. Confirm plain-language runtime permission and Play Data Safety changes for `RECORD_AUDIO` before releasing.
9. Record screenshots of actual running builds; do not market Figma mockups as app screenshots. Keep PR #3 draft and Play v11 untouched until the user explicitly authorizes merge/release.

## Status

**Figma master authored.** New Compose home code committed. Existing Mantra Studio engine and UI remain draft; Figma-to-Compose parity and remaining physical safety acceptance are pending. Having mockups or green CI does not itself prove every one of the 11,664 repetitions was physically validated.
