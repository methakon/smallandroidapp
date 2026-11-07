# Layaswarna — Low-Glare Temple Palette (2026-11-04)

**Status:** Applied to all 11 existing Figma screens and 3 concept screens; Android code is not yet updated.

## Design rationale
The previous #E94B26 vivid orange control fill, #FF781F bright brand accents and dark burgundy backdrop produced a high-saturation appearance. Warm hue **alone** is not a sleep-safety property: perceived screen brightness, surrounding illumination, viewing duration and device night settings matter. The Sleep Health Foundation recommends dimming evening screens; the American Academy of Ophthalmology discusses night mode and reducing screen exposure. WCAG 2.2 AA calls for >=4.5:1 normal text contrast and >=3:1 for large text and meaningful graphical UI elements. These are design targets, not certification of this Figma file.

## Revised Figma token mappings

| Token | Previous | Revised | Intent |
|---|---|---|---|
| Main backdrop | #321922 | **#241D28** | softer midnight plum |
| Hero panel | #9D392E | **#65414A** | quiet temple rose-brown |
| Standard control | #552630 | **#40313F** | low-glare plum slate |
| Emphasized control | #E94B26 | **#78444F** | muted terracotta rose |
| Brand/accent | #FF781F | **#D6A080** | restrained sandstone copper |
| Secondary accent | #FFC7A4 | **#CDAAA5** | warm carved-stone tint |
| Muted text | #F6C8B5 | **#E6D1CB** | warm ivory blush |
| Lotus linework | #F36B9B | **#B67D91** | subdued dusty rose |
| Main text | #FFF3E3 | **#F4E8E3** | softened ivory |

No neon orange, fluorescent pink or green is introduced. Historical/heritage detail remains through temple relief and original Chakra vector ornaments; large vivid fills are eliminated. Screens retain original hierarchy and actions.

## Night/sleep feature guidance
- Add a future **Sleep Studio** night preset with still darker background, simplified decoration and restrained warm controls, separate from ordinary daytime theme.
- Respect system dark/night mode and user display brightness. App colors cannot guarantee sleep quality or avoid circadian disruption.
- No flashing, pulsing visual Chakra animation, bright playback flashes or automatic display wakeups.
- Keep explicit play/stop labels, focus rings and readable contrast; audit the *composited* text and image backgrounds, not token values alone.
- Do not increase audio playback duration or weaken existing battery/thermal/volume safety restrictions.

## References
- https://www.sleephealthfoundation.org.au/sleep-topics/technology-and-sleep
- https://www.aao.org/eye-health/tips-prevention/should-you-use-night-mode-to-reduce-blue-light
- https://www.w3.org/TR/WCAG22/

**QA note:** Screenshots were regenerated for Home, Sound Player and Anahata Nada Studio after recoloring. Full device-level contrast and visual regression review remains required.

## 2026-11-05 update: modern-mystic concept palette and accessibility gate

The muted temple palette above is **historical and remains relevant to the current Android implementation**, but the newer Figma **redesign proposal** uses a `#0B0716` to `#1A0F2E` dark-violet backdrop, `#E8C872` gold accent, `#8B6CFF` restrained amethyst glow, `#D69AA8` brand rose, `#F4EEFF` primary and `#B9AFD1` secondary text. The seven muted chakra-node colors are listed in [LAYASWARNA-DESIGN-AUDIT-AND-DELIVERY-PLAN.md](LAYASWARNA-DESIGN-AUDIT-AND-DELIVERY-PLAN.md). The proposed translucent glass surface and blur **require composited contrast testing**; token contrast alone is insufficient.

Audit acceptance: body text >=14sp, captions preferably >=11–12sp, >=48dp effective tap targets, 24dp horizontal gutters, Android system insets, semantic non-Unicode vector icons, selected state beyond color, accessible alternatives to wheel gestures, and real independent Material 3 bottom navigation. The new Figma set illustrates these directions but **does not yet demonstrate a fully constrained component library or WCAG/Android accessibility pass**. Avoid animated glow or pulse when reduced motion is requested; never use flashing effects.

## 2026-11-07 reconciliation: Modern-Mystic palette adoption

The Android application (`Theme.kt`) has officially adopted the Modern-Mystic palette:
- `MysticBackground`: `#0B0716` (deep dark violet canvas)
- `MysticSurface`: `#1A1228` (elevated cards and panels)
- `MysticPrimary`: `#E8C872` (warm radiant gold for highlights and primary actions)
- `MysticSecondary`: `#8B6CFF` (calm amethyst for secondary accents)
- `MysticRose`: `#D69AA8` (soft terracotta rose for selection indicators)
- Seven distinct muted chakra node colors for symbolic radial nodes.

The audio safety contract, digital ceilings, and volume limits remain identical.
