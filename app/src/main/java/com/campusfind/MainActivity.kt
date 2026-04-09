package com.campusfind

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
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
import com.campusfind.data.sync.SyncManager
import com.campusfind.ui.navigation.CampusFindNavGraph
import com.campusfind.ui.theme.CampusFindTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var syncManager: SyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val launcher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { }
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // FIX: Removed triggerNow() from onCreate.
        // onResume() always fires immediately after onCreate() completes,
        // so calling triggerNow() in both places caused two back-to-back
        // SyncWorker runs at startup. The first run pushed items and added
        // their IDs to pushedItemIds. The second run (from onResume) then
        // skipped pulling those same items — so the UI never got the latest
        // data from Supabase on the resume sync.
        // onResume() alone is sufficient for both cold start and foreground.

        setContent {
            val isDarkMode by sessionManager.isDarkModeFlow.collectAsStateWithLifecycle()
            CampusFindTheme(darkTheme = isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    CampusFindNavGraph(
                        navController  = navController,
                        sessionManager = sessionManager
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("SYNC_DEBUG", "MainActivity.onResume — triggerNow()")
        syncManager.triggerNow()
    }
}