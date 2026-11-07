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
6. **Counted Japa Player**: Select **11, 21, 58 or 108** repetitions, with **0.5/1/2/3 seconds silence** between repetitions. Display completed / target, Stop, remaining status and explicit 30-minute maximum. No infinite background loop; no silent restart on relaunch. For long recordings the 30-minute cap can end the session before reaching 108 repeats. Show this limitation transparently.
7. **Foreground lifecycle and safe output**: Stop capture and repetition playback when leaving the Activity; no screen-lock continuation initially. Check microphone access, media-volume limits and actual built-in-speaker output. Preserve separate synthesized-tone SoundEngine safety policy. No recording of other people without consent.

## Adobe Express brief — original/editable screen system

Use captured v12 screen imagery as **visual references only**, never invent screenshots as actual app functionality. Request Adobe Express to create separate branded assets/screens or reusable backgrounds with consistent typography:

- **Existing screens**: Welcome, Home, Profiles, Player, Timer, Custom Sound, Settings, About/Privacy, Safety, Chakra Library, Chakra Console, Anahata Nada, Device Protection, Battery Saver, Playback Safety. Keep all navigation and controls; simplify ornamentation, reduce gradients, preserve color contrast and accessibility. Preserve the existing Modern-Mystic identity in a subdued manner.
- **New screen 1** Mantra Studio entry and local recordings.
- **New screen 2** Record with mic permission explainer, large start/stop control, visible timer.
- **New screen 3** Review take, preview, retry, title and save.
- **New screen 4** Library of titled recordings with duration and delete confirmation.
- **New screen 5** Counted session setup: 11/21/58/108 chips and short configurable gap.
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
- Test 11/21/58/108 completed counters; 0.5/1/2/3s silence measured across iterations; Stop during a gap and during audio; no 109th replay; session cap.
- Test connected A2DP but phone speaker active, then switch active route to external while playing; ensure no audible external leakage.
- Test Home, lock, app switch, forced process death and audio-focus interruptions; no background microphone or voice playback.
- Test actual maximum volume monitoring; no high-volume bypass.
- Review audio waveform user experience and voice quality; do not claim calibrated SPL or unlimited 108 chants.
- Finish complete design in Adobe Express only after Adobe connection, or using authorized user desktop Adobe workflow; keep original editable source and delivery evidence.

**No Play upload, no merge into production/main and no alterations to v11 unless explicitly authorized.**
