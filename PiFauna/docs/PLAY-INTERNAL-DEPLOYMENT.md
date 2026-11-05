# Google Play internal testing — deployment record

Everything here was read back from the Play Developer API after the release was
committed, not inferred from a successful upload call.

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
| **8** | **Internal Test 3** | **current** |

Version 7 was the API 36 build made before the Figma Mystic interface landed.
Eight commits arrived on the branch afterwards, so 7 was replaced rather than
left as the newest internal build. Version 6 never reached testers.

## How it was published

Play Developer API v3, authenticated with Application Default Credentials from
the `gcloud` CLI and a quota project (`homeguardian-ai-web-portal`, where
`androidpublisher.googleapis.com` was enabled). Sequence:

```
POST   /applications/{pkg}/edits                    -> edit id
POST   /upload/.../edits/{edit}/bundles?uploadType=media   (AAB, octet-stream)
PATCH  /applications/{pkg}/edits/{edit}/tracks/internal
POST   /applications/{pkg}/edits/{edit}:commit
```

Verification afterwards opened a fresh edit, read `tracks/internal`, and
deleted it again. No credentials or keystore material were written to the
repository.

## Open item: no testers on the internal track

`GET /edits/{id}/testers/internal` returned `{}` — the internal track has **no
tester list**. The release is published, but until testers are added nobody can
install it from the opt-in link.

Fix is one of:

- Play Console → Test and release → Testing → Internal testing → **Testers** →
  create an email list and add addresses, or
- supply the addresses and they can be set through
  `PUT /applications/{pkg}/edits/{id}/testers/internal` with
  `{"individualTesters": [...]}` or a Google Group.

## Remaining warnings

- **Native debug symbols.** The bundle carries four prebuilt
  `libandroidx.graphics.path.so` files from AndroidX. There is no native code
  compiled by this project, so no symbol metadata is produced. Play treats this
  as informational; it does not block internal testing.
- **No Android 16 device was available** for hardware verification of enforced
  edge-to-edge and predictive back. See `docs/TESTING.md`.

## Rollback

If versionCode 7 has to be withdrawn from internal testing, patch the same
track with the previous release or set a halt:

```
PATCH /applications/{pkg}/edits/{edit}/tracks/internal
{ "releases": [ { "versionCodes": ["7"], "status": "halted" } ] }
```

Nothing outside the internal track was affected, so no production users are at
risk either way.
