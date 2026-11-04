# Device validation report

Two physical devices were attached to the test host. Every entry below comes
from a command whose output is reproducible with the commands listed alongside
it. Anything that did not execute is marked as not executed.

## Host and toolchain

| Item | Value |
|---|---|
| Host | Ubuntu, kernel 7.0.0-42-generic |
| ADB used for all work | `/home/…/Android/Sdk/platform-tools/adb` — 1.0.41 / 37.0.1 |
| Conflicting ADB on the host | `/usr/bin/adb` → `/usr/lib/android-sdk/platform-tools/adb` — 1.0.41 / **34.0.4** |
| Host ADB key | single pair in `~/.android/`, created 2026-08-24, unchanged for the session |
| JDK / Gradle / AGP | 17.0.20.1 / 8.9 / 8.7.3 |

## ADB investigation

### Findings

1. **Two ADB builds on the host.** The SDK binary (37.0.1) and the distribution
   binary (34.0.4) both exist. `PATH` puts the SDK copy first in a login shell,
   but non-login shells resolve `/usr/bin/adb`, so the server and the client can
   be different builds.
2. **Stale port files.** `~/.android/adb.5038` and `~/.android/adb.47317` record
   servers previously started on two other ports — more than one server has
   coexisted on this machine.
3. **A stuck server loses a device permanently.** A server that had once listed a
   device as `offline` stopped re-registering that serial even after the device
   re-enumerated: a side-port server (`adb -P 5047 devices`) reported the same
   device as `device` while the main server reported nothing. Killing the server
   process by PID and letting a fresh one start resolved it, twice.
4. **A competing USB consumer exists.** Kernel audit records show the
   snap-packaged Notepad++ (Wine, thread `libusb_event`) opening
   `usb3/3-1/busnum` — the Mi A3 — in the same seconds the device disconnected:
   ```
   apparmor="DENIED" operation="open" profile="snap.notepad-plus-plus.notepad-plus-plus"
   name="/sys/devices/pci0000:00/0000:00:14.0/usb3/3-1/busnum" ... comm="libusb_event"
   ```
   It was stopped for the remainder of the session.
5. **The host key is not the problem.** There is exactly one key pair (no root
   `~/.android/adbkey`), and authorisation survived three server restarts on the
   healthy device (`transport_id` reset, device stayed `device`,
   `authorized=1`). Rotating host identity is therefore ruled out.
6. **No background restarter.** No systemd unit, shell hook or cron job calls
   `adb kill-server`/`start-server`; no Android Studio, emulator or scrcpy was
   running.

### Conclusion

Host identity is stable, so the repeated prompts are not a key-rotation symptom.
Two mechanisms fit the evidence: duplicate/mixed ADB servers causing repeated
reconnect churn, and the Mi A3's own `adbd` going unresponsive on each reconnect,
which re-arms the authorisation dialog on that device. The host side was cleaned
up; the device side could not be, because the phone stops answering ADB entirely.

## Device 1 — Panasonic ELUGA_I7_EE

| Property | Value |
|---|---|
| Serial | `LRW86SC6RO8TZ99H` |
| SoC / RAM | MediaTek mt6739 / 1.9 GB |
| Android | 9 (API 28) |
| Audio route | built-in speaker only (no headset, Bluetooth or USB audio attached) |
| Battery during session | 9% → 16% (USB powered, 500 mA), 34–39 °C |
| Stability | **stable** — constant `transport_id` across sampling; survived three ADB server restarts |

### Instrumented results

Run with `adb -s LRW86SC6RO8TZ99H shell am instrument -w -r -e class … com.pifauna.app.test/androidx.test.runner.AndroidJUnitRunner`.

| Suite | Tests | Result |
|---|---|---|
| `AppLaunchTest` | 2 | **OK (2 tests)** |
| `MainActivityNavigationTest` | 5 | **OK (5 tests)** |
| `SoundEngineDeviceTest` (real `AudioTrack`) | 5 | **OK (5 tests)** |

### Manual hardware checks

| Check | Method | Result |
|---|---|---|
| Battery refusal | drove UI to the player, tapped Start at 12% | **PASS** — "Playback blocked: battery at 12% (limit 15%). Charge the phone first." |
| Real audio output | started a session at 16% battery, 40% volume | **PASS** — `dumpsys audio`: `ID:423 type:android.media.AudioTrack state:started` for `com.pifauna.app`; UI showed "Playing 200–800 Hz …" and a live countdown at 9:52 |
| Audio focus acquired | `dumpsys audio` during playback | **PASS** — `requestAudioFocus … req=1 flags=0x2`, `loss: none` |
| Stop releases everything | tapped Stop | **PASS** — no active track, `abandonAudioFocus()` logged, **0** `PiFaunaAudio` threads left, UI restored to "Start timed playback" |
| Home stops playback | pressed HOME during playback | **PASS** — 1 → 0 tracks; status "Stopped: the app left the foreground." |
| Screen off stops playback | POWER during playback | **PASS** — 1 → 0 tracks, same status |
| Rotation stops playback | forced `user_rotation` during playback (reverted afterwards) | **PASS** — 1 → 0 tracks; display settings restored to their original values |
| Crashes / ANRs | `logcat` and the crash buffer over the whole session | **PASS** — 0 `FATAL EXCEPTION`, 0 `ANR in com.pifauna`, 0 crash-buffer entries |
| Idle memory | `dumpsys meminfo`, app open, not playing | PSS 63,989 kB (Java heap 9,988 kB, native 5,420 kB) |
| Memory during playback | same, session running | PSS 73,062 kB |
| CPU during playback | `dumpsys cpuinfo` | app below the reporting threshold (~0%); system total 30% |
| Thread leak across sessions | `ps -T` after 12 rapid start/stop cycles plus manual sessions | **0** `PiFaunaAudio` threads remaining |

### Framework noise observed

`E AudioTrack: start(): … mState = 1` appears in bursts during the rapid
start/stop test, which creates and tears down 12 `AudioTrack` instances in about
a second. The engine's play/pause ordering was tightened so this no longer
indicates a revived torn-down track: `play()` now runs under the same lock that
teardown takes, and no session starts after its stop was requested. Playback
still starts, completes and releases correctly in every test above, and the
remaining messages come from the MediaTek HAL rather than from application
state.

## Device 2 — Xiaomi Mi A3

| Property | Value |
|---|---|
| Serial | `edd53dbd04de` |
| Android | 11 (API 30) |
| USB state | enumerates as `2717:ff08`, `authorized=1`, ADB interface `ff/42/01` present, configuration named `adb` |
| Stability | **unstable** — registers as `device` for seconds, then disappears; interface left with no driver bound |
| Tests executed | **none** — never reachable long enough to install |

### What was tried, in order

| Action | Outcome |
|---|---|
| `adb reconnect offline` | device dropped from the list entirely |
| ADB server restart (`kill-server`/`start-server`, and by PID) | no change |
| USB port reset via `USBDEVFS_RESET` ioctl | accepted; device did not re-enumerate |
| Stop the competing Wine/libUSB consumer | removed one interference source; device still unstable |
| Fresh side-port server | listed the device as `device` **once**, then not again |

The failure is phone-side: USB enumerates and authorises, but `adbd` stops
answering, so no host-side action recovers it.

### Minimum physical actions required

1. Reboot the phone.
2. Different USB port and a different cable (the adjacent port is proven stable).
3. Developer options → toggle "USB debugging" off then on.
4. Only if all of the above fail: "Revoke USB debugging authorizations", then
   approve once.

A factory reset is **not** recommended: the fault is not in user data, and the
evidence does not implicate the key store.

## Bugs found and fixed in this phase

| # | Defect | Impact | Fix |
|---|---|---|---|
| 1 | `SafetyMonitor` held a field of type `PowerManager.OnThermalStatusChangedListener` (API 29) | **Crash at launch on Android 8 and 9.** ART fails class verification with `NoClassDefFoundError` before any `Build.VERSION` check can run, so the activity died in `onCreate` and no Compose tree existed | API 29 type moved into a nested `@RequiresApi(29)` class; the outer class holds `Any?` |
| 2 | Refusal banner rendered below the primary action | On a 640 dp screen, tapping Start looked like a no-op because the reason was below the fold | Status banner moved above the action button |
| 3 | `AudioTrack.play()` ran on the worker thread | An immediate `stop()` could pause and flush the track first, logging `start(): … mState = 4` and potentially reviving a torn-down stream | `play()` moved into `start()` under the same lock teardown takes |
| 4 | Instrumented tests asserted on off-screen nodes and used an ambiguous `"PiFauna"` matcher | 3 of 5 navigation tests failed for test-quality reasons | Exact matcher; `performScrollTo()` before display assertions |

Bug 1 is release-blocking: without it the app cannot run on any Android 8 or 9
device, which is inside the declared `minSdk 26` range. No lint rule and no JVM
unit test could detect it; only installing on API 28 exposed it.

## Regression coverage added

- `AppLaunchTest` — asserts `MainActivity` reaches `RESUMED` and survives
  `recreate()`. This guards the class-verification failure mode specifically.

## Not executed

- Any test on the Mi A3.
- Wired-headset, Bluetooth and USB-audio route refusal (no such device attached;
  attaching headphones to a phone during automated playback is a physical action
  and an acoustic-exposure risk).
- A real audio-focus interruption (needs a second audio app or an incoming call;
  not reproducible unattended).
- Thermal stop path on hardware (requires heating the device — deliberately not
  performed; covered by unit tests at the policy level).
- Timer expiry over a full session length: the engine's automatic end was verified
  with a 2-second session (`SoundEngineDeviceTest`), not by leaving a 10-minute
  tone running unattended.
- TalkBack and large-font accessibility review.
