package com.shopping.shoply.home.domain.model

import com.shopping.shoply.catalog.domain.model.Category
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.catalog.domain.model.Store

/** Everything the Home body shows, each list already in display order. */
data class HomeFeed(
    val promos: List<Promo>,
    val categories: List<Category>,
    val featuredStores: List<Store>,
    val popular: List<Product>,
    val recommended: List<Product>,
)
