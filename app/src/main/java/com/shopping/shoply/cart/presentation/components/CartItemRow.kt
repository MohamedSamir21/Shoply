package com.shopping.shoply.cart.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopping.shoply.R
import com.shopping.shoply.cart.domain.model.CartItem
import com.shopping.shoply.cart.presentation.preview.CartPreviewData
import com.shopping.shoply.common.presentation.components.QuantityStepper
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyShapes
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyCard
import com.shopping.shoply.common.presentation.theme.shoplyExtra
import com.shopping.shoply.common.presentation.util.formatPrice
import com.shopping.shoply.home.presentation.components.ProductImage

/**
 * One cart item: 84×92 image, name, variant ("Sand · M"), line price (price × quantity) and a
 * compact stepper whose − turns into 🗑 at 1. Swiping left reveals red "Remove" and removes it.
 * Tapping the card opens the product.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartItemRow(
    item: CartItem,
    onClick: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val remove by rememberUpdatedState(onRemove)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) remove()
            value == SwipeToDismissBoxValue.EndToStart
        },
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = { RemoveBackground() },
        enableDismissFromStartToEnd = false,
        modifier = modifier.clip(ShoplyShapes.card),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shoplyCard()
                .clickable(onClick = onClick)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ProductImage(
                icon = item.icon,
                cornerRadius = 12.dp,
                modifier = Modifier.size(width = 84.dp, height = 92.dp),
            )
            Column(modifier = Modifier.weight(1f).height(92.dp).padding(top = 2.dp)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val variant = listOfNotNull(item.color, item.size).joinToString(" · ")
                if (variant.isNotEmpty()) {
                    Text(
                        text = variant,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatPrice(item.priceCents * item.quantity),
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    QuantityStepper(
                        value = item.quantity,
                        onDecrease = onDecrease,
                        onIncrease = onIncrease,
                        compact = true,
                        onRemove = onRemove,
                    )
                }
            }
        }
    }
}

/** Red panel behind the row: 🗑 over "Remove", at the end. */
@Composable
private fun RemoveBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.shoplyExtra.red)
            .padding(end = ShoplyDimens.gutter),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(
                imageVector = ShoplyAppIcons.Trash,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(21.dp),
            )
            Text(
                text = stringResource(R.string.cart_remove),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}

/** Colour only (trash at 1), colour + size (quantity 2), and no variant. */
@PreviewLightDark
@Composable
fun CartItemRowPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(ShoplyDimens.gutter),
                verticalArrangement = Arrangement.spacedBy(ShoplyDimens.space3),
            ) {
                CartPreviewData.items.forEach { item ->
                    CartItemRow(
                        item = if (item == CartPreviewData.headphones) item.copy(quantity = 1) else item,
                        onClick = {},
                        onIncrease = {},
                        onDecrease = {},
                        onRemove = {},
                    )
                }
            }
        }
    }
}

/** What a half swipe shows. */
@PreviewLightDark
@Composable
fun CartItemRowRemoveBackgroundPreview() {
    ShoplyTheme {
        Box(modifier = Modifier.padding(ShoplyDimens.gutter).height(112.dp).clip(ShoplyShapes.card)) {
            RemoveBackground()
        }
    }
}
