package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.HabitDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-schedule all habit reminders after device reboot
            runBlocking {
                val database = HabitDatabase.getDatabase(context)
                val habits = database.habitDao.getAllHabits().first()
                AlarmScheduler.scheduleAllReminders(context, habits)
            }
        }
    }
}
