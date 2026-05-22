package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.database.AppDatabase
import com.example.data.model.MockScore
import com.example.data.model.Profile
import com.example.data.model.RevisionNote
import com.example.data.model.StudyTask
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

import com.example.util.ConnectivityObserver
import com.example.util.NetworkConnectivityObserver
import kotlinx.coroutines.delay

class AppViewModel(application: Application, private val repository: AppRepository) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "AppViewModel"
    }

    // Date formatting helper
    val currentDateStr: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // Network connectivity
    private val connectivityObserver = NetworkConnectivityObserver(application)
    
    val networkStatus: StateFlow<ConnectivityObserver.Status> = connectivityObserver.observe()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ConnectivityObserver.Status.Available
        )

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage = _syncMessage.asStateFlow()

    // UI State for AI processes
    private val _plannerLoading = MutableStateFlow(false)
    val plannerLoading = _plannerLoading.asStateFlow()

    private val _notesLoading = MutableStateFlow(false)
    val notesLoading = _notesLoading.asStateFlow()

    private val _apiError = MutableStateFlow<String?>(null)
    val apiError = _apiError.asStateFlow()

    // Active Profile Flow
    val profileState: StateFlow<Profile> = repository.profileFlow
        .map { it ?: Profile(id = 1) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Profile(id = 1)
        )

    // Active Tasks Flow for Today
    val todayTasks: StateFlow<List<StudyTask>> = repository.getTasksForDateFlow(currentDateStr)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // All Mock Scores Flow
    val mockScores: StateFlow<List<MockScore>> = repository.allMockScoresFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // All Revision Notes Flow
    val revisionNotes: StateFlow<List<RevisionNote>> = repository.allRevisionNotesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Free tier counts for demo protection
    private val _aiGenerationCount = MutableStateFlow(0)
    val aiGenerationCount = _aiGenerationCount.asStateFlow()

    init {
        // Observe network state for offline to online transitions to trigger sync
        viewModelScope.launch {
            var wasOffline = false
            networkStatus.collect { status ->
                if (status == ConnectivityObserver.Status.Available && wasOffline) {
                    performDataSync()
                }
                wasOffline = (status == ConnectivityObserver.Status.Lost || status == ConnectivityObserver.Status.Unavailable)
            }
        }

        // Build initial profile if base is empty
        viewModelScope.launch {
            val prof = repository.getProfileDirect()
            if (prof == null) {
                repository.saveProfile(
                    Profile(
                        id = 1,
                        examType = "UPSC Civil Services",
                        examDate = "2026-06-25",
                        availableHours = 6.0,
                        subjects = "Indian Polity, Indian Economy, Modern History, Geography",
                        currentStreak = 3, // Start with a small motivating streak!
                        lastStudyDate = "2026-05-21"
                    )
                )
                // Generate baseline initial tasks
                generateLocalDefaultPlan("UPSC Civil Services", "Indian Polity, Modern History", 6.0)
            }
        }
    }

    private suspend fun performDataSync() {
        _isSyncing.value = true
        _syncMessage.value = "Restored connection. Syncing local progress to cloud..."
        
        // Simulating artificial wait for data upload/download
        delay(2000)
        
        _syncMessage.value = "Progress synced successfully!"
        _isSyncing.value = false
        
        // Auto-dismiss the success message after 3 seconds
        delay(3000)
        if (_syncMessage.value == "Progress synced successfully!") {
            _syncMessage.value = null
        }
    }

    fun dismissSyncMessage() {
        _syncMessage.value = null
    }

    fun clearError() {
        _apiError.value = null
    }

    /**
     * Updates profile settings & generates new tasks.
     */
    fun saveProfileSettings(examType: String, examDate: String, availableHours: Double, subjects: String) {
        viewModelScope.launch {
            val current = repository.getProfileDirect() ?: Profile(id = 1)
            val updated = current.copy(
                examType = examType,
                examDate = examDate,
                availableHours = availableHours,
                subjects = subjects
            )
            repository.saveProfile(updated)
            triggerAiPlanGeneration()
        }
    }

    /**
     * Completes standard payment simulated checkout, upgrading the account immediately!
     */
    fun upgradeToPremium(tier: String) {
        viewModelScope.launch {
            val current = repository.getProfileDirect() ?: Profile(id = 1)
            val updated = current.copy(
                isPremium = true,
                subscriptionTier = tier
            )
            repository.saveProfile(updated)
        }
    }

    /**
     * Downgrades/resets tier for testing
     */
    fun togglePremiumStatus() {
        viewModelScope.launch {
            val current = repository.getProfileDirect() ?: Profile(id = 1)
            val updated = current.copy(
                isPremium = !current.isPremium,
                subscriptionTier = if (current.isPremium) "Free" else "Yearly Pro"
            )
            repository.saveProfile(updated)
        }
    }

    /**
     * Trigger AI daily planning schedule based on current profile.
     */
    fun triggerAiPlanGeneration() {
        viewModelScope.launch {
            val profile = repository.getProfileDirect() ?: return@launch
            
            // Limit checking for Free tier
            if (!profile.isPremium && _aiGenerationCount.value >= 2) {
                // Keep default list but flag limit warning
                _apiError.value = "AI Daily Task generation limit reached! Upgrade to Premium for infinite smart schedules."
                return@launch
            }

            _plannerLoading.value = true
            _apiError.value = null
            try {
                val modelTasks = GeminiClient.generateStudyPlan(
                    examType = profile.examType,
                    examDate = profile.examDate,
                    subjects = profile.subjects,
                    availableHours = profile.availableHours,
                    targetDate = currentDateStr
                )
                
                // Clear old tasks for today and save new ones
                repository.deleteTasksForDate(currentDateStr)
                repository.saveTasks(modelTasks)
                
                if (GeminiClient.isApiKeyAvailable()) {
                    _aiGenerationCount.value += 1
                }
            } catch (e: Exception) {
                _apiError.value = "Failed to plan: ${e.localizedMessage}. Loaded high-quality localized schedule."
                // Load localized fallback as safe outcome
                generateLocalDefaultPlan(profile.examType, profile.subjects, profile.availableHours)
            } finally {
                _plannerLoading.value = false
            }
        }
    }

    private suspend fun generateLocalDefaultPlan(examType: String, subjects: String, hours: Double) {
        repository.deleteTasksForDate(currentDateStr)
        val defaultTasks = listOf(
            StudyTask(
                title = "Study core concepts on ${subjects.split(",").firstOrNull() ?: "General Studies"}",
                subject = subjects.split(",").firstOrNull() ?: "General Studies",
                date = currentDateStr,
                estimatedHours = hours * 0.6,
                taskType = "Study"
            ),
            StudyTask(
                title = "Revise key formulas and historical dates for $examType Prep",
                subject = "Revision Day-1",
                date = currentDateStr,
                estimatedHours = hours * 0.25,
                taskType = "Revision"
            ),
            StudyTask(
                title = "Attempt mini high-yield mock MCQs test block",
                subject = "Mock Practice",
                date = currentDateStr,
                estimatedHours = hours * 0.15,
                taskType = "Mock Test"
            )
        )
        repository.saveTasks(defaultTasks)
    }

    /**
     * Toggles a study task complete, calculating streak updates in real time!
     */
    fun toggleTask(task: StudyTask) {
        viewModelScope.launch {
            val toggled = task.copy(isCompleted = !task.isCompleted)
            repository.updateTask(toggled)

            // Dynamic Streak recalculation when at least one task is completed
            val profile = repository.getProfileDirect() ?: return@launch
            val today = currentDateStr
            
            if (toggled.isCompleted) {
                // Completed task
                if (profile.lastStudyDate != today) {
                    val updatedStreak = profile.currentStreak + 1
                    repository.updateProfile(
                        profile.copy(
                            currentStreak = updatedStreak,
                            lastStudyDate = today
                        )
                    )
                }
            } else {
                // If checking if all tasks are uncompleted, we can optionally roll back, 
                // but let's keep it friendly: once task is done today, user keeps the streak intact!
            }
        }
    }

    /**
     * Delete task
     */
    fun deleteTask(task: StudyTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    /**
     * Save custom custom task
     */
    fun addCustomTask(title: String, subject: String, hours: Double, taskType: String) {
        viewModelScope.launch {
            val task = StudyTask(
                title = title,
                subject = subject,
                date = currentDateStr,
                estimatedHours = hours,
                taskType = taskType
            )
            repository.saveTask(task)
        }
    }

    /**
     * Convert textbook/file raw text to Bullet Revision Notes and Flashcards
     */
    fun processRevisionNotes(textInput: String, selectedSubject: String, onFinished: () -> Unit) {
        viewModelScope.launch {
            if (textInput.isBlank()) return@launch

            val profile = repository.getProfileDirect() ?: return@launch
            
            // Non-premium check: Free tier allows up to 2 generations
            if (!profile.isPremium && _aiGenerationCount.value >= 2) {
                _apiError.value = "AI Revision generation limit reached! Upgrade to Premium for unlimited instant flashcards & bullet notes."
                return@launch
            }

            _notesLoading.value = true
            _apiError.value = null
            try {
                val jsonObject = GeminiClient.generateRevisionNotes(textInput, selectedSubject)
                
                // Parse returned results
                val parsedTitle = jsonObject.optString("title", "AI Smart Summaries")
                val parsedSubject = jsonObject.optString("subject", selectedSubject)
                
                // Get bullet string lines
                val bulletsArr = jsonObject.optJSONArray("bullets")
                val bulletsList = StringBuilder()
                if (bulletsArr != null) {
                    for (i in 0 until bulletsArr.length()) {
                        bulletsList.append("• ").append(bulletsArr.getString(i)).append("\n")
                    }
                } else {
                    bulletsList.append("• Condensed learning concept point.")
                }
                
                // Flashcards JSON serialization
                val flashcardsArr = jsonObject.optJSONArray("flashcards")
                val flashcardsStr = flashcardsArr?.toString() ?: "[]"

                val note = RevisionNote(
                    title = parsedTitle,
                    sourceText = textInput,
                    summaryPoints = bulletsList.toString().trim(),
                    flashcardsJson = flashcardsStr,
                    subject = parsedSubject
                )
                repository.saveRevisionNote(note)
                
                if (GeminiClient.isApiKeyAvailable()) {
                    _aiGenerationCount.value += 1
                }
                onFinished()
            } catch (e: Exception) {
                _apiError.value = "AI note generation error: ${e.localizedMessage}. Local fallback generated."
                
                // Safe localized fallback creation for premium experience continuity
                val note = RevisionNote(
                    title = "Brief study outline notes",
                    sourceText = textInput,
                    summaryPoints = "• High-yield conceptual summarized detail 1.\n• Dynamic recall point 2.\n• Review formula 3.",
                    flashcardsJson = "[{\"question\":\"Why is spaced repetition useful?\",\"answer\":\"Maintains memory retentiveness.\"}]",
                    subject = selectedSubject
                )
                repository.saveRevisionNote(note)
                onFinished()
            } finally {
                _notesLoading.value = false
            }
        }
    }

    /**
     * Delete revision notes
     */
    fun deleteRevisionNote(note: RevisionNote) {
        viewModelScope.launch {
            repository.deleteRevisionNote(note)
        }
    }

    /**
     * Logs general exam scores 
     */
    fun logMockTestScore(testTitle: String, score: Double, totalMarks: Double, subject: String, weakAreasStr: String) {
        viewModelScope.launch {
            if (testTitle.isBlank()) return@launch
            val mockScore = MockScore(
                testTitle = testTitle,
                score = score,
                totalMarks = totalMarks,
                subject = subject,
                weakAreas = weakAreasStr
            )
            repository.saveMockScore(mockScore)
        }
    }

    /**
     * Delete mock marks
     */
    fun deleteMockScore(score: MockScore) {
        viewModelScope.launch {
            repository.deleteMockScore(score)
        }
    }

    // Factory Class for construction dependency
    class Factory(
        private val application: Application,
        private val repository: AppRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
                return AppViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
