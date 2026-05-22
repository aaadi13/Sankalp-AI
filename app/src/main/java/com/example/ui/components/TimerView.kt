package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun TimerView(
    modifier: Modifier = Modifier,
    initialMinutes: Int = 25,
    onTimerFinished: () -> Unit = {}
) {
    var totalTimeSeconds by remember { mutableStateOf(initialMinutes * 60) }
    var timeRemainingSeconds by remember { mutableStateOf(totalTimeSeconds) }
    var isTimerRunning by remember { mutableStateOf(false) }

    // Re-sync if initialMinutes changes
    LaunchedEffect(initialMinutes) {
        totalTimeSeconds = initialMinutes * 60
        timeRemainingSeconds = totalTimeSeconds
        isTimerRunning = false
    }

    // Timer logic unit
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (timeRemainingSeconds > 0) {
                delay(1000L)
                timeRemainingSeconds--
            }
            if (timeRemainingSeconds == 0) {
                isTimerRunning = false
                onTimerFinished()
            }
        }
    }

    val progress = if (totalTimeSeconds > 0) {
        timeRemainingSeconds.toFloat() / totalTimeSeconds.toFloat()
    } else 1.0f

    val minutes = timeRemainingSeconds / 60
    val seconds = timeRemainingSeconds % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Focus Pomodoro Session",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                // Background Track Arc
                Canvas(modifier = Modifier.size(150.dp)) {
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Foreground active ticking Arc
                Canvas(modifier = Modifier.size(150.dp)) {
                    drawArc(
                        color = primaryColor,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Countdown Text Content
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeFormatted,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isTimerRunning) "Studying..." else "Interval",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Start/Pause Button
                Button(
                    onClick = { isTimerRunning = !isTimerRunning },
                    modifier = Modifier.testTag("timer_run_toggle"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTimerRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Timer play-pause control",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isTimerRunning) "Pause" else "Start focus")
                }

                // Reset Button
                OutlinedButton(
                    onClick = {
                        isTimerRunning = false
                        timeRemainingSeconds = totalTimeSeconds
                    },
                    modifier = Modifier.testTag("timer_reset")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset timer",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset")
                }
            }
        }
    }
}
