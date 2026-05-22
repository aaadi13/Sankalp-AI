package com.example.data.dao

import androidx.room.*
import com.example.data.model.MockScore
import com.example.data.model.Profile
import com.example.data.model.RevisionNote
import com.example.data.model.StudyTask
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Profile ---
    @Query("SELECT * FROM profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<Profile?>

    @Query("SELECT * FROM profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileDirect(): Profile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: Profile)

    @Update
    suspend fun updateProfile(profile: Profile)

    // --- Daily Tasks ---
    @Query("SELECT * FROM study_tasks WHERE date = :date ORDER BY orderIndex ASC")
    fun getTasksForDateFlow(date: String): Flow<List<StudyTask>>

    @Query("SELECT * FROM study_tasks ORDER BY date DESC, orderIndex ASC")
    fun getAllTasksFlow(): Flow<List<StudyTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: StudyTask)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<StudyTask>)

    @Update
    suspend fun updateTask(task: StudyTask)

    @Delete
    suspend fun deleteTask(task: StudyTask)

    @Query("DELETE FROM study_tasks WHERE date = :date")
    suspend fun deleteTasksForDate(date: String)

    @Query("DELETE FROM study_tasks")
    suspend fun clearAllTasks()

    // --- Mock Scores ---
    @Query("SELECT * FROM mock_scores ORDER BY date DESC")
    fun getAllMockScoresFlow(): Flow<List<MockScore>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockScore(score: MockScore)

    @Delete
    suspend fun deleteMockScore(score: MockScore)

    // --- Revision Notes ---
    @Query("SELECT * FROM revision_notes ORDER BY dateCreated DESC")
    fun getAllRevisionNotesFlow(): Flow<List<RevisionNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevisionNote(note: RevisionNote)

    @Delete
    suspend fun deleteRevisionNote(note: RevisionNote)
}
