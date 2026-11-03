# Frequency presets, evidence, and ultrasound hardware — v0.2

## Evidence-based design rule

A species' hearing range is **not** a frequency that repels the species. Avoidance depends on more than frequency: actual sound-pressure level (SPL), distance, speaker output, frequency response, species, habituation, and context. Never market hearing-band presets as validated repellents.

## Acoustic target profiles

| Group | Example band in this app | What the band means | Repellency evidence |
|---|---:|---|---|
| Mosquito / Aedes | 200–800 Hz | Audible sound; includes frequencies used in mosquito hearing/flight-reaction studies. The ~200–800 Hz figure is broad and sex/species dependent. | Electronic sound-emitter protection against adult biting is **not supported** by field evidence; some sound may attract. |
| Other insects | 1–5 kHz | Demonstration band, not a biologically unified target. | No common frequency works across ants, cockroaches, flies, moths, etc. |
| Dogs | 16–18 kHz | Phone-limited high-frequency audible test. Dogs can hear higher than this. | Not a validated deterrent; avoid pet distress. |
| Cats | 16–18 kHz | Phone-limited high-frequency audible test. Cats can hear higher than this. | Not a validated deterrent; avoid pet distress. |
| Mouse/rat | 16–18 kHz | Phone-limited research tone; rodents often communicate/hear at 30–70 kHz or higher. | Some short-term responses, rapid habituation; no reliable lasting repellent. |
| Birds | 2–8 kHz | Ordinary audible range overlapping much of avian sensitivity. | Most birds do not hear ultrasound; no simple ultrasonic solution. |

These are **study/default bands**, not prescriptions for pest control. The user can set any frequency within 100–18,000 Hz for a supervised, short demonstration.

## Why a phone cannot provide true 40, 50, 60 kHz ultrasound

- Normal audio output sample rates are 44.1 or 48 kHz; the Nyquist frequency at 48 kHz is 24 kHz. Useful measured output is often much lower because speaker/audio-chain filtering attenuates high frequencies.
- Android `AudioTrack` accepting a high sample rate does **not** prove that DAC, amplifier, routing, speaker, or transducer preserve the signal. Android may resample digital audio.
- For true ultrasonic output, both sample rate and the entire *physical output chain* must be capable and experimentally verified with appropriate measurement equipment.
- 40 kHz and 60 kHz piezo transducers often have narrow resonant bands. A nominal 40 kHz transducer is not automatically a 60 kHz transducer. A 40–60 kHz software sweep alone is insufficient.
- For a 60 kHz sine output in a conventional PCM system, 192 kHz sampling offers digital headroom but does not overcome the output chain's physical limits. Alternatively, use dedicated hardware waveform generation with a matched transducer.

## Suggested optional ESP32 companion (NOT IMPLEMENTED in phone-only v0.2)

Android app (Wi-Fi/BLE) → explicit user authorization → ESP32 waveform generator → properly rated driver → characterized ultrasonic transducer(s) → calibrated measurement.

All components require verified resonance, drive voltage, duty cycle, thermal performance, audible distortion, spatial directivity, and animal/human safety. A software interface alone cannot determine safe sound levels. Do not use unmeasured high-intensity ultrasound as a way to drive off pets or wildlife.

A future hardware protocol might describe *requested* `frequency_hz`, `duration_ms`, `duty_cycle`, `transducer_profile_id`, and an `emergency_stop` command. The firmware should validate every field, enforce hardware-defined caps independently of the Android app, and time out to OFF after loss of connection. No such firmware is contained in this package.

## How to assess biological response responsibly

1. Avoid distressing pets/wildlife. Work with appropriate animal-welfare oversight before exposure experiments.
2. Identify a species precisely (e.g., *Aedes aegypti*, *Mus musculus*); broad animal categories cannot define a universal acoustic target.
3. Measure the acoustic output with equipment rated for the test band. Report SPL at the observation point, frequency spectrum, distance, location, orientation, obstacles, and environmental noise.
4. Include silent/sham controls, randomize trial order, record baseline observations and avoid observer bias.
5. Stop immediately if any sign of discomfort or aversion occurs; never use uncalibrated acoustic intensity to compel a behavior.
6. Report nonresponses and short-lived reactions as faithfully as apparent successes. Do not confuse brief surprise with lasting pest control.
7. Use proven pest-management approaches for practical protection, especially mosquito source reduction and physical screening.

## Further reading

- MemiSoft original: https://play.google.com/store/apps/details?id=app.memisoft.ultrasonicsounds
- Dmello et al. 2026 review: https://doi.org/10.1080/19397038.2026.2696677
- Cochrane review: https://www.cochrane.org/evidence/CD005434_electronic-mosquito-repellents-preventing-mosquito-bites-and-malaria-infection
- Mosquito acoustic responses: https://pmc.ncbi.nlm.nih.gov/articles/PMC7820424/
- Comparative animal hearing: https://pmc.ncbi.nlm.nih.gov/articles/PMC4212972/
- Bird acoustic range: https://pmc.ncbi.nlm.nih.gov/articles/PMC9104101/
- Android format docs: https://developer.android.com/reference/android/media/AudioFormat