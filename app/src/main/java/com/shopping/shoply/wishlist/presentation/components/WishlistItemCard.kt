package com.shopping.shoply.wishlist.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopping.shoply.R
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyCard
import com.shopping.shoply.common.presentation.theme.shoplyExtra
import com.shopping.shoply.common.presentation.util.formatPrice
import com.shopping.shoply.home.presentation.components.ProductImage
import com.shopping.shoply.home.presentation.preview.HomePreviewData

/**
 * One saved product: 88×96 image, name, filled ♥ (un-save), price (+ old price struck through),
 * and a soft "Move to Cart" button. Tapping the card opens the product.
 */
@Composable
fun WishlistItemCard(
    product: Product,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onMoveToCart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shoplyCard()
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ProductImage(
            icon = product.icon,
            cornerRadius = 12.dp,
            modifier = Modifier.size(width = 88.dp, height = 96.dp),
        )
        Column(modifier = Modifier.weight(1f).height(96.dp).padding(top = 2.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 14.5.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                RemoveButton(onClick = onRemove)
            }
            Row(
                modifier = Modifier.padding(top = ShoplyDimens.space1),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = formatPrice(product.priceCents),
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                product.oldPriceCents?.let { old ->
                    Text(
                        text = formatPrice(old),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        color = MaterialTheme.shoplyExtra.inkFaint,
                        textDecoration = TextDecoration.LineThrough,
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            MoveToCartButton(onClick = onMoveToCart)
        }
    }
}

/** 32 dp tap target around the 20 dp filled orange ♥. */
@Composable
private fun RemoveButton(onClick: () -> Unit) {
    val label = stringResource(R.string.cd_wishlist_remove)
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.TopEnd,
    ) {
        Icon(
            imageVector = ShoplyAppIcons.HeartFilled,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** The design's soft small button: 36 dp, orange tint, 🛍 + "Move to Cart". */
@Composable
private fun MoveToCartButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Row(
            modifier = Modifier
                .height(36.dp)
                .padding(horizontal = ShoplyDimens.space4),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = ShoplyAppIcons.Bag, contentDescription = null, modifier = Modifier.size(17.dp))
            Text(
                text = stringResource(R.string.wishlist_move_to_cart),
                style = MaterialTheme.typography.labelLarge,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/** A discounted product above, a full-price one with a long name below. */
@PreviewLightDark
@Composable
fun WishlistItemCardPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(ShoplyDimens.gutter),
                verticalArrangement = Arrangement.spacedBy(ShoplyDimens.space3),
            ) {
                WishlistItemCard(
                    product = HomePreviewData.headphones,
                    onClick = {},
                    onRemove = {},
                    onMoveToCart = {},
                )
                WishlistItemCard(
                    product = HomePreviewData.linenShirt.copy(name = "Linen Button-Down Shirt in Relaxed Fit"),
                    onClick = {},
                    onRemove = {},
                    onMoveToCart = {},
                )
            }
        }
    }
}
