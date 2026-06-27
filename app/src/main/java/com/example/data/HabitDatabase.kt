package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Habit::class, CompletionLog::class], version = 2, exportSchema = false)
abstract class HabitDatabase : RoomDatabase() {
    abstract val habitDao: HabitDao

    companion object {
        @Volatile
        private var INSTANCE: HabitDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE habits ADD COLUMN scheduleType TEXT NOT NULL DEFAULT 'WEEKLY'")
                database.execSQL("ALTER TABLE habits ADD COLUMN intervalDays INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE habits ADD COLUMN timesPerWeek INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE habits ADD COLUMN reminderEnabled INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE habits ADD COLUMN reminderOffsetMinutes INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): HabitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HabitDatabase::class.java,
                    "behavior_coach_db"
                ).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
