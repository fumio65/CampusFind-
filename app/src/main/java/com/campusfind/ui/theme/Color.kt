package com.campusfind.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * FILE: app/src/main/java/com/campusfind/ui/theme/Color.kt
 *
 * Color palette for CampusFind+ app.
 *
 * Brand colors (from DEC-012):
 * - Primary: Deep purple (#6c63ff) — buttons, FAB, app identity
 * - Lost status: Red (#ff4d6d) — item status badge
 * - Found status: Teal/Green (#2dd4a0) — item status badge
 *
 * Material 3 color system:
 * - Light and dark theme support
 * - Dynamic color (Android 12+) optional
 * - Semantic color roles (primary, secondary, tertiary, error, surface, etc.)
 *
 * See: DEC-012 (Material Design 3 Theme), TASK-124
 */

// ── Brand Colors ────────────────────────────────────────────────────────────

val CampusFindPurple = Color(0xFF6c63ff)    // Primary brand color
val LostRed = Color(0xFFff4d6d)              // Lost item status
val FoundGreen = Color(0xFF2dd4a0)           // Found item status

// ── Light Theme Colors ──────────────────────────────────────────────────────

val md_theme_light_primary = Color(0xFF6c63ff)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFE8E5FF)
val md_theme_light_onPrimaryContainer = Color(0xFF1B0066)

val md_theme_light_secondary = Color(0xFF5E5C71)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFE3DFF9)
val md_theme_light_onSecondaryContainer = Color(0xFF1A1A2B)

val md_theme_light_tertiary = Color(0xFF7A5368)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFFFD8E8)
val md_theme_light_onTertiaryContainer = Color(0xFF2D1223)

val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onErrorContainer = Color(0xFF410002)

val md_theme_light_background = Color(0xFFFFFBFF)
val md_theme_light_onBackground = Color(0xFF1C1B1E)

val md_theme_light_surface = Color(0xFFFFFBFF)
val md_theme_light_onSurface = Color(0xFF1C1B1E)
val md_theme_light_surfaceVariant = Color(0xFFE6E0F0)
val md_theme_light_onSurfaceVariant = Color(0xFF48464F)

val md_theme_light_outline = Color(0xFF79767F)
val md_theme_light_outlineVariant = Color(0xFFCAC4D3)

val md_theme_light_inverseSurface = Color(0xFF313033)
val md_theme_light_inverseOnSurface = Color(0xFFF4EFF4)
val md_theme_light_inversePrimary = Color(0xFFBDB1FF)

// ── Dark Theme Colors ───────────────────────────────────────────────────────

val md_theme_dark_primary = Color(0xFFBDB1FF)
val md_theme_dark_onPrimary = Color(0xFF31239C)
val md_theme_dark_primaryContainer = Color(0xFF4C3FB7)
val md_theme_dark_onPrimaryContainer = Color(0xFFE8E5FF)

val md_theme_dark_secondary = Color(0xFFC6C3DC)
val md_theme_dark_onSecondary = Color(0xFF2F2E41)
val md_theme_dark_secondaryContainer = Color(0xFF464559)
val md_theme_dark_onSecondaryContainer = Color(0xFFE3DFF9)

val md_theme_dark_tertiary = Color(0xFFEEB9CE)
val md_theme_dark_onTertiary = Color(0xFF472639)
val md_theme_dark_tertiaryContainer = Color(0xFF603C50)
val md_theme_dark_onTertiaryContainer = Color(0xFFFFD8E8)

val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)

val md_theme_dark_background = Color(0xFF1C1B1E)
val md_theme_dark_onBackground = Color(0xFFE6E1E6)

val md_theme_dark_surface = Color(0xFF1C1B1E)
val md_theme_dark_onSurface = Color(0xFFE6E1E6)
val md_theme_dark_surfaceVariant = Color(0xFF48464F)
val md_theme_dark_onSurfaceVariant = Color(0xFFCAC4D3)

val md_theme_dark_outline = Color(0xFF9A38F99)
val md_theme_dark_outlineVariant = Color(0xFF48464F)

val md_theme_dark_inverseSurface = Color(0xFFE6E1E6)
val md_theme_dark_inverseOnSurface = Color(0xFF313033)
val md_theme_dark_inversePrimary = Color(0xFF6c63ff)