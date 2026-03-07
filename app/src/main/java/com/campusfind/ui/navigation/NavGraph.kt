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
import com.campusfind.ui.screens.edititem.EditItemScreen  // ← ADDED
import com.campusfind.ui.screens.home.HomeScreen
import com.campusfind.ui.screens.login.LoginScreen
import com.campusfind.ui.screens.onboarding.OnboardingScreen
import com.campusfind.ui.screens.register.RegisterScreen
import com.campusfind.ui.screens.settings.SettingsScreen

/**
 * Navigation graph with modern HomeScreen support
 *
 * UPDATED: Added EditItem route for editing existing reports
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
                    // TODO: Navigate to Profile screen when implemented (Phase 4)
                    // For now, navigate to Settings as placeholder
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToSmartHistory = {
                    // TODO: Navigate to Smart History screen when implemented (Phase 4)
                    // For now, navigate to Settings as placeholder
                    navController.navigate(Screen.Settings.route)
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
                }
            )
        }

        // ── EditItem Route ───────────────────────────────────────────────────
        // ← ADDED THIS SECTION
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

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}