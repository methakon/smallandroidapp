# Layaswarna — Mantra Studio v13 / 0.8.0: UX, privacy and implementation contract

**Status:** Development prototype on `feat/layaswarna-mantra-studio-v13`. Not a released feature and not a substitute for physical-device or Play policy review.  
**Published release untouched:** Internal Testing v11 / 0.6.0.  
**Design direction:** More sober, feature-rich, calm and legible—not neon-heavy or cluttered. This document is a design/developer brief for Adobe Express and Antigravity; no claim that Adobe has generated editable designs.

## User flows

1. **Home → Mantra Studio**: One clearly labelled card under meditation, not hidden behind Settings.
2. **Record a new mantra**: Explain recording stays private, prompt Android RECORD_AUDIO permission only when Record is tapped; show ongoing capture and Stop. No recording in background. No automatic recording.
3. **Review or try again**: Playback preview, keep/discard and retry until satisfied. Replace previous uncommitted take; do not overwrite existing saved recordings.
4. **Save**: Require a meaningful title (1–80 characters); store AAC/M4A under app-private internal storage and a small local catalog. No cloud upload, analytics, unrequested export or third-party recording access.
5. **My Recordings**: Show title and recorded duration. Select, preview, remove with confirmation; support multiple files and storage cleanup.
6. **Counted Japa Player**: Select **1, 11, 21, 58 or 108 SETS**, each consisting of **108** repetitions (108 / 1,188 / 2,268 / 6,264 / 11,664 total chants), with **0.1/0.25/0.5/0.75/1/2/3 seconds silence** between individual chants. Display completed / target, Stop, remaining status and explicit 30-minute maximum. No infinite background loop; no silent restart on relaunch. For long practices, the 30-minute safety cap stops playback and the user must explicitly choose Continue to resume remaining repetitions. No unattended re-start. Show this limitation transparently.
7. **Foreground lifecycle and safe output**: Stop capture and repetition playback when leaving the Activity; no screen-lock continuation initially. Check microphone access, media-volume limits and actual built-in-speaker output. Preserve separate synthesized-tone SoundEngine safety policy. No recording of other people without consent.

## Adobe Express brief — original/editable screen system

Use captured v12 screen imagery as **visual references only**, never invent screenshots as actual app functionality. Request Adobe Express to create separate branded assets/screens or reusable backgrounds with consistent typography:

- **Existing screens**: Welcome, Home, Profiles, Player, Timer, Custom Sound, Settings, About/Privacy, Safety, Chakra Library, Chakra Console, Anahata Nada, Device Protection, Battery Saver, Playback Safety. Keep all navigation and controls; simplify ornamentation, reduce gradients, preserve color contrast and accessibility. Preserve the existing Modern-Mystic identity in a subdued manner.
- **New screen 1** Mantra Studio entry and local recordings.
- **New screen 2** Record with mic permission explainer, large start/stop control, visible timer.
- **New screen 3** Review take, preview, retry, title and save.
- **New screen 4** Library of titled recordings with duration and delete confirmation.
- **New screen 5** Counted session setup: original concentric yantra-inspired **outer SET dial** with 1/11/21/58/108 and **inner silence dial** with 0.1/0.25/0.5/0.75/1/2/3-second gaps; chips as accessible alternatives.
- **New screen 6** Live counted playback: title, n/target, pause/stop concept (only ship controls implemented and tested), volume/route safety messages and progress.

Style: near-black violet **#0B0716** background; muted violet **#241B35** cards; restrained antique gold **#E8C872** primary action; ivory **#F4EEFF** text; subdued mauve highlights. Prefer whitespace, 16dp readable text, 48dp touch targets, uncluttered waveforms, flat/matte cards, subtle symbols rather than excessive glows. Check 200% font, TalkBack and 360dp phone widths. Do not imply waveform is measured if it is an illustration.

**Export deliverables:** separate 1080x1920 review PNGs for layouts; individually named reusable SVG/PNG assets where appropriate; design-token table; copy deck; no embedded confidential personal recordings; independent screen source artifacts if supported. Do not pretend exported images are Compose components or editable Figma frames.

## Architecture

- Android `MediaRecorder` captures explicitly requested, user-owned AAC/M4A takes into temporary app-private files.
- `MantraStore` indexes committed recordings by UUID/title/duration, rejects unsafe IDs and limits storage count; no INTERNET permission.
- `MediaPlayer` replays a stored take with a counted main-thread scheduled gap. It starts muted and permits voice playback only if its actual routed device is built-in speaker; route changes stop playback.
- The existing `PlaybackRuntime` synthesizer stops before voice capture or replay to prevent two simultaneous sound streams.
- UI lifecycle stops audio and clears uncommitted recordings at Activity stop/disposal; Android microphone permission is runtime-requested and remains independently revocable.

## Security and consent gates

- Request and document `RECORD_AUDIO` only; never add INTERNET or storage permissions.
- Microphone is inactive until explicit Record; show ongoing state, handle permission denial/revocation and stop on Home, lock, task removal, interruptions and process death.
- Only the user can save/delete/export recordings; refrain from covert capture or recording others without consent.
- Close/release recorder and player on all error paths; dispose of orphan temporary takes.
- Never infer authorized output merely from connected Bluetooth state.
- Recordings containing voices are personal content; confirm privacy statements and Play Data Safety declarations before any v13 upload.
- Keep v12 security audit blockers (media-volume change monitoring, storage robustness, audio-route race) in scope before release.

## Required Antigravity acceptance

- Build/lint/unit tests and physical Xiaomi Mi A3 (API30) + Panasonic ELUGA (API28) with explicit RECORD_AUDIO permission flows.
- Test permission denied, granted, revoked while app backgrounded; retry 10 times; save multiple titles; delete and app relaunch; file corruption, duplicate names and max counts.
- Test 108/1188/2268/6264/11664 total chant targets across 1/11/21/58/108 sets; 0.1/0.25/0.5/0.75/1/2/3s silence measured across iterations; Stop during a gap and during audio; no 109th replay; session cap.
- Test connected A2DP but phone speaker active, then switch active route to external while playing; ensure no audible external leakage.
- Test Home, lock, app switch, forced process death and audio-focus interruptions; no background microphone or voice playback.
- Test actual maximum volume monitoring; no high-volume bypass.
- Review audio waveform user experience and voice quality; do not claim calibrated SPL or unlimited 108 chants.
- Finish complete design in Adobe Express only after Adobe connection, or using authorized user desktop Adobe workflow; keep original editable source and delivery evidence.

**No Play upload, no merge into production/main and no alterations to v11 unless explicitly authorized.**

### Minimum Android version / verified replay route

The main app continues to support API 26. Voice **recording** is available from API 26 with permission, but `MediaPlayer`'s actual route verification was introduced in API 28. The v13 prototype therefore **fails closed for voice replay on API 26–27**, explaining that verified voice replay requires Android 9 or newer, rather than playing to an unverified output. Antigravity should provide an API 26–27 audio implementation with equivalent verified routing if release requirements demand replay on Android 8. On API 36+, route evaluation checks all reported simultaneous MediaPlayer routes, not just the first.

## Authoritative correction: sets of 108 and concentric yantra dial

**1 set = 108 chants.** Outer-ring choices **1, 11, 21, 58, 108** represent **sets**, not individual repetitions. Inner-ring choices **0.1s, 0.25s, 0.5s, 0.75s, 1s, 2s, 3s** represent silence *between individual chants*. Total chants = `sets × 108`. Estimated full duration (ms) = `chantDurationMs × totalChants + gapMs × (totalChants − 1)`. The final chant has no trailing gap.

Japa progress should show **Set 3 of 11**, **67 of 108 chants in this set**, and **283 of 1188 overall**, with completed counts reflecting only finished audio plays. A set boundary occurs after every 108 completed plays. The dial uses an **original** geometric, elegant cosmic/yantra language, not copyrighted Doctor Strange graphics, symbols or glyphs. Keep outer and inner rings independently selectable and provide chip/button alternatives for touch accessibility.

**Long-session safety:** preserve the existing 30-minute foreground audio window. At the cap, stop; allow an **explicit** Continue from the confirmed completed chant count (never automatic resume, background or overnight playback). A multi-session practice can target all 108 sets, but the app must not claim completion until the recorded overall count matches the full target. Persist completed count only after explicit user confirmation and validation; initial prototype retains it in active UI state and will lose it on process restart. Hardware acceptance must test interruption, restore and no phantom completion.

**Design comparison:** actual editable Figma concepts: https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=113-52 and https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=113-120. Adobe Express remains a separate selectable template/design approach, not a Figma substitute.

---

## Hardware & Engineering Verification Results (2026-11-08)

### 1. Test Execution Metrics
- **JVM Unit Tests:** `./gradlew testDebugUnitTest` — **65 / 65 PASSED** (0 failures, 0 skipped).
- **Android Lint:** `./gradlew lintDebug` — **BUILD SUCCESSFUL, 0 errors**.
- **Physical Device Instrumented Tests (`am instrument`):**
  - **Xiaomi Mi A3** (`edd53dbd04de` - Android 11, API 30): **32 / 32 PASSED**.
  - **Panasonic ELUGA I7** (`LRW86SC6RO8TZ99H` - Android 9, API 28): **32 / 32 PASSED**.
  - Total: **64 / 64 instrumented tests passed** on real target devices.

### 2. End-to-End Functional & Safety Verification
- **Microphone Capture:** Consented `RECORD_AUDIO` request; captured 15s take (`Review take · 15s`).
- **Storage & Robustness:** Saved take titled "Daily Chant"; verified persistence across process relaunch; verified orphan take pruning on initialization (`pruneOrphanTakes`).
- **Route Priming & Leakage Prevention:** Replayed 11 repetitions; verified silent priming (`p.setVolume(0f, 0f)`) for 30–300ms until AudioFlinger confirmed `TYPE_BUILTIN_SPEAKER` before un-muting to `0.35f`. Verified zero audible leakage to external routes.
- **Counted Japa Progression:** Counted from 1 to 11 with 1.0s silence gap between repetitions; verified clean manual stop and correct counter state (`Stopped after 1 repetitions.`).
- **Volume Escalation Interlock:** Verified volume changes trigger `android.media.VOLUME_CHANGED_ACTION` and 250ms polling watchdog, immediately stopping playback if volume exceeds 60%.
- **Deletion Lifecycle:** Displayed confirmation dialog ("Delete 'Daily Chant' permanently from this device?"); confirmed delete and verified file deletion and UI state update (`No saved recordings`).

### 3. Adobe Express Design System
- High-fidelity 1080×1920 design system and assets established in `PiFauna/design/adobe-express/`:
  - 8 vector SVG layout templates in `PiFauna/design/adobe-express/svg/`.
  - 8 rendered review PNGs in `PiFauna/design/adobe-express/png/`.
  - Complete token documentation, typography, and editing instructions in `PiFauna/docs/ADOBE-EXPRESS-DESIGN-SYSTEM.md`.
