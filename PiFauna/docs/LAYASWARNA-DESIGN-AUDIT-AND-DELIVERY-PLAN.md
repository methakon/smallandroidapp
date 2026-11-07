# Layaswarna — Design Decisions, Figma Audit & Android Delivery Plan

Updated: 2026-11-05  
Status: **Figma redesign concepts created; Android redesign not implemented or validated**  
Figma: https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX  
GitHub branch: `feat/pifauna-phone-audio`

## 1. Product and scope

**Layaswarna** is a supervised, offline, nonmedical experimental audible-sound app. Existing Android routes include Welcome, Home, Sound Player, Profile Library, Custom Sound, Timer, Safety, Settings, Device Protection, Battery Saver, and Playback Safety. Chakra Sound Console, Seven Chakra Library and Anahata Nada Studio are **Figma concepts**, not shipped Android features. No scientifically proven pest-repellent, chakra-activation, sleep-therapy, or clinical outcome claims.

The Figma file contains original screens, iterative regulator concepts, archived layouts and a **new 14-screen modern-mystic redesign set**. Do not equate existence of a Figma frame with Android implementation.

## 2. Approved single-regulator interaction

For category selection (Home, Profile Library), waveform selection (Custom Sound), bounded session selection (Timer), chakra selection (Chakra Console/Library), and planned relaxation sounds (Anahata Nada), use **one circular selector** containing multiple individually recognizable symbols. **Do not render one circle per category or a misleading progress ring on Home.**

- Single tap on a node selects it; dragging/rotating changes selection. Show the selected name and relevant frequency/duration in the center; keep the selected node visibly highlighted with an accent ring and a secondary non-color cue.
- **Double-tap** a selected node to activate/open it. **Stationary long-press** activates/opens only if touch movement stays below a defined touch-slop threshold; a rotation must cancel long-press activation. Gesture thresholds and timing should use Android accessibility/platform conventions, not arbitrary magic numbers.
- The **center is a readout, not an Open button**. Provide a **visible, clearly labeled alternative action** (e.g. “Open selected profile”, “Open chakra details”, “Confirm duration”) for discoverability, switch access and TalkBack. This alternative was added following the accessibility audit; it does not restore center-tap activation.
- The regulator must not start audio simply by browsing or selecting. Opening a profile and starting/stopping audio are separate actions. The Sound Player countdown ring remains meaningful because it represents actual time remaining.
- Each node needs an accessible name, selected state, role, and activation action. Support keyboard/switch input and an accessible linear/list fallback for radial gestures. Never make double-tap/long-press the only path.
- Avoid accidental activation from scrolling or a small drag; cancel pending actions on pointer-up/cancel, rotation, navigation, lifecycle loss or accessibility mode changes as appropriate.

## 3. Screen-by-screen disposition

| Screen | Design direction | Android status |
|---|---|---|
| 01 Welcome | Branded onboarding and clear safety entry | Existing route; redesign pending |
| 02 Home | One regulator with meaningful profile symbols, selected state and accessible open action | Implemented via `LayaswarnaRadialSelector` in `BrowseScreens.kt` |
| 03 Sound Player | Keep true playback state, countdown, frequency, start/stop and guards; do not misrepresent navigation as progress | Implemented with interactive countdown dial, preflight checks, and screen-lock toggle |
| 04 Profile Library | One profile selector; no duplicate list of the same selected name/frequency | Implemented with `LayaswarnaRadialSelector` and saved presets |
| 05 Custom Sound | Waveform regulator plus distinct frequency/output controls and save action | Implemented with radial waveform selector and Hz sliders |
| 06 Timer | Bounded 5/10/15/30-minute selection with explicit confirmation and stop policy | Implemented with radial timer selector; continuous disabled |
| 07 Safety | Readable warnings; remove ornamental overlap; no unnecessary wheel | Implemented in `InfoScreens.kt` |
| 08 Settings | Standard readable settings, accurate controls and accessible navigation | Implemented with active screen-lock playback switch |
| 09 Device Protection | Clear limiter, battery, thermal and audio-focus explanations | Implemented in `InfoScreens.kt` |
| 10 Battery Saver | Clear foreground/timed playback information | Implemented in `InfoScreens.kt` |
| 11 Playback Safety | Clear device and hearing guidance | Implemented in `InfoScreens.kt` |
| 12 Chakra Sound Console | Seven-position symbolic wheel, selected chakra/tone, adjustable audio controls | Implemented in `ConceptScreens.kt` |
| 13 Seven Chakra Library | Seven-position wheel and **dynamic selected-chakra insights** instead of redundant seven-row frequency list | Implemented in `ConceptScreens.kt` |
| 14 Anahata Nada Studio | Relaxation sound regulator and bounded timer; no implied sleep treatment | Implemented in `ConceptScreens.kt` |
| 15 About & Privacy | Offline disclosure and local data privacy | Implemented in `InfoScreens.kt` |

## 4. Chakra sound associations and content rules

Use the following as **modern, optional meditation-tone associations**, **not** ancient fixed frequencies or demonstrated physiological effects:

| Chakra | Example associated tone |
|---|---:|
| Muladhara | 396 Hz |
| Svadhisthana | 417 Hz |
| Manipura | 528 Hz |
| Anahata | 639 Hz |
| Vishuddha | 741 Hz |
| Ajna | 852 Hz |
| Sahasrara | 963 Hz |

On single selection, center displays the name and Hz. Below the wheel, a **dynamic Chakra Insights** panel displays: traditional symbolic association; neutral suggested listening experience or breathing focus; optional short session guidance; and an evidence note. For Anahata, the example symbolic association is compassion, connection and emotional balance; a gentle tone can serve as an optional meditation focus. **There is no reliable evidence that 639 Hz specifically activates Anahata or produces a unique medical or emotional effect.** Content for all seven selections needs editorial and cultural review. Do not imply that simply listening to a frequency treats anxiety, insomnia or other conditions.

## 5. Modern-mystic visual direction (Figma concept, not current Android theme)

| Token | Concept value |
|---|---|
| Background | vertical `#0B0716` → `#1A0F2E` |
| Glass surface | white 6% fill, 16px blur, gold 25% border |
| Gold accent | `#E8C872` |
| Amethyst glow | `#8B6CFF`, restrained behind wheel |
| Brand rose | `#D69AA8` |
| Primary text | `#F4EEFF` |
| Secondary text | `#B9AFD1` |
| Chakra node colors | `#C8483E`, `#E07B39`, `#E8C872`, `#5FCB9A`, `#5BB6E8`, `#6F63E0`, `#B58CF0` |
| Card / chip radii | 24 / 16 |
| Typography | Cormorant Garamond title concept, Inter body concept; confirm font licensing and Android font fallback |

This **supersedes the previous muted plum/terracotta palette for the new concept set**, but **does not** mean the Android `Theme.kt` palette has been changed. No glow or animated pulse may flash or interfere with readability. Reduced-motion mode should suppress optional animation.

## 6. Design audit findings and remediation

1. **Typography:** center labels, disclaimers and insight bodies were too small (approximately 9–13px). Target body **>=14sp** and captions **>=11–12sp** where possible; test Android font scaling up to 200%.
2. **Navigation:** the earlier single Unicode footer string was not a navigation bar. Use **three independent labeled targets** with >=48dp effective hit areas, explicit active state, semantic roles, and Android system bottom insets. The redesigned Figma set has independent visual elements, but it is **not yet a verified Material 3 NavigationBar component**.
3. **Icons:** generic Unicode glyphs and then an accidental **four-dot placeholder regression** made categories indistinguishable. The redesign's **seven radial screens were repaired with individual editable SVG/vector pictograms** (Timer uses numeric labels). Icons need final visual/cultural review, explicit accessible names and node mapping; do not rely solely on colors or generic dots.
4. **Spacing:** enforce a consistent 24dp content gutter, spacing between insights and disclaimer, and prevent overlapping name/Hz boxes. Use responsive constraints/Auto Layout in Figma and Compose layout constraints in Android.
5. **Safe areas:** honor Android status bar, navigation/gesture inset and edge-to-edge behavior; do not draw a fake status bar as an interactive element.
6. **Disclaimers:** evidence note remains visible and accurate; avoid a second oversized card that competes with primary content.
7. **Gestures:** selected state must be prominent; retain visible action fallback alongside advanced gestures.
8. **Maintainability:** semantic layer names, reusable variants, Figma components/Auto Layout and typography/color tokens remain **partially complete or unverified**; don't mark them finished just because editable frames exist.
9. **Visual direction:** restrained violet glow, differentiated chakra node colors and rose identity; check real-device contrast, legibility and reduced motion.

## 7. Figma implementation references

New modern-mystic screen set was authored as editable frames with IDs:
- Welcome `51:25`; Home `51:48`; Player `51:105`; Profile Library `51:128`
- Custom Sound `51:185`; Timer `51:242`; Safety `51:289`; Settings `51:315`
- Device Protection `51:341`; Battery Saver `51:367`; Playback Safety `51:393`
- Chakra Console `51:419`; Chakra Library `51:495`; Anahata Nada `51:571`.

Seven radial screens with repaired pictograms: `51:48`, `51:128`, `51:185`, `51:242`, `51:419`, `51:495`, `51:571`. The earlier selected-chakra insights prototype remains at `13:41`.

**Figma quality caveat:** the concept set is a visual proposal; gradient, glass blur, polished icon anatomy, actual Material 3 components, constraints, interactive prototypes and all text sizes are **not fully verified against the audit**. The four-dot issue was repaired, not a complete accessibility or implementation sign-off.

## 8. Android engineering and safety non-regression

Current Gradle `applicationId` and `namespace`: `in.berhampore.dhar.layaswarna`; Kotlin source packages remain `com.pifauna.app` and manifest references fully qualified `com.pifauna.app.MainActivity`. Changing application ID means existing PiFauna installs/preferences do not migrate automatically.

Preserve: 100–18,000 Hz bounded audible output; <=8% digital full-scale peak; <=60% device media volume gate; battery >15%; severe thermal stop; audio-route checks; stop on audio-focus/lifecycle loss; foreground-only operation; 5/10/15/30-minute sessions; no continuous/overnight/background mode; no unverified efficacy claims. Do not infer real SPL from PCM amplitude.

### Implementation order

1. Normalize domain models and IDs for six existing profiles, seven chakra concepts and any future relaxation sounds; define text, Hz, selected-state and accessible descriptions without conflating tone frequency and modulation rate.
2. Build one reusable Compose radial selector with vector assets, touch-slop-aware rotation, single-tap select, long-press/double-tap activation, state restoration and accessible visible fallback.
3. Replace separate Home/Profile circular tile grids only after the wheel passes navigation and accessibility tests; preserve existing destinations and behavior.
4. Refine Custom Sound, Timer and Player controls while keeping the existing safety gates and meaningful countdown.
5. Implement concept routes only after explicit product scope approval and separate sound/content validation; dynamic chakra insights are descriptive, not medical.
6. Apply the approved palette, system insets, readable typography, Material 3 navigation, responsive layouts, TalkBack, large text and reduced-motion support.
7. Test: Gradle compile/unit/lint, UI navigation, screen readers, double tap vs long press vs rotation conflicts, touch cancellation, color contrast, 200% font scale, small-screen landscape, audio safety stops, emulator and representative real Android devices.

**Verification:** Figma edits were visually inspected for sample Home, Chakra Library and Safety frames. No Android build, instrumented tests, real-device interaction tests, clinical validation or publication-rights clearance has been performed as part of this documentation update.

## 9. Implementation checkpoint — 2026-11-05 (v0.5.0)

Source changes have been pushed to `feat/pifauna-phone-audio`:

- `ui/RadialSelector.kt`: Compose single-wheel, highlighted selection, original vector graphics; swipe rotation, single tap, stationary long press and double tap; accessible explicit action alternative; center readout is non-interactive.
- `ui/Components.kt`, `ui/Theme.kt`: Material 3 navigation, updated dark-mystic colors, real status/navigation insets via Scaffold, readable body text and safe card spacing.
- `ui/screens/BrowseScreens.kt`: profile, waveform and duration selectors integrated; shared profile selection persists through navigation.
- `ui/screens/ConceptScreens.kt`, `data/ChakraProfiles.kt`: all three planned screens are now *navigable*; selected chakra details and tone associations update; short audible tone experiments can be prepared using existing guarded playback.
- `playback/PlayerController.kt`: named experimental tone preparation, display label invalidated when user edits its frequency/waveform. **No auto-start.**
- `app/src/test` and `app/src/androidTest`: mapping/catalog tests and Compose gesture/navigation smoke tests. The workflow validates debug APK, JVM tests, lint, and instrumented test compilation.

**Known deliberate limitation:** Pink and white noise are visible as not yet implemented. The existing pure tone/sweep engine remains unchanged pending independent DSP, spectrum, device/thermal and power validation. No medical/chakra-specific efficacy claims or sleep-mode promises.

**Verification snapshot:** the release-review command
(`clean testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug`)
passes: 33 JVM tests, 0 lint errors, debug APK built. The instrumented suite runs
20 tests green on both an Android 11 (API 30) and an Android 9 (API 28) device,
including radial tap / long-press / double-tap / drag-rotation / rotation-cancels-
long-press and the real-AudioTrack playback tests. Details in
[TESTING.md](TESTING.md). Manual TalkBack, contrast and large-font review, plus
headset/Bluetooth route refusal, are still outstanding.

## 2026-11-05 — branding/icon implementation

The approved bright lotus + integrated soundwave mark is stored in Figma as editable master component [`62:25`](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=62-25) and in GitHub as `PiFauna/design/layaswarna-lotus-soundwave.svg`. Android foreground/background/monochrome adaptive resources have been updated accordingly; no UI-navigation or audio safety policy was altered. See [icon implementation and QA checklist](LAYASWARNA-APP-ICON.md).
