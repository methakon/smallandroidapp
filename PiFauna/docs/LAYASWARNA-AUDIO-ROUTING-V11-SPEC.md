# Layaswarna v11 — Active Audio Route & Hearing Protection

**Status:** development specification, not released. **Date:** 2026-11-07.
**Baseline:** `feat/pifauna-phone-audio` at `a7973a9be3aa36b9d169d72d215db3bde42916a1`.
**Development branch:** `feat/layaswarna-audio-route-v11`.
**Protected release:** versionCode 10 (0.5.0) AAB remains unchanged.
**Figma:** https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX

## Reason for change
Previously `AudioRouteMonitor.externalRouteName()` enumerated all available outputs through `AudioManager.getDevices(GET_DEVICES_OUTPUTS)`. That is NOT the active media route. As a result a paired, available, yet unrouted Bluetooth audio device could block playback even while Android directed sound to the phone speaker.

## Current verified constraints
Layaswarna synthesizes experimental audible sounds, not clinically validated medicine, sound therapy or pest repellence. High SPL can damage hearing, even at high frequencies the listener cannot hear. The existing 8% digital signal ceiling and 60% media volume gate do **not** establish safe acoustic SPL. Consent cannot prove safety, waive design responsibility, or identify a Bluetooth A2DP endpoint as a free-standing speaker.

## Route policy
| Actual playback output | Policy |
| --- | --- |
| Phone's built-in speaker, even if unrelated Bluetooth device is connected but not routed | Allow only when active stream route is positively verified and existing safety gates pass |
| Wired headset/headphones | Block |
| Bluetooth A2DP, BLE headset, earbuds, Bluetooth speaker (ambiguous) | Block |
| Hearing aid | Block, no consent override |
| USB audio, HDMI, dock, analog/digital line | Block |
| Null/unavailable/unknown routed device | Fail closed |
| Route changes mid-session or identity verification is lost | Immediately stop |
| Free-standing external Bluetooth speaker | Future non-shipping experiment requiring defensible route identification and measured acoustics |

**Do not** infer speaker/headphones from product name, Bluetooth class, connection state, device label, or user's checkbox.

## Engineering design
1. Use `AudioManager.getDevices()` for *inventory only*, not to authorize playback.
2. Verify `AudioTrack.getRoutedDevice()` on the exact playback track before allowing nonzero samples to leave the audio mixer. Treat null/unknown as unsafe.
3. Recheck for route changes before every audio write; register routing callbacks where possible, and stop immediately upon loss of verification.
4. Fail closed on API limitations or exceptions. Retain a separate preflight battery, thermal, volume, focus and duration safety gate.
5. Preserve `TimedPlaybackService`, `PlaybackRuntime`, screen-lock opt-in, notification Stop, and session bounds.
6. No always-on background service, microphone permission, or internet dependency.

## Non-shipping external speaker prototype
A later prototype can show affirmative warnings and session-specific consent; proposed initial *unvalidated* test envelope: <=5 minutes, <=30% Android media volume and <=2% digital amplitude. This is not a calibrated dB limit; consent alone must **never** activate Bluetooth output. Acoustic measurement, device identity evidence, accessibility assessment and explicit product approval are prerequisites.

## Figma alignment — edited frames
- [27 — Audio output](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=105-48): speaker-only currently, Bluetooth prototype planned, headsets/hearing aids blocked.
- [28 — Bluetooth speaker prototype](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=105-64): unavailable in v10, no consent-only override.
- [29 — Route safety](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=105-78): unknown/headset/hearing aid/USB blocked.
- [Settings](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=51-315): speaker-only copy.
- [Sound Player](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=51-105): active output verification before Start.
- [Playback Safety](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=51-393): inaudible tones, dB caveat and hearing warning.

## Release acceptance gates
- Regression tests for connected-unrouted A2DP, active A2DP, BLE/hearing aid, USB/HDMI, null route, fast route changes, races with Stop, and lock-screen notification.
- Android 9 (API 28), Android 11 (API 30), Android 13 (API 33) and Android 16 (API 36) device/emulator validation.
- Physical hardware routing proof, no nonzero output on unknown or unsafe route.
- Unit, instrumented, lint, accessibility, large font, TalkBack and acoustic checks (before *any* external playback).
- Reconcile legal pages, Play listing and changelog with actual implementation. New versionCode and new signed artifact for any future release; do not change or relabel v10 signed bundle.

## Evidence boundaries
v10 physical-device and CI results document the prior release candidate only. v11 code, if present, has **not** inherited those validations. Do not claim v11 is ready for Play testing until independently confirmed.
