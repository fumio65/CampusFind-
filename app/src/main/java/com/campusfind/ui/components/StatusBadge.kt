package com.campusfind.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.campusfind.domain.model.ItemStatus

/**
 * FILE: app/src/main/java/com/campusfind/ui/components/StatusBadge.kt
 *
 * Colored badge showing item status.
 *
 * Color scheme (from DEC-012):
 * - LOST  → red background + red text (#FFEBEE bg, #C62828 text)
 * - FOUND → green background + green text (#E8F5E9 bg, #2E7D32 text)
 *
 * Why hardcoded colors instead of MaterialTheme:
 * - These specific colors are part of the brand identity (DEC-012)
 * - They remain consistent across light/dark theme
 * - Status colors should not change based on system theme
 *
 * See: DEC-012 (brand colors), TASK-112, TASK-121
 */
@Composable
fun StatusBadge(
    status: ItemStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status) {
        ItemStatus.LOST -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        ItemStatus.FOUND -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
    }

    Text(
        text = status.name,
        style = MaterialTheme.typography.labelSmall,
        color = textColor,
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
    )
}