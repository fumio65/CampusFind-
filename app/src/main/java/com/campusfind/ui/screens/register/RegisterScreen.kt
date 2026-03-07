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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.campusfind.ui.theme.*

/**
 * Modern register screen - scrollable with 5 fields + password strength indicator
 */
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
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0a0a0f),
                        Color(0xFF1a1a2e)
                    )
                )
            )
    ) {
        // ── Gradient orbs ──
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = 30.dp, y = (-40).dp)
                .align(Alignment.TopEnd)
                .background(
                    color = Color(0xFFec4899).copy(alpha = 0.28f),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(150.dp)
                .offset(x = (-25).dp, y = 35.dp)
                .align(Alignment.BottomStart)
                .background(
                    color = ModernFound.copy(alpha = 0.22f),
                    shape = CircleShape
                )
        )

        // ── Scrollable content ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            // ── Header ──
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Create Account",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-0.7).sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Join the community",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Form card ──
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.05f)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 16.dp)
                ) {

                    // ── Full Name ──
                    Text(
                        text = "Full Name",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = uiState.fullName,
                        onValueChange = { viewModel.onFullNameChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Juan dela Cruz",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.3f)
                            )
                        },
                        leadingIcon = {
                            Text(text = "👤", fontSize = 14.sp, modifier = Modifier.alpha(0.6f))
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 12.sp,
                            color = Color.White
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(11.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.08f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
                            focusedBorderColor = Color.White.copy(alpha = 0.05f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.05f),
                            cursorColor = ModernAccent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // ── University Email ──
                    Text(
                        text = "University Email",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "you@university.edu",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.3f)
                            )
                        },
                        leadingIcon = {
                            Text(text = "✉️", fontSize = 14.sp, modifier = Modifier.alpha(0.6f))
                        },
                        trailingIcon = {
                            Text(text = "🎓", fontSize = 13.sp, modifier = Modifier.alpha(0.5f))
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 12.sp,
                            color = Color.White
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(11.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.08f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
                            focusedBorderColor = Color.White.copy(alpha = 0.05f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.05f),
                            cursorColor = ModernAccent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // ── Password ──
                    Text(
                        text = "Password",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.onPasswordChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Min. 8 characters",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.3f)
                            )
                        },
                        leadingIcon = {
                            Text(text = "🔒", fontSize = 14.sp, modifier = Modifier.alpha(0.6f))
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) {
                                        Icons.Default.Visibility
                                    } else {
                                        Icons.Default.VisibilityOff
                                    },
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = if (passwordVisible) 1f else 0.5f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 12.sp,
                            color = Color.White
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(11.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.08f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
                            focusedBorderColor = Color.White.copy(alpha = 0.05f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.05f),
                            cursorColor = ModernAccent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    // Password strength indicator (dynamic)
                    if (uiState.password.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))

                        val strength = when {
                            uiState.password.length < 6 -> Triple("Weak", ModernError, 0.3f)
                            uiState.password.length < 8 -> Triple("Fair", ModernWarning, 0.6f)
                            else -> Triple("Strong", ModernFound, 0.88f)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Strength",
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Text(
                                text = strength.first,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = strength.second
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(
                                    Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(2.dp)
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(strength.third)
                                    .height(3.dp)
                                    .background(
                                        color = strength.second,
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(7.dp))

                    // ── Confirm Password ──
                    Text(
                        text = "Confirm Password",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = uiState.confirmPassword,
                        onValueChange = { viewModel.onConfirmPasswordChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Re-enter password",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.3f)
                            )
                        },
                        leadingIcon = {
                            Text(text = "🔒", fontSize = 14.sp, modifier = Modifier.alpha(0.6f))
                        },
                        trailingIcon = {
                            if (uiState.confirmPassword.isNotEmpty() && uiState.password == uiState.confirmPassword) {
                                Text(text = "✓", fontSize = 13.sp, color = ModernFound)
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 12.sp,
                            color = Color.White
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(11.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.08f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
                            focusedBorderColor = Color.White.copy(alpha = 0.05f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.05f),
                            cursorColor = ModernAccent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // ── Messenger Username (Optional) ──
                    Row(
                        modifier = Modifier.padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "Messenger Username",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = ModernFound.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                ModernFound.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = "OPTIONAL",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = ModernFound,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = uiState.messengerHandle ?: "",
                        onValueChange = { viewModel.onMessengerHandleChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "your.messenger.username",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.3f)
                            )
                        },
                        leadingIcon = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "💬", fontSize = 14.sp)
                                Text(
                                    text = "@",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0084ff).copy(alpha = 0.8f)
                                )
                            }
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 12.sp,
                            color = Color.White
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(11.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF0084ff).copy(alpha = 0.12f),
                            unfocusedContainerColor = Color(0xFF0084ff).copy(alpha = 0.12f),
                            focusedBorderColor = Color(0xFF0084ff).copy(alpha = 0.35f),
                            unfocusedBorderColor = Color(0xFF0084ff).copy(alpha = 0.35f),
                            cursorColor = Color(0xFF0084ff),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Text(
                        text = "🔒 Only shown to approved finders — helps them contact you to return your item",
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.4f),
                        lineHeight = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Create Account button ──
                    Button(
                        onClick = { viewModel.onRegisterClicked(onSuccess = onNavigateToHome) },  // ← FIXED
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        enabled = !uiState.isSubmitting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF667eea),
                                            Color(0xFF764ba2)
                                        )
                                    )
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
                                    text = "Create Account",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.3.sp
                                )
                            }
                        }
                    }

                    // ── Error message ──
                    if (uiState.error != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = uiState.error!!,
                            fontSize = 11.sp,
                            color = ModernError,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Login link ──
            TextButton(onClick = onNavigateBack) {
                Text(
                    text = "Already have an account? Sign In",
                    fontSize = 12.sp,
                    color = Color(0xFFb794f6)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}