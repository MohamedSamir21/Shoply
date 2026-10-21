package com.shopping.shoply.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shopping.shoply.catalog.domain.model.CatalogIcon
import com.shopping.shoply.catalog.presentation.toImageVector
import com.shopping.shoply.common.presentation.theme.darkRing

/**
 * The v1 product "photo": the catalog glyph on a soft orange circle, centred on a tinted tile.
 * [modifier] sets the tile's size; the circle is [circleRatio] of the tile's height, and the
 * glyph about half the circle.
 */
@Composable
internal fun ProductImage(
    icon: CatalogIcon,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    circleRatio: Float = 0.62f,
) {
    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        val circle = maxHeight * circleRatio
        Box(
            modifier = Modifier
                .size(circle)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .darkRing(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon.toImageVector(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(circle * 0.54f),
            )
        }
    }
}
