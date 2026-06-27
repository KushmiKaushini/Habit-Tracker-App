# Architecture: Behavior Coach

**Author:** K K K Ekanayake
**Last Updated:** 2026-06-27
**Status:** Active

---

## 1. Overview & Design Principles

**Behavior Coach** is a native Android application designed to help users build and maintain positive habits through intelligent coaching. The app combines local habit tracking (daily check-ins) with AI-powered behavioral analysis to detect friction patterns and recommend actionable habit modifications.

### 1.1 Purpose

- Allow users to define habits with scheduling details (time-of-day, frequency).
- Log daily completions and misses per habit.
- Analyze the past 7 days of completion data to detect friction (repeated misses on scheduled days).
- Generate AI-driven recommendations (e.g., SHIFT_TIME, MICRO_INCREMENT) via the Gemini API, with a local regex-based fallback when the network is unavailable or the API key is absent.
- Present a clean, Material Design 3 UI built with Jetpack Compose.

### 1.2 Design Principles

| Principle | Rationale |
|---|---|
| **Offline-first** | All habit data is stored locally in Room. The app is fully functional without network access. |
| **Single source of truth** | Room database is the canonical store. UI observes `StateFlow` emissions from the ViewModel. |
| **Graceful degradation** | Gemini AI is an enhancement, not a requirement. A local regex parser provides recommendations when AI is unavailable. |
| **Separation of concerns** | Clear layering: Data → Repository → ViewModel → UI. Each layer has a well-defined responsibility. |
| **Minimal module surface** | Single-module architecture reduces build complexity and is appropriate for the app's scope. |
| **Testability** | Repository pattern and ViewModel abstraction enable unit testing with Robolectric and screenshot testing with Rorazzi. |

---

## 2. Architecture Pattern (MVVM)

The app follows the **Model-View-ViewModel (MVVM)** pattern, recommended by Google for Jetpack Compose applications.

```
┌─────────────────────────────────────────────────────┐
│                    UI Layer (View)                   │
│              Jetpack Compose Screens                 │
│         DailyTrackerScreen, IntakeScreen,            │
│         HabitItemCard, ManualHabitDialog             │
└──────────────────────┬──────────────────────────────┘
                       │ observes StateFlow
                       ▼
┌─────────────────────────────────────────────────────┐
│               ViewModel Layer                        │
│              HabitViewModel                          │
│   (habits, completionLogs, intakeState,              │
│    userProfile, coachRecommendations)                │
└──────────────────────┬──────────────────────────────┘
                       │ calls suspend functions
                       ▼
┌─────────────────────────────────────────────────────┐
│              Repository Layer                        │
│              HabitRepository                         │
│   (wraps HabitDao, calls Gemini API)                 │
└────────┬─────────────────────────┬──────────────────┘
         │                         │
         ▼                         ▼
┌─────────────────┐    ┌──────────────────────────┐
│   Data Layer    │    │   External Service       │
│   Room (local)  │    │   Gemini GenerateContent │
│   Entities,     │    │   API (v1beta)           │
│   DAO, Database │    │   via Retrofit           │
└─────────────────┘    └──────────────────────────┘
```

### 2.1 Data Flow

1. **User action** (e.g., marking a habit complete) triggers a Composable callback.
2. The callback invokes a method on `HabitViewModel`.
3. `HabitViewModel` calls `HabitRepository` (suspend function).
4. `HabitRepository` writes to Room via `HabitDao`.
5. Room emits new data; `HabitViewModel` collects it via `Flow` and exposes it as `StateFlow`.
6. Compose UI recomposes automatically on `StateFlow` changes.

### 2.2 State Management

- `HabitViewModel` extends `AndroidViewModel` (access to application context for Room).
- All UI state is exposed as `StateFlow<T>` (immutable, hot observable).
- UI collects state via `collectAsStateWithLifecycle()` for lifecycle-aware collection.
- One-shot events (snackbars, navigation) are handled via a separate `SharedFlow` channel.

---

## 3. Module / Package Structure

### 3.1 Single-Module Layout

```
com.example
├── MainActivity.kt                  # Entry point; hosts all Composables
│   ├── CoachAppContainer()          # Top-level scaffold + nav host
│   ├── DailyTrackerScreen()         # Main habit list with completion toggles
│   ├── HabitItemCard()              # Individual habit row/card
│   ├── IntakeScreen()               # Onboarding / habit creation form
│   └── ManualHabitDialog()          # Dialog for adding habits manually
│
├── ui/
│   ├── HabitViewModel.kt            # AndroidViewModel with StateFlow
│   ├── theme/
│   │   ├── Color.kt                 # MD3 color palette
│   │   ├── Theme.kt                 # MaterialTheme wrapper
│   │   └── Type.kt                  # Typography definitions
│   └── components/                  # Reusable Composable components
│
└── data/
    ├── db/
    │   ├── HabitEntity.kt           # Room entity: habits table
    │   ├── CompletionLogEntity.kt   # Room entity: completion_logs table
    │   ├── HabitDao.kt              # Data Access Object
    │   └── AppDatabase.kt           # Room database singleton
    ├── model/
    │   ├── Habit.kt                 # Domain model
    │   ├── CompletionLog.kt         # Domain model
    │   ├── IntakeState.kt           # Onboarding state
    │   ├── UserProfile.kt           # User preferences
    │   └── CoachRecommendation.kt   # AI recommendation model
    ├── remote/
    │   ├── GeminiApiService.kt       # Retrofit interface
    │   ├── GeminiRequest.kt         # Request DTO (Moshi)
    │   ├── GeminiResponse.kt        # Response DTO (Moshi)
    │   └── dto/                     # Additional API DTOs
    └── repository/
        ├── HabitRepository.kt       # Main repository
        └── FrictionAnalyzer.kt      # Friction analysis logic
```

### 3.2 Dependency Direction

```
UI → ViewModel → Repository → (Room DAO | Gemini API)
```

- UI never accesses the database or network directly.
- ViewModel never imports Compose or Android UI classes (except `AndroidViewModel`).
- Repository is the only class that knows about both local and remote data sources.

---

## 4. Data Layer

### 4.1 Room Schema

#### Entity: `habits`

| Column | Type | Constraints |
|---|---|---|
| `id` | `INTEGER` | PRIMARY KEY AUTOINCREMENT |
| `name` | `TEXT` | NOT NULL |
| `description` | `TEXT` | NOT NULL, default `""` |
| `scheduled_time` | `TEXT` | NOT NULL (HH:mm format) |
| `scheduled_days` | `TEXT` | NOT NULL (comma-separated: "Mon,Wed,Fri") |
| `created_at` | `INTEGER` | NOT NULL (epoch millis) |
| `is_active` | `INTEGER` | NOT NULL (0/1 boolean) |

#### Entity: `completion_logs`

| Column | Type | Constraints |
|---|---|---|
| `id` | `INTEGER` | PRIMARY KEY AUTOINCREMENT |
| `habit_id` | `INTEGER | NOT NULL, FOREIGN KEY → habits(id) |
| `date` | `TEXT` | NOT NULL (yyyy-MM-dd format) |
| `completed` | `INTEGER` | NOT NULL (0/1 boolean) |
| `logged_at` | `INTEGER` | NOT NULL (epoch millis) |

**Index:** `CREATE INDEX idx_completion_habit_date ON completion_logs(habit_id, date)`

### 4.2 DAO (HabitDao)

```kotlin
@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE is_active = 1 ORDER BY created_at ASC")
    fun getAllActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM completion_logs WHERE habit_id = :habitId AND date BETWEEN :startDate AND :endDate")
    suspend fun getLogsForHabit(habitId: Int, startDate: String, endDate: String): List<CompletionLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletionLog(log: CompletionLogEntity)

    @Query("SELECT * FROM completion_logs WHERE date = :date")
    suspend fun getLogsForDate(date: String): List<CompletionLogEntity>

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("UPDATE habits SET is_active = 0 WHERE id = :habitId")
    suspend fun deactivateHabit(habitId: Int)
}
```

### 4.3 Database Singleton

```kotlin
@Database(entities = [HabitEntity::class, CompletionLogEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "behavior_coach.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
```

- **KSP** is used for annotation processing (replacing kapt).
- Schema is exported to `app/schemas/` for version control and migration testing.
- `allowMainThreadQueries` is **never** enabled; all DB access is via coroutines.

---

## 5. Domain / Repository Layer

### 5.1 Repository Pattern

`HabitRepository` is the single point of access for all habit-related data operations. It abstracts the data sources (Room, Gemini API) from the ViewModel.

```kotlin
class HabitRepository(
    private val habitDao: HabitDao,
    private val geminiApiService: GeminiApiService?,
    private val frictionAnalyzer: FrictionAnalyzer
) {
    fun getActiveHabits(): Flow<List<Habit>> =
        habitDao.getAllActiveHabits().map { list -> list.map { it.toDomain() } }

    suspend fun addHabit(habit: Habit): Long =
        habitDao.insertHabit(habit.toEntity())

    suspend fun logCompletion(habitId: Int, date: String, completed: Boolean) { ... }

    suspend fun getRecommendations(): List<CoachRecommendation> {
        val habits = habitDao.getAllActiveHabits().first()
        val logs = mutableListOf<CompletionLogEntity>()
        habits.forEach { habit ->
            logs.addAll(habitDao.getLogsForHabit(habit.id, sevenDaysAgo(), today()))
        }
        return try {
            callGeminiApi(habits, logs)
        } catch (e: Exception) {
            frictionAnalyzer.analyzeLocally(habits, logs)  // Fallback
        }
    }
}
```

### 5.2 Data Flow Through Repository

```
ViewModel.getRecommendations()
  └─► Repository.getRecommendations()
        ├─► habitDao.getAllActiveHabits()        → Flow<List<HabitEntity>>
        ├─► habitDao.getLogsForHabit(...)        → List<CompletionLogEntity>
        ├─► GeminiApiService.generateContent()   → GeminiResponse (try)
        │     └─► on failure → FrictionAnalyzer.analyzeLocally()
        └─► List<CoachRecommendation>
```

### 5.3 Domain Models

```kotlin
data class Habit(
    val id: Int = 0,
    val name: String,
    val description: String,
    val scheduledTime: String,     // "07:30"
    val scheduledDays: List<String>, // ["Mon", "Wed", "Fri"]
    val createdAt: Long,
    val isActive: Boolean
)

data class CompletionLog(
    val id: Int = 0,
    val habitId: Int,
    val date: String,              // "2026-06-27"
    val completed: Boolean,
    val loggedAt: Long
)

data class CoachRecommendation(
    val habitId: Int,
    val type: RecommendationType,  // SHIFT_TIME, MICRO_INCREMENT, MAINTAIN
    val message: String,
    val suggestedTime: String?     // For SHIFT_TIME recommendations
)

enum class RecommendationType { SHIFT_TIME, MICRO_INCREMENT, MAINTAIN }
```

---

## 6. Presentation Layer

### 6.1 Compose UI Structure

All Composables reside in `MainActivity.kt` for simplicity (single-screen-dominant app):

```
MainActivity
└── CoachAppContainer()                    // Scaffold + TopAppBar
    ├── DailyTrackerScreen()               // LazyColumn of habits
    │   └── HabitItemCard()                // Per-habit card with checkbox
    ├── IntakeScreen()                     // Onboarding form
    └── ManualHabitDialog()                // AlertDialog for new habits
```

### 6.2 HabitViewModel

```kotlin
class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HabitRepository

    val habits: StateFlow<List<Habit>>
    val completionLogs: StateFlow<List<CompletionLog>>
    val intakeState: StateFlow<IntakeState>
    val userProfile: StateFlow<UserProfile>
    val coachRecommendations: StateFlow<List<CoachRecommendation>>

    fun addHabit(name: String, description: String, time: String, days: List<String>)
    fun toggleCompletion(habitId: Int, date: String)
    fun refreshRecommendations()
    fun updateProfile(profile: UserProfile)
}
```

**StateFlow initialization pattern:**
```kotlin
habits = repository.getActiveHabits()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
```

### 6.3 UI State Classes

```kotlin
data class IntakeState(
    val currentStep: Int = 0,
    val habitName: String = "",
    val habitDescription: String = "",
    val selectedTime: String = "09:00",
    val selectedDays: Set<String> = emptySet(),
    val isComplete: Boolean = false
)

data class UserProfile(
    val name: String = "",
    val preferredWakingHour: Int = 7,
    val preferredSleepHour: Int = 22,
    val timezone: String = TimeZone.getDefault().id
)
```

### 6.4 Compose Best Practices Applied

- **Unidirectional data flow:** UI emits events → ViewModel processes → StateFlow updates → UI recomposes.
- **Hoisting:** Stateful Composables are stateless where possible; state lives in ViewModel.
- **Preview annotations:** All screens have `@Preview` composables for development.
- **Material Design 3:** Dynamic color support, proper color tokens, `MaterialTheme` wrapping.

---

## 7. AI Integration

### 7.1 Gemini API Configuration

| Property | Value |
|---|---|
| Endpoint | `https://generativelanguage.googleapis.com/v1beta/` |
| Model | `gemini-2.0-flash` (or configured variant) |
| Method | `generateContent` |
| Auth | API key via query parameter (`?key=`) |
| Serialization | Moshi (`@Json` annotations) |

### 7.2 Structured Output Schema

The API is configured to return structured JSON matching a predefined schema:

```json
{
  "responseMimeType": "application/json",
  "responseSchema": {
    "type": "OBJECT",
    "properties": {
      "recommendations": {
        "type": "ARRAY",
        "items": {
          "type": "OBJECT",
          "properties": {
            "habitId": { "type": "INTEGER" },
            "type": { "type": "STRING", "enum": ["SHIFT_TIME", "MICRO_INCREMENT", "MAINTAIN"] },
            "message": { "type": "STRING" },
            "suggestedTime": { "type": "STRING" }
          },
          "required": ["habitId", "type", "message"]
        }
      }
    }
  }
}
```

### 7.3 Request Construction

```kotlin
data class GeminiRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig
)

data class GenerationConfig(
    @Json(name = "temperature") val temperature: Double = 0.1,
    @Json(name = "responseMimeType") val responseMimeType: String = "application/json",
    @Json(name = "responseSchema") val responseSchema: Map<String, Any>
)
```

**Temperature:** Set to `0.1` for deterministic, consistent recommendations. Low temperature reduces hallucination and ensures the output conforms to the schema.

### 7.4 Retrofit Service

```kotlin
interface GeminiApiService {
    @POST("models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}
```

### 7.5 Fallback Parser

When the Gemini API is unavailable (no network, missing/invalid API key, rate limit), the app falls back to `FrictionAnalyzer.analyzeLocally()`:

- Uses regex pattern matching on habit names and descriptions.
- Applies rule-based logic (same algorithm as Section 8).
- Returns `List<CoachRecommendation>` in the same format as the API response.
- The ViewModel cannot distinguish between AI-generated and locally-generated recommendations.

---

## 8. Friction Analysis Algorithm

### 8.1 Overview

The friction analysis identifies habits where the user consistently fails to complete on scheduled days, then recommends a behavioral modification.

### 8.2 Algorithm Flow

```
analyzeFrictionAndGenerateRecommendations()
│
├─► For each active habit:
│   ├─► Retrieve completion logs for past 7 days
│   ├─► Determine which of those 7 days were "scheduled" days
│   ├─► Count: scheduled_misses = scheduled_days_where_completed = false
│   ├─► Count: scheduled_completions = scheduled_days_where_completed = true
│   └─► Calculate: friction_ratio = scheduled_misses / total_scheduled_days
│
├─► Apply thresholds:
│   ├─► friction_ratio >= 0.7  →  Recommend SHIFT_TIME
│   │   └─► Suggest time 30 min earlier or later based on completion pattern
│   ├─► friction_ratio >= 0.4  →  Recommend MICRO_INCREMENT
│   │   └─► Suggest reducing scope (e.g., "5 min meditation" instead of "30 min")
│   └─► friction_ratio < 0.4   →  Recommend MAINTAIN
│
└─► Return List<CoachRecommendation>
```

### 8.3 Detailed Steps

1. **Define window:** `today - 6 days` to `today` (inclusive) = 7-day sliding window.
2. **For each habit:**
   a. Parse `scheduledDays` (e.g., `["Mon", "Wed", "Fri"]`).
   b. For each date in the window, check if the day-of-week matches a scheduled day.
   c. For each scheduled day, check `completion_logs` for a matching entry.
   d. If no log exists or `completed = false`, count as a **miss**.
3. **Calculate friction ratio:**
   ```
   friction_ratio = scheduled_misses / (scheduled_misses + scheduled_completions)
   ```
4. **Generate recommendation based on thresholds:**

| Friction Ratio | Recommendation | Logic |
|---|---|---|
| ≥ 0.70 | `SHIFT_TIME` | User consistently misses at current time. Suggest ±30 min offset based on when they *do* complete. |
| 0.40 – 0.69 | `MICRO_INCREMENT` | Partial adherence. Reduce habit scope to lower activation energy. |
| < 0.40 | `MAINTAIN` | Good adherence. No change needed. |

5. **Time suggestion logic (SHIFT_TIME):**
   - If completions cluster in the morning → suggest 30 min earlier.
   - If completions cluster in the evening → suggest 30 min later.
   - If no clear pattern → suggest a different time block entirely (e.g., afternoon).

### 8.4 Gemini Enhancement

When Gemini is available, the same 7-day data is sent as context in the prompt. Gemini provides:
- More nuanced recommendations (e.g., habit stacking suggestions).
- Natural-language explanations tailored to the user's profile.
- Alternative strategies beyond the rule-based thresholds.

The structured output schema ensures the response is parseable into `CoachRecommendation` objects regardless of the natural language content.

---

## 9. Build System

### 9.1 Gradle Configuration

| Property | Value |
|---|---|
| Gradle | 9.3.1 |
| Android Gradle Plugin | 9.1.1 |
| Kotlin | 2.2.10 |
| KSP | 2.2.10-2.0.2 |
| compileSdk | 36 |
| targetSdk | 36 |
| minSdk | 24 |
| Java toolchain | 17 |
| Build types | `debug`, `release` |

### 9.2 Key Dependencies

```kotlin
// Compose BOM
implementation(platform("androidx.compose:compose-bom:2024.12.01"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.ui:ui-tooling-preview")
implementation("androidx.activity:activity-compose:1.9.3")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

// Room
implementation("androidx.room:room-runtime:2.7.0")
implementation("androidx.room:room-ktx:2.7.0")
ksp("androidx.room:room-compiler:2.7.0")

// Networking
implementation("com.squareup.retrofit2:retrofit:2.12.0")
implementation("com.squareup.retrofit2:converter-moshi:2.12.0")
implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.1")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

// Testing
testImplementation("junit:junit:4.13.2")
testImplementation("org.robolectric:robolectric:4.14.1")
testImplementation("com.github.takahirom:rorazzi:1.2.0")
testImplementation("androidx.test.ext:junit:1.2.1")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
```

### 9.3 Build Variants

| Variant | Signing | API Key | Notes |
|---|---|---|---|
| `debug` | Debug keystore | From `local.properties` or BuildConfig | Logging enabled, strict mode off |
| `release` | Release keystore | From `local.properties` (not committed) | R8 minification, no logging |

### 9.4 Signing

- **Debug:** Default Android debug keystore (`~/.android/debug.keystore`).
- **Release:** Custom keystore file referenced via `signingConfigs` in `build.gradle.kts`.
- Keystore credentials stored in `local.properties` (gitignored) or environment variables.

---

## 10. Testing Strategy

### 10.1 Test Pyramid

```
         ┌──────────┐
         │ Screenshot│  (Rorazzi - visual regression)
         │  Tests    │
         ├──────────┤
         │Robolectric│ (ViewModel, Repository, DAO)
         │  Tests     │
         ├──────────┤
         │   Unit     │ (FrictionAnalyzer, regex parser, mappers)
         │   Tests    │
         └──────────┘
```

### 10.2 Unit Tests

- **Location:** `src/test/java/com/example/`
- **Framework:** JUnit 4 + kotlinx-coroutines-test
- **Targets:**
  - `FrictionAnalyzer` — verify threshold logic, edge cases (0 scheduled days, all misses, all completions).
  - `HabitRepository` — verify fallback behavior when Gemini throws.
  - Entity ↔ Domain mappers.
  - Regex fallback parser.

```kotlin
@Test
fun `friction ratio 0_7 returns SHIFT_TIME`() {
    val analyzer = FrictionAnalyzer()
    val result = analyzer.analyzeLocally(habits = listOf(testHabit), logs = mostlyMissLogs)
    assertEquals(RecommendationType.SHIFT_TIME, result.first().type)
}
```

### 10.3 Robolectric Tests

- **Location:** `src/test/java/com/example/`
- **Framework:** Robolectric 4.14.1
- **Targets:**
  - `HabitViewModel` — verify StateFlow emissions, coroutine behavior.
  - `HabitDao` — verify SQL queries against in-memory Room database.
  - `AppDatabase` — verify migration paths (when applicable).

```kotlin
@RunWith(AndroidJUnit4::class)
class HabitViewModelTest {
    @Test
    fun `toggling completion updates StateFlow`() = runTest {
        // Given: ViewModel with test repository
        // When: toggleCompletion() called
        // Then: completionLogs StateFlow emits updated list
    }
}
```

### 10.4 Screenshot Tests (Rorazzi)

- **Location:** `src/test/java/com/example/ui/`
- **Framework:** Rorazzi (Pixel 4 API 34 configuration)
- **Targets:**
  - `DailyTrackerScreen` — empty state, populated state, all-completed state.
  - `HabitItemCard` — checked, unchecked, with recommendation badge.
  - `IntakeScreen` — each step of the onboarding flow.
  - `ManualHabitDialog` — default state, validation error state.

```kotlin
@Test
fun dailyTrackerScreen_populated() {
    composeTestRule.setContent {
        DailyTrackerScreen(
            habits = sampleHabitList,
            onToggle = {},
            recommendations = sampleRecommendations
        )
    }
    composeTestRule.onNodeWithTag("daily_tracker_screen")
        .assertIsDisplayed()
        .captureRoboImage("daily_tracker_populated.png")
}
```

### 10.5 Test Coverage Goals

| Layer | Target Coverage |
|---|---|
| FrictionAnalyzer | 95%+ |
| Repository | 85%+ |
| ViewModel | 80%+ |
| DAO | 70%+ |
| UI (screenshot) | All screens in all states |

---

## 11. Security Considerations

### 11.1 API Key Handling

| Concern | Mitigation |
|---|---|
| Key in source control | API key is **never** committed. Stored in `local.properties` (gitignored). |
| Key in APK | Key is injected via `BuildConfig.GEMINI_API_KEY` at build time. For release, consider server-side proxy. |
| Key in logs | API key is never logged. Retrofit logging interceptor is disabled for release builds. |
| Key rotation | Key can be rotated by updating `local.properties` and rebuilding. |

### 11.2 BuildConfig Usage

```kotlin
// build.gradle.kts
val geminiApiKey: String = project.findProperty("GEMINI_API_KEY") as? String ?: ""
buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
```

- If the key is empty, the app skips Gemini calls entirely and uses the local fallback.
- The key is only included in the build if explicitly provided.

### 11.3 Local-First Data

- All user data (habits, logs, profile) is stored **only** in the local Room database.
- No user data is transmitted to any server except the anonymized habit completion context sent to Gemini for recommendations.
- No authentication or PII is collected.

### 11.4 Network Security

- All network communication uses HTTPS (TLS 1.2+).
- Retrofit base URL is `https://generativelanguage.googleapis.com/`.
- Certificate pinning is not implemented (Google-managed certificates rotate frequently) but can be added if required.

### 11.5 Data Privacy

- No analytics SDKs are integrated.
- No crash reporting SDKs are integrated (can be added later with user consent).
- The app does not request unnecessary permissions (only `INTERNET` for Gemini API).

---

## 12. Performance & Scalability

### 12.1 Startup Performance

| Optimization | Implementation |
|---|---|
| Lazy database initialization | Room database is created on first access, not in `Application.onCreate()`. |
| Compose lazy loading | `LazyColumn` for habit list; only visible items are composed. |
| No blocking main thread | All DB operations are `suspend` functions dispatched on `Dispatchers.IO`. |
| ViewModel scope | `viewModelScope` cancels coroutines on ViewModel clear, preventing leaks. |

### 12.2 Runtime Performance

| Concern | Strategy |
|---|---|
| Recomposition | StateFlow ensures only changed state triggers recomposition. `derivedStateOf` for computed values. |
| List performance | `LazyColumn` with stable keys (`key = { it.id }`) for efficient diffing. |
| Image loading | No images in current scope; if added, use Coil with memory/disk cache. |
| Gemini API latency | API call is fire-and-forget from UI perspective; recommendations update asynchronously. |

### 12.3 Database Performance

- **Indexing:** Composite index on `(habit_id, date)` for fast log lookups.
- **Query scope:** 7-day window limits result set size.
- **Flow-based observation:** Room `Flow` emits only on actual data change, not on poll.
- **WAL mode:** Enabled by default in Room for concurrent read/write performance.

### 12.4 Scalability Considerations

| Dimension | Current Design | Future Scaling |
|---|---|---|
| Habits per user | ~10-50 (single user) | Pagination if needed; current `Flow<List>` handles thousands |
| Completion logs | ~365/year per habit | Partition by year or archive old logs |
| Module structure | Single module | Extract `:core`, `:data`, `:feature:*` modules if team grows |
| AI provider | Gemini only | Abstract `AiCoach` interface for multi-provider support |
| Multi-user | Single device user | Add user authentication + cloud sync (Firebase/Supabase) |

### 12.5 Memory Management

- `AndroidViewModel` ensures ViewModel survives configuration changes without leaking Activity.
- `SharingStarted.WhileSubscribed(5000)` stops upstream Flow collection 5 seconds after last subscriber leaves.
- Room `Flow` collection is lifecycle-aware via `collectAsStateWithLifecycle()`.
- No static references to Context or Views.

---

## Appendix A: Technology Summary

| Category | Technology | Version |
|---|---|---|
| Language | Kotlin | 2.2.10 |
| Build | Gradle | 9.3.1 |
| Build | Android Gradle Plugin | 9.1.1 |
| UI | Jetpack Compose | BOM 2024.12.01 |
| UI | Material Design 3 | (bundled with Compose) |
| Architecture | MVVM + StateFlow | AndroidX Lifecycle 2.8.7 |
| Database | Room | 2.7.0 |
| Annotation Processing | KSP | 2.2.10-2.0.2 |
| Networking | Retrofit | 2.12.0 |
| Serialization | Moshi | 1.15.1 |
| AI | Gemini GenerateContent API | v1beta |
| Testing (Unit) | JUnit 4 | 4.13.2 |
| Testing (Integration) | Robolectric | 4.14.1 |
| Testing (Screenshot) | Rorazzi | 1.2.0 |
| Coroutines | kotlinx-coroutines | 1.9.0 |

## Appendix B: Key Files Reference

| File | Purpose |
|---|---|
| `MainActivity.kt` | App entry point, all Composables |
| `ui/HabitViewModel.kt` | Central ViewModel with all StateFlows |
| `data/db/AppDatabase.kt` | Room database singleton |
| `data/db/HabitDao.kt` | Database queries |
| `data/repository/HabitRepository.kt` | Repository pattern implementation |
| `data/repository/FrictionAnalyzer.kt` | Friction analysis algorithm |
| `data/remote/GeminiApiService.kt` | Retrofit interface for Gemini |
| `data/model/CoachRecommendation.kt` | Recommendation domain model |

---

*Document prepared by K K K Ekanayake — Senior Mobile Developer*
