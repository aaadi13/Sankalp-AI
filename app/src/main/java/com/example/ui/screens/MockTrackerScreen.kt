package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MockScore
import com.example.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MockTrackerScreen(
    viewModel: AppViewModel
) {
    val scores by viewModel.mockScores.collectAsState()

    var showAddScoreDialog by remember { mutableStateOf(false) }
    var testTitle by remember { mutableStateOf("") }
    var rawScore by remember { mutableStateOf("") }
    var rawMaxScore by remember { mutableStateOf("") }
    var rawSubject by remember { mutableStateOf("General Studies") }
    var rawWeakAreas by remember { mutableStateOf("") } // Comma separated topics

    val subjectsList = remember {
        listOf("General Studies", "Indian Polity", "Quant & DI", "Logical Reasoning", "English/Verbal", "Current Affairs")
    }
    var expandedDropdown by remember { mutableStateOf(false) }

    // Aggregate Weak Areas
    val weakAreaFrequencies = remember(scores) {
        val frequencies = mutableMapOf<String, Int>()
        scores.forEach { scoreObj ->
            if (scoreObj.weakAreas.isNotBlank()) {
                scoreObj.weakAreas.split(",").forEach { topic ->
                    val clean = topic.trim().capitalize(Locale.getDefault())
                    if (clean.isNotBlank()) {
                        frequencies[clean] = (frequencies[clean] ?: 0) + 1
                    }
                }
            }
        }
        frequencies.entries.sortedByDescending { it.value }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mock Tracker & Performance Analytics",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Log offline/online practice scores. Monitor accuracy graphs & target recurrent weak areas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // --- Custom Canvas Progress Accuracy Graph ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Accuracy % Over Time",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Visualizing the percentage scored on your consecutive logged tests.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (scores.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Log your first Mock Test below to construct analytics graph!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val strokeColor = MaterialTheme.colorScheme.secondary
                        val gridLineColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

                        // Flip scores chronological order (oldest to newest) for plotting
                        val chronologicalScores = remember(scores) {
                            scores.sortedBy { it.date }
                        }

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            
                            val paddingLeft = 30.dp.toPx()
                            val paddingRight = 10.dp.toPx()
                            val paddingTop = 10.dp.toPx()
                            val paddingBottom = 20.dp.toPx()

                            val graphWidth = canvasWidth - paddingLeft - paddingRight
                            val graphHeight = canvasHeight - paddingTop - paddingBottom

                            // Draw Y-axis reference boundaries (100%, 75%, 50%, 25%, 0%)
                            val referenceValues = listOf(1.0f, 0.75f, 0.5f, 0.25f, 0f)
                            referenceValues.forEach { ratio ->
                                val y = paddingTop + graphHeight * (1.0f - ratio)
                                drawLine(
                                    color = gridLineColor,
                                    start = Offset(paddingLeft, y),
                                    end = Offset(canvasWidth - paddingRight, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            // Math coordinates plotting
                            if (chronologicalScores.size == 1) {
                                // Draw a single centered accuracy indicator point
                                val accuracy = chronologicalScores[0].score / chronologicalScores[0].totalMarks
                                val boundedAccuracy = accuracy.coerceIn(0.0, 1.0).toFloat()
                                val x = paddingLeft + graphWidth / 2f
                                val y = paddingTop + graphHeight * (1.0f - boundedAccuracy)
                                drawCircle(color = primaryColor, radius = 6.dp.toPx(), center = Offset(x, y))
                            } else {
                                val path = Path()
                                val points = mutableListOf<Offset>()
                                
                                val stepX = graphWidth / (chronologicalScores.size - 1)
                                chronologicalScores.forEachIndexed { index, scoreObj ->
                                    val accuracy = scoreObj.score / scoreObj.totalMarks
                                    val boundedAccuracy = accuracy.coerceIn(0.0, 1.0).toFloat()
                                    val x = paddingLeft + index * stepX
                                    val y = paddingTop + graphHeight * (1.0f - boundedAccuracy)
                                    points.add(Offset(x, y))
                                    if (index == 0) {
                                        path.moveTo(x, y)
                                    } else {
                                        path.lineTo(x, y)
                                    }
                                }

                                // Render Accuracy Line
                                drawPath(
                                    path = path,
                                    color = strokeColor,
                                    style = Stroke(width = 3.dp.toPx())
                                )

                                // Render Joint Circles for tactile feedback on each score
                                points.forEach { pt ->
                                    drawCircle(
                                        color = primaryColor,
                                        radius = 4.dp.toPx(),
                                        center = pt
                                    )
                                }
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Oldest Limit", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Sequential Accuracy Metrics (%)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Newest Match", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // --- Extracted Weak Area Diagnostics Widget ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Troubleshoot, contentDescription = "Diagnostic reports", tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Smart Weak-Spot Detection",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Text(
                        text = "Recurrent topics you marked as difficult are auto-aggregated for dedicated spaced revision blocks.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    if (weakAreaFrequencies.isEmpty()) {
                        Text(
                            text = "No weak area items tracked yet. Log tests with focus topics to initiate auto-aggregation.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            weakAreaFrequencies.take(8).forEach { (topic, count) ->
                                InputChip(
                                    selected = true,
                                    onClick = {},
                                    label = { Text("$topic ($count times)") },
                                    colors = InputChipDefaults.inputChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                                    ),
                                    shape = CircleShape
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Tracker Log Title Header ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historical Practice Sheets (${scores.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Button(
                    onClick = { showAddScoreDialog = true },
                    modifier = Modifier.testTag("today_log_mock_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add score entry")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Score")
                }
            }
        }

        // --- Mock Test Grid List ---
        if (scores.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Mock sheet list is empty. Log mock marks to calculate consistency metrics.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(scores, key = { it.id }) { scoreObj ->
                val accuracyRatio = if (scoreObj.totalMarks > 0f) scoreObj.score / scoreObj.totalMarks else 0.0
                val accuracyPct = (accuracyRatio * 100).toInt()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = when {
                                accuracyPct >= 75 -> MaterialTheme.colorScheme.tertiaryContainer
                                accuracyPct >= 50 -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.errorContainer
                            },
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$accuracyPct%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = when {
                                        accuracyPct >= 75 -> MaterialTheme.colorScheme.onTertiaryContainer
                                        accuracyPct >= 50 -> MaterialTheme.colorScheme.onPrimaryContainer
                                        else -> MaterialTheme.colorScheme.onHorizontalContainer()
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = scoreObj.testTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Score: ${scoreObj.score} / ${scoreObj.totalMarks}  •  ${scoreObj.subject}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (scoreObj.weakAreas.isNotBlank()) {
                                Text(
                                    text = "Weak spots: ${scoreObj.weakAreas}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        IconButton(onClick = { viewModel.deleteMockScore(scoreObj) }) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = "Delete score logged record",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal dialogue to input score info
    if (showAddScoreDialog) {
        AlertDialog(
            onDismissRequest = { showAddScoreDialog = false },
            title = { Text("Log Mock Test Result") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = testTitle,
                        onValueChange = { testTitle = it },
                        label = { Text("Mock Practice Title / Code") },
                        placeholder = { Text("e.g. UPSC Prelims Full Mock 1") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = rawScore,
                            onValueChange = { rawScore = it },
                            label = { Text("Score Obtained") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = rawMaxScore,
                            onValueChange = { rawMaxScore = it },
                            label = { Text("Total Marks") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Box {
                        OutlinedTextField(
                            value = rawSubject,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Subject Area") },
                            trailingIcon = {
                                IconButton(onClick = { expandedDropdown = !expandedDropdown }) {
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "expand dropdown")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            subjectsList.forEach { valSub ->
                                DropdownMenuItem(
                                    text = { Text(valSub) },
                                    onClick = {
                                        rawSubject = valSub
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = rawWeakAreas,
                        onValueChange = { rawWeakAreas = it },
                        label = { Text("Weak Spots/Topics (Comma separated)") },
                        placeholder = { Text("e.g. Article 32, Trigonometry formula, Syllogisms") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val scoreVal = rawScore.toDoubleOrNull() ?: 0.0
                        val maxScoreVal = rawMaxScore.toDoubleOrNull() ?: 100.0
                        viewModel.logMockTestScore(
                            testTitle = testTitle.ifBlank { "Practice Test" },
                            score = scoreVal,
                            totalMarks = maxScoreVal,
                            subject = rawSubject,
                            weakAreasStr = rawWeakAreas
                        )
                        testTitle = ""
                        rawScore = ""
                        rawMaxScore = ""
                        rawWeakAreas = ""
                        showAddScoreDialog = false
                    },
                    modifier = Modifier.testTag("submit_mock_score")
                ) {
                    Text("Save Record")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddScoreDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Help resolve color contrast parameters
@Composable
private fun ColorScheme.onHorizontalContainer(): Color {
    return this.onErrorContainer
}
