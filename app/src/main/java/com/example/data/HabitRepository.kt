package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HabitRepository(private val habitDao: HabitDao) {

    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()
    val allCompletionLogs: Flow<List<CompletionLog>> = habitDao.getAllCompletionLogs()

    suspend fun insertHabit(habit: Habit): Long = withContext(Dispatchers.IO) {
        habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: Habit) = withContext(Dispatchers.IO) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habit: Habit) = withContext(Dispatchers.IO) {
        habitDao.deleteHabit(habit)
    }

    suspend fun addCompletionLog(habitId: Int, dateString: String) = withContext(Dispatchers.IO) {
        val log = CompletionLog(habitId = habitId, dateString = dateString)
        habitDao.insertCompletionLog(log)
    }

    suspend fun removeCompletionLog(habitId: Int, dateString: String) = withContext(Dispatchers.IO) {
        habitDao.deleteCompletionLog(habitId, dateString)
    }

    /**
     * Calls Gemini API to parse natural language habit intake,
     * or uses local regex parser if key is absent or request fails.
     */
    suspend fun parseHabitIntake(rawText: String): ExtractedHabit = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w("HabitRepository", "Gemini API key is missing or is the default placeholder. Falling back to local parser.")
            return@withContext parseLocalFallback(rawText, "Gemini API Key is in placeholder state. Set your key in the AI Studio Secrets panel!")
        }

        val prompt = """
            Extract structured habit information from this conversational phrase:
            "$rawText"
            
            Format response as JSON with fields:
            - title (string, capitalized, concise)
            - frequency (string, restricted to: "Daily" or "Weekly")
            - activeDays (array of strings, e.g. ["Monday", "Wednesday"] or all 7 days if daily)
            - targetTime (string, formatted strictly as 24-hour "HH:MM", e.g. "07:30" or "21:00")
            - behavioralTip (string, high-quality, actionable, behavioral psychology tip matching the specific routine to reduce friction)
        """.trimIndent()

        // Construct Schema
        val schema = ResponseSchema(
            type = "OBJECT",
            properties = mapOf(
                "title" to ResponseSchema(type = "STRING", description = "The short title of the habit activity"),
                "frequency" to ResponseSchema(type = "STRING", description = "Daily or Weekly"),
                "activeDays" to ResponseSchema(type = "ARRAY", items = ResponseSchema(type = "STRING")),
                "targetTime" to ResponseSchema(type = "STRING", description = "The time of day in 24-hour HH:MM format"),
                "behavioralTip" to ResponseSchema(type = "STRING", description = "A behavioral optimization/tip to maintain the streak")
            ),
            required = listOf("title", "frequency", "activeDays", "targetTime", "behavioralTip")
        )

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                responseSchema = schema,
                temperature = 0.1f
            ),
            systemInstruction = Content(
                parts = listOf(
                    Part(text = "You are an elite behavior design coach. You extract precise habit entities from conversational natural language strings and output scientifically tailored behavioral tips.")
                )
            )
        )

        try {
            val response = RetrofitClient.apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("Empty response from AI engine")
            
            Log.d("HabitRepository", "Gemini parsed response: $jsonText")
            val adapter = RetrofitClient.moshiParser.adapter(ExtractedHabit::class.java)
            val extracted = adapter.fromJson(jsonText)
                ?: throw Exception("JSON conversion returned null")
            extracted
        } catch (e: Exception) {
            Log.e("HabitRepository", "Failed calling Gemini API, falling back to local parsing.", e)
            parseLocalFallback(rawText, "Local parser active (Gemini call failed: ${e.localizedMessage ?: "Unknown error"}).")
        }
    }

    private fun parseLocalFallback(rawText: String, statusMessage: String): ExtractedHabit {
        val lowercase = rawText.lowercase(Locale.ROOT)
        
        // Match days
        val allDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val matchedDays = mutableListOf<String>()
        
        for (day in allDays) {
            val dayLower = day.lowercase(Locale.ROOT)
            if (lowercase.contains(dayLower) || lowercase.contains(dayLower.substring(0, 3))) {
                matchedDays.add(day)
            }
        }

        val frequency = if (lowercase.contains("weekly") || matchedDays.isNotEmpty() && !lowercase.contains("daily")) {
            "Weekly"
        } else {
            "Daily"
        }

        // If Daily or no matched days, default to all days
        if (frequency == "Daily" || matchedDays.isEmpty()) {
            matchedDays.clear()
            matchedDays.addAll(allDays)
        }

        // Parse Time: e.g. "9pm", "7 am", "18:30"
        var targetTime = "08:00"
        val timeRegex = "(\\d{1,2})\\s*(:?\\d{2})?\\s*(am|pm)?".toRegex()
        val match = timeRegex.find(lowercase)
        if (match != null) {
            val hourStr = match.groupValues[1]
            val minStr = match.groupValues[2]?.replace(":", "") ?: "00"
            val amPm = match.groupValues[3]
            var hour = hourStr.toIntOrNull() ?: 8
            
            if (amPm == "pm" && hour < 12) hour += 12
            if (amPm == "am" && hour == 12) hour = 0
            
            targetTime = String.format(Locale.US, "%02d:%02d", hour, minStr.toIntOrNull() ?: 0)
        }

        // Clean up title
        var title = rawText.replace(timeRegex, "")
            .replace("every monday|tuesday|wednesday|thursday|friday|saturday|sunday|daily|weekly".toRegex(), "")
            .replace("at\\s*$".toRegex(), "")
            .trim()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

        if (title.isEmpty()) {
            title = "New Custom Habit"
        }

        val behavioralTip = "Atomic step: Tie '$title' directly to an existing anchor routine. $statusMessage"

        return ExtractedHabit(
            title = title,
            frequency = frequency,
            activeDays = matchedDays,
            targetTime = targetTime,
            behavioralTip = behavioralTip
        )
    }
}
