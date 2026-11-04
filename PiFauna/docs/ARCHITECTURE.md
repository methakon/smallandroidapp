# Architecture

## Shape of the application

```
MainActivity (lifecycle owner)
├── PiFaunaApp            Compose shell, 11 routes, back stack
│   └── screens/…         one composable per Figma frame
├── PlayerController      playback policy + observable state (StateFlow)
│   ├── SoundEngine       AudioTrack streaming, one worker thread
│   │   └── ToneKernel    pure PCM synthesis, no Android types
│   ├── AudioRouteMonitor external-route detection, media volume ratio
│   └── SafetyPolicy      pure start/stop gate
├── SafetyMonitor         battery broadcast + thermal listener
├── SettingsStore         SharedPreferences (last profile, duration, gain)
└── PresetStore           SharedPreferences (custom bands, max 20)
```

One activity, no services, no background workers beyond the audio thread that
lives exactly as long as a session.

## Layers

**Pure core (unit-tested, no Android imports)**

- `audio/ToneKernel.kt` — `ToneConfig` validation and `renderInto()`. Every sample
  is a function of the absolute frame index, so output does not depend on how the
  caller chunks the stream. Guarantees: peak bounded by `gain * 32767`, 25 ms
  fades, per-transition fades for pulse and hop, silence past the session end.
- `safety/SafetyPolicy.kt` — `evaluate(snapshot)` and `mustStop(snapshot)`.
  Deterministic, ordered checks, machine-readable `BlockCode`.
- `data/PresetCodec.kt` — validates and serialises presets.
- `util/TimeFormat.kt` — countdown and frequency formatting.

**Android adapters**

- `audio/SoundEngine.kt` — `AudioTrack` in `MODE_STREAM`, mono PCM16, 48 kHz,
  1024-frame chunks. One `ShortArray` is allocated per engine and reused for
  every session; there is no per-sample allocation in the loop.
- `audio/AudioRouteMonitor.kt` — `AudioDeviceInfo` enumeration and
  `AudioDeviceCallback` so a route change during a session triggers a re-check.
- `safety/SafetyMonitor.kt` — `ACTION_BATTERY_CHANGED` receiver and
  `PowerManager.OnThermalStatusChangedListener`, registered only between
  `onStart` and `onStop`.
- `playback/PlayerController.kt` — the only place that decides whether sound may
  run. Owns audio focus and translates hazards into a stop.

## Playback state machine

```
Idle ──start()──► Playing ──timer/target reached──► Completed
  ▲                  │
  │                  ├── stop (user, lifecycle, focus, hazard) ──► Idle
  │                  └── audio device error ─────────────────────► Refused
  └── Blocked by SafetyPolicy ───────────────────────────────────► Refused
```

`PlayerState` is a `StateFlow`; the UI collects it and never calls `AudioTrack`
directly.

## Concurrency contract

- `SoundEngine.start()` is `synchronized`. It first shuts the previous session
  down (signal → `pause()` → `flush()` → `join(1500)`) and returns
  `StartResult.Busy` if that worker is still alive, so two audio threads can
  never exist at once.
- `stop()` performs the same shutdown and returns only after the worker has
  released the `AudioTrack`.
- The worker owns its `AudioTrack` and releases it in a `finally` block, so a
  device that disappears mid-session cannot leak a track.
- `onFinished` is delivered on the audio thread; `PlayerController` re-posts it to
  the main looper and ignores callbacks from a superseded session token.

## Timer

The deadline is `SystemClock.elapsedRealtime() + durationMs` and is enforced by
the audio loop itself, not by a UI timer. The UI runs a 500 ms tick only while
playing, purely to render the countdown; if it stalls, playback still ends on
time.

## Battery behaviour

- No wake lock, and no `FLAG_KEEP_SCREEN_ON`.
- No polling: battery arrives as a sticky broadcast, thermal status as a
  listener, route changes as a callback.
- One 1024-frame buffer (~2 kB) reused per engine instead of per-chunk
  allocation.
- Leaving the foreground stops playback, which also ends the audio thread and
  releases the device.

## Deliberate omissions

- No `ForegroundService`/`MediaSession`: background audio is refused by design.
- No navigation library: eleven destinations, no deep links, so a plain back
  stack in composition state is smaller and has no dependency cost.
- No network stack at all; the manifest declares no permissions.
- No microphone access, so the app cannot claim to measure its own output.
