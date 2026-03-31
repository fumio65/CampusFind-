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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ═══════════════════════════════════════════════════════════════════════════
// BRAND COLORS — fixed, never change with theme
// ═══════════════════════════════════════════════════════════════════════════

val ModernAccent       = Color(0xFF6C63FF)
val ModernAccentDim    = Color(0xFF2d2a55)
val ModernAccentBright = Color(0xFF8b84ff)

val ModernLost         = Color(0xFFff4d6d)
val ModernLostDim      = Color(0xFF3d1a22)
val ModernLostBg       = Color(0xFFfff0f3)

val ModernFound        = Color(0xFF2dd4a0)
val ModernFoundDim     = Color(0xFF0d3328)
val ModernFoundBg      = Color(0xFFF0fdf9)
val ModernFoundText    = Color(0xFF1a6b50)

val ModernError        = Color(0xFFff4d6d)
val ModernWarning      = Color(0xFFfebc2e)
val ModernSuccess      = Color(0xFF2dd4a0)

// ── Legacy constants — kept for ModernComponents.kt compatibility ──────────
val ModernBg           = Color(0xFF0f0f11)
val ModernSurface      = Color(0xFF1a1a1f)
val ModernSurface2     = Color(0xFF242429)
val ModernBorder       = Color(0xFF2e2e36)
val ModernText         = Color(0xFFe8e8f0)
val ModernTextMuted    = Color(0xFF6b6b7e)
val ModernTextDim      = Color(0xFF3e3e4e)

val LightCardBg        = Color(0xFFffffff)
val LightBodyBg        = Color(0xFFf5f5f0)
val LightTextPrimary   = Color(0xFF1a1a2e)
val LightTextSecondary = Color(0xFF888888)
val LightBorder        = Color(0xFFe8e8e4)

// ═══════════════════════════════════════════════════════════════════════════
// APP COLORS — adaptive per light / dark mode
// Use via: val colors = LocalAppColors.current
// ═══════════════════════════════════════════════════════════════════════════

data class AppColors(
    val screenBg: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val inputBg: Color,
    val pillBg: Color,
    val pillBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val divider: Color,
    val avatarBg: Color,
    val heroGradientStart: Color,
    val heroGradientMid: Color,
    val heroGradientEnd: Color,
    val isDark: Boolean
)

private val LightAppColors = AppColors(
    screenBg          = Color(0xFFF4F4F0),
    cardBg            = Color(0xFFFFFFFF),
    cardBorder        = Color(0xFFEBEBEB),
    inputBg           = Color(0xFFF9F9F6),
    pillBg            = Color(0xFFF9F9F6),
    pillBorder        = Color(0xFFE8E8E4),
    textPrimary       = Color(0xFF1A1A2E),
    textSecondary     = Color(0xFF666666),
    textMuted         = Color(0xFF888888),
    divider           = Color(0xFFF4F4F0),
    avatarBg          = Color(0xFFE8E8E4),
    heroGradientStart = Color(0xFF1a1228),
    heroGradientMid   = Color(0xFF2e1f48),
    heroGradientEnd   = Color(0xFF1a2a20),
    isDark            = false
)

private val DarkAppColors = AppColors(
    screenBg          = Color(0xFF0D0C12),
    cardBg            = Color(0xFF16151E),
    cardBorder        = Color(0xFF6C63FF).copy(alpha = 0.18f),
    inputBg           = Color(0xFF1C1B25),
    pillBg            = Color(0xFF1C1B25),
    pillBorder        = Color(0xFF6C63FF).copy(alpha = 0.25f),
    textPrimary       = Color(0xFFF2F1FF),
    textSecondary     = Color(0xFFB0AFCC),
    textMuted         = Color(0xFF7A7994),
    divider           = Color(0xFF252432),
    avatarBg          = Color(0xFF252432),
    heroGradientStart = Color(0xFF1a1228),
    heroGradientMid   = Color(0xFF2e1f48),
    heroGradientEnd   = Color(0xFF1a2a20),
    isDark            = true
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

// ═══════════════════════════════════════════════════════════════════════════
// MATERIAL COLOR SCHEMES
// ═══════════════════════════════════════════════════════════════════════════

private val LightColorScheme = lightColorScheme(
    primary            = ModernAccent,
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFEDE7FF),
    onPrimaryContainer = Color(0xFF3D35CC),
    background         = Color(0xFFF4F4F0),
    onBackground       = Color(0xFF1A1A2E),
    surface            = Color(0xFFFFFFFF),
    onSurface          = Color(0xFF1A1A2E),
    surfaceVariant     = Color(0xFFF4F4F0),
    onSurfaceVariant   = Color(0xFF666666),
    error              = Color(0xFFff4d6d),
    onError            = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary            = ModernAccentBright,
    onPrimary          = Color(0xFF1A1228),
    primaryContainer   = Color(0xFF2d2a55),
    onPrimaryContainer = Color(0xFFEDE7FF),
    background         = Color(0xFF0D0C12),
    onBackground       = Color(0xFFF2F1FF),
    surface            = Color(0xFF16151E),
    onSurface          = Color(0xFFF2F1FF),
    surfaceVariant     = Color(0xFF1C1B25),
    onSurfaceVariant   = Color(0xFFB0AFCC),
    error              = Color(0xFFff4d6d),
    onError            = Color.White
)

// ═══════════════════════════════════════════════════════════════════════════
// THEME COMPOSABLE
// ═══════════════════════════════════════════════════════════════════════════

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
        else      -> LightColorScheme
    }

    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars     = false  // always white — all screens have dark hero
            insetsController.isAppearanceLightNavigationBars = !darkTheme
            // Match nav bar background to screen background
            window.navigationBarColor = if (darkTheme)
                android.graphics.Color.parseColor("#0D0C12")  // DarkAppColors.screenBg
            else
                android.graphics.Color.parseColor("#F4F4F0")  // LightAppColors.screenBg
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = Typography,
            content     = content
        )
    }
}