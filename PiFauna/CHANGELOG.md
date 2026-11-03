# Release notes

## 0.2.0 — Phone-only implementation

- Added a deterministic, half-second randomized frequency-hop pattern with short fades at boundaries.
- Added saveable custom frequency ranges (up to 20, stored offline).
- Added local CSV export using the Android system document picker.
- Added structured local observation logs (last 100 entries).
- Kept phone-speaker-only routing, 100–18,000 Hz synthesis bound, 5/10/20-second auto-stop, PCM gain ceiling, no network/ads/permissions, and automatic stop on backgrounding.
- Extended the Kotlin signal-kernel smoke tests to cover random stepping, fades, and block-size invariance.

### Verification

The pure Kotlin generator was compiled and its smoke tests passed with `kotlinc` 1.9 and a JRE. The Android UI and APK have **not** been compiled or tested on a device. The project requires Android Studio, Gradle and Android SDK 35.

**The app is not a scientifically validated pest repellent and cannot guarantee ultrasonic acoustic output from a phone speaker.**