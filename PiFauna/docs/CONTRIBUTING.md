# Contributing to PiFauna

Use issues/PRs against `feat/pifauna-phone-audio`; never commit to `main` directly. Small Android projects live in independent subfolders of the parent repository. Use descriptive commit scopes such as `docs(pifauna)`, `fix(pifauna)` and `feat(pifauna)`.

Before submitting: run `ToneKernelSmokeTest`, build with the current Android SDK, verify all safety checks on a real device when behavior changes, and update relevant docs. Include a test phone/OS matrix and reproducible steps. Do not add unlicensed MemiSoft code/assets, hidden recordings, telemetry, unsupported repellency claims or continuous animal-targeted audio.

Every code change affecting output frequency, gain, route, durations or lifecycle must include an explicit regression test. Asset rights and Figma-to-app export licensing must be reviewed before incorporation into APKs.