package com.shopping.shoply.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
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
import com.shopping.shoply.home.domain.rules.discountPercent
import com.shopping.shoply.home.presentation.preview.HomePreviewData

/**
 * Product card used by the Popular rail (144 dp wide, [imageHeight] 140 dp) and the Recommended
 * grid (fills its column, [imageHeight] 152 dp).
 *
 * Shows: icon image, "−18%" badge (only when [discountPercent] isn't null), ♡, name (max 2 lines),
 * "★ 4.6 (128)", price and the struck old price, and [+].
 *
 * Accessibility: the card reads as one node, "{name}, {price}, rated {rating}". ♡ and + stay
 * separate buttons with 48 dp touch targets; ♡ reads "Save to wishlist" or "Remove from wishlist"
 * depending on [isSaved].
 */
@Composable
fun ProductCard(
    product: Product,
    isSaved: Boolean,
    onClick: () -> Unit,
    onToggleWishlist: () -> Unit,
    onQuickAdd: () -> Unit,
    modifier: Modifier = Modifier,
    imageHeight: Dp = 140.dp,
) {
    val price = formatPrice(product.priceCents)
    val summary = stringResource(R.string.home_product_summary, product.name, price, formatRating(product.rating))
    val discount = discountPercent(product.priceCents, product.oldPriceCents)

    Column(
        modifier = modifier
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = summary },
    ) {
        Box {
            ProductImage(
                icon = product.icon,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight),
            )
            if (discount != null) {
                DiscountBadge(
                    percent = discount,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                )
            }
            WishlistButton(
                isSaved = isSaved,
                onClick = onToggleWishlist,
                // The 48 dp touch box overhangs the 34 dp circle by 7 dp, so 1 dp puts the circle 8 dp in.
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(1.dp),
            )
        }

        Column(modifier = Modifier.padding(start = 2.dp, end = 2.dp, top = 10.dp)) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clearAndSetSemantics { },
            )
            Rating(
                rating = product.rating,
                reviewCount = product.reviewCount,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clearAndSetSemantics { },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PriceText(
                    price = price,
                    oldPrice = product.oldPriceCents?.let(::formatPrice),
                    modifier = Modifier
                        .weight(1f)
                        .clearAndSetSemantics { },
                )
                QuickAddButton(onClick = onQuickAdd)
            }
        }
    }
}

/** "−18%" in green on a small white chip. */
@Composable
private fun DiscountBadge(percent: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(24.dp)
            .shoplyCard(RoundedCornerShape(8.dp))
            .padding(horizontal = ShoplyDimens.space2),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "−$percent%",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.shoplyExtra.onGreenSoft,
        )
    }
}

/** 34 dp white circle with the heart, inside a 48 dp touch box. Orange and filled when saved. */
@Composable
private fun WishlistButton(isSaved: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val label = stringResource(if (isSaved) R.string.cd_wishlist_remove else R.string.cd_wishlist_add)
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .shoplyCard(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isSaved) ShoplyAppIcons.HeartFilled else ShoplyAppIcons.Heart,
                contentDescription = null,
                tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/** 32 dp orange square with "+", grown to a 48 dp touch target. */
@Composable
private fun QuickAddButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val label = stringResource(R.string.cd_add_to_cart)
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(32.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = ShoplyAppIcons.Plus,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(18.dp),
        )
    }
}

/** "$89.00" bold, then "$109.00" struck through when there's an old price. */
@Composable
private fun PriceText(price: String, oldPrice: String?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = price,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
        )
        if (oldPrice != null) {
            Text(
                text = oldPrice,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                color = MaterialTheme.shoplyExtra.inkFaint,
                textDecoration = TextDecoration.LineThrough,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        }
    }
}

@PreviewLightDark
@Composable
fun ProductCardPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier.padding(ShoplyDimens.gutter),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Discount + saved
                ProductCard(
                    product = HomePreviewData.headphones,
                    isSaved = true,
                    onClick = {},
                    onToggleWishlist = {},
                    onQuickAdd = {},
                    modifier = Modifier.width(144.dp),
                )
                // No discount + not saved
                ProductCard(
                    product = HomePreviewData.linenShirt,
                    isSaved = false,
                    onClick = {},
                    onToggleWishlist = {},
                    onQuickAdd = {},
                    modifier = Modifier.width(144.dp),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun ProductCardGridPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier.padding(ShoplyDimens.gutter),
                horizontalArrangement = Arrangement.spacedBy(ShoplyDimens.space4),
            ) {
                ProductCard(
                    product = HomePreviewData.linenShirt,
                    isSaved = false,
                    onClick = {},
                    onToggleWishlist = {},
                    onQuickAdd = {},
                    modifier = Modifier.weight(1f),
                    imageHeight = 152.dp,
                )
                ProductCard(
                    product = HomePreviewData.headphones,
                    isSaved = false,
                    onClick = {},
                    onToggleWishlist = {},
                    onQuickAdd = {},
                    modifier = Modifier.weight(1f),
                    imageHeight = 152.dp,
                )
            }
        }
    }
}
