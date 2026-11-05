# Layaswarna — Google Play Store listing

Last updated: 2026-11-05. Separate **committed assets**, **worker-reported API publication** and **remaining tasks** below.

## Identity and copy

| Item | Value |
|---|---|
| App name | Layaswarna |
| Package | `in.berhampore.dhar.layaswarna` |
| Current release | v0.5.0 / **versionCode 9** on Internal testing (per worker Play API read-back) |
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

**Status:** Worker reports `en-IN` app-name/icon/banner edits committed via API; short/full description save **not evidenced** by latest report. Enter/review the actual descriptions in Console before marking them published.

## Figma originals — committed to this Git branch

| Play asset | Source file | Dimensions / data |
|---|---|---|
| App icon | [`layaswarna-play-icon-512.png`](../store-assets/figma/layaswarna-play-icon-512.png) | 512×512, 228,464 bytes, PNG, alpha allowed for icon |
| Feature graphic | [`layaswarna-feature-graphic-1024x500.jpg`](../store-assets/figma/layaswarna-feature-graphic-1024x500.jpg) | 1024×500, 284,698 bytes, opaque RGB JPEG |
| Editable SVG | [`layaswarna-icon-master.svg`](../store-assets/figma/layaswarna-icon-master.svg) | 9,612 bytes; design source, not Play-upload format |

Sources: Figma icon [62:25](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=62-25); Figma feature graphic [87:2](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=87-2).

**Uploaded, worker-reported:** Both icon (SHA-1 starts `1db0a48c8`) and feature graphic (SHA-1 starts `f5dc41049`) uploaded to the default `en-IN` listing and committed through Play Developer API edit `03965603996939416137`; worker reports HTTP 200/read-back. No release rebuild needed for artwork.

## Actual app screenshots: report vs Git branch

Worker reports capturing **five real Android 11 Mi A3 screenshots (720×1560)**, converting them to opaque RGB JPEG:
`01-home.jpg`, `02-profiles.jpg`, `03-chakra-library.jpg`, `03-settings-safety.jpg`, `04-player-timer.jpg`.

**Important discrepancy:** Worker says they were committed under `PiFauna/store-assets/screenshots/`, but this directory returned **404** on the Git branch as checked immediately before this documentation update. Verify the commit/branch or re-push the screenshots before citing repository links. Worker expressly says **none of the five were uploaded to Play**. Upload only after verifying files, real capture provenance, legibility, dimensions and aspect ratios.

## Promo video

An editable 24-second Figma Motion preview and completed MP4 export exist. The video is **not a real-device recording** and has **not been uploaded to YouTube or entered in Play**. See [MARKETING-VIDEO.md](MARKETING-VIDEO.md). It is optional.

## Translations

Initial drafts for **en-IN, Bengali (bn), Hindi (hi), Assamese (as)** were generated as a ZIP in conversation; they have not been confirmed published. See [LOCALIZATION.md](LOCALIZATION.md).

## Outstanding publishing metadata

- [ ] Verify full/short `en-IN` descriptions are saved.
- [x] Icon and feature graphic reported uploaded to `en-IN` via Play API.
- [ ] Confirm five genuine screenshots are available in the **Git branch**.
- [ ] Upload and verify at least two real phone screenshots (five prepared according to worker).
- [ ] Optional Figma promo → reviewed video → authorized YouTube upload → Play YouTube URL.
- [ ] Make the canonical privacy-policy URL publicly accessible and set it in Google Play Console; see [LEGAL-SITE-HOSTING.md](LEGAL-SITE-HOSTING.md).
- [ ] Configure internal testers; existing worker report said tester list was empty.
- [ ] Review category, pricing, Data safety, store contact email and locale-specific listings.
