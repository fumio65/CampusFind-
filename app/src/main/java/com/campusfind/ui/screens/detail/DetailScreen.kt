package com.campusfind.ui.screens.detail

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.campusfind.domain.model.Claim
import com.campusfind.domain.model.ClaimReply
import com.campusfind.domain.model.ClaimStatus
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.Tip
import com.campusfind.ui.components.HeroBackButton
import com.campusfind.ui.components.VerifiedClaimerAvatar
import com.campusfind.ui.screens.detail.DetailViewModel.Companion.MAX_TIP_LENGTH
import com.campusfind.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    itemId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToSubmitClaim: (String) -> Unit,
    onNavigateToReviewClaims: (itemId: String, itemTitle: String) -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors  = LocalAppColors.current
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu         by remember { mutableStateOf(false) }
    var showFullImage    by remember { mutableStateOf(false) }

    LaunchedEffect(itemId) { viewModel.loadItem(itemId) }
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }

    if (uiState.showLimitDialog != null) {
        LimitReminderDialog(
            message   = uiState.showLimitDialog ?: "",
            onDismiss = { viewModel.dismissLimitDialog() }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.screenBg)) {
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ModernAccent)
                }
            }
            uiState.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.error ?: "Error loading item",
                        color = ModernError, modifier = Modifier.padding(16.dp))
                }
            }
            uiState.item != null -> {
                val item = uiState.item!!

                Column(modifier = Modifier.fillMaxSize()) {

                    // ── Hero image ─────────────────────────────────────────
                    Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {

                        // Background gradient or photo
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF12101E), Color(0xFF1E1340), Color(0xFF0E1F18))
                                    )
                                )
                        )

                        val photoFile = item.photoUri?.let { File(it) }
                        val hasPhoto  = photoFile?.exists() == true

                        if (hasPhoto) {
                            Image(
                                painter            = rememberAsyncImagePainter(photoFile),
                                contentDescription = item.title,
                                modifier           = Modifier
                                    .fillMaxSize()
                                    .clickable { showFullImage = true },
                                contentScale       = ContentScale.Crop
                            )
                            // Scrim
                            Box(modifier = Modifier.fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.25f)))
                        }

                        // Bottom gradient for text
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color(0xFF0a0814).copy(0.65f),
                                            Color(0xFF0a0814).copy(0.95f)
                                        )
                                    )
                                )
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        item.title,
                                        fontSize = 18.sp, fontWeight = FontWeight.ExtraBold,
                                        color = Color.White, lineHeight = 22.sp,
                                        style = LocalTextStyle.current.copy(
                                            shadow = Shadow(Color.Black.copy(0.5f), Offset(0f, 1f), 6f)
                                        )
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                                        modifier = Modifier.padding(top = 3.dp)
                                    ) {
                                        Text("📍", fontSize = 10.sp)
                                        Text(
                                            item.location ?: "Location not specified",
                                            fontSize = 10.sp, color = Color.White.copy(0.6f)
                                        )
                                    }
                                }

                                // Status badge
                                val statusColor = if (item.status == ItemStatus.LOST) ModernLost else ModernFound
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(statusColor.copy(0.25f))
                                        .border(1.5.dp, statusColor.copy(0.55f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "● ${item.status.name}",
                                        fontSize = 10.sp, fontWeight = FontWeight.ExtraBold,
                                        color = if (item.status == ItemStatus.LOST)
                                            Color(0xFFff8099) else Color(0xFF2dd4a0)
                                    )
                                }
                            }
                        }

                        // ── Back button ────────────────────────────────────
                        HeroBackButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = 14.dp, top = 44.dp)
                        )

                        // ── Share button (replaces the two non-functioning buttons) ─
                        Surface(
                            onClick = {
                                val shareText = "Lost item: ${item.title}\n" +
                                        "Location: ${item.location ?: "Unknown"}\n" +
                                        "Description: ${item.description}\n\n" +
                                        "Reported via CampusFind+"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share lost item"))
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 14.dp, top = 44.dp)
                                .size(36.dp),
                            shape = CircleShape,
                            color = Color.Black.copy(0.38f),
                            border = BorderStroke(1.dp, Color.White.copy(0.18f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Share,
                                    contentDescription = "Share",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // ── "Tap to expand" hint — only if photo exists ────
                        if (hasPhoto) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 12.dp, bottom = 62.dp)
                                    .clickable { showFullImage = true },
                                shape = RoundedCornerShape(7.dp),
                                color = Color.Black.copy(0.4f)
                            ) {
                                Text(
                                    "⤢ Tap to expand",
                                    fontSize = 9.sp, color = Color.White.copy(0.75f),
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    // ── Meta strip ─────────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.screenBg)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Reported date pill
                        MetaPill(
                            icon    = "📅",
                            label   = "REPORTED",
                            value   = formatDate(item.reportedAt),
                            colors  = colors,
                            modifier = Modifier.weight(1f)
                        )
                        // Time pill
                        MetaPill(
                            icon    = "🕐",
                            label   = "TIME",
                            value   = formatTime(item.reportedAt),
                            colors  = colors,
                            modifier = Modifier.weight(1f)
                        )
                        // Sync pill
                        MetaPill(
                            icon        = "📍",
                            label       = "SYNC",
                            value       = "Local",
                            colors      = colors,
                            valueColor  = ModernAccent,
                            modifier    = Modifier.weight(1f)
                        )
                    }

                    // ── Scrollable content ─────────────────────────────────
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(colors.cardBg)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp)
                    ) {
                        Spacer(Modifier.height(14.dp))

                        // Description
                        Text("DESCRIPTION", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            color = colors.textMuted, letterSpacing = 0.6.sp)
                        Spacer(Modifier.height(5.dp))
                        Text(item.description, fontSize = 13.sp, color = colors.textPrimary, lineHeight = 19.sp)

                        Spacer(Modifier.height(12.dp))

                        // Reporter row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(9.dp))
                                    .background(Brush.linearGradient(listOf(ModernAccent, Color(0xFF4F46E5)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    (uiState.reporterName ?: "U").firstOrNull()?.uppercase() ?: "U",
                                    fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White
                                )
                            }
                            Column {
                                Text("Posted by", fontSize = 10.sp, color = colors.textMuted)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(uiState.reporterName ?: "Unknown", fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                    Text("· ${formatRelativeTime(item.reportedAt)}",
                                        fontSize = 11.sp, color = colors.textMuted)
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = colors.cardBorder)
                        Spacer(Modifier.height(10.dp))

                        // Inline claim submission
                        if (!uiState.isOwner && item.status == ItemStatus.LOST) {
                            val hasPending  = uiState.claims.any { it.status == ClaimStatus.PENDING }
                            val hasApproved = uiState.claims.any { it.status == ClaimStatus.APPROVED }
                            when {
                                hasPending -> {
                                    val warningBg     = if (colors.isDark) Color(0xFF1A1000) else Color(0xFFFFF4E6)
                                    val warningBorder = if (colors.isDark) Color(0xFFFFB74D).copy(0.28f) else Color(0xFFFFE0B2)
                                    val warningColor  = Color(0xFFFFB74D)
                                    val warningText   = if (colors.isDark) Color(0xFFFFB74D).copy(0.65f) else Color(0xFF6D4C41)

                                    Surface(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = warningBg,
                                        border = BorderStroke(1.dp, warningBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier.size(32.dp)
                                                    .clip(RoundedCornerShape(9.dp))
                                                    .background(warningColor.copy(0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) { Text("⏳", fontSize = 16.sp) }
                                            Column {
                                                Text("Claim Under Review",
                                                    fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                                    color = warningColor)
                                                Text("The reporter is reviewing a claim.",
                                                    fontSize = 10.sp, color = warningText,
                                                    lineHeight = 14.sp)
                                            }
                                        }
                                    }
                                }
                                !hasApproved -> {
                                    IFoundThisItemSection(onSubmit = { location, photoUri ->
                                        viewModel.submitClaimInline(location, photoUri)
                                    })
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                        }

                        // Claims
                        if (uiState.claims.isNotEmpty()) {
                            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                uiState.claims.forEach { claim ->
                                    when (claim.status) {
                                        ClaimStatus.APPROVED -> PinnedVerifiedClaim(
                                            claim             = claim,
                                            isOwner           = uiState.isOwner,
                                            onReply           = { viewModel.replyToClaim(claim.id, it) },
                                            messengerUsername = claim.messengerUsername,
                                            messageCount      = viewModel.getReplyCount(claim.id),
                                            replies           = viewModel.getReplies(claim.id),
                                            currentUserId     = viewModel.getCurrentUserId()
                                        )
                                        ClaimStatus.PENDING -> PendingClaimCard(
                                            claim     = claim,
                                            isOwner   = uiState.isOwner,
                                            currentUserId = viewModel.getCurrentUserId(),
                                            onApprove = { viewModel.approveClaim(claim.id) },
                                            onReject  = { viewModel.rejectClaim(claim.id) },
                                            onWithdraw = { viewModel.withdrawClaim(claim.id) }
                                        )
                                        ClaimStatus.REJECTED -> { /* hidden */ }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }

                        // Tips
                        PublicTipsSection(
                            tips          = uiState.tips,
                            tipCount      = uiState.tipCount,
                            isOwner       = uiState.isOwner,
                            hasUserTipped = uiState.hasUserTipped,
                            currentUserId = viewModel.getCurrentUserId(),
                            reportOwnerId = item.reportedBy,
                            colors        = colors,
                            onSubmitTip   = { viewModel.submitTip(it) },
                            onSubmitReply = { tipId, msg -> viewModel.submitReply(tipId, msg) }
                        )

                        Spacer(Modifier.height(80.dp))
                    }
                }

                // ── Sticky bottom bar (owner only) ─────────────────────────
                if (uiState.isOwner && item.status == ItemStatus.LOST) {
                    Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color    = colors.cardBg,
                            shadowElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Mark as Found
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Brush.linearGradient(listOf(ModernAccent, Color(0xFF4F46E5))))
                                        .clickable { viewModel.markAsFound(itemId, item) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp),
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Text("✓", fontSize = 14.sp, color = Color.White)
                                        Text("Mark as Found", fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    }
                                }

                                // ⋮ Menu button
                                Surface(
                                    onClick = { showMenu = true },
                                    modifier = Modifier.size(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = colors.pillBg,
                                    border = BorderStroke(1.dp, colors.cardBorder)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("⋮", fontSize = 18.sp, color = colors.textPrimary)
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Custom action sheet (replaces DropdownMenu) ───────────
                if (showMenu) {
                    Dialog(
                        onDismissRequest = { showMenu = false },
                        properties = DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().clickable { showMenu = false },
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 24.dp)
                                    .clickable(enabled = false) {},
                                shape = RoundedCornerShape(24.dp),
                                color = colors.cardBg,
                                shadowElevation = 24.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Handle bar
                                    Box(
                                        modifier = Modifier
                                            .width(36.dp)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(colors.cardBorder)
                                            .align(Alignment.CenterHorizontally)
                                    )
                                    Spacer(Modifier.height(4.dp))

                                    // Edit Item
                                    Surface(
                                        onClick = { showMenu = false; onNavigateToEdit(itemId) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        color = colors.pillBg
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 14.dp),
                                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(RoundedCornerShape(11.dp))
                                                    .background(ModernAccent.copy(alpha = 0.12f)),
                                                contentAlignment = Alignment.Center
                                            ) { Text("✏️", fontSize = 18.sp) }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Edit Item", fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.textPrimary)
                                                Text("Update title, description or photo",
                                                    fontSize = 11.sp, color = colors.textMuted)
                                            }
                                            Text("›", fontSize = 18.sp, color = colors.textMuted)
                                        }
                                    }

                                    // Delete Item
                                    Surface(
                                        onClick = { showMenu = false; showDeleteDialog = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        color = ModernError.copy(alpha = 0.06f),
                                        border = BorderStroke(1.dp, ModernError.copy(alpha = 0.15f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 14.dp),
                                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(RoundedCornerShape(11.dp))
                                                    .background(ModernError.copy(alpha = 0.12f)),
                                                contentAlignment = Alignment.Center
                                            ) { Text("🗑️", fontSize = 18.sp) }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Delete Item", fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ModernError)
                                                Text("Permanently remove this report",
                                                    fontSize = 11.sp, color = ModernError.copy(0.6f))
                                            }
                                            Text("›", fontSize = 18.sp, color = ModernError.copy(0.5f))
                                        }
                                    }

                                    Spacer(Modifier.height(4.dp))

                                    // Cancel
                                    Surface(
                                        onClick = { showMenu = false },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        color = colors.pillBg
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 14.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Cancel", fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = colors.textMuted)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Delete dialog ──────────────────────────────────────────
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        icon    = { Text("🗑️", fontSize = 32.sp) },
                        title   = { Text("Delete Item?", fontWeight = FontWeight.Bold) },
                        text    = { Text("This will permanently remove your lost item report.") },
                        confirmButton = {
                            Button(
                                onClick = { showDeleteDialog = false; viewModel.deleteItem(onSuccess = onNavigateBack) },
                                colors = ButtonDefaults.buttonColors(containerColor = ModernError)
                            ) { Text("Delete", fontWeight = FontWeight.Bold) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                        }
                    )
                }

                // ── Full screen image viewer ───────────────────────────────
                if (showFullImage && item.photoUri != null) {
                    PhotoPreviewDialog(
                        photoUri  = item.photoUri!!,
                        onDismiss = { showFullImage = false }
                    )
                }
            }
        }
    }
}

// ── Meta column helper ─────────────────────────────────────────────────────

@Composable
private fun MetaPill(
    icon: String,
    label: String,
    value: String,
    colors: AppColors,
    valueColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.cardBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, fontSize = 9.sp)
                Text(label, fontSize = 8.sp, color = colors.textMuted,
                    letterSpacing = 0.4.sp, fontWeight = FontWeight.Medium)
            }
            Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = valueColor ?: colors.textPrimary)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// PUBLIC TIPS
// ══════════════════════════════════════════════════════════════════════════

@Composable
fun PublicTipsSection(
    tips: List<Tip>,
    tipCount: Int,
    isOwner: Boolean,
    hasUserTipped: Boolean,
    currentUserId: String?,
    reportOwnerId: String,
    colors: AppColors,
    onSubmitTip: (String) -> Unit,
    onSubmitReply: (String, String) -> Unit
) {
    var tipText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth().background(colors.cardBg),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("PUBLIC TIPS", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    color = colors.textMuted, letterSpacing = 0.6.sp)
                if (tipCount > 0) {
                    Surface(shape = RoundedCornerShape(6.dp), color = ModernAccent.copy(0.10f)) {
                        Text("$tipCount", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ModernAccent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp))
                    }
                }
            }
            Text("1 tip/user", fontSize = 9.sp, color = colors.textMuted)
        }

        Text("Seen it? Leave a tip", fontSize = 9.sp, color = colors.textMuted)

        val topLevel = tips.filter { !it.isReply }
        if (topLevel.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                topLevel.forEach { tip ->
                    TipCard(
                        tip           = tip,
                        replies       = tips.filter { it.isReply && it.parentTipId == tip.id },
                        isReportOwner = isOwner,
                        currentUserId = currentUserId,
                        reportOwnerId = reportOwnerId,
                        colors        = colors,
                        onReply       = onSubmitReply
                    )
                }
            }
        }

        when {
            !isOwner && !hasUserTipped -> {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(26.dp).clip(RoundedCornerShape(9.dp))
                        .background(colors.pillBg), contentAlignment = Alignment.Center) {
                        Text("👤", fontSize = 11.sp)
                    }
                    Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp),
                        color = colors.cardBg, border = BorderStroke(1.dp, colors.cardBorder)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            BasicTextField(
                                value = tipText,
                                onValueChange = { if (it.length <= MAX_TIP_LENGTH) tipText = it },
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(fontSize = 11.sp, color = colors.textPrimary),
                                decorationBox = { inner ->
                                    if (tipText.isEmpty()) Text("Seen it? Leave a tip...",
                                        fontSize = 11.sp, color = colors.textMuted)
                                    inner()
                                }
                            )
                            Surface(
                                onClick = { if (tipText.isNotBlank()) { onSubmitTip(tipText.trim()); tipText = "" } },
                                modifier = Modifier.size(26.dp), shape = CircleShape,
                                color = if (tipText.isNotBlank()) ModernAccent else colors.pillBg
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("↑", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                        color = if (tipText.isNotBlank()) Color.White else colors.textMuted)
                                }
                            }
                        }
                    }
                }
                Text("🔒 Only the reporter can reply to tips",
                    fontSize = 9.sp, color = colors.textMuted, modifier = Modifier.padding(start = 33.dp))
            }
            !isOwner && hasUserTipped -> {
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                    color = colors.pillBg, border = BorderStroke(1.dp, colors.cardBorder)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("✓", fontSize = 14.sp, color = ModernFound)
                        Text("You've already left a comment", fontSize = 11.sp, color = colors.textMuted)
                    }
                }
            }
            isOwner -> {
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                    color = colors.pillBg, border = BorderStroke(1.dp, colors.cardBorder)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("🔒", fontSize = 14.sp)
                        Text("You can only reply to comments", fontSize = 11.sp, color = colors.textMuted)
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// TIP CARD
// ══════════════════════════════════════════════════════════════════════════

@Composable
fun TipCard(
    tip: Tip, replies: List<Tip>,
    isReportOwner: Boolean, currentUserId: String?,
    reportOwnerId: String, colors: AppColors,
    onReply: (String, String) -> Unit
) {
    var showReply by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
        color = colors.cardBg, border = BorderStroke(1.dp, colors.cardBorder)) {
        Column(modifier = Modifier.padding(8.dp, 10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Box(modifier = Modifier.size(24.dp).clip(RoundedCornerShape(8.dp))
                    .background(colors.pillBg), contentAlignment = Alignment.Center) {
                    Text("👤", fontSize = 10.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(tip.authorName, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                                color = colors.textSecondary)
                            if (tip.authorId == reportOwnerId) {
                                Surface(shape = RoundedCornerShape(5.dp), color = ModernAccent) {
                                    Text("Reporter", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
                                }
                            }
                        }
                        Text(formatRelativeTime(tip.createdAt), fontSize = 9.sp,
                            color = colors.textMuted, fontFamily = FontFamily.Monospace)
                    }
                    Text(tip.message, fontSize = 10.sp, color = colors.textSecondary,
                        lineHeight = 14.sp, modifier = Modifier.padding(top = 2.dp))
                    if (isReportOwner && tip.authorId != currentUserId) {
                        Row(modifier = Modifier.padding(top = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                onClick = { showReply = !showReply },
                                shape = RoundedCornerShape(7.dp),
                                color = ModernAccent.copy(0.08f),
                                border = BorderStroke(1.dp, ModernAccent.copy(0.2f))
                            ) {
                                Text("↩ Reply", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                    color = ModernAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                            Text("only you can reply", fontSize = 9.sp, color = colors.textMuted)
                        }
                    }
                }
            }

            if (showReply) {
                Row(modifier = Modifier.fillMaxWidth().padding(start = 31.dp, top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(modifier = Modifier.width(2.dp).height(40.dp)
                        .background(Brush.verticalGradient(listOf(ModernAccent, ModernAccent.copy(0.1f))),
                            RoundedCornerShape(2.dp)))
                    BasicTextField(
                        value = replyText,
                        onValueChange = { if (it.length <= MAX_TIP_LENGTH) replyText = it },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(fontSize = 10.sp, color = colors.textPrimary),
                        decorationBox = { inner ->
                            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(9.dp),
                                color = if (colors.isDark) Color(0xFF1A1730) else Color(0xFFF4F3FF),
                                border = BorderStroke(1.5.dp, ModernAccent.copy(0.3f))) {
                                Box(modifier = Modifier.padding(6.dp, 9.dp)) {
                                    if (replyText.isEmpty()) Text("Write a reply...",
                                        fontSize = 10.sp, color = colors.textMuted)
                                    inner()
                                }
                            }
                        }
                    )
                    Surface(
                        onClick = { if (replyText.isNotBlank()) {
                            onReply(tip.id, replyText.trim()); replyText = ""; showReply = false }
                        },
                        modifier = Modifier.size(28.dp), shape = CircleShape,
                        color = if (replyText.isNotBlank()) ModernAccent else colors.pillBg
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("↑", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                color = if (replyText.isNotBlank()) Color.White else colors.textMuted)
                        }
                    }
                }
            }

            if (replies.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth().padding(start = 31.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    replies.forEach { ReplyCard(reply = it, reportOwnerId = reportOwnerId, colors = colors) }
                }
            }
        }
    }
}

@Composable
fun ReplyCard(reply: Tip, reportOwnerId: String, colors: AppColors) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(modifier = Modifier.width(2.dp).height(60.dp)
            .background(Brush.verticalGradient(listOf(ModernAccent, ModernAccent.copy(0.1f))),
                RoundedCornerShape(2.dp)))
        Box(modifier = Modifier.size(22.dp).clip(RoundedCornerShape(7.dp))
            .background(Brush.linearGradient(listOf(ModernAccent, Color(0xFF4F46E5)))),
            contentAlignment = Alignment.Center) {
            Text("👤", fontSize = 10.sp)
        }
        Surface(modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(0.dp, 9.dp, 9.dp, 9.dp),
            color = if (colors.isDark) Color(0xFF1A1730) else Color(0xFFF4F3FF),
            border = BorderStroke(1.5.dp, ModernAccent.copy(0.25f))) {
            Column(modifier = Modifier.padding(6.dp, 9.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(reply.authorName, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ModernAccent)
                    if (reply.authorId == reportOwnerId) {
                        Surface(shape = RoundedCornerShape(4.dp), color = ModernAccent) {
                            Text("Reporter", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                }
                Text(reply.message, fontSize = 10.sp, color = colors.textPrimary, lineHeight = 14.sp,
                    modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// I FOUND THIS ITEM
// ══════════════════════════════════════════════════════════════════════════

@Composable
private fun IFoundThisItemSection(onSubmit: (String, String?) -> Unit, modifier: Modifier = Modifier) {
    var location  by remember { mutableStateOf("") }
    // Support up to 3 photos — matches SubmitClaimViewModel.MAX_PHOTOS
    var photoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val launcher  = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { if (photoUris.size < 3) photoUris = photoUris + it }
    }
    val colors     = LocalAppColors.current
    val cardBg     = if (colors.isDark) colors.cardBg     else Color(0xFFedfcf5)
    val cardBorder = if (colors.isDark) ModernFound.copy(0.25f) else Color(0xFFa8eecf)
    val inputBg    = if (colors.isDark) colors.pillBg     else Color.White
    val titleColor = if (colors.isDark) ModernFound       else Color(0xFF0a5c3c)
    val photoBg    = if (colors.isDark) colors.pillBg     else Color.White.copy(0.6f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .border(1.5.dp, cardBorder, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            // ── Header ─────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Box(
                    modifier = Modifier.size(24.dp).clip(RoundedCornerShape(8.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF0ea870), Color(0xFF2dd4a0)))),
                    contentAlignment = Alignment.Center
                ) { Text("✋", fontSize = 14.sp) }
                Text("I Found This Item", fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold, color = titleColor)
            }

            // ── Location input ─────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                shape = RoundedCornerShape(10.dp),
                color = inputBg,
                border = BorderStroke(1.5.dp, cardBorder)
            ) {
                BasicTextField(
                    value = location,
                    onValueChange = { if (it.length <= 200) location = it },
                    modifier = Modifier.fillMaxWidth().padding(8.dp).heightIn(min = 40.dp),
                    textStyle = TextStyle(fontSize = 10.sp, color = colors.textPrimary, lineHeight = 14.sp),
                    decorationBox = { inner ->
                        if (location.isEmpty()) {
                            Text("Describe where you found it...",
                                fontSize = 10.sp, color = colors.textMuted, lineHeight = 14.sp)
                        }
                        inner()
                    }
                )
            }

            // ── Photo slots (up to 3) ──────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                // Show existing selected photos
                photoUris.forEachIndexed { index, uri ->
                    Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(10.dp))) {
                        Image(
                            rememberAsyncImagePainter(uri), null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Remove button
                        Surface(
                            onClick = { photoUris = photoUris.toMutableList().also { it.removeAt(index) } },
                            modifier = Modifier.align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp).size(16.dp),
                            shape = CircleShape, color = ModernLost,
                            border = BorderStroke(1.5.dp, cardBg)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("✕", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }

                // Add photo slot (only if under 3)
                if (photoUris.size < 3) {
                    Surface(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier.size(60.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = photoBg,
                        border = BorderStroke(1.5.dp, if (photoUris.isEmpty()) ModernFound.copy(0.5f) else cardBorder)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("📷", fontSize = 18.sp)
                            Text(
                                if (photoUris.isEmpty()) "Add" else "+",
                                fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                color = if (photoUris.isEmpty()) ModernFound else colors.textMuted
                            )
                        }
                    }
                }

                // Empty remaining slots (visual indicator only)
                val emptySlots = 3 - photoUris.size - 1
                if (emptySlots > 0 && photoUris.isNotEmpty()) {
                    repeat(emptySlots.coerceAtLeast(0)) {
                        Box(
                            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(10.dp))
                                .background(photoBg)
                                .border(1.dp, cardBorder, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📷", fontSize = 18.sp, modifier = Modifier.alpha(0.25f))
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                // Photo counter badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (photoUris.isEmpty()) ModernLost.copy(0.10f) else ModernFound.copy(0.15f),
                    modifier = Modifier.align(Alignment.Bottom)
                ) {
                    Text(
                        "${photoUris.size}/3",
                        fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        color = if (photoUris.isEmpty()) ModernLost else ModernFound,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            // Required photo hint
            if (photoUris.isEmpty()) {
                Text(
                    "⚠ At least 1 photo is required",
                    fontSize = 9.sp, color = ModernLost,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // ── Submit button ──────────────────────────────────────────────
            val isValid = location.isNotBlank() && photoUris.isNotEmpty()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isValid)
                            Brush.linearGradient(listOf(Color(0xFF0ea870), Color(0xFF2dd4a0)))
                        else
                            Brush.linearGradient(listOf(
                                colors.textMuted.copy(0.3f), colors.textMuted.copy(0.3f)
                            ))
                    )
                    .clickable(enabled = isValid) {
                        // Pass all photo URIs pipe-delimited so repository saves all 3
                        val allPhotos = photoUris.joinToString("|") { it.toString() }
                        onSubmit(location, allPhotos.ifEmpty { null })
                    }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✋", fontSize = 12.sp)
                    Text(
                        "Submit Claim",
                        fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
                        color = if (isValid) Color.White else colors.textMuted
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// PINNED VERIFIED CLAIM
// ══════════════════════════════════════════════════════════════════════════

@Composable
private fun PinnedVerifiedClaim(
    claim: Claim, isOwner: Boolean,
    onReply: ((String) -> Unit)? = null,
    messengerUsername: String? = null,
    messageCount: Int = 0,
    replies: List<ClaimReply> = emptyList(),
    currentUserId: String? = null,
    modifier: Modifier = Modifier
) {
    var showPhotoDialog  by remember { mutableStateOf(false) }
    var showReplyInput   by remember { mutableStateOf(false) }
    var replyText        by remember { mutableStateOf("") }
    val userAlreadyReplied = currentUserId != null && replies.any { it.authorId == currentUserId }
    val colors = LocalAppColors.current

    // Theme-aware colors
    val cardBg     = if (colors.isDark) Color(0xFF0D1F16) else Color(0xFFedfcf5)
    val cardBorder = if (colors.isDark) ModernFound.copy(0.25f) else Color(0xFFa8eecf)
    val msgColor   = if (colors.isDark) Color(0xFF9DE8C5) else Color(0xFF1a4a35)
    val pinnedColor = ModernFound

    Box(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(cardBg)
        .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
        .padding(12.dp)
    ) {
        Column {
            // Pinned label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier.size(5.dp).clip(CircleShape)
                        .background(pinnedColor)
                )
                Text("PINNED · Verified Claim",
                    fontSize = 9.sp, fontWeight = FontWeight.ExtraBold,
                    color = pinnedColor, letterSpacing = 0.4.sp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                VerifiedClaimerAvatar(name = claim.claimerName)

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(bottom = 2.dp)) {
                        Text(claim.claimerName, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            color = colors.textPrimary)
                        Surface(shape = RoundedCornerShape(5.dp), color = ModernFound) {
                            Text("✓ Finder", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
                        }
                        Spacer(Modifier.weight(1f))
                        Text(formatTimeAgo(claim.claimedAt), fontSize = 9.sp,
                            color = colors.textMuted, fontFamily = FontFamily.Monospace)
                    }

                    Text(claim.message, fontSize = 10.sp, color = msgColor, lineHeight = 14.sp,
                        modifier = Modifier.padding(bottom = 5.dp))

                    // Photos — show all up to 3 as tappable thumbnails
                    val allPhotos = claim.photoUris.ifEmpty { listOfNotNull(claim.photoUri) }
                    if (allPhotos.isNotEmpty()) {
                        var selectedPhoto by remember { mutableStateOf(allPhotos.first()) }
                        Row(
                            modifier = Modifier.padding(bottom = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            allPhotos.forEach { uri ->
                                Surface(
                                    onClick = { selectedPhoto = uri; showPhotoDialog = true },
                                    modifier = Modifier.size(60.dp, 46.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.5.dp, Color(0xFF0ea870))
                                ) {
                                    Image(rememberAsyncImagePainter(uri), "Proof",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize())
                                }
                            }
                        }
                        if (showPhotoDialog) {
                            PhotoPreviewDialog(photoUri = selectedPhoto, onDismiss = { showPhotoDialog = false })
                        }
                    }

                    // Replies thread
                    if (replies.isNotEmpty()) {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            replies.forEach { reply ->
                                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Box(modifier = Modifier.width(2.dp).height(60.dp)
                                        .background(Brush.verticalGradient(listOf(Color(0xFF0ea870), Color(0xFF0ea870).copy(0.1f))),
                                            RoundedCornerShape(2.dp)))
                                    Surface(modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(0.dp, 9.dp, 9.dp, 9.dp),
                                        color = colors.pillBg,
                                        border = BorderStroke(1.dp, cardBorder)) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically) {
                                                Text(reply.authorName, fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold, color = ModernFound)
                                                Text(formatTimeAgo(reply.createdAt), fontSize = 8.sp,
                                                    color = colors.textMuted, fontFamily = FontFamily.Monospace)
                                            }
                                            Text(reply.message, fontSize = 10.sp, color = msgColor,
                                                lineHeight = 14.sp, modifier = Modifier.padding(top = 3.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Reply controls
                    if (onReply != null) {
                        when {
                            messageCount >= 2 && !messengerUsername.isNullOrBlank() -> {
                                Box(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Brush.linearGradient(listOf(Color(0xFF0084ff).copy(0.1f), Color(0xFF00c6ff).copy(0.1f))))
                                    .border(1.5.dp, Color(0xFF0ea870), RoundedCornerShape(8.dp))
                                    .padding(10.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Text("💬", fontSize = 14.sp)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Message on Messenger", fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold, color = Color(0xFF0084ff))
                                            Text("@$messengerUsername", fontSize = 9.sp, color = Color(0xFF0084ff).copy(0.7f))
                                        }
                                        Text("→", fontSize = 14.sp, color = Color(0xFF0084ff))
                                    }
                                }
                                Text("💡 2-message limit reached. Continue on Messenger to save server costs!",
                                    fontSize = 8.sp, color = Color(0xFF666666), lineHeight = 10.sp,
                                    modifier = Modifier.padding(top = 3.dp))
                            }
                            userAlreadyReplied -> {
                                Surface(modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = colors.pillBg, border = BorderStroke(1.dp, colors.cardBorder)) {
                                    Row(modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Text("✓", fontSize = 12.sp, color = ModernFound)
                                        Text("You've sent your message", fontSize = 9.sp, color = colors.textMuted)
                                    }
                                }
                                Text("⏳ Waiting for finder's reply...", fontSize = 8.sp,
                                    color = Color(0xFF888888), modifier = Modifier.padding(top = 3.dp))
                            }
                            else -> {
                                Row(modifier = Modifier.padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Surface(onClick = { showReplyInput = !showReplyInput },
                                        shape = RoundedCornerShape(7.dp),
                                        color = Color(0xFF0ea870).copy(0.12f),
                                        border = BorderStroke(1.dp, Color(0xFF0ea870).copy(0.3f))) {
                                        Text("💬 Reply to finder", fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold, color = Color(0xFF0ea870),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                    }
                                    Text("$messageCount/2 messages", fontSize = 8.sp, color = Color(0xFFAAAAAA))
                                }
                            }
                        }
                    }
                }
            }

            // Reply input
            if (showReplyInput && onReply != null && messageCount < 2 && !userAlreadyReplied) {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(modifier = Modifier.width(2.dp).height(50.dp)
                        .background(Brush.verticalGradient(listOf(Color(0xFF0ea870), Color(0xFF0ea870).copy(0.1f))),
                            RoundedCornerShape(2.dp)))
                    BasicTextField(value = replyText,
                        onValueChange = { if (it.length <= 200) replyText = it },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(fontSize = 10.sp, color = Color.Black),
                        decorationBox = { inner ->
                            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(9.dp),
                                color = Color.White, border = BorderStroke(1.5.dp, Color(0xFF0ea870).copy(0.3f))) {
                                Box(modifier = Modifier.padding(10.dp)) {
                                    if (replyText.isEmpty()) Text("Arrange meetup details...",
                                        fontSize = 10.sp, color = Color(0xFFBBBBBB))
                                    inner()
                                }
                            }
                        }
                    )
                    Surface(onClick = { if (replyText.isNotBlank()) { onReply(replyText.trim()); replyText = ""; showReplyInput = false } },
                        modifier = Modifier.size(36.dp), shape = CircleShape,
                        color = if (replyText.isNotBlank()) Color(0xFF0ea870) else Color(0xFFE8E8E4)) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("↑", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                                color = if (replyText.isNotBlank()) Color.White else Color(0xFFCCCCCC))
                        }
                    }
                }
            }
        }
    }

    // Photo dialog handled inline in the photos row above
}
// ══════════════════════════════════════════════════════════════════════════

@Composable
private fun PendingClaimCard(
    claim: Claim, isOwner: Boolean,
    onApprove: () -> Unit, onReject: () -> Unit,
    onWithdraw: (() -> Unit)? = null,   // claimer can withdraw pending claim
    currentUserId: String? = null,
    modifier: Modifier = Modifier
) {
    var showPhotoDialog    by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    val colors = LocalAppColors.current
    val warningColor = Color(0xFFFFB74D)
    val isClaimer = currentUserId != null && currentUserId == claim.claimerId

    Surface(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, warningColor.copy(if (colors.isDark) 0.25f else 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // ── Header ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(30.dp).clip(RoundedCornerShape(9.dp))
                            .background(warningColor.copy(if (colors.isDark) 0.18f else 0.12f)),
                        contentAlignment = Alignment.Center
                    ) { Text("⏳", fontSize = 15.sp) }
                    Column {
                        Text(
                            "Under Review",
                            fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            color = warningColor
                        )
                        Text(
                            formatTimeAgo(claim.claimedAt),
                            fontSize = 9.sp, color = colors.textMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                        .background(warningColor.copy(if (colors.isDark) 0.15f else 0.10f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        "PENDING", fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = warningColor, letterSpacing = 0.5.sp
                    )
                }
            }

            HorizontalDivider(color = colors.cardBorder)
            Spacer(Modifier.height(10.dp))

            // ── Claimer info ───────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape)
                        .background(ModernAccent.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        claim.claimerName.firstOrNull()?.uppercase() ?: "?",
                        fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ModernAccent
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        claim.claimerName, fontSize = 13.sp,
                        fontWeight = FontWeight.Bold, color = colors.textPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        claim.message, fontSize = 12.sp,
                        color = colors.textSecondary, lineHeight = 17.sp
                    )

                    // Photo proof — modern grid layout
                    val allPhotos = claim.photoUris.ifEmpty { listOfNotNull(claim.photoUri) }
                    if (allPhotos.isNotEmpty()) {
                        var selectedUri by remember { mutableStateOf(allPhotos.first()) }
                        Spacer(Modifier.height(10.dp))

                        // Section label
                        Row(
                            modifier = Modifier.padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(3.dp).clip(CircleShape)
                                .background(colors.textMuted))
                            Text("PROOF PHOTOS (${allPhotos.size})",
                                fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                color = colors.textMuted, letterSpacing = 0.5.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            allPhotos.forEachIndexed { index, uri ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(if (allPhotos.size == 1) 160.dp else 110.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedUri = uri; showPhotoDialog = true }
                                ) {
                                    Image(
                                        rememberAsyncImagePainter(uri), "Proof ${index + 1}",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    // Subtle scrim
                                    Box(modifier = Modifier.fillMaxSize()
                                        .background(Color.Black.copy(0.08f)))
                                    // Subtle bottom scrim only — no overlay icon
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(28.dp)
                                            .align(Alignment.BottomCenter)
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color.Transparent, Color.Black.copy(0.35f))
                                                )
                                            )
                                    )
                                }
                            }
                        }

                        if (showPhotoDialog) {
                            PhotoPreviewDialog(photoUri = selectedUri, onDismiss = { showPhotoDialog = false })
                        }
                    }

                    // Approve / Reject buttons (owner only)
                    if (isOwner) {
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                onClick = onReject,
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = ModernLost.copy(0.08f),
                                border = BorderStroke(1.dp, ModernLost.copy(0.35f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("✕", fontSize = 12.sp, color = ModernLost)
                                        Text("Reject", fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold, color = ModernLost)
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier.weight(1f).height(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Brush.linearGradient(listOf(ModernFound, Color(0xFF20B080))))
                                    .clickable(onClick = onApprove),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("✓", fontSize = 12.sp, color = Color.White)
                                    Text("Approve", fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    // Withdraw button — only visible to the claimer, not the owner
                    if (isClaimer && !isOwner && onWithdraw != null) {
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            onClick = { showWithdrawDialog = true },
                            modifier = Modifier.fillMaxWidth().height(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = colors.pillBg,
                            border = BorderStroke(1.dp, colors.cardBorder)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("↩", fontSize = 13.sp, color = colors.textMuted)
                                    Text("Withdraw & Resubmit",
                                        fontSize = 12.sp, fontWeight = FontWeight.Medium,
                                        color = colors.textSecondary)
                                }
                            }
                        }
                        Text(
                            "You can withdraw your claim and submit a new one while it's still pending.",
                            fontSize = 9.sp, color = colors.textMuted,
                            modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                        )
                    }
                }
            }
        }
    }

    // Withdraw confirmation dialog
    if (showWithdrawDialog) {
        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            icon  = { Text("↩", fontSize = 28.sp) },
            title = { Text("Withdraw Claim?", fontWeight = FontWeight.Bold) },
            text  = {
                Text("This will delete your current claim. You can then submit a new one with the correct details.")
            },
            confirmButton = {
                Button(
                    onClick = { showWithdrawDialog = false; onWithdraw?.invoke() },
                    colors  = ButtonDefaults.buttonColors(containerColor = ModernLost)
                ) { Text("Withdraw", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Photo dialog handled inline in the photos row above
}
// ══════════════════════════════════════════════════════════════════════════

@Composable
private fun PhotoPreviewDialog(photoUri: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true, dismissOnClickOutside = true, usePlatformDefaultWidth = false
        )
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black).clickable { onDismiss() }) {
            Image(
                painter = rememberAsyncImagePainter(model = photoUri),
                contentDescription = "Full screen photo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
            // Top bar
            Box(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(Color.Black.copy(0.7f), Color.Transparent)))
                .padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Photo", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Surface(onClick = onDismiss, shape = CircleShape, color = Color.White.copy(0.2f)) {
                        Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                            Text("✕", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            // Bottom hint
            Box(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.6f))))
                .padding(16.dp)) {
                Text("Tap anywhere to close", fontSize = 12.sp,
                    color = Color.White.copy(0.7f), textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// DIALOGS & HELPERS
// ══════════════════════════════════════════════════════════════════════════

@Composable
fun LimitReminderDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon    = { Text("⚠️", fontSize = 32.sp) },
        title   = { Text("Limit Reached", fontWeight = FontWeight.Bold) },
        text    = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK", fontWeight = FontWeight.Bold) }
        }
    )
}

fun formatDate(timestamp: Long): String =
    SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))

fun formatTime(timestamp: Long): String =
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))

fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000     -> "just now"
        diff < 3_600_000  -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else              -> "${diff / 86_400_000}d ago"
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000       -> "Just now"
        diff < 3_600_000    -> "${diff / 60_000}m ago"
        diff < 86_400_000   -> "${diff / 3_600_000}h ago"
        diff < 604_800_000  -> "${diff / 86_400_000}d ago"
        else                -> "${diff / 604_800_000}w ago"
    }
}