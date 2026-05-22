package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class Profile(
    @PrimaryKey val id: Int = 1, // Single row profile
    val examType: String = "", // e.g. "UPSC Civil Services", "SSC CGL", "SBI PO", etc.
    val examDate: String = "", // e.g. "2026-06-21" Or user selected date
    val availableHours: Double = 4.0,
    val subjects: String = "", // Comma-separated or line-separated list of subjects
    val currentStreak: Int = 0,
    val lastStudyDate: String = "", // Date of last task completion "YYYY-MM-DD"
    val isPremium: Boolean = false,
    val subscriptionTier: String = "Free" // "Free", "Monthly Starter", "Yearly Pro"
)
