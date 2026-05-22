package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mock_scores")
data class MockScore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val testTitle: String,
    val date: Long = System.currentTimeMillis(),
    val score: Double,
    val totalMarks: Double,
    val subject: String,
    val weakAreas: String // Comma-separated list of topics/concepts flagged as weak
)
