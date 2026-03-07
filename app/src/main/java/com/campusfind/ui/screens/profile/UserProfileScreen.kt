package com.campusfind.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

/**
 * Modern User Profile screen matching wireframes design
 *
 * Key changes from old version:
 * - Stats cards INSIDE gradient header (not below)
 * - Compact header with better spacing
 * - Matches wireframes.html "User Profile" section exactly
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddItem: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Scaffold(
        containerColor = Color(0xFFf4f4f0)
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
                    // Gradient header with profile + stats (matching wireframes)
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF1a1228),
                                            Color(0xFF2e1f48),
                                            Color(0xFF1a2a20)
                                        )
                                    )
                                )
                                .padding(bottom = 14.dp)
                        ) {
                            // Purple orb
                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .offset(x = 0.dp, y = (-20).dp)
                                    .align(Alignment.TopEnd)
                                    .background(
                                        color = ModernAccent.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                            )

                            // Teal orb
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .offset(x = (-10).dp, y = (-10).dp)
                                    .align(Alignment.BottomStart)
                                    .background(
                                        color = ModernFound.copy(alpha = 0.22f),
                                        shape = CircleShape
                                    )
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Status bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(28.dp)
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "9:41",
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.75f),
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                    Text(
                                        text = "●●● 82%",
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.75f),
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                }

                                // Back button + Settings (floating)
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
                                            modifier = Modifier.padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
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
                                                    text = "‹",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                            Text(
                                                text = "Back",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    // Settings button (placeholder for Phase 2)
                                    Surface(
                                        onClick = { /* TODO */ },
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

                                // Avatar and name
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(90.dp)
                                            .clip(CircleShape)
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(ModernAccent, Color(0xFF5246d5))
                                                )
                                            )
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "👤",
                                            fontSize = 40.sp,
                                            color = Color.White
                                        )
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
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

                                // Stats row (inside gradient header - matching wireframes!)
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
                                        count = 0, // Phase 2 feature - will track claims you helped with
                                        label = "Helped",
                                        isHighlighted = false,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // Section header
                    item {
                        Text(
                            text = "MY ITEMS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF888888),
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                        )
                    }

                    // User's items
                    if (uiState.items.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("📭", fontSize = 48.sp)
                                    Text(
                                        text = "No items posted yet",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF888888)
                                    )
                                    Text(
                                        text = "Report a lost item to get started",
                                        fontSize = 12.sp,
                                        color = Color(0xFFaaaaaa)
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    Button(
                                        onClick = onNavigateToAddItem,
                                        colors = ButtonDefaults.buttonColors(containerColor = ModernAccent),
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
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = count.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = if (isHighlighted) ModernFound else Color.White,
                style = LocalTextStyle.current.copy(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.3f),
                        offset = Offset(0f, 1f),
                        blurRadius = 4f
                    )
                )
            )
            Text(
                text = label.uppercase(),
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 0.05.sp
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
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(width = 1.5.dp, color = Color(0xFFebebeb)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Gradient thumbnail
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF2a1838),
                                Color(0xFF3e2f58),
                                Color(0xFF2a3830)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📷",
                    fontSize = 24.sp,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1a1a2e),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (item.status) {
                            ItemStatus.LOST -> ModernLost.copy(alpha = 0.15f)
                            ItemStatus.FOUND -> ModernFound.copy(alpha = 0.15f)
                        },
                        border = BorderStroke(
                            width = 1.dp,
                            color = when (item.status) {
                                ItemStatus.LOST -> ModernLost.copy(alpha = 0.4f)
                                ItemStatus.FOUND -> ModernFound.copy(alpha = 0.4f)
                            }
                        )
                    ) {
                        Text(
                            text = item.status.name,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when (item.status) {
                                ItemStatus.LOST -> Color(0xFFff6b6b)
                                ItemStatus.FOUND -> ModernFound
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = item.description,
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = formatTimestamp(item.reportedAt),
                    fontSize = 11.sp,
                    color = Color(0xFF888888)
                )
            }
        }
    }
}

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