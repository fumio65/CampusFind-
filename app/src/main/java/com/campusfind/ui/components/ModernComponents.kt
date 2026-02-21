package com.campusfind.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campusfind.domain.model.ItemStatus
import com.campusfind.ui.theme.*

/**
 * FILE: app/src/main/java/com/campusfind/ui/components/ModernComponents.kt
 *
 * Reusable modern UI components matching wireframes.html design.
 *
 * Components:
 * - ModernButton: Primary action button with gradient
 * - FrostedButton: Glass morphism button (for hero overlays)
 * - ModernTextField: Minimalist text input
 * - ModernStatusBadge: LOST/FOUND pill badge
 * - ModernChip: Filter chip component
 *
 * Design principles:
 * - Compact sizing (wireframes are very tight)
 * - High contrast borders
 * - Rounded corners (8-22dp range)
 * - Minimal padding
 */

// ═══════════════════════════════════════════════════════════════════════════
// BUTTONS
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Modern primary button with gradient background.
 * Used for: Submit, Login, Register, Mark as Found
 */
@Composable
fun ModernButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    gradient: Brush = Brush.linearGradient(
        colors = listOf(ModernAccent, Color(0xFF5246d5))
    )
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(44.dp),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContainerColor = ModernSurface2,
            disabledContentColor = ModernTextDim
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (enabled) gradient else SolidColor(ModernSurface2)),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 13.sp
                )
            }
        }
    }
}

/**
 * Frosted glass button with blur effect.
 * Used for: Back buttons, overlay buttons on gradient heroes
 */
@Composable
fun FrostedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: String? = null
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = Color.Black.copy(alpha = 0.38f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.18f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
            Text(
                text = text,
                fontSize = 11.sp,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// TEXT FIELDS
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Modern text field with clean styling.
 * Used for: Login, Register, Add Item forms
 */
@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isPassword: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Label
        Text(
            text = label,
            style = SectionLabelText,
            color = ModernTextMuted,
            modifier = Modifier.padding(start = 4.dp)
        )

        // Text field
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    color = ModernTextDim,
                    fontSize = 13.sp
                )
            },
            textStyle = LocalTextStyle.current.copy(
                fontSize = 14.sp,
                color = ModernText
            ),
            visualTransformation = if (isPassword) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            enabled = enabled,
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ModernSurface,
                unfocusedContainerColor = ModernSurface,
                disabledContainerColor = ModernSurface2,
                focusedBorderColor = ModernAccent,
                unfocusedBorderColor = ModernBorder,
                disabledBorderColor = ModernBorder,
                cursorColor = ModernAccent,
                focusedTextColor = ModernText,
                unfocusedTextColor = ModernText
            )
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// BADGES & CHIPS
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Status badge for LOST/FOUND items.
 * Used in: Item cards, detail screen
 */
@Composable
fun ModernStatusBadge(
    status: ItemStatus,
    modifier: Modifier = Modifier,
    style: BadgeStyle = BadgeStyle.Filled
) {
    val (bgColor, borderColor, textColor) = when (style) {
        BadgeStyle.Filled -> when (status) {
            ItemStatus.LOST -> Triple(
                ModernLostBg,
                ModernLost.copy(alpha = 0.6f),
                ModernLost
            )
            ItemStatus.FOUND -> Triple(
                ModernFoundBg,
                ModernFound.copy(alpha = 0.6f),
                ModernFoundText
            )
        }
        BadgeStyle.Glass -> when (status) {
            ItemStatus.LOST -> Triple(
                ModernLost.copy(alpha = 0.25f),
                ModernLost.copy(alpha = 0.6f),
                Color(0xFFff8099)
            )
            ItemStatus.FOUND -> Triple(
                ModernFound.copy(alpha = 0.25f),
                ModernFound.copy(alpha = 0.6f),
                Color(0xFF5fead4)
            )
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "●",
                fontSize = 6.sp,
                color = textColor
            )
            Text(
                text = status.name,
                style = TinyBadgeText,
                color = textColor,
                fontSize = 9.sp
            )
        }
    }
}

enum class BadgeStyle {
    Filled,  // Light background for cards
    Glass    // Transparent for hero overlays
}

/**
 * Modern filter chip (All / Lost / Found).
 * Used in: HomeScreen filter row
 */
@Composable
fun ModernChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = LightTextPrimary
) {
    val (bgColor, borderColor, textColor) = when {
        isSelected && text == "All" -> Triple(
            LightTextPrimary,
            LightTextPrimary,
            Color.White
        )
        isSelected && text == "Lost" -> Triple(
            ModernLostBg,
            ModernLost,
            ModernLost
        )
        isSelected && text == "Found" -> Triple(
            ModernFoundBg,
            ModernFound,
            ModernFoundText
        )
        else -> Triple(
            Color.Transparent,
            Color(0xFFd0d0cc),
            Color(0xFF666666)
        )
    }

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            fontSize = 11.sp,
            color = textColor,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// GRADIENT HELPERS
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Create gradient background matching wireframes.
 * Usage: modifier = Modifier.background(modernGradient())
 */
fun modernGradient(
    colors: List<Color> = listOf(
        Color(0xFF1a1228),  // Dark purple
        Color(0xFF2e1f48),  // Mid purple
        Color(0xFF1a2a20)   // Dark green-teal
    )
): Brush {
    return Brush.linearGradient(colors = colors)
}