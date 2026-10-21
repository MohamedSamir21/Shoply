package com.shopping.shoply.catalog.domain.model

/** A product category, e.g. "Electronics". [itemCount] is shown as "7 items". */
data class Category(
    val id: String,
    val name: String,
    val icon: CatalogIcon,
    val itemCount: Int,
)
