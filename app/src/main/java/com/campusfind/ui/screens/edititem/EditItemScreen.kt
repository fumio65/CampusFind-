package com.campusfind.ui.screens.edititem

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.campusfind.ui.components.HeroBackButton
import com.campusfind.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemScreen(
    itemId: String,
    onNavigateBack: () -> Unit,
    viewModel: EditItemViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors  = LocalAppColors.current

    LaunchedEffect(itemId) { viewModel.loadItem(itemId) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> if (uri != null) viewModel.onPhotoSelected(uri) }

    Box(modifier = Modifier.fillMaxSize().background(colors.screenBg)) {
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ModernAccent, strokeWidth = 2.dp)
                }
            }
            uiState.error != null && !uiState.isItemLoaded -> {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("❌", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(uiState.error ?: "Error loading item",
                        color = ModernError, fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 24.dp))
                }
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {

                    // ── Hero ───────────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF12101E), Color(0xFF1E1340), Color(0xFF0E1F18))
                                )
                            )
                    ) {
                        // Ambient orbs
                        Box(modifier = Modifier.size(130.dp).offset(x = 230.dp, y = (-20).dp)
                            .background(ModernAccent.copy(0.18f), CircleShape).blur(40.dp))
                        Box(modifier = Modifier.size(90.dp).offset(x = (-10).dp, y = 90.dp)
                            .background(ModernFound.copy(0.12f), CircleShape).blur(30.dp))

                        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                            Spacer(Modifier.height(8.dp))

                            // Back / Cancel button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp)
                            ) {
                                HeroBackButton(
                                    onClick = onNavigateBack,
                                    modifier = Modifier.align(Alignment.CenterStart)
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            // Title
                            Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("✏️", fontSize = 22.sp)
                                    Text("Edit Report",
                                        fontSize = 24.sp, fontWeight = FontWeight.Black,
                                        color = Color.White)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text("Update your lost item details",
                                    fontSize = 12.sp, color = Color.White.copy(0.6f))
                            }
                        }
                    }

                    // ── Scrollable form ────────────────────────────────────
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(colors.screenBg)
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Error banner
                        if (uiState.error != null && uiState.isItemLoaded) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = ModernError.copy(0.10f),
                                border = BorderStroke(1.dp, ModernError.copy(0.3f))
                            ) {
                                Text(uiState.error ?: "", color = ModernError,
                                    fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                            }
                        }

                        // Item Title
                        FormField(label = "ITEM TITLE") {
                            FormInput(
                                value    = uiState.title,
                                onChange = { viewModel.onTitleChanged(it) },
                                icon     = "📦",
                                hint     = "e.g., Black Wallet",
                                colors   = colors
                            )
                        }

                        // Description
                        FormField(label = "DESCRIPTION") {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape    = RoundedCornerShape(14.dp),
                                color    = colors.cardBg,
                                border   = BorderStroke(1.dp, colors.cardBorder)
                            ) {
                                BasicTextField(
                                    value = uiState.description,
                                    onValueChange = { viewModel.onDescriptionChanged(it) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                        .height(100.dp),
                                    textStyle = TextStyle(
                                        fontSize = 13.sp, lineHeight = 20.sp,
                                        color = colors.textPrimary
                                    ),
                                    decorationBox = { inner ->
                                        if (uiState.description.isEmpty()) {
                                            Text("Describe the item, where it was lost, and when...",
                                                fontSize = 13.sp, color = colors.textMuted,
                                                lineHeight = 20.sp)
                                        }
                                        inner()
                                    }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Be specific to help others identify it",
                                    fontSize = 9.sp, color = colors.textMuted)
                                Text("${uiState.description.length} / 500",
                                    fontSize = 9.sp, color = colors.textMuted)
                            }
                        }

                        // Location
                        FormField(
                            label    = "LAST SEEN LOCATION",
                            optional = true
                        ) {
                            FormInput(
                                value    = uiState.location,
                                onChange = { viewModel.onLocationChanged(it) },
                                icon     = "📍",
                                hint     = "e.g., Library entrance",
                                colors   = colors
                            )
                        }

                        // Photo
                        FormField(label = "PHOTO", optional = true) {
                            val hasPhoto = uiState.selectedPhotoUri != null ||
                                    (!uiState.currentPhotoUri.isNullOrBlank())

                            if (hasPhoto) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(200.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                ) {
                                    // New selected photo (Uri) takes priority
                                    if (uiState.selectedPhotoUri != null) {
                                        Image(
                                            painter = rememberAsyncImagePainter(uiState.selectedPhotoUri),
                                            contentDescription = "Selected photo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        // Current photo from DB (file path String)
                                        val currentPath = uiState.currentPhotoUri
                                        if (!currentPath.isNullOrBlank()) {
                                            val photoFile = remember(currentPath) { File(currentPath) }
                                            if (photoFile.exists()) {
                                                Image(
                                                    painter = rememberAsyncImagePainter(photoFile),
                                                    contentDescription = "Current photo",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        }
                                    }

                                    // Dark scrim at bottom
                                    Box(modifier = Modifier.fillMaxWidth().height(60.dp)
                                        .align(Alignment.BottomCenter)
                                        .background(Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(0.5f)))))

                                    // Remove button — top right
                                    Surface(
                                        onClick = { viewModel.onRemovePhoto() },
                                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                                        shape = CircleShape,
                                        color = Color.Black.copy(0.55f)
                                    ) {
                                        Icon(Icons.Default.Close, "Remove",
                                            tint = Color.White,
                                            modifier = Modifier.padding(6.dp).size(14.dp))
                                    }

                                    // Change photo — bottom center
                                    Surface(
                                        onClick = { photoPickerLauncher.launch("image/*") },
                                        modifier = Modifier.align(Alignment.BottomCenter).padding(10.dp),
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color.White.copy(0.90f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("📷", fontSize = 12.sp)
                                            Text("Change Photo", fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold, color = ModernAccent)
                                        }
                                    }
                                }
                            } else {
                                // Upload zone
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(colors.cardBg)
                                        .drawBehind {
                                            val stroke = Stroke(
                                                width = 1.5.dp.toPx(),
                                                pathEffect = PathEffect.dashPathEffect(
                                                    floatArrayOf(10f, 10f), 0f
                                                )
                                            )
                                            drawRoundRect(
                                                color = if (colors.isDark)
                                                    ModernAccent.copy(0.3f) else Color(0xFFD0D0CC),
                                                style = stroke,
                                                cornerRadius = CornerRadius(14.dp.toPx())
                                            )
                                        }
                                        .clickable { photoPickerLauncher.launch("image/*") }
                                        .padding(28.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.size(52.dp).clip(CircleShape)
                                                .background(ModernAccent.copy(0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) { Text("📷", fontSize = 24.sp) }
                                        Text("Tap to add photo", fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold, color = ModernAccent)
                                        Text("Helps others recognize the item",
                                            fontSize = 10.sp, color = colors.textMuted)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(80.dp))
                    }

                    // ── Sticky save button ─────────────────────────────────
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color    = colors.cardBg,
                        shadowElevation = 8.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .height(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (!uiState.isSaving)
                                        Brush.linearGradient(listOf(ModernAccent, Color(0xFF4F46E5)))
                                    else
                                        Brush.linearGradient(listOf(
                                            colors.textMuted.copy(0.3f), colors.textMuted.copy(0.3f)
                                        ))
                                )
                                .clickable(enabled = !uiState.isSaving) {
                                    viewModel.onSave(onSuccess = onNavigateBack)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White, strokeWidth = 2.dp
                                )
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("💾", fontSize = 14.sp)
                                    Text("Save Changes", fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Reusable form components ───────────────────────────────────────────────

@Composable
private fun FormField(
    label: String,
    optional: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                color = LocalAppColors.current.textMuted, letterSpacing = 0.6.sp)
            if (optional) {
                Surface(shape = RoundedCornerShape(6.dp), color = ModernAccent.copy(0.10f)) {
                    Text("Optional", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        color = ModernAccent,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp))
                }
            }
        }
        content()
    }
}

@Composable
private fun FormInput(
    value: String,
    onChange: (String) -> Unit,
    icon: String,
    hint: String,
    colors: AppColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        color    = colors.cardBg,
        border   = BorderStroke(1.dp, colors.cardBorder)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(icon, fontSize = 16.sp)
            BasicTextField(
                value = value,
                onValueChange = onChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(fontSize = 13.sp, color = colors.textPrimary),
                decorationBox = { inner ->
                    if (value.isEmpty()) Text(hint, fontSize = 13.sp, color = colors.textMuted)
                    inner()
                }
            )
        }
    }
}