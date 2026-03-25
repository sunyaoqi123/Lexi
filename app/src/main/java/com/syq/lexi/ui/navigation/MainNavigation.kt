package com.syq.lexi.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.syq.lexi.data.database.LexiDatabase
import com.syq.lexi.data.database.WordbookEntity
import com.syq.lexi.data.repository.WordbookRepository
import com.syq.lexi.ui.screens.DrawerContent
import com.syq.lexi.ui.screens.HomeScreen
import com.syq.lexi.ui.screens.WordbookScreen
import com.syq.lexi.ui.screens.GameScreen
import com.syq.lexi.ui.screens.LearningScreen
import com.syq.lexi.ui.screens.StarredWordsScreen
import com.syq.lexi.ui.screens.StudyScreenGrouped
import com.syq.lexi.ui.viewmodel.LearningViewModel
import com.syq.lexi.ui.viewmodel.StudyPlanViewModel
import com.syq.lexi.ui.viewmodel.WordbookViewModel
import com.syq.lexi.ui.viewmodel.AuthViewModel
import com.syq.lexi.ui.viewmodel.SyncViewModel
import kotlinx.coroutines.launch

enum class NavigationItem {
    HOME, WORDBOOK, GAME, STUDY, LEARNING, STARRED
}

@Composable
fun MainNavigation(
    drawerOpen: MutableState<Boolean>,
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: () -> Unit = {},
    authViewModel: AuthViewModel? = null,
    syncViewModel: SyncViewModel? = null
) {
    val currentScreen = remember { mutableStateOf(NavigationItem.HOME) }
    val selectedWordbook = remember { mutableStateOf<WordbookEntity?>(null) }
    val selectedStarredWordbook = remember { mutableStateOf<WordbookEntity?>(null) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val wordbookViewModel = remember {
        val database = LexiDatabase.getDatabase(context)
        val repository = WordbookRepository(
            database.wordbookDao(),
            database.wordDao(),
            database.studyRecordDao(),
            database.studyPlanDao()
        )
        WordbookViewModel(repository, context)
    }

    val learningViewModel = remember {
        val database = LexiDatabase.getDatabase(context)
        val repository = WordbookRepository(
            database.wordbookDao(),
            database.wordDao(),
            database.studyRecordDao()
        )
        LearningViewModel(repository)
    }

    val studyPlanViewModel = remember {
        val database = LexiDatabase.getDatabase(context)
        StudyPlanViewModel(database.studyPlanDao(), context)
    }

    val selectedLearningWordbook = remember { mutableStateOf<WordbookEntity?>(null) }
    val selectedGroupSize = remember { mutableStateOf(10) }
    val isStarredMode = remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                isDarkTheme = isDarkTheme,
                onToggleDarkTheme = onToggleDarkTheme,
                authViewModel = authViewModel,
                syncViewModel = syncViewModel
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (currentScreen.value != NavigationItem.STUDY &&
                    currentScreen.value != NavigationItem.LEARNING &&
                    currentScreen.value != NavigationItem.STARRED) {
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = "首页") },
                            label = { androidx.compose.material3.Text("首页") },
                            selected = currentScreen.value == NavigationItem.HOME,
                            onClick = { currentScreen.value = NavigationItem.HOME }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Favorite, contentDescription = "单词本") },
                            label = { androidx.compose.material3.Text("单词本") },
                            selected = currentScreen.value == NavigationItem.WORDBOOK,
                            onClick = { currentScreen.value = NavigationItem.WORDBOOK }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Star, contentDescription = "游戏") },
                            label = { androidx.compose.material3.Text("游戏") },
                            selected = currentScreen.value == NavigationItem.GAME,
                            onClick = { currentScreen.value = NavigationItem.GAME }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                when (currentScreen.value) {
                    NavigationItem.HOME -> HomeScreen(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        innerPadding = innerPadding,
                        wordbooks = wordbookViewModel.wordbooks.collectAsState().value,
                        wordCounts = wordbookViewModel.wordCounts.collectAsState().value,
                        masteredCounts = wordbookViewModel.masteredCounts.collectAsState().value,
                        starredCounts = wordbookViewModel.starredCounts.collectAsState().value,
                        plans = studyPlanViewModel.plans.collectAsState().value,
                        onStartLearning = { wordbook, groupSize ->
                            selectedLearningWordbook.value = wordbook
                            selectedGroupSize.value = groupSize
                            isStarredMode.value = false
                            currentScreen.value = NavigationItem.LEARNING
                        },
                        onStartStarredLearning = { wordbook ->
                            selectedLearningWordbook.value = wordbook
                            isStarredMode.value = true
                            currentScreen.value = NavigationItem.LEARNING
                        },
                        onAddPlan = { wordbookId, dailyWords ->
                            val wb = wordbookViewModel.wordbooks.value.find { it.id == wordbookId }
                            if (wb != null) studyPlanViewModel.addPlan(wb, dailyWords)
                            else studyPlanViewModel.addPlan(wordbookId, dailyWords)
                        },
                        onDeletePlan = { plan ->
                            val wb = wordbookViewModel.wordbooks.value.find { it.id == plan.wordbookId }
                            if (wb != null) studyPlanViewModel.deletePlan(plan, wb.name)
                            else studyPlanViewModel.deletePlan(plan)
                        }
                    )
                    NavigationItem.WORDBOOK -> WordbookScreen(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        innerPadding = innerPadding,
                        viewModel = wordbookViewModel,
                        onWordbookClick = { wordbook ->
                            selectedWordbook.value = wordbook
                            wordbookViewModel.selectWordbook(wordbook)
                            currentScreen.value = NavigationItem.STUDY
                        },
                        onStarredClick = { wordbook ->
                            selectedStarredWordbook.value = wordbook
                            currentScreen.value = NavigationItem.STARRED
                        }
                    )
                    NavigationItem.GAME -> GameScreen(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        innerPadding = innerPadding
                    )
                    NavigationItem.STUDY -> {
                        selectedWordbook.value?.let { wordbook ->
                            StudyScreenGrouped(
                                wordbookId = wordbook.id,
                                wordbookName = wordbook.name,
                                onMenuClick = { scope.launch { drawerState.open() } },
                                onBackClick = { currentScreen.value = NavigationItem.WORDBOOK },
                                innerPadding = innerPadding,
                                viewModel = wordbookViewModel
                            )
                        }
                    }
                    NavigationItem.LEARNING -> {
                        selectedLearningWordbook.value?.let { wordbook ->
                            LearningScreen(
                                wordbookId = wordbook.id,
                                wordbookName = wordbook.name,
                                groupSize = selectedGroupSize.value,
                                starredOnly = isStarredMode.value,
                                onBackClick = { currentScreen.value = NavigationItem.HOME },
                                innerPadding = innerPadding,
                                viewModel = learningViewModel,
                                onStarChanged = { wordId, isStarred ->
                                    if (isStarred) wordbookViewModel.starWord(wordId)
                                    else wordbookViewModel.unstarWord(wordId)
                                }
                            )
                        }
                    }
                    NavigationItem.STARRED -> {
                        selectedStarredWordbook.value?.let { wordbook ->
                            StarredWordsScreen(
                                wordbookId = wordbook.id,
                                wordbookName = wordbook.name,
                                onBackClick = { currentScreen.value = NavigationItem.WORDBOOK },
                                innerPadding = innerPadding,
                                viewModel = wordbookViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
