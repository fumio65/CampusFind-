package com.campusfind.ui.screens.additem

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.campusfind.ui.theme.*

@Composable
fun AddItemScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddItemViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value
    val colors  = LocalAppColors.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.onPhotoSelected(it) } }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Hero ───────────────────────────────────────────────────────
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
                // Ambient orbs
                Box(modifier = Modifier.size(160.dp).offset(x = 220.dp, y = (-40).dp)
                    .background(ModernAccent.copy(alpha = 0.15f), CircleShape))
                Box(modifier = Modifier.size(90.dp).offset(x = (-10).dp, y = 100.dp)
                    .background(ModernFound.copy(alpha = 0.08f), CircleShape))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                ) {
                    Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                    Spacer(Modifier.height(8.dp))

                    // Back button only — no ? button
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
                        Surface(
                            onClick = onNavigateBack,
                            shape = RoundedCornerShape(22.dp),
                            color = Color.Black.copy(alpha = 0.38f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Row(
                                modifier = Modifier.padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(20.dp).clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("‹", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Text("Back", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Title
                    Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                        Text(
                            "Report Lost Item",
                            fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White,
                            style = LocalTextStyle.current.copy(
                                shadow = Shadow(Color.Black.copy(0.5f), Offset(0f, 2f), 10f)
                            )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Help someone find what they lost 🙏",
                            fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // ── Scrollable form ────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(colors.screenBg)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Error banner
                if (uiState.error != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = ModernError.copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, ModernError.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = uiState.error ?: "",
                            color = ModernError, fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // ── Item Title ─────────────────────────────────────────────
                FormField(label = "ITEM TITLE", colors = colors) {
                    FormInput(
                        value       = uiState.title,
                        onChange    = { viewModel.onTitleChanged(it) },
                        placeholder = "e.g., Black Wallet",
                        leadingIcon = "📦",
                        colors      = colors
                    )
                }

                // ── Description ────────────────────────────────────────────
                FormField(label = "DESCRIPTION", colors = colors) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(14.dp),
                        color    = colors.cardBg,
                        border   = BorderStroke(1.dp, colors.cardBorder)
                    ) {
                        BasicTextField(
                            value       = uiState.description,
                            onValueChange = { viewModel.onDescriptionChanged(it) },
                            modifier    = Modifier.fillMaxWidth().height(100.dp).padding(14.dp),
                            textStyle   = TextStyle(
                                fontSize = 13.sp, lineHeight = 20.sp, color = colors.textPrimary
                            ),
                            cursorBrush = SolidColor(ModernAccent),
                            decorationBox = { inner ->
                                if (uiState.description.isEmpty()) {
                                    Text(
                                        "Describe the item, where it was lost, and when...",
                                        fontSize = 13.sp, color = colors.textMuted, lineHeight = 20.sp
                                    )
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

                // ── Location ───────────────────────────────────────────────
                FormField(label = "LAST SEEN LOCATION", isOptional = true, colors = colors) {
                    FormInput(
                        value       = uiState.location,
                        onChange    = { viewModel.onLocationChanged(it) },
                        placeholder = "e.g., Library entrance",
                        leadingIcon = "📍",
                        colors      = colors
                    )
                }

                // ── Photo ──────────────────────────────────────────────────
                FormField(label = "PHOTO (IF YOU HAVE ONE)", isOptional = true, colors = colors) {
                    if (uiState.photoUri != null) {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                            Image(
                                painter = rememberAsyncImagePainter(uiState.photoUri),
                                contentDescription = "Selected photo",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
                                contentScale = ContentScale.Crop
                            )
                            // Gradient scrim
                            Box(
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(0.3f))
                                        )
                                    )
                            )
                            // Remove button
                            Surface(
                                onClick = { viewModel.onRemovePhoto() },
                                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.55f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = Color.White,
                                    modifier = Modifier.padding(8.dp).size(14.dp)
                                )
                            }
                            // Change photo
                            Surface(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.92f)
                            ) {
                                Text(
                                    "Change Photo", fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold, color = ModernAccent,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                                )
                            }
                        }
                    } else {
                        // Upload zone — dashed border
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { photoPickerLauncher.launch("image/*") }
                                .background(
                                    if (colors.isDark) ModernAccent.copy(0.04f) else Color(0xFFF9F9F6),
                                    RoundedCornerShape(14.dp)
                                )
                                .drawBehind {
                                    val stroke = Stroke(
                                        width = 1.5.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                                    )
                                    drawRoundRect(
                                        color = if (colors.isDark)
                                            ModernAccent.copy(alpha = 0.25f)
                                        else
                                            Color(0xFFCCCCCC),
                                        style = stroke,
                                        cornerRadius = CornerRadius(14.dp.toPx())
                                    )
                                }
                                .padding(28.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(52.dp).clip(CircleShape)
                                        .background(ModernAccent.copy(alpha = 0.10f)),
                                    contentAlignment = Alignment.Center
                                ) { Text("📷", fontSize = 24.sp) }
                                Text("Tap to add photo", fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold, color = ModernAccent)
                                Text("Helps others recognize the item",
                                    fontSize = 10.sp, color = colors.textMuted)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(80.dp))
            }

            // ── Sticky submit button ───────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color    = colors.cardBg,
                shadowElevation = 12.dp
            ) {
                Button(
                    onClick  = { viewModel.onSubmit(onSuccess = onNavigateBack) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(52.dp),
                    enabled  = !uiState.isSubmitting,
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp),
                    shape    = RoundedCornerShape(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (!uiState.isSubmitting)
                                    Brush.linearGradient(listOf(ModernAccent, Color(0xFF4F46E5)))
                                else
                                    Brush.linearGradient(listOf(colors.textMuted, colors.textMuted))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White, strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📢", fontSize = 16.sp)
                                Text(
                                    "Post Lost Item",
                                    fontSize = 14.sp, fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Reusable form field wrapper ────────────────────────────────────────────

@Composable
private fun FormField(
    label: String,
    colors: AppColors,
    isOptional: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                label, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                color = colors.textMuted, letterSpacing = 0.6.sp
            )
            if (isOptional) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ModernAccent.copy(alpha = 0.10f)
                ) {
                    Text(
                        "Optional", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        color = ModernAccent,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        content()
    }
}

// ── Reusable single-line input ─────────────────────────────────────────────

@Composable
private fun FormInput(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: String,
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
            Text(leadingIcon, fontSize = 16.sp)
            BasicTextField(
                value         = value,
                onValueChange = onChange,
                modifier      = Modifier.weight(1f),
                textStyle     = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, color = colors.textPrimary),
                singleLine    = true,
                cursorBrush   = SolidColor(ModernAccent),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(placeholder, fontSize = 13.sp, lineHeight = 18.sp, color = colors.textMuted)
                    }
                    inner()
                }
            )
        }
    }
}