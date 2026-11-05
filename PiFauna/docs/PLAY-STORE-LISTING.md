# Layaswarna — Google Play Store listing

Last updated: 2026-11-05. Branch: `feat/pifauna-phone-audio`. Separates **committed/verified assets**, **worker-reported API publication**, and **remaining tasks**.

## Identity and copy

| Item | Value |
|---|---|
| App name | Layaswarna |
| Package | `in.berhampore.dhar.layaswarna` |
| Current release | v0.5.0 / **versionCode 9** on Internal testing (Play API read-back: `completed`) |
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
> Choose a supervised listening session of 5, 10, 15 or 30 minutes. Playback includes a digital amplitude limit, media volume checks, battery and thermal safeguards, audio-focus handling and built-in-speaker-only operation. Playback stops when you leave the app.
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

## Phone screenshots (en-IN `phoneScreenshots`) — **IN PROGRESS / NOT COMPLETE**

**Device (verified):** Xiaomi Mi A3, ADB `edd53dbd04de` (Android 11 / API 30). App **`in.berhampore.dhar.layaswarna` versionCode 9** (installed; no rebuild). Device config **restored and verified:** Physical 720×1560, density 320, no `wm` overrides.

**Native captures (authentic, preserved locally, uncommitted):** 7 real screens via `adb exec-out screencap -p` → `PiFauna/store-assets/screenshots/originals/` (720×1560 RGBA PNGs):
1. `01-home-orig.png` — Home / interactive circular sound profile regulator
2. `02-profiles-orig.png` — Profile Library
3. `03-chakra-library-orig.png` — Chakra Library / seven chakra-inspired tones
4. `04-chakra-console-orig.png` — Chakra Sound Console / selected tone & listening info
5. `05-custom-sound-orig.png` — Custom Sound / frequency & waveform controls
6. `06-anahata-nada-orig.png` — Anahata Nada / audible sounds & timer choices (shows “Planned” for unavailable sounds — accurate)
7. `07-player-timer-orig.png` — Sound Player / timed playback controls & safety info

**Not done (blocking):** inspect & crop each original **720×1560 → exact 720×1280 (9:16, portrait, RGB/no alpha, < 8 MB)** preserving controls/text (no stretching/fabrication); export finals as `01-home.jpg`–`07-player-timer.jpg`; verify programmatically; commit+push; **upload all 7** to en-IN `phoneScreenshots` via Play Developer API (quota `layaswarna-play`), commit Play edit, **read back** to confirm acceptance. Any older top-level JPGs present locally (720×1560 / wrong names) are **not** the compliant final set.

**Play status:** **0** new compliant screenshots uploaded for this task (API read-back showed `phoneScreenshots: 0`; icon/feature present). **Accepted screenshots (verified): 0.**

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
