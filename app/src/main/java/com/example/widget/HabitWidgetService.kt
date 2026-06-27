package com.example.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.R
import com.example.data.HabitDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*

class HabitWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return HabitWidgetItemFactory(applicationContext)
    }
}

class HabitWidgetItemFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private var habits: List<HabitItem> = emptyList()

    data class HabitItem(val id: Int, val title: String, val isCompleted: Boolean)

    override fun onCreate() {}

    override fun onDataSetChanged() {
        habits = runBlocking {
            val database = HabitDatabase.getDatabase(context)
            val dao = database.habitDao
            val allHabits = dao.getAllHabits().first()
            val today = SimpleDateFormat("EEEE", Locale.US).format(Date())
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val logs = dao.getAllCompletionLogs().first()

            allHabits.filter { habit ->
                !habit.isArchived && habit.activeDays.split(",").any {
                    it.trim().lowercase(Locale.ROOT) == today.lowercase(Locale.ROOT) ||
                    it.trim().lowercase(Locale.ROOT) == today.lowercase(Locale.ROOT).substring(0, 3)
                }
            }.map { habit ->
                HabitItem(
                    id = habit.id,
                    title = habit.title,
                    isCompleted = logs.any { it.habitId == habit.id && it.dateString == todayDate }
                )
            }
        }
    }

    override fun onDestroy() { habits = emptyList() }

    override fun getCount(): Int = habits.size

    override fun getViewAt(position: Int): RemoteViews {
        val item = habits[position]
        return RemoteViews(context.packageName, R.layout.habit_widget_item).apply {
            setTextViewText(R.id.widget_habit_title, item.title)
            setInt(R.id.widget_habit_checkbox, "setChecked", if (item.isCompleted) 1 else 0)

            // Set click intent for checkbox toggle
            val fillInIntent = Intent().apply {
                putExtra("habit_id", item.id)
                putExtra("action", "toggle")
            }
            setOnClickFillInIntent(R.id.widget_habit_checkbox, fillInIntent)
        }
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = habits[position].id.toLong()
    override fun hasStableIds(): Boolean = true
}
