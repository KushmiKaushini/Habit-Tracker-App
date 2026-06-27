package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.HabitViewModel
import com.example.ui.IntakeState
import com.example.ui.CoachRecommendation
import com.example.ui.ActionType
import com.example.ui.components.*
import com.example.data.Habit
import com.example.data.CompletionLog
import com.example.data.ExtractedHabit
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = PrimaryTeal,
                    secondary = ActiveBlue,
                    background = CarbonBackground,
                    surface = CharcoalSurface,
                    onBackground = TextLight,
                    onSurface = TextLight
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CarbonBackground
                ) {
                    CoachAppContainer()
                }
            }
        }
    }
}

@Composable
fun CoachAppContainer(viewModel: HabitViewModel = viewModel()) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    
    // Intake NLP fields
    var nlpInputText by remember { mutableStateOf("") }
    
    // Manual intake dialog
    var showManualDialog by remember { mutableStateOf(false) }

    // State bindings
    val habits by viewModel.habits.collectAsState()
    val logs by viewModel.completionLogs.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val recommendations by viewModel.coachRecommendations.collectAsState()
    val intakeState by viewModel.intakeState.collectAsState()

    // Resilient Warning Alert configuration
    val isApiKeyPresent = BuildConfig.GEMINI_API_KEY.isNotEmpty() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        bottomBar = {
            NavigationBar(
                containerColor = CharcoalSurface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Daily Tracker") },
                    label = { Text("Daily Coach") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ActiveBlue,
                        selectedTextColor = PrimaryTeal,
                        indicatorColor = SoftCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Add, contentDescription = "AI Routine Intake") },
                    label = { Text("AI Intake") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ActiveBlue,
                        selectedTextColor = PrimaryTeal,
                        indicatorColor = SoftCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CarbonBackground)
                .padding(innerPadding)
        ) {
            // App Greeting Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CustomAvatar(name = profile.name)
                    Column {
                        Text(
                            text = "Hello, ${profile.name.ifBlank { "User" }}",
                            color = TextLight,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "AI-Driven Behavior Coach",
                            color = PrimaryTeal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Quick Action: Inject Past Misses to test Adaptive Coach easily!
                    IconButton(
                        onClick = {
                            viewModel.injectDemoFailures()
                            Toast.makeText(context, "Injected historical pattern drops to test Coach analyzer!", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier
                            .background(LightSurface, RoundedCornerShape(10.dp))
                            .size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Simulate Friction Window",
                            tint = HighFrictionOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { showManualDialog = true },
                        modifier = Modifier
                            .background(LightSurface, RoundedCornerShape(10.dp))
                            .size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Create,
                            contentDescription = "Manual setup",
                            tint = PrimaryTeal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Warning Notice regarding APK decryption
            if (!isApiKeyPresent) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x11FF9100)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Notice",
                            tint = HighFrictionOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "No custom API Key configured in AI Studio Secrets panel. Operating in local failsafe parser.",
                            color = HighFrictionOrange,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            // Screen Switch based on Bottom Nav
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (selectedTab == 0) {
                    DailyTrackerScreen(
                        habits = habits,
                        logs = logs,
                        recommendations = recommendations,
                        pastSevenDays = viewModel.pastSevenDays,
                        onToggleCompletion = { habitId, dateStr ->
                            viewModel.toggleCompletion(habitId, dateStr)
                        },
                        onDeleteHabit = { habit ->
                            viewModel.deleteHabit(habit)
                        },
                        onApplyRecommendation = { rec ->
                            viewModel.applyRecommendation(rec)
                            Toast.makeText(context, "Routine updated instantly!", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    IntakeScreen(
                        inputText = nlpInputText,
                        onValueChange = { nlpInputText = it },
                        intakeState = intakeState,
                        onSubmitIntake = {
                            viewModel.submitNaturalLanguageIntake(nlpInputText)
                        },
                        onSaveHabit = { extracted ->
                            viewModel.saveExtractedHabit(extracted)
                            nlpInputText = ""
                            selectedTab = 0 // return back
                            Toast.makeText(context, "Added atomic habit routine!", Toast.LENGTH_LONG).show()
                        },
                        onCancelParse = {
                            viewModel.clearIntakeState()
                        }
                    )
                }
            }
        }
    }

    if (showManualDialog) {
        ManualHabitDialog(
            onDismiss = { showManualDialog = false },
            onConfirmAdd = { title, targetTime, weekdays, tip ->
                viewModel.addManualHabit(title, targetTime, weekdays, tip)
                showManualDialog = false
                Toast.makeText(context, "Added habit!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun DailyTrackerScreen(
    habits: List<Habit>,
    logs: List<CompletionLog>,
    recommendations: List<CoachRecommendation>,
    pastSevenDays: List<Pair<String, String>>,
    onToggleCompletion: (Int, String) -> Unit,
    onDeleteHabit: (Habit) -> Unit,
    onApplyRecommendation: (CoachRecommendation) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Coach Recommendations (Interactive adaptive coaching)
        if (recommendations.isNotEmpty()) {
            item {
                Text(
                    text = "Adaptive Coach Actions",
                    color = PrimaryTeal,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            items(recommendations) { rec ->
                GradientCard(
                    borderBrush = Brush.linearGradient(listOf(HighFrictionOrange, Color.Yellow))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Coach Insights",
                            tint = HighFrictionOrange,
                            modifier = Modifier
                                .size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Friction Drop in: ${rec.habitTitle}",
                                color = TextLight,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = rec.frictionReason,
                                color = HighFrictionOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = rec.alternativeText,
                                color = TextMuted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = { onApplyRecommendation(rec) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (rec.actionType == ActionType.SHIFT_TIME) "Shift schedule to ${rec.payloadValue}" else "Scale down to Micro-Action",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Active Tasks section
        item {
            Text(
                text = "My Atomic Routines",
                color = TextLight,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (habits.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Empty",
                            tint = PrimaryTeal,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No routines yet",
                            color = TextLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Type conversational habits in the 'AI Intake' tab (or tap + manual) to begin!",
                            color = TextMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(habits) { habit ->
                HabitItemCard(
                    habit = habit,
                    logs = logs,
                    pastSevenDays = pastSevenDays,
                    onToggleCompletion = onToggleCompletion,
                    onDeleteHabit = onDeleteHabit
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitItemCard(
    habit: Habit,
    logs: List<CompletionLog>,
    pastSevenDays: List<Pair<String, String>>,
    onToggleCompletion: (Int, String) -> Unit,
    onDeleteHabit: (Habit) -> Unit
) {
    var expandedTip by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { expandedTip = !expandedTip },
                onLongClick = { onDeleteHabit(habit) }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.title,
                        color = TextLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Time",
                                tint = PrimaryTeal,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = habit.targetTime,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                        
                        StatusBadge(
                            text = habit.frequency,
                            color = if (habit.frequency == "Daily") PrimaryTeal else ActiveBlue
                        )
                    }
                }
                
                IconButton(
                    onClick = { onDeleteHabit(habit) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = TextMuted.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scheduled Weekday target indicator
            val activeWeekdays = habit.activeDays.split(",").map { it.trim().lowercase(Locale.ROOT) }
            Text(
                text = "Target Schedule: " + habit.activeDays,
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Linear Interactive Calendar Row for checklist toggle (Safeguards duplicate entries via Room uniqueness index)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Reverse list of pastSevenDays to display historically chronologically left-to-right (from 6 days ago -> today)
                pastSevenDays.asReversed().forEach { (dateStr, dayOfWeek) ->
                    val dayInitial = dayOfWeek.take(3)
                    val isScheduled = activeWeekdays.contains(dayOfWeek.lowercase(Locale.ROOT))
                    val isDone = logs.any { it.habitId == habit.id && it.dateString == dateStr }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onToggleCompletion(habit.id, dateStr) }
                    ) {
                        Text(
                            text = dayInitial,
                            color = if (isScheduled) TextLight else TextMuted.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            fontWeight = if (isScheduled) FontWeight.Bold else FontWeight.Normal
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isDone -> PrimaryTeal
                                        isScheduled -> LightSurface
                                        else -> LightSurface.copy(alpha = 0.3f)
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = when {
                                        isDone -> Color.Transparent
                                        isScheduled -> PrimaryTeal.copy(alpha = 0.4f)
                                        else -> Color.Transparent
                                    },
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else if (isScheduled) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryTeal.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }
            }

            // Expandable Behavioral Tip
            if (habit.behavioralTip.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(LightSurface)
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Tip",
                            tint = PrimaryTeal,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "AI Behavioral Strategy",
                            color = PrimaryTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = habit.behavioralTip,
                        color = TextLight,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun IntakeScreen(
    inputText: String,
    onValueChange: (String) -> Unit,
    intakeState: IntakeState,
    onSubmitIntake: () -> Unit,
    onSaveHabit: (ExtractedHabit) -> Unit,
    onCancelParse: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "AI Conversational Intake",
            color = TextLight,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Type your desired routine plan naturally. The behavior analyzer will extract schedule metadata, establish relational triggers, and generate habit strategies.",
            color = TextMuted,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )

        OutlinedTextField(
            value = inputText,
            onValueChange = onValueChange,
            placeholder = { Text("e.g. Read a finance book for 20 minutes every Monday and Wednesday night at 10 PM") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .testTag("nlp_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryTeal,
                unfocusedBorderColor = LightSurface,
                focusedContainerColor = CharcoalSurface,
                unfocusedContainerColor = CharcoalSurface,
                focusedTextColor = TextLight,
                unfocusedTextColor = TextLight,
                focusedLabelColor = PrimaryTeal,
                unfocusedPlaceholderColor = TextMuted.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = onSubmitIntake,
            enabled = inputText.isNotBlank() && intakeState != IntakeState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("submit_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryTeal,
                disabledContainerColor = LightSurface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Analyze Blueprint with AI",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // State Machine rendering
        when (intakeState) {
            is IntakeState.Loading -> {
                AnimatedProcessingLoader(
                    tipText = "Friction is reduced by anchoring new habits on top of stable existing routines (anchoring behavior)."
                )
            }
            is IntakeState.Success -> {
                val extracted = intakeState.habit
                GradientCard(
                    borderBrush = Brush.linearGradient(listOf(PrimaryTeal, ActiveBlue))
                ) {
                    Text(
                        text = "Extracted Habit Blueprint",
                        color = PrimaryTeal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Title: ${extracted.title}",
                        color = TextLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Schedule: ${extracted.frequency} on ${extracted.activeDays.joinToString(", ")} at ${extracted.targetTime}",
                        color = TextLight,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Display behavioral tip card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(LightSurface)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "AI Behavioral Tip (Cognitive Hook)",
                            color = PrimaryTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = extracted.behavioralTip,
                            color = TextLight,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCancelParse,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextLight),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Discard", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onSaveHabit(extracted) },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Deploy Routines", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            is IntakeState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0x33FF1744))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Extraction Failure",
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = intakeState.message,
                            color = TextLight,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun ManualHabitDialog(
    onDismiss: () -> Unit,
    onConfirmAdd: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetTime by remember { mutableStateOf("08:00") }
    // All days by default
    var weekdays by remember { mutableStateOf("Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday") }
    var tip by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manual Habit Entry", color = PrimaryTeal) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Habit Name") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryTeal)
                )

                OutlinedTextField(
                    value = targetTime,
                    onValueChange = { targetTime = it },
                    label = { Text("Target Clock Time (HH:MM)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryTeal)
                )

                OutlinedTextField(
                    value = weekdays,
                    onValueChange = { weekdays = it },
                    label = { Text("Active Days (comma separated)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryTeal)
                )

                OutlinedTextField(
                    value = tip,
                    onValueChange = { tip = it },
                    placeholder = { Text("Optional: Custom strategy tip...") },
                    label = { Text("Action Tip") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryTeal)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirmAdd(title, targetTime, weekdays, tip)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
            ) {
                Text("Confirm", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextLight)
            }
        },
        containerColor = CharcoalSurface
    )
}
