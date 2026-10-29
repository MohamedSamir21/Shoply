package com.shopping.shoply.products.domain.rules

import com.shopping.shoply.catalog.domain.model.Product

/** The subcategory chips: each non-blank subcategory once, in first-seen order. */
fun subcategoriesOf(products: List<Product>): List<String> =
    products.map { it.subcategory }.filter { it.isNotBlank() }.distinct()

/** Products of [subcategory]; null (the "All" chip) keeps every product. */
fun inSubcategory(products: List<Product>, subcategory: String?): List<Product> =
    if (subcategory == null) products else products.filter { it.subcategory == subcategory }
