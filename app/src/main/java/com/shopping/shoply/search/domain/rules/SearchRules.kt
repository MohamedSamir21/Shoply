package com.shopping.shoply.search.domain.rules

import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.catalog.domain.model.Store

const val MAX_RECENT_SEARCHES = 5

/**
 * Products whose name, subcategory, category, keywords or store name contain [query], ignoring
 * case and a trailing "s" ("plants" finds "Plant"). Keeps the given order; a blank query matches nothing.
 *
 * @param storeNames store id -> store name.
 */
fun matchProducts(products: List<Product>, storeNames: Map<String, String>, query: String): List<Product> {
    val needle = needleOf(query) ?: return emptyList()
    return products.filter { product ->
        listOf(product.name, product.subcategory, product.categoryId, product.keywords, storeNames[product.storeId].orEmpty())
            .any { it.contains(needle, ignoreCase = true) }
    }
}

/** Stores whose name or category contain [query], same matching as [matchProducts]. */
fun matchStores(stores: List<Store>, query: String): List<Store> {
    val needle = needleOf(query) ?: return emptyList()
    return stores.filter { it.name.contains(needle, ignoreCase = true) || it.category.contains(needle, ignoreCase = true) }
}

/** [query] moved to the front of [recent], without case-insensitive duplicates, at most [MAX_RECENT_SEARCHES]. */
fun withRecent(recent: List<String>, query: String): List<String> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return recent
    return (listOf(trimmed) + recent.filterNot { it.equals(trimmed, ignoreCase = true) }).take(MAX_RECENT_SEARCHES)
}

private fun needleOf(query: String): String? =
    query.trim().takeIf { it.isNotEmpty() }?.let { if (it.length > 1) it.removeSuffix("s").removeSuffix("S") else it }
