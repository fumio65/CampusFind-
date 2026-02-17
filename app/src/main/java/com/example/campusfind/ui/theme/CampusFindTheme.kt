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

/**
 * FILE: app/src/main/java/com/campusfind/ui/theme/CampusFindTheme.kt
 *
 * Brand colors from DEC-012:
 *   Primary:      #6C63FF  (deep purple)
 *   Status LOST:  #FF4D6D  (red)
 *   Status FOUND: #2DD4A0  (teal/green)
 */

// ─── Brand Colors ────────────────────────────────────────────────────────────
val Purple          = Color(0xFF6C63FF)
val PurpleLight     = Color(0xFFEDE7FF)
val PurpleDark      = Color(0xFF3D35CC)

// Status badge colors — used directly in StatusBadge.kt (TASK-121)
val StatusLostBg    = Color(0xFFFFEBEE)
val StatusLostText  = Color(0xFFC62828)
val StatusFoundBg   = Color(0xFFE8F5E9)
val StatusFoundText = Color(0xFF2E7D32)

// ─── Color Schemes ───────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary             = Purple,
    onPrimary           = Color.White,
    primaryContainer    = PurpleLight,
    onPrimaryContainer  = PurpleDark,
    background          = Color(0xFFFFFBFE),
    onBackground        = Color(0xFF1C1B1F),
    surface             = Color(0xFFFFFBFE),
    onSurface           = Color(0xFF1C1B1F),
    error               = Color(0xFFB00020),
    onError             = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary             = Color(0xFFBB86FC),
    onPrimary           = Color(0xFF21005E),
    primaryContainer    = PurpleDark,
    onPrimaryContainer  = PurpleLight,
    background          = Color(0xFF121212),
    onBackground        = Color(0xFFE6E1E5),
    surface             = Color(0xFF121212),
    onSurface           = Color(0xFFE6E1E5),
    error               = Color(0xFFCF6679),
    onError             = Color.Black
)

// ─── Theme Composable ─────────────────────────────────────────────────────────
@Composable
fun CampusFindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,   // optional per DEC-012
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}