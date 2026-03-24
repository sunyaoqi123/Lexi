package com.syq.lexi

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.syq.lexi.ui.navigation.MainNavigation
import com.syq.lexi.ui.theme.LexiTheme

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
    val drawerOpen = remember { mutableStateOf(false) }

    Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
        MainNavigation(
            drawerOpen = drawerOpen,
            isDarkTheme = isDarkTheme,
            onToggleDarkTheme = onToggleDarkTheme
        )
    }
}
