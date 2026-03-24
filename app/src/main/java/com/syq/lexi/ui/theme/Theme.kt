package com.syq.lexi.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = BluePrimaryContainer,
    onPrimaryContainer = BlueOnPrimaryContainer,
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = BlueSurfaceVariant,
    onSecondaryContainer = PrimaryDark,
    tertiary = Tertiary,
    onTertiary = Color.White,
    background = BlueBackground,
    onBackground = Color(0xFF1A1C2E),
    surface = BlueSurface,
    onSurface = Color(0xFF1A1C2E),
    surfaceVariant = BlueSurfaceVariant,
    onSurfaceVariant = Color(0xFF3D5070),
    outline = Color(0xFFADC6E8)
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = Color(0xFF003D82),
    primaryContainer = PrimaryDark,
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = SecondaryLight,
    onSecondary = Color(0xFF003550),
    background = Color(0xFF0F1729),
    onBackground = Color(0xFFE0EAFF),
    surface = Color(0xFF1A2540),
    onSurface = Color(0xFFE0EAFF),
    surfaceVariant = Color(0xFF243050),
    onSurfaceVariant = Color(0xFFADC6E8),
    outline = Color(0xFF3D5070)
)

@Composable
fun LexiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
