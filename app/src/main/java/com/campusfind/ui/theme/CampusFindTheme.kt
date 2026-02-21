package com.campusfind.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ═══════════════════════════════════════════════════════════════════════════
// NOTE: Brand colors moved to Color.kt
// Modern UI colors defined below (not in Color.kt)
// ═══════════════════════════════════════════════════════════════════════════

// ═══ MODERN UI COLORS ═══
val ModernBg = Color(0xFF0f0f11)
val ModernSurface = Color(0xFF1a1a1f)
val ModernSurface2 = Color(0xFF242429)
val ModernBorder = Color(0xFF2e2e36)

val ModernText = Color(0xFFe8e8f0)
val ModernTextMuted = Color(0xFF6b6b7e)
val ModernTextDim = Color(0xFF3e3e4e)

val ModernAccent = Color(0xFF6c63ff)
val ModernAccentDim = Color(0xFF2d2a55)
val ModernAccentBright = Color(0xFF8b84ff)

val ModernLost = Color(0xFFff4d6d)
val ModernLostDim = Color(0xFF3d1a22)
val ModernLostBg = Color(0xFFfff0f3)

val ModernFound = Color(0xFF2dd4a0)
val ModernFoundDim = Color(0xFF0d3328)
val ModernFoundBg = Color(0xFFf0fdf9)
val ModernFoundText = Color(0xFF1a6b50)

val ModernError = Color(0xFFff4d6d)
val ModernWarning = Color(0xFFfebc2e)
val ModernSuccess = Color(0xFF2dd4a0)

val LightCardBg = Color(0xFFffffff)
val LightBodyBg = Color(0xFFf5f5f0)
val LightTextPrimary = Color(0xFF1a1a2e)
val LightTextSecondary = Color(0xFF888888)
val LightBorder = Color(0xFFe8e8e4)

private val LightColorScheme = lightColorScheme(
    primary = CampusFindPurple,  // ← Use from Color.kt
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE7FF),
    onPrimaryContainer = Color(0xFF3D35CC),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    error = Color(0xFFB00020),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    onPrimary = Color(0xFF21005E),
    primaryContainer = Color(0xFF3D35CC),
    onPrimaryContainer = Color(0xFFEDE7FF),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFE6E1E5),
    error = Color(0xFFCF6679),
    onError = Color.Black
)

@Composable
fun CampusFindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)

            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.statusBarColor = colorScheme.primary.toArgb()
                insetsController.isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}