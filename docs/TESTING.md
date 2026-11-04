# Verification and release checklist

## Current verification status
Source pushed; **not built or tested on a device**. No release readiness claim.

## Manual tests
- [ ] Clean Gradle sync/build on JDK 17, SDK 35.
- [ ] Start/stop each frequency and duration without crash.
- [ ] Verify actual timer expiration using elapsed realtime.
- [ ] Press Home, lock screen or switch apps: playback stops.
- [ ] Receive a call or competing audio: playback stops.
- [ ] Test wired headset, USB audio and Bluetooth route changes; add route protection before shipping.
- [ ] At battery <=15%, playback cannot start; if battery falls to threshold, playback stops.
- [ ] Simulate severe thermal status; verify playback stops on API 29+.
- [ ] Confirm AudioTrack resources released after rapid start/stop cycles.
- [ ] Verify app requests no sensitive permissions.
- [ ] Run Android Studio profiler and Battery Historian / system battery stats on multiple devices.
- [ ] Test speaker and hearing safety at conservative system volume with appropriate instrumentation.
- [ ] Verify screen reader labels, contrast and large font scaling.
- [ ] Add unit tests and instrumented UI tests.

## Release gate
Do not release as a pest-repellent efficacy product. Do not publish until audio-route protection, hardware testing, accessibility review and build verification pass.
