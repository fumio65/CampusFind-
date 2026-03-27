package com.campusfind.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.campusfind.ui.theme.*

/**
 * SettingsScreen - App settings and account management
 *
 * Features:
 * - Dark mode toggle
 * - Notification settings
 * - About app
 * - Help & Support
 * - Logout with confirmation
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
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF4F4F0)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ══════════════════════════════════════
            // HERO SECTION
            // ══════════════════════════════════════
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1a1228),
                                    Color(0xFF2e1f48)
                                )
                            )
                        )
                        .padding(bottom = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(Modifier.height(8.dp))

                        // Back button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                        ) {
                            Surface(
                                onClick = onNavigateBack,
                                shape = RoundedCornerShape(22.dp),
                                color = Color.Black.copy(alpha = 0.38f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                                modifier = Modifier.align(Alignment.CenterStart)
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
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "‹",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Text(
                                        "Back",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Title
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                "⚙️",
                                fontSize = 48.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Settings",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                style = LocalTextStyle.current.copy(
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        offset = Offset(0f, 2f),
                                        blurRadius = 10f
                                    )
                                )
                            )
                            Text(
                                "Manage your app preferences",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // ══════════════════════════════════════
            // PREFERENCES SECTION
            // ══════════════════════════════════════
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 14.dp, end = 14.dp)
                ) {
                    Text(
                        "ACCOUNT INFO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFAAAAAA),
                        letterSpacing = 0.6.sp,
                        modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.5.dp, Color(0xFFE8E8E4))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = CircleShape,
                                    color = ModernAccent.copy(alpha = 0.15f)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            currentUserName.firstOrNull()?.uppercase() ?: "U",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ModernAccent
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        currentUserName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1a1a2e)
                                    )
                                    Text(
                                        currentUserEmail,
                                        fontSize = 11.sp,
                                        color = Color(0xFF888888)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ══════════════════════════════════════
            // PREFERENCES SECTION
            // ══════════════════════════════════════
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 14.dp, end = 14.dp)
                ) {
                    Text(
                        "PREFERENCES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFAAAAAA),
                        letterSpacing = 0.6.sp,
                        modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.5.dp, Color(0xFFE8E8E4))
                    ) {
                        Column {
                            // Dark Mode Toggle
                            SettingRow(
                                icon = if (uiState.isDarkMode) "🌙" else "☀️",
                                title = "Dark Mode",
                                subtitle = if (uiState.isDarkMode) "Enabled" else "Disabled",
                                onClick = { /* TODO: Toggle dark mode */ },
                                showDivider = true,
                                trailingContent = {
                                    Switch(
                                        checked = uiState.isDarkMode,
                                        onCheckedChange = { viewModel.toggleDarkMode() },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = ModernAccent,
                                            checkedTrackColor = ModernAccent.copy(alpha = 0.3f)
                                        )
                                    )
                                }
                            )

                            // Notifications Toggle
                            SettingRow(
                                icon = "🔔",
                                title = "Notifications",
                                subtitle = if (uiState.notificationsEnabled) "Enabled" else "Disabled",
                                onClick = { /* TODO: Toggle notifications */ },
                                showDivider = false,
                                trailingContent = {
                                    Switch(
                                        checked = uiState.notificationsEnabled,
                                        onCheckedChange = { viewModel.toggleNotifications() },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = ModernAccent,
                                            checkedTrackColor = ModernAccent.copy(alpha = 0.3f)
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // ══════════════════════════════════════
            // ABOUT SECTION
            // ══════════════════════════════════════
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 14.dp, end = 14.dp)
                ) {
                    Text(
                        "ABOUT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFAAAAAA),
                        letterSpacing = 0.6.sp,
                        modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.5.dp, Color(0xFFE8E8E4))
                    ) {
                        Column {
                            SettingRow(
                                icon = "ℹ️",
                                title = "App Version",
                                subtitle = "Version 1.0.0",
                                onClick = { /* No action */ },
                                showDivider = true,
                                trailingContent = null
                            )

                            SettingRow(
                                icon = "📖",
                                title = "Terms of Service",
                                subtitle = "View our terms",
                                onClick = { /* TODO: Open terms */ },
                                showDivider = true,
                                trailingContent = { ChevronRight() }
                            )

                            SettingRow(
                                icon = "🔒",
                                title = "Privacy Policy",
                                subtitle = "How we protect your data",
                                onClick = { /* TODO: Open privacy */ },
                                showDivider = false,
                                trailingContent = { ChevronRight() }
                            )
                        }
                    }
                }
            }

            // ══════════════════════════════════════
            // SUPPORT SECTION
            // ══════════════════════════════════════
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 14.dp, end = 14.dp)
                ) {
                    Text(
                        "SUPPORT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFAAAAAA),
                        letterSpacing = 0.6.sp,
                        modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.5.dp, Color(0xFFE8E8E4))
                    ) {
                        Column {
                            SettingRow(
                                icon = "❓",
                                title = "Help & FAQ",
                                subtitle = "Get answers to common questions",
                                onClick = { /* TODO: Open help */ },
                                showDivider = true,
                                trailingContent = { ChevronRight() }
                            )

                            SettingRow(
                                icon = "💬",
                                title = "Contact Us",
                                subtitle = "campusfind@nwssu.edu.ph",
                                onClick = { /* TODO: Email support */ },
                                showDivider = true,
                                trailingContent = { ChevronRight() }
                            )

                            SettingRow(
                                icon = "⭐",
                                title = "Rate CampusFind",
                                subtitle = "Share your feedback",
                                onClick = { /* TODO: Open store */ },
                                showDivider = false,
                                trailingContent = { ChevronRight() }
                            )
                        }
                    }
                }
            }

            // ══════════════════════════════════════
            // DANGER ZONE
            // ══════════════════════════════════════
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 14.dp, end = 14.dp, bottom = 16.dp)
                ) {
                    Text(
                        "ACCOUNT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFAAAAAA),
                        letterSpacing = 0.6.sp,
                        modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                    )

                    Surface(
                        onClick = { showLogoutDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.5.dp, Color(0xFFFFDEDC))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = Color(0xFFFFEBEE)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🚪", fontSize = 18.sp)
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Logout",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ModernError
                                )
                                Text(
                                    "Sign out of your account",
                                    fontSize = 11.sp,
                                    color = Color(0xFF888888)
                                )
                            }
                        }
                    }
                }
            }

            // Footer info
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "CampusFind for NWSSU",
                        fontSize = 11.sp,
                        color = Color(0xFFAAAAAA)
                    )
                    Text(
                        "Made with 💜 by Team CampusFind",
                        fontSize = 10.sp,
                        color = Color(0xFFCCCCCC)
                    )
                }
            }
        }

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            LogoutDialog(
                onDismiss = { showLogoutDialog = false },
                onConfirm = {
                    showLogoutDialog = false
                    viewModel.logout()
                    onNavigateToLogin()
                }
            )
        }
    }
}

// ══════════════════════════════════════
// COMPOSABLE COMPONENTS
// ══════════════════════════════════════

@Composable
private fun SettingRow(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showDivider: Boolean,
    trailingContent: (@Composable () -> Unit)?
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color(0xFFF4F4F0)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 18.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1a1a2e)
                )
                Text(
                    subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF888888)
                )
            }

            if (trailingContent != null) {
                trailingContent()
            }
        }

        if (showDivider) {
            Divider(
                modifier = Modifier.padding(start = 66.dp),
                color = Color(0xFFF4F4F0),
                thickness = 1.dp
            )
        }
    }
}

@Composable
private fun ChevronRight() {
    Text(
        "›",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFCCCCCC)
    )
}

@Composable
private fun LogoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            Color(0xFFFFEBEE),
                            CircleShape
                        )
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🚪", fontSize = 28.sp)
                }

                Spacer(Modifier.height(16.dp))

                // Title
                Text(
                    "Logout?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1a1a2e),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(8.dp))

                // Message
                Text(
                    "Are you sure you want to sign out of your account?",
                    fontSize = 13.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFE8E8E4)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF666666)
                        )
                    ) {
                        Text("Cancel", fontSize = 13.sp)
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ModernError
                        )
                    ) {
                        Text("Logout", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}