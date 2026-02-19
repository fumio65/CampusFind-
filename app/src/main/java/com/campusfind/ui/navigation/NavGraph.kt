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
import com.campusfind.ui.screens.home.HomeScreen
import com.campusfind.ui.screens.login.LoginScreen
import com.campusfind.ui.screens.register.RegisterScreen
import com.campusfind.ui.screens.settings.SettingsScreen

/**
 * FILE: app/src/main/java/com/campusfind/ui/navigation/NavGraph.kt
 *
 * Navigation graph with auth guard — COMPLETE for MCO 1.
 *
 * All routes wired ✅:
 * - Login ✅
 * - Register ✅
 * - Home ✅
 * - AddItem ✅
 * - Detail ✅ (TASK-114)
 * - Settings ✅
 *
 * See: DEC-013 (Single Activity), TASK-110, TASK-112, TASK-113, TASK-114, TASK-123
 */
@Composable
fun CampusFindNavGraph(
    navController: NavHostController,
    sessionManager: SessionManager
) {
    // Determine start destination based on login state
    val startDestination = remember(sessionManager.isLoggedIn) {
        if (sessionManager.isLoggedIn) {
            Screen.Home.route
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
                }
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
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}