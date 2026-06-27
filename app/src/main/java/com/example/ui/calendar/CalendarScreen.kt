package com.example.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PrimaryTeal
import com.example.ui.components.TextLight
import com.example.ui.components.TextMuted
import java.text.SimpleDateFormat
import java.util.*

import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CalendarScreen(
    habits: List<com.example.data.Habit>,
    onDateSelected: (DayCompletion) -> Unit
) {
    val viewModel = viewModel<CalendarViewModel>()
    val calendarData by viewModel.calendarData.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()

    val monthName = SimpleDateFormat("MMMM yyyy", Locale.US).format(
        Calendar.getInstance().apply {
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.YEAR, selectedYear)
        }.time
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.previousMonth() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous", tint = PrimaryTeal)
            }
            Text(
                text = monthName,
                color = TextLight,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { viewModel.nextMonth() }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next", tint = PrimaryTeal)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Day headers
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(calendarData) { day ->
                DayCell(day = day, onClick = { onDateSelected(day) })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(color = Color(0xFF4CAF50), label = "All done")
            LegendItem(color = Color(0xFFFFC107), label = "Partial")
            LegendItem(color = Color(0xFFF44336), label = "Missed")
            LegendItem(color = Color(0xFF9E9E9E), label = "No habits")
        }
    }
}

@Composable
fun DayCell(day: DayCompletion, onClick: () -> Unit) {
    val backgroundColor = when (day.status) {
        DayStatus.ALL_DONE -> Color(0xFF4CAF50)
        DayStatus.PARTIAL -> Color(0xFFFFC107)
        DayStatus.NONE_DONE -> Color(0xFFF44336)
        DayStatus.NO_HABITS -> Color(0xFF9E9E9E).copy(alpha = 0.3f)
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(backgroundColor.copy(alpha = 0.2f))
            .then(
                if (day.isToday) Modifier.border(2.dp, PrimaryTeal, CircleShape)
                else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.dayNumber.toString(),
                color = if (day.isToday) PrimaryTeal else TextLight,
                fontSize = 12.sp,
                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
            )
            if (day.totalHabits > 0) {
                Text(
                    text = "${day.completedHabits}/${day.totalHabits}",
                    color = TextMuted,
                    fontSize = 8.sp
                )
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, color = TextMuted, fontSize = 10.sp)
    }
}
