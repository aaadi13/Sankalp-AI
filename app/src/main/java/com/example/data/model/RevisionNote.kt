package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "revision_notes")
data class RevisionNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val sourceText: String,
    val summaryPoints: String,       // JSON array or bullet characters
    val flashcardsJson: String,      // JSON lists of Question-Answer pairs
    val subject: String = "General Studies",
    val dateCreated: Long = System.currentTimeMillis()
)
