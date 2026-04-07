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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.campusfind.data.local.preferences.SessionManager
import com.campusfind.data.network.ConnectivityObserver
import com.campusfind.data.repository.FirestoreClaimRepositoryImpl
import com.campusfind.data.repository.FirestoreLostItemRepositoryImpl
import com.campusfind.data.repository.FirestoreTipRepositoryImpl
import com.campusfind.data.sync.SyncManager
import com.campusfind.domain.repository.ClaimRepository
import com.campusfind.domain.repository.LostItemRepository
import com.campusfind.ui.components.OfflineBanner
import com.campusfind.ui.navigation.CampusFindNavGraph
import com.campusfind.ui.theme.CampusFindTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var syncManager: SyncManager
    @Inject lateinit var lostItemRepository: FirestoreLostItemRepositoryImpl
    @Inject lateinit var tipRepository: FirestoreTipRepositoryImpl
    @Inject lateinit var claimRepository: FirestoreClaimRepositoryImpl
    @Inject lateinit var lostItemRepo: LostItemRepository
    @Inject lateinit var claimRepo: ClaimRepository
    @Inject lateinit var connectivityObserver: ConnectivityObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (sessionManager.isLoggedIn) {
            syncAll()
            syncManager.schedulePeriodicSync()
        }

        setContent {
            val isDarkMode by sessionManager.isDarkModeFlow.collectAsStateWithLifecycle()

            CampusFindTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = MaterialTheme.colorScheme.background
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val permissionLauncher = rememberLauncherForActivityResult(
                            ActivityResultContracts.RequestPermission()
                        ) { }
                        LaunchedEffect(Unit) {
                            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                                != PackageManager.PERMISSION_GRANTED) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }

                    val navController = rememberNavController()

                    // Column so the banner pushes content down (never blocks UI)
                    Column(modifier = Modifier.fillMaxSize()) {
                        OfflineBanner(connectivityObserver = connectivityObserver)
                        CampusFindNavGraph(
                            navController  = navController,
                            sessionManager = sessionManager
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (sessionManager.isLoggedIn) syncAll()
    }

    private fun syncAll() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                lostItemRepository.syncFromFirestore()
                val allItems      = lostItemRepo.getAllItems().first()
                val currentUserId = sessionManager.currentUserId ?: return@launch
                allItems.forEach { item ->
                    if (item.reportedBy == currentUserId) {
                        claimRepository.syncClaimsFromFirestore(item.id)
                        tipRepository.syncTipsFromFirestore(item.id)
                    } else {
                        val myClaims = claimRepo.getClaimsByItem(item.id).first()
                            .filter { it.claimerId == currentUserId }
                        if (myClaims.isNotEmpty()) {
                            claimRepository.syncClaimsFromFirestore(item.id)
                        }
                        tipRepository.syncTipsFromFirestore(item.id)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}