package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.HabitDatabase
import com.example.data.CompletionLog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*

class HabitWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_TOGGLE_HABIT) {
            val habitId = intent.getIntExtra(EXTRA_HABIT_ID, -1)
            if (habitId == -1) return

            runBlocking {
                val database = HabitDatabase.getDatabase(context)
                val dao = database.habitDao
                val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                val logs = dao.getAllCompletionLogs().first()
                val isCompleted = logs.any { it.habitId == habitId && it.dateString == todayDate }

                if (isCompleted) {
                    dao.deleteCompletionLog(habitId, todayDate)
                } else {
                    dao.insertCompletionLog(CompletionLog(habitId = habitId, dateString = todayDate))
                }
            }

            // Refresh all widgets after toggle
            refreshAllWidgets(context)
        }
    }

    override fun onEnabled(context: Context) {
        // First widget created
    }

    override fun onDisabled(context: Context) {
        // Last widget removed
    }

    companion object {
        const val ACTION_TOGGLE_HABIT = "com.example.ACTION_TOGGLE_HABIT"
        const val EXTRA_HABIT_ID = "habit_id"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.habit_widget)

            // Set up the intent to launch the app when widget title is tapped
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_title, pendingIntent)

            // Set up the list adapter
            val serviceIntent = Intent(context, HabitWidgetService::class.java)
            views.setRemoteAdapter(R.id.widget_list, serviceIntent)

            // Set up the pending intent template for list item clicks (toggle)
            val toggleIntent = Intent(context, HabitWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_HABIT
            }
            val togglePendingIntent = PendingIntent.getBroadcast(
                context, 0, toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setPendingIntentTemplate(R.id.widget_list, togglePendingIntent)

            views.setEmptyView(R.id.widget_list, R.id.widget_empty)

            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list)
        }

        fun refreshAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = android.content.ComponentName(context, HabitWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}
