package com.campusfind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.ui.navigation.CampusFindNavGraph
import com.campusfind.ui.theme.CampusFindTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * FILE: app/src/main/java/com/campusfind/MainActivity.kt
 *
 * Single Activity for CampusFind+ (DEC-013).
 *
 * Why @AndroidEntryPoint:
 * - Required for Hilt to inject SessionManager into this Activity
 * - Also enables hiltViewModel() to work in all child Composables
 *
 * Why @Inject SessionManager:
 * - NavGraph needs SessionManager to determine the startDestination
 * - Hilt provides it automatically via field injection
 * - lateinit var allows Hilt to set it before onCreate() runs
 *
 * Navigation flow:
 * - rememberNavController() creates a NavController
 * - CampusFindNavGraph receives it + SessionManager
 * - NavGraph decides: logged in → Home, not logged in → Login
 *
 * See: DEC-013 (Single Activity), DEC-022 (Hilt), TASK-110
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampusFindTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    CampusFindNavGraph(
                        navController = navController,
                        sessionManager = sessionManager
                    )
                }
            }
        }
    }
}