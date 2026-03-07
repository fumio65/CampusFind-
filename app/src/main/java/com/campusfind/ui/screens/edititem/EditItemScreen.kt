package com.campusfind.ui.screens.edititem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.campusfind.ui.theme.*

/**
 * EditItemScreen - Edit existing lost item report
 * Pre-fills form with current item data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")  // ← Suppress ArrowBack deprecation warning
@Composable
fun EditItemScreen(
    itemId: String,
    onNavigateBack: () -> Unit,
    viewModel: EditItemViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(itemId) {
        viewModel.loadItem(itemId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Report",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ModernAccent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFf4f4f0))
        ) {
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ModernAccent)
                    }
                }

                uiState.error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("❌", fontSize = 48.sp)
                            Text(
                                uiState.error ?: "Unknown error",
                                color = ModernError,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title field
                        OutlinedTextField(
                            value = uiState.title,
                            onValueChange = { viewModel.onTitleChanged(it) },
                            label = { Text("Item Title") },
                            placeholder = { Text("e.g., Black Wallet") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = uiState.titleError != null,
                            supportingText = uiState.titleError?.let {
                                { Text(it, color = ModernError) }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ModernAccent,
                                focusedLabelColor = ModernAccent
                            )
                        )

                        // Description field
                        OutlinedTextField(
                            value = uiState.description,
                            onValueChange = { viewModel.onDescriptionChanged(it) },
                            label = { Text("Description") },
                            placeholder = { Text("Describe the item, where it was lost...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            maxLines = 5,
                            isError = uiState.descriptionError != null,
                            supportingText = uiState.descriptionError?.let {
                                { Text(it, color = ModernError) }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ModernAccent,
                                focusedLabelColor = ModernAccent
                            )
                        )

                        // TODO: Add photo upload when implementing Phase 2
                        // For now, just show placeholder
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFf9f9f6),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.5.dp,
                                color = Color(0xFFe4e4e0)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("📷", fontSize = 40.sp)
                                Text(
                                    "Photo upload coming in Phase 2",
                                    fontSize = 12.sp,
                                    color = Color(0xFF888888)
                                )
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        // Save button
                        Button(
                            onClick = {
                                viewModel.onSave(
                                    onSuccess = onNavigateBack
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = !uiState.isSaving,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ModernAccent,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    "Save Changes",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}