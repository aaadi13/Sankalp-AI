package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Profile
import com.example.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    viewModel: AppViewModel,
    profile: Profile
) {
    val loading by viewModel.plannerLoading.collectAsState()
    val apiError by viewModel.apiError.collectAsState()
    val todayTasks by viewModel.todayTasks.collectAsState()

    var examType by remember { mutableStateOf(profile.examType) }
    var examDate by remember { mutableStateOf(profile.examDate) }
    var availableHours by remember { mutableStateOf(profile.availableHours.toString()) }
    var subjects by remember { mutableStateOf(profile.subjects) }

    // Sync state locally if profile updates asynchronously
    LaunchedEffect(profile) {
        if (examType.isEmpty()) examType = profile.examType
        if (examDate.isEmpty()) examDate = profile.examDate
        if (subjects.isEmpty()) subjects = profile.subjects
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "AI Study Roadmap Planner",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Configure your target milestones. Gemini will craft a daily spaced retention study sequence.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Error message banner
        if (apiError != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error notification icon",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notice:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = apiError ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear error text", tint = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
        }

        // --- Inputs Panel ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Milestone Parameters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Dropdown simulation / selection for Indian Govt Exams
                    OutlinedTextField(
                        value = examType,
                        onValueChange = { examType = it },
                        label = { Text("Target Exam (e.g. UPSC, SSC CGL, RRB NTPC)") },
                        placeholder = { Text("UPSC Civil Services") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("planner_exam_input"),
                        leadingIcon = { Icon(imageVector = Icons.Default.School, contentDescription = "Exam type icon") }
                    )

                    OutlinedTextField(
                        value = examDate,
                        onValueChange = { examDate = it },
                        label = { Text("Target Exam Date (YYYY-MM-DD)") },
                        placeholder = { Text("2026-06-25") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("planner_date_input"),
                        leadingIcon = { Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Date icon") }
                    )

                    OutlinedTextField(
                        value = availableHours,
                        onValueChange = { availableHours = it },
                        label = { Text("Daily Study Hours Target") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("planner_hours_input"),
                        leadingIcon = { Icon(imageVector = Icons.Default.Timer, contentDescription = "Hours icon") }
                    )

                    OutlinedTextField(
                        value = subjects,
                        onValueChange = { subjects = it },
                        label = { Text("Focus Subjects (Comma separated)") },
                        placeholder = { Text("Polity, Economy, Current Affairs") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("planner_subjects_input"),
                        leadingIcon = { Icon(imageVector = Icons.Default.LibraryBooks, contentDescription = "Subjects list icon") }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            val hours = availableHours.toDoubleOrNull() ?: profile.availableHours
                            viewModel.saveProfileSettings(
                                examType = examType.ifBlank { "UPSC CSE" },
                                examDate = examDate.ifBlank { "2026-06-25" },
                                availableHours = hours,
                                subjects = subjects.ifBlank { "General Studies" }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("schedule_generate_submit"),
                        enabled = !loading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp).padding(end = 8.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text("Gemini is Planning...")
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Auto-planning")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate My Study Plan")
                        }
                    }
                }
            }
        }

        // --- Current Generated Output Task Summary ---
        item {
            Text(
                text = "Planned Daily Tasks Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (todayTasks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No study milestones specified. Hit 'Generate' above!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(todayTasks, key = { it.id }) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (task.taskType) {
                                "Revision" -> "🔄"
                                "Mock Test" -> "📝"
                                else -> "📖"
                            },
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                modifier = Modifier.padding(top = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = task.subject,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Estimated: ${task.estimatedHours} hrs",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
