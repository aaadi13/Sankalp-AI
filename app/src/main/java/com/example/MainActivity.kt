package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.AppDatabase
import com.example.data.repository.AppRepository
import com.example.ui.screens.MockTrackerScreen
import com.example.ui.screens.PlannerScreen
import com.example.ui.screens.PremiumSettingsScreen
import com.example.ui.screens.RevisionScreen
import com.example.ui.screens.TodayFocusScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppViewModel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import com.example.util.ConnectivityObserver

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize Room DB & Repository
        val database = AppDatabase.getDatabase(this)
        val repository = AppRepository(database.appDao())

        setContent {
            MyApplicationTheme {
                // Initialize Viewmodel with Factory injection inside Compose
                val appViewModel: AppViewModel = viewModel(
                    factory = AppViewModel.Factory(application, repository)
                )
                
                AppMainScaffold(viewModel = appViewModel)
            }
        }
    }
}

enum class MainTab {
    TODAY, PLANNER, REVISION, TRACKER, PREMIUM
}

@Composable
fun AppMainScaffold(viewModel: AppViewModel) {
    var activeTab by rememberSaveable { mutableStateOf(MainTab.TODAY) }
    val profile by viewModel.profileState.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(syncMessage) {
        syncMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissSyncMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // Full bleed edge-to-edge
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("app_bottom_nav"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == MainTab.TODAY,
                    onClick = { activeTab = MainTab.TODAY },
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "today focuses tab") },
                    label = { Text("Today Focus") },
                    modifier = Modifier.testTag("nav_tab_today")
                )
                
                NavigationBarItem(
                    selected = activeTab == MainTab.PLANNER,
                    onClick = { activeTab = MainTab.PLANNER },
                    icon = { Icon(imageVector = Icons.Default.DateRange, contentDescription = "ai planners roadmap tab") },
                    label = { Text("Study Planner") },
                    modifier = Modifier.testTag("nav_tab_planner")
                )

                NavigationBarItem(
                    selected = activeTab == MainTab.REVISION,
                    onClick = { activeTab = MainTab.REVISION },
                    icon = { Icon(imageVector = Icons.Default.Book, contentDescription = "ai revision notes flashcards tab") },
                    label = { Text("Revision") },
                    modifier = Modifier.testTag("nav_tab_revision")
                )

                NavigationBarItem(
                    selected = activeTab == MainTab.TRACKER,
                    onClick = { activeTab = MainTab.TRACKER },
                    icon = { Icon(imageVector = Icons.Default.ShowChart, contentDescription = "mock tests analytics tracker tab") },
                    label = { Text("Mock Tracker") },
                    modifier = Modifier.testTag("nav_tab_tracker")
                )

                NavigationBarItem(
                    selected = activeTab == MainTab.PREMIUM,
                    onClick = { activeTab = MainTab.PREMIUM },
                    icon = { Icon(imageVector = Icons.Default.Stars, contentDescription = "billing premium tab") },
                    label = { Text("Sankalp Pro") },
                    modifier = Modifier.testTag("nav_tab_premium")
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                val isOffline = networkStatus == ConnectivityObserver.Status.Lost || networkStatus == ConnectivityObserver.Status.Unavailable
                if (isOffline) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudOff, contentDescription = "Offline Mode", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Offline Mode. Data saved locally.", color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                if (isSyncing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Syncing to cloud...", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (activeTab) {
                        MainTab.TODAY -> TodayFocusScreen(
                            viewModel = viewModel,
                            profile = profile,
                            onNavigateToPlanner = { activeTab = MainTab.PLANNER },
                            onNavigateToPremium = { activeTab = MainTab.PREMIUM }
                        )
                        MainTab.PLANNER -> PlannerScreen(
                            viewModel = viewModel,
                            profile = profile
                        )
                        MainTab.REVISION -> RevisionScreen(
                            viewModel = viewModel,
                            profile = profile
                        )
                        MainTab.TRACKER -> MockTrackerScreen(
                            viewModel = viewModel
                        )
                        MainTab.PREMIUM -> PremiumSettingsScreen(
                            viewModel = viewModel,
                            profile = profile
                        )
                    }
                }
            }
        }
    }
}
