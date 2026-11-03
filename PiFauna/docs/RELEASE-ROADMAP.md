# PiFauna — roadmap, build instructions and release gates

## Current snapshot

- v0.2.1: Kotlin prototype / audio engine / native single-activity interface / custom presets / CSV export.
- Figma v0.1: editable four-screen concept, not yet implemented as four Activities/Fragments.
- APK: **not built or hardware-tested in this workspace**. No Play Store release or field efficacy validation.

## Build prerequisites

Android Studio, JDK 17, Android SDK Platform 35, supported Android Gradle plugin 8.x and matching Gradle 8.9. The source does not include `gradlew` or wrapper JAR, so set up a supported wrapper locally before using `./gradlew`. Use **File > Open > PiFauna**, allow Gradle sync and select an Android 8.0+ test device.

## Milestones

1. **M0** — Source and docs versioned in `methakon/smallandroidapp`, branch `feat/pifauna-phone-audio`.
2. **M1** — Add wrapper/CI, run Android Studio build, resolve Android 15/API 35 compatibility and deprecations.
3. **M2** — Physical-device route safety / volume / stop validation; report hardware limitations.
4. **M3** — Implement Figma navigation and accessibility as a UI refactor; retain safety gates.
5. **M4** — Improve protocol metadata, optional silent controls and reporting, no efficacy advertising.
6. **M5** — Independent legal/claims/privacy review before any public distribution.

## Git workflow

```bash
cd /home/swarna-sekhar-dhar/projects/smallandroidapp
git fetch origin
git switch feat/pifauna-phone-audio
git pull --ff-only origin feat/pifauna-phone-audio
cd PiFauna
# Open here in Android Studio; do not assume source root is workspace root.
```

Do not merge to `main` until source builds, device-safety tests pass, and README/claims language is audited.