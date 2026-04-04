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
import com.campusfind.ui.components.HeroBackButton
import com.campusfind.ui.theme.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    currentUserName: String,
    currentUserEmail: String,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors  = LocalAppColors.current
    var showLogoutDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.screenBg)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {

            // ── Hero ───────────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF12101E), Color(0xFF1E1340), Color(0xFF0E1F18))
                            )
                        )
                        .padding(bottom = 24.dp)
                ) {
                    // Ambient orbs
                    Box(modifier = Modifier.size(160.dp).offset(x = 220.dp, y = (-40).dp)
                        .background(ModernAccent.copy(alpha = 0.15f), CircleShape))
                    Box(modifier = Modifier.size(90.dp).offset(x = (-10).dp, y = 120.dp)
                        .background(ModernFound.copy(alpha = 0.08f), CircleShape))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                        Spacer(Modifier.height(8.dp))

                        // Back button
                        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
                            HeroBackButton(
                                onClick = onNavigateBack,
                                modifier = Modifier.align(Alignment.CenterStart)
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        // Title
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
                            Text("⚙️", fontSize = 40.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Settings",
                                fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White,
                                style = LocalTextStyle.current.copy(
                                    shadow = Shadow(Color.Black.copy(0.5f), Offset(0f, 2f), 10f)
                                )
                            )
                            Text(
                                "Manage your app preferences",
                                fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // ── Account Info ───────────────────────────────────────────────
            item {
                SettingsSection(label = "ACCOUNT INFO", colors = colors) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = colors.cardBg,
                        border = BorderStroke(1.dp, colors.cardBorder)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(48.dp).clip(CircleShape)
                                    .background(ModernAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    currentUserName.firstOrNull()?.uppercase() ?: "U",
                                    fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ModernAccent
                                )
                            }
                            Column {
                                Text(currentUserName, fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                Text(currentUserEmail, fontSize = 11.sp, color = colors.textMuted)
                            }
                        }
                    }
                }
            }

            // ── Preferences ────────────────────────────────────────────────
            item {
                SettingsSection(label = "PREFERENCES", colors = colors) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = colors.cardBg,
                        border = BorderStroke(1.dp, colors.cardBorder)
                    ) {
                        Column {
                            SettingRow(
                                icon = if (uiState.isDarkMode) "🌙" else "☀️",
                                title = "Dark Mode",
                                subtitle = if (uiState.isDarkMode) "Enabled" else "Disabled",
                                colors = colors,
                                showDivider = true,
                                onClick = {}
                            ) {
                                Switch(
                                    checked = uiState.isDarkMode,
                                    onCheckedChange = { viewModel.toggleDarkMode() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor  = Color.White,
                                        checkedTrackColor  = ModernAccent,
                                        uncheckedThumbColor = colors.textMuted,
                                        uncheckedTrackColor = colors.pillBg
                                    )
                                )
                            }
                            SettingRow(
                                icon = "🔔",
                                title = "Notifications",
                                subtitle = if (uiState.notificationsEnabled) "Enabled" else "Disabled",
                                colors = colors,
                                showDivider = false,
                                onClick = {}
                            ) {
                                Switch(
                                    checked = uiState.notificationsEnabled,
                                    onCheckedChange = { viewModel.toggleNotifications() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor  = Color.White,
                                        checkedTrackColor  = ModernAccent,
                                        uncheckedThumbColor = colors.textMuted,
                                        uncheckedTrackColor = colors.pillBg
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // ── About ──────────────────────────────────────────────────────
            item {
                SettingsSection(label = "ABOUT", colors = colors) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = colors.cardBg,
                        border = BorderStroke(1.dp, colors.cardBorder)
                    ) {
                        Column {
                            SettingRow("ℹ️", "App Version", "Version 1.0.0",
                                colors, showDivider = true, onClick = {})
                            SettingRow("📖", "Terms of Service", "View our terms",
                                colors, showDivider = true, onClick = {}) { ChevronRight(colors) }
                            SettingRow("🔒", "Privacy Policy", "How we protect your data",
                                colors, showDivider = false, onClick = {}) { ChevronRight(colors) }
                        }
                    }
                }
            }

            // ── Support ────────────────────────────────────────────────────
            item {
                SettingsSection(label = "SUPPORT", colors = colors) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = colors.cardBg,
                        border = BorderStroke(1.dp, colors.cardBorder)
                    ) {
                        Column {
                            SettingRow("❓", "Help & FAQ", "Get answers to common questions",
                                colors, showDivider = true, onClick = {}) { ChevronRight(colors) }
                            SettingRow("💬", "Contact Us", "campusfind@nwssu.edu.ph",
                                colors, showDivider = true, onClick = {}) { ChevronRight(colors) }
                            SettingRow("⭐", "Rate CampusFind", "Share your feedback",
                                colors, showDivider = false, onClick = {}) { ChevronRight(colors) }
                        }
                    }
                }
            }

            // ── Logout ─────────────────────────────────────────────────────
            item {
                SettingsSection(label = "ACCOUNT", colors = colors) {
                    Surface(
                        onClick = { showLogoutDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        color = if (colors.isDark) Color(0xFF1A0A0A) else Color.White,
                        border = BorderStroke(1.dp,
                            if (colors.isDark) ModernError.copy(0.3f) else Color(0xFFFFDEDC))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape)
                                    .background(ModernError.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) { Text("🚪", fontSize = 18.sp) }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Logout", fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold, color = ModernError)
                                Text("Sign out of your account", fontSize = 11.sp, color = colors.textMuted)
                            }
                        }
                    }
                }
            }

            // ── Footer ─────────────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("CampusFind for NWSSU", fontSize = 11.sp, color = colors.textMuted)
                    Text("Made with 💜 by Team CampusFind", fontSize = 10.sp, color = colors.textMuted.copy(alpha = 0.6f))
                }
            }
        }

        // ── Logout dialog ──────────────────────────────────────────────────
        if (showLogoutDialog) {
            LogoutDialog(
                colors    = colors,
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

// ══════════════════════════════════════════════════════════════════════════
// COMPONENTS
// ══════════════════════════════════════════════════════════════════════════

@Composable
private fun SettingsSection(
    label: String,
    colors: AppColors,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, start = 14.dp, end = 14.dp)) {
        Text(
            label, fontSize = 10.sp, fontWeight = FontWeight.Bold,
            color = colors.textMuted, letterSpacing = 0.6.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
        )
        content()
    }
}

@Composable
private fun SettingRow(
    icon: String,
    title: String,
    subtitle: String,
    colors: AppColors,
    showDivider: Boolean,
    onClick: () -> Unit,
    trailingContent: (@Composable () -> Unit)? = null
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
            // Icon circle — theme-aware background
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape)
                    .background(colors.pillBg),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 18.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                Text(subtitle, fontSize = 11.sp, color = colors.textMuted)
            }

            trailingContent?.invoke()
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 66.dp),
                color = colors.cardBorder,
                thickness = 1.dp
            )
        }
    }
}

@Composable
private fun ChevronRight(colors: AppColors) {
    Text("›", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textMuted)
}

@Composable
private fun LogoutDialog(colors: AppColors, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = colors.cardBg) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape)
                        .background(ModernError.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) { Text("🚪", fontSize = 28.sp) }

                Spacer(Modifier.height(16.dp))

                Text("Logout?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Are you sure you want to sign out of your account?",
                    fontSize = 13.sp, color = colors.textSecondary
                )
                Spacer(Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, colors.cardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textSecondary)
                    ) { Text("Cancel", fontSize = 13.sp) }

                    Button(
                        onClick = onConfirm, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ModernError)
                    ) { Text("Logout", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}