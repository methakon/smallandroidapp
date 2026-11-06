# Antigravity — Layaswarna versionCode 11 external speaker routing acceptance

**Development-only feature branch:** `feat/layaswarna-audio-output-consent-v11`. Base is last verified v10 work `a7973a9`. Do not overwrite, rotate keys, rebuild, or upload the existing signed versionCode 10 AAB from the user's machine. VersionCode 11 is not released.

## Mandatory baseline review

Read `PiFauna/docs/AUDIO-OUTPUT-CONSENT.md`, `SAFETY.md`, `TESTING.md`, `ARCHITECTURE.md` and changed Kotlin files. Verify `git status`, remote HEAD and latest GitHub Actions run. Fix any build or lint regressions, including Kotlin API compatibility, and commit/push safely without force push. Do not treat a passing CI run as proof of real audio-routing safety.

## Functional policy

1. Phone speaker default; mere Bluetooth connectivity should not block if AudioTrack actually routes to the phone.
2. A deliberate Bluetooth **free-standing loudspeaker** session needs two separate checkboxes on the Player: not headphones/earbuds/hearing aid; explicit hearing-risk and low-volume acknowledgement. Dismiss/one-checkbox grants nothing.
3. Route mode/consent is valid for one session and one output-device ID only; fail/reset on device change or refused start, Stop, completion, profile changes and app restart.
4. External mode <=5 minutes, <=2% PCM gain and <=30% system media volume. These limits do NOT establish a safe acoustic level.
5. Headphones, hearing aids, USB/HDMI, unidentified devices and multiple/changed output routes remain blocked. Classic A2DP is ambiguous between speakers and headphones; device labels are not proof of type.
6. Existing 15% battery cutoff, thermal, focus loss, audio route, one AudioTrack, screen-lock explicit opt-in and notification Stop remain intact.
7. No Internet, microphone, Bluetooth scan/pairing or additional runtime permissions.

## Device and SDK checks

- Xiaomi Mi A3, Android 11/API30, serial `edd53dbd04de`.
- Panasonic ELUGA, Android 9/API28, serial `LRW86SC6RO8TZ99H`.
- API33 and API36 emulator/device: `getAudioDevicesForAttributes`, `getRoutedDevice` and multi-route `getRoutedDevices` behavior.
- Specifically test a Bluetooth device connected but phone selected; confirm the phone emits sound and no false positive. Test an A2DP speaker under consent after physically verifying it is a *standalone speaker*. Test a real headset (must block), output switch during audio (must stop), unplug/reconnect, notification Stop, force-stop, rapid Start/Stop and 5-minute timer.
- Capture audibility and physical device routing evidence. If actual route cannot be reliably determined before audible frames, **fail closed** and report the device limitation; never simply disable safety checks.
- Check consent dialog at 200% font, TalkBack, keyboard focus and small-screen layout. Avoid claiming measured safe SPL without acoustic measurement.

## Play, Figma and legal materials

Figma frames: [105:48 default](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=105-48), [105:64 consent](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=105-64), [105:78 blocked](https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=105-78).

The updated `legal-site/` files are **source-only**. Do not redeploy or announce v11 as released until tests and legal/policy wording have been reviewed. The bundled policy can be included with a future signed v11 AAB after validation. The existing v10 demo video documents timed screen-lock mediaPlayback, NOT external audio safety.

Do not change Production, Open or Closed testing or post v11 to Internal testing without fresh approval.

## Report

Return Git SHA, CI URL, JVM/device test counts, actual route/device type for each case, consent/session reset evidence, volume/duration/frequency behavior, known device limitations, Figma comparison, legal-page deployment status, and whether v11 is ready for an authorized Internal-test rollout. Do not claim success on unsupported Android versions.
