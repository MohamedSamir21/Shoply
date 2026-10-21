package com.shopping.shoply.home.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import java.util.Locale

/** "★ 4.6 (128)": orange star, the rating to one decimal, then the review count when given. */
@Composable
internal fun Rating(
    rating: Double,
    reviewCount: Int?,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 12.5.sp,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = ShoplyAppIcons.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(fontSize.value.dp + 2.dp),
        )
        Text(
            text = formatRating(rating),
            style = MaterialTheme.typography.bodySmall,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (reviewCount != null) {
            Text(
                text = "($reviewCount)",
                style = MaterialTheme.typography.bodySmall,
                fontSize = fontSize,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** 4.6 → "4.6", 5.0 → "5.0". */
internal fun formatRating(rating: Double): String = String.format(Locale.US, "%.1f", rating)
