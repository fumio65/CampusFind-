package com.campusfind.ui.navigation

/**
 * FILE: app/src/main/java/com/campusfind/ui/navigation/Screen.kt
 *
 * Sealed class defining all navigation routes in the app.
 *
 * UPDATED: Added Onboarding and Profile routes.
 *
 * See: DEC-013 (Single Activity Navigation), TASK-110, TASK-118
 */
sealed class Screen(val route: String) {

    // ── Onboarding / Auth ────────────────────────────────────────────────────

    object Onboarding : Screen("onboarding")

    object Login : Screen("login")

    object Register : Screen("register")

    // ── Main App ─────────────────────────────────────────────────────────────

    object Home : Screen("home")

    object AddItem : Screen("add_item")

    object Detail : Screen("detail/{itemId}") {
        fun createRoute(itemId: String) = "detail/$itemId"
    }

    object Profile : Screen("profile")

    object Settings : Screen("settings")
}