package com.shopping.shoply.store.domain.rules

import com.shopping.shoply.catalog.domain.model.Store

enum class StoreSort { NEAREST, TOP_RATED, FASTEST }

/** Each store category once, in first-seen order (the All Stores chips). */
fun storeCategoriesOf(stores: List<Store>): List<String> = stores.map { it.category }.filter { it.isNotBlank() }.distinct()

/**
 * The All Stores list: stores of [category] (null = all) whose name or category contains [query]
 * (ignoring case; blank = all), ordered by [sort]. Fastest compares the shortest delivery time.
 */
fun storeList(stores: List<Store>, category: String?, query: String, sort: StoreSort): List<Store> {
    val needle = query.trim()
    val kept = stores.filter { store ->
        (category == null || store.category == category) &&
            (needle.isEmpty() || store.name.contains(needle, ignoreCase = true) || store.category.contains(needle, ignoreCase = true))
    }
    return when (sort) {
        StoreSort.NEAREST -> kept.sortedBy { it.distanceMiles }
        StoreSort.TOP_RATED -> kept.sortedByDescending { it.rating }
        StoreSort.FASTEST -> kept.sortedBy { it.etaMinutes.first }
    }
}
