# Layaswarna — Adobe Express Design System & Modern-Mystic Specification

**Version:** 0.8.0 (v13)  
**Status:** Approved Specification & Production Asset Library  
**Target Repository:** `methakon/smallandroidapp`  
**Branch:** `feat/layaswarna-mantra-studio-v13`  
**Design Philosophy:** Modern-Mystic — Sober, calm, matte, legibly contrast-rich, free of garish neon glows or cluttered gradients.

---

## 1. Visual Token Architecture

The Layaswarna Modern-Mystic visual language is built upon deep cosmic purples, matte obsidian canvases, subdued antique gold highlights, and clean ivory typography. All elements comply with WCAG 2.1 AA contrast standards (minimum 4.5:1 for body copy, 3.0:1 for large display elements).

### 1.1 Color Tokens

| Token Name | Hex Code | Role & Usage | Contrast vs Canvas |
|---|---|---|---|
| `canvas-bg` | `#0B0716` | Main application background (Deep Obsidian / Near-Black Violet) | 1:1 (Base) |
| `card-surface` | `#241B35` | Primary elevation container (Matte Subdued Plum) | 1.8:1 |
| `card-surface-elevated` | `#33274A` | Secondary elevation / active card highlight | 2.5:1 |
| `border-divider` | `#625C70` | Subtle structural 1dp divider and card outline | 3.6:1 |
| `text-primary` | `#F4EEFF` | High-contrast body text, headings, and primary indicators (Ivory) | **17.8:1** (AAA) |
| `text-secondary` | `#BEB6CA` | Subtitles, labels, secondary metadata, unit markers (Mauve) | **9.2:1** (AAA) |
| `text-tertiary` | `#827B91` | Inactive chips, disabled labels, placeholder text | **4.6:1** (AA) |
| `accent-primary` | `#E8C872` | Primary interactive buttons, active chips, key status (Antique Gold) | **13.5:1** (AAA) |
| `accent-glow` | `#FFDE8A` | Subtle focal highlights, active stroke accent | **15.8:1** (AAA) |
| `status-danger` | `#FF6B6B` | Stop button, recording active indicator, delete confirmation | **7.5:1** (AAA) |
| `status-warning` | `#FFD93D` | Volume limit warning, route caution indicator | **14.2:1** (AAA) |
| `status-safe` | `#6BCB77` | Verified built-in speaker route badge, battery safe badge | **9.1:1** (AAA) |

### 1.2 Typography Hierarchy

- **Font Family (Display / Headings):** `Outfit`, `Cinzel`, or Android system sans-serif `sans-serif-medium`.
- **Font Family (Body / Controls):** Android system `sans-serif` (clean, un-stylized for optimal legibility).

| Style | Size (sp/px) | Weight | Line Height | Letter Spacing | Purpose |
|---|---|---|---|---|---|
| **Display Large** | 32sp | 700 (Bold) | 38sp | -0.5px | Welcome hero title, main modal headers |
| **Headline Medium** | 24sp | 600 (SemiBold) | 30sp | 0.0px | Screen headers, Japa repetition count |
| **Title Medium** | 18sp | 600 (SemiBold) | 24sp | 0.2px | Card titles, recording names, preset names |
| **Body Large** | 15sp | 400 (Regular) | 22sp | 0.1px | Explanatory copy, consent descriptions |
| **Body Small** | 13sp | 400 (Regular) | 18sp | 0.2px | Timestamps, durations, safety notices |
| **Button Label** | 15sp | 600 (SemiBold) | 20sp | 0.5px | Action buttons, chip selectors, dialog actions |
| **Status Tag** | 11sp | 700 (Bold) | 14sp | 1.0px | UPPERCASE route badges, volume indicators |

### 1.3 Surface Geometry & Spacing

- **Minimum Touch Target:** 48dp × 48dp on all interactive elements (chips, buttons, icon hitboxes).
- **Corner Radii:**
  - Standard cards: `16dp`
  - Floating action buttons / primary controls: `24dp` or `32dp`
  - Chip selectors: `20dp`
  - Circular buttons / progress indicators: Full circular (`50%`)
- **Card Strokes:** 1dp solid border using `#625C70` with 35% opacity. Never apply heavy dropped shadows or high-frequency blur glows.

---

## 2. Screen Inventory & Layout Architecture

The complete Layaswarna system comprises 21 distinct screen views: 15 existing core screens refreshed into the Modern-Mystic style, and 6 new Mantra Studio views.

### 2.1 Existing Core Screens (Refreshed)

1. **Welcome Screen:** Minimalist brand introduction with antique gold lotus icon, sober tagline ("Harmonic Resonance for Mind & Prana"), and explicit onboarding permissions summary.
2. **Home Screen (Radial Chakra Navigator):** Interactive circular arrangement of 7 energy centers with central frequency readout and direct navigation card to Mantra Studio.
3. **Sound Profiles:** Clean list of master sound configurations with dual-tone frequency selectors and volume cap safeguards.
4. **Player Screen (Synthesizer):** Real-time dual-oscillator frequency synthesizer with interactive waveform preview, safety status bar, and stop timer.
5. **Timer Selection:** Quick duration tiles (5m, 10m, 15m, 30m) with strict 30-minute hard cap notification.
6. **Custom Sound Creator:** Carrier and beat frequency dials with real-time hearing safety boundary enforcement.
7. **Settings Screen:** General preferences, audio routing override warnings, opt-in screen-lock playback toggle (default OFF).
8. **About & Privacy:** Zero-analytics policy, local-only storage guarantee, independent audio consent statement.
9. **Safety Center:** Complete breakdown of the 60% media volume threshold, built-in speaker requirement, and battery safeguards.
10. **Chakra Library:** Educational reference guide for the 7 primary chakras, traditional bija mantras, and associated frequencies.
11. **Chakra Console:** Granular tuning controls for root, sacral, solar plexus, heart, throat, third eye, and crown tones.
12. **Anahata Nada (Heart Center):** Specialized 432 Hz / 528 Hz harmonic player with gentle pulse animation.
13. **Device Protection:** Live sensor dashboard displaying device thermal status, battery level, and hardware audio route.
14. **Battery Saver Mode:** Low-power rendering toggle, background task termination policy, screen-off power consumption monitor.
15. **Playback Safety Interlock:** Detailed diagnostic screen illustrating fail-closed state when headphones or Bluetooth audio are active.

### 2.2 Mantra Studio Screen System (New v13)

1. **Mantra Studio Entry (`03_mantra_studio_entry.svg`):**
   - Header with back navigation, antique gold "Mantra Studio" title, and subtitle "Personal Chant & Japa Practice".
   - Educational explainer card emphasizing private internal storage (`No cloud upload · Stored only on this device`).
   - Primary Action Button: "Record New Mantra" (`#E8C872` fill with `#0B0716` text).
   - "Saved Chants" catalog header and empty/filled state listing takes with duration chips.
2. **Record Capture (`04_mantra_record_capture.svg`):**
   - Live recording state with pulsed concentric recording rings.
   - Prominent elapsed time counter (`00:04 / 01:30`) and active status badge (`● RECORDING · MIC ACTIVE`).
   - Microphone permission explainer banner.
   - Large 72dp Circular Stop Button (`#FF6B6B` danger background with ivory stop square).
3. **Review & Retry Take (`05_mantra_review_retry.svg`):**
   - Preview card with waveform representation and recorded duration badge (`Duration: 15s`).
   - Audio preview button ("Play Preview" / "Stop Preview").
   - Title input field with hint "e.g., Gayatri Mantra or Daily Chant".
   - Dual actions: "Retry Take" (subdued outline) and "Save Chant" (antique gold primary).
4. **Saved Recordings Manager:**
   - Catalog of saved recordings with custom titles, recording timestamps, and file size.
   - Action buttons: "Practice" and "Delete".
   - Confirmation dialog for permanent deletion: "Delete 'Daily Chant' permanently from this device? This action cannot be undone."
5. **Counted Session Setup (`06_mantra_counted_setup.svg`):**
   - Active chant banner showing selected title and duration.
   - Japa Repetition Selector chips: `11`, `21`, `58`, `108` repetitions.
   - Silence Gap Selector chips: `0.5s`, `1.0s`, `2.0s`, `3.0s` inter-repetition silence.
   - 30-minute session duration safeguard notice.
   - Primary Action Button: "Begin Japa Practice".
6. **Live Counted Japa Playback (`07_mantra_live_japa_playback.svg`):**
   - Circular progress indicator showing active repetition vs target (`1 / 11`).
   - Progress bar with percentage completion.
   - Status indicators: `Playing chant...` or `Silence gap: 1.0s remaining`.
   - Verified output route badge (`● PHONE SPEAKER VERIFIED`).
   - Prominent "Stop Session" button (`#FF6B6B`).

---

## 3. Rendered Design Assets Index

High-fidelity 1080×1920 (9:16 mobile aspect ratio) vector SVGs and rendered review PNGs are located in the repository under `PiFauna/design/adobe-express/`:

| Screen Name | Vector Template (SVG) | Rendered Asset (PNG) | Primary Focus |
|---|---|---|---|
| **01 Welcome** | [01_welcome.svg](file:///home/swarna-sekhar-dhar/projects/smallandroidapp/PiFauna/design/adobe-express/svg/01_welcome.svg) | [01_welcome.png](file:///home/swarna-sekhar-dhar/projects/smallandroidapp/PiFauna/design/adobe-express/png/01_welcome.png) | Onboarding, Brand Identity, Permissions |
| **02 Home Radial** | [02_home_radial.svg](file:///home/swarna-sekhar-dhar/projects/smallandroidapp/PiFauna/design/adobe-express/svg/02_home_radial.svg) | [02_home_radial.png](file:///home/swarna-sekhar-dhar/projects/smallandroidapp/PiFauna/design/adobe-express/png/02_home_radial.png) | 7-Chakra Radial Hub + Mantra Studio Access |
| **03 Mantra Entry** | [03_mantra_studio_entry.svg](file:///home/swarna-sekhar-dhar/projects/smallandroidapp/PiFauna/design/adobe-express/svg/03_mantra_studio_entry.svg) | [03_mantra_studio_entry.png](file:///home/swarna-sekhar-dhar/projects/smallandroidapp/PiFauna/design/adobe-express/png/03_mantra_studio_entry.png) | Privacy Guarantee, Record Action, Saved Chants |
| **04 Record Capture** | [04_mantra_record_capture.svg](file:///home/swarna-sekhar-dhar/projects/smallandroidapp/PiFauna/design/adobe-express/svg/04_mantra_record_capture.svg) | [04_mantra_record_capture.png](file:///home/swarna-sekhar-dhar/projects/smallandroidapp/PiFauna/design/adobe-express/png/04_mantra_record_capture.png) | Live Audio Recording, Wave Rings, Stop Control |
| **05 Review & Retry** | [05_mantra_review_retry.svg](file:///home/swarna-sekhar-dhar/projects/smallandroidapp/PiFauna/design/adobe-express/svg/05_mantra_review_retry.svg) | [05_mantra_review_retry.png](file:///home/swarna-sekhar-dhar/projects/smallandroidapp/PiFauna/design/adobe-express/png/05_mantra_review_retry.png) | Audio Preview, Title Input, Retry / Save |
| **06 Counted Setup** | [06_mantra_counted_setup.svg](file:///home/swarna-sekhar-dhar/projects/smallandroidapp/PiFauna/design/adobe-express/svg/06_mantra_counted_setup.svg) | [06_mantra_counted_setup.png](file:///home/swarna-sekhar-dhar/projects/smallandroidapp/PiFauna/design/adobe-express/png/06_mantra_counted_setup.png) | 11/21/58/108 Reps, 0.5s–3s Gaps, 30m Cap |
| **07 Live Japa Playback** | [07_mantra_live_japa_playback.svg](file:///home/swarna-sekhar-dhar/projects/smallandroidapp/PiFauna/design/adobe-express/svg/07_mantra_live_japa_playback.svg) | [07_mantra_live_japa_playback.png](file:///home/swarna-sekhar-dhar/projects/smallandroidapp/PiFauna/design/adobe-express/png/07_mantra_live_japa_playback.png) | Circular Progress, Repetition Counter, Route Interlock |
| **08 Sound Player** | [08_sound_player.svg](file:///home/swarna-sekhar-dhar/projects/smallandroidapp/PiFauna/design/adobe-express/svg/08_sound_player.svg) | [08_sound_player.png](file:///home/swarna-sekhar-dhar/projects/smallandroidapp/PiFauna/design/adobe-express/png/08_sound_player.png) | Dual-tone Synthesizer Player, Waveform, Safe Route |

---

## 4. Adobe Express Workflow & Editing Guide

To edit, customize, or export these designs using an authorized Adobe account:

### 4.1 Importing to Adobe Express
1. Open a browser and navigate to **[express.adobe.com](https://express.adobe.com)**.
2. Sign in with the authorized Adobe account active on your workstation.
3. Click **"+" (Create new)** → **Custom size** → enter **1080 px width** by **1920 px height** (or choose standard Mobile / Story format).
4. From the left sidebar, click **Media** → **Upload from device**.
5. Select any of the SVG vector template files from `PiFauna/design/adobe-express/svg/`.
6. Adobe Express will parse the SVG vector nodes into editable shapes, text layers, and grouped containers while preserving exact RGB color values.

### 4.2 Editing Guidelines
- **Color Consistency:** Use the color hex values defined in Section 1.1. Do not introduce saturated neons, high-contrast cyan, or multi-stop radial rainbow gradients.
- **Text Layers:** Double-click on any text block to edit copy. If Adobe Express substitutes a missing font, select `Outfit`, `Century Gothic`, or `Montserrat` for headers, and standard `Open Sans` or `Roboto` for body text.
- **Icons & Symbols:** The lotus, chakra mandalas, microphone, and speaker icons are vector paths. You can scale them proportionally while holding `Shift`.
- **Exporting:** When finished editing, click **Download** in the top navigation bar → choose **PNG** (lossless) or **SVG** (vector) → select **Original Size (1080×1920)**.
