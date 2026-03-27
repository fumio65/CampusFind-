package com.campusfind.ui.screens.detail

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.Claim
import com.campusfind.domain.model.ClaimStatus
import com.campusfind.domain.model.Tip
import com.campusfind.domain.model.ClaimReply
import com.campusfind.ui.screens.detail.DetailViewModel.Companion.MAX_TIP_LENGTH
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * DetailScreen - Complete version with inline claims + reply feature
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    itemId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToSubmitClaim: (String) -> Unit,
    onNavigateToReviewClaims: (String) -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(itemId) {
        viewModel.loadItem(itemId)
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }

    if (uiState.showLimitDialog != null) {
        LimitReminderDialog(
            message = uiState.showLimitDialog ?: "",
            onDismiss = { viewModel.dismissLimitDialog() }
        )
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF6C63FF),
            surface = Color.White,
            background = Color(0xFFF4F4F0),
            onSurface = Color(0xFF1a1a2e),
            onBackground = Color(0xFF333333)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF4F4F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF6C63FF))
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF4F4F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            uiState.error ?: "Error loading item",
                            color = Color(0xFFFF4D6D),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                uiState.item != null -> {
                    val item = uiState.item!!

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF4F4F0))
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // IMMERSIVE HERO (210dp)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(210.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(
                                                        Color(0xFF1a1228),
                                                        Color(0xFF2e1f48),
                                                        Color(0xFF1a2a20)
                                                    )
                                                )
                                            )
                                    )

                                    item.photoUri?.let { photoUri ->
                                        val photoFile = File(photoUri)
                                        if (photoFile.exists()) {
                                            Image(
                                                painter = rememberAsyncImagePainter(photoFile),
                                                contentDescription = item.title,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color.Black.copy(alpha = 0.3f))
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 10.dp, y = (-10).dp)
                                            .size(120.dp)
                                            .background(
                                                Color(0xFF6c63ff).copy(alpha = 0.25f),
                                                shape = CircleShape
                                            )
                                            .blur(36.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .offset(x = 10.dp, y = 10.dp)
                                            .size(90.dp)
                                            .background(
                                                Color(0xFF2dd4a0).copy(alpha = 0.18f),
                                                shape = CircleShape
                                            )
                                            .blur(28.dp)
                                    )
                                }

                                Surface(
                                    onClick = onNavigateBack,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(start = 12.dp, top = 36.dp),
                                    shape = RoundedCornerShape(22.dp),
                                    color = Color.Black.copy(alpha = 0.38f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                                    shadowElevation = 8.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(20.dp),
                                            shape = CircleShape,
                                            color = Color.White.copy(alpha = 0.15f)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    "‹",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.offset(x = (-1).dp)
                                                )
                                            }
                                        }
                                        Text(
                                            "Back",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(end = 12.dp, top = 36.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(32.dp),
                                        shape = CircleShape,
                                        color = Color.Black.copy(alpha = 0.38f),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("↗", fontSize = 13.sp, color = Color.White)
                                        }
                                    }
                                    Surface(
                                        modifier = Modifier.size(32.dp),
                                        shape = CircleShape,
                                        color = Color.Black.copy(alpha = 0.38f),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("🔖", fontSize = 13.sp)
                                        }
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(end = 12.dp, bottom = 64.dp),
                                    shape = RoundedCornerShape(7.dp),
                                    color = Color.Black.copy(alpha = 0.4f)
                                ) {
                                    Text(
                                        "⤢ Tap to expand",
                                        fontSize = 9.sp,
                                        color = Color.White.copy(alpha = 0.65f),
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color(0xFF0a0814).copy(alpha = 0.6f),
                                                    Color(0xFF0a0814).copy(alpha = 0.92f)
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
                                        Column(
                                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                                        ) {
                                            Text(
                                                item.title,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                lineHeight = 20.sp,
                                                style = androidx.compose.ui.text.TextStyle(
                                                    shadow = androidx.compose.ui.graphics.Shadow(
                                                        color = Color.Black.copy(alpha = 0.5f),
                                                        offset = androidx.compose.ui.geometry.Offset(0f, 1f),
                                                        blurRadius = 6f
                                                    )
                                                )
                                            )
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                Text("📍", fontSize = 10.sp)
                                                Text(
                                                    item.location ?: "Location not specified",
                                                    fontSize = 10.sp,
                                                    color = Color.White.copy(alpha = 0.55f)
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = when (item.status) {
                                                ItemStatus.LOST -> Color(0xFFff4d6d).copy(alpha = 0.25f)
                                                ItemStatus.FOUND -> Color(0xFF2dd4a0).copy(alpha = 0.25f)
                                            },
                                            border = BorderStroke(
                                                1.5.dp,
                                                when (item.status) {
                                                    ItemStatus.LOST -> Color(0xFFff4d6d).copy(alpha = 0.55f)
                                                    ItemStatus.FOUND -> Color(0xFF2dd4a0).copy(alpha = 0.55f)
                                                }
                                            )
                                        ) {
                                            Text(
                                                "● ${item.status.name}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = when (item.status) {
                                                    ItemStatus.LOST -> Color(0xFFff8099)
                                                    ItemStatus.FOUND -> Color(0xFF2dd4a0)
                                                },
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Meta strip
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF4F4F0))
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("REPORTED", fontSize = 8.sp, color = Color(0xFFAAAAAA))
                                    Text(formatDate(item.reportedAt), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("TIME", fontSize = 8.sp, color = Color(0xFFAAAAAA))
                                    Text(formatTime(item.reportedAt), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("SYNC", fontSize = 8.sp, color = Color(0xFFAAAAAA))
                                    Text("Local", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6c63ff))
                                }
                            }

                            // Scrollable content
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.White)
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 14.dp)
                            ) {
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    "DESCRIPTION",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFAAAAAA)
                                )
                                Spacer(modifier = Modifier.height(5.dp))
                                Text(
                                    item.description,
                                    fontSize = 12.sp,
                                    color = Color(0xFF333333),
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(28.dp),
                                        shape = RoundedCornerShape(9.dp),
                                        color = Color.Transparent
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.linearGradient(
                                                        colors = listOf(Color(0xFF6c63ff), Color(0xFF5246d5))
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("👤", fontSize = 13.sp)
                                        }
                                    }

                                    Column {
                                        Text("Posted by", fontSize = 10.sp, color = Color(0xFFAAAAAA))
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                uiState.reporterName ?: "Unknown",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1a1a2e)
                                            )
                                            Text(
                                                "· ${formatRelativeTime(item.reportedAt)}",
                                                fontSize = 11.sp,
                                                color = Color(0xFFAAAAAA)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEBEBEB)))
                                Spacer(modifier = Modifier.height(8.dp))

                                // INLINE CLAIM SUBMISSION
                                if (!uiState.isOwner && item.status == ItemStatus.LOST) {
                                    val hasPending = uiState.claims.any { it.status == ClaimStatus.PENDING }
                                    val hasApproved = uiState.claims.any { it.status == ClaimStatus.APPROVED }  // ✅ ADD THIS LINE

                                    if (hasPending) {
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFFFF4E6),
                                            border = BorderStroke(1.dp, Color(0xFFFFE0B2))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("⏳", fontSize = 18.sp)
                                                Column {
                                                    Text(
                                                        "Claim Under Review",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFE65100)
                                                    )
                                                    Text(
                                                        "The reporter is reviewing a claim. You'll be able to submit if it's rejected.",
                                                        fontSize = 9.sp,
                                                        color = Color(0xFF6D4C41),
                                                        lineHeight = 12.sp
                                                    )
                                                }
                                            }
                                        }
                                    } else if (!hasApproved) {  // ✅ CHANGE: from "} else {" to "} else if (!hasApproved) {"
                                        IFoundThisItemSection(
                                            onSubmit = { location, photoUri ->
                                                viewModel.submitClaimInline(location, photoUri)
                                            }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                // CLAIMS DISPLAY
                                if (uiState.claims.isNotEmpty()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                    ) {
                                        uiState.claims.forEach { claim ->
                                            when (claim.status) {
                                                ClaimStatus.APPROVED -> {
                                                    PinnedVerifiedClaim(
                                                        claim = claim,
                                                        isOwner = uiState.isOwner,
                                                        onReply = { replyMessage ->
                                                            viewModel.replyToClaim(claim.id, replyMessage)
                                                        },
                                                        messengerUsername = claim.messengerUsername,
                                                        messageCount = viewModel.getReplyCount(claim.id),
                                                        replies = viewModel.getReplies(claim.id),       // ✅ ADD THIS
                                                        currentUserId = viewModel.getCurrentUserId()    // ✅ ADD THIS
                                                    )
                                                }
                                                ClaimStatus.PENDING -> {
                                                    PendingClaimCard(
                                                        claim = claim,
                                                        isOwner = uiState.isOwner,
                                                        onApprove = { viewModel.approveClaim(claim.id) },
                                                        onReject = { viewModel.rejectClaim(claim.id) }
                                                    )
                                                }
                                                ClaimStatus.REJECTED -> {
                                                    // Don't display rejected claims
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                // Public Tips
                                PublicTipsSection(
                                    tips = uiState.tips,
                                    tipCount = uiState.tipCount,
                                    isOwner = uiState.isOwner,
                                    hasUserTipped = uiState.hasUserTipped,
                                    currentUserId = viewModel.getCurrentUserId(),
                                    reportOwnerId = item.reportedBy,
                                    onSubmitTip = { message -> viewModel.submitTip(message) },
                                    onSubmitReply = { tipId, message -> viewModel.submitReply(tipId, message) }
                                )

                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }

                        // Sticky bottom (owner only)
                        if (uiState.isOwner && item.status == ItemStatus.LOST) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth(),
                                color = Color.White,
                                shadowElevation = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        onClick = { viewModel.markAsFound(itemId, item) },
                                        modifier = Modifier.weight(1f).height(44.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.Transparent
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.linearGradient(
                                                        colors = listOf(Color(0xFF6c63ff), Color(0xFF5246d5))
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                                Text("✓", fontSize = 14.sp, color = Color.White)
                                                Text(
                                                    "Mark as Found",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }

                                    Box {
                                        Surface(
                                            onClick = { showMenu = true },
                                            modifier = Modifier.size(44.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFFF4F4F0),
                                            border = BorderStroke(1.5.dp, Color(0xFFEBEBEB))
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("⋮", fontSize = 16.sp, color = Color(0xFF1a1a2e))
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = showMenu,
                                            onDismissRequest = { showMenu = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Edit Item") },
                                                onClick = {
                                                    showMenu = false
                                                    onNavigateToEdit(itemId)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Delete Item", color = Color(0xFFFF4D6D)) },
                                                onClick = {
                                                    showMenu = false
                                                    showDeleteDialog = true
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            icon = { Text("🗑️", fontSize = 32.sp) },
                            title = { Text("Delete Item?", fontWeight = FontWeight.Bold) },
                            text = { Text("This will permanently remove your lost item report.") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showDeleteDialog = false
                                        viewModel.deleteItem(onSuccess = onNavigateBack)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4D6D))
                                ) {
                                    Text("Delete", fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PublicTipsSection(
    tips: List<Tip>,
    tipCount: Int,
    isOwner: Boolean,
    hasUserTipped: Boolean,
    currentUserId: String?,
    reportOwnerId: String,
    onSubmitTip: (String) -> Unit,
    onSubmitReply: (String, String) -> Unit
) {
    var tipText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "PUBLIC TIPS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFAAAAAA),
                    letterSpacing = 0.06.sp
                )
                if (tipCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF6c63ff).copy(alpha = 0.1f)
                    ) {
                        Text(
                            "$tipCount",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6c63ff),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
            }
            Text(
                "1 tip/user",
                fontSize = 9.sp,
                color = Color(0xFFAAAAAA)
            )
        }

        Text(
            "Seen it? Leave a tip",
            fontSize = 9.sp,
            color = Color(0xFFBBBBBB)
        )

        val topLevelTips = tips.filter { !it.isReply }

        if (topLevelTips.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                topLevelTips.forEach { tip ->
                    val replies = tips.filter { it.isReply && it.parentTipId == tip.id }

                    TipCard(
                        tip = tip,
                        replies = replies,
                        isReportOwner = isOwner,
                        currentUserId = currentUserId,
                        reportOwnerId = reportOwnerId,
                        onReply = onSubmitReply
                    )
                }
            }
        }

        if (!isOwner && !hasUserTipped) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(26.dp),
                    shape = RoundedCornerShape(9.dp),
                    color = Color(0xFFE8E8E4)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👤", fontSize = 11.sp)
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(1.5.dp, Color(0xFFE4E4E0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = tipText,
                            onValueChange = {
                                if (it.length <= MAX_TIP_LENGTH) tipText = it
                            },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 11.sp, color = Color.Black),
                            decorationBox = { innerTextField ->
                                if (tipText.isEmpty()) {
                                    Text(
                                        "Seen it? Leave a tip...",
                                        fontSize = 11.sp,
                                        color = Color(0xFFCCCCCC)
                                    )
                                }
                                innerTextField()
                            }
                        )

                        Surface(
                            onClick = {
                                if (tipText.isNotBlank()) {
                                    onSubmitTip(tipText.trim())
                                    tipText = ""
                                }
                            },
                            modifier = Modifier.size(26.dp),
                            shape = CircleShape,
                            color = if (tipText.isNotBlank()) Color.Transparent else Color(0xFFE8E8E4)
                        ) {
                            if (tipText.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFF6c63ff),
                                                    Color(0xFF5246d5)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "↑",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "↑",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFCCCCCC)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Text(
                "🔒 Only the reporter can reply to tips",
                fontSize = 9.sp,
                color = Color(0xFFBBBBBB),
                modifier = Modifier.padding(start = 33.dp)
            )
        } else if (!isOwner && hasUserTipped) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF4F4F0),
                border = BorderStroke(1.5.dp, Color(0xFFE4E4E0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✓", fontSize = 14.sp, color = Color(0xFF2DD4A0))
                    Text(
                        "You've already left a comment",
                        fontSize = 11.sp,
                        color = Color(0xFF888888)
                    )
                }
            }
        } else if (isOwner) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF9F9F6),
                border = BorderStroke(1.dp, Color(0xFFE8E8E4))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔒", fontSize = 14.sp)
                    Text(
                        "You can only reply to comments",
                        fontSize = 11.sp,
                        color = Color(0xFF888888)
                    )
                }
            }
        }
    }
}

@Composable
fun TipCard(
    tip: Tip,
    replies: List<Tip>,
    isReportOwner: Boolean,
    currentUserId: String?,
    reportOwnerId: String,
    onReply: (String, String) -> Unit
) {
    var showReplyInput by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.5.dp, Color(0xFFF0F0EC))
    ) {
        Column(modifier = Modifier.padding(7.dp, 9.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Surface(
                    modifier = Modifier.size(24.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE8E8E4)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👤", fontSize = 10.sp)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                tip.authorName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF888888)
                            )
                            if (tip.authorId == reportOwnerId) {
                                Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    color = Color(0xFF6c63ff)
                                ) {
                                    Text(
                                        "Reporter",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            formatRelativeTime(tip.createdAt),
                            fontSize = 9.sp,
                            color = Color(0xFFDDDDDD),
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }

                    Text(
                        tip.message,
                        fontSize = 10.sp,
                        color = Color(0xFF666666),
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    if (isReportOwner && tip.authorId != currentUserId) {
                        Row(
                            modifier = Modifier.padding(top = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = { showReplyInput = !showReplyInput },
                                shape = RoundedCornerShape(7.dp),
                                color = Color(0xFF6c63ff).copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, Color(0xFF6c63ff).copy(alpha = 0.2f))
                            ) {
                                Text(
                                    "↩ Reply",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6c63ff),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                "only you can reply",
                                fontSize = 9.sp,
                                color = Color(0xFFCCCCCC)
                            )
                        }
                    }
                }
            }

            if (showReplyInput) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 31.dp, top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(40.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF6c63ff),
                                        Color(0xFF6c63ff).copy(alpha = 0.1f)
                                    )
                                ),
                                RoundedCornerShape(2.dp)
                            )
                    )

                    BasicTextField(
                        value = replyText,
                        onValueChange = {
                            if (it.length <= MAX_TIP_LENGTH) replyText = it
                        },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(fontSize = 10.sp, color = Color.Black),
                        decorationBox = { innerTextField ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(9.dp),
                                color = Color(0xFFF4F3FF),
                                border = BorderStroke(1.5.dp, Color(0xFFDDDCFF))
                            ) {
                                Box(modifier = Modifier.padding(6.dp, 9.dp)) {
                                    if (replyText.isEmpty()) {
                                        Text(
                                            "Write a reply...",
                                            fontSize = 10.sp,
                                            color = Color(0xFFBBBBBB)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        }
                    )

                    Surface(
                        onClick = {
                            if (replyText.isNotBlank()) {
                                onReply(tip.id, replyText.trim())
                                replyText = ""
                                showReplyInput = false
                            }
                        },
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape,
                        color = if (replyText.isNotBlank()) Color(0xFF6c63ff) else Color(0xFFE8E8E4)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "↑",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (replyText.isNotBlank()) Color.White else Color(0xFFCCCCCC)
                            )
                        }
                    }
                }
            }

            if (replies.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 31.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    replies.forEach { reply ->
                        ReplyCard(reply = reply, reportOwnerId = reportOwnerId)
                    }
                }
            }
        }
    }
}

@Composable
fun ReplyCard(reply: Tip, reportOwnerId: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(60.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6c63ff),
                            Color(0xFF6c63ff).copy(alpha = 0.1f)
                        )
                    ),
                    RoundedCornerShape(2.dp)
                )
        )

        Surface(
            modifier = Modifier.size(22.dp),
            shape = RoundedCornerShape(7.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF6c63ff),
                                Color(0xFF5246d5)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("👤", fontSize = 10.sp)
            }
        }

        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(0.dp, 9.dp, 9.dp, 9.dp),
            color = Color(0xFFF4F3FF),
            border = BorderStroke(1.5.dp, Color(0xFFDDDCFF))
        ) {
            Column(modifier = Modifier.padding(6.dp, 9.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        reply.authorName,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6c63ff)
                    )
                    if (reply.authorId == reportOwnerId) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF6c63ff)
                        ) {
                            Text(
                                "Reporter",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Text(
                    reply.message,
                    fontSize = 10.sp,
                    color = Color(0xFF2a2060),
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun IFoundThisItemSection(
    onSubmit: (String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var location by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        photoUri = uri
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFedfcf5), Color(0xFFd8fbed))
                    )
                )
                .border(
                    width = 1.5.dp,
                    color = Color(0xFFa8eecf),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(8.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Transparent,
                        shadowElevation = 6.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF0ea870), Color(0xFF2dd4a0))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✋", fontSize = 14.sp)
                        }
                    }

                    Text(
                        "I Found This Item",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0a5c3c)
                    )
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    border = BorderStroke(1.5.dp, Color(0xFFa8eecf))
                ) {
                    BasicTextField(
                        value = location,
                        onValueChange = { if (it.length <= 200) location = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(7.dp)
                            .heightIn(min = 40.dp),
                        textStyle = TextStyle(
                            fontSize = 10.sp,
                            color = Color(0xFF1a1a2e),
                            lineHeight = 14.sp
                        ),
                        decorationBox = { innerTextField ->
                            if (location.isEmpty()) {
                                Text(
                                    "Describe where you found it...",
                                    fontSize = 10.sp,
                                    color = Color(0xFFbbbbbb),
                                    lineHeight = 14.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    if (photoUri != null) {
                        Surface(
                            modifier = Modifier.size(38.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box {
                                Image(
                                    painter = rememberAsyncImagePainter(photoUri),
                                    contentDescription = "Selected photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Surface(
                                    onClick = { photoUri = null },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-4).dp)
                                        .size(12.dp),
                                    shape = CircleShape,
                                    color = Color(0xFFff4d6d),
                                    border = BorderStroke(2.dp, Color(0xFFedfcf5))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            "✕",
                                            fontSize = 7.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Surface(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            modifier = Modifier.size(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.6f),
                            border = BorderStroke(1.5.dp, Color(0xFFa8eecf))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("📷", fontSize = 14.sp, modifier = Modifier.alpha(0.5f))
                            }
                        }
                    }

                    repeat(2) {
                        Surface(
                            modifier = Modifier.size(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.6f),
                            border = BorderStroke(1.5.dp, Color(0xFFa8eecf))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("📷", fontSize = 14.sp, modifier = Modifier.alpha(0.5f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                }

                Surface(
                    onClick = {
                        if (location.isNotBlank() && photoUri != null) {
                            onSubmit(location, photoUri.toString())
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color.Transparent,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF0ea870), Color(0xFF2dd4a0))
                                )
                            )
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✋", fontSize = 12.sp)
                            Text(
                                "Submit Claim",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// ========================================================================
// COMPLETE REPLACEMENT FOR PinnedVerifiedClaim FUNCTION
// Copy this ENTIRE function and replace the existing one (lines ~1404-1574)
// ========================================================================

@Composable
private fun PinnedVerifiedClaim(
    claim: Claim,
    isOwner: Boolean,
    onReply: ((String) -> Unit)? = null,
    messengerUsername: String? = null,
    messageCount: Int = 0,
    replies: List<ClaimReply> = emptyList(),        // ✅ NEW PARAMETER
    currentUserId: String? = null,                   // ✅ NEW PARAMETER
    modifier: Modifier = Modifier
) {
    var showPhotoDialog by remember { mutableStateOf(false) }
    var showReplyInput by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }

    // ✅ NEW: Check if current user already replied
    val userAlreadyReplied = currentUserId != null && replies.any { it.authorId == currentUserId }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFedfcf5), Color(0xFFd4f7e8))
                    )
                )
                .border(
                    width = 1.5.dp,
                    color = Color(0xFFa8eecf),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(6.dp)
        ) {
            Column {
                Text(
                    "📌 PINNED · Verified Claim",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0ea870),
                    modifier = Modifier.padding(bottom = 5.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Box {
                        Surface(
                            modifier = Modifier.size(26.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF2dd4a0), Color(0xFF0ea870))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👤", fontSize = 12.sp)
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 2.dp, y = 2.dp)
                                .size(12.dp),
                            shape = CircleShape,
                            color = Color(0xFF0ea870),
                            border = BorderStroke(2.dp, Color(0xFFedfcf5))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "✓",
                                    fontSize = 7.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            Text(
                                claim.claimerName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1a1a2e)
                            )

                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = Color(0xFF0ea870)
                            ) {
                                Text(
                                    "✓ Finder",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Text(
                                formatTimeAgo(claim.claimedAt),
                                fontSize = 9.sp,
                                color = Color(0xFFbbbbbb),
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            claim.message,
                            fontSize = 10.sp,
                            color = Color(0xFF1a4a35),
                            lineHeight = 14.sp,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )

                        claim.photoUri?.let { photoUri ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.padding(bottom = 5.dp)
                            ) {
                                Surface(
                                    onClick = { showPhotoDialog = true },
                                    modifier = Modifier.size(50.dp, 38.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.5.dp, Color(0xFF0ea870))
                                ) {
                                    Box {
                                        Image(
                                            painter = rememberAsyncImagePainter(photoUri),
                                            contentDescription = "Proof photo - Tap to enlarge",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Surface(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(2.dp),
                                            shape = RoundedCornerShape(3.dp),
                                            color = Color.Black.copy(alpha = 0.5f)
                                        ) {
                                            Text(
                                                "🔍",
                                                fontSize = 8.sp,
                                                modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ====================================================
                        // ✅ NEW SECTION: DISPLAY REPLIES
                        // ====================================================
                        if (replies.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                replies.forEach { reply ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        // Thread line
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(60.dp)
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color(0xFF0ea870),
                                                            Color(0xFF0ea870).copy(alpha = 0.1f)
                                                        )
                                                    ),
                                                    RoundedCornerShape(2.dp)
                                                )
                                        )

                                        // Reply bubble
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(0.dp, 9.dp, 9.dp, 9.dp),
                                            color = Color.White,
                                            border = BorderStroke(1.5.dp, Color(0xFF0ea870).copy(alpha = 0.3f))
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        reply.authorName,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF0ea870)
                                                    )
                                                    Text(
                                                        formatTimeAgo(reply.createdAt),
                                                        fontSize = 8.sp,
                                                        color = Color(0xFFbbbbbb),
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }

                                                Text(
                                                    reply.message,
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF1a4a35),
                                                    lineHeight = 14.sp,
                                                    modifier = Modifier.padding(top = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ====================================================
                        // ✅ UPDATED: Reply button logic with 1-per-person limit
                        // ====================================================
                        if (onReply != null) {
                            if (messageCount >= 2 && !messengerUsername.isNullOrBlank()) {
                                // Show Messenger link after 2 messages
                                Surface(
                                    onClick = {
                                        // TODO: Open Messenger deep link
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Transparent,
                                    border = BorderStroke(1.5.dp, Color(0xFF0ea870))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(
                                                        Color(0xFF0084ff).copy(alpha = 0.1f),
                                                        Color(0xFF00c6ff).copy(alpha = 0.1f)
                                                    )
                                                )
                                            )
                                            .padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("💬", fontSize = 14.sp)
                                            Column {
                                                Text(
                                                    "Message on Messenger",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF0084ff)
                                                )
                                                Text(
                                                    "@$messengerUsername",
                                                    fontSize = 9.sp,
                                                    color = Color(0xFF0084ff).copy(alpha = 0.7f)
                                                )
                                            }
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text(
                                                "→",
                                                fontSize = 14.sp,
                                                color = Color(0xFF0084ff)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    "💡 2-message limit reached. Continue on Messenger to save server costs!",
                                    fontSize = 8.sp,
                                    color = Color(0xFF666666),
                                    lineHeight = 10.sp,
                                    modifier = Modifier.padding(top = 3.dp)
                                )
                            } else if (userAlreadyReplied) {
                                // ✅ NEW: Show disabled state when user already replied
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFf0f0f0),
                                    border = BorderStroke(1.dp, Color(0xFFdddddd))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("✓", fontSize = 12.sp, color = Color(0xFF0ea870))
                                        Text(
                                            "You've sent your message",
                                            fontSize = 9.sp,
                                            color = Color(0xFF888888)
                                        )
                                    }
                                }
                                Text(
                                    "⏳ Waiting for finder's reply...",
                                    fontSize = 8.sp,
                                    color = Color(0xFF888888),
                                    modifier = Modifier.padding(top = 3.dp)
                                )
                            } else {
                                // Show reply button - user can still reply
                                Row(
                                    modifier = Modifier.padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        onClick = { showReplyInput = !showReplyInput },
                                        shape = RoundedCornerShape(7.dp),
                                        color = Color(0xFF0ea870).copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, Color(0xFF0ea870).copy(alpha = 0.3f))
                                    ) {
                                        Text(
                                            "💬 Reply to finder",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0ea870),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Text(
                                        "${messageCount}/2 messages",
                                        fontSize = 8.sp,
                                        color = Color(0xFFAAAAAA)
                                    )
                                }
                            }
                        }
                    }
                }

                // Reply input (when button clicked)
                if (showReplyInput && onReply != null && messageCount < 2 && !userAlreadyReplied) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(50.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF0ea870),
                                            Color(0xFF0ea870).copy(alpha = 0.1f)
                                        )
                                    ),
                                    RoundedCornerShape(2.dp)
                                )
                        )

                        BasicTextField(
                            value = replyText,
                            onValueChange = {
                                if (it.length <= 200) replyText = it
                            },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 10.sp, color = Color.Black),
                            decorationBox = { innerTextField ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(9.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.5.dp, Color(0xFF0ea870).copy(alpha = 0.3f))
                                ) {
                                    Box(modifier = Modifier.padding(10.dp)) {
                                        if (replyText.isEmpty()) {
                                            Text(
                                                "Arrange meetup details...",
                                                fontSize = 10.sp,
                                                color = Color(0xFFBBBBBB)
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            }
                        )

                        Surface(
                            onClick = {
                                if (replyText.isNotBlank()) {
                                    onReply(replyText.trim())
                                    replyText = ""
                                    showReplyInput = false
                                }
                            },
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = if (replyText.isNotBlank()) Color(0xFF0ea870) else Color(0xFFE8E8E4)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "↑",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (replyText.isNotBlank()) Color.White else Color(0xFFCCCCCC)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPhotoDialog && claim.photoUri != null) {
        PhotoPreviewDialog(
            photoUri = claim.photoUri,
            onDismiss = { showPhotoDialog = false }
        )
    }
}
@Composable
private fun PendingClaimCard(
    claim: Claim,
    isOwner: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPhotoDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFFFF9E6),
        border = BorderStroke(1.5.dp, Color(0xFFFFE0B2))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "⏳ Under Review",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFE65100)
                )

                Surface(
                    shape = RoundedCornerShape(5.dp),
                    color = Color(0xFFffa500).copy(alpha = 0.2f)
                ) {
                    Text(
                        "PENDING",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFffa500),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFffa500)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("👤", fontSize = 14.sp)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            claim.claimerName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1a1a2e)
                        )
                        Text(
                            formatTimeAgo(claim.claimedAt),
                            fontSize = 9.sp,
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        claim.message,
                        fontSize = 10.sp,
                        color = Color(0xFF5D4037),
                        lineHeight = 13.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    claim.photoUri?.let { photoUri ->
                        Column(
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                "PROOF PHOTO",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100),
                                letterSpacing = 0.05.sp,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )

                            Surface(
                                onClick = { showPhotoDialog = true },
                                modifier = Modifier.size(120.dp, 90.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFF5E6),
                                border = BorderStroke(2.dp, Color(0xFFffa500)),
                                shadowElevation = 2.dp
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(
                                            model = photoUri
                                        ),
                                        contentDescription = "Proof photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(30.dp)
                                            .align(Alignment.BottomCenter)
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        Color.Black.copy(alpha = 0.6f)
                                                    )
                                                )
                                            )
                                    )

                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🔍", fontSize = 12.sp)
                                        Text(
                                            "Tap to verify",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isOwner) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                onClick = onApprove,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp),
                                shape = RoundedCornerShape(7.dp),
                                color = Color(0xFF2dd4a0)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text("✓", fontSize = 11.sp, color = Color.White)
                                        Text(
                                            "Approve",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Surface(
                                onClick = onReject,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp),
                                shape = RoundedCornerShape(7.dp),
                                color = Color.Transparent,
                                border = BorderStroke(1.dp, Color(0xFFff4d6d))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text("✕", fontSize = 11.sp, color = Color(0xFFff4d6d))
                                        Text(
                                            "Reject",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFff4d6d)
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

    if (showPhotoDialog && claim.photoUri != null) {
        PhotoPreviewDialog(
            photoUri = claim.photoUri,
            onDismiss = { showPhotoDialog = false }
        )
    }
}

@Composable
private fun PhotoPreviewDialog(
    photoUri: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() }
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = photoUri
                ),
                contentDescription = "Proof photo - full view",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Proof Photo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Surface(
                        onClick = onDismiss,
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "✕",
                                fontSize = 18.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Pinch to zoom • Tap to close",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Verify the item matches before approving",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000 -> "just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> "${diff / 86400_000}d ago"
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 604800_000 -> "${diff / 86400_000}d ago"
        else -> "${diff / 604800_000}w ago"
    }
}

@Composable
fun LimitReminderDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Text("⚠️", fontSize = 32.sp) },
        title = { Text("Limit Reached", fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK", fontWeight = FontWeight.Bold)
            }
        }
    )
}