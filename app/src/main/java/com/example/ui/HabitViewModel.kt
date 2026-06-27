package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed interface IntakeState {
    object Idle : IntakeState
    object Loading : IntakeState
    data class Success(val habit: ExtractedHabit) : IntakeState
    data class Error(val message: String) : IntakeState
}

data class UserProfile(
    val name: String,
    val email: String,
    val avatarUrl: String,
    val isAuthenticated: Boolean = false
)

data class CoachRecommendation(
    val habitId: Int,
    val habitTitle: String,
    val frictionReason: String, // e.g. "Missed 3 times on Mon mornings"
    val alternativeText: String, // e.g. "Shift time to 09:30 or break into smaller blocks"
    val actionType: ActionType,
    val payloadValue: String // targetTime shift value, or modified title
)

enum class ActionType {
    SHIFT_TIME,
    MICRO_INCREMENT
}

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val database = HabitDatabase.getDatabase(application)
    private val repository = HabitRepository(database.habitDao)

    // State flows
    val habits: StateFlow<List<Habit>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completionLogs: StateFlow<List<CompletionLog>> = repository.allCompletionLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _intakeState = MutableStateFlow<IntakeState>(IntakeState.Idle)
    val intakeState: StateFlow<IntakeState> = _intakeState.asStateFlow()

    private val _userProfile = MutableStateFlow(
        UserProfile("Kush Mi", "kushmi1428@gmail.com", "https://api.dicebear.com/7.x/bottts/svg?seed=kushmi", isAuthenticated = true)
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // Dynamic Coach Recommendations based on automated failure pattern detection
    val coachRecommendations: StateFlow<List<CoachRecommendation>> = combine(habits, completionLogs) { habitList, logList ->
        analyzeFrictionAndGenerateRecommendations(habitList, logList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Dates in focus (Last 7 days sequence for retroactive logging)
    val pastSevenDays: List<Pair<String, String>> = getPastSevenDaysList()

    fun signIn(email: String, name: String) {
        val seed = name.lowercase().replace(" ", "")
        _userProfile.value = UserProfile(
            name = name,
            email = email,
            avatarUrl = "https://api.dicebear.com/7.x/bottts/svg?seed=$seed",
            isAuthenticated = true
        )
    }

    fun signOut() {
        _userProfile.value = UserProfile("", "", "", isAuthenticated = false)
    }

    fun submitNaturalLanguageIntake(rawText: String) {
        if (rawText.isBlank()) return
        viewModelScope.launch {
            _intakeState.value = IntakeState.Loading
            try {
                val extracted = repository.parseHabitIntake(rawText)
                _intakeState.value = IntakeState.Success(extracted)
            } catch (e: Exception) {
                _intakeState.value = IntakeState.Error(e.localizedMessage ?: "Unknown parse error")
            }
        }
    }

    fun clearIntakeState() {
        _intakeState.value = IntakeState.Idle
    }

    fun saveExtractedHabit(extracted: ExtractedHabit) {
        viewModelScope.launch {
            val habit = Habit(
                title = extracted.title,
                frequency = extracted.frequency,
                activeDays = extracted.activeDays.joinToString(", "),
                targetTime = extracted.targetTime,
                behavioralTip = extracted.behavioralTip
            )
            repository.insertHabit(habit)
            _intakeState.value = IntakeState.Idle
        }
    }

    fun addManualHabit(title: String, targetTime: String, weekdaysStr: String, tip: String) {
        viewModelScope.launch {
            val habit = Habit(
                title = title,
                frequency = if (weekdaysStr.split(",").size >= 5) "Daily" else "Weekly",
                activeDays = weekdaysStr,
                targetTime = targetTime,
                behavioralTip = tip.ifBlank { "Tie this micro-step to an anchor routine." }
            )
            repository.insertHabit(habit)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    fun toggleCompletion(habitId: Int, dateString: String) {
        viewModelScope.launch {
            val logs = completionLogs.value
            val isCompleted = logs.any { it.habitId == habitId && it.dateString == dateString }
            if (isCompleted) {
                repository.removeCompletionLog(habitId, dateString)
            } else {
                repository.addCompletionLog(habitId, dateString)
            }
        }
    }

    fun applyRecommendation(recommendation: CoachRecommendation) {
        viewModelScope.launch {
            val targetHabit = habits.value.find { it.id == recommendation.habitId } ?: return@launch
            when (recommendation.actionType) {
                ActionType.SHIFT_TIME -> {
                    val updated = targetHabit.copy(
                        targetTime = recommendation.payloadValue,
                        behavioralTip = "Aligned to active/frictionless window: ${recommendation.payloadValue}"
                    )
                    repository.updateHabit(updated)
                }
                ActionType.MICRO_INCREMENT -> {
                    val updated = targetHabit.copy(
                        title = recommendation.payloadValue,
                        behavioralTip = "Reduced granularity to ease behavioral entry."
                    )
                    repository.updateHabit(updated)
                }
            }
        }
    }

    // Interactive helper: populates some dummy failure histories to let users immediately see the Analyzer & Coach in action
    fun injectDemoFailures() {
        viewModelScope.launch {
            // Let's verify if we have habits first, if not create one
            val currentHabits = habits.value
            val habitIdToUse = if (currentHabits.isEmpty()) {
                val demoHabitId = repository.insertHabit(
                    Habit(
                        title = "Morning Cardio Gym Session",
                        frequency = "Weekly",
                        activeDays = "Monday, Wednesday, Friday",
                        targetTime = "06:30",
                        behavioralTip = "Prepare your gym gear right next to your bed so you trip on it in the morning."
                    )
                )
                demoHabitId.toInt()
            } else {
                currentHabits.first().id
            }

            // We want to simulate failures. To do this, we don't complete it for active days in the past 7 days.
            // Let's delete any completion logs for this habit in the past 7 days:
            pastSevenDays.forEach { (dateStr, _) ->
                repository.removeCompletionLog(habitIdToUse, dateStr)
            }
            
            // To ensure the system immediately sees it as high-friction (consecutive misses on scheduled days),
            // let's do nothing! Since we removed all logs, the system will detect scheduled active days with zero completion.
        }
    }

    private fun analyzeFrictionAndGenerateRecommendations(
        habitList: List<Habit>,
        logList: List<CompletionLog>
    ): List<CoachRecommendation> {
        val recommendations = mutableListOf<CoachRecommendation>()
        
        habitList.forEach { habit ->
            // Let's evaluate past 7 days to see scheduled days vs completed days
            val scheduledDays = habit.activeDays.split(",").map { it.trim().lowercase(Locale.ROOT) }
            var consecutiveMisses = 0
            val missedWeekdays = mutableSetOf<String>()

            // Look at past 7 days (excluding today because today is still buildable/completable)
            // Skip the first element in pastSevenDays (which is index 0 = today)
            for (i in 1 until pastSevenDays.size) {
                val (dateStr, dayOfWeek) = pastSevenDays[i]
                val isScheduled = scheduledDays.contains(dayOfWeek.lowercase(Locale.ROOT))
                if (isScheduled) {
                    val isCompleted = logList.any { it.habitId == habit.id && it.dateString == dateStr }
                    if (!isCompleted) {
                        consecutiveMisses++
                        missedWeekdays.add(dayOfWeek)
                    }
                }
            }

            // If we missed 2 or more active days, let's trigger an AI Adaptive Coach optimization card!
            if (consecutiveMisses >= 2) {
                val daysString = missedWeekdays.joinToString(" & ")
                val timeParts = habit.targetTime.split(":")
                val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 8

                if (hour in 5..10) {
                    // Morning morning heavy friction
                    recommendations.add(
                        CoachRecommendation(
                            habitId = habit.id,
                            habitTitle = habit.title,
                            frictionReason = "Missed $consecutiveMisses mornings on Scheduled Days ($daysString)",
                            alternativeText = "Morning exhaustion detected. Shift start time forward to reduce circadian friction?",
                            actionType = ActionType.SHIFT_TIME,
                            payloadValue = "09:30"
                        )
                    )
                } else {
                    // General/Evening habit friction - suggest micro-incrementing the task burden
                    val microTitle = if (habit.title.contains("minute", ignoreCase = true) || habit.title.contains("min", ignoreCase = true)) {
                        habit.title.replace("\\d+".toRegex(), "5") // Scale down to 5 minutes
                    } else {
                        "Micro: ${habit.title} (5 mins)"
                    }
                    recommendations.add(
                        CoachRecommendation(
                            habitId = habit.id,
                            habitTitle = habit.title,
                            frictionReason = "Missed $consecutiveMisses times on Scheduled Days ($daysString)",
                            alternativeText = "Large cognitive barrier detected. Simplify target to micro-increments (5 mins) to build automated activation first?",
                            actionType = ActionType.MICRO_INCREMENT,
                            payloadValue = microTitle
                        )
                    )
                }
            }
        }
        return recommendations
    }

    private fun getPastSevenDaysList(): List<Pair<String, String>> {
        val list = mutableListOf<Pair<String, String>>()
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val sdfDay = SimpleDateFormat("EEEE", Locale.US) // Full day name: e.g. Monday
        
        val cal = Calendar.getInstance()
        for (i in 0 until 7) {
            val dateStr = sdfDate.format(cal.time)
            val dayName = sdfDay.format(cal.time)
            list.add(Pair(dateStr, dayName))
            cal.add(Calendar.DAY_OF_YEAR, -1) // Go backward
        }
        return list
    }
}
