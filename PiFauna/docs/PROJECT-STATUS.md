# Project status and decision log

**2026-11-04 — PiFauna / Mode A (phone only)**

- Chosen scope: built-in smartphone speaker; no ESP32, USB audio peripheral, hardware horns, Bluetooth speaker, or background service.
- Product name: PiFauna; package `com.methakon.pifauna`; repo subfolder `PiFauna/`.
- Branch: `feat/pifauna-phone-audio`; primary branch `main` should remain unchanged until reviewed.
- Current source: native Kotlin single-Activity proof of concept, target Android API 35, minimum API 26.
- UX direction: four-screen Material-inspired design with Figma AI decorative illustration; proposed Figma flow is not implemented in Kotlin yet.
- Scientific stance: research/demo bands only; no guaranteed acoustic output or repellent efficacy.
- Privacy: offline SharedPreferences, explicit export, no permission or account infrastructure.
- Blocking work: Android build validation, device output-route testing, accessibility, stronger evidence protocol and release review.

## References

- Figma: https://www.figma.com/design/5HCEW8MLKjVTpJ3XW0s7xM
- Git branch: https://github.com/methakon/smallandroidapp/tree/feat/pifauna-phone-audio
- Parent project: https://github.com/methakon/smallandroidapp

## Change control

Any proposed feature that increases frequency range above 18 kHz, permits external routing, unattended playback, animal-specific deterrence claims or continuous exposure requires separate architecture, safety and compliance review before coding.