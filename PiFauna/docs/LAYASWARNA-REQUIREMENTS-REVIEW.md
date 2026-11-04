# Layaswarna — Requirements and Research Verification (v0.2)

Date: 2026-11-04 · Status: **reviewed research / design requirements; no approval for overnight medical audio**  
Source reviewed: user-supplied *Advanced Methodologies for Long-Term Acoustic Neuromodulation in Mobile Ecosystems: Integrating Vedic Archaeoacoustics, Neurophysiology, and System Architecture* (247-line pasted research report).  
Related design: [LAYASWARNA-CHAKRA-DESIGN-SPEC.md](LAYASWARNA-CHAKRA-DESIGN-SPEC.md).

## 1. Evidence assessment

The source mixes philosophical interpretations, peer-reviewed sleep research, Android engineering recommendations, and strong therapeutic claims. The bibliography includes primary papers, reviews, preprints, commercial websites, and unrelated material; numerical citation markers do not constitute independently checked support. **Do not present the document as a verified clinical or engineering standard.**

| Source claim | Review verdict | Requirement consequence |
|---|---|---|
| Ahata / Anahata distinction in Nada Yoga | Traditional/philosophical framing, not a measurable acoustic protocol | Explain tradition respectfully; do not describe synthesized speaker sound as literal internally perceived *Anahata Nada* |
| Om chanting, tanpura, ragas produce exact predictable brain-state effects | Overgeneralized from heterogeneous studies | Optional cultural/audio descriptions only; no quantified cognitive or medical promise |
| Closed-loop, EEG-phase-locked pink-noise stimulation can modulate slow waves | Research-supported in selected experimental settings; outcomes vary | Document as research background; no claim that an EEG-free phone reproduces clinical CLAS |
| Open-loop 1–4 Hz isochronic/pink-noise audio reliably entrains deep sleep | **Not established**; rhythmic amplitude modulation is not proof of neural entrainment | Experimental relaxation-audio concept only; no SWS, dementia, memory, glymphatic, sleep-stage or treatment claims |
| 400–500 Hz carrier over phone speaker | Plausible engineering starting point, hardware-dependent | Optional lab-only audible carrier; test speaker response and distortion per device |
| Phone app can ensure 35–40 dB(A) at pillow | **Incorrect as a guarantee** without calibrated acoustic measurement | Never display absolute SPL, claim WHO compliance, or assert safe pillow-level dB from digital amplitude |
| WHO 30 dB(A) indoor recommendation and proposed 35–40 dB(A) target | The source's recommended target is internally inconsistent with its quoted threshold; ambient exposure and device output are not interchangeable | Avoid numerical exposure promises; retain low gain, explicit volume gate and supervision |
| Thermal headroom 0..1 and fixed mapping to status | Simplified and OEM-dependent; Android values can be NaN or exceed 1 under some conditions | Prefer categorical thermal callbacks; fail conservatively on unavailable data; no assumed universal mapping |
| Android offload is automatically available for generated PCM | Not guaranteed; support varies by API, device and audio format | No offload requirement until capability-tested; preserve existing streaming engine |
| Android foreground service is mandatory for overnight audio | Only if product explicitly supports background media; conflicts with current foreground-only safety contract | **Do not add service** in current release |
| Google Sleep API measures sleep stages accurately | It estimates coarse sleep-related activity; not an EEG sleep-stage detector | Not in scope; requires separate privacy, permissions and validation review |
| Eight-hour audio is safe if thermal listener is enabled | Not proven; thermal callbacks do not guarantee safe placement, device cooling or battery condition | No unattended/overnight mode in current release |

## 2. Approved current-release requirements

**R-01 — Positioning.** Layaswarna is an offline, nonmedical, supervised **audible tone and relaxation-sound experiment**. Anahata Nada is a planned culturally inspired module name, not a medical indication.

**R-02 — Preserve safety contract.** Maintain current app's 100–18,000 Hz tone range, maximum 8% digital full-scale peak, <=60% device media-volume start/continue gate, battery >15%, stop at severe thermal status, external-route restriction, audio-focus/lifecycle stop, and bounded 5/10/15/30-minute sessions. Do not weaken any rule while redesigning.

**R-03 — Sleep mode deferred.** No 45-minute, continuous, eight-hour, background or unattended playback in the existing build. Any proposed extended duration requires a separate architecture decision, measured power/thermal tests on representative devices, product-safety assessment, and explicit user approval.

**R-04 — New optional relaxation-audio research prototype.** Future prototype may explore low-level audible pink noise and gently amplitude-modulated audible carriers (e.g., 400–500 Hz) under current short-session constraints. Carrier frequency (Hz) and modulation rate (cycles/s) must be shown as **different quantities**. Do not label a 1 Hz modulation as a 1 Hz acoustic carrier.

**R-05 — User comprehension.** Show “Experimental sound; not a medical treatment; sleep effects are not established.” Do not claim treatment of insomnia, dementia, depression, anxiety or memory loss. Avoid misleading “neurotoxin clearance” and guaranteed entrainment copy.

**R-06 — Audio verification.** Unit-test modulation envelope bounds, clipping, start/stop fades, timing, buffer allocation, deterministic rendering, and cancellation. Device-test distortion, route handling, focus loss, thermal/battery stop and real-world audibility. Never infer sound pressure level from PCM amplitude.

**R-07 — Permissions/privacy.** Retain no account/network/microphone permissions in current release. Google Play Services Sleep API and sensor data are out of scope; any future proposal requires opt-in, retention/deletion policy and accuracy disclaimer.

**R-08 — UI/brand.** Retain 11 existing functional routes. Three additional screens remain clearly labeled concepts. Chakra icons must be independently authored editable vectors, not traced screenshots; use labels and 48dp touch targets. Do not equate Chakras with scientifically verified frequencies.

**R-09 — Figma collision requirements.** Artboards must not overlap on canvas. Keep 60px+ gutters between artboards, move large artwork masters to a separate reference zone, and ensure decorative icons/friezes never obscure text, buttons, timer, warning or bottom navigation. Constrain text within each 390px artboard with >=16px right inset. Avoid duplicate icons on the same control.

**R-10 — Release gate.** Figma review is not Android implementation. Require documented source/provenance review, accessibility/contrast checks, real-device tests, lint and unit tests before calling any feature shipped.

## 3. Research backlog (not approved product requirements)

- B-01: Reconcile clinical papers and actual methods/sample sizes; independently validate the original report's citation mapping.
- B-02: Assess whether any optional soundscapes are appropriate for people with hearing sensitivities or sleep disorders; seek qualified clinical input before health claims.
- B-03: Compare real battery/thermal measurements for short pre-rendered clips versus live AudioTrack synthesis, across Android versions.
- B-04: Evaluate audio offload only when the device/API/format support it; measure rather than assume battery benefits.
- B-05: Assess possible background playback separately; it would fundamentally change current foreground-only policy.
- B-06: Investigate copyright/licensing of any chants, raga recordings, textures, fonts and AI-generated imagery before shipping.

## 4. Verification sources and limitations

Relevant primary documentation for **future** independent review:
- Android thermal APIs: https://developer.android.com/games/optimize/adpf/thermal
- AudioTrack.Builder: https://developer.android.com/reference/android/media/AudioTrack.Builder
- Foreground service types: https://developer.android.com/develop/background-work/services/fgs/service-types
- Google Sleep API: https://developers.google.com/android/reference/com/google/android/gms/location/ActivityRecognitionClient
- Sleep stimulation research: https://pubmed.ncbi.nlm.nih.gov/23583623/
- Potential adverse effects of overnight pink noise: https://pmc.ncbi.nlm.nih.gov/articles/PMC10722168/

**Scope of this review:** internal consistency, known API behavior and evidence-quality assessment of the supplied text; not an exhaustive live fact-check of every cited paper. No clinical efficacy or copyright clearance is certified.

## 5. 2026-11-05 implementation-status note

The Figma-only Chakra Sound Console, Seven Chakra Library and Anahata Nada concepts are now **navigable in the Android app**, but this is not approval for additional medical, sleep or continuous audio claims. Seven symbolic chakra tones (396–963 Hz) can be prepared as ordinary audible fixed tones, subject to all existing playback gates. A selected chakra's traditional association, optional listening practice and evidence note appear in an informational panel. Supported short-session Anahata audible tone/sweep options are distinct from the **unimplemented** pink/white noise options; no clinical or overnight playback has been added.

User-approved radial gestures and selected-state highlighting have been authored in Compose with an explicit accessible action alternative. A core Gradle build passed; newest commits must be checked separately. Physical-device, gesture and TalkBack verification are still required. See [the delivery plan](LAYASWARNA-DESIGN-AUDIT-AND-DELIVERY-PLAN.md).
