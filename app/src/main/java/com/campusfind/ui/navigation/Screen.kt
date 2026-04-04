package com.campusfind.ui.navigation

import android.net.Uri

/**
 * Navigation routes for CampusFind+
 *
 * UPDATED: Added Notifications route, ReviewClaims with itemTitle
 */
sealed class Screen(val route: String) {

    // Auth & Onboarding
    object Onboarding    : Screen("onboarding")
    object Login         : Screen("login")
    object Register      : Screen("register")

    // Main App
    object Home          : Screen("home")
    object AddItem       : Screen("add_item")
    object Notifications : Screen("notifications")

    object Detail : Screen("detail/{itemId}") {
        fun createRoute(itemId: String) = "detail/$itemId"
    }

    object EditItem : Screen("edit_item/{itemId}") {
        fun createRoute(itemId: String) = "edit_item/$itemId"
    }

    // User
    object Profile      : Screen("profile")
    object Settings     : Screen("settings")
    object SmartHistory : Screen("smart_history")

    // Claims (Phase 6)n
    object SubmitClaim : Screen("submit_claim/{itemId}") {
        fun createRoute(itemId: String) = "submit_claim/$itemId"
    }

    object ReviewClaims : Screen("review_claims/{itemId}/{itemTitle}") {
        fun createRoute(itemId: String, itemTitle: String = "Lost Item") =
            "review_claims/$itemId/${Uri.encode(itemTitle)}"
    }
}