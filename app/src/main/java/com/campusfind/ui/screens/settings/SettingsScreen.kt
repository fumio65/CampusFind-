package com.campusfind.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.campusfind.ui.theme.*

/**
 * FILE: app/src/main/java/com/campusfind/ui/screens/settings/SettingsScreen.kt
 *
 * Modern Settings screen with gradient design.
 *
 * Features:
 * - Gradient header with user info
 * - App info section
 * - Logout button with confirmation
 * - Clean, modern card-based layout
 *
 * Phase 1 only — Phase 2 will add:
 * - Theme toggle
 * - Notification preferences
 * - Sync settings
 *
 * See: DEC-005 (Jetpack Compose), TASK-119
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    currentUserName: String,
    currentUserEmail: String,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color(0xFFf4f4f0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Gradient header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1a1228),
                                Color(0xFF2e1f48),
                                Color(0xFF1a2a20)
                            )
                        )
                    )
                    .padding(bottom = 24.dp)
            ) {
                // Purple orb
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .offset(x = (-20).dp, y = (-20).dp)
                        .align(Alignment.TopEnd)
                        .background(
                            color = ModernAccent.copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                )

                // Teal orb
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .offset(x = (-10).dp, y = 10.dp)
                        .align(Alignment.BottomStart)
                        .background(
                            color = ModernFound.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Profile icon
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(ModernAccent, Color(0xFF5246d5))
                                )
                            )
                            .border(
                                width = 3.dp,
                                color = Color.White.copy(alpha = 0.3f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👤",
                            fontSize = 32.sp,
                            color = Color.White
                        )
                    }

                    // User name
                    Text(
                        text = currentUserName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        style = LocalTextStyle.current.copy(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.3f),
                                offset = Offset(0f, 2f),
                                blurRadius = 8f
                            )
                        )
                    )

                    // User email
                    Text(
                        text = currentUserEmail,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // App Info Section
                Text(
                    text = "APP INFO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF888888),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        SettingsItem(
                            icon = "ℹ️",
                            title = "Version",
                            subtitle = "1.0.0 (MCO 1 - Phase 1)",
                            onClick = {}
                        )

                        HorizontalDivider(color = Color(0xFFf0f0f0))

                        SettingsItem(
                            icon = "📱",
                            title = "About CampusFind+",
                            subtitle = "Lost & Found for Northwest Samar State University",
                            onClick = {}
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Account Section
                Text(
                    text = "ACCOUNT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF888888),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    SettingsItem(
                        icon = "🚪",
                        title = "Logout",
                        subtitle = "Sign out of your account",
                        onClick = { showLogoutDialog = true },
                        tintColor = ModernError
                    )
                }

                Spacer(Modifier.weight(1f))

                // Footer
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append("Made with ")
                            withStyle(SpanStyle(color = Color(0xFFff6b6b))) {
                                append("❤️")
                            }
                            append(" at NWSSU")
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF888888)
                    )

                    Text(
                        text = "CampusFind+ © 2026",
                        fontSize = 11.sp,
                        color = Color(0xFFaaaaaa)
                    )
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Text("🚪", fontSize = 32.sp) },
            title = {
                Text(
                    "Logout",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Are you sure you want to log out?",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                        onNavigateToLogin()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ModernError
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Logout", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun SettingsItem(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tintColor: Color = Color(0xFF1a1a2e)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = tintColor.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 18.sp
            )
        }

        // Text content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = tintColor
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF888888),
                lineHeight = 16.sp
            )
        }
    }
}