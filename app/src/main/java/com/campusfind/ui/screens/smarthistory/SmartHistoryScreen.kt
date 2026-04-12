package com.campusfind.ui.screens.smarthistory

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.campusfind.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartHistoryScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: SmartHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors  = LocalAppColors.current

    LaunchedEffect(Unit) { viewModel.loadHistory() }

    // Pull-to-refresh — waits for data to land before hiding indicator
    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(pullRefreshState.isRefreshing) {
        if (pullRefreshState.isRefreshing) {
            val snapCount    = viewModel.uiState.value.activities.size
            val snapResolved = viewModel.uiState.value.resolvedItems
            viewModel.loadHistory()
            var elapsed = 0
            while (elapsed < 5000) {
                kotlinx.coroutines.delay(200)
                elapsed += 200
                val current = viewModel.uiState.value
                if (current.activities.size != snapCount ||
                    current.resolvedItems != snapResolved) {
                    break
                }
            }
            pullRefreshState.endRefresh()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.screenBg)
            .nestedScroll(pullRefreshState.nestedScrollConnection)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            SmartHistoryHero(
                totalItems  = uiState.totalItemsPosted,
                foundItems  = uiState.itemsFound,
                successRate = uiState.successRate
            )

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ModernAccent, strokeWidth = 2.dp)
                    }
                }
                uiState.error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("⚠️", fontSize = 48.sp)
                            Text(uiState.error ?: "Error", fontSize = 14.sp, color = ModernError)
                            Button(
                                onClick = { viewModel.loadHistory() },
                                colors  = ButtonDefaults.buttonColors(containerColor = ModernAccent)
                            ) { Text("Retry") }
                        }
                    }
                }
                uiState.totalItemsPosted == 0 -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text("📊", fontSize = 64.sp)
                            Text("No Activity Yet", fontSize = 18.sp,
                                fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            Text(
                                "Your activity history will appear here once you start posting items",
                                fontSize = 13.sp, color = colors.textMuted, textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier       = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        item {
                            InsightsDashboard(
                                avgResolutionDays = uiState.averageResolutionDays,
                                mostActiveDay     = uiState.mostActiveDay,
                                recentPosts       = uiState.recentPosts,
                                pendingItems      = uiState.pendingItems,
                                resolvedItems     = uiState.resolvedItems,
                                colors            = colors
                            )
                        }
                        item {
                            FilterSection(
                                selectedFilter    = uiState.selectedFilter,
                                selectedDateRange = uiState.selectedDateRange,
                                customDate        = uiState.customDate,
                                onFilterChanged   = { viewModel.onFilterChanged(it) },
                                onDateRangeChanged = { viewModel.onDateRangeChanged(it) },
                                activityCount     = uiState.filteredActivities.size,
                                colors            = colors
                            )
                        }
                        if (uiState.filteredActivities.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No activities match your filters",
                                        fontSize = 13.sp, color = colors.textMuted)
                                }
                            }
                        } else {
                            items(items = uiState.filteredActivities, key = { it.id }) { activity ->
                                ActivityCard(
                                    activity = activity,
                                    colors   = colors,
                                    onClick  = { activity.itemId?.let { onNavigateToDetail(it) } }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Pull-to-refresh indicator — always on top
        PullToRefreshContainer(
            state          = pullRefreshState,
            modifier       = Modifier.align(Alignment.TopCenter),
            containerColor = if (colors.isDark) Color(0xFF1C1B2E) else Color.White,
            contentColor   = ModernAccent
        )

        if (uiState.showDatePicker) {
            DatePickerBottomSheet(
                initialDate    = uiState.customDate,
                onDateSelected = { viewModel.onCustomDateSelected(it) },
                onDismiss      = { viewModel.onDatePickerDismissed() },
                colors         = colors
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// HERO
// ══════════════════════════════════════════════════════════════════════════

@Composable
fun SmartHistoryHero(totalItems: Int, foundItems: Int, successRate: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(Brush.linearGradient(
                colors = listOf(Color(0xFF12101E), Color(0xFF1E1340), Color(0xFF0E1F18))))
    ) {
        Box(modifier = Modifier.size(160.dp).offset(x = 220.dp, y = (-40).dp)
            .background(ModernAccent.copy(alpha = 0.15f), CircleShape))
        Box(modifier = Modifier.size(100.dp).offset(x = (-20).dp, y = 100.dp)
            .background(ModernFound.copy(alpha = 0.10f), CircleShape))

        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            Spacer(Modifier.height(16.dp))
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📊", fontSize = 26.sp)
                    Text("Smart History", fontSize = 26.sp, fontWeight = FontWeight.Black,
                        color = Color.White,
                        style = LocalTextStyle.current.copy(
                            shadow = Shadow(Color.Black.copy(0.4f), Offset(0f, 2f), 12f)))
                }
                Spacer(Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickStat("$totalItems", "Items",    Color.White,   Modifier.weight(1f))
                    QuickStat("$foundItems", "Found",    ModernFound,   Modifier.weight(1f))
                    QuickStat("$successRate%", "Success", ModernAccent, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuickStat(value: String, label: String, color: Color, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = color,
            style = LocalTextStyle.current.copy(
                shadow = Shadow(Color.Black.copy(0.3f), Offset(0f, 1f), 4f)))
        Text(label.uppercase(), fontSize = 9.sp,
            color = Color.White.copy(alpha = 0.55f), letterSpacing = 0.5.sp)
    }
}

// ══════════════════════════════════════════════════════════════════════════
// INSIGHTS
// ══════════════════════════════════════════════════════════════════════════

@Composable
private fun InsightsDashboard(
    avgResolutionDays: Int, mostActiveDay: String?,
    recentPosts: Int, pendingItems: Int, resolvedItems: Int, colors: AppColors
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("INSIGHTS", fontSize = 10.sp, fontWeight = FontWeight.Bold,
            color = colors.textMuted, letterSpacing = 0.6.sp,
            modifier = Modifier.padding(start = 2.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            InsightCard(icon = "⚡",
                value = if (avgResolutionDays == 0) "N/A" else "$avgResolutionDays days",
                label = "Avg Resolution", accentColor = Color(0xFFFFB74D),
                colors = colors, modifier = Modifier.weight(1f))
            InsightCard(icon = "📢", value = "$recentPosts", label = "Last 7 Days",
                accentColor = ModernAccent, colors = colors, modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            InsightCard(icon = "⏳", value = "$pendingItems", label = "Pending",
                accentColor = ModernLost, colors = colors, modifier = Modifier.weight(1f))
            InsightCard(icon = "✅", value = "$resolvedItems", label = "Resolved",
                accentColor = ModernFound, colors = colors, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun InsightCard(
    icon: String, value: String, label: String,
    accentColor: Color, colors: AppColors, modifier: Modifier
) {
    Box(modifier = modifier.clip(RoundedCornerShape(14.dp))
        .background(accentColor.copy(alpha = if (colors.isDark) 0.10f else 0.07f))
        .padding(horizontal = 12.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()) {
            Text(icon, fontSize = 20.sp)
            Column {
                Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = accentColor)
                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = colors.textMuted)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// FILTER SECTION
// ══════════════════════════════════════════════════════════════════════════

@Composable
private fun FilterSection(
    selectedFilter: HistoryFilter, selectedDateRange: DateRange,
    customDate: CustomDate?, onFilterChanged: (HistoryFilter) -> Unit,
    onDateRangeChanged: (DateRange) -> Unit, activityCount: Int, colors: AppColors
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (selectedDateRange == DateRange.CUSTOM && customDate != null) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("ACTIVITY ($activityCount)", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    color = colors.textMuted, letterSpacing = 0.6.sp)
                Surface(shape = RoundedCornerShape(12.dp), color = ModernAccent.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, ModernAccent.copy(alpha = 0.3f))) {
                    Text("📅 ${customDate.getLabel()}", fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold, color = ModernAccent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                }
            }
        } else {
            Text("ACTIVITY ($activityCount)", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                color = colors.textMuted, letterSpacing = 0.6.sp,
                modifier = Modifier.padding(horizontal = 16.dp))
        }

        DateRangeSegmented(selectedDateRange = selectedDateRange,
            onDateRangeChanged = onDateRangeChanged, colors = colors)

        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            HistoryFilter.values().forEach { filter ->
                val isSelected = selectedFilter == filter
                Surface(onClick = { onFilterChanged(filter) }, shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) ModernAccent else colors.cardBg,
                    border = BorderStroke(1.dp, if (isSelected) ModernAccent else colors.cardBorder),
                    shadowElevation = if (isSelected) 3.dp else 0.dp) {
                    Text(text = filter.getLabel(), fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun DateRangeSegmented(
    selectedDateRange: DateRange, onDateRangeChanged: (DateRange) -> Unit, colors: AppColors
) {
    val trackColor = if (colors.isDark) Color(0xFF1C1B2E) else Color(0xFFE4E4E4)
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)
        .shadow(2.dp, RoundedCornerShape(26.dp))
        .background(trackColor, RoundedCornerShape(26.dp)).padding(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            DateRange.values().forEach { range ->
                val isSelected = range == selectedDateRange
                val chipColor  = if (isSelected) ModernAccent else colors.textMuted
                Box(modifier = Modifier.weight(1f).height(36.dp)
                    .then(if (isSelected) Modifier.shadow(3.dp, RoundedCornerShape(22.dp))
                        .background(if (colors.isDark) Color(0xFF2A2740) else Color.White, RoundedCornerShape(22.dp))
                    else Modifier.clip(RoundedCornerShape(22.dp)))
                    .clickable { onDateRangeChanged(range) },
                    contentAlignment = Alignment.Center) {
                    if (range == DateRange.CUSTOM) {
                        Icon(imageVector = Icons.Default.DateRange, contentDescription = "Pick custom date",
                            tint = chipColor, modifier = Modifier.size(16.dp))
                    } else {
                        Text(text = range.getShortLabel(), fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = chipColor, maxLines = 1, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// DATE PICKER BOTTOM SHEET
// ══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerBottomSheet(
    initialDate: CustomDate?, onDateSelected: (CustomDate) -> Unit,
    onDismiss: () -> Unit, colors: AppColors = LocalAppColors.current
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val today      = Calendar.getInstance()

    var viewYear  by remember { mutableStateOf(initialDate?.year  ?: today.get(Calendar.YEAR)) }
    var viewMonth by remember { mutableStateOf(initialDate?.month ?: (today.get(Calendar.MONTH) + 1)) }
    var selDay    by remember { mutableStateOf(initialDate?.day) }
    var showYearMonthPicker by remember { mutableStateOf(false) }

    val monthName = SimpleDateFormat("MMMM", Locale.getDefault())
        .format(Calendar.getInstance().apply { set(Calendar.MONTH, viewMonth - 1) }.time)
    val daysInMonth = Calendar.getInstance().apply { set(viewYear, viewMonth - 1, 1) }
        .getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = Calendar.getInstance().apply { set(viewYear, viewMonth - 1, 1) }
        .get(Calendar.DAY_OF_WEEK) - 1

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState,
        containerColor = colors.cardBg,
        dragHandle = {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.width(36.dp).height(4.dp)
                    .background(colors.cardBorder, RoundedCornerShape(2.dp)))
            }
        }) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 16.dp)) {
            Box(modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF12101E), Color(0xFF1E1340))))
                .padding(horizontal = 20.dp, vertical = 18.dp)) {
                Column {
                    Text("Select Date", fontSize = 11.sp, color = Color.White.copy(0.6f),
                        letterSpacing = 0.8.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Text(if (selDay != null) "$monthName $selDay, $viewYear" else "Choose a day",
                        fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                if (!showYearMonthPicker) {
                    Surface(onClick = {
                        if (viewMonth == 1) { viewMonth = 12; viewYear-- } else viewMonth--
                        selDay = null }, shape = CircleShape, color = colors.pillBg) {
                        Text("‹", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                } else { Spacer(Modifier.width(48.dp)) }

                Surface(onClick = { showYearMonthPicker = !showYearMonthPicker },
                    shape = RoundedCornerShape(12.dp),
                    color = if (showYearMonthPicker) ModernAccent.copy(0.12f) else colors.inputBg) {
                    Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("$monthName $viewYear", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold,
                            color = if (showYearMonthPicker) ModernAccent else colors.textPrimary)
                        Text(if (showYearMonthPicker) "▲" else "▼", fontSize = 9.sp,
                            color = if (showYearMonthPicker) ModernAccent else colors.textMuted)
                    }
                }

                if (!showYearMonthPicker) {
                    Surface(onClick = {
                        if (viewMonth == 12) { viewMonth = 1; viewYear++ } else viewMonth++
                        selDay = null }, shape = CircleShape, color = colors.pillBg) {
                        Text("›", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                } else { Spacer(Modifier.width(48.dp)) }
            }

            Spacer(Modifier.height(12.dp))

            if (showYearMonthPicker) {
                YearMonthPicker(currentYear = viewYear, currentMonth = viewMonth, colors = colors,
                    onSelected = { y, m -> viewYear = y; viewMonth = m; selDay = null; showYearMonthPicker = false })
            } else {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    listOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat").forEach { d ->
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(d, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMuted)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                val totalCells = firstDayOfWeek + daysInMonth
                val rows = (totalCells + 6) / 7
                Column(modifier = Modifier.padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    for (row in 0 until rows) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (col in 0..6) {
                                val day        = row * 7 + col - firstDayOfWeek + 1
                                val isValid    = day in 1..daysInMonth
                                val isSelected = isValid && day == selDay
                                val isToday    = isValid &&
                                        day == today.get(Calendar.DAY_OF_MONTH) &&
                                        viewMonth == today.get(Calendar.MONTH) + 1 &&
                                        viewYear  == today.get(Calendar.YEAR)
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f).padding(3.dp)
                                    .then(when {
                                        isSelected -> Modifier.background(ModernAccent, CircleShape)
                                        isToday    -> Modifier.border(1.5.dp, ModernAccent, CircleShape)
                                        else       -> Modifier
                                    }).clip(CircleShape)
                                    .then(if (isValid) Modifier.clickable { selDay = day } else Modifier),
                                    contentAlignment = Alignment.Center) {
                                    if (isValid) {
                                        Text("$day", fontSize = 13.sp,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isSelected -> Color.White
                                                isToday    -> ModernAccent
                                                else       -> colors.textPrimary
                                            })
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, colors.cardBorder)) {
                    Text("Cancel", color = colors.textSecondary, fontWeight = FontWeight.SemiBold)
                }
                Button(onClick = { selDay?.let { onDateSelected(CustomDate(viewYear, viewMonth, it)) } },
                    modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ModernAccent),
                    enabled = selDay != null) {
                    Text("Apply", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun YearMonthPicker(
    currentYear: Int, currentMonth: Int, colors: AppColors, onSelected: (Int, Int) -> Unit
) {
    val today      = Calendar.getInstance()
    var pickerYear by remember { mutableStateOf(currentYear) }
    val monthNames = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    val monthActiveBg   = if (colors.isDark) Color(0xFF1C1B30) else Color(0xFFF0EFFF)
    val monthDisabledBg = colors.pillBg.copy(alpha = if (colors.isDark) 0.4f else 0.6f)

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Surface(onClick = { pickerYear-- }, shape = CircleShape, color = colors.pillBg) {
                Text("‹", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            }
            Text("$pickerYear", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = colors.textPrimary)
            val canGoForward = pickerYear < today.get(Calendar.YEAR)
            Surface(onClick = { if (canGoForward) pickerYear++ }, shape = CircleShape,
                color = if (canGoForward) colors.pillBg else colors.pillBg.copy(alpha = 0.4f)) {
                Text("›", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                    color = if (canGoForward) colors.textPrimary else colors.textMuted.copy(alpha = 0.4f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            }
        }
        Spacer(Modifier.height(14.dp))
        monthNames.chunked(3).forEachIndexed { rowIdx, rowMonths ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowMonths.forEachIndexed { colIdx, name ->
                    val monthNum   = rowIdx * 3 + colIdx + 1
                    val isSelected = monthNum == currentMonth && pickerYear == currentYear
                    val isFuture   = pickerYear == today.get(Calendar.YEAR) &&
                            monthNum > today.get(Calendar.MONTH) + 1
                    val isPast     = pickerYear > today.get(Calendar.YEAR)
                    Surface(onClick = { if (!isFuture && !isPast) onSelected(pickerYear, monthNum) },
                        modifier = Modifier.weight(1f).height(42.dp), shape = RoundedCornerShape(10.dp),
                        color = when {
                            isSelected         -> ModernAccent
                            isFuture || isPast -> monthDisabledBg
                            else               -> monthActiveBg
                        },
                        border = if (isSelected) null else BorderStroke(1.dp, colors.cardBorder)) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(name, fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    isSelected         -> Color.White
                                    isFuture || isPast -> colors.textMuted.copy(alpha = 0.4f)
                                    else               -> colors.textPrimary
                                })
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// ACTIVITY CARD
// ══════════════════════════════════════════════════════════════════════════

@Composable
fun ActivityCard(activity: ActivityEvent, colors: AppColors, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 5.dp)
        .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp), color = colors.cardBg,
        border = BorderStroke(1.dp, colors.cardBorder),
        shadowElevation = if (colors.isDark) 0.dp else 2.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.size(46.dp)
                .background(activity.type.getColor().copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                .border(1.dp, activity.type.getColor().copy(alpha = 0.20f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center) { Text(activity.type.getIcon(), fontSize = 20.sp) }
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top) {
                    Text(activity.title, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(end = 8.dp))
                    activity.insightBadge?.let { badge ->
                        val (bg, fg) = when {
                            badge.contains("QUICK") || badge.contains("SAME DAY") ->
                                ModernFound.copy(alpha = 0.15f) to ModernFound
                            badge.contains("ATTENTION") ->
                                ModernError.copy(alpha = 0.15f) to ModernError
                            else -> colors.pillBg to colors.textMuted
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = bg) {
                            Text(badge, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = fg,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(activity.description, fontSize = 11.sp, color = colors.textSecondary,
                    maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 15.sp)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(4.dp).background(colors.divider, CircleShape))
                    Text(formatActivityTimestamp(activity.timestamp),
                        fontSize = 10.sp, color = colors.textMuted, letterSpacing = 0.2.sp)
                }
            }
        }
    }
}

fun formatActivityTimestamp(timestamp: Long): String {
    val now  = System.currentTimeMillis(); val diff = now - timestamp
    val hours = TimeUnit.MILLISECONDS.toHours(diff); val days = TimeUnit.MILLISECONDS.toDays(diff)
    return when {
        hours < 1  -> "Just now"
        hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
        days == 1L -> "Yesterday"
        days < 7   -> "$days days ago"
        else       -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}