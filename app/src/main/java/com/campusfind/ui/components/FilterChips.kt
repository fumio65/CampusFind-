package com.campusfind.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.campusfind.domain.model.ItemStatus

/**
 * FILE: app/src/main/java/com/campusfind/ui/components/FilterChips.kt
 *
 * Reusable filter chip row for Home screen.
 *
 * Why FilterChip (not Button):
 * - Material 3 component specifically designed for filtering
 * - Has built-in selected state styling
 * - Automatically shows selected state with filled background
 *
 * Interaction model:
 * - Tapping active chip deselects it (returns to All)
 * - Only one chip can be selected at a time
 * - "All" is selected when selectedFilter == null
 *
 * See: DEC-012 (Material Design 3), TASK-112, TASK-122
 */
@Composable
fun FilterChips(
    selectedFilter: ItemStatus?,
    onFilterSelected: (ItemStatus?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // All filter
        FilterChip(
            selected = selectedFilter == null,
            onClick = { onFilterSelected(null) },
            label = { Text("All") }
        )

        // Lost filter
        FilterChip(
            selected = selectedFilter == ItemStatus.LOST,
            onClick = {
                onFilterSelected(
                    if (selectedFilter == ItemStatus.LOST) null
                    else ItemStatus.LOST
                )
            },
            label = { Text("Lost") }
        )

        // Found filter
        FilterChip(
            selected = selectedFilter == ItemStatus.FOUND,
            onClick = {
                onFilterSelected(
                    if (selectedFilter == ItemStatus.FOUND) null
                    else ItemStatus.FOUND
                )
            },
            label = { Text("Found") }
        )
    }
}