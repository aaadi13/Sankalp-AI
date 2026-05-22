package com.example.data.repository

import com.example.data.dao.AppDao
import com.example.data.model.MockScore
import com.example.data.model.Profile
import com.example.data.model.RevisionNote
import com.example.data.model.StudyTask
import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {
    
    // Profile
    val profileFlow: Flow<Profile?> = appDao.getProfileFlow()
    
    suspend fun getProfileDirect(): Profile? = appDao.getProfileDirect()
    
    suspend fun saveProfile(profile: Profile) {
        appDao.insertProfile(profile)
    }

    suspend fun updateProfile(profile: Profile) {
        appDao.updateProfile(profile)
    }

    // Tasks
    fun getTasksForDateFlow(date: String): Flow<List<StudyTask>> = appDao.getTasksForDateFlow(date)
    val allTasksFlow: Flow<List<StudyTask>> = appDao.getAllTasksFlow()

    suspend fun saveTask(task: StudyTask) = appDao.insertTask(task)
    suspend fun saveTasks(tasks: List<StudyTask>) = appDao.insertTasks(tasks)
    suspend fun updateTask(task: StudyTask) = appDao.updateTask(task)
    suspend fun deleteTask(task: StudyTask) = appDao.deleteTask(task)
    suspend fun deleteTasksForDate(date: String) = appDao.deleteTasksForDate(date)
    suspend fun clearAllTasks() = appDao.clearAllTasks()

    // Mock Scores
    val allMockScoresFlow: Flow<List<MockScore>> = appDao.getAllMockScoresFlow()
    suspend fun saveMockScore(score: MockScore) = appDao.insertMockScore(score)
    suspend fun deleteMockScore(score: MockScore) = appDao.deleteMockScore(score)

    // Revision Notes
    val allRevisionNotesFlow: Flow<List<RevisionNote>> = appDao.getAllRevisionNotesFlow()
    suspend fun saveRevisionNote(note: RevisionNote) = appDao.insertRevisionNote(note)
    suspend fun deleteRevisionNote(note: RevisionNote) = appDao.deleteRevisionNote(note)
}
