package com.syq.lexi.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import com.syq.lexi.ui.screens.StudyScreenGrouped
import com.syq.lexi.ui.viewmodel.WordbookViewModel
import kotlinx.coroutines.launch

enum class NavigationItem {
    HOME, WORDBOOK, GAME, STUDY
}

@Composable
fun MainNavigation(drawerOpen: MutableState<Boolean>) {
    val currentScreen = remember { mutableStateOf(NavigationItem.HOME) }
    val selectedWordbook = remember { mutableStateOf<WordbookEntity?>(null) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val wordbookViewModel = remember {
        val database = LexiDatabase.getDatabase(context)
        val repository = WordbookRepository(
            database.wordbookDao(),
            database.wordDao(),
            database.studyRecordDao()
        )
        WordbookViewModel(repository)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent()
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (currentScreen.value != NavigationItem.STUDY) {
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
                        innerPadding = innerPadding
                    )
                    NavigationItem.WORDBOOK -> WordbookScreen(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        innerPadding = innerPadding,
                        viewModel = wordbookViewModel,
                        onWordbookClick = { wordbook ->
                            selectedWordbook.value = wordbook
                            wordbookViewModel.selectWordbook(wordbook)
                            currentScreen.value = NavigationItem.STUDY
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
                }
            }
        }
    }
}
