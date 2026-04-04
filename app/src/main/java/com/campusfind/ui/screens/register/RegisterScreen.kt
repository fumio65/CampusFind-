package com.campusfind.ui.screens.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.campusfind.ui.theme.*

@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF0a0a0f), Color(0xFF1a1a2e))
                )
            )
    ) {
        // Ambient orbs
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = 30.dp, y = (-40).dp)
                .align(Alignment.TopEnd)
                .background(Color(0xFFec4899).copy(0.28f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(150.dp)
                .offset(x = (-25).dp, y = 35.dp)
                .align(Alignment.BottomStart)
                .background(ModernFound.copy(0.22f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            // Header
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Text("Create Account", fontSize = 24.sp, fontWeight = FontWeight.Black,
                    color = Color.White, letterSpacing = (-0.7).sp)
                Spacer(Modifier.height(3.dp))
                Text("Join the community", fontSize = 12.sp, color = Color.White.copy(0.5f))
            }

            Spacer(Modifier.height(14.dp))

            // Form card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(0.05f)
            ) {
                Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 16.dp)) {

                    // Full Name
                    FieldLabel("Full Name")
                    OutlinedTextField(
                        value = uiState.fullName,
                        onValueChange = { viewModel.onFullNameChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { FieldPlaceholder("Juan dela Cruz") },
                        leadingIcon = { Text("👤", fontSize = 14.sp, modifier = Modifier.alpha(0.6f)) },
                        textStyle = fieldTextStyle(),
                        singleLine = true,
                        shape = RoundedCornerShape(11.dp),
                        colors = fieldColors()
                    )

                    Spacer(Modifier.height(10.dp))

                    // University Email
                    FieldLabel("University Email")
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { FieldPlaceholder("you@university.edu") },
                        leadingIcon = { Text("✉️", fontSize = 14.sp, modifier = Modifier.alpha(0.6f)) },
                        trailingIcon = { Text("🎓", fontSize = 13.sp, modifier = Modifier.alpha(0.5f)) },
                        textStyle = fieldTextStyle(),
                        singleLine = true,
                        shape = RoundedCornerShape(11.dp),
                        colors = fieldColors()
                    )

                    Spacer(Modifier.height(10.dp))

                    // Password
                    FieldLabel("Password")
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.onPasswordChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { FieldPlaceholder("Min. 8 characters") },
                        leadingIcon = { Text("🔒", fontSize = 14.sp, modifier = Modifier.alpha(0.6f)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color.White.copy(if (passwordVisible) 1f else 0.5f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        textStyle = fieldTextStyle(),
                        singleLine = true,
                        shape = RoundedCornerShape(11.dp),
                        colors = fieldColors()
                    )

                    // Password strength
                    if (uiState.password.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        val strength = when {
                            uiState.password.length < 6 -> Triple("Weak",   ModernError,   0.3f)
                            uiState.password.length < 8 -> Triple("Fair",   ModernWarning, 0.6f)
                            else                        -> Triple("Strong", ModernFound,   0.88f)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Strength", fontSize = 9.sp, color = Color.White.copy(0.5f))
                            Text(strength.first, fontSize = 9.sp,
                                fontWeight = FontWeight.Bold, color = strength.second)
                        }
                        Spacer(Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(3.dp)
                            .background(Color.White.copy(0.1f), RoundedCornerShape(2.dp))) {
                            Box(modifier = Modifier.fillMaxWidth(strength.third).height(3.dp)
                                .background(strength.second, RoundedCornerShape(2.dp)))
                        }
                    }

                    Spacer(Modifier.height(7.dp))

                    // Confirm Password
                    FieldLabel("Confirm Password")
                    OutlinedTextField(
                        value = uiState.confirmPassword,
                        onValueChange = { viewModel.onConfirmPasswordChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { FieldPlaceholder("Re-enter password") },
                        leadingIcon = { Text("🔒", fontSize = 14.sp, modifier = Modifier.alpha(0.6f)) },
                        trailingIcon = {
                            if (uiState.confirmPassword.isNotEmpty() &&
                                uiState.password == uiState.confirmPassword) {
                                Text("✓", fontSize = 13.sp, color = ModernFound)
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        textStyle = fieldTextStyle(),
                        singleLine = true,
                        shape = RoundedCornerShape(11.dp),
                        colors = fieldColors()
                    )

                    Spacer(Modifier.height(10.dp))

                    // Messenger Username (REQUIRED)
                    Row(
                        modifier = Modifier.padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text("Messenger Username", fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold, color = Color.White.copy(0.8f))
                        Text("*", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ModernError)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = ModernError.copy(0.2f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, ModernError.copy(0.3f))
                        ) {
                            Text("REQUIRED", fontSize = 8.sp, fontWeight = FontWeight.Bold,
                                color = ModernError,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
                        }
                    }

                    // Messenger field — @ prefix shown as separate label
                    // to avoid cursor bug caused by Row inside leadingIcon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        // @ prefix box outside the field
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 11.dp, bottomStart = 11.dp,
                                topEnd = 0.dp, bottomEnd = 0.dp
                            ),
                            color = Color(0xFF0084ff).copy(0.18f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, Color(0xFF0084ff).copy(0.35f)
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(52.dp)
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "@",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0084ff)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = (uiState.messengerHandle ?: "").removePrefix("@"),
                            onValueChange = { viewModel.onMessengerHandleChanged(it.removePrefix("@")) },
                            modifier = Modifier.weight(1f),
                            placeholder = { FieldPlaceholder("your.messenger.username") },
                            textStyle = TextStyle(
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = Color.White
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(
                                topStart = 0.dp, bottomStart = 0.dp,
                                topEnd = 11.dp, bottomEnd = 11.dp
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = Color(0xFF0084ff).copy(0.12f),
                                unfocusedContainerColor = Color(0xFF0084ff).copy(0.12f),
                                focusedBorderColor      = Color(0xFF0084ff).copy(0.35f),
                                unfocusedBorderColor    = Color(0xFF0084ff).copy(0.35f),
                                cursorColor             = Color(0xFF0084ff),
                                focusedTextColor        = Color.White,
                                unfocusedTextColor      = Color.White
                            )
                        )
                    }

                    Text(
                        "🔒 Needed to coordinate item return with finders — only shown after you approve their claim",
                        fontSize = 9.sp, color = Color.White.copy(0.4f),
                        lineHeight = 12.sp, modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    // Create Account button
                    Button(
                        onClick = { viewModel.onRegisterClicked(onSuccess = onNavigateToHome) },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        enabled = !uiState.isSubmitting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor   = Color.White
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF667eea), Color(0xFF764ba2))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White, strokeWidth = 2.dp
                                )
                            } else {
                                Text("Create Account", fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold, letterSpacing = 0.3.sp)
                            }
                        }
                    }

                    // Error
                    if (uiState.error != null) {
                        Spacer(Modifier.height(12.dp))
                        Text(uiState.error!!, fontSize = 11.sp, color = ModernError,
                            modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            TextButton(onClick = onNavigateBack) {
                Text("Already have an account? Sign In",
                    fontSize = 12.sp, color = Color(0xFFb794f6))
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

// ── Shared field helpers ───────────────────────────────────────────────────

@Composable
private fun FieldLabel(text: String) {
    Text(text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
        color = Color.White.copy(0.8f), modifier = Modifier.padding(bottom = 6.dp))
}

@Composable
private fun FieldPlaceholder(text: String) {
    Text(text, fontSize = 12.sp, color = Color.White.copy(0.3f))
}

@Composable
private fun fieldTextStyle() = TextStyle(
    fontSize = 12.sp, lineHeight = 16.sp, color = Color.White
    // No textDirection here — default (Content) is correct for all fields
)

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = Color.White.copy(0.08f),
    unfocusedContainerColor = Color.White.copy(0.08f),
    focusedBorderColor      = Color.White.copy(0.05f),
    unfocusedBorderColor    = Color.White.copy(0.05f),
    cursorColor             = ModernAccent,
    focusedTextColor        = Color.White,
    unfocusedTextColor      = Color.White
)