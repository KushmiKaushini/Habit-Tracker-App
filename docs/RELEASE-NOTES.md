# Release Notes: Behavior Coach v1.0

**Author:** K K K Ekanayake
**Release Date:** 2026-06-27
**Version:** 1.0.0
**Build:** 1

---

## What's New

Behavior Coach v1.0 is the initial release — an AI-driven habit tracker that adapts to your behavior patterns.

### Features

- **AI Natural Language Intake:** Describe habits in plain English; Gemini AI extracts structured data (title, schedule, time, behavioral tip)
- **Local Fallback Parser:** Full habit creation works offline without an API key
- **Daily Tracking:** 7-day completion grid with retroactive logging
- **Adaptive Coach:** Automatic friction detection with SHIFT_TIME and MICRO_INCREMENT recommendations
- **Manual Habit Entry:** Traditional form-based habit creation
- **Behavioral Tips:** Science-backed strategies displayed per habit
- **Demo Mode:** One-tap failure simulation to test the coach
- **Material Design 3:** Professional lavender/purple theme
- **Offline-First:** All data stored locally in Room database

---

## Known Limitations

| Limitation | Details | Planned Fix |
|---|---|---|
| No push notifications | Habits have no reminders | v1.1 |
| Single user only | No authentication or multi-user support | v2.0 |
| No cloud sync | Data is device-local only | v2.0 |
| English only | No localization | v1.1 |
| Gemini model | Uses gemini-3.5-flash (subject to deprecation) | Configurable in v1.1 |
| No habit categories | All habits in a single list | v1.1 |
| Hardcoded user profile | User "Kush Mi" is hardcoded | v2.0 (with auth) |
| No streak visualization | Completions shown as grid only | v1.1 |
| No data export | Cannot export habit data | v1.1 |
| Android only | No iOS version | v2.0 (KMP evaluation) |

---

## System Requirements

| Requirement | Minimum |
|---|---|
| Android OS | 7.0 (API 24) or higher |
| RAM | 2 GB recommended |
| Storage | 50 MB free space |
| Internet | Optional (required only for AI intake) |

---

## Installation

### Debug Build (Development)

1. Ensure Java 17 is installed and `JAVA_HOME` is set
2. Clone the repository
3. Run `./build.sh assembleDebug`
4. Install APK: `adb install app/build/outputs/apk/debug/app-debug.apk`

### Release Build (Distribution)

1. Configure release keystore via environment variables
2. Run `./build.sh assembleRelease`
3. Distribute via Play Store or sideloading

---

## Dependencies

| Library | Version | Purpose |
|---|---|---|
| Jetpack Compose BOM | 2024.09.00 | UI framework |
| Room | 2.7.0 | Local database |
| Retrofit | 2.12.0 | Networking |
| Moshi | 1.15.2 | JSON serialization |
| Kotlin Coroutines | 1.10.2 | Async/StateFlow |
| Robolectric | 4.16.1 | Unit testing |
| Rorazzi | 1.59.0 | Screenshot testing |
| Firebase BOM | 34.12.0 | (Reserved for future use) |

---

## Bug Fixes

N/A — initial release.

---

## Feedback

Report issues via the project's GitHub issue tracker.

---

**Author:** K K K Ekanayake
**License:** MIT
**Copyright:** 2026 Kaushini Ekanayake
