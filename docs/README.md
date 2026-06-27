# Behavior Coach — README

**Author:** K K K Ekanayake
**Last Updated:** 2026-06-27
**Version:** 1.0

---

## Overview

Behavior Coach is an AI-driven native Android app that helps users build and maintain positive habits. It combines natural language habit creation (powered by Gemini AI) with adaptive behavioral coaching that detects friction patterns and recommends actionable adjustments.

### Key Features

- **AI Natural Language Intake:** Type habits in plain English — the app extracts schedule, time, and behavioral tips automatically
- **Daily Tracking:** 7-day completion grid with retroactive logging
- **Adaptive Coach:** Detects when you're struggling and suggests schedule shifts or micro-increments
- **Offline-First:** Fully functional without internet; AI is an enhancement, not a requirement
- **Material Design 3:** Clean, professional lavender/purple theme

---

## Tech Stack

| Component | Technology |
|---|---|
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose + Material Design 3 |
| Architecture | MVVM + Repository Pattern |
| Database | Room 2.7.0 (SQLite) with KSP |
| Networking | Retrofit 2.12.0 + Moshi |
| AI | Gemini GenerateContent API (v1beta) |
| State | Kotlin StateFlow |
| Build | AGP 9.1.1, Gradle 9.3.1 |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 (Android 15) |

---

## Prerequisites

### Required

1. **Java 17** — The system Java on this machine is 26, which is incompatible with AGP. You must use Java 17.
   - Download: [Temurin JDK 17](https://adoptium.net/temurin/releases/?version=17)
   - Or use the pre-downloaded copy at: `C:\Users\kushm\java\jdk-17.0.12+7`

2. **Android SDK** — With the following components:
   - Platform: `android-36`
   - Build-Tools: `35.0.0` or `34.0.0`
   - Location: `C:\Users\kushm\AppData\Local\Android\Sdk`

3. **Gemini API Key** (optional) — For AI features. Without it, the app uses a local regex parser.
   - Get a key: [Google AI Studio](https://aistudio.google.com/app/apikey)

---

## Quick Start

### Option 1: Using the build helper (recommended)

```bash
./build.sh assembleDebug
```

### Option 2: Manual with Java 17

```bash
export JAVA_HOME=/c/Users/kushm/java/jdk-17.0.12+7
export ANDROID_HOME=/c/Users/kushm/AppData/Local/Android/Sdk
export PATH=$JAVA_HOME/bin:$PATH
./gradlew assembleDebug
```

### Output

APK: `app/build/outputs/apk/debug/app-debug.apk`

---

## Project Structure

```
Habit-Tracker-App/
├── app/
│   ├── build.gradle.kts          # App-level build config
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── java/com/example/
│       │   │   ├── MainActivity.kt          # All Compose UI (897 lines)
│       │   │   ├── ui/
│       │   │   │   ├── HabitViewModel.kt    # State management + coach logic
│       │   │   │   ├── theme/               # MD3 theme, colors, typography
│       │   │   │   └── components/          # Reusable UI components
│       │   │   └── data/
│       │   │       ├── HabitEntities.kt     # Room entities (Habit, CompletionLog)
│       │   │       ├── HabitDao.kt          # Room DAO
│       │   │       ├── HabitDatabase.kt     # Room database singleton
│       │   │       ├── HabitRepository.kt   # Repository + Gemini integration
│       │   │       └── GeminiModels.kt      # Retrofit API models
│       │   ├── res/                         # Android resources
│       │   └── AndroidManifest.xml
│       ├── test/                            # Unit + Robolectric tests
│       └── androidTest/                     # Instrumented tests
├── build.gradle.kts              # Root build file
├── settings.gradle.kts           # Project settings
├── gradle/
│   ├── libs.versions.toml        # Version catalog
│   └── wrapper/                  # Gradle wrapper
├── docs/                         # Documentation
│   ├── PRD.md
│   ├── API.md
│   ├── ARCHITECTURE.md
│   ├── USER-GUIDE.md
│   └── RELEASE-NOTES.md
├── gradlew / gradlew.bat
├── build.sh                      # Build helper script
├── debug.keystore                # Debug signing key (DO NOT COMMIT)
└── README.md                     # This file
```

---

## Build Commands

| Command | Description |
|---|---|
| `./build.sh assembleDebug` | Build debug APK |
| `./build.sh assembleRelease` | Build release APK (requires release keystore) |
| `./build.sh testDebugUnitTest` | Run unit tests |
| `./build.sh clean` | Clean build artifacts |
| `./build.sh tasks` | List all available Gradle tasks |

---

## Testing

```bash
# Unit tests (JVM + Robolectric)
./build.sh testDebugUnitTest

# Screenshot tests (Rorazzi)
./build.sh verifyRoborazziDebug

# Instrumented tests (requires device/emulator)
./build.sh connectedAndroidTest
```

---

## Configuration

### Gemini API Key

The app uses the Secrets Gradle Plugin. Configure your API key in one of:

1. **`.env` file** (project root, git-ignored):
   ```
   GEMINI_KEY=your_api_key_here
   ```

2. **Environment variable:**
   ```bash
   export GEMINI_KEY=your_api_key_here
   ```

3. **AI Studio Secrets panel** (if building in Android Studio)

Without a key, the app operates in local parser mode with a warning banner.

### Signing

- Debug: Uses auto-generated `debug.keystore` (git-ignored)
- Release: Configure via environment variables `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`

---

## Architecture

See `docs/architecture/ARCHITECTURE.md` for full technical details.

**Pattern:** MVVM with Repository
- **Data Layer:** Room database with Flow-based observables
- **Repository:** Mediates between DAO and Gemini API; handles fallback logic
- **ViewModel:** Exposes StateFlow to Compose UI; contains friction analysis algorithm
- **UI:** Single-activity Compose app with bottom navigation (Daily Coach / AI Intake)

---

## Contributing

1. Create a feature branch: `git checkout -b feature/<role>/<description>`
2. Make changes, commit with descriptive messages
3. Push branch: `git push origin HEAD`
4. Open a pull request for PM review
5. After approval, PM merges to main

**Branch naming:** `feature/<role>/<task>` (e.g., `feature/dev/add-notifications`)

---

## License

MIT License — see `LICENSE` file for details.

Copyright (c) 2026 Kaushini Ekanayake

---

## Contact

**Author:** K K K Ekanayake
