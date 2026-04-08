package com.campusfind.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campusfind.data.network.ConnectivityObserver
import com.campusfind.ui.theme.ModernFound
import com.campusfind.ui.theme.ModernLost
import kotlinx.coroutines.delay

/**
 * OfflineBanner
 *
 * Overlays a banner at the very top of the screen (above all content).
 * - Offline  → red banner with message
 * - Back online → green flash for 2.5s then disappears
 *
 * Place inside a Box that wraps your entire screen content.
 * The banner aligns to TopCenter so it never pushes content down.
 */
@Composable
fun OfflineBanner(connectivityObserver: ConnectivityObserver) {
    val isConnected by connectivityObserver.isConnected.collectAsState(initial = true)

    var wasOffline       by remember { mutableStateOf(false) }
    var showOnlineBanner by remember { mutableStateOf(false) }

    LaunchedEffect(isConnected) {
        if (!isConnected) {
            wasOffline       = true
            showOnlineBanner = false
        } else if (wasOffline) {
            showOnlineBanner = true
            delay(2500)
            showOnlineBanner = false
            wasOffline       = false
        }
    }

    // ── Offline banner ─────────────────────────────────────────────────────
    AnimatedVisibility(
        visible = !isConnected,
        enter   = expandVertically(expandFrom = Alignment.Top),
        exit    = shrinkVertically(shrinkTowards = Alignment.Top)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFB71C1C))   // deep red — visible over any hero
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("📡", fontSize = 16.sp)
            Column {
                Text(
                    "No Internet Connection",
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )
                Text(
                    "Showing cached data. Changes won't sync until you're back online.",
                    fontSize   = 10.sp,
                    color      = Color.White.copy(alpha = 0.9f),
                    lineHeight = 14.sp
                )
            }
        }
    }

    // ── Back online flash ──────────────────────────────────────────────────
    AnimatedVisibility(
        visible = showOnlineBanner,
        enter   = expandVertically(expandFrom = Alignment.Top),
        exit    = shrinkVertically(shrinkTowards = Alignment.Top)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1B5E20))   // deep green
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("✅", fontSize = 16.sp)
            Text(
                "Back online — syncing data...",
                fontSize   = 12.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White
            )
        }
    }
}