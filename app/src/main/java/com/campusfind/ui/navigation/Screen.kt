package com.campusfind.ui.navigation

/**
 * FILE: app/src/main/java/com/campusfind/ui/navigation/Screen.kt
 *
 * Sealed class defining all navigation routes in the app.
 *
 * Why sealed class:
 * - Type-safe navigation — compiler ensures all routes are handled
 * - Each route is a unique object or class with parameters
 * - No magic strings scattered across the codebase
 *
 * Why createRoute() methods:
 * - Detail route needs itemId parameter: "detail/{itemId}" → "detail/abc123"
 * - createRoute(itemId) builds the actual navigation string
 *
 * Phase 1 routes (MCO 1):
 * - Login, Register, Home, AddItem, Detail, Settings
 *
 * Phase 2 additions (MCO 2):
 * - Onboarding, Profile, Notifications
 *
 * See: DEC-013 (Single Activity Navigation), TASK-110, TASK-123 (full NavGraph)
 */
sealed class Screen(val route: String) {

    // ── Auth ─────────────────────────────────────────────────────────────────

    object Login : Screen("login")

    object Register : Screen("register")

    // ── Main App ─────────────────────────────────────────────────────────────

    object Home : Screen("home")

    object AddItem : Screen("add_item")

    object Detail : Screen("detail/{itemId}") {
        fun createRoute(itemId: String) = "detail/$itemId"
    }

    object Settings : Screen("settings")

    // ── Phase 2 (not used in MCO 1) ─────────────────────────────────────────

    object Onboarding : Screen("onboarding")

    object Profile : Screen("profile")
}