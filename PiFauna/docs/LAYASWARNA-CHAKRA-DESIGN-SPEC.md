# Layaswarna — Chakra Icon Controls & Heritage UI Specification

Status: **design requirements approved for prototyping; Android implementation not started**  
Updated: 2026-11-04  
Figma: https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX  
Repository branch: `feat/pifauna-phone-audio`

## 1. Naming and scope

- Main application: **Layaswarna (लयस्वर्ण)**.
- Planned sleep/relaxation module: **Anahata Nada (अनाहत नाद)**.
- Preserve the existing 11 Android routes, their actions, layout hierarchy, and safety restrictions. Three additional Figma **concept** frames (Chakra Sound Console, Seven Chakra Library, Anahata Nada Studio) do not imply implemented functionality.
- Visual identity: richly carved classical Indian temple architecture, manuscript illumination, lotus medallions, sacred geometry, carved arch borders, high-contrast interactive Chakra buttons.
- Preferred UI accents: temple orange `#FF781F`, vermilion `#E94B26`, lotus pink `#F36B9B`, terracotta `#9D392E`, peach `#FFC7A4`, ivory `#FFF3E3`, dark plum `#321922`. Avoid green or metallic-gold-dominant themes. Chakra iconography may use distinct traditional colors when necessary to distinguish symbols.

## 2. Reference images and intellectual-property requirements

The user supplied screenshot references of black outline Chakra emblems, seven-Chakra diagrams, ornate sacred geometry and temple illustrations. These are **visual research only**, not licensed production assets. Their source, authorship and commercial reuse permissions have **not** been verified.

- **Do not** trace, vectorize, crop, remove watermarks from, or directly distribute the supplied reference icons, screenshots, artwork, diagrams, or their distinctive layouts. No screenshot is evidence of a reuse license.
- Create **original** icons from first principles: generic geometric elements (circles, triangles, lotus petals, spokes, symmetric polygons) and independently constructed traditional Chakra symbolism. Historic cultural motifs and general ideas are not the same as a modern artist's particular rendering.
- Do not copy a source artist's distinctive line weights, ornament sequencing, precise petal outlines, calligraphic treatments, labels, arrangement, or composite icon designs.
- Prefer original Figma vector paths and components; AI-generated decorative artwork may be used only after visual similarity review and asset-provenance documentation. Figma AI output does **not** guarantee non-infringement.
- Do not include third-party logos, watermarks, stock vectors, proprietary fonts, or copyrighted chants/audio without verified commercial licenses.
- Before publication: perform reverse-image/similarity review, confirm font and any third-party asset licenses, document asset source and creation method, and obtain legal review if rights are uncertain. Copyright clearance is a process, not a guarantee.

## 3. Original icon system

Create an original **seven-symbol family** with a consistent 48dp touch target, 24–32dp graphic, minimum 3:1 contrast for critical icon shapes, distinct accessible labels and visible selected/focused/disabled states. Suggested symbol identities, used as *cultural navigation metaphors* only:

| Symbol | Original geometric construction | Suggested UI mapping |
|---|---|---|
| Muladhara | four-petal lotus around a square | Ground / low-frequency preset |
| Svadhisthana | six-petal lotus around crescent geometry | Flow / sweep |
| Manipura | ten-petal radial ring around triangle | Pulse / modulation |
| Anahata | twelve-petal lotus around intersecting triangles | Anahata Nada feature |
| Vishuddha | sixteen-petal radial outline around circle | Tone / sound player |
| Ajna | two-petal shape around center disc | Focus / custom sound |
| Sahasrara | many-petal concentric mandala, simplified at small sizes | Library / overview |

Do not claim that each frequency is medically associated with a Chakra or that sounds heal, entrain sleep, cure disease, improve memory, or guarantee prosperity.

## 4. Control components and behavior

1. **ChakraIconButton**: original SVG/vector emblem, circular hit area >=48dp, text label, semantic action, focus ring, pressed and disabled states; never rely on color alone.
2. **ChakraPlayPause**: large central circle with clearly legible play/pause glyph, decorative lotus outside the hit region; unchanged start/stop behavior.
3. **MandalaFrequencyDial**: optional radial frequency adjustment with numeric Hz readout and +/- buttons or an accessible linear slider fallback; preserve 100–18,000 Hz validated range.
4. **ChakraPresetSelector**: seven decorative symbols as category navigation; not seven unverified medical frequency mappings.
5. **TempleTimer**: timer choices 5/10/15/30 minutes for existing playback; Anahata Nada options remain **proposed**, not enabled until separately implemented and validated.
6. **Safety indicators**: standard unambiguous warning, stop, battery and thermal symbols alongside decoration. Ornate artwork must never obscure safety warnings, frequency units or controls.

Maintain sufficient text contrast, legibility at 200% text scale, TalkBack labels, keyboard navigation where relevant, and reduced-motion preference. Do not make decorative glyphs the only way to understand a control.

## 5. Figma design deliverables

- Keep original 11 frames (390x844) and existing routes intact.
- Create a reusable **Original Chakra Controls** component sheet with seven independent symbols, their selected/unselected/disabled states, a circular play button, frequency control and timer.
- Apply these components to suitable existing screens, particularly Home, Sound Player, Profile Library, Custom Sound and Timer; other screens retain appropriate recognizable controls.
- Keep the three concept frames clearly identified as non-implemented concepts.
- Use original vectors for interactive symbols; Figma AI can create **decorative background textures**, not replace functional accessible UI controls with uneditable images.
- Review all 11 screens for tap-target overlap, clipping, readability and decorative occlusion.

## 6. Engineering and safety constraints

The current code is still named `PiFauna` internally; app/package rename to Layaswarna is a **separate planned code migration**. Do not claim the UI redesign is shipped in the APK. Preserve existing foreground-only playback, hard-stop policies, 8% digital peak cap, <=60% system media volume gate, >15% battery gate, severe-thermal cutoff, audio route checks and session time limits. No medical/therapeutic efficacy claims.

## 7. Acceptance checklist

- [ ] Documentation committed on feature branch **before** new Figma icon edits.
- [ ] Seven original icons independently constructed as editable vector geometry.
- [ ] No reference screenshot or third-party illustration embedded as an app icon.
- [ ] Components have clear names, variants and accessible labels.
- [ ] Original 11 screen layouts and actions retained.
- [ ] All user-visible names use Layaswarna; Anahata Nada reserved for the planned feature.
- [ ] Rights/provenance ledger and visual-similarity review recorded.
- [ ] Screenshots and design reviewed; no gold/green-dominant regression.
- [ ] Android implementation and tests are separately tracked, not marked complete by Figma changes.

## 8. Asset provenance ledger

| Asset | Source | Rights status | Permitted use |
|---|---|---|---|
| User-uploaded Chakra icon reference screenshot | User-provided reference | Unknown | Inspiration and analysis only; **not** bundled |
| User-uploaded heritage/chakra image screenshots | User-provided references | Unknown | Inspiration and analysis only; **not** bundled |
| Figma AI temple texture | Generated in Layaswarna Figma file | Generated, similarity review pending | Concept/background pending review |
| Original vector Chakra icons | To be authored in Figma from geometric primitives | Original work; final similarity review pending | UI prototypes; release only after review |

This specification is design guidance, not legal advice or a certification of copyright clearance.

## 9. 2026-11-05 design evolution — current decision overrides earlier aesthetic proposals

The preceding temple-orange/terracotta palette and central Play/Open wheel suggestions record the **original specification**. The user subsequently approved a **modern-mystic** dark violet, rose, restrained gold and amethyst concept, with seven differentiated chakra-node colors. The current wheel's **center is a readout only**; long-press without rotation or double-tap on a selected symbol opens its details, and an independent visible action remains available for accessibility. For exact tokens, screen IDs, gesture rules and implementation gaps, see [LAYASWARNA-DESIGN-AUDIT-AND-DELIVERY-PLAN.md](LAYASWARNA-DESIGN-AUDIT-AND-DELIVERY-PLAN.md).

The Figma Chakra Library now illustrates a **dynamic Chakra Insights** panel in place of a redundant seven-row name/Hz list. The example association list (396, 417, 528, 639, 741, 852, 963 Hz) is a **modern meditation convention only**, not validated chakra activation or a clinical protocol. The seven icon shapes are original geometric prototypes and require further authenticity, copyright, accessibility and visual review. The four-dot placeholder regression in the modern-mystic wheel was corrected on all seven radial redesign frames with editable vector pictograms.

**Status correction:** the Android application ID/Gradle namespace has already changed to `in.berhampore.dhar.layaswarna` (Kotlin source packages have not moved). Existing Android Home/Profile screens still use separate circular tiles, **not** the approved single regulator. Chakra and Anahata concept routes are not implemented. Figma concept work is not an APK release or test pass.
