package com.campusfind.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.ui.screens.login.LoginScreen
import com.campusfind.ui.screens.register.RegisterScreen
import com.campusfind.ui.screens.settings.SettingsScreen

/**
 * FILE: app/src/main/java/com/campusfind/ui/navigation/NavGraph.kt
 *
 * Navigation graph with auth guard.
 *
 * Why SessionManager is passed in:
 * - NavGraph needs to check isLoggedIn to decide the startDestination
 * - SessionManager is provided by Hilt at the MainActivity level
 * - No @Inject in Composables — dependencies must be passed from parent
 *
 * Auth guard logic:
 * - If SessionManager.isLoggedIn → startDestination = Home
 * - If NOT logged in → startDestination = Login
 * - This runs on every NavGraph recomposition (app launch, after logout)
 *
 * Why popUpTo(0) on logout:
 * - Clears the entire back stack
 * - After logout, back button cannot return to Home or Settings
 * - User stays on LoginScreen until they log in again
 *
 * Why launchSingleTop on login/register success:
 * - Prevents multiple Home screens in the back stack if user spams the button
 * - Only one instance of Home exists
 *
 * Phase 1 routes wired:
 * - Login, Register, Settings
 *
 * Phase 1 routes NOT YET WIRED (TASK-111+ needed):
 * - Home, AddItem, Detail
 * - These depend on LostItemRepository which doesn't exist yet
 * - Will be added in TASK-112 (HomeScreen), TASK-113 (AddItemScreen), TASK-114 (DetailScreen)
 *
 * See: DEC-013 (Single Activity), TASK-110, TASK-123 (complete NavGraph)
 */
@Composable
fun CampusFindNavGraph(
    navController: NavHostController,
    sessionManager: SessionManager
) {
    // Determine start destination based on login state
    val startDestination = remember(sessionManager.isLoggedIn) {
        if (sessionManager.isLoggedIn) {
            Screen.Home.route   // TODO TASK-112: Home route will be added
        } else {
            Screen.Login.route
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // ── Auth Routes ──────────────────────────────────────────────────────

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToHome = {
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

        // TODO TASK-112: Add Home composable
        // composable(Screen.Home.route) { HomeScreen(...) }

        // TODO TASK-113: Add AddItem composable
        // composable(Screen.AddItem.route) { AddItemScreen(...) }

        // TODO TASK-114: Add Detail composable
        // composable(
        //     route = Screen.Detail.route,
        //     arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        // ) { backStackEntry ->
        //     val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
        //     DetailScreen(itemId = itemId, ...)
        // }

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
                }
            )
        }
    }
}