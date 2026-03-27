package com.campusfind.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.LostItem
import com.campusfind.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import androidx.compose.ui.window.Dialog

/**
 * FULLY DYNAMIC UserProfileScreen WITH EDIT MESSENGER
 * - Achievements: only if earned
 * - Messenger: always shown, with Add/Edit functionality
 * - Trust Score: only if user has activity
 */

// Helper functions to check if sections should be shown
private fun UserProfileUiState.hasAchievements(): Boolean {
    val foundItems = items.count { it.status == ItemStatus.FOUND }
    return items.size >= 5 || foundItems >= 3 || hasEarlyAdopterBadge()
}

private fun UserProfileUiState.hasEarlyAdopterBadge(): Boolean {
    if (joinedDate == null) return false
    val joinMonth = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(joinedDate!!))
    return joinMonth == "Jan 2026"
}

private fun UserProfileUiState.hasMessengerConnected(): Boolean {
    return messengerHandle != null && messengerHandle.isNotBlank()
}

private fun UserProfileUiState.hasTrustScoreData(): Boolean {
    return (trustScore ?: 0) > 0
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddItem: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Scaffold(
        containerColor = Color(0xFFF4F4F0)
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ModernAccent)
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("⚠️", fontSize = 48.sp)
                        Text(
                            text = uiState.error ?: "Unknown error",
                            fontSize = 14.sp,
                            color = ModernError
                        )
                        Button(
                            onClick = { viewModel.loadProfile() },
                            colors = ButtonDefaults.buttonColors(containerColor = ModernAccent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // ══════════════════════════════════════
                    // HERO SECTION (always shown)
                    // ══════════════════════════════════════
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF1a1228),
                                            Color(0xFF2e1f48),
                                            Color(0xFF1a2a20)
                                        )
                                    )
                                )
                                .padding(bottom = 14.dp)
                        ) {
                            // Purple blob
                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .offset(x = 250.dp, y = (-20).dp)
                                    .background(
                                        ModernAccent.copy(alpha = 0.3f),
                                        CircleShape
                                    )
                                    .blur(42.dp)
                            )

                            // Green blob
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .offset(x = (-10).dp, y = 100.dp)
                                    .background(
                                        ModernFound.copy(alpha = 0.22f),
                                        CircleShape
                                    )
                                    .blur(36.dp)
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Spacer(Modifier.height(8.dp))

                                // Back + Settings buttons
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp)
                                ) {
                                    // Back button
                                    Surface(
                                        onClick = onNavigateBack,
                                        shape = RoundedCornerShape(22.dp),
                                        color = Color.Black.copy(alpha = 0.38f),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                                        modifier = Modifier.align(Alignment.CenterStart)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(
                                                start = 8.dp,
                                                end = 12.dp,
                                                top = 6.dp,
                                                bottom = 6.dp
                                            ),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "‹",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                            Text(
                                                "Back",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    // Settings button
                                    Surface(
                                        onClick = onNavigateToSettings,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .align(Alignment.CenterEnd),
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("⚙", fontSize = 15.sp, color = Color.White)
                                        }
                                    }
                                }

                                Spacer(Modifier.height(20.dp))

                                // Avatar + Name
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(90.dp),
                                        shape = CircleShape,
                                        color = Color.Transparent,
                                        border = BorderStroke(4.dp, Color.White.copy(alpha = 0.2f)),
                                        shadowElevation = 16.dp
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.linearGradient(
                                                        colors = listOf(
                                                            ModernAccent,
                                                            Color(0xFF5246d5)
                                                        )
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("👤", fontSize = 40.sp)
                                        }
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = uiState.userName ?: "User",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            style = LocalTextStyle.current.copy(
                                                shadow = Shadow(
                                                    color = Color.Black.copy(alpha = 0.5f),
                                                    offset = Offset(0f, 2f),
                                                    blurRadius = 10f
                                                )
                                            )
                                        )
                                        Text(
                                            text = buildString {
                                                append("📍 Main Campus")
                                                if (uiState.joinedDate != null) {
                                                    append(" · Member since ${formatMonthYear(uiState.joinedDate!!)}")
                                                }
                                            },
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.6f)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                // Stats row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    GlassStatCard(
                                        count = uiState.items.size,
                                        label = "Posted",
                                        isHighlighted = false,
                                        modifier = Modifier.weight(1f)
                                    )
                                    GlassStatCard(
                                        count = uiState.items.count { it.status == ItemStatus.FOUND },
                                        label = "Resolved",
                                        isHighlighted = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    GlassStatCard(
                                        count = 0, // Phase 2: tips given
                                        label = "Helped",
                                        isHighlighted = false,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // ══════════════════════════════════════
                    // ACHIEVEMENTS (only if user earned any)
                    // ══════════════════════════════════════
                    if (uiState.hasAchievements()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF4F4F0))
                                    .padding(top = 14.dp, start = 14.dp, end = 14.dp)
                            ) {
                                Text(
                                    "ACHIEVEMENTS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFAAAAAA),
                                    letterSpacing = 0.6.sp,
                                    modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Helper Badge - 5+ items posted
                                    if (uiState.items.size >= 5) {
                                        AchievementBadge(
                                            emoji = "🏆",
                                            title = "Helper",
                                            subtitle = "${uiState.items.size} items posted",
                                            bgColors = listOf(Color(0xFFF0EFFF), Color(0xFFE8E6FF)),
                                            borderColor = Color(0xFFD8D5FF),
                                            titleColor = Color(0xFF6C63FF),
                                            subtitleColor = Color(0xFF9A8DC8)
                                        )
                                    }

                                    // Finder Badge - 3+ items found
                                    val foundCount = uiState.items.count { it.status == ItemStatus.FOUND }
                                    if (foundCount >= 3) {
                                        AchievementBadge(
                                            emoji = "✋",
                                            title = "Finder",
                                            subtitle = "$foundCount items found",
                                            bgColors = listOf(Color(0xFFEDFCF5), Color(0xFFD8FBED)),
                                            borderColor = Color(0xFFA8EECF),
                                            titleColor = Color(0xFF0EA870),
                                            subtitleColor = Color(0xFF5AAD88)
                                        )
                                    }

                                    // Early Adopter - joined in Jan 2026
                                    if (uiState.hasEarlyAdopterBadge()) {
                                        AchievementBadge(
                                            emoji = "⭐",
                                            title = "Early Adopter",
                                            subtitle = "Joined in Jan",
                                            bgColors = listOf(Color(0xFFFFF5E6), Color(0xFFFFE8CC)),
                                            borderColor = Color(0xFFFFD699),
                                            titleColor = Color(0xFFD4A300),
                                            subtitleColor = Color(0xFFB8900F)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ══════════════════════════════════════
                    // MESSENGER - Always shown with Add/Edit
                    // ══════════════════════════════════════
                    item {
                        var showMessengerDialog by remember { mutableStateOf(false) }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF4F4F0))
                                .padding(top = 14.dp, start = 14.dp, end = 14.dp)
                        ) {
                            Text(
                                "CONTACT INFO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFAAAAAA),
                                letterSpacing = 0.6.sp,
                                modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                            )

                            if (uiState.hasMessengerConnected()) {
                                // CONNECTED STATE - Show with edit option
                                Surface(
                                    modifier = Modifier.clickable { showMessengerDialog = true },
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.5.dp, Color(0xFFE8E8E4))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                modifier = Modifier.size(36.dp),
                                                shape = CircleShape,
                                                color = Color.Transparent
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(
                                                            Brush.linearGradient(
                                                                colors = listOf(
                                                                    Color(0xFF0084FF),
                                                                    Color(0xFF0066CC)
                                                                )
                                                            )
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("💬", fontSize = 16.sp)
                                                }
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    "Messenger",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF888888)
                                                )
                                                Text(
                                                    "@${uiState.messengerHandle}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF0084FF)
                                                )
                                            }

                                            // Edit icon
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFFF4F4F0)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .padding(6.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("✏️", fontSize = 12.sp)
                                                }
                                            }
                                        }

                                        Spacer(Modifier.height(8.dp))

                                        Text(
                                            "Finders can message you directly on Messenger after you approve their claim. Tap to edit.",
                                            fontSize = 9.sp,
                                            color = Color(0xFFAAAAAA),
                                            lineHeight = 12.sp
                                        )
                                    }
                                }
                            } else {
                                // NOT CONNECTED STATE - Show add option
                                Surface(
                                    modifier = Modifier.clickable { showMessengerDialog = true },
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.White,
                                    border = BorderStroke(
                                        width = 1.5.dp,
                                        color = Color(0xFFFFE8CC)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                modifier = Modifier.size(36.dp),
                                                shape = CircleShape,
                                                color = Color(0xFFF4F4F0)
                                            ) {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("💬", fontSize = 16.sp)
                                                }
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    "Messenger",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF888888)
                                                )
                                                Text(
                                                    "Not connected",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFCCCCCC)
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFF0084FF)
                                            ) {
                                                Text(
                                                    "Add",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(
                                                        horizontal = 12.dp,
                                                        vertical = 5.dp
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(Modifier.height(8.dp))

                                        Text(
                                            "Add your Messenger handle so finders can contact you after approval.",
                                            fontSize = 9.sp,
                                            color = Color(0xFFAAAAAA),
                                            lineHeight = 12.sp
                                        )
                                    }
                                }
                            }

                            // Messenger Dialog (shared for Add/Edit)
                            if (showMessengerDialog) {
                                MessengerDialog(
                                    currentHandle = uiState.messengerHandle,
                                    onDismiss = { showMessengerDialog = false },
                                    onSave = { handle ->
                                        viewModel.updateMessengerHandle(handle)
                                        showMessengerDialog = false
                                    }
                                )
                            }
                        }
                    }

                    // ══════════════════════════════════════
                    // TRUST SCORE (only if user has activity)
                    // ══════════════════════════════════════
                    if (uiState.hasTrustScoreData()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF4F4F0))
                                    .padding(top = 14.dp, start = 14.dp, end = 14.dp)
                            ) {
                                Text(
                                    "REPUTATION",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFAAAAAA),
                                    letterSpacing = 0.6.sp,
                                    modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                                )

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.Transparent,
                                    border = BorderStroke(1.5.dp, Color(0xFFD8D5FF))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(
                                                        Color(0xFFF0EFFF),
                                                        Color(0xFFE8E6FF)
                                                    )
                                                )
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    modifier = Modifier.size(48.dp),
                                                    shape = CircleShape,
                                                    color = Color.Transparent,
                                                    shadowElevation = 8.dp
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(
                                                                Brush.linearGradient(
                                                                    colors = listOf(
                                                                        Color(0xFF6C63FF),
                                                                        Color(0xFF5246D5)
                                                                    )
                                                                )
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            "${uiState.trustScore ?: 0}",
                                                            fontSize = 18.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = Color.White
                                                        )
                                                    }
                                                }

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        uiState.trustScoreLabel ?: "New User",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFF6C63FF)
                                                    )
                                                    Text(
                                                        uiState.trustScoreRank ?: "Get started!",
                                                        fontSize = 9.sp,
                                                        color = Color(0xFF9A8DC8)
                                                    )
                                                }
                                            }

                                            if (uiState.responseRate != null || uiState.recoveredRate != null) {
                                                Spacer(Modifier.height(10.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    if (uiState.responseRate != null) {
                                                        TrustMetric(
                                                            value = uiState.responseRate!!,
                                                            label = "Response Rate",
                                                            color = Color(0xFF2DD4A0),
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                    if (uiState.avgReplyTime != null) {
                                                        TrustMetric(
                                                            value = uiState.avgReplyTime!!,
                                                            label = "Avg Reply",
                                                            color = Color(0xFF6C63FF),
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                    if (uiState.recoveredRate != null) {
                                                        TrustMetric(
                                                            value = uiState.recoveredRate!!,
                                                            label = "Recovered",
                                                            color = Color(0xFF0EA870),
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ══════════════════════════════════════
                    // QUICK ACTIONS (always shown)
                    // ══════════════════════════════════════
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF4F4F0))
                                .padding(top = 14.dp, start = 14.dp, end = 14.dp)
                        ) {
                            Text(
                                "QUICK ACTIONS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFAAAAAA),
                                letterSpacing = 0.6.sp,
                                modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                QuickActionCard(
                                    emoji = "📢",
                                    label = "Post Item",
                                    onClick = onNavigateToAddItem,
                                    modifier = Modifier.weight(1f)
                                )
                                QuickActionCard(
                                    emoji = "🔍",
                                    label = "My Items",
                                    onClick = { /* Stay on current screen */ },
                                    modifier = Modifier.weight(1f)
                                )
                                QuickActionCard(
                                    emoji = "📋",
                                    label = "Browse All",
                                    onClick = onNavigateToHome,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // ══════════════════════════════════════
                    // YOUR ITEMS (always shown)
                    // ══════════════════════════════════════
                    item {
                        Text(
                            "YOUR ITEMS (${uiState.items.size})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFAAAAAA),
                            letterSpacing = 0.6.sp,
                            modifier = Modifier.padding(
                                top = 14.dp,
                                start = 16.dp,
                                bottom = 8.dp
                            )
                        )
                    }

                    if (uiState.items.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("📭", fontSize = 48.sp)
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        "No items posted yet",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF888888)
                                    )
                                    Text(
                                        "Report a lost item to get started",
                                        fontSize = 12.sp,
                                        color = Color(0xFFAAAAAA)
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Button(
                                        onClick = onNavigateToAddItem,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ModernAccent
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Post Item")
                                    }
                                }
                            }
                        }
                    } else {
                        items(
                            items = uiState.items,
                            key = { it.id }
                        ) { item ->
                            ProfileItemCard(
                                item = item,
                                onClick = { onNavigateToDetail(item.id) },
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                            )
                        }
                    }

                    item {
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════
// COMPOSABLE COMPONENTS
// ══════════════════════════════════════

@Composable
private fun GlassStatCard(
    count: Int,
    label: String,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (isHighlighted) {
            ModernFound.copy(alpha = 0.2f)
        } else {
            Color.White.copy(alpha = 0.12f)
        },
        border = BorderStroke(
            width = 1.5.dp,
            color = if (isHighlighted) {
                ModernFound.copy(alpha = 0.4f)
            } else {
                Color.White.copy(alpha = 0.2f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                count.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = if (isHighlighted) ModernFound else Color.White,
                style = LocalTextStyle.current.copy(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.3f),
                        offset = Offset(0f, 1f),
                        blurRadius = 4f)
                )
            )
            Text(
                label.uppercase(),
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 0.05.sp
            )
        }
    }
}

@Composable
private fun AchievementBadge(
    emoji: String,
    title: String,
    subtitle: String,
    bgColors: List<Color>,
    borderColor: Color,
    titleColor: Color,
    subtitleColor: Color
) {
    Surface(
        modifier = Modifier.width(100.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        border = BorderStroke(1.5.dp, borderColor),
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(bgColors))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(emoji, fontSize = 28.sp)
                Spacer(Modifier.height(5.dp))
                Text(
                    title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                Text(
                    subtitle,
                    fontSize = 9.sp,
                    color = subtitleColor
                )
            }
        }
    }
}

@Composable
private fun TrustMetric(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier.padding(7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                label,
                fontSize = 8.sp,
                color = Color(0xFF888888)
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    emoji: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.5.dp, Color(0xFFE8E8E4))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 24.sp)
            Spacer(Modifier.height(5.dp))
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1a1a2e)
            )
        }
    }
}

@Composable
private fun ProfileItemCard(
    item: LostItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(
            1.5.dp,
            if (item.status == ItemStatus.LOST) {
                Color(0xFFFFDEDC)
            } else {
                Color(0xFFE0FDF4)
            }
        ),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("👜", fontSize = 32.sp)

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1a1a2e),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (item.status == ItemStatus.LOST) {
                            Color(0xFFFF4D6D).copy(alpha = 0.15f)
                        } else {
                            Color(0xFF0EA870).copy(alpha = 0.15f)
                        }
                    ) {
                        Text(
                            if (item.status == ItemStatus.LOST) "● LOST" else "✓ FOUND",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (item.status == ItemStatus.LOST) {
                                Color(0xFFFF4D6D)
                            } else {
                                Color(0xFF0EA870)
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(Modifier.height(3.dp))

                Text(
                    item.description,
                    fontSize = 10.sp,
                    color = Color(0xFF666666),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    formatTimestamp(item.reportedAt),
                    fontSize = 9.sp,
                    color = Color(0xFFCCCCCC)
                )
            }
        }
    }
}

@Composable
private fun MessengerDialog(
    currentHandle: String?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val isEditMode = currentHandle != null
    var messengerHandle by remember { mutableStateOf(currentHandle ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF0084FF),
                                            Color(0xFF0066CC)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💬", fontSize = 18.sp)
                        }
                    }

                    Column {
                        Text(
                            if (isEditMode) "Edit Messenger Account" else "Add Messenger Account",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1a1a2e)
                        )
                        Text(
                            if (isEditMode) "Update your username" else "Let finders contact you",
                            fontSize = 11.sp,
                            color = Color(0xFF888888)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF0F7FF)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("ℹ️", fontSize = 14.sp)
                        Text(
                            "After you approve a claim, the finder can message you on Messenger to arrange pickup.",
                            fontSize = 10.sp,
                            color = Color(0xFF0066CC),
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    "Messenger Username",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                OutlinedTextField(
                    value = messengerHandle,
                    onValueChange = {
                        messengerHandle = it.trimStart().replace(" ", ".")
                        error = null
                    },
                    placeholder = {
                        Text(
                            "e.g., juan.delacruz",
                            fontSize = 12.sp,
                            color = Color(0xFFCCCCCC)
                        )
                    },
                    leadingIcon = {
                        Text(
                            "@",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0084FF)
                        )
                    },
                    isError = error != null,
                    supportingText = {
                        if (error != null) {
                            Text(
                                error!!,
                                fontSize = 10.sp,
                                color = ModernError
                            )
                        } else {
                            Text(
                                "Enter your Messenger username (without @)",
                                fontSize = 9.sp,
                                color = Color(0xFF888888)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0084FF),
                        unfocusedBorderColor = Color(0xFFE8E8E4),
                        errorBorderColor = ModernError,
                        cursorColor = Color(0xFF0084FF)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFE8E8E4)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF666666)
                        )
                    ) {
                        Text("Cancel", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val trimmed = messengerHandle.trim()
                            when {
                                trimmed.isEmpty() -> {
                                    error = "Username is required"
                                }
                                trimmed.length < 3 -> {
                                    error = "Username too short (min 3 characters)"
                                }
                                !trimmed.matches(Regex("^[a-zA-Z0-9._]+$")) -> {
                                    error = "Only letters, numbers, dots, and underscores allowed"
                                }
                                isEditMode && trimmed == currentHandle -> {
                                    onDismiss()
                                }
                                else -> {
                                    onSave(trimmed)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0084FF)
                        )
                    ) {
                        Text(
                            if (isEditMode) "Update" else "Save",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════
// UTILITY FUNCTIONS
// ══════════════════════════════════════

private fun formatMonthYear(millis: Long): String {
    return SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(millis))
}

private fun formatTimestamp(millis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - millis
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        hours < 1 -> "Just now"
        hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
        days < 7 -> "$days day${if (days > 1) "s" else ""} ago"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(millis))
    }
}