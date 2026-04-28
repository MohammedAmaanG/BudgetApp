package com.prog7313.budgetapp.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary          = AccentBlue,
    onPrimary        = White,
    primaryContainer = Color(0xFFD6E8FF),
    secondary        = AccentGreen,
    onSecondary      = White,
    tertiary         = AccentPurple,
    background       = SurfaceLight,
    surface          = White,
    onBackground     = TextPrimary,
    onSurface        = TextPrimary,
    error            = AccentRed,
    outline          = Divider
)

private val DarkColorScheme = darkColorScheme(
    primary          = AccentBlue,
    onPrimary        = White,
    primaryContainer = Color(0xFF1A3A5C),
    secondary        = AccentGreen,
    onSecondary      = White,
    tertiary         = AccentPurple,
    background       = DarkNavy,
    surface          = SurfaceCard,
    onBackground     = White,
    onSurface        = White,
    error            = AccentRed,
    outline          = Color(0xFF3A4A5C)
)

@Composable
fun Application001Theme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content
    )
}