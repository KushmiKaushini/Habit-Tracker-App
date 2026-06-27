package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ScheduleType {
    DAILY,          // Every day
    WEEKLY,         // Specific days (Mon, Wed, Fri)
    INTERVAL,       // Every N days
    TIMES_PER_WEEK, // X times this week (flexible days)
    WEEKDAYS        // Mon-Fri only
}

@Entity(
    tableName = "habits"
)
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val frequency: String, // e.g. "Daily" or "Weekly" (legacy, kept for compat)
    val activeDays: String, // Comma-separated list: "Monday, Wednesday, Friday"
    val targetTime: String, // 24H: "08:00"
    val behavioralTip: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    // New fields for flexible scheduling
    val scheduleType: String = ScheduleType.WEEKLY.name,
    val intervalDays: Int = 1, // For INTERVAL type: every N days
    val timesPerWeek: Int = 0, // For TIMES_PER_WEEK type
    val reminderEnabled: Boolean = true,
    val reminderOffsetMinutes: Int = 0 // Minutes before targetTime to notify
)

@Entity(
    tableName = "completion_logs",
    indices = [
        Index(value = ["habitId", "dateString"], unique = true)
    ]
)
data class CompletionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val dateString: String // Standard calendar format: "YYYY-MM-DD"
)
