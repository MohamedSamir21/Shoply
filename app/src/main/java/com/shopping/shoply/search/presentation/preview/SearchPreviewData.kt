package com.shopping.shoply.search.presentation.preview

import com.shopping.shoply.catalog.domain.rules.PriceRange
import com.shopping.shoply.catalog.domain.rules.ProductFilters
import com.shopping.shoply.home.presentation.preview.HomePreviewData
import com.shopping.shoply.search.presentation.SearchCatalog
import com.shopping.shoply.search.presentation.SearchState

/** Sample Search states for previews. */
internal object SearchPreviewData {
    private val stores = listOf(HomePreviewData.voltElectronics, HomePreviewData.fernAndCo)
    private val products = listOf(HomePreviewData.headphones, HomePreviewData.lamp, HomePreviewData.mug)

    val idle = SearchState(
        catalog = SearchCatalog.Ready,
        recent = listOf("Wireless headphones", "Linen shirt", "Desk lamp"),
        stores = stores,
    )

    val typing = idle.copy(
        query = "volt",
        suggestedStores = listOf(HomePreviewData.voltElectronics),
        suggestions = listOf(HomePreviewData.headphones),
    )

    val results = idle.copy(
        query = "home",
        submitted = true,
        matches = products,
        results = products,
        filters = ProductFilters(price = PriceRange.FROM_25_TO_50),
        wishlist = setOf(HomePreviewData.mug.id),
    )
}
