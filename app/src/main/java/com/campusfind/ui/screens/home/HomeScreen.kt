package com.campusfind.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.LostItem
import com.campusfind.ui.components.NavigationDrawer
import com.campusfind.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun HomeScreen(
    onNavigateToAddItem: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSmartHistory: () -> Unit,
    onLogout: () -> Unit,
    currentUserName: String,
    currentUserEmail: String,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val filteredItems = remember(uiState.items, searchQuery) {
        if (searchQuery.isBlank()) {
            uiState.items
        } else {
            uiState.items.filter { item ->
                item.title.contains(searchQuery, ignoreCase = true) ||
                        item.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawer(
                currentUserName = currentUserName,
                currentUserEmail = currentUserEmail,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToSmartHistory = onNavigateToSmartHistory,
                onNavigateToSettings = onNavigateToSettings,
                onLogout = onLogout,
                onDismiss = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFf4f4f0))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                GradientHero(
                    itemCount = filteredItems.size,
                    selectedFilter = uiState.selectedFilter,
                    lostCount = filteredItems.count { it.status == ItemStatus.LOST },
                    foundCount = filteredItems.count { it.status == ItemStatus.FOUND },
                    searchQuery = searchQuery,
                    onSearchQueryChanged = { searchQuery = it },
                    onFilterChanged = { viewModel.onFilterChanged(it) },
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onNavigateToProfile = onNavigateToProfile
                )

                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = ModernAccent)
                        }
                    }

                    uiState.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.error ?: "Unknown error",
                                color = ModernError,
                                fontSize = 14.sp
                            )
                        }
                    }

                    uiState.items.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("📭", fontSize = 48.sp)
                                Text(
                                    "No items found",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF888888)
                                )
                                Text(
                                    "Be the first to report!",
                                    fontSize = 12.sp,
                                    color = Color(0xFFaaaaaa)
                                )
                            }
                        }
                    }

                    filteredItems.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🔍", fontSize = 48.sp)
                                Text(
                                    "No matches found",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF888888)
                                )
                                Text(
                                    "Try a different search",
                                    fontSize = 12.sp,
                                    color = Color(0xFFaaaaaa)
                                )
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 14.dp,
                                end = 14.dp,
                                top = 12.dp,
                                bottom = 80.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = filteredItems,
                                key = { it.id }
                            ) { item ->
                                ModernItemCard(
                                    item = item,
                                    reporterName = uiState.reporterNames[item.reportedBy] ?: "Loading...",
                                    onClick = { onNavigateToDetail(item.id) }
                                )
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = onNavigateToAddItem,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 20.dp, end = 16.dp)
                    .size(56.dp),
                containerColor = Color.Transparent,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(ModernAccent, Color(0xFF5246d5))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun GradientHero(
    itemCount: Int,
    selectedFilter: ItemStatus?,
    lostCount: Int,
    foundCount: Int,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onFilterChanged: (ItemStatus?) -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
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
    ) {
        // Ambient blur orbs (layered for blur effect)
        // Purple orb (top-right)
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = (-30).dp, y = (-30).dp)
                .align(Alignment.TopEnd)
                .background(
                    color = ModernAccent.copy(alpha = 0.08f),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(140.dp)
                .offset(x = (-20).dp, y = (-20).dp)
                .align(Alignment.TopEnd)
                .background(
                    color = ModernAccent.copy(alpha = 0.15f),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = (-10).dp, y = (-10).dp)
                .align(Alignment.TopEnd)
                .background(
                    color = ModernAccent.copy(alpha = 0.22f),
                    shape = CircleShape
                )
        )

        // Teal orb (bottom-left)
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = (-20).dp, y = 20.dp)
                .align(Alignment.BottomStart)
                .background(
                    color = ModernFound.copy(alpha = 0.08f),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(100.dp)
                .offset(x = (-10).dp, y = 10.dp)
                .align(Alignment.BottomStart)
                .background(
                    color = ModernFound.copy(alpha = 0.15f),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(x = 0.dp, y = 0.dp)
                .align(Alignment.BottomStart)
                .background(
                    color = ModernFound.copy(alpha = 0.2f),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // Status bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
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
            }

            // Top navigation bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Burger menu
                Surface(
                    onClick = onOpenDrawer,
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.12f),
                    border = BorderStroke(width = 1.dp, color = Color.White.copy(alpha = 0.18f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("☰", fontSize = 15.sp, color = Color.White)
                    }
                }

                Spacer(Modifier.weight(1f))

                // Notifications
                Surface(
                    onClick = { /* TODO: Phase 2 */ },
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.12f),
                    border = BorderStroke(width = 1.dp, color = Color.White.copy(alpha = 0.18f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🔔", fontSize = 15.sp, color = Color.White)
                    }
                }

                Spacer(Modifier.width(10.dp))

                // Profile
                Surface(
                    onClick = onNavigateToProfile,
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(ModernAccent, Color(0xFF5246d5))
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👤", fontSize = 14.sp, color = Color.White)
                    }
                }
            }

            // Title + Subtitle
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("CampusFind")
                        withStyle(SpanStyle(color = ModernAccent)) {
                            append("+")
                        }
                    },
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    style = LocalTextStyle.current.copy(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.4f),
                            offset = Offset(0f, 2f),
                            blurRadius = 12f
                        )
                    )
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "📍 $itemCount items lost today",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            // Search bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.14f),
                border = BorderStroke(width = 1.5.dp, color = Color.White.copy(alpha = 0.22f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🔍", fontSize = 15.sp, color = Color.White.copy(alpha = 0.55f))

                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChanged,
                        modifier = Modifier.weight(1f),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 13.sp,
                            color = Color.White
                        ),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search lost items...",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                            innerTextField()
                        }
                    )

                    if (searchQuery.isNotEmpty()) {
                        Surface(
                            onClick = { onSearchQueryChanged("") },
                            modifier = Modifier.size(28.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "✕",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Filter pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                FilterPill(
                    text = "LOST",
                    count = lostCount,
                    isActive = selectedFilter == ItemStatus.LOST,
                    activeColor = ModernLost,
                    onClick = {
                        onFilterChanged(if (selectedFilter == ItemStatus.LOST) null else ItemStatus.LOST)
                    }
                )

                FilterPill(
                    text = "FOUND",
                    count = foundCount,
                    isActive = selectedFilter == ItemStatus.FOUND,
                    activeColor = ModernFound,
                    onClick = {
                        onFilterChanged(if (selectedFilter == ItemStatus.FOUND) null else ItemStatus.FOUND)
                    }
                )

                FilterPill(
                    text = "ALL",
                    count = null,
                    isActive = selectedFilter == null,
                    activeColor = Color.White,
                    onClick = { onFilterChanged(null) }
                )
            }
        }
    }
}

@Composable
fun FilterPill(
    text: String,
    count: Int?,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isActive) {
            activeColor.copy(alpha = 0.3f)
        } else {
            Color.White.copy(alpha = 0.12f)
        },
        border = BorderStroke(
            width = 1.5.dp,
            color = if (isActive) {
                activeColor.copy(alpha = 0.6f)
            } else {
                Color.White.copy(alpha = 0.2f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "● $text",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isActive) {
                    when (text) {
                        "LOST" -> Color(0xFFff8099)
                        "FOUND" -> Color(0xFF5fead4)
                        else -> Color.White
                    }
                } else {
                    Color.White.copy(alpha = 0.7f)
                }
            )

            if (count != null) {
                Text(
                    text = count.toString(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isActive) Color.White else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier
                        .background(
                            color = Color.White.copy(alpha = if (isActive) 0.25f else 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
fun ModernItemCard(
    item: LostItem,
    reporterName: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(width = 1.5.dp, color = Color(0xFFebebeb)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Gradient photo hero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                // Background gradient (shown when no photo or behind photo)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF2a1838),
                                    Color(0xFF3e2f58),
                                    Color(0xFF2a3830)
                                )
                            )
                        )
                )

                // Actual photo (if available)
                // TODO: Replace with actual image loading when you add photo field to LostItem
                // Example with Coil:
                // if (!item.photoUrl.isNullOrEmpty()) {
                //     AsyncImage(
                //         model = item.photoUrl,
                //         contentDescription = item.title,
                //         modifier = Modifier.fillMaxSize(),
                //         contentScale = ContentScale.Crop
                //     )
                // } else {
                //     // Show placeholder with gradient + orbs
                // }

                // Ambient orbs (layered for blur effect) - shown when no photo
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .offset(x = 20.dp, y = (-20).dp)
                        .align(Alignment.TopEnd)
                        .background(
                            color = ModernAccent.copy(alpha = 0.08f),
                            shape = CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .offset(x = 10.dp, y = (-10).dp)
                        .align(Alignment.TopEnd)
                        .background(
                            color = ModernAccent.copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .offset(x = 0.dp, y = 0.dp)
                        .align(Alignment.TopEnd)
                        .background(
                            color = ModernAccent.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                )

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .offset(x = 20.dp, y = 20.dp)
                        .align(Alignment.BottomStart)
                        .background(
                            color = ModernFound.copy(alpha = 0.06f),
                            shape = CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .offset(x = 10.dp, y = 10.dp)
                        .align(Alignment.BottomStart)
                        .background(
                            color = ModernFound.copy(alpha = 0.12f),
                            shape = CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .offset(x = 0.dp, y = 0.dp)
                        .align(Alignment.BottomStart)
                        .background(
                            color = ModernFound.copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                )

                // Photo placeholder icon (only show when no photo)
                Box(
                    modifier = Modifier.align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📷",
                        fontSize = 48.sp,
                        color = Color.White.copy(alpha = 0.3f)
                    )
                }

                // Status badge overlay (always on top)
                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(10.dp),
                    color = when (item.status) {
                        ItemStatus.LOST -> ModernLost.copy(alpha = 0.25f)
                        ItemStatus.FOUND -> ModernFound.copy(alpha = 0.25f)
                    },
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = when (item.status) {
                            ItemStatus.LOST -> ModernLost.copy(alpha = 0.6f)
                            ItemStatus.FOUND -> ModernFound.copy(alpha = 0.6f)
                        }
                    )
                ) {
                    Text(
                        text = "● ${item.status.name}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = when (item.status) {
                            ItemStatus.LOST -> Color(0xFFff8099)
                            ItemStatus.FOUND -> ModernFound
                        },
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }
            }

            // Card content
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                // Title
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1a1a2e),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                // Description
                Text(
                    text = item.description,
                    fontSize = 11.sp,
                    color = Color(0xFF666666),
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(8.dp))

                // Meta pills
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MetaPill(icon = "📍", text = "Campus")
                    MetaPill(icon = "🕐", text = formatTimestamp(item.reportedAt))
                }

                Spacer(Modifier.height(8.dp))

                // Reporter info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(
                                    color = Color(0xFFe8e8e4),
                                    shape = RoundedCornerShape(7.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👤", fontSize = 11.sp)
                        }

                        Text(
                            text = reporterName,
                            fontSize = 10.sp,
                            color = Color(0xFF888888)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetaPill(icon: String, text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFf9f9f6),
        border = BorderStroke(width = 1.dp, color = Color(0xFFe8e8e4))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 10.sp)
            Text(text, fontSize = 10.sp, color = Color(0xFF666666))
        }
    }
}

fun formatTimestamp(millis: Long): String {
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