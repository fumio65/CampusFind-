package com.campusfind.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.LostItem
import com.campusfind.ui.components.NavigationDrawer
import com.campusfind.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
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
    val colors = LocalAppColors.current
    var searchQuery by remember { mutableStateOf("") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val filteredItems = remember(uiState.items, searchQuery) {
        if (searchQuery.isBlank()) uiState.items
        else uiState.items.filter { item ->
            item.title.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawer(
                currentUserName      = currentUserName,
                currentUserEmail     = currentUserEmail,
                onNavigateToProfile  = onNavigateToProfile,
                onNavigateToSmartHistory = onNavigateToSmartHistory,
                onNavigateToSettings = onNavigateToSettings,
                onLogout             = onLogout,
                onDismiss            = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.screenBg)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                GradientHero(
                    itemCount            = filteredItems.size,
                    selectedFilter       = uiState.selectedFilter,
                    lostCount            = filteredItems.count { it.status == ItemStatus.LOST },
                    foundCount           = filteredItems.count { it.status == ItemStatus.FOUND },
                    searchQuery          = searchQuery,
                    onSearchQueryChanged = { searchQuery = it },
                    onFilterChanged      = { viewModel.onFilterChanged(it) },
                    onOpenDrawer         = { scope.launch { drawerState.open() } },
                    onNavigateToProfile  = onNavigateToProfile
                )

                when {
                    uiState.isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = ModernAccent)
                        }
                    }
                    uiState.error != null -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(uiState.error ?: "Unknown error", color = ModernError, fontSize = 14.sp)
                        }
                    }
                    uiState.items.isEmpty() -> {
                        EmptyState(
                            icon    = "📭",
                            title   = "No items found",
                            subtitle = "Be the first to report!",
                            colors  = colors
                        )
                    }
                    filteredItems.isEmpty() -> {
                        EmptyState(
                            icon    = "🔍",
                            title   = "No matches found",
                            subtitle = "Try a different search",
                            colors  = colors
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 14.dp, end = 14.dp,
                                top = 14.dp, bottom = 80.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(items = filteredItems, key = { it.id }) { item ->
                                ModernItemCard(
                                    item         = item,
                                    reporterName = uiState.reporterNames[item.reportedBy] ?: "Loading...",
                                    onClick      = { onNavigateToDetail(item.id) }
                                )
                            }
                        }
                    }
                }
            }

            // FAB
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 24.dp, end = 18.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(ModernAccent, Color(0xFF5246d5)))
                    )
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    onClick = onNavigateToAddItem,
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent,
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("+", fontSize = 26.sp, fontWeight = FontWeight.Light, color = Color.White)
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// EMPTY STATE
// ──────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(icon: String, title: String, subtitle: String, colors: AppColors) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(icon, fontSize = 48.sp)
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textMuted)
            Text(subtitle, fontSize = 12.sp, color = colors.textMuted)
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// GRADIENT HERO — always dark, unchanged between light and dark mode
// ──────────────────────────────────────────────────────────────────────────

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
                Brush.linearGradient(
                    colors = listOf(Color(0xFF1a1228), Color(0xFF2e1f48), Color(0xFF1a2a20))
                )
            )
    ) {
        // Ambient orbs
        Box(modifier = Modifier.size(160.dp).offset(x = (-30).dp, y = (-30).dp)
            .align(Alignment.TopEnd).background(ModernAccent.copy(alpha = 0.08f), CircleShape))
        Box(modifier = Modifier.size(140.dp).offset(x = (-20).dp, y = (-20).dp)
            .align(Alignment.TopEnd).background(ModernAccent.copy(alpha = 0.15f), CircleShape))
        Box(modifier = Modifier.size(120.dp).offset(x = (-10).dp, y = (-10).dp)
            .align(Alignment.TopEnd).background(ModernAccent.copy(alpha = 0.22f), CircleShape))
        Box(modifier = Modifier.size(120.dp).offset(x = (-20).dp, y = 20.dp)
            .align(Alignment.BottomStart).background(ModernFound.copy(alpha = 0.08f), CircleShape))
        Box(modifier = Modifier.size(100.dp).offset(x = (-10).dp, y = 10.dp)
            .align(Alignment.BottomStart).background(ModernFound.copy(alpha = 0.15f), CircleShape))
        Box(modifier = Modifier.size(80.dp)
            .align(Alignment.BottomStart).background(ModernFound.copy(alpha = 0.2f), CircleShape))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // Status bar spacer — pushes content below system status bar
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            Spacer(Modifier.height(8.dp))

            // Top nav row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onOpenDrawer, modifier = Modifier.size(36.dp), shape = CircleShape,
                    color = Color.White.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("☰", fontSize = 15.sp, color = Color.White)
                    }
                }

                Spacer(Modifier.weight(1f))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        onClick = {}, modifier = Modifier.size(36.dp), shape = CircleShape,
                        color = Color.White.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🔔", fontSize = 15.sp, color = Color.White)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(ModernAccent, Color(0xFF5246d5)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            onClick = onNavigateToProfile,
                            modifier = Modifier.fillMaxSize(),
                            color = Color.Transparent,
                            shape = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("👤", fontSize = 14.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Title
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = buildAnnotatedString {
                        append("CampusFind")
                        withStyle(SpanStyle(color = ModernAccent)) { append("+") }
                    },
                    fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White,
                    style = LocalTextStyle.current.copy(
                        shadow = Shadow(Color.Black.copy(alpha = 0.4f), Offset(0f, 2f), 12f)
                    )
                )
                Spacer(Modifier.height(2.dp))
                Text("📍 $itemCount items reported on campus",
                    fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
            }

            // Search bar
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.14f),
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.22f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🔍", fontSize = 15.sp, color = Color.White.copy(alpha = 0.55f))
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery, onValueChange = onSearchQueryChanged,
                        modifier = Modifier.weight(1f),
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, color = Color.White),
                        singleLine = true,
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) {
                                Text("Search lost items...", fontSize = 13.sp, color = Color.White.copy(alpha = 0.5f))
                            }
                            inner()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        Surface(
                            onClick = { onSearchQueryChanged("") }, modifier = Modifier.size(28.dp),
                            shape = CircleShape, color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("✕", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Filter pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                FilterPill("LOST", lostCount, selectedFilter == ItemStatus.LOST, ModernLost) {
                    onFilterChanged(if (selectedFilter == ItemStatus.LOST) null else ItemStatus.LOST)
                }
                FilterPill("FOUND", foundCount, selectedFilter == ItemStatus.FOUND, ModernFound) {
                    onFilterChanged(if (selectedFilter == ItemStatus.FOUND) null else ItemStatus.FOUND)
                }
                FilterPill("ALL", null, selectedFilter == null, Color.White) {
                    onFilterChanged(null)
                }
            }
        }
    }
}

@Composable
fun FilterPill(text: String, count: Int?, isActive: Boolean, activeColor: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick, shape = RoundedCornerShape(20.dp),
        color = if (isActive) activeColor.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.12f),
        border = BorderStroke(1.5.dp, if (isActive) activeColor.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "● $text", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
                color = if (isActive) when (text) {
                    "LOST" -> Color(0xFFff8099)
                    "FOUND" -> Color(0xFF5fead4)
                    else -> Color.White
                } else Color.White.copy(alpha = 0.7f)
            )
            if (count != null) {
                Text(
                    count.toString(), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold,
                    color = if (isActive) Color.White else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = if (isActive) 0.25f else 0.15f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// ITEM CARD — premium dark/light adaptive design
// ──────────────────────────────────────────────────────────────────────────

@Composable
fun ModernItemCard(item: LostItem, reporterName: String, onClick: () -> Unit) {
    val colors = LocalAppColors.current

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
        border = BorderStroke(
            width = if (colors.isDark) 1.dp else 1.5.dp,
            color = colors.cardBorder
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (colors.isDark) 0.dp else 3.dp
        )
    ) {
        Column {
            // ── Photo section ──────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth().height(190.dp)) {
                val hasPhoto = !item.photoUri.isNullOrBlank()
                val photoFile = if (hasPhoto) remember(item.photoUri) { File(item.photoUri!!) } else null

                if (hasPhoto && photoFile?.exists() == true) {
                    Image(
                        painter = rememberAsyncImagePainter(photoFile),
                        contentDescription = "Item photo",
                        modifier = Modifier.fillMaxSize().clip(
                            RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        ),
                        contentScale = ContentScale.Crop
                    )
                    // gradient overlay for text readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f))
                                )
                            )
                    )
                } else {
                    GradientPlaceholder(
                        modifier = Modifier.fillMaxSize().clip(
                            RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        )
                    )
                }

                // Status badge
                Surface(
                    modifier = Modifier.padding(10.dp).align(Alignment.TopStart),
                    shape = RoundedCornerShape(10.dp),
                    color = when (item.status) {
                        ItemStatus.LOST  -> ModernLost.copy(alpha = 0.22f)
                        ItemStatus.FOUND -> ModernFound.copy(alpha = 0.22f)
                    },
                    border = BorderStroke(1.5.dp, when (item.status) {
                        ItemStatus.LOST  -> ModernLost.copy(alpha = 0.7f)
                        ItemStatus.FOUND -> ModernFound.copy(alpha = 0.7f)
                    })
                ) {
                    Text(
                        "● ${item.status.name}",
                        fontSize = 9.sp, fontWeight = FontWeight.ExtraBold,
                        color = when (item.status) {
                            ItemStatus.LOST  -> Color(0xFFff8099)
                            ItemStatus.FOUND -> Color(0xFF5fead4)
                        },
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            // ── Content section ────────────────────────────────────────────
            Column(modifier = Modifier.padding(14.dp)) {

                // Title
                Text(
                    item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                // Description
                Text(
                    item.description,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    lineHeight = 17.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(10.dp))

                // Meta row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Location + time pills
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MetaPill(
                            icon = "📍",
                            text = if (!item.location.isNullOrBlank()) item.location else "Campus"
                        )
                        MetaPill(icon = "🕐", text = formatTimestamp(item.reportedAt))
                    }

                    // Reporter
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    if (colors.isDark) ModernAccent.copy(alpha = 0.2f)
                                    else colors.avatarBg,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👤", fontSize = 10.sp)
                        }
                        Text(
                            reporterName,
                            fontSize = 10.sp,
                            color = colors.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// GRADIENT PLACEHOLDER
// ──────────────────────────────────────────────────────────────────────────

@Composable
fun GradientPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.linearGradient(listOf(Color(0xFF2a1838), Color(0xFF3e2f58), Color(0xFF2a3830)))
        ))
        // Orbs
        Box(modifier = Modifier.size(100.dp).offset(x = 20.dp, y = (-20).dp)
            .align(Alignment.TopEnd).background(ModernAccent.copy(alpha = 0.08f), CircleShape))
        Box(modifier = Modifier.size(80.dp).offset(x = 10.dp, y = (-10).dp)
            .align(Alignment.TopEnd).background(ModernAccent.copy(alpha = 0.15f), CircleShape))
        Box(modifier = Modifier.size(60.dp)
            .align(Alignment.TopEnd).background(ModernAccent.copy(alpha = 0.2f), CircleShape))
        Box(modifier = Modifier.size(80.dp).offset(x = 20.dp, y = 20.dp)
            .align(Alignment.BottomStart).background(ModernFound.copy(alpha = 0.06f), CircleShape))
        Box(modifier = Modifier.size(60.dp).offset(x = 10.dp, y = 10.dp)
            .align(Alignment.BottomStart).background(ModernFound.copy(alpha = 0.12f), CircleShape))
        Box(modifier = Modifier.size(40.dp)
            .align(Alignment.BottomStart).background(ModernFound.copy(alpha = 0.15f), CircleShape))
        Box(modifier = Modifier.align(Alignment.Center), contentAlignment = Alignment.Center) {
            Text("📷", fontSize = 44.sp, color = Color.White.copy(alpha = 0.25f))
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// META PILL
// ──────────────────────────────────────────────────────────────────────────

@Composable
fun MetaPill(icon: String, text: String) {
    val colors = LocalAppColors.current
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = colors.pillBg,
        border = BorderStroke(1.dp, colors.pillBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 10.sp)
            Text(text, fontSize = 10.sp, color = colors.textSecondary)
        }
    }
}

fun formatTimestamp(millis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - millis
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    return when {
        hours < 1  -> "Just now"
        hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
        days < 7   -> "$days day${if (days > 1) "s" else ""} ago"
        else       -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(millis))
    }
}