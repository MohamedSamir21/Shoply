package com.shopping.shoply.catalog.domain.rules

import com.shopping.shoply.catalog.domain.model.Product

/** Price buckets of the Filters sheet, in cents. Bounds are inclusive except "Under" and "Over". */
enum class PriceRange { ANY, UNDER_25, FROM_25_TO_50, FROM_50_TO_100, OVER_100 }

enum class ProductSort { RELEVANCE, PRICE_LOW_HIGH, PRICE_HIGH_LOW, TOP_RATED }

/**
 * What the Filters sheet and the quick chips choose. The default value filters nothing.
 *
 * @property minRating only products rated at least this; null for any rating.
 * @property categoryIds only these categories; empty for all.
 * @property freeDeliveryOnly only products from stores with no delivery fee.
 */
data class ProductFilters(
    val price: PriceRange = PriceRange.ANY,
    val minRating: Double? = null,
    val categoryIds: Set<String> = emptySet(),
    val freeDeliveryOnly: Boolean = false,
) {
    /** The badge on the Filters chip: how many of the four filters are on. */
    val activeCount: Int
        get() = listOf(price != PriceRange.ANY, minRating != null, categoryIds.isNotEmpty(), freeDeliveryOnly).count { it }
}

/**
 * [products] that pass [filters], ordered by [sort]. [ProductSort.RELEVANCE] keeps the given order.
 *
 * @param freeDeliveryStoreIds ids of stores with no delivery fee, for [ProductFilters.freeDeliveryOnly].
 */
fun applyFilters(
    products: List<Product>,
    filters: ProductFilters,
    sort: ProductSort,
    freeDeliveryStoreIds: Set<String>,
): List<Product> {
    val kept = products.filter { product ->
        product.priceCents in filters.price &&
            (filters.minRating == null || product.rating >= filters.minRating) &&
            (filters.categoryIds.isEmpty() || product.categoryId in filters.categoryIds) &&
            (!filters.freeDeliveryOnly || product.storeId in freeDeliveryStoreIds)
    }
    return when (sort) {
        ProductSort.RELEVANCE -> kept
        ProductSort.PRICE_LOW_HIGH -> kept.sortedBy { it.priceCents }
        ProductSort.PRICE_HIGH_LOW -> kept.sortedByDescending { it.priceCents }
        ProductSort.TOP_RATED -> kept.sortedByDescending { it.rating }
    }
}

private operator fun PriceRange.contains(cents: Long): Boolean = when (this) {
    PriceRange.ANY -> true
    PriceRange.UNDER_25 -> cents < 2500
    PriceRange.FROM_25_TO_50 -> cents in 2500..5000
    PriceRange.FROM_50_TO_100 -> cents in 5000..10000
    PriceRange.OVER_100 -> cents > 10000
}
