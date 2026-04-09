package com.campusfind.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun ConnectivityBanner(isOnline: Boolean) {
    // When coming back online, show "Back online" briefly then hide
    var showOnlineBanner by remember { mutableStateOf(false) }
    var previousState    by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(isOnline) {
        if (previousState == false && isOnline) {
            // Was offline, now online — show "Back online" for 2.5 seconds
            showOnlineBanner = true
            delay(2500)
            showOnlineBanner = false
        }
        previousState = isOnline
    }

    val showOffline = !isOnline
    val showOnline  = showOnlineBanner

    // Offline banner — stays visible until reconnected
    AnimatedVisibility(
        visible = showOffline,
        enter   = expandVertically(),
        exit    = shrinkVertically()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF1A0A0A), Color(0xFF2D1010), Color(0xFF1A0A0A))
                    )
                )
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFFF4D6D))
                )
                Text(
                    text       = "No internet connection",
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color(0xFFFF8099)
                )
            }
        }
    }

    // "Back online" banner — shows briefly after reconnecting
    AnimatedVisibility(
        visible = showOnline,
        enter   = expandVertically(),
        exit    = shrinkVertically()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF0A1A0F), Color(0xFF0D2B18), Color(0xFF0A1A0F))
                    )
                )
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF2DD4A0))
                )
                Text(
                    text       = "Back online",
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color(0xFF2DD4A0)
                )
            }
        }
    }
}