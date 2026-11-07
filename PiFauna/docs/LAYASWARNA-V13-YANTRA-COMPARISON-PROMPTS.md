> **DESIGN DECISION (2026-11-08): FIGMA ONLY.** This Adobe/Figma comparison is historical. The selected live editable artboards and Android implementation mapping are in [FIGMA-V13-SOURCE-OF-TRUTH.md](FIGMA-V13-SOURCE-OF-TRUTH.md). Do not generate or request Adobe Express designs for this release.\n\n# Layaswarna v13 — Side-by-side Figma and Adobe Express japa dial comparison

**Design status:** Figma concept screens are written in the connected Layaswarna Figma file. Adobe Express alternative requires a selected editable template/document; its template gallery has been opened for selection. This is not evidence that an Adobe document was generated. All designs must represent the corrected sets-of-108 mathematics.

## Product truth — non-negotiable

- **One full japa set = exactly 108 chants of the selected recording**.
- Outer concentric selector: **1, 11, 21, 58, 108 sets**.
- Derived overall targets: **108, 1,188, 2,268, 6,264, 11,664 chants**, respectively.
- Inner concentric selector: **0.1 / 0.25 / 0.5 / 0.75 / 1 / 2 / 3 seconds silence between each pair of chants**.
- Estimate in milliseconds: `recordingDurationMs * (selectedSets * 108) + gapMs * (selectedSets * 108 - 1)`.
- Text must distinguish set count, repetitions per set, total chants, selected gap, full estimated duration, current set, within-set progress, and overall completed.
- User may explicitly continue after the existing 30-minute foreground safety stop. Never auto-restart, run overnight, or silently pretend the set finished.
- Temporary preview = **one chant only**, NOT a full 108-count set.
- No network, covert microphone use, claims of supernatural/medical effects, or unauthorized audio routing.

## Artboard 1 — Japa cycle setup (390×844 dp)

**Top bar:** Layaswarna · MANTRA STUDIO  
**Heading:** Set your japa cycle  
**Subheading:** Outer ring: sets · Inner ring: silence

**Primary interaction:** original two-ring circular sacred-geometry-inspired dial.

- Outer orbit radius ~140dp. Exactly five large gold-accented selectable sectors, labelled **1 / 11 / 21 / 58 / 108**. Add ring label **SETS OF 108**.
- Inner orbit radius ~90dp. Exactly seven violet/mauve selectable sectors, labelled **0.1s / 0.25s / 0.5s / 0.75s / 1s / 2s / 3s**, with the label **PAUSE BETWEEN CHANTS**.
- Center medallion displays **11 × 108** and **1,188 TOTAL CHANTS** for the illustrative selected state.
- Use thin concentric circle accents, delicate celestial ticks, abstract radial geometry, lightly illuminated selected sectors and subtle lotus-petal geometry. **Do not copy Doctor Strange movie artwork or Marvel emblems.**
- Never place illegible symbolic glyphs in place of actual numerals; labels must stay understandable at 360dp width and 200% text.
- Provide conventional accessible chips below the dial as an alternative to precise radial touch.
- Summary: **Morning Chant** / **11 sets × 108 = 1,188 chants** / **Silence: 1 second** / **Estimated full time: 2h 18m** (example only if underlying recorded duration supports it). Real UI dynamically calculates time.
- Warning: **More than one session needed. Your current session stops after 30 minutes; choose Continue to resume.**
- CTA: **Start practice**. Never suppress valid large set choices solely because the full total is longer than 30 minutes.

## Artboard 2 — Active japa practice (390×844 dp)

**Heading:** Your mantra practice  
**Subheading:** Morning Chant · Counted japa  
**Outer dial state:** 3 of 11 sets, with gold arc progress.  
**Inner dial state:** 67 of 108 chants in current set, with violet arc progress.  
**Center:** **SET 03 / 11** · **67 / 108**.  
**Lower summary:** **283 / 1,188 chants completed** · **Playing chant**.  
**Prominent immediate control:** **STOP PRACTICE**.  
**Safety copy:** Phone speaker only · No unattended playback · 30-minute session maximum.

## Artboard 3 — Interrupted practice and explicit continuation

**Heading:** Practice paused at the safety limit  
**Copy:** Your 30-minute session has ended. No audio is playing. Your progress is recorded for this practice.  
**Summary:** Completed 283 / 1,188 · Set 3 of 11 · Next chant 68 of 108.  
**Primary action:** Continue from chant 284 (explicitly user-initiated).  
**Secondary:** End practice / Start over.  
**Privacy note:** Your recorded voice remains stored only on this phone.

**Technical limitation:** initial prototype holds progress in Compose state; process-restart persistence is a required Antigravity follow-up before release. Do not display “progress saved” across restarts until implemented.

## Artboard 4 — Redesigned home

- Heading: **Layaswarna**
- Eyebrow: **SOUND · CHAKRA · MANTRA**
- Hero: **A quieter space for your practice**
- Feature CTA: **Mantra Studio · Record a chant in your own voice and repeat it in sets of 108**
- Secondary cards: Sound Profiles · Chakra Library · Anahata Nada · Custom Sound
- Bottom nav: Home · Library · Player · Settings
- Footer: **Offline by design · Your recordings stay on this phone**
- Keep existing features reachable, add a clear Mantra Studio destination, avoid suggesting medical/repellent efficacy.

## Adobe Express AI design prompt (full, self-contained)

> Create a polished, editable multi-page mobile app visual design presentation titled “Layaswarna — Japa Yantra Studio”. Produce FOUR distinct portrait mobile artboards or pages: (1) japa setup dual concentric selector; (2) active 108-count practice; (3) long-session interruption and explicit Continue; (4) redesigned Layaswarna home. Artboards should visually resemble implementable 390×844dp Android phone screens; actual export can use 1080×1920px. Style sober, spiritual, modern, futuristic, and practical: near-black violet background #0E0D17, matte cards #231F2E, ivory text #F5F2F7, muted antique-gold outer orbit #DEC588, restrained lavender inner orbit #947FCD, and grey-purple explanatory text #BEB6CA. Use original layered geometric yantra/sacred-geometry-inspired concentric dials with thin celestial ticks and subtle lotus linework. Never copy Doctor Strange, Marvel, or third-party copyrighted movie symbols. Outer ring values must be 1 / 11 / 21 / 58 / 108 **SETS**, each set equal to 108 chants; inner ring values 0.1s / 0.25s / 0.5s / 0.75s / 1s / 2s / 3s denote the quiet pause between individual chant repeats. Center must show selected set count and accurately computed chant total; for 11 sets show 1,188 chants. Example active progress: Set 3/11, 67/108 within set, 283/1,188 overall. Include title “Morning Chant”, dynamic estimated duration summary, immediate Stop control, 30-minute per-start safety warning, and manual Continue control after an interruption. Provide a conventional labelled choice row for accessibility. Add redesigned home with prominent Mantra Studio card, Sound Profiles, Chakra Library, Anahata Nada and Custom Sound navigation. Use restrained typography, clear contrast, large human-readable numerals, 48dp touch targets, subtle rather than garish glows. All text and background shapes should remain editable and organized into logical pages. Do not represent mockups as already-implemented features.

## Figma AI prompt (for refinement or alternative)

> In the existing Layaswarna Figma design file, build editable Compose-friendly mobile UI frames for Japa Setup, Active Practice, Explicit Continue and New Home, each 390×844. Use an original yantra-inspired double radial dial: outer ring selects 1/11/21/58/108 *sets of 108*, inner ring selects 0.1/0.25/0.5/0.75/1/2/3-second silence between chants, center computes sets×108. Represent active progress with two concentric progress arcs and explicit counters (Set 3/11, chant 67/108, 283/1188 completed). Add accessible normal text chips. Palette dark #0E0D17, gold #DEC588, lavender #947FCD, muted surfaces #231F2E, accessible ivory #F5F2F7. Use modular components, editable text/vector shapes, consistent 8dp spacing and realistic Android navigation. Make imagery original; no Doctor Strange/Marvel marks. Long sets require explicit user-started 30-minute sessions, never unattended loop. Include home hero and Mantra Studio feature card without dropping existing profiles/Chakra/Anahata screens. Compare the output to previously created Figma japa frames (node 113:52 and 113:120), then polish contrast and spacing.

## Design evidence

Existing edited Figma file:
- [Setup · editable](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=113-52)
- [Active · editable](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=113-120)

These mockups are examples, not a Play Store release or a guarantee of hardware safety. The Adobe tool currently requires selecting a template from the gallery; do not claim an Adobe edit finished unless the connector returns a terminal success and editor link.

## Comparison scorecard (apply after both design outputs exist)

Evaluate each Figma and Adobe alternative across: outer/inner controls clarity (25%), correctness of 108-multiplication and session cap (25%), readability and accessibility (20%), aesthetic sophistication without copied artwork (15%), and accuracy/ease of Jetpack Compose implementation (15%). Reject any concept that suggests an infinite unattended loop, hides Stop or mislabels sets as raw chants.
