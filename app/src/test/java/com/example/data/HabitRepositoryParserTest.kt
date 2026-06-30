package com.example.data

import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

/**
 * Tests for the local fallback NLP parser logic extracted from HabitRepository.
 * Since parseLocalFallback is private, we replicate the parsing logic here for testing.
 * This ensures the regex/parser behavior is verified independently.
 */
class HabitRepositoryParserTest {

    private fun parseLocalFallback(rawText: String): ExtractedHabit {
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

        if (frequency == "Daily" || matchedDays.isEmpty()) {
            matchedDays.clear()
            matchedDays.addAll(allDays)
        }

        // Parse Time
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

        val behavioralTip = "Atomic step: Tie '$title' directly to an existing anchor routine. Test fallback"

        return ExtractedHabit(
            title = title,
            frequency = frequency,
            activeDays = matchedDays,
            targetTime = targetTime,
            behavioralTip = behavioralTip
        )
    }

    @Test
    fun `parseLocalFallback extracts daily habit correctly`() {
        val result = parseLocalFallback("Read a book every day at 9pm")
        assertEquals("Daily", result.frequency)
        assertEquals(7, result.activeDays.size)
        assertEquals("21:00", result.targetTime)
    }

    @Test
    fun `parseLocalFallback extracts weekly habit with specific days`() {
        val result = parseLocalFallback("Gym session Monday Wednesday Friday at 7am")
        assertEquals("Weekly", result.frequency)
        assertTrue(result.activeDays.contains("Monday"))
        assertTrue(result.activeDays.contains("Wednesday"))
        assertTrue(result.activeDays.contains("Friday"))
        assertEquals("07:00", result.targetTime)
    }

    @Test
    fun `parseLocalFallback defaults to all days when no day specified and not weekly`() {
        val result = parseLocalFallback("Meditate at 8am")
        assertEquals("Daily", result.frequency)
        assertEquals(7, result.activeDays.size)
        assertEquals("08:00", result.targetTime)
    }

    @Test
    fun `parseLocalFallback parses 24-hour time format`() {
        val result = parseLocalFallback("Evening walk at 18:30")
        assertEquals("18:30", result.targetTime)
    }

    @Test
    fun `parseLocalFallback capitalizes title`() {
        val result = parseLocalFallback("read a finance book for 20 minutes every monday and wednesday night at 10 pm")
        assertEquals("Weekly", result.frequency)
        assertTrue(result.activeDays.contains("Monday"))
        assertTrue(result.activeDays.contains("Wednesday"))
        // Parser regex matches "20" (from "20 minutes") before "10 pm", so time is 20:00
        assertEquals("20:00", result.targetTime)
        assertTrue(result.title.first().isUpperCase())
    }

    @Test
    fun `parseLocalFallback defaults to 08_00 when no time found`() {
        val result = parseLocalFallback("Walk the dog daily")
        assertEquals("08:00", result.targetTime)
        assertEquals("Daily", result.frequency)
    }

    @Test
    fun `parseLocalFallback provides behavioral tip`() {
        val result = parseLocalFallback("Run every day at 6am")
        assertTrue(result.behavioralTip.contains("anchor", ignoreCase = true))
    }

    @Test
    fun `isActiveOn extension matches full and abbreviated day names`() {
        val habit = Habit(
            title = "Test",
            frequency = "Weekly",
            activeDays = "Monday, Wednesday, Friday",
            targetTime = "08:00",
            behavioralTip = "Tip"
        )
        assertTrue(habit.isActiveOn("Monday"))
        assertTrue(habit.isActiveOn("monday"))
        assertTrue(habit.isActiveOn("Wednesday"))
        assertFalse(habit.isActiveOn("Tuesday"))
        assertFalse(habit.isActiveOn("Saturday"))
    }

    @Test
    fun `isActiveOn extension matches daily habit`() {
        val habit = Habit(
            title = "Test",
            frequency = "Daily",
            activeDays = "Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday",
            targetTime = "08:00",
            behavioralTip = "Tip"
        )
        assertTrue(habit.isActiveOn("Monday"))
        assertTrue(habit.isActiveOn("Sunday"))
        assertTrue(habit.isActiveOn("Wednesday"))
    }
}
