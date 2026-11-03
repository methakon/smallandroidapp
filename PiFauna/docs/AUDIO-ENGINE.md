# Audio engine specification — accurate interpretation

## Supported generator

- Sample rate: `48,000` samples/second, PCM 16-bit, mono. `AudioTrack.MODE_STREAM` receives 480-sample (~10 ms) blocks.
- Both requested frequencies must be 100–18,000 Hz. `sampleRate >= 44,100` and `maxFrequency < 0.45 * sampleRate` are enforced by `ToneConfig`.
- Gain options are fractions of digital full scale: `0.03`, `0.06`, `0.10`. The values are **not** sound-pressure levels or safe-exposure certifications.
- Initial and final 25 ms envelopes reduce clicks; phase continuity is preserved between blocks.

## Algorithms

| Mode | Formula/behavior | Purpose |
|---|---|---|
| Fixed | `f(t)=startHz` | Frequency-output experiment |
| Sweep | `start + (end−start)*triangle(t/2.5 s)` | Repeating linear rise/fall |
| Pulse | 250 ms audible phase + 750 ms silence per second, 10 ms transitions | Compare exposure timing |
| Random hop | Deterministic pseudorandom choice every 500 ms; 12 ms edge fades | Repeatable variability by time step |

The “random” mode is reproducible for given timings; **not cryptographic entropy**. Frequency values are digitally synthesized; physical transducer output may be filtered or distorted. Above approximately 15–18 kHz, many phones have poor acoustic response. A 48 kHz sample rate theoretically represents higher digital frequencies, **but does not make a speaker ultrasonic**.

## Device and safety checks

1. Refuse listed wired/Bluetooth/USB/hearing-aid external output devices.
2. Require reporting of a built-in speaker and `mediaVolume/maxVolume <= 0.35`.
3. Ask for launch-scoped informed consent.
4. Request a built-in-speaker preferred route; check subsequent reported routed device.
5. Stop at foreground loss, manual Stop, time limit or audio errors.

Known limits: route enumeration and `setPreferredDevice` do not certify route fidelity; a device can remap outputs, resample, attenuate or distort. PCM amplitude cap cannot replace acoustic calibration. Even supposedly “inaudible” sounds may disturb people or animals. Test without animals present first.

## Measurement and verification

For calibrated evidence, use an appropriate frequency-capable reference microphone, coupler or audio analyzer, document distance, orientation, noise floor, peak SPL, harmonic distortion and device model. Ordinary smartphone microphone readings do not establish 18 kHz output and should not be interpreted as ultrasonic evidence.