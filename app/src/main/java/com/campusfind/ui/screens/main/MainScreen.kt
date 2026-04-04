package com.campusfind.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.campusfind.ui.screens.home.HomeScreen
import com.campusfind.ui.screens.notifications.NotificationViewModel
import com.campusfind.ui.screens.profile.UserProfileScreen
import com.campusfind.ui.screens.smarthistory.SmartHistoryScreen
import com.campusfind.ui.theme.ModernAccent
import com.campusfind.ui.theme.ModernLost

private enum class MainTab(val label: String, val icon: String) {
    HOME("Home", "🏠"),
    HISTORY("History", "📊"),
    PROFILE("Profile", "👤")
}

@Composable
fun MainScreen(
    onNavigateToAddItem: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    currentUserName: String,
    currentUserEmail: String,
    notifViewModel: NotificationViewModel
) {
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }
    val unreadCount by notifViewModel.unreadCount.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0E0D17),
                tonalElevation = 0.dp,
                modifier = Modifier.shadow(16.dp)
            ) {
                MainTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick  = { selectedTab = tab },
                        icon = {
                            Box {
                                Text(tab.icon, fontSize = 20.sp)
                                // Unread dot on Home tab
                                if (tab == MainTab.HOME && unreadCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(8.dp)
                                            .background(ModernLost, CircleShape)
                                    )
                                }
                            }
                        },
                        label = {
                            Text(
                                text       = tab.label,
                                fontSize   = 10.sp,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = ModernAccent,
                            selectedTextColor   = ModernAccent,
                            unselectedIconColor = Color.White.copy(alpha = 0.45f),
                            unselectedTextColor = Color.White.copy(alpha = 0.45f),
                            indicatorColor      = ModernAccent.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        // Pad the content area so it never hides behind the NavigationBar.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (selectedTab) {
                MainTab.HOME -> HomeScreen(
                    onNavigateToAddItem       = onNavigateToAddItem,
                    onNavigateToDetail        = onNavigateToDetail,
                    onNavigateToNotifications = onNavigateToNotifications,
                    onNavigateToProfile       = { selectedTab = MainTab.PROFILE },
                    currentUserName           = currentUserName,
                    currentUserEmail          = currentUserEmail,
                    unreadNotificationCount   = unreadCount
                )
                MainTab.HISTORY -> SmartHistoryScreen(
                    onNavigateToDetail = onNavigateToDetail
                )
                MainTab.PROFILE -> UserProfileScreen(
                    onNavigateToAddItem  = onNavigateToAddItem,
                    onNavigateToDetail   = onNavigateToDetail,
                    onNavigateToSettings = onNavigateToSettings,
                    onLogout             = onLogout
                )
            }
        }
    }
}
