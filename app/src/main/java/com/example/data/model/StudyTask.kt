package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_tasks")
data class StudyTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val subject: String,
    val date: String,             // Format "YYYY-MM-DD"
    val isCompleted: Boolean = false,
    val estimatedHours: Double = 1.0,
    val taskType: String = "Study", // "Study", "Revision", "Mock Test"
    val orderIndex: Int = 0
)
