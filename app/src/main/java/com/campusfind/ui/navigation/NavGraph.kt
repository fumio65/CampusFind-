package com.campusfind.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.ui.screens.additem.AddItemScreen
import com.campusfind.ui.screens.detail.DetailScreen
import com.campusfind.ui.screens.edititem.EditItemScreen
import com.campusfind.ui.screens.login.LoginScreen
import com.campusfind.ui.screens.main.MainScreen
import com.campusfind.ui.screens.notifications.NotificationScreen
import com.campusfind.ui.screens.notifications.NotificationViewModel
import com.campusfind.ui.screens.onboarding.OnboardingScreen
import com.campusfind.ui.screens.register.RegisterScreen
import com.campusfind.ui.screens.settings.SettingsScreen
import com.campusfind.ui.screens.reviewclaims.ReviewClaimsScreen
import com.campusfind.ui.screens.submitclaim.SubmitClaimScreen

/**
 * NavGraph.kt — Phase 2 (Firebase Auth)
 *
 * Change from Phase 1:
 * - startDestination auth guard now uses SessionManager.isLoggedIn
 *   which checks firebaseAuth.currentUser != null first
 * - Logout now calls sessionManager.clearSession() which internally
 *   calls firebaseAuth.signOut() — no other changes needed
 *
 * Everything else is identical to Phase 1.
 */
@Composable
fun CampusFindNavGraph(
    navController: NavHostController,
    sessionManager: SessionManager
) {
    // Auth guard — Firebase Auth state is checked inside isLoggedIn
    val startDestination = remember(
        sessionManager.hasCompletedOnboarding,
        sessionManager.isLoggedIn
    ) {
        when {
            !sessionManager.hasCompletedOnboarding -> Screen.Onboarding.route
            sessionManager.isLoggedIn              -> Screen.Home.route
            else                                   -> Screen.Login.route
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {

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

        // ── Auth ─────────────────────────────────────────────────────────────

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
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
                onNavigateBack   = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // ── Main ─────────────────────────────────────────────────────────────

        composable(Screen.Home.route) { backStackEntry ->
            val notifViewModel: NotificationViewModel = hiltViewModel(backStackEntry)

            androidx.compose.runtime.LaunchedEffect(Unit) {
                notifViewModel.loadNotifications()
            }

            MainScreen(
                onNavigateToAddItem       = { navController.navigate(Screen.AddItem.route) },
                onNavigateToDetail        = { navController.navigate(Screen.Detail.createRoute(it)) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToSettings      = { navController.navigate(Screen.Settings.route) },
                onLogout = {
                    // clearSession() calls firebaseAuth.signOut() internally
                    sessionManager.clearSession()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                currentUserName  = sessionManager.currentUserName  ?: "User",
                currentUserEmail = sessionManager.currentUserEmail ?: "user@university.edu",
                notifViewModel   = notifViewModel
            )
        }

        // ── Notifications ────────────────────────────────────────────────────

        composable(Screen.Notifications.route) {
            NotificationScreen(
                onNavigateBack     = { navController.popBackStack() },
                onNavigateToDetail = { navController.navigate(Screen.Detail.createRoute(it)) }
            )
        }

        // ── Add Item ─────────────────────────────────────────────────────────

        composable(Screen.AddItem.route) {
            AddItemScreen(onNavigateBack = { navController.popBackStack() })
        }

        // ── Detail ───────────────────────────────────────────────────────────

        composable(
            route     = Screen.Detail.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            DetailScreen(
                itemId                   = itemId,
                onNavigateBack           = { navController.popBackStack() },
                onNavigateToEdit         = { navController.navigate(Screen.EditItem.createRoute(it)) },
                onNavigateToSubmitClaim  = { navController.navigate(Screen.SubmitClaim.createRoute(it)) },
                onNavigateToReviewClaims = { id, title ->
                    navController.navigate(Screen.ReviewClaims.createRoute(id, title))
                }
            )
        }

        // ── Edit Item ────────────────────────────────────────────────────────

        composable(
            route     = Screen.EditItem.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            EditItemScreen(
                itemId         = itemId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Settings ─────────────────────────────────────────────────────────

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack    = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                currentUserName  = sessionManager.currentUserName  ?: "User",
                currentUserEmail = sessionManager.currentUserEmail ?: ""
            )
        }

        // ── Claims ───────────────────────────────────────────────────────────

        composable(
            route     = Screen.SubmitClaim.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            SubmitClaimScreen(
                itemId         = itemId,
                itemTitle      = "Lost Item",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.ReviewClaims.route,
            arguments = listOf(
                navArgument("itemId")    { type = NavType.StringType },
                navArgument("itemTitle") { type = NavType.StringType; defaultValue = "Lost Item" }
            )
        ) { backStackEntry ->
            val itemId    = backStackEntry.arguments?.getString("itemId")    ?: return@composable
            val itemTitle = backStackEntry.arguments?.getString("itemTitle") ?: "Lost Item"
            ReviewClaimsScreen(
                itemId         = itemId,
                itemTitle      = itemTitle,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}