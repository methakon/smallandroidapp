# Small Android Apps

A repository of independent Android applications. Each app lives in its own
subfolder with its own Gradle project, sources, tests and documentation, and is
developed on its own feature branch.

| App | Folder | Branch | Description |
|---|---|---|---|
| Layaswarna (formerly PiFauna) | [`PiFauna/`](PiFauna/README.md) | `feat/pifauna-phone-audio` (v13 merged into development) | Offline phone-speaker sound exploration, optional private mantra recording, 108-count sets and device-safety limits |

## Layaswarna

Layaswarna is an offline **phone-speaker sound exploration app** for Android (minSdk 26, targetSdk 36). It offers configurable audible tones, study bands, chakra meditation frequencies, timed sessions (5–30 min), screen-lock foreground playback opt-in, and strict device safety controls. **It is not a pest repellent or medical device**: do not rely on it to protect people, animals or property.

- [Application README, build and install instructions](PiFauna/README.md)
- [Architecture](PiFauna/docs/ARCHITECTURE.md)
- [Testing and recorded results](PiFauna/docs/TESTING.md)
- [Safety contract](PiFauna/docs/SAFETY.md)
- [Figma v13 source of truth](PiFauna/docs/FIGMA-V13-SOURCE-OF-TRUTH.md)
- [Independent final v13 audit — 9 November 2026](PiFauna/docs/FINAL-AUDIT-V13-2026-11-09.md)
- [Figma historical implementation status](PiFauna/docs/FIGMA-IMPLEMENTATION.md)
- [Google Play Store listing (last documented Internal versionCode 11)](PiFauna/docs/PLAY-STORE-LISTING.md)

**Development vs release:** Mantra Studio v13 (0.8.0) was merged into the *development* branch through PR #3, but no v13 Play upload was verified. The latest post-merge audit remediations and documentation are proposed separately through a **draft** pull request; follow the final audit gates before any signed v13 release. Figma is the only approved v13 design tool; historical Adobe assets are archival only.

## Adding an app

Create `<AppName>/` at the repository root with its own `settings.gradle.kts`,
`build.gradle.kts`, committed Gradle wrapper and `docs/`. Do not share build
files between apps and do not move an app's build configuration to the
repository root.
