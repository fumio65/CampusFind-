package com.campusfind.ui.navigation

/**
 * Navigation routes for CampusFind+
 *
 * UPDATED: Added SmartHistory route + Phase 6 Claims
 */
sealed class Screen(val route: String) {

    // Auth & Onboarding
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")

    // Main App
    object Home : Screen("home")
    object AddItem : Screen("add_item")

    object Detail : Screen("detail/{itemId}") {
        fun createRoute(itemId: String) = "detail/$itemId"
    }

    object EditItem : Screen("edit_item/{itemId}") {
        fun createRoute(itemId: String) = "edit_item/$itemId"
    }

    // User
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object SmartHistory : Screen("smart_history")  // ✅ NEW: Smart History

    // Claims (Phase 6)
    object SubmitClaim : Screen("submit_claim/{itemId}") {
        fun createRoute(itemId: String) = "submit_claim/$itemId"
    }

    object ReviewClaims : Screen("review_claims/{itemId}") {
        fun createRoute(itemId: String) = "review_claims/$itemId"
    }
}