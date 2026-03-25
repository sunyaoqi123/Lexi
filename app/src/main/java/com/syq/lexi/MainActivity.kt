package com.syq.lexi

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.syq.lexi.data.database.LexiDatabase
import com.syq.lexi.data.repository.WordbookRepository
import com.syq.lexi.ui.navigation.MainNavigation
import com.syq.lexi.ui.screens.AuthScreen
import com.syq.lexi.ui.theme.LexiTheme
import com.syq.lexi.ui.viewmodel.AuthViewModel
import com.syq.lexi.ui.viewmodel.SyncState
import com.syq.lexi.ui.viewmodel.SyncViewModel
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val prefs = context.getSharedPreferences("lexi_prefs", Context.MODE_PRIVATE)
            val isDarkTheme = remember {
                mutableStateOf(prefs.getBoolean("dark_theme", false))
            }

            LexiTheme(darkTheme = isDarkTheme.value) {
                MainApp(
                    isDarkTheme = isDarkTheme.value,
                    onToggleDarkTheme = {
                        isDarkTheme.value = !isDarkTheme.value
                        prefs.edit().putBoolean("dark_theme", isDarkTheme.value).apply()
                    }
                )
            }
        }
    }
}

@Composable
fun MainApp(
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: () -> Unit = {}
) {
    val context = LocalContext.current
    val authViewModel = remember { AuthViewModel(context) }
    val token by authViewModel.token.collectAsState()
    val drawerOpen = remember { mutableStateOf(false) }

    val syncViewModel = remember {
        val db = LexiDatabase.getDatabase(context)
        val repo = WordbookRepository(db.wordbookDao(), db.wordDao(), db.studyRecordDao(), db.studyPlanDao())
        SyncViewModel(context, repo)
    }
    val syncState by syncViewModel.syncState.collectAsState()

    // 不自动同步，用户需手动点击同步
    // 但登录后如果本地为空，自动从用户词库初始化一次
    LaunchedEffect(token) {
        if (!token.isNullOrEmpty()) {
            val db = LexiDatabase.getDatabase(context)
            val repo = WordbookRepository(db.wordbookDao(), db.wordDao(), db.studyRecordDao(), db.studyPlanDao())
            val localWordbooks = repo.getAllWordbooks().first()
            if (localWordbooks.isEmpty()) {
                syncViewModel.syncFromUserWordbooks()
            }
        }
    }

    if (token.isNullOrEmpty()) {
        AuthScreen(
            viewModel = authViewModel,
            onAuthSuccess = {}
        )
    } else {
        Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
            MainNavigation(
                drawerOpen = drawerOpen,
                isDarkTheme = isDarkTheme,
                onToggleDarkTheme = onToggleDarkTheme,
                authViewModel = authViewModel,
                syncViewModel = syncViewModel
            )
        }
    }
}
