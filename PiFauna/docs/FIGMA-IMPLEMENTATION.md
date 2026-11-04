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
