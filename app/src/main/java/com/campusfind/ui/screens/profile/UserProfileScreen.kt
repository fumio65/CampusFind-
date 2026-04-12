package com.campusfind.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.LostItem
import com.campusfind.ui.theme.*
import com.campusfind.ui.util.buildImageRequest
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// ── Helpers ────────────────────────────────────────────────────────────────

private fun UserProfileUiState.hasAchievements(): Boolean {
    val foundItems = items.count { it.status == ItemStatus.FOUND }
    return items.size >= 5 || foundItems >= 3 || hasEarlyAdopterBadge()
}

private fun UserProfileUiState.hasEarlyAdopterBadge(): Boolean {
    if (joinedDate == null) return false
    val joinMonth = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(joinedDate!!))
    return joinMonth == "Jan 2026"
}

private fun UserProfileUiState.hasMessengerConnected(): Boolean =
    messengerHandle != null && messengerHandle.isNotBlank()

private fun UserProfileUiState.hasTrustScoreData(): Boolean =
    (trustScore ?: 0) > 0

// ══════════════════════════════════════════════════════════════════════════
// SCREEN
// ══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onNavigateToAddItem: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors  = LocalAppColors.current

    LaunchedEffect(Unit) { viewModel.loadProfile() }

    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(pullRefreshState.isRefreshing) {
        if (pullRefreshState.isRefreshing) {
            val snapName  = viewModel.uiState.value.userName
            val snapItems = viewModel.uiState.value.items.size
            viewModel.refresh()
            var waited = 0
            while (waited < 5000) {
                kotlinx.coroutines.delay(200)
                waited += 200
                val cur = viewModel.uiState.value
                if (cur.userName != snapName || cur.items.size != snapItems) break
            }
            pullRefreshState.endRefresh()
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(colors.screenBg)
        .nestedScroll(pullRefreshState.nestedScrollConnection)) {
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ModernAccent)
                }
            }
            uiState.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("⚠️", fontSize = 48.sp)
                        Text(uiState.error ?: "Unknown error", fontSize = 14.sp, color = ModernError)
                        Button(onClick = { viewModel.loadProfile() },
                            colors = ButtonDefaults.buttonColors(containerColor = ModernAccent),
                            shape = RoundedCornerShape(12.dp)) { Text("Retry") }
                    }
                }
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        ProfileHero(
                            uiState               = uiState,
                            onNavigateToSettings  = onNavigateToSettings,
                            onPhotoChanged        = { viewModel.updateProfilePhoto(it) },
                            onEditProfile         = { name, email -> viewModel.updateProfile(name, email) }
                        )
                    }
                    if (uiState.hasAchievements()) {
                        item { AchievementsSection(uiState = uiState, colors = colors) }
                    }
                    item { MessengerSection(uiState = uiState, colors = colors, viewModel = viewModel) }
                    if (uiState.hasTrustScoreData()) {
                        item { TrustScoreSection(uiState = uiState, colors = colors) }
                    }
                    item { QuickActionsSection(colors = colors, onNavigateToAddItem = onNavigateToAddItem) }
                    item {
                        Text("YOUR ITEMS (${uiState.items.size})", fontSize = 10.sp,
                            fontWeight = FontWeight.Bold, color = colors.textMuted, letterSpacing = 0.6.sp,
                            modifier = Modifier.padding(top = 14.dp, start = 16.dp, bottom = 8.dp))
                    }
                    if (uiState.items.isEmpty()) {
                        item {
                            Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                                shape = RoundedCornerShape(16.dp), color = colors.cardBg,
                                border = BorderStroke(1.dp, colors.cardBorder)) {
                                Column(modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📭", fontSize = 48.sp)
                                    Spacer(Modifier.height(12.dp))
                                    Text("No items posted yet", fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold, color = colors.textMuted)
                                    Text("Report a lost item to get started",
                                        fontSize = 12.sp, color = colors.textMuted)
                                    Spacer(Modifier.height(16.dp))
                                    Button(onClick = onNavigateToAddItem,
                                        colors = ButtonDefaults.buttonColors(containerColor = ModernAccent),
                                        shape = RoundedCornerShape(12.dp)) { Text("Post Item") }
                                }
                            }
                        }
                    } else {
                        items(items = uiState.items, key = { it.id }) { item ->
                            ProfileItemCard(item = item, colors = colors,
                                onClick = { onNavigateToDetail(item.id) },
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp))
                        }
                    }
                    item { LogoutSection(onLogout = onLogout, colors = colors) }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }

        // Pull-to-refresh indicator
        PullToRefreshContainer(
            state          = pullRefreshState,
            modifier       = Modifier.align(Alignment.TopCenter),
            containerColor = if (colors.isDark) Color(0xFF1C1B2E) else Color.White,
            contentColor   = ModernAccent
        )

        // Save success toast
        if (uiState.saveSuccess) {
            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)) {
                Surface(shape = RoundedCornerShape(24.dp), color = ModernFound,
                    shadowElevation = 8.dp) {
                    Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("✓", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Profile updated", fontSize = 13.sp, color = Color.White,
                            fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// HERO — profile photo + edit button
// ══════════════════════════════════════════════════════════════════════════

@Composable
private fun ProfileHero(
    uiState: UserProfileUiState,
    onNavigateToSettings: () -> Unit,
    onPhotoChanged: (String) -> Unit,
    onEditProfile: (name: String, email: String) -> Unit
) {
    var showEditDialog   by remember { mutableStateOf(false) }
    var previewUri       by remember { mutableStateOf<android.net.Uri?>(null) }
    val context          = LocalContext.current

    // After picking, show preview dialog instead of saving immediately
    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { previewUri = it }
    }

    // Photo preview + confirm dialog
    // Pass the raw content URI string to the ViewModel — PhotoManager.savePhoto()
    // handles copying it to permanent internal storage on IO dispatcher.
    if (previewUri != null) {
        PhotoPreviewConfirmDialog(
            uri       = previewUri!!,
            onConfirm = {
                onPhotoChanged(previewUri.toString())
                previewUri = null
            },
            onDismiss = { previewUri = null }
        )
    }

    Box(modifier = Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
        .background(Brush.linearGradient(
            colors = listOf(Color(0xFF12101E), Color(0xFF1E1340), Color(0xFF0E1F18))))
        .padding(bottom = 20.dp)) {

        // Ambient orbs
        Box(modifier = Modifier.size(160.dp).offset(x = 220.dp, y = (-40).dp)
            .background(ModernAccent.copy(alpha = 0.15f), CircleShape))
        Box(modifier = Modifier.size(100.dp).offset(x = (-20).dp, y = 140.dp)
            .background(ModernFound.copy(alpha = 0.10f), CircleShape))

        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            Spacer(Modifier.height(8.dp))

            // Top bar — settings button
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
                Surface(onClick = onNavigateToSettings,
                    modifier = Modifier.size(36.dp).align(Alignment.CenterEnd),
                    shape = CircleShape, color = Color.White.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))) {
                    Box(contentAlignment = Alignment.Center) { Text("⚙", fontSize = 15.sp, color = Color.White) }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Avatar + edit button
            Column(modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)) {

                // Profile photo with camera overlay
                Box(contentAlignment = Alignment.BottomEnd) {
                    // Avatar circle — shows photo if available, letter otherwise
                    Box(modifier = Modifier.size(88.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(ModernAccent, Color(0xFF4F46E5)))),
                        contentAlignment = Alignment.Center) {

                        val photoRequest = buildImageRequest(context, uiState.profilePhotoUri)
                        if (photoRequest != null) {
                            Image(painter = rememberAsyncImagePainter(photoRequest),
                                contentDescription = "Profile photo",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop)
                        } else {
                            Text(uiState.userName?.firstOrNull()?.uppercase() ?: "U",
                                fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }

                    // Camera button overlay
                    Surface(onClick = { photoLauncher.launch("image/*") },
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape,
                        color = ModernAccent,
                        shadowElevation = 4.dp) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("📷", fontSize = 12.sp)
                        }
                    }
                }

                // Name + location
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(uiState.userName ?: "User", fontSize = 22.sp,
                            fontWeight = FontWeight.Black, color = Color.White,
                            style = LocalTextStyle.current.copy(
                                shadow = Shadow(Color.Black.copy(0.5f), Offset(0f, 2f), 10f)))
                        // Edit profile button
                        Surface(onClick = { showEditDialog = true },
                            modifier = Modifier.size(26.dp), shape = CircleShape,
                            color = Color.White.copy(0.15f),
                            border = BorderStroke(1.dp, Color.White.copy(0.2f))) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("✏️", fontSize = 11.sp)
                            }
                        }
                    }
                    Text(buildString {
                        append("📍 Main Campus")
                        if (uiState.joinedDate != null) {
                            append(" · Since ${formatMonthYear(uiState.joinedDate!!)}")
                        }
                    }, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    // Email shown below name
                    if (!uiState.userEmail.isNullOrBlank()) {
                        Text(uiState.userEmail, fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.45f))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Stats row
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassStatCard("${uiState.items.size}", "Posted", false, Modifier.weight(1f))
                GlassStatCard("${uiState.items.count { it.status == ItemStatus.FOUND }}",
                    "Resolved", true, Modifier.weight(1f))
                GlassStatCard("0", "Helped", false, Modifier.weight(1f))
            }
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            currentName  = uiState.userName ?: "",
            currentEmail = uiState.userEmail ?: "",
            isSaving     = uiState.isSaving,
            onDismiss    = { showEditDialog = false },
            onSave       = { name, email ->
                onEditProfile(name, email)
                showEditDialog = false
            }
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════
// EDIT PROFILE DIALOG
// ══════════════════════════════════════════════════════════════════════════

@Composable
private fun EditProfileDialog(
    currentName: String,
    currentEmail: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, email: String) -> Unit
) {
    var name  by remember { mutableStateOf(currentName) }
    var email by remember { mutableStateOf(currentEmail) }
    var nameError  by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    val colors = LocalAppColors.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = colors.cardBg, shadowElevation = 24.dp) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)) {

                // Header
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(ModernAccent, Color(0xFF4F46E5)))),
                        contentAlignment = Alignment.Center) {
                        Text("✏️", fontSize = 18.sp)
                    }
                    Column {
                        Text("Edit Profile", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                            color = colors.textPrimary)
                        Text("Update your name and email", fontSize = 11.sp, color = colors.textMuted)
                    }
                }

                HorizontalDivider(color = colors.cardBorder)

                // Full name field
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Full Name", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        color = colors.textSecondary)
                    OutlinedTextField(
                        value         = name,
                        onValueChange = { name = it; nameError = null },
                        placeholder   = { Text("e.g., Maria Santos", fontSize = 12.sp) },
                        isError       = nameError != null,
                        supportingText = nameError?.let { { Text(it, fontSize = 9.sp, color = ModernError) } },
                        colors        = outlinedFieldColors(colors),
                        shape         = RoundedCornerShape(10.dp),
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true
                    )
                }

                // Email field
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("University Email", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        color = colors.textSecondary)
                    OutlinedTextField(
                        value         = email,
                        onValueChange = { email = it; emailError = null },
                        placeholder   = { Text("e.g., maria@uni.edu", fontSize = 12.sp) },
                        isError       = emailError != null,
                        supportingText = emailError?.let { { Text(it, fontSize = 9.sp, color = ModernError) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors        = outlinedFieldColors(colors),
                        shape         = RoundedCornerShape(10.dp),
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true
                    )
                }

                // Action buttons
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, colors.cardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textSecondary)) {
                        Text("Cancel", fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            val trimName  = name.trim()
                            val trimEmail = email.trim()
                            var valid = true
                            if (trimName.length < 2) { nameError = "Name too short"; valid = false }
                            if (!trimEmail.contains("@") || !trimEmail.contains(".")) {
                                emailError = "Invalid email"; valid = false
                            }
                            if (valid) onSave(trimName, trimEmail)
                        },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = ModernAccent),
                        enabled  = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp),
                                color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Save", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun outlinedFieldColors(colors: AppColors) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = ModernAccent,
    unfocusedBorderColor    = colors.cardBorder,
    focusedTextColor        = colors.textPrimary,
    unfocusedTextColor      = colors.textPrimary,
    focusedContainerColor   = colors.cardBg,
    unfocusedContainerColor = colors.cardBg,
    cursorColor             = ModernAccent
)

// ══════════════════════════════════════════════════════════════════════════
// GLASS STAT CARD (hero)
// ══════════════════════════════════════════════════════════════════════════

@Composable
private fun GlassStatCard(value: String, label: String, isHighlighted: Boolean, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp),
        color = if (isHighlighted) ModernFound.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.10f),
        border = BorderStroke(1.5.dp,
            if (isHighlighted) ModernFound.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.16f))) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black,
                color = if (isHighlighted) ModernFound else Color.White,
                style = LocalTextStyle.current.copy(shadow = Shadow(Color.Black.copy(0.3f), Offset(0f, 1f), 4f)))
            Text(label.uppercase(), fontSize = 9.sp, color = Color.White.copy(alpha = 0.55f), letterSpacing = 0.3.sp)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// ACHIEVEMENTS
// ══════════════════════════════════════════════════════════════════════════

@Composable
private fun AchievementsSection(uiState: UserProfileUiState, colors: AppColors) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, start = 14.dp, end = 14.dp)) {
        SectionLabel("ACHIEVEMENTS", colors)
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (uiState.items.size >= 5) {
                AchievementBadge("🏆", "Helper", "${uiState.items.size} items posted",
                    if (colors.isDark) listOf(Color(0xFF1E1B3A), Color(0xFF16143A)) else listOf(Color(0xFFF0EFFF), Color(0xFFE8E6FF)),
                    if (colors.isDark) ModernAccent.copy(0.3f) else Color(0xFFD8D5FF),
                    ModernAccent, if (colors.isDark) ModernAccent.copy(0.7f) else Color(0xFF9A8DC8))
            }
            val foundCount = uiState.items.count { it.status == ItemStatus.FOUND }
            if (foundCount >= 3) {
                AchievementBadge("✋", "Finder", "$foundCount found",
                    if (colors.isDark) listOf(Color(0xFF0D2420), Color(0xFF0A1E1A)) else listOf(Color(0xFFEDFCF5), Color(0xFFD8FBED)),
                    if (colors.isDark) ModernFound.copy(0.3f) else Color(0xFFA8EECF),
                    ModernFound, if (colors.isDark) ModernFound.copy(0.7f) else Color(0xFF5AAD88))
            }
            if (uiState.hasEarlyAdopterBadge()) {
                AchievementBadge("⭐", "Early Adopter", "Jan 2026",
                    if (colors.isDark) listOf(Color(0xFF2A1F00), Color(0xFF1F1700)) else listOf(Color(0xFFFFF5E6), Color(0xFFFFE8CC)),
                    if (colors.isDark) Color(0xFFD4A300).copy(0.4f) else Color(0xFFFFD699),
                    Color(0xFFD4A300), if (colors.isDark) Color(0xFFB8900F) else Color(0xFFB8900F))
            }
        }
    }
}

@Composable
private fun AchievementBadge(
    emoji: String, title: String, subtitle: String,
    bgColors: List<Color>, borderColor: Color, titleColor: Color, subtitleColor: Color
) {
    Surface(modifier = Modifier.width(100.dp), shape = RoundedCornerShape(14.dp),
        color = Color.Transparent, border = BorderStroke(1.5.dp, borderColor), shadowElevation = 2.dp) {
        Box(modifier = Modifier.background(Brush.linearGradient(bgColors)).padding(12.dp),
            contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(emoji, fontSize = 28.sp)
                Spacer(Modifier.height(5.dp))
                Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = titleColor)
                Text(subtitle, fontSize = 9.sp, color = subtitleColor)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// MESSENGER
// ══════════════════════════════════════════════════════════════════════════

@Composable
private fun MessengerSection(uiState: UserProfileUiState, colors: AppColors, viewModel: UserProfileViewModel) {
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, start = 14.dp, end = 14.dp)) {
        SectionLabel("CONTACT INFO", colors)
        Surface(modifier = Modifier.fillMaxWidth().clickable { showDialog = true },
            shape = RoundedCornerShape(14.dp), color = colors.cardBg,
            border = BorderStroke(1.5.dp,
                if (uiState.hasMessengerConnected())
                    if (colors.isDark) Color(0xFF0084FF).copy(0.3f) else Color(0xFFE8E8E4)
                else
                    if (colors.isDark) ModernWarning.copy(0.3f) else Color(0xFFFFE8CC))) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF0084FF), Color(0xFF0066CC)))),
                        contentAlignment = Alignment.Center) { Text("💬", fontSize = 16.sp) }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Messenger", fontSize = 10.sp, color = colors.textMuted)
                        if (uiState.hasMessengerConnected()) {
                            Text("@${uiState.messengerHandle}", fontSize = 12.sp,
                                fontWeight = FontWeight.Bold, color = Color(0xFF0084FF))
                        } else {
                            Text("Not connected", fontSize = 12.sp,
                                fontWeight = FontWeight.Bold, color = colors.textMuted)
                        }
                    }
                    if (uiState.hasMessengerConnected()) {
                        Surface(shape = CircleShape, color = colors.pillBg) {
                            Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                                Text("✏️", fontSize = 12.sp)
                            }
                        }
                    } else {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF0084FF)) {
                            Text("Add", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(if (uiState.hasMessengerConnected())
                    "Finders can message you after you approve their claim. Tap to edit."
                else "Add your Messenger handle so finders can contact you after approval.",
                    fontSize = 9.sp, color = colors.textMuted, lineHeight = 13.sp)
            }
        }
        if (showDialog) {
            MessengerDialog(currentHandle = uiState.messengerHandle, colors = colors,
                onDismiss = { showDialog = false },
                onSave    = { handle -> viewModel.updateMessengerHandle(handle); showDialog = false })
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// TRUST SCORE
// ══════════════════════════════════════════════════════════════════════════

@Composable
private fun TrustScoreSection(uiState: UserProfileUiState, colors: AppColors) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, start = 14.dp, end = 14.dp)) {
        SectionLabel("REPUTATION", colors)
        Surface(shape = RoundedCornerShape(14.dp), color = Color.Transparent,
            border = BorderStroke(1.5.dp, if (colors.isDark) ModernAccent.copy(0.25f) else Color(0xFFD8D5FF))) {
            Box(modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(
                    if (colors.isDark) listOf(Color(0xFF1A1730), Color(0xFF141228))
                    else listOf(Color(0xFFF0EFFF), Color(0xFFE8E6FF))))
                .padding(14.dp)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape)
                            .background(Brush.linearGradient(listOf(ModernAccent, Color(0xFF4F46E5)))),
                            contentAlignment = Alignment.Center) {
                            Text("${uiState.trustScore ?: 0}", fontSize = 18.sp,
                                fontWeight = FontWeight.Black, color = Color.White)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(uiState.trustScoreLabel ?: "New User", fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold, color = ModernAccent)
                            Text(uiState.trustScoreRank ?: "Get started!", fontSize = 9.sp,
                                color = if (colors.isDark) ModernAccent.copy(0.6f) else Color(0xFF9A8DC8))
                        }
                    }
                    if (uiState.responseRate != null || uiState.recoveredRate != null) {
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.responseRate?.let { TrustMetric(it, "Response Rate", ModernFound, colors, Modifier.weight(1f)) }
                            uiState.avgReplyTime?.let { TrustMetric(it, "Avg Reply", ModernAccent, colors, Modifier.weight(1f)) }
                            uiState.recoveredRate?.let { TrustMetric(it, "Recovered", ModernFound, colors, Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrustMetric(value: String, label: String, color: Color, colors: AppColors, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(8.dp),
        color = if (colors.isDark) Color.White.copy(0.06f) else Color.White.copy(0.7f)) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, fontSize = 8.sp, color = colors.textMuted)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// QUICK ACTIONS
// ══════════════════════════════════════════════════════════════════════════

@Composable
private fun QuickActionsSection(colors: AppColors, onNavigateToAddItem: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, start = 14.dp, end = 14.dp)) {
        SectionLabel("QUICK ACTIONS", colors)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickActionCard("📢", "Post Item", onNavigateToAddItem, colors, Modifier.weight(1f))
        }
    }
}

@Composable
private fun QuickActionCard(emoji: String, label: String, onClick: () -> Unit, colors: AppColors, modifier: Modifier) {
    Surface(modifier = modifier.clickable(onClick = onClick), shape = RoundedCornerShape(12.dp),
        color = colors.cardBg, border = BorderStroke(1.dp, colors.cardBorder)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 24.sp)
            Spacer(Modifier.height(5.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// PROFILE ITEM CARD
// ══════════════════════════════════════════════════════════════════════════

@Composable
private fun ProfileItemCard(item: LostItem, colors: AppColors, onClick: () -> Unit, modifier: Modifier) {
    val statusColor = if (item.status == ItemStatus.LOST) ModernLost else ModernFound
    Surface(modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp), color = colors.cardBg,
        border = BorderStroke(1.dp, if (colors.isDark) statusColor.copy(alpha = 0.20f) else colors.cardBorder),
        shadowElevation = if (colors.isDark) 0.dp else 2.dp) {
        Row(modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                .background(statusColor.copy(alpha = if (colors.isDark) 0.15f else 0.10f)),
                contentAlignment = Alignment.Center) {
                Text(if (item.status == ItemStatus.LOST) "🔍" else "✅", fontSize = 20.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(item.title, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false))
                    Surface(shape = RoundedCornerShape(6.dp), color = statusColor.copy(alpha = 0.15f)) {
                        Text(if (item.status == ItemStatus.LOST) "LOST" else "FOUND",
                            fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = statusColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(item.description, fontSize = 11.sp, color = colors.textSecondary,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(3.dp))
                Text(formatTimestamp(item.reportedAt), fontSize = 9.sp, color = colors.textMuted)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// MESSENGER DIALOG
// ══════════════════════════════════════════════════════════════════════════

@Composable
private fun MessengerDialog(
    currentHandle: String?, colors: AppColors,
    onDismiss: () -> Unit, onSave: (String) -> Unit
) {
    val isEditMode = currentHandle != null
    var handle by remember { mutableStateOf(currentHandle ?: "") }
    var error  by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = colors.cardBg) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF0084FF), Color(0xFF0066CC)))),
                        contentAlignment = Alignment.Center) { Text("💬", fontSize = 18.sp) }
                    Column {
                        Text(if (isEditMode) "Edit Messenger" else "Add Messenger",
                            fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text(if (isEditMode) "Update your username" else "Let finders contact you",
                            fontSize = 11.sp, color = colors.textMuted)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Surface(shape = RoundedCornerShape(10.dp),
                    color = if (colors.isDark) Color(0xFF001830) else Color(0xFFF0F7FF),
                    border = BorderStroke(1.dp, Color(0xFF0084FF).copy(0.3f))) {
                    Row(modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("ℹ️", fontSize = 14.sp)
                        Text("After approving a claim, the finder can message you to arrange pickup.",
                            fontSize = 10.sp, color = Color(0xFF0084FF), lineHeight = 14.sp)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text("Messenger Username", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = colors.textSecondary, modifier = Modifier.padding(bottom = 6.dp))
                OutlinedTextField(
                    value = handle,
                    onValueChange = { handle = it.trimStart().replace(" ", "."); error = null },
                    placeholder = { Text("e.g., juan.delacruz", fontSize = 12.sp, color = colors.textMuted) },
                    leadingIcon = { Text("@", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0084FF)) },
                    isError = error != null,
                    supportingText = { Text(error ?: "Enter your Messenger username (without @)",
                        fontSize = 9.sp, color = if (error != null) ModernError else colors.textMuted) },
                    colors = outlinedFieldColors(colors),
                    shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, colors.cardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textSecondary)) {
                        Text("Cancel", fontSize = 12.sp)
                    }
                    Button(onClick = {
                        val t = handle.trim()
                        when {
                            t.isEmpty() -> error = "Username is required"
                            t.length < 3 -> error = "Min 3 characters"
                            !t.matches(Regex("^[a-zA-Z0-9._]+$")) -> error = "Only letters, numbers, dots, underscores"
                            isEditMode && t == currentHandle -> onDismiss()
                            else -> onSave(t)
                        }
                    }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0084FF))) {
                        Text(if (isEditMode) "Update" else "Save",
                            fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// LOGOUT
// ══════════════════════════════════════════════════════════════════════════

@Composable
private fun LogoutSection(onLogout: () -> Unit, colors: AppColors) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 20.dp, start = 14.dp, end = 14.dp)) {
        Surface(onClick = onLogout, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp), color = ModernError.copy(alpha = 0.10f),
            border = BorderStroke(1.dp, ModernError.copy(alpha = 0.30f))) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("🚪", fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Text("Log Out", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = ModernError)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// PHOTO PREVIEW + CONFIRM DIALOG
// ══════════════════════════════════════════════════════════════════════════

@Composable
private fun PhotoPreviewConfirmDialog(
    uri: android.net.Uri,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
        ) {
            // Full preview image
            Image(
                painter            = rememberAsyncImagePainter(uri),
                contentDescription = "Profile photo preview",
                modifier           = androidx.compose.ui.Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .align(Alignment.Center)
                    .clip(CircleShape),
                contentScale       = ContentScale.Crop
            )

            // Top label
            Box(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(listOf(Color.Black.copy(0.7f), Color.Transparent))
                    )
                    .padding(20.dp)
            ) {
                Text("Preview photo", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = androidx.compose.ui.Modifier.align(Alignment.Center))
            }

            // Bottom action buttons
            Row(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.85f)))
                    )
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cancel
                Surface(
                    onClick   = onDismiss,
                    modifier  = androidx.compose.ui.Modifier.weight(1f).height(50.dp),
                    shape     = RoundedCornerShape(14.dp),
                    color     = Color.White.copy(0.15f),
                    border    = BorderStroke(1.dp, Color.White.copy(0.3f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Retake", fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                            color = Color.White)
                    }
                }

                // Confirm
                Box(
                    modifier = androidx.compose.ui.Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(ModernAccent, Color(0xFF4F46E5))))
                        .clickable { onConfirm() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("✓", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Use Photo", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                            color = Color.White)
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════
// HELPERS
// ══════════════════════════════════════════════════════════════════════════

@Composable
private fun SectionLabel(text: String, colors: AppColors) {
    Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMuted,
        letterSpacing = 0.6.sp, modifier = Modifier.padding(bottom = 8.dp, start = 2.dp))
}

private fun formatMonthYear(millis: Long): String =
    SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(millis))

private fun formatTimestamp(millis: Long): String {
    val now   = System.currentTimeMillis()
    val diff  = now - millis
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days  = TimeUnit.MILLISECONDS.toDays(diff)
    return when {
        hours < 1  -> "Just now"
        hours < 24 -> "$hours hr${if (hours > 1) "s" else ""} ago"
        days < 7   -> "$days day${if (days > 1) "s" else ""} ago"
        else       -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(millis))
    }
}