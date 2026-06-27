package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habits"
)
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val frequency: String, // e.g. "Daily" or "Weekly"
    val activeDays: String, // Comma-separated list: "Monday, Wednesday, Friday"
    val targetTime: String, // 24H: "08:00"
    val behavioralTip: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
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
