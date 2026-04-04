package com.campusfind

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.ui.navigation.CampusFindNavGraph
import com.campusfind.ui.theme.CampusFindTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Collect dark mode StateFlow — recomposes CampusFindTheme instantly
            // when SettingsViewModel calls sessionManager.setDarkMode()
            val isDarkMode by sessionManager.isDarkModeFlow.collectAsStateWithLifecycle()

            CampusFindTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Request POST_NOTIFICATIONS on Android 13+ at first launch.
                    // The system only shows the dialog once — after that the user
                    // manages it via the phone's app settings. No in-app toggle needed.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val permissionLauncher = rememberLauncherForActivityResult(
                            ActivityResultContracts.RequestPermission()
                        ) { /* result handled by system — nothing to do here */ }

                        LaunchedEffect(Unit) {
                            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                                != PackageManager.PERMISSION_GRANTED) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }

                    val navController = rememberNavController()
                    CampusFindNavGraph(
                        navController  = navController,
                        sessionManager = sessionManager
                    )
                }
            }
        }
    }
}