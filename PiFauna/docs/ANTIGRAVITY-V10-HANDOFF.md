# Antigravity handoff — Layaswarna timed screen-lock playback v10

Updated: 2026-11-06. **Source code and Figma are prepared; validation and Play rollout remain unfinished.**

## Repo

- Private repository: methakon/smallandroidapp
- Branch: feat/pifauna-phone-audio
- Android subproject: PiFauna/
- App ID: in.berhampore.dhar.layaswarna
- VersionCode: 10; versionName: 0.5.0; minSdk 26, targetSdk 36
- Last Play Internal release reported: versionCode 9; do not confuse v10 source with a published AAB.

## User requirement

Default OFF setting, explicitly opted into in Settings or Player: 'Continue timed playback with screen locked'. ON plus user pressing Start may continue the selected 5/10/15/30-minute PCM session with the screen locked. Foreground media notification must provide working Stop. OFF retains stop-on-lock behavior. Never automatically start/resume, never remove 30-minute limit or sound safety gates.

## Source and design

- See SCREEN-LOCK-PLAYBACK.md and ARCHITECTURE.md.
- MainActivity, PlaybackRuntime, TimedPlaybackService, SettingsStore, PiFaunaApp, PlayerScreen, InfoScreens, BrowseScreens, AndroidManifest and ic_stat_audio updated.
- Platform MediaSession/MediaStyle; no media3 or network stack. FGS permissions FOREGROUND_SERVICE and FOREGROUND_SERVICE_MEDIA_PLAYBACK, service unexported.
- Figma OFF: https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=102-48
- Figma ON: https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=102-73
- Notification concept: https://www.figma.com/design/oPIHDBtBn2H7xcbPmjq2PX?node-id=102-98

## Mandatory execution

1. On the authorized local Ubuntu machine, inspect git status before touching anything. Fetch/switch to feature branch and merge/pull only safely; preserve unrelated uncommitted changes.
2. Inspect newest GitHub Actions run. Run Gradle :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug. Fix compile/runtime/lint regressions. Never claim green unless reported.
3. Inspect service and lifecycle for races: user Stop vs Service watchdog; locked screen vs unlocked Home; Activity re-creation; focus/thermal/route/battery monitoring; Notification Stop; failed foreground service startup; process/task termination. Add regression tests.
4. Test on physically connected Mi A3 API30 and Panasonic ELUGA API28. Verify auto-lock & hardware Power on/off, 5-minute and 10-minute sessions, timer expiry, notification Stop, foreground app switch, lock/unlock, volume/battery/thermal/focus/route, no second AudioTrack or leaked thread. Device reset/factory reset forbidden.
5. Test API33 and API36 emulator/device for notification media exemption, lock-screen Stop, FGS start restrictions and notification permissions. Verify accessibility/TalkBack/200% font and compare screenshots with editable Figma designs.
6. Review legal-site/privacy/index.html, safety/index.html and bundled legal/privacy.html against the actual v10 implementation. Redeploy the updated static site to Cloudflare Pages and verify public HTTPS 200 and policy text; do not change unrelated DNS/MX/SPF. Canonical domain may still need CNAME authorization.
7. In Play Console, prepare the mandatory mediaPlayback foreground-service declaration under App content. Describe explicit user Start, 5–30 minute duration, notification Stop, and impact if OS interrupts. Record a real Android device demonstration video (toggle ON, Start, lock screen, notification, Stop), upload via authorized account and supply the real URL. Figma mock video is not an acceptable replacement.
8. Review app descriptions to clarify optional locked-screen sessions (no medical/repellent efficacy). Prepare signed v10 AAB with existing official upload key only after validation; never expose private signing credentials.
9. If all gates pass, publish v10 to Google Play INTERNAL TESTING ONLY, read back track/versionCode/AAB SHA-256. Do not touch Production, Closed or Open testing. Verify tester opt-in link on an actual authorized Google account; an API completed status alone is insufficient.
10. Update TESTING.md, PLAY-INTERNAL-DEPLOYMENT.md, SCREEN-LOCK-PLAYBACK.md and this report with exact commands, results, device ids, artifact SHA, video URL, release status and any remaining issues. Commit/push corrections.

## Hard safety rules

- Never bypass route (built-in speaker only), 60% media-volume cap, 8% PCM peak, battery cutoff <=15%, severe thermal stop or focus-loss Stop.
- No continuous playback, automatic reboot playback, alarm or background mic, internet, hidden service, wakelock or battery-optimization exemption.
- No FGS/media claim in Play until source behavior and required declaration/video are verified.
- If CI or device acceptance fails, leave v10 in source/development and report exact blocker; do not upload.

## Useful Android source and platform documentation

- https://developer.android.com/media/media3/session/background-playback
- https://support.google.com/googleplay/android-developer/answer/13392821
