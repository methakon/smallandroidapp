# PiFauna architecture and safety contract

## Scope
Offline Android application. MainActivity owns a foreground-only SoundEngine; it writes mono PCM16 sine samples using AudioTrack in a dedicated audio-priority thread. There is no server, analytics, telemetry, ads, microphone or persistent service.

## Safety rules
1. Frequency 200–16,000 Hz, below the 22.05 kHz Nyquist limit for 44.1 kHz sampling. No claim that speakers reproduce any selected tone accurately.
2. PCM amplitude limited to 2621/32767 (~8% digital full scale). This is NOT a calibrated acoustic loudness limiter; system volume, equalizers and hardware vary.
3. User-selected timer 5/10/20/30 minutes; default 10. No indefinite playback.
4. Stop when activity leaves foreground, audio focus is lost, battery reaches 15%, or Android thermal status becomes SEVERE (API 29+).
5. Release AudioTrack after every session; no wake locks, polling loops, network or background tasks.
6. Playback is optional and user-initiated. Avoid headphones, close-range use and prolonged exposure. Stop at any sign of distress.
7. No promise of pest deterrence; controlled evidence is insufficient for the app's claimed use cases.

## Performance
AudioTrack uses a reused short PCM buffer and blocks on the audio sink; no per-sample allocation, no busy-waiting. System broadcasts handle battery changes, and PowerManager handles thermal events where supported. CPU use and energy consumption require on-device measurement.

## Known engineering follow-ups
- Validate audio route changes (wired/Bluetooth) and stop automatically if unsafe.
- Add comprehensive audio-focus tests and concurrent start/stop race tests.
- Calibrate and measure sound pressure on real hardware; do not present an arbitrary dB estimate.
- Integrate the remaining Figma screens, profile presets and accessibility checks.
- Implement a Gradle wrapper and CI before release.
