package com.shopping.shoply.catalog.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.home.presentation.components.ProductCard

/**
 * [products] as a 2-column grid of Home's [ProductCard]s, added as rows to a LazyColumn so the grid
 * scrolls with whatever sits above it. [keyPrefix] keeps keys unique when a list has two grids.
 */
fun LazyListScope.productGrid(
    products: List<Product>,
    wishlist: Set<String>,
    onClick: (Product) -> Unit,
    onToggleWishlist: (Product) -> Unit,
    onQuickAdd: (Product) -> Unit,
    keyPrefix: String = "grid",
) {
    items(
        items = products.chunked(2),
        key = { row -> "$keyPrefix-" + row.joinToString("-") { it.id } },
        contentType = { "productRow" },
    ) { row ->
        Row(
            modifier = Modifier
                .padding(horizontal = ShoplyDimens.gutter)
                .padding(bottom = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(ShoplyDimens.space4),
        ) {
            row.forEach { product ->
                ProductCard(
                    product = product,
                    isSaved = product.id in wishlist,
                    onClick = { onClick(product) },
                    onToggleWishlist = { onToggleWishlist(product) },
                    onQuickAdd = { onQuickAdd(product) },
                    modifier = Modifier.weight(1f),
                    imageHeight = 152.dp,
                )
            }
            // An odd last item keeps half the width instead of stretching.
            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
        }
    }
}
