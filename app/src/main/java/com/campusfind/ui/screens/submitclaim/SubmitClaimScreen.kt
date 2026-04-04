package com.campusfind.ui.screens.submitclaim

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.campusfind.ui.theme.LocalAppColors
import com.campusfind.ui.theme.ModernAccent

/**
 * Submit Claim Screen - Matches CampusFind+ theme
 * Dark hero + beige scrollable content
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitClaimScreen(
    itemId: String,
    itemTitle: String,
    onNavigateBack: () -> Unit,
    viewModel: SubmitClaimViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalAppColors.current

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onPhotoSelected(it) }
    }

    LaunchedEffect(itemId, itemTitle) {
        viewModel.initialize(itemId, itemTitle)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // ══ DARK HERO SECTION (140dp) ══
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            // Dark gradient background
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
            ) {
                // Green blob (matching "I Found This" theme)
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .offset(x = 250.dp, y = (-15).dp)
                        .background(
                            Color(0xFF2DD4A0).copy(alpha = 0.25f),
                            CircleShape
                        )
                        .blur(32.dp)
                )

                // Purple blob
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .offset(x = 15.dp, y = 100.dp)
                        .background(
                            Color(0xFF6C63FF).copy(alpha = 0.18f),
                            CircleShape
                        )
                        .blur(24.dp)
                )
            }

            // Back button
            Surface(
                modifier = Modifier
                    .padding(start = 12.dp, top = 8.dp)
                    .clickable { onNavigateBack() },
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

            // Title + Icon
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0ea870),
                        shadowElevation = 6.dp
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✋", fontSize = 20.sp)
                        }
                    }

                    Column {
                        Text(
                            "Submit Claim",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            "Found this item?",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        // ══ BEIGE SCROLLABLE CONTENT ══
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(colors.screenBg)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Claiming Item Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE8F5E9),
                border = BorderStroke(1.5.dp, Color(0xFFA8EECF))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF2DD4A0).copy(alpha = 0.2f)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📦", fontSize = 16.sp)
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "CLAIMING ITEM",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0a5c3c),
                            letterSpacing = 0.6.sp
                        )
                        Text(
                            uiState.itemTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0a5c3c)
                        )
                    }
                }
            }

            // Where did you find it?
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "WHERE DID YOU FIND IT?",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMuted,
                    letterSpacing = 0.6.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                OutlinedTextField(
                    value = uiState.location,
                    onValueChange = { viewModel.onLocationChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "e.g., Found in library, 2nd floor near computers",
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = colors.cardBg,
                        unfocusedContainerColor = colors.cardBg,
                        focusedBorderColor      = Color(0xFF2DD4A0),
                        unfocusedBorderColor    = colors.cardBorder,
                        focusedTextColor        = colors.textPrimary,
                        unfocusedTextColor      = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    supportingText = {
                        Text(
                            "${uiState.location.length}/500",
                            fontSize = 10.sp,
                            color = if (uiState.location.length > 450) {
                                Color(0xFFFF6F00)
                            } else {
                                colors.textMuted
                            }
                        )
                    }
                )
            }

            // Add Photo Proof
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ADD PHOTO PROOF",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textMuted,
                        letterSpacing = 0.6.sp
                    )
                    Text(
                        "Required • Max 3 photos",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFF6F00)  // Orange to indicate required
                    )
                }

                Spacer(Modifier.height(6.dp))

                // Photo grid (up to 3 photos)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Show existing photos
                    uiState.photoUris.forEach { uri ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFE8E8E4)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "📷",
                                        fontSize = 32.sp,
                                        color = Color(0xFF888888).copy(alpha = 0.5f)
                                    )
                                }
                            }

                            // Remove button
                            Surface(
                                onClick = { viewModel.onRemovePhoto(uri) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(24.dp),
                                shape = CircleShape,
                                color = Color(0xFFFF4D6D),
                                shadowElevation = 4.dp
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "✕",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Add photo button (show if less than 3 photos)
                    if (uiState.photoUris.size < 3) {
                        Surface(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = colors.cardBg,
                            border = BorderStroke(
                                1.5.dp,
                                if (uiState.photoUris.isEmpty()) {
                                    Color(0xFFFF6F00)  // Orange border if no photos
                                } else {
                                    Color(0xFFE8E8E4)
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    modifier = Modifier.size(36.dp),
                                    shape = CircleShape,
                                    color = Color(0xFF6C63FF).copy(alpha = 0.1f)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("📷", fontSize = 18.sp)
                                    }
                                }

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    if (uiState.photoUris.isEmpty()) "Add photo" else "+ Add",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF6C63FF)
                                )
                            }
                        }
                    }

                    // Fill remaining space if less than 3 slots
                    repeat(2 - uiState.photoUris.size) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }

                // Show error if no photos
                if (uiState.photoUris.isEmpty()) {
                    Text(
                        "At least one photo is required",
                        fontSize = 10.sp,
                        color = Color(0xFFFF6F00),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            // Helper text
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFF9E6),
                border = BorderStroke(1.5.dp, Color(0xFFFFE082))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("💡", fontSize = 16.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Claim Tips",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF795600)
                        )
                        Text(
                            "The reporter will review your claim. Add a photo and clear location to increase trust!",
                            fontSize = 10.sp,
                            color = Color(0xFF795600),
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(80.dp)) // Bottom padding for button
        }

        // ══ STICKY BOTTOM BUTTON ══
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.cardBg,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                val isValid = uiState.location.isNotBlank() && uiState.photoUris.isNotEmpty()

                Button(
                    onClick = {
                        viewModel.submitClaim(onSuccess = onNavigateBack)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !uiState.isSubmitting && isValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color(0xFFE8E8E4)
                    ),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (uiState.isSubmitting || !isValid) {
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFCCCCCC),
                                            Color(0xFFAAAAAA)
                                        )
                                    )
                                } else {
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF0ea870),
                                            Color(0xFF2dd4a0)
                                        )
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("✋", fontSize = 16.sp)
                                Text(
                                    "Submit Claim",
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

    // Error snackbar
    if (uiState.error != null) {
        LaunchedEffect(uiState.error) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }
}