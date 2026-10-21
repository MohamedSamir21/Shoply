package com.shopping.shoply.categories.presentation.preview

import com.shopping.shoply.catalog.domain.model.CatalogIcon
import com.shopping.shoply.catalog.domain.model.Category

/**
 * Sample data for the Categories `@Preview`s only (the test builders live in the test source set,
 * which previews can't see). Values match the design's demo content.
 */
internal object CategoriesPreviewData {

    val categories = listOf(
        Category("electronics", "Electronics", CatalogIcon.DEVICE, 128),
        Category("fashion", "Fashion", CatalogIcon.SHIRT, 342),
        Category("home", "Home", CatalogIcon.SOFA, 96),
        Category("beauty", "Beauty", CatalogIcon.SERUM, 74),
        Category("sports", "Sports", CatalogIcon.BALL, 58),
        Category("grocery", "Grocery", CatalogIcon.APPLE, 210),
        Category("books", "Books", CatalogIcon.BOOK, 1),
        Category("toys", "Toys", CatalogIcon.BLOCKS, 45),
    )
}
