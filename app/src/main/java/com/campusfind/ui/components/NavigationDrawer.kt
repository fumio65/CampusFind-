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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Modern Navigation Drawer
 * ✅ SPAM-PROOF: Prevents rapid-click navigation crashes
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
    val scope = rememberCoroutineScope()

    // ✅ ANTI-SPAM: Track navigation state
    var isNavigating by remember { mutableStateOf(false) }
    var lastClickTime by remember { mutableStateOf(0L) }

    fun safeNavigate(action: () -> Unit) {
        val currentTime = System.currentTimeMillis()

        // Block rapid clicks (within 500ms)
        if (isNavigating || (currentTime - lastClickTime) < 500L) {
            android.util.Log.d("NavigationDrawer", "⚠️ Click blocked - too fast!")
            return
        }

        isNavigating = true
        lastClickTime = currentTime

        scope.launch {
            try {
                onDismiss()        // 1. Start closing drawer
                delay(300)         // 2. Wait for close animation
                action()           // 3. Navigate
                delay(200)         // 4. Safety buffer before allowing next click
            } catch (e: Exception) {
                android.util.Log.e("NavigationDrawer", "Navigation error", e)
            } finally {
                isNavigating = false
            }
        }
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
            // ══════════ PROFILE HEADER ══════════
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .clickable { safeNavigate(onNavigateToProfile) }
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

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color.White.copy(alpha = 0.1f)
            )

            // ══════════ MENU ITEMS ══════════

            DrawerMenuItem(
                icon = "👤",
                label = "Profile",
                onClick = { safeNavigate(onNavigateToProfile) },
                isEnabled = !isNavigating
            )

            DrawerMenuItem(
                icon = "📊",
                label = "Smart History",
                onClick = { safeNavigate(onNavigateToSmartHistory) },
                isEnabled = !isNavigating
            )

            DrawerMenuItem(
                icon = "⚙️",
                label = "Settings",
                onClick = { safeNavigate(onNavigateToSettings) },
                isEnabled = !isNavigating
            )

            Spacer(Modifier.weight(1f))

            // ══════════ LOGOUT BUTTON ══════════
            Surface(
                onClick = { safeNavigate(onLogout) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                color = ModernError.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    ModernError.copy(alpha = 0.3f)
                ),
                enabled = !isNavigating
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
                        color = if (isNavigating)
                            ModernError.copy(alpha = 0.5f)
                        else
                            ModernError
                    )
                }
            }

            // App version
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
    onClick: () -> Unit,
    isEnabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        enabled = isEnabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                icon,
                fontSize = 18.sp,
                color = if (isEnabled) Color.White else Color.White.copy(alpha = 0.3f)
            )
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isEnabled)
                    Color.White.copy(alpha = 0.9f)
                else
                    Color.White.copy(alpha = 0.3f)
            )
        }
    }
}