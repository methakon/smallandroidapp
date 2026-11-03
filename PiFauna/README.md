# PiFauna — Phone-only Android bioacoustic generator — prototype v0.2.1

An original, **offline**, native Kotlin Android application inspired by the *category* of high-frequency emitter apps, not a copy of MemiSoft's code or visuals.

## Documentation and design

- [Product requirements](docs/PRODUCT-REQUIREMENTS.md)
- [Architecture and source map](docs/ARCHITECTURE.md)
- [Audio engine specification](docs/AUDIO-ENGINE.md)
- [Safety, claims and scientific evidence](docs/SAFETY-AND-EVIDENCE.md)
- [Figma design system / implementation handoff](docs/DESIGN-SYSTEM-AND-HANDOFF.md)
- [Test plan](docs/TEST-PLAN.md)
- [Release roadmap](docs/RELEASE-ROADMAP.md)
- [Project status / decisions](docs/PROJECT-STATUS.md)
- [Contributor guidance](docs/CONTRIBUTING.md)
- [Figma UX concept](https://www.figma.com/design/5HCEW8MLKjVTpJ3XW0s7xM) — four editable frames; this is a **future UI proposal**, not the current Kotlin single-screen app. The Figma AI-generated illustration is not bundled into the Android source.

## The goal

Generate short, explicitly controlled acoustic test signals and track observations for different biological groups. It does **not** claim to repel pests or prevent mosquito bites.

### What works in this prototype

- Seven selectable research groups: mosquitoes, other insects, dogs, cats, rodents, birds, custom.
- Per-group **illustrative** acoustic bands (not proven deterrent bands).
- Four signal patterns: fixed sine tone, triangle sweep, 250 ms on / 750 ms off pulse, and randomized frequency hops every 0.5 seconds (with fades at transitions).
- Precisely entered 100–18,000 Hz; 48 kHz / 16-bit mono PCM synthesis using `AudioTrack`.
- Three low PCM amplitude caps (3%, 6%, 10%); 5/10/20-second auto-stop.
- Refuses playback if an external audio device is connected; attempts to route specifically to built-in speaker and checks the resulting route.
- Stops when the app leaves the foreground. No background playback.
- Save up to 20 custom frequency-range presets on device.
- Manual observation log saved in local SharedPreferences (last 100 entries; last 5 displayed).
- Export your observation log to CSV through Android's document picker; no general storage permission required.
- No Android permissions, ads, accounts, or network requests.

### Important limitations

- **It does not produce true measured ultrasound**; at 18 kHz a device's audio path may already have sharply attenuated or distorted output, and some devices may produce *nothing* at the requested frequency.
- Sample rate and synthesized digital frequency do not prove actual acoustic output, speaker SPL, coverage, or effectiveness. Output needs calibrated measurement with appropriate instruments.
- Human/animal discomfort can occur even if a listener cannot hear the tone. Avoid headphones, children, pets, and nearby people. Stop at signs of distress.
- A 2026 review found no consistent pest suppression from consumer ultrasound systems. A Cochrane review found no protection from adult mosquitoes. These presets are not recommended as pest-control solutions.
- Dogs and cats may hear higher frequencies; do not use the app to frighten or punish them.
- Mosquito presets are in the *audible*, not ultrasonic, range because mosquito wingbeat and antennal reception are often in the hundreds of hertz. Acoustic responsiveness does not demonstrate repellency.
- Birds generally respond to *audible* frequencies, not ultrasound.
- External ultrasonic driver support (e.g., an explicitly specified USB audio interface + suitable transducer, or ESP32 hardware) is a **future feature, not included**.
- The existing MemiSoft app belongs to its respective developer. This project does not decompile or incorporate it.

## Build in Android Studio

1. Install Android Studio with Android SDK Platform 35 (API 35), Android build tools, and JDK 17 or newer.
2. Open the `PiFauna` directory using **File > Open**, allow Gradle sync, and install requested SDK components.
3. If Android Studio says it cannot find Gradle because a wrapper is missing, install a local Gradle 8.9 then run `gradle wrapper --gradle-version 8.9`; reopen the project. (The ZIP deliberately does not include a downloaded wrapper binary.)
4. Choose an actual Android device (minimum Android 8.0, API 26). The emulator can exercise the UI but not validate ultrasound or acoustic output.
5. Click **Run** to install. For a sideloadable development APK, use **Build > Build APK(s)**. This environment cannot compile an APK because the Android SDK/Gradle are not installed.

No publishing, Play Store app signing, or Android permission requests are included.

## Next steps after v0.2

- Actual Android device build/run and accessibility validation (not performed here).
- Device capability inventory (reported sample rates and audio outputs), and calibrated audio measurement workflow.
- Improve repeatability of short acoustic experiments and offer logged silent controls.
- Optional sound-level measurements through external calibrated instruments (the phone microphone cannot safely certify ultrasonic SPL).
- Use validated pest-control methods (screens, standing-water reduction, exclusion and sanitation) for protection.

## Phone-only usage

1. Select a species/observation group or **Custom frequency laboratory**. These are research labels only, not proven repellent profiles.
2. Enter start and end frequencies between 100 and 18,000 Hz.
3. Choose fixed tone, 2.5-second triangle sweep, 250/750 ms pulse, or 500 ms random hop.
4. Keep media volume low (the app refuses to start above 35% of the reported max volume). Disconnect headphones, Bluetooth audio, and USB audio devices.
5. Choose 5/10/20 seconds and a 3/6/10% PCM amplitude cap. Start only a supervised test and stop immediately for discomfort.
6. Save observations, including no response, and export CSV if needed. For repeated custom ranges, use **Save these settings as a local preset**.

**Note:** The phone's media-volume scale and PCM percentage are not calibrated acoustic sound-pressure levels. A tone may be inaudible, distorted, or attenuated.

## Sources

- Original app listing: https://play.google.com/store/apps/details?id=app.memisoft.ultrasonicsounds
- Android AudioTrack: https://developer.android.com/reference/android/media/AudioTrack
- Android AudioFormat and resampling: https://developer.android.com/reference/android/media/AudioFormat
- Review (2026): https://doi.org/10.1080/19397038.2026.2696677
- Cochrane electronic mosquito repellent review: https://www.cochrane.org/evidence/CD005434_electronic-mosquito-repellents-preventing-mosquito-bites-and-malaria-infection
- Mosquito acoustic response: https://pmc.ncbi.nlm.nih.gov/articles/PMC7820424/
- Animal hearing comparison: https://pmc.ncbi.nlm.nih.gov/articles/PMC4212972/
- Bird hearing: https://pmc.ncbi.nlm.nih.gov/articles/PMC9104101/

## License

Apache-2.0, original app source provided as an educational prototype. Third-party cited literature is not included in the license.