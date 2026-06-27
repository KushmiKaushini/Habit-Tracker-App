# API Documentation: Behavior Coach

**Author:** K K K Ekanayake
**Last Updated:** 2026-06-27
**Status:** Active
**Version:** 1.0

---

## Table of Contents

1. [Internal API: Gemini Integration](#internal-api-gemini-integration)
2. [Future Public API Design](#future-public-api-design)

---

## Internal API: Behavior Coach

The app integrates with Google's Gemini API to extract structured habit information from natural language input. This section covers the internal API contract.

### 1.1 Endpoint

```
POST https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key={API_KEY}
```

**Base URL:** `https://generativelanguage.googleapis.com/`
**Model:** `gemini-3.5-flash`
**Authentication:** Query parameter `key` (Gemini API key)

### 1.2 Request Schema

The app sends a `GenerateContentRequest` with structured output configuration:

```json
{
  "contents": [
    {
      "parts": [
        {
          "text": "Extract structured habit information from this conversational phrase:\n\"Read a finance book for 20 minutes every Monday and Wednesday night at 10 PM\"\n\nFormat response as JSON with fields:\n- title (string, capitalized, concise)\n- frequency (string, restricted to: \"Daily\" or \"Weekly\")\n- activeDays (array of strings, e.g. [\"Monday\", \"Wednesday\"] or all 7 days if daily)\n- targetTime (string, formatted strictly as 24-hour \"HH:MM\", e.g. \"07:30\" or \"21:00\")\n- behavioralTip (string, high-quality, actionable, behavioral psychology tip matching the specific routine to reduce friction)"
        }
      ]
    }
  ],
  "generationConfig": {
    "responseMimeType": "application/json",
    "responseSchema": {
      "type": "OBJECT",
      "properties": {
        "title": { "type": "STRING", "description": "The short title of the habit activity" },
        "frequency": { "type": "STRING", "description": "Daily or Weekly" },
        "activeDays": { "type": "ARRAY", "items": { "type": "STRING" } },
        "targetTime": { "type": "STRING", "description": "The time of day in 24-hour HH:MM format" },
        "behavioralTip": { "type": "STRING", "description": "A behavioral optimization/tip to maintain the streak" }
      },
      "required": ["title", "frequency", "activeDays", "targetTime", "behavioralTip"]
    },
    "temperature": 0.1
  },
  "systemInstruction": {
    "parts": [
      {
        "text": "You are an elite behavior design coach. You extract precise habit entities from conversational natural language strings and output scientifically tailored behavioral tips."
      }
    ]
  }
}
```

**Key fields:**

| Field | Type | Description |
|---|---|---|
| `contents[].parts[].text` | string | The prompt containing user's natural language input |
| `generationConfig.responseMimeType` | string | Must be `"application/json"` for structured output |
| `generationConfig.responseSchema` | object | JSON schema defining expected output structure |
| `generationConfig.temperature` | float | 0.1 for deterministic, consistent extraction |
| `systemInstruction.parts[].text` | string | System prompt defining the AI persona |

### 1.3 Response Schema

```json
{
  "candidates": [
    {
      "content": {
        "parts": [
          {
            "text": "{\"title\":\"Read A Finance Book For 20 Minutes\",\"frequency\":\"Weekly\",\"activeDays\":[\"Monday\",\"Wednesday\"],\"targetTime\":\"22:00\",\"behavioralTip\":\"Anchor your reading session to an existing evening routine — place the book on your pillow after making the bed each morning.\"}"
          }
        ]
      }
    }
  ]
}
```

The `text` field in the response contains a JSON string matching the requested schema:

```json
{
  "title": "Read A Finance Book For 20 Minutes",
  "frequency": "Weekly",
  "activeDays": ["Monday", "Wednesday"],
  "targetTime": "22:00",
  "behavioralTip": "Anchor your reading session to an existing evening routine..."
}
```

### 1.4 Data Models (Kotlin)

```kotlin
// Request models
@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(val parts: List<Part>)

@JsonClass(generateAdapter = true)
data class Part(val text: String? = null)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseMimeType: String? = null,
    val responseSchema: ResponseSchema? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class ResponseSchema(
    val type: String,
    val description: String? = null,
    val properties: Map<String, ResponseSchema>? = null,
    val required: List<String>? = null,
    val items: ResponseSchema? = null
)

// Response models
@JsonClass(generateAdapter = true)
data class GenerateContentResponse(val candidates: List<Candidate>?)

@JsonClass(generateAdapter = true)
data class Candidate(val content: Content?)

// Extracted habit
@JsonClass(generateAdapter = true)
data class ExtractedHabit(
    val title: String,
    val frequency: String,
    val activeDays: List<String>,
    val targetTime: String,
    val behavioralTip: String
)
```

### 1.5 Error Handling

| Error Type | Behavior |
|---|---|
| API key missing/placeholder | Falls back to local regex parser; shows warning banner |
| Network timeout (30s) | Falls back to local regex parser; logs error |
| API returns empty response | Throws Exception → IntakeState.Error |
| JSON parsing fails | Throws Exception → IntakeState.Error |
| API quota exceeded | Falls back to local regex parser |

### 1.6 Retrofit Configuration

```kotlin
interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    // OkHttp: 30s connect/read/write timeouts
    // Moshi with KotlinJsonAdapterFactory
}
```

### 1.7 Local Fallback Parser

When Gemini is unavailable, the app uses regex-based local parsing:

- **Day matching:** Scans for full day names or 3-letter abbreviations (mon, tue, etc.)
- **Time parsing:** Regex `(\d{1,2})\s*:?(\d{2})?\s*(am|pm)?` with AM/PM conversion
- **Title extraction:** Strips time/day words, capitalizes first letter
- **Frequency:** "Weekly" if specific days matched or "weekly" in text; "Daily" otherwise
- **Default time:** 08:00 if no time found

---

## Future Public API Design

This section documents the planned REST API for future cloud sync functionality.

### 2.1 Base URL

```
https://api.behaviorcoach.example.com/v1
```

### 2.2 Authentication

All endpoints require a Bearer token:
```
Authorization: Bearer <jwt_token>
```

### 2.3 Endpoints

#### Habits

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/habits` | List all habits for authenticated user |
| `POST` | `/habits` | Create a new habit |
| `GET` | `/habits/{id}` | Get a specific habit |
| `PUT` | `/habits/{id}` | Update a habit |
| `DELETE` | `/habits/{id}` | Delete a habit |
| `GET` | `/habits/{id}/logs` | Get completion logs for a habit |

#### Completion Logs

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/habits/{id}/complete` | Mark habit complete for a date |
| `DELETE` | `/habits/{id}/complete/{date}` | Remove completion for a date |

#### User Profile

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/profile` | Get user profile |
| `PUT` | `/profile` | Update user profile |

#### Coach

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/coach/recommendations` | Get current coach recommendations |
| `POST` | `/coach/recommendations/{id}/apply` | Apply a recommendation |

### 2.4 Request/Response Examples

**Create Habit:**
```http
POST /v1/habits
Authorization: Bearer eyJhbG...
Content-Type: application/json

{
  "title": "Morning Meditation",
  "frequency": "Daily",
  "activeDays": ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"],
  "targetTime": "07:00",
  "behavioralTip": "Meditate immediately after brushing your teeth."
}
```

**Response (201 Created):**
```json
{
  "id": 42,
  "title": "Morning Meditation",
  "frequency": "Daily",
  "activeDays": ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"],
  "targetTime": "07:00",
  "behavioralTip": "Meditate immediately after brushing your teeth.",
  "createdAt": "2026-06-27T10:30:00Z",
  "isArchived": false
}
```

**Mark Complete:**
```http
POST /v1/habits/42/complete
Authorization: Bearer eyJhbG...
Content-Type: application/json

{ "date": "2026-06-27" }
```

**Response (200 OK):**
```json
{
  "id": 1042,
  "habitId": 42,
  "date": "2026-06-27",
  "completedAt": "2026-06-27T07:15:00Z"
}
```

### 2.5 Error Responses

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "targetTime must be in HH:MM format",
    "field": "targetTime"
  }
}
```

| HTTP Status | Code | Description |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Invalid request data |
| 401 | `UNAUTHORIZED` | Missing or invalid token |
| 403 | `FORBIDDEN` | Insufficient permissions |
| 404 | `NOT_FOUND` | Resource not found |
| 409 | `CONFLICT` | Duplicate completion for date |
| 429 | `RATE_LIMITED` | Too many requests |
| 500 | `INTERNAL_ERROR` | Server error |

### 2.6 Rate Limiting

- 100 requests per minute per user
- 1000 requests per hour per user
- Headers: `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`

---

**Author:** K K K Ekanayake
