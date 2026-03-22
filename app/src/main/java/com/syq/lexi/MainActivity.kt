package com.syq.lexi

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
import com.syq.lexi.ui.navigation.MainNavigation
import com.syq.lexi.ui.theme.LexiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LexiTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val drawerOpen = remember { mutableStateOf(false) }
    
    Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
        MainNavigation(drawerOpen)
    }
}
