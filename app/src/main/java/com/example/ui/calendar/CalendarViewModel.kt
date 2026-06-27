package com.example.ui.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Habit
import com.example.data.HabitDatabase
import com.example.data.HabitRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class DayCompletion(
    val date: String,
    val dayNumber: Int,
    val isCurrentMonth: Boolean,
    val totalHabits: Int,
    val completedHabits: Int,
    val isToday: Boolean = false
) {
    val completionRate: Float get() = if (totalHabits > 0) completedHabits.toFloat() / totalHabits else 0f
    val status: DayStatus get() = when {
        totalHabits == 0 -> DayStatus.NO_HABITS
        completionRate >= 1f -> DayStatus.ALL_DONE
        completionRate > 0f -> DayStatus.PARTIAL
        else -> DayStatus.NONE_DONE
    }
}

enum class DayStatus {
    NO_HABITS, ALL_DONE, PARTIAL, NONE_DONE
}

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val database = HabitDatabase.getDatabase(application)
    private val repository = HabitRepository(database.habitDao)

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    val habits: StateFlow<List<Habit>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    val calendarData: StateFlow<List<DayCompletion>> = combine(
        habits, _selectedMonth, _selectedYear
    ) { habitList, month, year ->
        generateCalendarData(habitList, month, year)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun generateCalendarData(habits: List<Habit>, month: Int, year: Int): List<DayCompletion> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val today = sdf.format(Date())
        val activeHabits = habits.filter { !it.isArchived }
        val logMap = mutableMapOf<String, MutableSet<Int>>()

        return (1..daysInMonth).map { day ->
            cal.set(Calendar.DAY_OF_MONTH, day)
            val dateStr = sdf.format(cal.time)
            val dayOfWeek = SimpleDateFormat("EEEE", Locale.US).format(cal.time)
            val scheduledCount = activeHabits.count { isScheduledForDay(it, dayOfWeek) }
            val completedCount = logMap[dateStr]?.size ?: 0

            DayCompletion(
                date = dateStr,
                dayNumber = day,
                isCurrentMonth = true,
                totalHabits = scheduledCount,
                completedHabits = completedCount,
                isToday = dateStr == today
            )
        }
    }

    private fun isScheduledForDay(habit: Habit, dayOfWeek: String): Boolean {
        val activeDays = habit.activeDays.split(",").map { it.trim().lowercase(Locale.ROOT) }
        return activeDays.any { it == dayOfWeek.lowercase(Locale.ROOT) || it == dayOfWeek.lowercase(Locale.ROOT).substring(0, 3) }
    }

    fun nextMonth() {
        if (_selectedMonth.value == 11) {
            _selectedMonth.value = 0
            _selectedYear.value = _selectedYear.value + 1
        } else {
            _selectedMonth.value = _selectedMonth.value + 1
        }
    }

    fun previousMonth() {
        if (_selectedMonth.value == 0) {
            _selectedMonth.value = 11
            _selectedYear.value = _selectedYear.value - 1
        } else {
            _selectedMonth.value = _selectedMonth.value - 1
        }
    }
}
