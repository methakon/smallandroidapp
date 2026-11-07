# Small Android Apps

A repository of independent Android applications. Each app lives in its own
subfolder with its own Gradle project, sources, tests and documentation, and is
developed on its own feature branch.

| App | Folder | Branch | Description |
|---|---|---|---|
| Layaswarna (formerly PiFauna) | [`PiFauna/`](PiFauna/README.md) | `feat/layaswarna-audit-reconciliation-v12` | Offline phone-speaker sound exploration & study app with device-safety limits |

## Layaswarna

Layaswarna is an offline **phone-speaker sound exploration app** for Android (minSdk 26, targetSdk 36). It offers configurable audible tones, study bands, chakra meditation frequencies, timed sessions (5–30 min), screen-lock foreground playback opt-in, and strict device safety controls. **It is not a pest repellent or medical device**: do not rely on it to protect people, animals or property.

- [Application README, build and install instructions](PiFauna/README.md)
- [Architecture](PiFauna/docs/ARCHITECTURE.md)
- [Testing and recorded results](PiFauna/docs/TESTING.md)
- [Safety contract](PiFauna/docs/SAFETY.md)
- [Figma implementation status](PiFauna/docs/FIGMA-IMPLEMENTATION.md)
- [Google Play Store listing](PiFauna/docs/PLAY-STORE-LISTING.md)

## Adding an app

Create `<AppName>/` at the repository root with its own `settings.gradle.kts`,
`build.gradle.kts`, committed Gradle wrapper and `docs/`. Do not share build
files between apps and do not move an app's build configuration to the
repository root.
