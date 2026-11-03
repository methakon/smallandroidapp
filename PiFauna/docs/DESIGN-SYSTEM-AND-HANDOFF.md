# PiFauna — Figma UX/design handoff

## Figma source of truth

[PiFauna — Android Bioacoustic Lab | UX & Design System v0.1](https://www.figma.com/design/5HCEW8MLKjVTpJ3XW0s7xM)

The file contains four **editable native Figma screens**, component instances from the **Material 3 Design Kit**, local color variables, and a single original **Figma AI-generated decorative illustration**. Figma AI image generation used plan credits; the illustration is an image asset, **not** an editable vector or generated functional app.

## Screen inventory

| Figma frame | Scope | Current source status |
|---|---|---|
| 01 Generator | Observation group, ranges, signal pattern, limit, primary action | MainActivity provides equivalent controls in one scrollable screen |
| 02 Profiles | Species cards, illustrative bands, cautionary descriptions | Data available in `SpeciesProfiles`, separate screen **not built** |
| 03 Journal | Local entries, result descriptions, export | SharedPreferences and CSV implemented; separate screen **not built**; Figma uses illustrative example rows |
| 04 Safety & Presets | Warning, safety gates, save custom range | Warnings and local save implemented in single Activity; separate screen **not built** |

## Visual tokens (Figma local variables)

| Token | Hex | Role |
|---|---|---|
| teal | `#076D65` | Primary brand/action |
| ink | `#173239` | Primary typography |
| muted | `#59747A` | Supporting text |
| bg | `#F7FAF8` | App canvas |
| white | `#FFFFFF` | Cards |
| mint | `#DEF1E9` | Information panels |
| navy | `#123F43` | Hero / strong contrast |
| lime | `#C5E97E` | Accent |

Typeface: **Roboto**, using Regular / Medium / Bold, consistent with Android sans-serif expectations. Figma primary buttons are linked Material 3 component instances with PiFauna teal overrides. Target artboard: 390 × 844 px Android mockup; implement responsive dp layouts, **do not equate pixels with dp without density conversion**.

## Interaction rules for implementation

- Tap Start → validate fields, route, volume, show consent on first start → run bounded tone.
- UI must show clear Stop control during playback. Show separate ready, playing, stopped, completed, refused and error states.
- Disable Start while playing, and always enable immediate stop affordance; never use background autoplay.
- Profile selection is informational; users can override the range; no “repels” success indicator.
- Journal uses user-entered outcomes; example rows in Figma **must never** ship as actual data.
- Explicit disclosures accompany selectable research groups and exported results.
- Accessibility: minimum 48 dp tap target, reasonable dynamic text size, contrast check, talkback labels and frequency unit in all numeric fields.

## Engineering handoff / traceability

- Figma `01` → `MainActivity` configuration / `ToneKernel` controls.
- Figma `02` → `SpeciesProfiles.list`; future Profiles destination.
- Figma `03` → `saveObservation()`, `refreshObservations()`, `exportCsv()`.
- Figma `04` → `requestStart()` safety checks, `promptSavePreset()`.

**Important:** Keep initial code deploy focused on the validated prototype. The Figma file is an **aspirational UX design**, not a claim of completed app features. The AI illustration should be stored in Figma or exported with documented rights before shipping in Android; it is not bundled in source assets.