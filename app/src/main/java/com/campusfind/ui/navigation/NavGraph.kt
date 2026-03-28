package com.campusfind.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.ui.screens.additem.AddItemScreen
import com.campusfind.ui.screens.detail.DetailScreen
import com.campusfind.ui.screens.edititem.EditItemScreen
import com.campusfind.ui.screens.home.HomeScreen
import com.campusfind.ui.screens.login.LoginScreen
import com.campusfind.ui.screens.onboarding.OnboardingScreen
import com.campusfind.ui.screens.profile.UserProfileScreen
import com.campusfind.ui.screens.register.RegisterScreen
import com.campusfind.ui.screens.settings.SettingsScreen
import com.campusfind.ui.screens.reviewclaims.ReviewClaimsScreen  // NEW: Phase 6
import com.campusfind.ui.screens.submitclaim.SubmitClaimScreen    // NEW: Phase 6

/**
 * Navigation graph with modern HomeScreen support
 *
 * UPDATED: Fixed Smart History crash - removed navigation call
 * Phase 2 feature navigation disabled until screen is implemented
 */
@Composable
fun CampusFindNavGraph(
    navController: NavHostController,
    sessionManager: SessionManager
) {
    // Determine start destination based on state
    val startDestination = remember(
        sessionManager.hasCompletedOnboarding,
        sessionManager.isLoggedIn
    ) {
        when {
            !sessionManager.hasCompletedOnboarding -> Screen.Onboarding.route
            sessionManager.isLoggedIn -> Screen.Home.route
            else -> Screen.Login.route
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // ── Onboarding ───────────────────────────────────────────────────────

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    sessionManager.markOnboardingCompleted()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Auth Routes ──────────────────────────────────────────────────────

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // ── Main App Routes ──────────────────────────────────────────────────

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAddItem = {
                    navController.navigate(Screen.AddItem.route)
                },
                onNavigateToDetail = { itemId ->
                    navController.navigate(Screen.Detail.createRoute(itemId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToSmartHistory = {
                    // ✅ FIXED: Phase 2 feature - do nothing to prevent crash
                    // Smart History screen not yet implemented
                    // When implemented, add: navController.navigate(Screen.SmartHistory.route)
                    android.util.Log.d("CampusFind", "Smart History - Phase 2 feature (not implemented)")
                },
                onLogout = {
                    sessionManager.clearSession()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                currentUserName = sessionManager.currentUserName ?: "User",
                currentUserEmail = sessionManager.currentUserEmail ?: "user@university.edu"
            )
        }

        composable(Screen.AddItem.route) {
            AddItemScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ── Detail Route (UPDATED FOR PHASE 6) ──────────────────────────────

        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("itemId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            DetailScreen(
                itemId = itemId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { itemId ->
                    navController.navigate(Screen.EditItem.createRoute(itemId))
                },
                // ↓ NEW: Phase 6 Claims navigation
                onNavigateToSubmitClaim = { itemId ->
                    navController.navigate(Screen.SubmitClaim.createRoute(itemId))
                },
                onNavigateToReviewClaims = { itemId ->
                    navController.navigate(Screen.ReviewClaims.createRoute(itemId))
                }
            )
        }

        // ── EditItem Route ───────────────────────────────────────────────────

        composable(
            route = Screen.EditItem.route,
            arguments = listOf(
                navArgument("itemId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            EditItemScreen(
                itemId = itemId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ── Settings Route ───────────────────────────────────────────────────

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                currentUserName = sessionManager.currentUserName ?: "User",
                currentUserEmail = sessionManager.currentUserEmail ?: ""
            )
        }

        // ── Profile Route ────────────────────────────────────────────────────

        composable(Screen.Profile.route) {
            UserProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAddItem = {
                    navController.navigate(Screen.AddItem.route)
                },
                onNavigateToHome = {
                    // ✅ FIX: Just go back - we came from Home, so going back = Home
                    navController.popBackStack()
                },
                onNavigateToDetail = { itemId ->
                    navController.navigate(Screen.Detail.createRoute(itemId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // ── CLAIMS ROUTES (NEW IN PHASE 6) ──────────────────────────────────

        composable(
            route = Screen.SubmitClaim.route,
            arguments = listOf(
                navArgument("itemId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable

            // TODO: Get item title from viewModel or pass via savedStateHandle
            val itemTitle = "Lost Item"  // Placeholder

            SubmitClaimScreen(
                itemId = itemId,
                itemTitle = itemTitle,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.ReviewClaims.route,
            arguments = listOf(
                navArgument("itemId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable

            // TODO: Get item title from viewModel or pass via savedStateHandle
            val itemTitle = "Lost Item"  // Placeholder

            ReviewClaimsScreen(
                itemId = itemId,
                itemTitle = itemTitle,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}