# Layaswarna — Lotus + Soundwave Launcher Icon

Updated: 2026-11-05  
Status: **Editable Figma source and Android adaptive launcher resources committed; latest CI / device validation must be checked**.

## Design source and concept

- [Editable Figma artboard](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=62-26) — `Layaswarna / App Icon / Lotus Soundwave Master`, artboard ID `62:26`.
- [Canonical 512px vector SVG](assets/layaswarna-lotus-soundwave-icon.svg) — first-party source, suitable for future marketing/export, not a copied third-party icon.
- The Figma artwork is a **vector interpretation** of the previously approved generated lotus/sound-wave concept. The original generated raster is a visual reference, not an identical bitmap inside the app.
- A central pink-violet lotus represents heritage, meditation and the chakra-inspired sound library. Golden equalizer bars represent **audible sound synthesis**; concentric purple/gold resonance rings evoke selection and rhythm. Tiny multicolored dots reference seven chakra concepts. No letters or unsupported physiological claims are embedded.
- Strong contrast and a simple silhouette are prioritized at small launcher sizes. The Figma artboard includes 512px, 128px and 48px examples.

## Android integration

This project supports Android API 26+, so the launcher uses platform **adaptive icons** rather than a collection of legacy raster mipmaps:

| Resource | Purpose |
|---|---|
| `app/src/main/res/drawable/ic_launcher_foreground.xml` | New lotus/soundwave colored `VectorDrawable`, scaled into Android's adaptive foreground safe area |
| `app/src/main/res/drawable/ic_launcher_background.xml` | Violet radial-gradient backdrop (the existing Android drawable is retained) |
| `app/src/main/res/drawable/ic_launcher_monochrome.xml` | Original monochrome lotus silhouette for supported themed launchers |
| `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` | Adaptive icon: background, foreground, monochrome |
| `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml` | Same resources for round-mask launchers |
| `app/src/main/res/values/colors.xml` | Deep-violet backing token for launcher compatibility |
| `app/src/main/AndroidManifest.xml` | Existing `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round` references remain unchanged |

The Figma master uses richer SVG gradient fills; Android's colored foreground uses solid vector colors with gold outlines and violet background for predictable rendering. The two are designed to be visually consistent, **not pixel-identical**.

## Required validation

1. Build `cd PiFauna && ./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug`; also compile `assembleDebugAndroidTest`.
2. Inspect launcher on Android 8 / API 26 and on a recent Android 13+ launcher, with **round and squircle masks**. Verify no petals/wave bars are cropped by icon masking.
3. Check themed/monochrome launcher in both dark and light launcher color schemes. The user may apply a system tint to the monochrome layer.
4. Check icon appearance at 48px, 72px, 128px and a 512px Google Play listing export, including clarity of the golden sound bars. Store listing assets must be explicitly exported from the Figma artboard before submission.
5. Confirm the app remains `in.berhampore.dhar.layaswarna`; no new Android permissions, runtime audio modes or efficacy claims were added.

**Provenance:** Original visual concept was generated for this project in the ChatGPT conversation; the Figma SVG and Android resources are newly authored recreations of its generic lotus-plus-soundwave elements. No downloaded third-party logos, paid icons, source fonts or sound recordings were incorporated. This document records the artwork process, not a legal certification or clearance opinion.
