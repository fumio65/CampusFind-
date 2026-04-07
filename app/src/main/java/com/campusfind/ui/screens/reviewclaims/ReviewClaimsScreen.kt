package com.campusfind.ui.screens.reviewclaims

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.campusfind.domain.model.Claim
import com.campusfind.domain.model.ClaimStatus
import com.campusfind.ui.theme.*
import com.campusfind.ui.components.HeroBackButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewClaimsScreen(
    itemId: String,
    itemTitle: String,
    onNavigateBack: () -> Unit,
    viewModel: ReviewClaimsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors  = LocalAppColors.current

    LaunchedEffect(itemId) { viewModel.loadClaims(itemId) }

    Box(modifier = Modifier.fillMaxSize().background(colors.screenBg)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Hero ───────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(Brush.linearGradient(
                        listOf(Color(0xFF12101E), Color(0xFF1E1340), Color(0xFF0E1F18))))
            ) {
                Box(modifier = Modifier.size(160.dp).offset(x = 220.dp, y = (-40).dp)
                    .background(ModernAccent.copy(alpha = 0.15f), CircleShape))
                Box(modifier = Modifier.size(90.dp).offset(x = (-10).dp, y = 100.dp)
                    .background(ModernFound.copy(alpha = 0.08f), CircleShape))

                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                    Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
                        HeroBackButton(onClick = onNavigateBack,
                            modifier = Modifier.align(Alignment.CenterStart))
                    }
                    Spacer(Modifier.height(16.dp))
                    Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("✋", fontSize = 24.sp)
                            Text("Review Claims", fontSize = 24.sp,
                                fontWeight = FontWeight.Black, color = Color.White)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(itemTitle, fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                }
            }

            // ── Content with Pull to Refresh ───────────────────────────────
            var isRefreshing by remember { mutableStateOf(false) }

            val refreshScope = rememberCoroutineScope()
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh    = {
                    refreshScope.launch {
                        isRefreshing = true
                        viewModel.loadClaims(itemId)
                        kotlinx.coroutines.delay(1500)
                        isRefreshing = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLoading && !isRefreshing -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = ModernAccent, strokeWidth = 2.dp)
                        }
                    }
                    uiState.error != null -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(uiState.error ?: "", color = ModernError,
                                modifier = Modifier.padding(20.dp))
                        }
                    }
                    uiState.claims.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(32.dp)) {
                                Text("✋", fontSize = 56.sp)
                                Text("No Claims Yet", fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                Text("Claims will appear here when someone says they found your item.",
                                    fontSize = 13.sp, color = colors.textMuted,
                                    textAlign = TextAlign.Center)
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier       = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(items = uiState.claims, key = { it.id }) { claim ->
                                ClaimCard(
                                    claim     = claim,
                                    colors    = colors,
                                    onApprove = { viewModel.approveClaim(claim.id) },
                                    onReject  = { viewModel.rejectClaim(claim.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Claim Card ─────────────────────────────────────────────────────────────

@Composable
private fun ClaimCard(
    claim: Claim, colors: AppColors,
    onApprove: () -> Unit, onReject: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        color = colors.cardBg, border = BorderStroke(1.dp, colors.cardBorder),
        shadowElevation = if (colors.isDark) 0.dp else 2.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape)
                        .background(ModernAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center) {
                        Text(claim.claimerName.firstOrNull()?.uppercase() ?: "?",
                            fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ModernAccent)
                    }
                    Column {
                        Text(claim.claimerName, fontSize = 13.sp,
                            fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text(formatTimestamp(claim.claimedAt), fontSize = 10.sp, color = colors.textMuted)
                    }
                }
                ClaimStatusBadge(claim.status, colors)
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = colors.cardBorder)
            Spacer(Modifier.height(10.dp))

            Text(claim.message, fontSize = 13.sp, color = colors.textSecondary, lineHeight = 19.sp)

            if (claim.photoUri != null) {
                Spacer(Modifier.height(10.dp))
                AsyncImage(
                    model              = File(claim.photoUri),
                    contentDescription = "Claim photo",
                    modifier           = Modifier.fillMaxWidth().height(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale       = ContentScale.Crop
                )
            }

            if (claim.status == ClaimStatus.PENDING) {
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(onClick = onReject, modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(12.dp), color = ModernLost.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, ModernLost.copy(alpha = 0.4f))) {
                        Box(contentAlignment = Alignment.Center) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Text("✕", fontSize = 13.sp, color = ModernLost)
                                Text("Reject", fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold, color = ModernLost)
                            }
                        }
                    }
                    Box(modifier = Modifier.weight(1f).height(42.dp).clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(ModernFound, Color(0xFF20B080)))),
                        contentAlignment = Alignment.Center) {
                        Surface(onClick = onApprove, modifier = Modifier.fillMaxSize(),
                            color = Color.Transparent, shape = RoundedCornerShape(12.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Text("✓", fontSize = 13.sp, color = Color.White)
                                    Text("Approve", fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Status Badge ───────────────────────────────────────────────────────────

@Composable
private fun ClaimStatusBadge(status: ClaimStatus, colors: AppColors) {
    val (bgColor, textColor, text) = when (status) {
        ClaimStatus.PENDING  -> Triple(
            if (colors.isDark) Color(0xFF2A1800) else Color(0xFFFFF3E0),
            Color(0xFFFF6F00), "PENDING")
        ClaimStatus.APPROVED -> Triple(
            if (colors.isDark) Color(0xFF0D2010) else Color(0xFFE8F5E9),
            ModernFound, "APPROVED")
        ClaimStatus.REJECTED -> Triple(
            if (colors.isDark) Color(0xFF1A0808) else Color(0xFFFFEBEE),
            ModernLost, "REJECTED")
    }
    Surface(color = bgColor, shape = RoundedCornerShape(8.dp)) {
        Text(text, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

private fun formatTimestamp(timestamp: Long): String =
    SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault()).format(Date(timestamp))