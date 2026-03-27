package com.campusfind.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.campusfind.domain.model.ItemStatus
import com.campusfind.domain.model.LostItem
import java.text.SimpleDateFormat
import java.util.*

/**
 * ItemCard - Card component for displaying lost items in a list
 *
 * Features:
 * - Photo thumbnail (if available)
 * - Title + status badge
 * - Description preview (2 lines)
 * - Timestamp
 */
@Composable
fun ItemCard(
    item: LostItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFF0F0EC))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ✅ Photo thumbnail (70x70dp)
            if (item.photoUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(item.photoUri),
                    contentDescription = "Item photo",
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder if no photo
                Surface(
                    modifier = Modifier.size(70.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF4F4F0)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📷", fontSize = 28.sp, color = Color(0xFFCCCCCC))
                    }
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Title + Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1a1a2e),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.width(8.dp))

                    // Status badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (item.status) {
                            ItemStatus.LOST -> Color(0xFFFF4D6D).copy(alpha = 0.15f)
                            ItemStatus.FOUND -> Color(0xFF2DD4A0).copy(alpha = 0.15f)
                        },
                        border = BorderStroke(
                            1.dp,
                            when (item.status) {
                                ItemStatus.LOST -> Color(0xFFFF4D6D)
                                ItemStatus.FOUND -> Color(0xFF2DD4A0)
                            }
                        )
                    ) {
                        Text(
                            item.status.name,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (item.status) {
                                ItemStatus.LOST -> Color(0xFFFF4D6D)
                                ItemStatus.FOUND -> Color(0xFF2DD4A0)
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Description
                Text(
                    item.description,
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                Spacer(Modifier.height(4.dp))

                // Timestamp
                Text(
                    formatTimestamp(item.reportedAt),
                    fontSize = 10.sp,
                    color = Color(0xFFAAAAAA)
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}