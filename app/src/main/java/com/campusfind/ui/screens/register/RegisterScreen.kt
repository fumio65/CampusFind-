package com.campusfind.ui.screens.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * FILE: app/src/main/java/com/campusfind/ui/screens/register/RegisterScreen.kt
 *
 * Registration screen — demo steps 2 and 7.
 *
 * Why verticalScroll:
 * - 6 input fields + error message + button = ~680dp height
 * - On small screens, content would be cut off without scrolling
 * - verticalScroll makes the entire form scrollable
 *
 * Why password visibility toggle:
 * - User can verify they typed the password correctly before submitting
 * - Standard UX pattern in auth forms
 *
 * Why hiltViewModel():
 * - Retrieves the RegisterViewModel instance from Hilt
 * - No manual ViewModel factory needed — Hilt handles it
 *
 * Why collectAsState():
 * - Converts StateFlow<RegisterUiState> to State<RegisterUiState>
 * - Automatically triggers recomposition when state changes
 *
 * Why onSuccess callback clears the back stack:
 * - After successful registration, back button should NOT return to RegisterScreen
 * - User is logged in — they should stay on HomeScreen until they logout
 *
 * See: DEC-005 (Jetpack Compose), DEC-013 (Single Activity),
 *      TASK-107, demo steps 2 & 7
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Error message
            if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Full Name
            OutlinedTextField(
                value = uiState.fullName,
                onValueChange = viewModel::onFullNameChanged,
                label = { Text("Full Name") },
                placeholder = { Text("Juan dela Cruz") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Email
            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChanged,
                label = { Text("University Email") },
                placeholder = { Text("juan@university.edu.ph") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Password
            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChanged,
                label = { Text("Password") },
                placeholder = { Text("At least 6 characters") },
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Default.Visibility
                            } else {
                                Icons.Default.VisibilityOff
                            },
                            contentDescription = if (passwordVisible) {
                                "Hide password"
                            } else {
                                "Show password"
                            }
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Confirm Password
            OutlinedTextField(
                value = uiState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChanged,
                label = { Text("Confirm Password") },
                visualTransformation = if (confirmPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) {
                                Icons.Default.Visibility
                            } else {
                                Icons.Default.VisibilityOff
                            },
                            contentDescription = if (confirmPasswordVisible) {
                                "Hide password"
                            } else {
                                "Show password"
                            }
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Messenger Handle (optional)
            OutlinedTextField(
                value = uiState.messengerHandle,
                onValueChange = viewModel::onMessengerHandleChanged,
                label = { Text("Messenger Username (optional)") },
                placeholder = { Text("@juandc") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // Submit Button
            Button(
                onClick = {
                    viewModel.onSubmit(onSuccess = onNavigateToHome)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSubmitting
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Account")
                }
            }
        }
    }
}