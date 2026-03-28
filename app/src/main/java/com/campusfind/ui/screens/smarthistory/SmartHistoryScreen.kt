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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartHistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: SmartHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadHistory() }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF6F5F2))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SmartHistoryHero(
                onNavigateBack = onNavigateBack,
                totalItems     = uiState.totalItemsPosted,
                foundItems     = uiState.itemsFound,
                successRate    = uiState.successRate
            )

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
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("⚠️", fontSize = 48.sp)
                            Text(uiState.error ?: "Error", fontSize = 14.sp, color = ModernError)
                            Button(
                                onClick = { viewModel.loadHistory() },
                                colors = ButtonDefaults.buttonColors(containerColor = ModernAccent)
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
                            Text("No Activity Yet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF666666))
                            Text(
                                "Your activity history will appear here once you start posting items",
                                fontSize = 13.sp, color = Color(0xFFAAAAAA), textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        item {
                            InsightsDashboard(
                                avgResolutionDays = uiState.averageResolutionDays,
                                mostActiveDay     = uiState.mostActiveDay,
                                recentPosts       = uiState.recentPosts,
                                pendingItems      = uiState.pendingItems,
                                resolvedItems     = uiState.resolvedItems
                            )
                        }
                        item {
                            FilterSection(
                                selectedFilter    = uiState.selectedFilter,
                                selectedDateRange = uiState.selectedDateRange,
                                customDate        = uiState.customDate,
                                onFilterChanged   = { viewModel.onFilterChanged(it) },
                                onDateRangeChanged = { viewModel.onDateRangeChanged(it) },
                                activityCount     = uiState.filteredActivities.size
                            )
                        }
                        if (uiState.filteredActivities.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No activities match your filters", fontSize = 13.sp, color = Color(0xFFAAAAAA))
                                }
                            }
                        } else {
                            items(items = uiState.filteredActivities, key = { it.id }) { activity ->
                                ActivityCard(
                                    activity = activity,
                                    onClick  = { activity.itemId?.let { onNavigateToDetail(it) } }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Date Picker Bottom Sheet ──────────────────────────────────────
        if (uiState.showDatePicker) {
            DatePickerBottomSheet(
                initialDate  = uiState.customDate,
                onDateSelected = { viewModel.onCustomDateSelected(it) },
                onDismiss    = { viewModel.onDatePickerDismissed() }
            )
        }
    }
}

// ══════════════════════════════════════════════════════
// HERO
// ══════════════════════════════════════════════════════

@Composable
fun SmartHistoryHero(
    onNavigateBack: () -> Unit,
    totalItems: Int,
    foundItems: Int,
    successRate: Int
) {
    Box(
        modifier = Modifier.fillMaxWidth().background(
            Brush.linearGradient(colors = listOf(Color(0xFF1a1228), Color(0xFF2e1f48), Color(0xFF1a2a20)))
        )
    ) {
        Box(modifier = Modifier.size(140.dp).offset(x = 250.dp, y = (-20).dp)
            .background(ModernAccent.copy(alpha = 0.3f), CircleShape).blur(42.dp))
        Box(modifier = Modifier.size(110.dp).offset(x = (-10).dp, y = 100.dp)
            .background(ModernFound.copy(alpha = 0.22f), CircleShape).blur(36.dp))

        Column(modifier = Modifier.fillMaxWidth().padding(top = 36.dp, bottom = 24.dp)) {
            Surface(
                onClick = onNavigateBack,
                modifier = Modifier.padding(start = 14.dp, top = 8.dp),
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
                        modifier = Modifier.size(20.dp).background(Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Text("‹", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White) }
                    Text("Back", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
            Spacer(Modifier.height(18.dp))
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📊", fontSize = 28.sp)
                    Text("Smart History", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White,
                        style = LocalTextStyle.current.copy(shadow = Shadow(Color.Black.copy(alpha = 0.4f), Offset(0f, 2f), 12f)))
                }
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickStat("$totalItems", "Items", modifier = Modifier.weight(1f))
                    QuickStat("$foundItems", "Found", color = ModernFound, modifier = Modifier.weight(1f))
                    QuickStat("$successRate%", "Success", color = ModernAccent, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun QuickStat(value: String, label: String, color: Color = Color.White, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = color,
            style = LocalTextStyle.current.copy(shadow = Shadow(Color.Black.copy(alpha = 0.3f), Offset(0f, 1f), 4f)))
        Text(label.uppercase(), fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f), letterSpacing = 0.5.sp)
    }
}

// ══════════════════════════════════════════════════════
// INSIGHTS
// ══════════════════════════════════════════════════════

@Composable
fun InsightsDashboard(avgResolutionDays: Int, mostActiveDay: String?, recentPosts: Int, pendingItems: Int, resolvedItems: Int) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("INSIGHTS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFAAAAAA), letterSpacing = 0.6.sp, modifier = Modifier.padding(start = 2.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            InsightCard("⚡", if (avgResolutionDays == 0) "N/A" else "$avgResolutionDays days", "Avg Resolution", Color(0xFFFFF5E6), Color(0xFFFFB74D), Modifier.weight(1f))
            InsightCard("📢", "$recentPosts", "Last 7 Days", Color(0xFFF0EFFF), ModernAccent, Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            InsightCard("⏳", "$pendingItems", "Pending", Color(0xFFFFEBEE), ModernLost, Modifier.weight(1f))
            InsightCard("✅", "$resolvedItems", "Resolved", Color(0xFFEDFCF5), ModernFound, Modifier.weight(1f))
        }
    }
}

@Composable
fun InsightCard(icon: String, value: String, label: String, bgColor: Color, iconColor: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = bgColor,
        border = BorderStroke(1.5.dp, iconColor.copy(alpha = 0.2f)), shadowElevation = 1.dp) {
        Column(modifier = Modifier.padding(vertical = 14.dp, horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 22.sp)
            Spacer(Modifier.height(6.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1a1a2e))
            Spacer(Modifier.height(2.dp))
            Text(label, fontSize = 10.sp, color = Color(0xFF888888), textAlign = TextAlign.Center)
        }
    }
}

// ══════════════════════════════════════════════════════
// FILTER SECTION
// ══════════════════════════════════════════════════════

@Composable
fun FilterSection(
    selectedFilter: HistoryFilter,
    selectedDateRange: DateRange,
    customDate: CustomDate?,
    onFilterChanged: (HistoryFilter) -> Unit,
    onDateRangeChanged: (DateRange) -> Unit,
    activityCount: Int
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Active custom date label
        if (selectedDateRange == DateRange.CUSTOM && customDate != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ACTIVITY ($activityCount)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFAAAAAA), letterSpacing = 0.6.sp)
                Surface(shape = RoundedCornerShape(12.dp), color = ModernAccent.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, ModernAccent.copy(alpha = 0.3f))) {
                    Text("📅 ${customDate.getLabel()}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        color = ModernAccent, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                }
            }
        } else {
            Text("ACTIVITY ($activityCount)", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                color = Color(0xFFAAAAAA), letterSpacing = 0.6.sp, modifier = Modifier.padding(horizontal = 16.dp))
        }

        // Segmented date range (includes 📅 Custom chip)
        DateRangeSegmented(
            selectedDateRange  = selectedDateRange,
            onDateRangeChanged = onDateRangeChanged
        )

        // Activity type chips
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HistoryFilter.values().forEach { filter ->
                val isSelected = selectedFilter == filter
                Surface(
                    onClick = { onFilterChanged(filter) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) ModernAccent else Color.White,
                    border = BorderStroke(1.5.dp, if (isSelected) ModernAccent else Color(0xFFE0E0E0)),
                    shadowElevation = if (isSelected) 3.dp else 0.dp
                ) {
                    Text(text = filter.getLabel(), fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF666666),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun DateRangeSegmented(selectedDateRange: DateRange, onDateRangeChanged: (DateRange) -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)
            .shadow(2.dp, RoundedCornerShape(26.dp), ambientColor = Color.Black.copy(0.06f))
            .background(Color(0xFFE4E4E4), RoundedCornerShape(26.dp)).padding(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            DateRange.values().forEach { range ->
                val isSelected = range == selectedDateRange
                val chipColor  = if (isSelected) ModernAccent else Color(0xFF999999)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .then(
                            if (isSelected)
                                Modifier
                                    .shadow(3.dp, RoundedCornerShape(22.dp),
                                        ambientColor = Color.Black.copy(0.08f),
                                        spotColor = Color.Black.copy(0.10f))
                                    .background(Color.White, RoundedCornerShape(22.dp))
                            else Modifier.clip(RoundedCornerShape(22.dp))
                        )
                        .clickable { onDateRangeChanged(range) },
                    contentAlignment = Alignment.Center
                ) {
                    if (range == DateRange.CUSTOM) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Pick custom date",
                            tint = chipColor,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = range.getShortLabel(),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = chipColor,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════
// DATE PICKER BOTTOM SHEET  (fixed)
//
// Fix 1 — sheetState skipPartiallyExpanded=true → always full height
// Fix 2 — tapping "Month Year" header toggles a Year/Month picker grid
// Fix 3 — "📅" chip label replaced with "Pick" (done in UiState)
// ══════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerBottomSheet(
    initialDate: CustomDate?,
    onDateSelected: (CustomDate) -> Unit,
    onDismiss: () -> Unit
) {
    // FIX 1: skipPartiallyExpanded forces the sheet to open fully, not at 50%
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val today     = Calendar.getInstance()
    var viewYear  by remember { mutableStateOf(initialDate?.year  ?: today.get(Calendar.YEAR)) }
    var viewMonth by remember { mutableStateOf(initialDate?.month ?: (today.get(Calendar.MONTH) + 1)) }
    var selDay    by remember { mutableStateOf(initialDate?.day) }

    // FIX 2: toggle between calendar view and year/month picker view
    var showYearMonthPicker by remember { mutableStateOf(false) }

    val monthName = SimpleDateFormat("MMMM", Locale.getDefault())
        .format(Calendar.getInstance().apply { set(Calendar.MONTH, viewMonth - 1) }.time)

    val daysInMonth = Calendar.getInstance().apply {
        set(viewYear, viewMonth - 1, 1)
    }.getActualMaximum(Calendar.DAY_OF_MONTH)

    val firstDayOfWeek = Calendar.getInstance().apply {
        set(viewYear, viewMonth - 1, 1)
    }.get(Calendar.DAY_OF_WEEK) - 1

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,          // ← FIX 1
        containerColor   = Color.White,
        dragHandle = {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.width(36.dp).height(4.dp)
                    .background(Color(0xFFDDDDDD), RoundedCornerShape(2.dp)))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            // ── Gradient header ──────────────────────────────────────────
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.linearGradient(
                        colors = listOf(Color(0xFF1a1228), Color(0xFF2e1f48))
                    ))
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Column {
                    Text("Select Date", fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        letterSpacing = 0.8.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (selDay != null) "$monthName $selDay, $viewYear" else "Choose a day",
                        fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Month/Year nav row ───────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prev arrow — hidden in picker mode
                if (!showYearMonthPicker) {
                    Surface(onClick = {
                        if (viewMonth == 1) { viewMonth = 12; viewYear-- } else viewMonth--
                        selDay = null
                    }, shape = CircleShape, color = Color(0xFFF0F0F0)) {
                        Text("‹", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF444444),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                } else { Spacer(Modifier.width(48.dp)) }

                // FIX 2: tapping the month+year label toggles picker mode
                Surface(
                    onClick = { showYearMonthPicker = !showYearMonthPicker },
                    shape = RoundedCornerShape(12.dp),
                    color = if (showYearMonthPicker)
                        ModernAccent.copy(alpha = 0.12f)
                    else Color(0xFFF5F5F5)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "$monthName $viewYear",
                            fontSize = 15.sp, fontWeight = FontWeight.ExtraBold,
                            color = if (showYearMonthPicker) ModernAccent else Color(0xFF1a1a2e)
                        )
                        // Chevron indicates it's tappable
                        Text(
                            if (showYearMonthPicker) "▲" else "▼",
                            fontSize = 9.sp,
                            color = if (showYearMonthPicker) ModernAccent else Color(0xFF999999)
                        )
                    }
                }

                // Next arrow — hidden in picker mode
                if (!showYearMonthPicker) {
                    Surface(onClick = {
                        if (viewMonth == 12) { viewMonth = 1; viewYear++ } else viewMonth++
                        selDay = null
                    }, shape = CircleShape, color = Color(0xFFF0F0F0)) {
                        Text("›", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF444444),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                } else { Spacer(Modifier.width(48.dp)) }
            }

            Spacer(Modifier.height(12.dp))

            // ── Year / Month picker (shown when header tapped) ───────────
            if (showYearMonthPicker) {
                YearMonthPicker(
                    currentYear  = viewYear,
                    currentMonth = viewMonth,
                    onSelected   = { y, m ->
                        viewYear  = y
                        viewMonth = m
                        selDay    = null
                        showYearMonthPicker = false
                    }
                )
            } else {
                // ── Day-of-week headers ──────────────────────────────────
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { d ->
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(d, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBBBBBB))
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ── Calendar grid ────────────────────────────────────────
                val totalCells = firstDayOfWeek + daysInMonth
                val rows       = (totalCells + 6) / 7

                Column(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    for (row in 0 until rows) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (col in 0..6) {
                                val cellIndex  = row * 7 + col
                                val day        = cellIndex - firstDayOfWeek + 1
                                val isValid    = day in 1..daysInMonth
                                val isSelected = isValid && day == selDay
                                val isToday    = isValid &&
                                        day == today.get(Calendar.DAY_OF_MONTH) &&
                                        viewMonth == today.get(Calendar.MONTH) + 1 &&
                                        viewYear  == today.get(Calendar.YEAR)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(3.dp)
                                        .then(when {
                                            isSelected -> Modifier.background(ModernAccent, CircleShape)
                                            isToday    -> Modifier.border(1.5.dp, ModernAccent, CircleShape)
                                            else       -> Modifier
                                        })
                                        .clip(CircleShape)
                                        .then(if (isValid) Modifier.clickable { selDay = day } else Modifier),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isValid) {
                                        Text(
                                            "$day", fontSize = 13.sp,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isSelected -> Color.White
                                                isToday    -> ModernAccent
                                                else       -> Color(0xFF333333)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Action buttons ───────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFDDDDDD))
                ) {
                    Text("Cancel", color = Color(0xFF666666), fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = {
                        val d = selDay
                        if (d != null) onDateSelected(CustomDate(viewYear, viewMonth, d))
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ModernAccent),
                    enabled = selDay != null
                ) {
                    Text("Apply", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Year/Month picker grid — shown when header is tapped ──────────────────
@Composable
private fun YearMonthPicker(
    currentYear: Int,
    currentMonth: Int,
    onSelected: (year: Int, month: Int) -> Unit
) {
    val today        = Calendar.getInstance()
    var pickerYear   by remember { mutableStateOf(currentYear) }
    val monthNames   = listOf("Jan","Feb","Mar","Apr","May","Jun",
        "Jul","Aug","Sep","Oct","Nov","Dec")

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {

        // Year scroller
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(onClick = { pickerYear-- }, shape = CircleShape, color = Color(0xFFF0F0F0)) {
                Text("‹", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF444444),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            }
            Text("$pickerYear", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1a1a2e))
            Surface(
                onClick = { if (pickerYear < today.get(Calendar.YEAR)) pickerYear++ },
                shape = CircleShape,
                color = if (pickerYear < today.get(Calendar.YEAR)) Color(0xFFF0F0F0) else Color(0xFFEEEEEE)
            ) {
                Text("›", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                    color = if (pickerYear < today.get(Calendar.YEAR)) Color(0xFF444444) else Color(0xFFCCCCCC),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            }
        }

        Spacer(Modifier.height(14.dp))

        // Month grid — 3 columns × 4 rows
        val monthChunks = monthNames.chunked(3)
        monthChunks.forEachIndexed { rowIdx, rowMonths ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowMonths.forEachIndexed { colIdx, name ->
                    val monthNum   = rowIdx * 3 + colIdx + 1
                    val isSelected = monthNum == currentMonth && pickerYear == currentYear
                    val isFuture   = pickerYear == today.get(Calendar.YEAR) &&
                            monthNum > today.get(Calendar.MONTH) + 1
                    val isPast     = pickerYear > today.get(Calendar.YEAR)   // year in future

                    Surface(
                        onClick = {
                            if (!isFuture && !isPast) onSelected(pickerYear, monthNum)
                        },
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = when {
                            isSelected           -> ModernAccent
                            isFuture || isPast   -> Color(0xFFF8F8F8)
                            else                 -> Color(0xFFF0EFFF)
                        },
                        border = if (isSelected) null
                        else BorderStroke(1.dp, Color(0xFFE8E8E8))
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                name, fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    isSelected           -> Color.White
                                    isFuture || isPast   -> Color(0xFFCCCCCC)
                                    else                 -> Color(0xFF444444)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════
// ACTIVITY CARD
// ══════════════════════════════════════════════════════

@Composable
fun ActivityCard(activity: ActivityEvent, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 5.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp), color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)), shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier.size(46.dp)
                    .background(activity.type.getColor().copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .border(1.dp, activity.type.getColor().copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) { Text(activity.type.getIcon(), fontSize = 20.sp) }

            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Text(activity.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1a1a2e),
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(end = 8.dp))
                    activity.insightBadge?.let { badge ->
                        val (bg, fg) = when {
                            badge.contains("QUICK") || badge.contains("SAME DAY") -> ModernFound.copy(alpha = 0.15f) to ModernFound
                            badge.contains("ATTENTION") -> ModernError.copy(alpha = 0.15f) to ModernError
                            else -> Color(0xFFF0F0F0) to Color(0xFF888888)
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = bg) {
                            Text(badge, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = fg,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(activity.description, fontSize = 11.sp, color = Color(0xFF888888), maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 15.sp)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(4.dp).background(Color(0xFFDDDDDD), CircleShape))
                    Text(formatActivityTimestamp(activity.timestamp), fontSize = 10.sp, color = Color(0xFFBBBBBB), letterSpacing = 0.2.sp)
                }
            }
        }
    }
}

fun formatActivityTimestamp(timestamp: Long): String {
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