# Layaswarna — App icon specification and implementation

Updated: 2026-11-05  
Status: **Launcher resources and Figma master committed; physical-device appearance QA pending**  
Figma file: https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX  
Figma master component: https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=62-25  
Source master: [../design/layaswarna-lotus-soundwave.svg](../design/layaswarna-lotus-soundwave.svg)

## Concept and design provenance

The mark combines a blooming lotus (Layaswarna's Indian-inspired meditation/Chakra identity) with an **integrated audible waveform** (the core experimental sound-generator function), surrounded by resonance rings and restrained multi-colored beads (the symbolic chakra library). No text, third-party brand mark, stock vector, or proprietary glyph is required.

The color study was inspired by the freshly generated radiant lotus/soundwave illustration in this conversation. **The committed SVG is an original editable vector adaptation of that visual direction, not a pixel-identical copy of the generated raster.** The icon remains visually readable at small launcher sizes without relying on the complex raster glow; its source of truth for application resources is the vector geometry.

Palette: midnight violet `#0B0716`, deep violet `#210638`, amethyst `#8B43FF`, lotus rose `#D746CF` / `#E954C6`, vibrant pink `#ED63BB`, golden sound bars and petal outlines `#FFE7A3` / `#FFE08A`, and secondary chakra beads.

## Figma

- **Editable master component:** `Layaswarna · Lotus Soundwave Launcher Icon` (node `62:25`), 512 × 512 px.
- The icon is imported from the same SVG source committed to GitHub and retains editable vector layers instead of a flattened screenshot.
- Keep this component separate from the 14 application screens. Do not substitute a larger hero illustration or screenshot for the launcher icon.
- If the icon is modified in Figma, update the SVG and Android vector drawables together; verify foreground geometry and themed monochrome variant.

## Android resources

| File | Role |
|---|---|
| `app/src/main/res/drawable/ic_launcher_foreground.xml` | Original-color lotus and soundwave scalable vector foreground |
| `app/src/main/res/drawable/ic_launcher_background.xml` | Full-bleed dark-violet radial gradient background |
| `app/src/main/res/drawable/ic_launcher_monochrome.xml` | Separate high-contrast silhouette for Android themed icons |
| `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` | Main adaptive launcher icon |
| `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml` | Round adaptive launcher variant |
| `app/src/main/AndroidManifest.xml` | Already points to `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round` |

Android minSdk is 26, so adaptive icons are supported on all currently supported devices. Foreground geometry is deliberately scaled around the 108dp viewport center for safety under circular/squircle launcher masks. The foreground and the gradient background are separate layers; do not bake a rounded-square border or opaque background into the foreground asset. The standalone 512px SVG is a store-design **source candidate**, not evidence of Play Store listing submission.

**The Android vector is a contrast-optimized color simplification of the SVG's gradient petals.** Full-fidelity raster art was not committed to the app; the adaptive launcher remains crisp at varying densities. The monochrome asset intentionally omits decorative orbit rings and uses a lotus silhouette with a negative-space waveform for legibility under system tinting.

## Verification checklist

- [x] Master icon saved as a component in the connected Figma file.
- [x] SVG master committed to the existing GitHub feature branch.
- [x] Foreground, gradient background, monochrome layers and both adaptive-icon XML entries committed.
- [x] Manifest icon references verified against the resource names.
- [ ] Latest feature-branch Android CI: `:app:assembleDebug`, `:app:lintDebug`, `:app:testDebugUnitTest`.
- [ ] Install on Android 8+ and inspect icon on device launchers with circle, squircle and rounded-square masks.
- [ ] Test themed icon tinting on supported Android 13+ devices; inspect monochrome negative-space waveform.
- [ ] Verify Play Store 512 × 512 export, file size, and brand consistency before publication.

No network, microphone, location, account, telemetry, or new permissions are required by the launcher icon.
