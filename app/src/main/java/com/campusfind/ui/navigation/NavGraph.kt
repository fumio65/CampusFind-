package com.campusfind.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.campusfind.ui.screens.notifications.NotificationScreen
import com.campusfind.ui.screens.notifications.NotificationViewModel
import com.campusfind.ui.screens.onboarding.OnboardingScreen
import com.campusfind.ui.screens.profile.UserProfileScreen
import com.campusfind.ui.screens.register.RegisterScreen
import com.campusfind.ui.screens.settings.SettingsScreen
import com.campusfind.ui.screens.smarthistory.SmartHistoryScreen
import com.campusfind.ui.screens.reviewclaims.ReviewClaimsScreen
import com.campusfind.ui.screens.submitclaim.SubmitClaimScreen

@Composable
fun CampusFindNavGraph(
    navController: NavHostController,
    sessionManager: SessionManager
) {
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
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // ── Home ─────────────────────────────────────────────────────────────
        // NotificationViewModel is created HERE at the backStackEntry level,
        // not inside HomeScreen — this prevents the dual hiltViewModel() crash.
        // unreadCount is passed as a plain Int parameter to HomeScreen.

        composable(Screen.Home.route) { backStackEntry ->
            val notifViewModel: NotificationViewModel = hiltViewModel(backStackEntry)
            val unreadCount by notifViewModel.unreadCount.collectAsStateWithLifecycle()

            // Reload each time Home becomes visible so badge reflects
            // any reads done inside NotificationScreen
            androidx.compose.runtime.LaunchedEffect(Unit) {
                notifViewModel.loadNotifications()
            }

            HomeScreen(
                onNavigateToAddItem       = { navController.navigate(Screen.AddItem.route) },
                onNavigateToDetail        = { navController.navigate(Screen.Detail.createRoute(it)) },
                onNavigateToSettings      = { navController.navigate(Screen.Settings.route) },
                onNavigateToProfile       = { navController.navigate(Screen.Profile.route) },
                onNavigateToSmartHistory  = { navController.navigate(Screen.SmartHistory.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onLogout = {
                    sessionManager.clearSession()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                currentUserName             = sessionManager.currentUserName ?: "User",
                currentUserEmail            = sessionManager.currentUserEmail ?: "user@university.edu",
                unreadNotificationCount     = unreadCount
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
            route = Screen.Detail.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            DetailScreen(
                itemId                   = itemId,
                onNavigateBack           = { navController.popBackStack() },
                onNavigateToEdit         = { navController.navigate(Screen.EditItem.createRoute(it)) },
                onNavigateToSubmitClaim  = { navController.navigate(Screen.SubmitClaim.createRoute(it)) },
                onNavigateToReviewClaims = { navController.navigate(Screen.ReviewClaims.createRoute(it)) }
            )
        }

        // ── Edit Item ────────────────────────────────────────────────────────

        composable(
            route = Screen.EditItem.route,
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
                currentUserName  = sessionManager.currentUserName ?: "User",
                currentUserEmail = sessionManager.currentUserEmail ?: ""
            )
        }

        // ── Profile ──────────────────────────────────────────────────────────

        composable(Screen.Profile.route) {
            UserProfileScreen(
                onNavigateBack       = { navController.popBackStack() },
                onNavigateToAddItem  = { navController.navigate(Screen.AddItem.route) },
                onNavigateToHome     = { navController.popBackStack() },
                onNavigateToDetail   = { navController.navigate(Screen.Detail.createRoute(it)) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        // ── Smart History ────────────────────────────────────────────────────

        composable(Screen.SmartHistory.route) {
            SmartHistoryScreen(
                onNavigateBack     = { navController.popBackStack() },
                onNavigateToDetail = { navController.navigate(Screen.Detail.createRoute(it)) }
            )
        }

        // ── Claims ───────────────────────────────────────────────────────────

        composable(
            route = Screen.SubmitClaim.route,
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
            route = Screen.ReviewClaims.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            ReviewClaimsScreen(
                itemId         = itemId,
                itemTitle      = "Lost Item",
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}