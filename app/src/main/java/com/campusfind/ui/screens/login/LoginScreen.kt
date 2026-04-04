package com.campusfind.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.campusfind.ui.theme.*

/**
 * Modern login screen - NO wrapper card, fields directly on gradient background
 */
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

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
                .size(200.dp)
                .offset(x = (-40).dp, y = (-50).dp)
                .background(
                    color = ModernAccent.copy(alpha = 0.3f),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 30.dp, y = 40.dp)
                .background(
                    color = Color(0xFFec4899).copy(alpha = 0.25f),
                    shape = CircleShape
                )
        )

        // ── Main content ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Header ──
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Welcome Back",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-0.8).sp,
                    lineHeight = 36.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Sign in to continue",
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Frosted glass form card ──
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.05f)
                // Border removed completely
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp)
                ) {

                    // ── Email field ──
                    Text(
                        text = "Email",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 7.dp)
                    )

                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "you@university.edu",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.3f)
                            )
                        },
                        leadingIcon = {
                            Text(
                                text = "✉️",
                                fontSize = 15.sp,
                                modifier = Modifier.alpha(0.6f)
                            )
                        },
                        textStyle = TextStyle(
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = Color.White
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // ── Password field ──
                    Text(
                        text = "Password",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 7.dp)
                    )

                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.onPasswordChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "••••••••",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.3f)
                            )
                        },
                        leadingIcon = {
                            Text(
                                text = "🔒",
                                fontSize = 15.sp,
                                modifier = Modifier.alpha(0.6f)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) {
                                        Icons.Default.Visibility
                                    } else {
                                        Icons.Default.VisibilityOff
                                    },
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = Color.White.copy(alpha = if (passwordVisible) 1f else 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        textStyle = TextStyle(
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = Color.White
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Remember Me + Forgot Password ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.clickable { rememberMe = !rememberMe }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        brush = if (rememberMe) {
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFF667eea),
                                                    Color(0xFF764ba2)
                                                )
                                            )
                                        } else {
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color.White.copy(alpha = 0.1f),
                                                    Color.White.copy(alpha = 0.1f)
                                                )
                                            )
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (rememberMe) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Checked",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Remember me",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }

                        Text(
                            text = "Forgot?",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFb794f6),
                            modifier = Modifier.clickable { /* TODO */ }
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // ── Sign In button ──
                    Button(
                        onClick = { viewModel.onLoginClicked(onSuccess = onLoginSuccess) },
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
                                    text = "Sign In",
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

            Spacer(modifier = Modifier.height(24.dp))

            // ── Register link ──
            val annotatedString = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                ) {
                    append("New here? ")
                }
                pushStringAnnotation(tag = "register", annotation = "register")
                withStyle(
                    style = SpanStyle(
                        color = Color(0xFFb794f6),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append("Create Account")
                }
                pop()
            }

            ClickableText(
                text = annotatedString,
                onClick = { offset ->
                    annotatedString.getStringAnnotations(
                        tag = "register",
                        start = offset,
                        end = offset
                    ).firstOrNull()?.let {
                        onNavigateToRegister()
                    }
                }
            )
        }
    }
}