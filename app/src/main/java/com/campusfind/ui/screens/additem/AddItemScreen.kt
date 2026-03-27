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

/**
 * AddItemScreen - COMPLETE WITH LOCATION SUPPORT
 *
 * FEATURES:
 * 1. Photo upload with preview
 * 2. Remove/change photo
 * 3. Location field connected to ViewModel
 * 4. All fields save to database
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddItemViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onPhotoSelected(uri)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ══════════════════════════════════════════════════
            // IMMERSIVE HERO
            // ══════════════════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1a1228),
                                Color(0xFF2e1f48),
                                Color(0xFF1a2a20)
                            )
                        )
                    )
            ) {
                // Purple ambient blob
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .offset(x = 250.dp, y = (-10).dp)
                        .background(
                            Color(0xFF6C63FF).copy(alpha = 0.28f),
                            CircleShape
                        )
                        .blur(38.dp)
                )

                // Green ambient blob
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .offset(x = 10.dp, y = 100.dp)
                        .background(
                            Color(0xFF2DD4A0).copy(alpha = 0.2f),
                            CircleShape
                        )
                        .blur(30.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    // Status bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "9:41",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "●●● 82%",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Back button + Help button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back button
                        Surface(
                            onClick = onNavigateBack,
                            modifier = Modifier,
                            shape = RoundedCornerShape(22.dp),
                            color = Color.Black.copy(alpha = 0.38f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    start = 8.dp,
                                    end = 12.dp,
                                    top = 6.dp,
                                    bottom = 6.dp
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(20.dp),
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.15f)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "‹",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
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

                        // Help button
                        Surface(
                            onClick = { /* TODO: Show help */ },
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.38f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // Title + Subtitle
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "Report Lost Item",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            lineHeight = 28.sp
                        )
                        Text(
                            "Help someone find what they lost 🙏",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // ══════════════════════════════════════════════════
            // SCROLLABLE FORM
            // ══════════════════════════════════════════════════
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFFF4F4F0))
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Error message
                if (uiState.error != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFEBEE)
                    ) {
                        Text(
                            text = uiState.error ?: "",
                            color = Color(0xFFC62828),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Item Title
                Column {
                    Text(
                        "ITEM TITLE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFAAAAAA),
                        letterSpacing = 0.6.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.5.dp, Color(0xFFE4E4E0)),
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("📦", fontSize = 15.sp, color = Color.Black.copy(alpha = 0.4f))

                            BasicTextField(
                                value = uiState.title,
                                onValueChange = { viewModel.onTitleChanged(it) },
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                                decorationBox = { innerTextField ->
                                    if (uiState.title.isEmpty()) {
                                        Text("e.g., Black Wallet", fontSize = 13.sp, color = Color.Gray)
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }
                }

                // Description
                Column {
                    Text(
                        "DESCRIPTION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFAAAAAA),
                        letterSpacing = 0.6.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.5.dp, Color(0xFFE4E4E0)),
                        shadowElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(11.dp)
                        ) {
                            BasicTextField(
                                value = uiState.description,
                                onValueChange = { viewModel.onDescriptionChanged(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp),
                                textStyle = TextStyle(
                                    fontSize = 12.sp,
                                    lineHeight = 19.sp,
                                    color = Color.Black
                                ),
                                decorationBox = { innerTextField ->
                                    if (uiState.description.isEmpty()) {
                                        Text(
                                            "Describe the item, where it was lost, and when...",
                                            fontSize = 12.sp,
                                            color = Color(0xFFCCCCCC),
                                            lineHeight = 19.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }

                    // Helper text + character counter
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, start = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Be specific to help others identify it",
                            fontSize = 9.sp,
                            color = Color(0xFFCCCCCC)
                        )
                        Text(
                            "${uiState.description.length} / 500",
                            fontSize = 9.sp,
                            color = Color(0xFFBBBBBB),
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                // ✅ LOCATION (CONNECTED TO VIEWMODEL)
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            "LAST SEEN LOCATION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFAAAAAA),
                            letterSpacing = 0.6.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF6C63FF).copy(alpha = 0.1f)
                        ) {
                            Text(
                                "Optional",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6C63FF),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.5.dp, Color(0xFFE4E4E0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("📍", fontSize = 15.sp, color = Color.Black.copy(alpha = 0.4f))

                            BasicTextField(
                                value = uiState.location,  // ✅ CHANGED: Use ViewModel state
                                onValueChange = { viewModel.onLocationChanged(it) },  // ✅ CHANGED: Call ViewModel
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                                decorationBox = { innerTextField ->
                                    if (uiState.location.isEmpty()) {  // ✅ CHANGED: Check ViewModel state
                                        Text("e.g., Library entrance", fontSize = 13.sp, color = Color.Gray)
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }
                }

                // ══════════════════════════════════════════════════
                // PHOTO UPLOAD
                // ══════════════════════════════════════════════════
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            "PHOTO (IF YOU HAVE ONE)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFAAAAAA),
                            letterSpacing = 0.6.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF6C63FF).copy(alpha = 0.1f)
                        ) {
                            Text(
                                "Optional",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6C63FF),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }

                    // Show preview if photo selected
                    if (uiState.photoUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            // Photo preview
                            Image(
                                painter = rememberAsyncImagePainter(uiState.photoUri),
                                contentDescription = "Selected photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(14.dp)),
                                contentScale = ContentScale.Crop
                            )

                            // Remove button
                            Surface(
                                onClick = { viewModel.onRemovePhoto() },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp),
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.6f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove photo",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(16.dp)
                                )
                            }

                            // Change photo button
                            Surface(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(12.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.9f)
                            ) {
                                Text(
                                    "Change Photo",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6C63FF),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    } else {
                        // Show upload zone if no photo
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { photoPickerLauncher.launch("image/*") }
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFFF9F9F6), Color(0xFFF0F0EC))
                                    ),
                                    RoundedCornerShape(14.dp)
                                )
                                .drawBehind {
                                    val stroke = Stroke(
                                        width = 1.5.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                    )
                                    drawRoundRect(
                                        color = Color(0xFFD0D0CC),
                                        style = stroke,
                                        cornerRadius = CornerRadius(14.dp.toPx())
                                    )
                                }
                                .padding(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(52.dp),
                                    shape = CircleShape,
                                    color = Color(0xFF6C63FF).copy(alpha = 0.1f)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("📷", fontSize = 24.sp)
                                    }
                                }

                                Text(
                                    "Tap to add photo",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6C63FF)
                                )

                                Text(
                                    "Helps others recognize the item",
                                    fontSize = 10.sp,
                                    color = Color(0xFFAAAAAA)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(80.dp)) // Padding for bottom button
            }

            // ══════════════════════════════════════════════════
            // STICKY BOTTOM BUTTON
            // ══════════════════════════════════════════════════
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        viewModel.onSubmit(onSuccess = onNavigateBack)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .height(48.dp),
                    enabled = !uiState.isSubmitting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (!uiState.isSubmitting) {
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF6C63FF),
                                            Color(0xFF5246d5)
                                        )
                                    )
                                } else {
                                    Brush.linearGradient(
                                        colors = listOf(Color.Gray, Color.Gray)
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Post Lost Item",
                                fontSize = 14.sp,
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