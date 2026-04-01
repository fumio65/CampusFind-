package com.campusfind.ui.screens.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.campusfind.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun NotificationScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Reload on entry so persisted read state is applied immediately
    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalAppColors.current.screenBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Hero header ────────────────────────────────────────────────
            NotificationHero(
                unreadCount  = uiState.unreadCount,
                onNavigateBack = onNavigateBack,
                onMarkAllRead = { viewModel.markAllAsRead() }
            )

            // ── Content ────────────────────────────────────────────────────
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ModernAccent)
                    }
                }
                uiState.error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text("⚠️", fontSize = 48.sp)
                            Text(uiState.error ?: "Error", fontSize = 14.sp,
                                color = ModernError, textAlign = TextAlign.Center)
                            Button(
                                onClick = { viewModel.loadNotifications() },
                                colors = ButtonDefaults.buttonColors(containerColor = ModernAccent)
                            ) { Text("Retry") }
                        }
                    }
                }
                uiState.notifications.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text("🔔", fontSize = 64.sp)
                            Text("No Notifications",
                                fontSize = 18.sp, fontWeight = FontWeight.Bold,
                                color = LocalAppColors.current.textMuted)
                            Text(
                                "You're all caught up! Notifications about your items will appear here.",
                                fontSize = 13.sp, color = LocalAppColors.current.textMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = 14.dp, vertical = 12.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = uiState.notifications,
                            key   = { it.id }
                        ) { notification ->
                            NotificationCard(
                                notification = notification,
                                onClick = {
                                    // Mark as read on tap, then navigate
                                    viewModel.markAsRead(notification.id)
                                    notification.itemId?.let { onNavigateToDetail(it) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Hero ───────────────────────────────────────────────────────────────────

@Composable
private fun NotificationHero(
    unreadCount: Int,
    onNavigateBack: () -> Unit,
    onMarkAllRead: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF1a1228), Color(0xFF2e1f48), Color(0xFF1a2a20))
                )
            )
    ) {
        // Ambient orbs
        Box(modifier = Modifier.size(130.dp).offset(x = 260.dp, y = (-20).dp)
            .background(ModernAccent.copy(alpha = 0.25f), CircleShape).blur(40.dp))
        Box(modifier = Modifier.size(100.dp).offset(x = (-10).dp, y = 80.dp)
            .background(ModernFound.copy(alpha = 0.18f), CircleShape).blur(32.dp))

        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            Spacer(Modifier.height(8.dp))

            // Back + Mark all read row
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                Surface(
                    onClick = onNavigateBack,
                    shape = RoundedCornerShape(22.dp),
                    color = Color.Black.copy(alpha = 0.38f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
                ) {
                    Row(
                        modifier = Modifier.padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(20.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) { Text("‹", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White) }
                        Text("Back", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }

                // Mark all as read button
                if (unreadCount > 0) {
                    Surface(
                        onClick = onMarkAllRead,
                        shape = RoundedCornerShape(20.dp),
                        color = ModernAccent.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, ModernAccent.copy(alpha = 0.4f))
                    ) {
                        Text(
                            "Mark all read",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Title
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🔔", fontSize = 28.sp)
                    Text(
                        "Notifications",
                        fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White,
                        style = LocalTextStyle.current.copy(
                            shadow = Shadow(Color.Black.copy(alpha = 0.4f), Offset(0f, 2f), 12f)
                        )
                    )
                }
                Spacer(Modifier.height(6.dp))
                if (unreadCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ModernAccent.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, ModernAccent.copy(alpha = 0.5f))
                    ) {
                        Text(
                            "$unreadCount unread notification${if (unreadCount > 1) "s" else ""}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Text("You're all caught up ✓",
                        fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                }
            }
        }
    }
}

// ── Notification Card ──────────────────────────────────────────────────────

@Composable
private fun NotificationCard(
    notification: NotificationItem,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current

    val accentColor = when (notification.type) {
        NotificationType.ITEM_FOUND           -> ModernFound
        NotificationType.STILL_PENDING        -> ModernLost
        NotificationType.NEW_REPORT           -> ModernAccent
        NotificationType.CLAIM_RECEIVED       -> ModernAccent
        NotificationType.CLAIM_APPROVED       -> ModernFound
        NotificationType.CLAIM_REJECTED       -> ModernLost
        NotificationType.CLAIM_REPLY_RECEIVED -> Color(0xFF9C27B0)
        NotificationType.CLAIM_REPLY_TO_YOU   -> Color(0xFF9C27B0)
        NotificationType.TIP_RECEIVED         -> Color(0xFFFFB74D)
        NotificationType.TIP_REPLY_RECEIVED   -> Color(0xFFFFB74D)
    }

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (!notification.isRead && colors.isDark)
            colors.cardBg
        else if (!notification.isRead)
            Color.White
        else
            colors.cardBg.copy(alpha = if (colors.isDark) 0.6f else 1f),
        border = BorderStroke(
            width = if (!notification.isRead) 1.5.dp else 1.dp,
            color = if (!notification.isRead) accentColor.copy(alpha = 0.4f) else colors.cardBorder
        ),
        shadowElevation = if (!notification.isRead) 3.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(13.dp))
                    .then(
                        if (!notification.isRead)
                            Modifier.background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(13.dp))
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(notification.type.getIcon(), fontSize = 22.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        notification.title,
                        fontSize = 13.sp,
                        fontWeight = if (!notification.isRead) FontWeight.ExtraBold else FontWeight.Bold,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Unread dot
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(accentColor, CircleShape)
                        )
                    }
                }

                Spacer(Modifier.height(3.dp))

                Text(
                    notification.message,
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(4.dp).background(colors.divider, CircleShape))
                    Text(
                        formatNotifTimestamp(notification.timestamp),
                        fontSize = 10.sp,
                        color = colors.textMuted
                    )
                    if (notification.itemId != null) {
                        Text("·", fontSize = 10.sp, color = colors.textMuted)
                        Text(
                            "Tap to view →",
                            fontSize = 10.sp,
                            color = accentColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ── Helpers ────────────────────────────────────────────────────────────────

private fun formatNotifTimestamp(timestamp: Long): String {
    val now   = System.currentTimeMillis()
    val diff  = now - timestamp
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days  = TimeUnit.MILLISECONDS.toDays(diff)
    return when {
        hours < 1  -> "Just now"
        hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
        days == 1L -> "Yesterday"
        days < 7   -> "$days days ago"
        else       -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}