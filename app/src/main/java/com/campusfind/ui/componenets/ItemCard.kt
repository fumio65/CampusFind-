package com.campusfind.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.campusfind.domain.model.LostItem
import java.text.SimpleDateFormat
import java.util.*

/**
 * FILE: app/src/main/java/com/campusfind/ui/components/ItemCard.kt
 *
 * Card component for displaying a lost/found item in the list.
 *
 * Why clickable modifier:
 * - Entire card is tappable → navigates to DetailScreen
 * - More accessible than a tiny "View" button
 * - Standard Material Design pattern
 *
 * Why TextOverflow.Ellipsis:
 * - Long titles/descriptions would overflow the card
 * - Ellipsis shows there's more content → user taps to see full detail
 *
 * Why maxLines:
 * - Title: 1 line max → keeps cards uniform height
 * - Description: 2 lines max → preview without overwhelming the list
 *
 * See: TASK-112, TASK-120 (ItemCard component task)
 */
@Composable
fun ItemCard(
    item: LostItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                StatusBadge(status = item.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description preview
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Timestamp
            Text(
                text = formatTimestamp(item.reportedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Format Unix epoch milliseconds to human-readable date+time.
 *
 * Example output: "Feb 19, 2026 at 02:25 PM"
 */
private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}