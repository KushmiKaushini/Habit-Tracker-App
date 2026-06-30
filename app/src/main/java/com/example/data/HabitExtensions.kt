package com.example.data

import java.util.Locale

/**
 * Checks if this habit is scheduled for the given day of week.
 * @param dayOfWeek Full day name e.g. "Monday", lowercase or mixed case accepted.
 */
fun Habit.isActiveOn(dayOfWeek: String): Boolean {
    val activeDays = this.activeDays.split(",").map { it.trim().lowercase(Locale.ROOT) }
    val target = dayOfWeek.lowercase(Locale.ROOT)
    return activeDays.any { it == target || it == target.substring(0, minOf(3, target.length)) }
}
