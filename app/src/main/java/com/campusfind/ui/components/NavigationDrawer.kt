package com.campusfind.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
 *
 * FIX: Removed delay() + coroutine navigation pattern which caused
 * silent crashes when composition left during the 300ms wait.
 * Now uses simple immediate dismiss + navigate — Compose Navigation
 * handles the back stack safely without needing artificial delays.
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
    // Simple debounce — prevents double-tap, no coroutines needed
    var lastClickTime by remember { mutableStateOf(0L) }

    fun safeNavigate(action: () -> Unit) {
        val now = System.currentTimeMillis()
        if (now - lastClickTime < 500L) return
        lastClickTime = now
        onDismiss()   // dismiss drawer immediately
        action()      // navigate — NavController is safe to call instantly
    }

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
            // ── Profile header ─────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .clickable { safeNavigate(onNavigateToProfile) }
            ) {
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

                Text(
                    text = currentUserName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = currentUserEmail,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color.White.copy(alpha = 0.1f)
            )

            // ── Menu items ─────────────────────────────────────────────────
            DrawerMenuItem(
                icon  = "👤",
                label = "Profile",
                onClick = { safeNavigate(onNavigateToProfile) }
            )
            DrawerMenuItem(
                icon  = "📊",
                label = "Smart History",
                onClick = { safeNavigate(onNavigateToSmartHistory) }
            )
            DrawerMenuItem(
                icon  = "⚙️",
                label = "Settings",
                onClick = { safeNavigate(onNavigateToSettings) }
            )

            Spacer(Modifier.weight(1f))

            // ── Logout ─────────────────────────────────────────────────────
            Surface(
                onClick = { safeNavigate(onLogout) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                color = ModernError.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, ModernError.copy(alpha = 0.3f)
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
            Text(icon, fontSize = 18.sp, color = Color.White)
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}