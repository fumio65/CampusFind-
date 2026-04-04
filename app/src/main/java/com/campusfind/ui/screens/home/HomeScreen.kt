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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    onNavigateToNotifications: () -> Unit,
    onLogout: () -> Unit,
    currentUserName: String,
    currentUserEmail: String,
    unreadNotificationCount: Int = 0,
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
        drawerState     = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent   = {
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

                // ── Hero ───────────────────────────────────────────────────
                GradientHero(
                    itemCount             = filteredItems.size,
                    selectedFilter        = uiState.selectedFilter,
                    lostCount             = filteredItems.count { it.status == ItemStatus.LOST },
                    foundCount            = filteredItems.count { it.status == ItemStatus.FOUND },
                    searchQuery           = searchQuery,
                    onSearchQueryChanged  = { searchQuery = it },
                    onFilterChanged       = { viewModel.onFilterChanged(it) },
                    onOpenDrawer          = { scope.launch { drawerState.open() } },
                    onNavigateToProfile   = onNavigateToProfile,
                    onNavigateToNotifications = onNavigateToNotifications,
                    unreadCount           = unreadNotificationCount,
                    currentUserName       = currentUserName
                )

                // ── Content ────────────────────────────────────────────────
                when {
                    uiState.isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = ModernAccent, strokeWidth = 2.dp)
                        }
                    }
                    uiState.error != null -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(uiState.error ?: "Unknown error", color = ModernError, fontSize = 14.sp)
                        }
                    }
                    uiState.items.isEmpty() -> {
                        EmptyState(
                            icon     = "📭",
                            title    = "No items reported yet",
                            subtitle = "Be the first to report a lost item!",
                            colors   = colors
                        )
                    }
                    filteredItems.isEmpty() -> {
                        EmptyState(
                            icon     = "🔍",
                            title    = "No matches found",
                            subtitle = "Try different keywords",
                            colors   = colors
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier        = Modifier.fillMaxSize(),
                            contentPadding  = PaddingValues(
                                start  = 16.dp, end = 16.dp,
                                top    = 12.dp, bottom = 88.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                            items(items = filteredItems, key = { it.id }) { item ->
                                ModernItemCard(
                                    item         = item,
                                    reporterName = uiState.reporterNames[item.reportedBy] ?: "...",
                                    onClick      = { onNavigateToDetail(item.id) }
                                )
                            }
                        }
                    }
                }
            }

            // ── FAB ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 28.dp, end = 20.dp)
                    .shadow(12.dp, CircleShape, ambientColor = ModernAccent.copy(0.4f), spotColor = ModernAccent.copy(0.4f))
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(ModernAccent, Color(0xFF4F46E5)))),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    onClick   = onNavigateToAddItem,
                    modifier  = Modifier.fillMaxSize(),
                    color     = Color.Transparent,
                    shape     = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("+", fontSize = 28.sp, fontWeight = FontWeight.Thin, color = Color.White)
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// EMPTY STATE
// ══════════════════════════════════════════════════════════════════════════

@Composable
private fun EmptyState(icon: String, title: String, subtitle: String, colors: AppColors) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(icon, fontSize = 56.sp)
            Text(
                title, fontSize = 17.sp, fontWeight = FontWeight.Bold,
                color = colors.textPrimary, textAlign = TextAlign.Center
            )
            Text(
                subtitle, fontSize = 13.sp,
                color = colors.textMuted, textAlign = TextAlign.Center
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// HERO — redesigned with wave bottom edge and cleaner layout
// ══════════════════════════════════════════════════════════════════════════

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
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    unreadCount: Int = 0,
    currentUserName: String = ""
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF12101E), Color(0xFF1E1340), Color(0xFF0E1F18))
                )
            )
    ) {
        // Ambient glow orbs — clipped by parent so they don't bleed
        Box(modifier = Modifier.size(180.dp).offset(x = 200.dp, y = (-40).dp)
            .background(ModernAccent.copy(alpha = 0.15f), CircleShape))
        Box(modifier = Modifier.size(100.dp).offset(x = 240.dp, y = 20.dp)
            .background(ModernAccent.copy(alpha = 0.08f), CircleShape))
        Box(modifier = Modifier.size(100.dp).offset(x = (-20).dp, y = 150.dp)
            .background(ModernFound.copy(alpha = 0.08f), CircleShape))

        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            Spacer(Modifier.height(6.dp))

            // ── Top bar ────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hamburger
                IconButton36(onClick = onOpenDrawer) {
                    Text("☰", fontSize = 16.sp, color = Color.White)
                }

                Spacer(Modifier.weight(1f))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bell with badge
                    Box {
                        IconButton36(onClick = onNavigateToNotifications) {
                            Text("🔔", fontSize = 15.sp, color = Color.White)
                        }
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(16.dp)
                                    .shadow(4.dp, CircleShape)
                                    .background(ModernLost, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (unreadCount > 9) "9+" else "$unreadCount",
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Avatar — shows first letter of name
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(6.dp, CircleShape, ambientColor = ModernAccent.copy(0.5f))
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(ModernAccent, Color(0xFF4F46E5)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            onClick   = onNavigateToProfile,
                            modifier  = Modifier.fillMaxSize(),
                            color     = Color.Transparent,
                            shape     = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = currentUserName.firstOrNull()?.uppercase() ?: "U",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── App title + subtitle ───────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Black)) {
                            append("Campus")
                        }
                        withStyle(SpanStyle(color = ModernAccent, fontWeight = FontWeight.Black)) {
                            append("Find+")
                        }
                    },
                    fontSize = 28.sp,
                    style = LocalTextStyle.current.copy(
                        shadow = Shadow(Color.Black.copy(0.5f), Offset(0f, 2f), 16f)
                    )
                )
                Spacer(Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                if (itemCount > 0) ModernLost else ModernFound,
                                CircleShape
                            )
                    )
                    Text(
                        text = if (itemCount > 0) "$itemCount item${if (itemCount > 1) "s" else ""} reported on campus"
                        else "No active reports — campus is clear!",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Search bar — frosted glass ─────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.10f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChanged,
                        modifier = Modifier.weight(1f),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 14.sp, color = Color.White
                        ),
                        singleLine = true,
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    "Search lost items...",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                            }
                            inner()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        Surface(
                            onClick = { onSearchQueryChanged("") },
                            modifier = Modifier.size(24.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("✕", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Filter chips ───────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ALL chip
                FilterChipModern(
                    label    = "All",
                    count    = null,
                    isActive = selectedFilter == null,
                    activeGradient = listOf(Color.White.copy(0.25f), Color.White.copy(0.15f)),
                    activeLabelColor = Color.White,
                    onClick  = { onFilterChanged(null) }
                )
                // LOST chip
                FilterChipModern(
                    label    = "Lost",
                    count    = lostCount,
                    isActive = selectedFilter == ItemStatus.LOST,
                    activeGradient = listOf(ModernLost, Color(0xFFCC3355)),
                    activeLabelColor = Color.White,
                    onClick  = { onFilterChanged(if (selectedFilter == ItemStatus.LOST) null else ItemStatus.LOST) }
                )
                // FOUND chip
                FilterChipModern(
                    label    = "Found",
                    count    = foundCount,
                    isActive = selectedFilter == ItemStatus.FOUND,
                    activeGradient = listOf(ModernFound, Color(0xFF20B080)),
                    activeLabelColor = Color.White,
                    onClick  = { onFilterChanged(if (selectedFilter == ItemStatus.FOUND) null else ItemStatus.FOUND) }
                )
            }

            Spacer(Modifier.height(14.dp))
        }
    }
}

// ── Reusable frosted icon button ───────────────────────────────────────────

@Composable
private fun IconButton36(onClick: () -> Unit, content: @Composable () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
    ) {
        Box(contentAlignment = Alignment.Center) { content() }
    }
}

// ── Modern filter chip with gradient fill ─────────────────────────────────

@Composable
private fun FilterChipModern(
    label: String,
    count: Int?,
    isActive: Boolean,
    activeGradient: List<Color>,
    activeLabelColor: Color,
    onClick: () -> Unit
) {
    val bgBrush = if (isActive)
        Brush.linearGradient(activeGradient)
    else
        Brush.linearGradient(listOf(Color.White.copy(0.08f), Color.White.copy(0.08f)))

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .then(
                if (isActive)
                    Modifier.shadow(6.dp, RoundedCornerShape(20.dp),
                        ambientColor = activeGradient.first().copy(0.5f),
                        spotColor = activeGradient.first().copy(0.4f))
                else Modifier
            )
            .background(bgBrush)
            .clip(RoundedCornerShape(20.dp))
    ) {
        Surface(
            onClick = onClick,
            color   = Color.Transparent,
            shape   = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    label,
                    fontSize = 12.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    color = if (isActive) activeLabelColor else Color.White.copy(alpha = 0.6f)
                )
                if (count != null && count > 0) {
                    Box(
                        modifier = Modifier
                            .background(
                                Color.White.copy(alpha = if (isActive) 0.30f else 0.12f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            count.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isActive) Color.White else Color.White.copy(0.5f)
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// ITEM CARD — redesigned for premium feel
// ══════════════════════════════════════════════════════════════════════════

@Composable
fun ModernItemCard(item: LostItem, reporterName: String, onClick: () -> Unit) {
    val colors = LocalAppColors.current

    val statusColor = when (item.status) {
        ItemStatus.LOST  -> ModernLost
        ItemStatus.FOUND -> ModernFound
    }

    Card(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (colors.isDark) Color(0xFF1C1B2E) else colors.cardBg
        ),
        border    = BorderStroke(
            width = if (colors.isDark) 1.dp else 1.dp,
            color = if (colors.isDark) Color.White.copy(alpha = 0.08f) else colors.cardBorder
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (colors.isDark) 0.dp else 4.dp
        )
    ) {
        Column {
            // ── Photo / Placeholder ────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                val hasPhoto  = !item.photoUri.isNullOrBlank()
                val photoFile = if (hasPhoto) remember(item.photoUri) { File(item.photoUri!!) } else null

                if (hasPhoto && photoFile?.exists() == true) {
                    Image(
                        painter          = rememberAsyncImagePainter(photoFile),
                        contentDescription = "Item photo",
                        modifier         = Modifier.fillMaxSize().clip(
                            RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        ),
                        contentScale     = ContentScale.Crop
                    )
                    // Bottom scrim for readability
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                0.6f to Color.Transparent,
                                1f to Color.Black.copy(alpha = 0.5f)
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

                // ── Status badge ───────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(statusColor.copy(0.85f), statusColor.copy(0.7f))
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(5.dp).background(Color.White, CircleShape))
                        Text(
                            item.status.name,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // ── Content ────────────────────────────────────────────────────
            Column(modifier = Modifier.padding(14.dp)) {

                Text(
                    item.title,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = colors.textPrimary,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    item.description,
                    fontSize   = 12.sp,
                    color      = colors.textSecondary,
                    lineHeight = 17.sp,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(12.dp))

                // ── Bottom row: pills + reporter ───────────────────────────
                Row(
                    modifier                = Modifier.fillMaxWidth(),
                    horizontalArrangement   = Arrangement.SpaceBetween,
                    verticalAlignment       = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MetaPill(
                            icon = "📍",
                            text = if (!item.location.isNullOrBlank()) item.location else "Campus"
                        )
                        MetaPill(icon = "🕐", text = formatTimestamp(item.reportedAt))
                    }

                    // Reporter — constrained so it never overflows
                    Row(
                        modifier = Modifier.widthIn(max = 100.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(
                                    if (colors.isDark) ModernAccent.copy(alpha = 0.25f)
                                    else colors.avatarBg
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = reporterName.firstOrNull()?.uppercase() ?: "?",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (colors.isDark) ModernAccent else colors.textMuted
                            )
                        }
                        Text(
                            reporterName,
                            fontSize  = 10.sp,
                            color     = colors.textMuted,
                            maxLines  = 1,
                            overflow  = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// GRADIENT PLACEHOLDER
// ══════════════════════════════════════════════════════════════════════════

@Composable
fun GradientPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.linearGradient(
                    listOf(Color(0xFF1A1030), Color(0xFF2A2050), Color(0xFF102820))
                )
            )
        )
        // Glow orbs
        Box(modifier = Modifier.size(120.dp).offset(x = 180.dp, y = (-30).dp)
            .background(ModernAccent.copy(alpha = 0.12f), CircleShape))
        Box(modifier = Modifier.size(80.dp).offset(x = 200.dp, y = (-10).dp)
            .background(ModernAccent.copy(alpha = 0.08f), CircleShape))
        Box(modifier = Modifier.size(90.dp).offset(x = (-10).dp, y = 80.dp)
            .background(ModernFound.copy(alpha = 0.08f), CircleShape))

        // Placeholder — minimal, no boxy container
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("📷", fontSize = 36.sp, color = Color.White.copy(alpha = 0.35f))
            Text(
                "No photo",
                fontSize      = 11.sp,
                color         = Color.White.copy(alpha = 0.35f),
                fontWeight    = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// META PILL
// ══════════════════════════════════════════════════════════════════════════

@Composable
fun MetaPill(icon: String, text: String) {
    val colors = LocalAppColors.current
    Surface(
        shape  = RoundedCornerShape(10.dp),
        color  = colors.pillBg,
        border = BorderStroke(1.dp, colors.pillBorder)
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 10.sp)
            Text(text, fontSize = 10.sp, color = colors.textSecondary, fontWeight = FontWeight.Medium)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// HELPERS
// ══════════════════════════════════════════════════════════════════════════

fun formatTimestamp(millis: Long): String {
    val now   = System.currentTimeMillis()
    val diff  = now - millis
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days  = TimeUnit.MILLISECONDS.toDays(diff)
    return when {
        hours < 1  -> "Just now"
        hours < 24 -> "$hours hr${if (hours > 1) "s" else ""} ago"
        days < 7   -> "$days day${if (days > 1) "s" else ""} ago"
        else       -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(millis))
    }
}