package com.campusfind.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campusfind.ui.theme.*

/**
 * Modern Navigation Drawer
 * Shown when burger menu (☰) is tapped
 */
@Composable
fun NavigationDrawer(
    currentUserName: String,
    currentUserEmail: String,
    onNavigateToProfile: () -> Unit,
    onNavigateToSmartHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(280.dp),
        drawerContainerColor = Color(0xFF1a1a1f),
        drawerContentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 20.dp)
        ) {
            // ══════════ PROFILE HEADER ══════════
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .clickable {
                        onNavigateToProfile()
                        onDismiss()
                    }
            ) {
                // Avatar with gradient
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(ModernAccent, Color(0xFF5246d5))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentUserName.firstOrNull()?.toString()?.uppercase() ?: "U",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(12.dp))

                // User name
                Text(
                    text = currentUserName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(Modifier.height(4.dp))

                // Email
                Text(
                    text = currentUserEmail,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color.White.copy(alpha = 0.1f)
            )

            // ══════════ MENU ITEMS ══════════

            DrawerMenuItem(
                icon = "👤",
                label = "Profile",
                onClick = {
                    onNavigateToProfile()
                    onDismiss()
                }
            )

            DrawerMenuItem(
                icon = "📊",
                label = "Smart History",
                onClick = {
                    onNavigateToSmartHistory()
                    onDismiss()
                }
            )

            DrawerMenuItem(
                icon = "⚙️",
                label = "Settings",
                onClick = {
                    onNavigateToSettings()
                    onDismiss()
                }
            )

            Spacer(Modifier.weight(1f))

            // ══════════ LOGOUT BUTTON ══════════
            Surface(
                onClick = {
                    onLogout()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                color = ModernError.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    ModernError.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🚪", fontSize = 18.sp)
                    Text(
                        "Logout",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ModernError
                    )
                }
            }

            // App version (optional)
            Text(
                text = "CampusFind+ v1.0.0",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.3f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun DrawerMenuItem(
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 18.sp)
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}