package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY id DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("SELECT * FROM completion_logs")
    fun getAllCompletionLogs(): Flow<List<CompletionLog>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCompletionLog(log: CompletionLog): Long

    @Query("DELETE FROM completion_logs WHERE habitId = :habitId AND dateString = :dateString")
    suspend fun deleteCompletionLog(habitId: Int, dateString: String)
}
