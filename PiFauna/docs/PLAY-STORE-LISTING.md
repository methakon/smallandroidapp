# Layaswarna — Google Play Store listing

Last updated: 2026-11-05. Branch: `feat/pifauna-phone-audio`. Separates **committed/verified assets**, **worker-reported API publication**, and **remaining tasks**.

## Identity and copy

| Item | Value |
|---|---|
| App name | Layaswarna |
| Package | `in.berhampore.dhar.layaswarna` |
| Active Play Release | v0.6.0 / **versionCode 11** on Internal testing (Android Publisher API: `completed`) |
| Development Version | v0.7.0 / **versionCode 12** on `feat/layaswarna-audit-reconciliation-v12` |
| Suggested app category | **Music & Audio** |
| Default locale | **English (India), en-IN** |
| Product | Offline, audible experimental sound generator; not medically validated or a proven repellent |

**Proposed short description** (under 80 characters):

> Explore audible tones, custom sound profiles and timed listening sessions.

**Proposed full description** (under 4,000 characters):

> Layaswarna is an offline sound exploration app for Android.
>
> Explore experimental sound profiles with a circular selector, create custom frequency ranges, and try four waveforms: sine, sweep, pulse and hop. Discover seven chakra-inspired tones with traditional associations, and explore the experimental Anahata Nada sound studio.
>
> Choose a supervised listening session of 5, 10, 15 or 30 minutes. Sessions play while the app is active, and can continue with the screen locked when you explicitly enable screen-lock playback. Playback includes a digital amplitude limit, media volume checks, battery and thermal safeguards, audio-focus handling and built-in-speaker-only operation.
>
> Save custom presets on your device. No account, ads or analytics are required.
>
> IMPORTANT: Layaswarna is a sound experiment, not a medical treatment, sleep therapy or proven animal or insect repellent. Phone speakers vary. Some frequencies may be inaudible or uncomfortable. Keep volume moderate, do not direct sound at people or animals, and stop if anyone experiences discomfort. Pink and white noise are planned features and are not playable in this version.

**Status:** `en-IN` icon/feature graphic reported committed via Play Developer API; short/full description save **not evidenced** — enter/review the actual descriptions in Console before marking published.

## Figma / graphic assets — committed

| Play asset | Source file | Dimensions / data | Play status |
|---|---|---|---|
| App icon | [`layaswarna-play-icon-512.png`](../store-assets/figma/layaswarna-play-icon-512.png) | 512×512, PNG (alpha allowed) | **Uploaded** to en-IN (API edit `03965603996939416137`, HTTP 200 / read-back) |
| Feature graphic | [`layaswarna-feature-graphic-1024x500.jpg`](../store-assets/figma/layaswarna-feature-graphic-1024x500.jpg) | 1024×500, opaque RGB JPEG | **Uploaded** to en-IN (read-back present) |
| Editable SVG | [`layaswarna-icon-master.svg`](../store-assets/figma/layaswarna-icon-master.svg) | design source, not Play-upload format | n/a |

Figma sources: icon [62:25](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=62-25); feature graphic [87:2](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=87-2). No release rebuild needed for artwork.

## Phone screenshots (en-IN `phoneScreenshots`) — **CAPTURED & VERIFIED**

**Device (verified):** Xiaomi Mi A3, ADB `edd53dbd04de` (Android 11 / API 30).
**Dimensions:** 720×1280 (16:9 portrait, 24-bit RGB, no alpha, ~80–180 KB per image, all < 8 MB limit).
**Location:** [`PiFauna/store-assets/screenshots/`](../store-assets/screenshots/) (both `.png` and `.jpg` variants available; uncropped 720×1560 originals in `originals/`).

| Screenshot | Screen / Feature | Verified Contents |
|---|---|---|
| `01-home.jpg` | Home | Radial sound profile regulator, time-aware greeting, selected mosquito profile insight |
| `02-profiles.jpg` | Profile Library | Full species profile library, radial selection, custom band preset access |
| `03-chakra-library.jpg` | Seven Chakra Library | Radial selector for 7 symbolic chakra meditation tones with dynamic insight card |
| `04-chakra-console.jpg` | Chakra Sound Console | Selected Anahata meditation tone preparation, session guidance, audible-only controls |
| `05-custom-sound.jpg` | Custom Sound | Radial waveform selector (sine, sweep, pulse, hop), frequency range & output level controls |
| `06-anahata-nada.jpg` | Anahata Nada Studio | Relaxation sound studio, audible sweep choices, bounded session times (noise marked planned) |
| `07-player-timer.jpg` | Sound Player | Interactive countdown dial, start/stop action, preflight device check card, screen-lock toggle |
| `08-settings.jpg` | Settings | "Continue timed playback with screen locked" toggle (default OFF), safety limits overview |

## Promo video

Optional. A 24-second Figma Motion preview/MP4 exists — **not a real-device recording**, **not uploaded to YouTube/Play**. See [MARKETING-VIDEO.md](MARKETING-VIDEO.md).

## Translations

Drafts for **en-IN, bn, hi, as** exist locally (`~/Downloads/Layaswarna-Play-Translations.zip`); not confirmed published. See [LOCALIZATION.md](LOCALIZATION.md).

## Outstanding publishing metadata

- [ ] Verify full/short `en-IN` descriptions are saved in Console.
- [x] Icon and feature graphic uploaded to `en-IN` (API, read-back).
- [ ] Produce **7 compliant** real screenshots (720×1280, 9:16, RGB, <8 MB) and commit.
- [ ] Upload & **read-back verify** 7 `phoneScreenshots` (en-IN).
- [ ] Optional: reviewed real-device/approved promo → authorized YouTube → Play URL.
- [ ] **Cloudflare DNS:** add `CNAME layaswarna -> layaswarna-legal.pages.dev` (zone `berhampore.in`, proxy on); verify HTTPS 200 for `/`, `/privacy/`, `/terms/`, `/safety/` on `layaswarna.berhampore.in` (currently 530/pending).
- [ ] Set Play **Privacy Policy URL** (use `https://layaswarna-legal.pages.dev/privacy/` now or canonical custom domain after DNS 200).
- [ ] Configure internal testers (list currently empty).
- [ ] Review category, pricing, Data safety, store contact email, locale listings.
