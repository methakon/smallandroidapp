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
| 02 | Home | `1:15` | Implemented | `HomeScreen` (greeting is time-aware) |
| 03 | Sound Player | `1:32` | Implemented | `PlayerScreen` |
| 04 | Profile Library | `1:47` | Implemented | `ProfilesScreen` |
| 05 | Custom Sound | `1:64` | Implemented | `CustomSoundScreen` |
| 06 | Timer | `1:81` | Implemented, one card disabled | `TimerScreen` |
| 07 | Safety | `1:98` | Implemented | `SafetyScreen` |
| 08 | Settings | `1:115` | Implemented, three rows read-only | `SettingsScreen` |
| 09 | Device Protection | `2:2` | Implemented | `DeviceProtectionScreen` |
| 10 | Battery Saver | `2:42` | Implemented | `BatterySaverScreen` |
| 11 | Playback Safety | `2:22` | Implemented | `PlaybackSafetyScreen` |

Navigation: the Figma footer defines three destinations (`Home`, `Profiles`,
`Settings`). Those are the bottom-bar tabs; the remaining eight screens are
pushed on top of the current tab and reached from cards inside them.

## Deliberate deviations from the design

These are product decisions, not unfinished work.

1. **Continuous playback is disabled.** Figma 06 offers a "Continuous" card. The
   app renders it disabled with the explanation "Not offered — every session must
   end on its own", because unattended playback is the main safety hazard this
   app is built to avoid.
2. **Maximum frequency is 18,000 Hz, not 20,000 Hz.** Figma 05 shows a
   "12–20 kHz" band. Nothing above 18 kHz is meaningful through a phone speaker
   and calling it ultrasonic would be false, so the slider stops at 18 kHz.
3. **No background playback toggle.** Figma 08 lists "Background playback". The
   row is present but read-only: there is no service, so the setting cannot
   exist.
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
- **Not implemented:** Three concept routes (Chakra Sound Console, Seven Chakra Library, Anahata Nada Studio), AI raster artwork as app assets, original Figma vector path replication, theme switching, extended sleep playback.
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
