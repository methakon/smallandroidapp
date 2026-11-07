# Layaswarna v13 — UI copy deck and implementation plan

**Status:** Proposed wording and engineering specification; not a declaration that every screen or behavior is implemented.  
**Branch:** `feat/layaswarna-mantra-studio-v13`  
**Target:** 0.8.0 / versionCode 13 — unreleased.  
**Scope:** new sober home experience and new user-recorded mantra feature.

## Part A — approved-for-design English UI copy

### H-01 New home — refreshed information architecture

Top: **Layaswarna**  
Eyebrow: **SOUND · CHAKRA · MANTRA**  
Hero: **A quieter space for your practice**  
Supporting line: *Explore sound, create your own chants, and choose how long you listen.*  
Featured action title: **Mantra Studio**  
Featured subtitle: *Record a chant in your own voice, save your favorite take, and repeat it with a counted practice.*  
CTA: **Open Mantra Studio**  
Explore title: **Explore your practice**  
Four feature cards:
- **Sound Profiles** — *Browse experimental sound profiles.*
- **Chakra Library** — *Explore symbolic chakra tones and their traditional associations.*
- **Anahata Nada** — *Discover guided choices for mindful sound exploration.*
- **Custom Sound** — *Choose a frequency and a timed session.*
Footer: *Offline by design. Recordings stay on this phone. Keep listening comfortable.*  
Bottom tabs: **Home · Library · Player · Settings**.  
Use a sober, low-glow design. Keep safety language visible, and never suggest health treatment or proven repellent effects.

### M-01 Mantra Studio — landing

Title: **Mantra Studio**  
Subtitle: *Your voice. Your rhythm. Your practice.*  
Intro: *Record a mantra or chant, try again until it sounds right, and save the take you want to keep.*  
Primary action: **Record a mantra**  
Library title: **My recordings**  
Empty state: **No recordings yet** / *Your saved chants will appear here.*  
Secondary action: **How it works**  
Privacy note: *Microphone access is used only when you choose to record. Audio stays on your phone.*

### M-02 Microphone access

Title: **Allow microphone access**  
Body: *Layaswarna needs microphone access to capture a mantra you choose to record. Recording starts only when you tap Record and ends when you stop or leave the screen.*  
Buttons: **Continue** / **Not now**  
Denied state: *Microphone permission wasn't granted. You can try again whenever you're ready.*  
Permanently denied state: *To record, allow Microphone access in Android Settings.* / **Open settings**  
Consent footer: *Record another person's voice only with their permission.*

### M-03 Record your chant

Title: **Record a mantra**  
Subtitle: *Take your time. You can make another attempt later.*  
Idle guidance: **Ready when you are** / *Find a quiet place and speak comfortably.*  
Button idle: **Start recording**  
Active: **Recording…** / **Stop recording**  
Timer: **00:00 / 01:30** (prototype maximum; this limit may change only after performance/safety acceptance)  
Quality hint: *Keep the phone steady and speak at a natural level.*  
Failure: *We couldn't finish this recording. Try again.*  
No background recording.

### M-04 Review and retry

Title: **Listen to your take**  
Duration: **Recording · 00:36** (dynamic)  
Buttons: **Play preview** / **Stop preview** / **Record again** / **Keep this take**  
Supporting text: *Not quite right? Record another take. Only the take you choose is saved.*  
When starting again: *This unsaved take will be replaced.* / **Record again** / **Cancel**

### M-05 Save a recording

Title: **Save your chant**  
Field: **Recording title**  
Placeholder: *e.g. Morning Gayatri Mantra*  
Optional note in future iteration: **Notes (optional)** (do not display until implemented)  
Primary: **Save recording**  
Secondary: **Back to review**  
Validation empty: *Give your recording a title.*  
Validation long: *Keep titles within 80 characters.*  
Success: **Recording saved** / *Your chant is now in My recordings.*  
Actions: **Play now** / **My recordings**

### M-06 My recordings

Title: **My recordings**  
Subtitle: *Your saved chants live on this device.*  
Primary: **Record new**  
Row: **{title}** / **{duration}** / **Play** / **Options**  
Options: **Choose for repetitions** / **Rename** (only after implemented) / **Delete**  
Delete confirmation: **Delete recording?** / *“{title}” will be permanently deleted from this device.* / **Delete** / **Cancel**  
Empty state: **Make your first recording** / *Record a chant to begin your library.*  
Error: *This recording can't be opened. You can delete it or try again.*

### M-07 Configure repetitions

Title: **Set your repetitions**  
Selected label: **Selected recording**  
Section: **Number of repetitions**  
Choices: **11 · 21 · 58 · 108**  
Section: **Quiet pause between repeats**  
Choices: **0.5 s · 1 s · 2 s · 3 s**  
Estimate: **Estimated time: {duration}**  
Caution: *Sessions stop after 30 minutes. Long chants might finish before the selected count.*  
Primary: **Start repetitions**  
Secondary: **Change recording**  
Avoid saying "continuous" without explaining this is a finite counted practice.

### M-08 Active counted playback

Title: **Your mantra practice**  
Subtitle: **{recording title}**  
Progress: **{completed} of {target} completed**  
Status: **Playing chant {current}** / **Quiet pause** / **Stopped** / **Completed**  
Primary: **Stop**  
Pause/resume: only display if implemented and tested.  
Footer: *Phone-speaker playback only. Keep volume at a comfortable level.*  
Completion: **Practice complete** / *You finished {count} repetitions.* / **Done** / **Repeat the set** (only explicit start)

### M-09 Feedback and interruptions

- **Lower the volume** — *Reduce media volume to 60% or less before starting.*
- **Speaker required** — *Choose your phone's built-in speaker to continue.*
- **Playback stopped** — *The audio output changed. Your session ended to protect listening safety.*
- **Microphone unavailable** — *Close another recording app and try again.*
- **Storage unavailable** — *Free some space on your phone before saving.*
- **Recording too short** — *Record for a little longer, then try again.*
- **Session limit reached** — *This session ended at the 30-minute safety limit.*
- **App left foreground** — *Recording or playback stopped when you left Layaswarna.*
- **Permission revoked** — *Microphone access was removed. Recording stopped.*
- **Could not save** — *Your recording wasn't saved. Try again.*

## Visual guidance for H-01 and M-01 to M-09

- Target artboard 390×844 dp; validate 360dp widths and large accessibility font settings.
- Quiet modern-mystic palette: #0E0D17 background, #231F2E cards, #F5F2F7 body, #DEC588 accents, #947FCD secondary, #BEB6CA helper copy.
- 8dp spacing grid, 16–24dp panel spacing, 48dp minimum touch targets, restrained corner radii, minimal glows.
- Home hero should be visually clear within first fold; featured Mantra Studio card above secondary modules, but retain direct access to existing sound and chakra features.
- Motion and waveform illustration are decorative unless accurate audio metering is implemented. No fictitious microphone activity graphs.
- Record button is visually distinct red/rose active state with persistent timer; Stop is always visible.
- The recorded-voice flow is not the same as the experimental frequency synthesizer; do not reuse its technical claims.

## Part B — Jetpack Compose / Android implementation plan

### B1. Routing and state

- Add a dedicated `Route.MantraStudio` and `MantraStudioScreen` from Home.
- Refactor into navigation substates `Library, Capture, Review, Save, Setup, Playing`; survive configuration changes via stable view model or lifecycle-aware state where appropriate.
- Extract strings into Android resources for localization; avoid spreading literals across Composables.

### B2. Capture engine

- Runtime-request `android.permission.RECORD_AUDIO` on explicit Start only.
- `MediaRecorder` AAC in MPEG-4 (`.m4a`) into app-private temporary files, no `INTERNET`, external storage, foreground microphone service or automatic startup.
- Handle denial, revoke, phone interruption, lifecycle stop, very short captures, low storage and device microphone errors.
- Never record while screen locked or app backgrounded; release microphone immediately on Stop or error.
- Retry removes only the *unsaved* take, not any committed recording.

### B3. Local library

- File naming by opaque UUID; validate titles and metadata; private internal storage.
- Defensive index reading and atomic replacement; remove orphan `take-*.m4a` after abnormal termination.
- Bound number of recordings and file duration/size; reject path traversal and corrupted metadata.
- Support list/preview/delete; rename only when implemented and tested. Deletion must be confirmed.
- Review Android auto-backup rules and privacy policy: current app declares no backup.

### B4. Playback and japa timing

- Distinguish user-selected `targetCount` (11,21,58,108), `completedCount`, current segment state and `gapMs`.
- For each repetition, play exactly the saved audio content, on completion schedule the next after 500/1000/2000/3000 ms; gaps must not count as repetitions.
- Stop immediately on explicit user Stop, activity background, output route changes, focus loss, thermal limits, battery cutoff, or media-volume threshold.
- Enforce 30-minute session cap, surface an estimate before starting and clear partial-completion state explicitly.
- Use real audio routing from the active player, not Bluetooth connection state. Test platform restrictions on API26–27 and API36 simultaneous routes.
- No infinite replay and no screen-lock continuation in this initial version; an opt-in foreground service would require separate review.

### B5. Security/privacy and quality gates

- Update bundled privacy policy and public legal website **only as part of an authorized release**. Voice recordings are user-generated personal data.
- Test volume escalation, route switching, audio focus, permission revoke, app switch, locking, recording delete, storage-full, multiple retries and process death.
- Unit tests for count/gap calculations, max duration and catalog validation; instrumented tests for navigation and permission prompts; physical audio tests on Mi A3 and ELUGA.
- Retain blocker from v12 security audit concerning dynamic volume monitoring and route-change leakage; verify both tone and mantra playback.
- CI/lint/build and accessibility pass required; PR stays draft until verified and approved. Do not publish v13 automatically.

## Adobe Express editor direction

Start with a *design concept* based on the sober palette and the home copy here. Editable Adobe templates may help with branded marketing/layout presentations, but **do not assume Express exports structured Compose source or fully editable multiframe product prototypes**. Any Adobe output must be reviewed and then implemented as actual Compose screens. Keep original design links and exports in the PR documentation.
