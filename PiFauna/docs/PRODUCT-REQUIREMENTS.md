# PiFauna — product requirements (PRD)

Status: **prototype v0.2.1 implemented; multi-screen UX is a design proposal**. Last updated: 2026-11-04.

## Purpose and boundary

PiFauna is an **offline, phone-only acoustic signal generator and manual observation journal** for short, supervised experiments. It is **not** a certified ultrasonic emitter, animal training device, medical instrument, mosquito-bite prevention tool, or pest-control product. A selectable animal/insect group organizes study notes; it never certifies that its listed frequencies repel that group.

## Target users

- Hobbyists and students testing Android audio-synthesis behavior on their own device.
- Bioacoustic researchers logging controlled observations without assuming causality.
- Android developers learning `AudioTrack` and audio-routing safeguards.

## Scope by priority

| Requirement | Acceptance criteria | State |
|---|---|---|
| Frequency control | Start/end inputs validated between 100 and 18,000 Hz | Implemented |
| Signal synthesis | Fixed, 2.5 s triangle sweep, pulse 250/750 ms, 500 ms pseudorandom hop | Implemented |
| Exposure limits | Only 5, 10, or 20 s; amplitude 3, 6, or 10% of full-scale PCM | Implemented |
| Device/output checks | Reject recognized external routes, prefer built-in speaker, cap reported media volume to 35% | Implemented, device validation pending |
| Stop behavior | Manual Stop; stop on `onPause`; automatic duration limit | Implemented, device validation pending |
| Profiles | Mosquito, insects, dogs, cats, rodents, birds, custom; all labeled illustrative | Implemented |
| Custom presets | Local persistence, at most 20 names/frequency pairs | Implemented |
| Journal | Local log of up to 100 entries; record no change/distress | Implemented |
| Data portability | CSV via Android Storage Access Framework | Implemented |
| Polished native multi-screen UI | Generator, Profiles, Journal, Safety | **Figma proposal only** |
| Controlled silent trial design | A/B controls, randomized trial sequence, predefined scoring | Future |
| Acoustic calibration | External measured speaker response / SPL | Future, outside phone-only scope |

## Core journeys

1. Read research limitation and safety warning.
2. Select research group, inspect illustrative range, and edit frequencies.
3. Choose signal pattern, duration and digital gain; reduce phone media volume.
4. Consent to a short test, play only through built-in speaker, stop at discomfort or lifecycle loss.
5. Record observed result (including **no change**) and, optionally, export CSV.
6. Save a personal frequency-range preset without creating an unsupported efficacy claim.

## Non-functional requirements

- Minimum API 26; Android SDK target 35; native Kotlin; no network/advertising/account access; no permissions in manifest.
- Keep audio generation off the main/UI thread; finite duration and immediate stop intent.
- Treat all speaker output as **uncalibrated**. The PCM amplitude is not dB SPL.
- Respect battery/thermal limits: no continuous unattended background audio.
- Avoid dark patterns: no animal-targeting claims, flashing alerts or false 'protection' badges.

## Success criteria / release gates

- App builds and installs on Android 8.0+ representative devices.
- A headset, BLE device, USB audio output, or route change never enables unattended playback; failures are visible.
- Playback does not persist after pressing Stop or leaving activity; manual timing and route tests pass.
- CSV escapes quotes/line breaks properly; user data stays local unless user deliberately exports.
- Accessibility and UI copy reviewed, including a readable explanation of what ultrasound is and is not.
- No claim of proven mosquito, rodent, bird, dog, or cat repellency without species-specific controlled evidence.