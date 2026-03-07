package com.campusfind.ui.screens.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.campusfind.domain.model.ItemStatus
import com.campusfind.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Modern DetailScreen with immersive photo hero
 * Wireframe: lines 1899-2200
 */
@Composable
fun DetailScreen(
    itemId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,  // ← ADD THIS
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val item = uiState.item
    val isOwner = uiState.isOwner
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPhotoDialog by remember { mutableStateOf(false) }  // ← ADD THIS

    LaunchedEffect(itemId) {
        viewModel.loadItem(itemId)
    }

    if (item == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = ModernAccent)
            } else {
                Text("Item not found", color = ModernError)
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFf4f4f0))
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // ═══ IMMERSIVE PHOTO HERO (200dp) ═══
            item {
                PhotoHero(
                    item = item,
                    onNavigateBack = onNavigateBack,
                    onPhotoClick = { showPhotoDialog = true }  // ← ADD THIS
                )
            }

            // ═══ META STRIP ═══
            item {
                MetaStrip(
                    reportedAt = item.reportedAt
                )
            }

            // ═══ DESCRIPTION ═══
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                        Text(
                            text = "DESCRIPTION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFaaaaaa),
                            letterSpacing = 0.06.sp,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                        Text(
                            text = item.description,
                            fontSize = 12.sp,
                            color = Color(0xFF333333),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // ═══ REPORTER INFO ═══
            item {
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    border = BorderStroke(width = 1.dp, color = Color(0xFFf0f0ec))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "REPORTED BY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFaaaaaa),
                            letterSpacing = 0.06.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color(0xFFe8e8e4), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👤", fontSize = 18.sp)
                            }
                            Column {
                                Text(
                                    text = uiState.reporterName ?: "Loading...",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1a1a2e)
                                )
                                Text(
                                    text = formatDate(item.reportedAt),
                                    fontSize = 11.sp,
                                    color = Color(0xFF888888)
                                )
                            }
                        }
                    }
                }
            }

            // ═══ PHASE 2 PLACEHOLDER ═══
            item {
                Spacer(Modifier.height(12.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFf4f3ff),
                    border = BorderStroke(width = 1.5.dp, color = Color(0xFFd8d5ff))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("💬", fontSize = 16.sp)
                        Column {
                            Text(
                                text = "Claims & Tips",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ModernAccent
                            )
                            Text(
                                text = "Available in Phase 2 with cloud sync",
                                fontSize = 10.sp,
                                color = Color(0xFF7a73c8),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(80.dp)) // Space for bottom bar
            }
        }

        // ═══ BOTTOM ACTION BAR (only visible to owner) ═══
        if (isOwner) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = Color.White,
                shadowElevation = 4.dp,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Mark as Found/Lost button
                    Button(
                        onClick = {
                            val newStatus = if (item.status == ItemStatus.LOST) {
                                ItemStatus.FOUND
                            } else {
                                ItemStatus.LOST
                            }
                            viewModel.onStatusUpdated(item.id, newStatus)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(ModernAccent, Color(0xFF5246d5))
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (item.status == ItemStatus.LOST) "✓" else "↺",
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = if (item.status == ItemStatus.LOST) "Mark as Found" else "Mark as Lost",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Overflow menu
                    var showMenu by remember { mutableStateOf(false) }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFf4f4f0),
                                border = BorderStroke(width = 1.5.dp, color = Color(0xFFebebeb))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "More options",
                                        tint = Color(0xFF666666)
                                    )
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("✏️ Edit") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToEdit(item.id)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🗑️ Delete", color = ModernError) },
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

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Report?") },
            text = { Text("This action cannot be undone. The item will be permanently deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDeleteItem(item.id)
                        onNavigateBack()
                    }
                ) {
                    Text("Delete", color = ModernError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Photo viewer dialog
    if (showPhotoDialog) {
        PhotoViewerDialog(
            onDismiss = { showPhotoDialog = false }
        )
    }
}

@Composable
fun PhotoHero(
    item: com.campusfind.domain.model.LostItem,
    onNavigateBack: () -> Unit,
    onPhotoClick: () -> Unit  // ← ADD THIS
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clickable(onClick = onPhotoClick)  // ← ADD THIS - makes entire hero clickable
    ) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1a1228),
                            Color(0xFF2e1f48),
                            Color(0xFF1a2a20)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
        )

        // Ambient blur orbs (layered)
        Box(
            modifier = Modifier
                .size(140.dp)
                .offset(x = 20.dp, y = (-10).dp)
                .align(Alignment.TopEnd)
                .background(ModernAccent.copy(alpha = 0.12f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = 10.dp, y = 0.dp)
                .align(Alignment.TopEnd)
                .background(ModernAccent.copy(alpha = 0.18f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(100.dp)
                .offset(x = 0.dp, y = 10.dp)
                .align(Alignment.TopEnd)
                .background(ModernAccent.copy(alpha = 0.25f), CircleShape)
        )

        Box(
            modifier = Modifier
                .size(110.dp)
                .offset(x = 10.dp, y = 20.dp)
                .align(Alignment.BottomStart)
                .background(ModernFound.copy(alpha = 0.10f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(90.dp)
                .offset(x = 10.dp, y = 10.dp)
                .align(Alignment.BottomStart)
                .background(ModernFound.copy(alpha = 0.15f), CircleShape)
        )

        // TODO: Actual photo with AsyncImage when photoUrl exists

        // Photo placeholder
        Box(
            modifier = Modifier.align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Text("📷", fontSize = 60.sp, color = Color.White.copy(alpha = 0.25f))
        }

        // Status bar overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .background(Color.Black.copy(alpha = 0.35f))
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "9:41",
                    fontSize = 10.sp,
                    color = Color.White,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Text(
                    "●●● 82%",
                    fontSize = 10.sp,
                    color = Color.White,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }

        // Frosted back button
        Surface(
            onClick = onNavigateBack,
            modifier = Modifier
                .padding(top = 36.dp, start = 12.dp)
                .align(Alignment.TopStart),
            shape = RoundedCornerShape(22.dp),
            color = Color.Black.copy(alpha = 0.38f),
            border = BorderStroke(width = 1.dp, color = Color.White.copy(alpha = 0.18f)),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "‹",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.offset(x = (-1).dp)
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

        // Title + status overlay at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF0a0814).copy(alpha = 0.6f),
                            Color(0xFF0a0814).copy(alpha = 0.92f)
                        )
                    )
                )
                .align(Alignment.BottomCenter)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = item.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        style = LocalTextStyle.current.copy(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(0f, 1f),
                                blurRadius = 6f
                            )
                        )
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "📍 Campus", // TODO: Add location field
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.55f)
                    )
                }

                // Status badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when (item.status) {
                        ItemStatus.LOST -> ModernLost.copy(alpha = 0.25f)
                        ItemStatus.FOUND -> ModernFound.copy(alpha = 0.25f)
                    },
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = when (item.status) {
                            ItemStatus.LOST -> ModernLost.copy(alpha = 0.55f)
                            ItemStatus.FOUND -> ModernFound.copy(alpha = 0.55f)
                        }
                    )
                ) {
                    Text(
                        text = "● ${item.status.name}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = when (item.status) {
                            ItemStatus.LOST -> Color(0xFFff8099)
                            ItemStatus.FOUND -> ModernFound
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MetaStrip(reportedAt: Long) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 9.dp)
        ) {
            MetaCell(
                label = "REPORTED",
                value = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(reportedAt)),
                divider = true
            )
            MetaCell(
                label = "TIME",
                value = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(reportedAt)),
                divider = true
            )
            MetaCell(
                label = "STATUS",
                value = "Local", // Phase 1: all items are local-only
                valueColor = Color(0xFF888888),
                divider = false
            )
        }
    }
}

@Composable
fun RowScope.MetaCell(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF1a1a2e),
    divider: Boolean
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            color = Color(0xFFaaaaaa),
            letterSpacing = 0.06.sp
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
    if (divider) {
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(32.dp)
                .background(Color(0xFFf0f0ec))
                .align(Alignment.CenterVertically)
        )
    }
}

fun formatDate(millis: Long): String {
    return SimpleDateFormat("MMMM dd, yyyy 'at' h:mm a", Locale.getDefault()).format(Date(millis))
}

@Composable
fun PhotoViewerDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            // Phase 1: No photo support yet
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("📷", fontSize = 72.sp, color = Color.White.copy(alpha = 0.3f))
                Text(
                    "Photo support coming in Phase 2",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Text(
                    "Photos will be added with cloud sync",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }

            // Close button
            Surface(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(44.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "✕",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}