# Small Android Apps

A repository of independent Android applications. Each app lives in its own
subfolder with its own Gradle project, sources, tests and documentation, and is
developed on its own feature branch.

| App | Folder | Branch | Description |
|---|---|---|---|
| PiFauna | [`PiFauna/`](PiFauna/README.md) | `feat/pifauna-phone-audio` | Offline phone-speaker tone generator with device-safety limits |

## PiFauna

PiFauna is an experimental **phone-speaker sound generator** for Android. It
offers configurable audible and high-frequency tones, timed sessions and
device-safety controls. **It is not a validated pest repellent**: do not rely on
it to protect people, animals or property.

- [Application README, build and install instructions](PiFauna/README.md)
- [Architecture](PiFauna/docs/ARCHITECTURE.md)
- [Testing and recorded results](PiFauna/docs/TESTING.md)
- [Safety contract](PiFauna/docs/SAFETY.md)
- [Figma implementation status](PiFauna/docs/FIGMA-IMPLEMENTATION.md)

## Adding an app

Create `<AppName>/` at the repository root with its own `settings.gradle.kts`,
`build.gradle.kts`, committed Gradle wrapper and `docs/`. Do not share build
files between apps and do not move an app's build configuration to the
repository root.
