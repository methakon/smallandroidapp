# Google Play internal testing — current release and historical deployment record

## Current release — versionCode 9 (2026-11-05, Hermes verified)

Hermes reports an authenticated read-back of Play Developer API: internal track **Layaswarna 0.5.0 - Internal Test 4**, versionCodes **["9"]**, status **completed**. No Production/alpha/beta track was touched. This record summarizes the worker's reported checks; it is not a separate direct Play Console verification.

| Field | VersionCode 9 |
|---|---|
| App ID | `in.berhampore.dhar.layaswarna` |
| Version name | 0.5.0 |
| Android SDK | minSdk 26, targetSdk 36 |
| Signed AAB | 6,277,154 bytes |
| SHA-256 (local and Play reported equal) | `c75661316630e5d8c1f5e18fe94d00ef1d3703360b54cd14a49cc43fa3c5f77e` |
| Upload certificate SHA-256 | `F8:10:3F:C9:91:D9:D0:49:43:44:38:6A:FB:57:70:73:E6:EC:1D:7C:76:7F:DD:29:1D:16:0A:6C:08:52:45:31` |
| Validation | bundletool PASS, jarsigner PASS, unit tests 33/33, instrumentation 27/27 on each API 28/API 30 device, lint 0 errors / 27 warnings |

VersionCode 9 introduces the single-owner AudioTrack teardown claim, explicit VolumeShaper user-stop fade, drain-on-natural-completion, immediate safety stop, and bundled offline privacy screen. Digital functional tests are not proof of physically click-free playback. Android 16, TalkBack and physical external-route QA remain pending.

### Current Play listing and legal site

- **en-IN app icon and feature graphic:** reported uploaded and committed through Android Publisher API edit `03965603996939416137`.
- **Five real phone screenshots:** reportedly captured/converted to RGB JPEG on Mi A3; **not uploaded to the Play listing**. Worker said committed under `PiFauna/store-assets/screenshots/`, but that path returned 404 on the reviewed Git branch; verify commit/branch before linking.
- **Translations:** en-IN/bn/hi/as CSV drafts exist; no import or localized publishing verified.
- **Privacy Policy field:** not set. `layaswarna-legal.pages.dev/privacy/` is live; canonical custom domain pending a DNS CNAME and currently returns 530.
- **Tester list:** last read-back empty (`{}`). Release availability for testers is not established until the list/opt-in link is configured.

See [PLAY-STORE-LISTING.md](PLAY-STORE-LISTING.md), [LEGAL-SITE-HOSTING.md](LEGAL-SITE-HOSTING.md), and [MARKETING-VIDEO.md](MARKETING-VIDEO.md).

## Historical versionCode 8 deployment — superseded by versionCode 9

The older record below was a verified v8 snapshot; its release identity and bundle checksum do **not** describe the current release.

## Release identity

| Field | Value |
|---|---|
| Package | `in.berhampore.dhar.layaswarna` |
| Track | `internal` |
| Release name | Layaswarna 0.5.0 - Internal Test 3 |
| versionCode | **8** |
| versionName | 0.5.0 |
| minSdk / targetSdk | 26 / **36** |
| Status | `completed` (rolled out to the internal track) |
| Release type | Internal testing only — no closed, open or production track touched |

## Bundle

| Field | Value |
|---|---|
| Local file | `PiFauna/app/build/outputs/bundle/release/app-release.aab` |
| Size | 6,268,550 bytes |
| SHA-256 (local) | `9828d7d7a825d6a018a669ad4e2c03ff65bfc2e3c0ef71c525bb179aeaa99640` |
| SHA-256 (Play, after upload) | `9828d7d7a825d6a018a669ad4e2c03ff65bfc2e3c0ef71c525bb179aeaa99640` |
| SHA-1 (Play) | `6f96e279c6916dd2147f1becdc8c642e0c8f3f90` |
| Upload key | SHA-256 `F8:10:3F:C9:91:D9:D0:49:43:44:38:6A:FB:57:70:73:E6:EC:1D:7C:76:7F:DD:29:1D:16:0A:6C:08:52:45:31` |

Play computed the same SHA-256 as the local build, so the artifact that was
published is byte-for-byte the artifact that was tested here.

## Release notes published

> Figma Mystic interface: redrawn radial wheel with original chakra glyphs,
> insight-first profile and chakra screens, and Anahata timer chips bound to the
> existing bounded session controller. Targets Android 16 (API 36). Playback
> still stops on low battery, high media volume, external audio device, or when
> the app leaves the foreground.

## Release history on the internal track

| versionCode | Release | Outcome |
|---|---|---|
| 6 | accepted for processing | blocked — targeted API 35, Play requires 36 |
| 7 | Internal Test 2 | published, then superseded the same day by 8 |
| **8** | **Internal Test 3** | superseded by 9 |
| **9** | **Internal Test 4** | current internal release (reported) |

Version 7 was the API 36 build made before the Figma Mystic interface landed.
Eight commits arrived on the branch afterwards, so 7 was replaced rather than
left as the newest internal build. Version 6 never reached testers.

## How it was published

Play Developer API v3, authenticated with Application Default Credentials from
the `gcloud` CLI and a quota project dedicated to this app,
`layaswarna-play` (where `androidpublisher.googleapis.com` is enabled and which
is also set as the ADC quota project). Sequence:

```
POST   /applications/{pkg}/edits                    -> edit id
POST   /upload/.../edits/{edit}/bundles?uploadType=media   (AAB, octet-stream)
PATCH  /applications/{pkg}/edits/{edit}/tracks/internal
POST   /applications/{pkg}/edits/{edit}:commit
```

Verification afterwards opened a fresh edit, read `tracks/internal`, and
deleted it again. No credentials or keystore material were written to the
repository.

## Testers — recheck against versionCode 9

`GET /edits/{id}/testers/internal` returned `{}` — the internal track has **no
tester list**. The release is published, but until testers are added nobody can
install it from the opt-in link.

Fix is one of:

- Play Console → Test and release → Testing → Internal testing → **Testers** →
  create an email list and add addresses, or
- use a Google Group for API-supported tester configuration; manage individual email lists in Play Console, as described by the worker.

## Remaining warnings

- **Native debug symbols.** The bundle carries four prebuilt
  `libandroidx.graphics.path.so` files from AndroidX. There is no native code
  compiled by this project, so no symbol metadata is produced. Play treats this
  as informational; it does not block internal testing.
- **No Android 16 device was available** for hardware verification of enforced
  edge-to-edge and predictive back. See `docs/TESTING.md`.

## Historical rollback example — not for the current release

The following example was written for an older build; do **not** execute against versionCode 9 without a fresh rollback assessment:

```
PATCH /applications/{pkg}/edits/{edit}/tracks/internal
{ "releases": [ { "versionCodes": ["7"], "status": "halted" } ] }
```

Nothing outside the internal track was affected, so no production users are at
risk either way.
