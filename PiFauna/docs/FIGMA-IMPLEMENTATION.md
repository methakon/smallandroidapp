# Figma implementation status

Source: <https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX>
Frames: 11, artboards 390 × 844, single dark canvas.

Retrieved with the Figma design API on 2026-11-04; node IDs below are the ones
returned by that call.

## Design tokens

| Token | Figma | Implementation |
|---|---|---|
| Canvas | `#0C211D` | `PiFaunaColors.Background` |
| Hero card | `#174338`, radius 20 | `HeroHeader` |
| List card | `#18352F`, radius 20 | `PfCard`, `PfTextCard` |
| Divider | `#31574C` | `PfBottomBar`, sliders |
| Accent | `#75E2B8` | wordmark, section labels, primary action |
| Body text | `#F0FAF5` | `PiFaunaColors.BodyText` |
| Muted text | `#A8C9BE` / `#B6D9CA` | `PiFaunaColors.Muted`, `HeroSubtitle` |
| Typeface | Inter | system sans-serif (Roboto) — see deviations |
| Artboard | 390 × 844 px | responsive dp layout, 20 dp margins, 76 dp cards, 16 dp gaps |

## Screen-by-screen

| # | Figma frame | Node | Status | Implementation |
|---|---|---|---|---|
| 01 | Welcome | `1:2` | Implemented | `WelcomeScreen` |
| 02 | Home | `1:15` | Implemented | `HomeScreen` (radial profile selector with time-aware greeting) |
| 03 | Sound Player | `1:32` | Implemented | `PlayerScreen` (interactive countdown dial, frequency controls, preflight check cards) |
| 04 | Profile Library | `1:47` | Implemented | `ProfilesScreen` (radial profile selector + presets) |
| 05 | Custom Sound | `1:64` | Implemented | `CustomSoundScreen` (radial waveform selector + custom Hz sliders) |
| 06 | Timer | `1:81` | Implemented | `TimerScreen` (5, 10, 15, 30 min bounded choices; continuous card disabled) |
| 07 | Safety | `1:98` | Implemented | `SafetyScreen` |
| 08 | Settings | `1:115` | Implemented | `SettingsScreen` (includes active screen-lock playback toggle switch) |
| 09 | Device Protection | `2:2` | Implemented | `DeviceProtectionScreen` |
| 10 | Battery Saver | `2:42` | Implemented | `BatterySaverScreen` |
| 11 | Playback Safety | `2:22` | Implemented | `PlaybackSafetyScreen` |
| 12 | About & Privacy | n/a | Implemented | `AboutPrivacyScreen` |
| 13 | Chakra Sound Console | `13:23` / `73:2` | Implemented | `ChakraConsoleScreen` (in `ConceptScreens.kt`) |
| 14 | Seven Chakra Library | `50:2` | Implemented | `ChakraLibraryScreen` (in `ConceptScreens.kt`) |
| 15 | Anahata Nada Studio | `73:101` | Implemented | `AnahataNadaScreen` (in `ConceptScreens.kt`) |

Navigation: the bottom bar defines three primary destinations (`Home`, `Profiles`,
`Settings`). The remaining twelve screens are navigated hierarchically from cards,
radial selectors, or links inside them (total 15 destinations in `com.pifauna.app.ui.Route`).

## Deliberate deviations from the design

These are product decisions, not unfinished work.

1. **Continuous playback is disabled.** Figma 06 offers a "Continuous" card. The
   app renders it disabled with the explanation "Not offered — every session must
   end on its own", because unattended playback is the main safety hazard this
   app is built to avoid.
2. **Maximum frequency is 18,000 Hz, not 20,000 Hz.** Figma 05 shows a
   "12–20 kHz" band. Nothing above 18 kHz is meaningful through a phone speaker
   and calling it ultrasonic would be false, so the slider stops at 18 kHz.
3. **Explicit screen-lock timed playback opt-in.** In place of generic "Background playback",
   the app provides an explicit, default-OFF toggle: "Continue timed playback with screen locked".
   When enabled, user-initiated sessions continue under an Android `mediaPlayback` foreground
   service with a lock-screen Stop notification. Indefinite background playback is prohibited.
4. **Dark appearance is fixed.** Figma 08 lists a "Dark appearance" switch; the
   design file contains only a dark palette, so the row reports that instead of
   pretending to toggle something.
5. **Inter is replaced by the system sans-serif.** Bundling Inter would add font
   assets for a cosmetic difference. Sizes, weights and spacing follow the Figma
   values.
6. **Custom Sound adds an output-level control** (3% / 5% / 8%) so the digital
   ceiling is visible to the user. The Figma frame shows only a waveform row.
7. **The waveform row offers four shapes** (sine, sweep, pulse, hop) rather than
   a "Sine / Sweep · Off" pair, because the engine supports all four and hiding
   them would leave dead code.

## Not reproduced

- The Figma status-bar mock ("9:41") is drawn by the system, not the app.
- The decorative hero illustration referenced in earlier design notes is not
  bundled; the hero is a flat colour panel as in the current frames.


## 2026-11-04 layout and Chakra-control audit

Design source: https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX

- Repositioned concept screens 12, 13, 14 into a dedicated fourth row (x=100, 550, 1000; y=2960), with non-overlapping 60px horizontal gutters.
- Repositioned original Chakra control sheet to (1450,2960); moved large reference-art masters into a separate right-side area (x=2600,3680,4760,5840).
- On all eleven implemented-route artboards, moved newly created original Chakra vector groups from overlapping hero content into the first selectable/action row.
- Removed the previous first-row Chakra linework where it collided with the new symbol, and removed eleven decorative friezes that overlapped lower content/Anahata Nada links.
- Constrained overflow-prone text widths, including mock status time, to the 390px artboard. Enabled artboard content clipping.
- Captured updated screenshots of Home (node 1:15), Chakra Sound Console (13:23) and original icon sheet (15:5) after edits.
- **Scope:** These are Figma composition edits, not changes to the Android app or guarantees of responsive layout. Visual QA on all 14 frames, text wrapping, accessible touch targets and actual interactive component variants remain pending.
- Research requirements and claim-quality review: [LAYASWARNA-REQUIREMENTS-REVIEW.md](LAYASWARNA-REQUIREMENTS-REVIEW.md).
- Original icon provenance/IP constraints: [LAYASWARNA-CHAKRA-DESIGN-SPEC.md](LAYASWARNA-CHAKRA-DESIGN-SPEC.md).


## 2026-11-04 Android implementation (Layaswarna visual pass)

Implemented directly on `feat/pifauna-phone-audio`:
- `ui/Theme.kt`: replaced the older green PiFauna palette with the muted Layaswarna Figma tokens (midnight plum, rose terracotta, sandstone, ivory); retained the Material 3 dark color scheme.
- `ui/Components.kt`: Layaswarna wordmark, original Compose Canvas lotus/Chakra medallion, decorative hero medallion on all 11 existing routes, optional action-row symbol, bounded title area to avoid hero icon/text collision. Decorative icons have no independent accessibility announcement; their parent button retains a spoken text label.
- `ui/screens/BrowseScreens.kt`: original geometric Chakra symbols in profile and timer controls; softened selected chips and primary/stop action backgrounds.
- `ui/screens/InfoScreens.kt`: changed user-facing PiFauna references to Layaswarna.
- `res/values/strings.xml`: changed Android launcher label to Layaswarna.
- **Unchanged:** Kotlin package/application ID, 11-route navigation, audio engine, battery/thermal/volume/route guards, foreground-only and session-duration policy.
- **Not implemented in this historical pass (superseded by 2026-11-05 Mystic pass):** Three concept routes (Chakra Sound Console, Seven Chakra Library, Anahata Nada Studio) were concepts here, but were subsequently implemented in `ConceptScreens.kt` in the Mystic integration. AI raster artwork as app assets, theme switching, and extended sleep playback remain omitted by design.
- **Validation status:** GitHub commits confirmed; Gradle build, unit tests, emulator/device screenshots and runtime accessibility tests **not executed in this session**. Run `cd PiFauna && ./gradlew testDebugUnitTest assembleDebug` on a machine with Android SDK and Java configured, and inspect all 11 routes before merging.


## 2026-11-04 circular dashboard and Android identifier update

- Android `applicationId` and Gradle `namespace` changed from `com.pifauna.app` to **`in.berhampore.dhar.layaswarna`**. Android manifest uses fully qualified `com.pifauna.app.MainActivity` because the Kotlin source packages have **not yet been migrated**. The launcher label is Layaswarna; the root Gradle project is Layaswarna.
- **Compatibility warning:** This is a **different installed Android app** from `com.pifauna.app`. Existing locally saved preferences and presets do not automatically migrate, and an update over the old package is not possible. Plan explicit export/migration before any public release.
- Added original Compose circular dial inspired by generic timer/progress-ring interaction principles (not copied from reference screenshots), with calm sandstone progress stroke, plum track, restrained lotus details and descriptive TalkBack action.
- Player dial shows controller-backed countdown, and its tap invokes the **existing** `onStart`/`onStop` handlers. No audio-policy change.
- Home circular dial navigates to the **existing** profile library; it does not pretend to display measured audio or environmental data.
- The user's two reference images are **inspiration only**, not bundled, traced, or redistributed.
- These are functional Compose components but **not** full reproductions of the Figma three concept routes or the pictured environmental-sensor dashboard. No microphone, sensor readings or extra permissions were added.
- Gradle build, tests, APK install, visual screenshots and contrast checks **remain unverified** in this environment.


## 2026-11-04 correction: meaningful circular category controls

The prior Home "Library" progress ring was visually misleading because a full progress indicator implied playback/session state when the action only navigated. **Removed that ring from Home**. The timed playback ring remains only in the Sound Player, where its countdown has genuine meaning.

- Added `LayaswarnaProfileCircle` reusable 108dp circular icon in a 152dp+ clickable category tile, with muted rose/sandstone Chakra petal outline and a category-specific Unicode pictogram.
- Home uses a responsive 2-column circular category grid for actual profiles (mosquito, general insects, rodents, dog/cat, birds). Each opens its existing profile detail/playback path.
- Profile Library uses the same grid for all six profiles, including Custom band. Existing preset and profile-notes sections remain.
- The icon-to-profile mapping is based on stable profile IDs, not arbitrary list positions; descriptions show the actual profile test-frequency range.
- This is a navigation/category treatment, **not** an assertion that sounds repel or control animals. No audio or safety changes.
- The user's screenshot is a layout reference only. No copied UI imagery, fonts, glyph vector packs or device mockups were added.
- Verification still needed: Gradle compile/test, TalkBack reading order, emoji rendering on Android versions, screenshot/layout inspection at small width and large font scale.

## 2026-11-05 — authoritative redesign status (supersedes older visual-status notes)

**Read [LAYASWARNA-DESIGN-AUDIT-AND-DELIVERY-PLAN.md](LAYASWARNA-DESIGN-AUDIT-AND-DELIVERY-PLAN.md) before implementing any new Figma screen.** The opening 11-frame green token table and several historical notes above describe **earlier versions**, not the current approved design direction. In particular, the earlier Home "Library" ring was removed; the later two-column circular category tiles are the **current Android code**, not the newly approved one-wheel Figma design. The statement above that the Android package was unchanged is historical: the current Gradle application ID/namespace is `in.berhampore.dhar.layaswarna`, while Kotlin source packages still use `com.pifauna.app`.

- **Figma:** 14 editable modern-mystic redesign frames (IDs `51:25` through `51:571`; see the linked audit for the exact mapping), alongside older originals and concepts. Seven radial screens were repaired after a four-dot placeholder regression; meaningful editable vector pictograms now replace those dots. The selected chakra details panel is intended to change with selection rather than repeat seven static frequency cards.
- **Design behavior:** one circular selector with selected-node highlight, single-tap/rotation selection, stationary long-press or double-tap activation, center readout **not** center Open action, plus a visible accessible action fallback.
- **Accessibility:** independent bottom navigation targets, system safe-area insets, >=14sp body target, readable evidence notes, TalkBack/keyboard alternatives and semantic icon names. These are **acceptance requirements**, not all proven Figma component or Android features.
- **Visual direction:** newer dark-violet/amethyst/rose/gold modern-mystic proposal supersedes the older green and muted-terracotta **concept tokens**; Android Theme.kt is not yet migrated to this latest scheme.
- **Safety:** no changed audio policy, clinical claims, continuous playback or approved overnight feature.
- **Validation:** Figma concept screenshots inspected; Android Gradle build, instrumentation, device screenshots and gesture QA **not executed**.

## 2026-11-05 — implemented Android design pass (v0.5.0)

The approved design is now wired into Android Compose on `feat/pifauna-phone-audio`:

- **Shared wheel:** `ui/RadialSelector.kt` implements one radial regulator with original Canvas pictograms, highlighted selected item, center readout (never an Open button), tap or drag/rotate selection, stationary long-press or double-tap activation, and a visible labeled fallback button.
- **Existing routes upgraded:** `ui/screens/BrowseScreens.kt` uses the wheel for Home, Profile Library, Custom Sound waveform selection, and Timer. Home/Profile selection is hoisted into `PiFaunaApp.kt` so the highlight survives navigation.
- **Concept routes made navigable:** `ui/screens/ConceptScreens.kt` adds Chakra Library, Chakra Sound Console and Anahata Nada. `data/ChakraProfiles.kt` contains seven symbolic associations and the dynamic insights copy. `PlayerController.prepareExperimentalTone` configures existing audible tone modes and **does not start playback**.
- **Anahata scope:** Current Sound Player can prepare soft audible tones and an audible 400–500 Hz sweep. Pink/white noise are **visibly marked planned/unavailable** and are not represented by a mislabeled sine wave. No new waveform engine, background service, extended duration or overnight playback.
- **Visuals/navigation:** `ui/Theme.kt` uses dark-violet, gold, rose and chakra-node accents; `ui/Components.kt` uses independent vector-icon Material 3 navigation targets, readable informational cards and larger typography; `res/values/colors.xml` matches the new startup background.
- **Safety and application ID:** Existing audio limits and lifecycle stops remain in place; Gradle namespace/application ID remains `in.berhampore.dhar.layaswarna`, with existing `com.pifauna.app` Kotlin source packages.
- **Test coverage:** JUnit test for seven chakra tones and radial hit geometry; Android instrumented navigation/gesture tests added. GitHub Actions workflow compiles, unit-tests, assembles APK and runs lint; later revision also compiles `assembleDebugAndroidTest`. A core build [passed](https://github.com/methakon/smallandroidapp/actions/runs/41890856104). Latest commits require their own successful workflow run. Physical-device/TalkBack/gesture tests remain pending.

**Clarification:** This is the implementation of the *current-release approved subset* of the redesign, not an assertion that planned noise synthesis, medically meaningful sound effects, calibrated loudness or advanced Figma animations have shipped.

## 2026-11-05 — lotus-and-soundwave launcher icon

- Reusable Figma icon master component created: [node `62:25`](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=62-25). It is an editable SVG-imported vector component, not a screenshot.
- Canonical asset: `PiFauna/design/layaswarna-lotus-soundwave.svg`. Bright lotus petals, soundwave bars, resonance circles and subtle chakra beads, on deep-violet gradients.
- Android adaptive launcher now uses `drawable/ic_launcher_foreground.xml`, `drawable/ic_launcher_background.xml`, `drawable/ic_launcher_monochrome.xml`, and both `mipmap-anydpi-v26/ic_launcher*.xml` resources. Manifest icon references remain valid.
- This is a vector adaptation inspired by the earlier generated luminous raster, not its pixel-exact copy. The Figma SVG includes richer gradient fills; the Android VectorDrawable uses a contrast-optimized color simplification for crisp density-independent launch icons.
- Adaptive shape masks, themed-icon contrast, small-device recognition and Google Play asset/export compliance require manual QA. See [LAYASWARNA-APP-ICON.md](LAYASWARNA-APP-ICON.md).

## 2026-11-05 — Figma Mystic screen-to-Compose integration

The newest detailed Mystic reference frames are **50:2** (Chakra Library), **72:2** (Home), **72:82** (Profile Library), **73:2** (Chakra Console), and **73:101** (Anahata Nada). The earlier 14-screen design study is retained as the shared reference for Welcome, Player, Custom Sound, Timer and informational screens.

The Android source on `feat/pifauna-phone-audio` has now been updated to follow those designs:

- `ui/Components.kt`: flat dark-violet Mystic header, 24dp gutter, serif heading, rounded translucent-looking panels, optional grouped insight sections, and a separate gold full-width primary action after the contextual information. These changes apply to all routes through the shared `PfScreen`.
- `ui/RadialSelector.kt`: slightly larger responsive radial wheel, static amethyst glow, gold orbit lines, subtle stars, highlighted selected item, enlarged center label, and no center Open target. The previously verified single-tap, rotary/drag selection, stationary long-press and double-tap behavior is retained.
- `app/src/main/res/drawable/ic_chakra_*.xml`: seven **exact 24×24 Figma icon path geometries** translated from `50:33`, `50:37`, `50:40`, `50:45`, `50:49`, `50:53`, `50:57` into Android VectorDrawable resources. The interactive node still provides the accessibility label, not the decorative icon.
- `screens/BrowseScreens.kt`: Home, Profiles, waveform and timer now place a dynamically selected information card between the wheel and a visible accessible action. Saved presets and navigation to meditation concepts remain discoverable below the main design.
- `screens/ConceptScreens.kt`: the selected Chakra Insights are grouped in one compact panel (traditional association, listening practice, short session and evidence note). Chakra Console retains a **Prepare** action rather than falsely implying auto-play; Anahata Nada uses a sound selector, per-sound status panel, and existing bounded duration choices.
- `PiFaunaApp.kt`: Anahata duration chips are connected to the existing `PlayerController` limits and setting persistence; they cannot lengthen a live session.

**Safety-preserving deviations from the Figma mockups:** the mock Anahata 45-minute chip remains **excluded** (30-minute hard cap); pink/white noise are still shown as **planned/unavailable**, rather than playing mislabeled tones; the Chakra Console action prepares but does not start audio; the Android status and navigation bars are real system controls, not fake UI glyphs; Home's animated emojis are replaced by original vector/Canvas pictograms. No photos, Figma screenshot bitmaps or temporary Figma URLs were embedded as app assets.

**Build/release boundary:** the worker's previous API-36 signed `versionCode 7` AAB was produced **before these screen commits**. Rebuild and re-sign from the updated GitHub head before uploading a bundle intended to include this Mystic pass. The signing keys, SDK 36 settings, and all audio policy code were not modified here. The GitHub Actions build on the latest UI commit must succeed; actual Android 16 visual comparison, 200% font scale, TalkBack and real external-route tests remain release QA.


## Screen-lock playback — three new editable Figma frames (2026-11-06)

- [Settings OFF default, node 102:48](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=102-48)
- [Settings ON opt-in, node 102:73](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=102-73)
- [Media notification with Stop, node 102:98](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=102-98)

All layers are editable and follow the Mystic dark-violet/gold/rose theme. SettingsScreen and PlayerScreen bind Material3 switches to the default-Off local preference. The notification frame is a concept; real Android notification layouts vary. Screen-lock playback was merged with speaker route verification into versionCode 11, hardware-tested on physical devices, and published to Google Play Internal testing. See SCREEN-LOCK-PLAYBACK.md.

## 2026-11-07 Audit reconciliation & current state

- **Theme & Tokens:** The active theme in `Theme.kt` is the Modern-Mystic dark-violet (`#0B0716`), gold (`#E8C872`), amethyst (`#8B6CFF`), and muted rose/chakra node colors. The legacy green (`#0C211D`) is preserved in this document only as historical context.
- **Concept Screens Implemented:** Chakra Sound Console (`ChakraConsoleScreen`), Seven Chakra Library (`ChakraLibraryScreen`), and Anahata Nada Studio (`AnahataNadaScreen`) are all implemented in `ConceptScreens.kt` with dynamic insights and safety-bounded controls.
- **Radial Selector Wheel:** Implemented in `RadialSelector.kt` and active across Home, Profiles, Waveform, and Timer selection flows.
- **Navigation Structure:** 15 total destinations defined in `Route` and hosted in `PiFaunaApp`.
- **String Resource Migration:** Hardcoded Compose strings are inventoried and extracted into `res/values/strings.xml` to prepare for Bengali, Hindi, and Assamese localization without disrupting layout or behavior.
